package mekong.ditagis.com.qlts.async

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatSpinner
import com.esri.arcgisruntime.data.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.layout_progress_dialog.view.*
import mekong.ditagis.com.qlts.R
import mekong.ditagis.com.qlts.utities.Constant
import mekong.ditagis.com.qlts.utities.DApplication
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


/**
 * Created by ThanLe on 4/16/2018.
 */
class AddFeatureTask(private val delegate: Response) {
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var mDialog: BottomSheetDialog

    interface Response {
        fun post(output: Feature?)
    }

    fun execute(activity: Activity, application: DApplication, layoutField: LinearLayout, feature: Feature) {
        val serviceFeatureTable = feature.featureTable as ServiceFeatureTable
        val attributes = getAttributes(layoutField, serviceFeatureTable)
        preExecute(activity)
        executor.execute {
            try {
                feature.geometry = application.center
                for (field in serviceFeatureTable.fields) {
                    try {
                        val value = attributes!![field.name].toString().trim { it <= ' ' }
                        if (value.isEmpty()) continue
                        when (field.fieldType) {
                            Field.Type.TEXT -> feature.attributes[field.name] = value
                            Field.Type.DOUBLE -> feature.attributes[field.name] = value.toDouble()
                            Field.Type.FLOAT -> feature.attributes[field.name] = value.toFloat()
                            Field.Type.INTEGER -> feature.attributes[field.name] = value.toInt()
                            Field.Type.SHORT -> feature.attributes[field.name] = value.toShort()
                        }
                    } catch (e: Exception) {
                        Log.e("Lỗi thêm điểm", e.toString())
                    }
                    when (field.name) {
                        Constant.Field.CREATED_DATE, Constant.Field.LAST_EDITED_DATE,
                        Constant.Field.NGAY_CAP_NHAT -> feature.attributes[field.name] = Calendar.getInstance()
                        Constant.Field.CREATED_USER, Constant.Field.LAST_EDITED_USER,
                        Constant.Field.NGUOI_CAP_NHAT -> feature.attributes[field.name] = application.user!!.username
                    }
                }
                var queryParameters = QueryParameters()
                queryParameters.geometry = application.center
                var listenableFuture = application.SFTAdministrator!!.queryFeaturesAsync(queryParameters, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL)
                listenableFuture.addDoneListener {
                    try {
                        var featureQueryResult = listenableFuture.get()
                        val iterator = featureQueryResult.iterator()

                        while (iterator.hasNext()) {
                            val featureHanhChinh = iterator.next() as Feature
                            for (field in serviceFeatureTable.fields) {
                                when (field.name) {
                                    Constant.Field.MA_HUYEN -> feature.attributes[field.name] = featureHanhChinh.attributes[
                                            application.appInfo!!.config.MaHuyen]
                                    Constant.Field.MA_XA -> feature.attributes[field.name] = featureHanhChinh.attributes[
                                            application.appInfo!!.config.IDHanhChinh]
                                }
                            }
                        }
                        addFeatureAsync(feature, serviceFeatureTable, application)
                    } catch (e: Exception) {
                        addFeatureAsync(feature, serviceFeatureTable, application)
                    }
                }
//                addFeatureAsync(feature, serviceFeatureTable, application)
            } catch (e: Exception) {
                postExecute()
            }
        }
    }

    private fun preExecute(activity: Activity) {
        mDialog = BottomSheetDialog(activity)
        val layoutView = activity.layoutInflater.inflate(R.layout.layout_progress_dialog, null)
        layoutView.txtProgressDialogTitle.text = "Đang lưu..."
        mDialog!!.setContentView(layoutView)
        mDialog.setCancelable(false)

        mDialog.show()
    }

    private fun postExecute(vararg values: Feature?) {
        handler.post {
            if (mDialog.isShowing)
                mDialog.dismiss()
            if (values == null || values.isEmpty()) {
                delegate.post(null)
            } else if (values.isNotEmpty()) delegate.post(values[0])
        }

    }


    private fun getCodeDomain(codedValues: List<CodedValue>, value: String): Any? {
        var code: Any? = null
        for (codedValue in codedValues) {
            if (codedValue.name == value) {
                code = codedValue.code
                break
            }
        }
        return code
    }

    private fun addFeatureAsync(feature: Feature, serviceFeatureTable: ServiceFeatureTable, application: DApplication) {
        val mapViewResult = serviceFeatureTable.addFeatureAsync(feature)
        mapViewResult.addDoneListener {
            val listListenableEditAsync = serviceFeatureTable.applyEditsAsync()
            listListenableEditAsync.addDoneListener {
                try {
                    val edits = listListenableEditAsync.get()
                    if (edits != null && edits.size > 0) {
                        if (!edits[0].hasCompletedWithErrors()) {
                            val objectId = edits[0].objectId
                            val queryParameters = QueryParameters()
                            val query = String.format("%s = %d", Constant.Field.OBJECTID, objectId)
                            queryParameters.whereClause = query
                            val featuresAsync = serviceFeatureTable.queryFeaturesAsync(queryParameters, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL)
                            featuresAsync.addDoneListener {
                                try {
                                    val result = featuresAsync.get()
                                    if (result.iterator().hasNext()) {
                                        val item = result.iterator().next()
                                        application.objectIDAddFeature = objectId
                                        if (application.images != null && application.images!!.isNotEmpty())
                                            addAttachment(item as ArcGISFeature, item, serviceFeatureTable, application)
                                        else postExecute(item)
                                    } else postExecute()
                                } catch (e: InterruptedException) {
                                    postExecute()
                                } catch (e: ExecutionException) {
                                    postExecute()
                                }
                            }
                        } else {
                            postExecute()
                        }
                    }
                } catch (e: InterruptedException) {
                    postExecute()
                } catch (e: ExecutionException) {
                    postExecute()
                }
            }
        }
    }

    //        try {
    //        } catch (Exception e) {
//            Log.e("Lỗi lấy attributes", e.toString());
//        }

    private fun getAttributes(layoutField: LinearLayout, serviceFeatureTable: ServiceFeatureTable): HashMap<String, Any?> {
        val attributes = HashMap<String, Any?>()
        var currentFieldName: String
        var hasValue = false
        for (i in 0 until layoutField.childCount) {
            val viewI = layoutField.getChildAt(i) as LinearLayout
            for (j in 0 until viewI.childCount) {
                try {
                    val viewJ = viewI.getChildAt(j) as TextInputLayout
                    if (viewJ.visibility == View.VISIBLE
                            && viewJ.hint != null) {
                        val fieldName = viewJ.tag.toString()
                        val field = serviceFeatureTable.getField(fieldName)
                        currentFieldName = fieldName
                        if (currentFieldName.isEmpty()) continue
                        for (k in 0 until viewJ.childCount) {
                            val viewK = viewJ.getChildAt(k)
                            if (viewK is FrameLayout) {
                                for (l in 0 until viewK.childCount) {
                                    val viewL = viewK.getChildAt(l)
                                    if (viewL is TextInputEditText) {
                                        if (field.domain != null) {
                                            val codedValues = (field.domain as CodedValueDomain).codedValues

                                            val valueDomain = getCodeDomain(codedValues, viewL.text.toString())
                                            if (valueDomain != null) {
                                                attributes[currentFieldName] = valueDomain.toString()
                                                hasValue = true
                                            }
                                        } else {
                                            attributes[currentFieldName] = viewL.text.toString()
                                            hasValue = true
                                        }

                                    }
                                }
                            } else if (viewK is AppCompatSpinner) {
                                if (field.domain != null) {
                                    val codedValues = (field.domain as CodedValueDomain).codedValues
                                    val valueDomain = getCodeDomain(codedValues, viewK.selectedItem.toString())
                                    if (valueDomain != null) {
                                        attributes[currentFieldName] = valueDomain.toString()
                                        hasValue = true
                                    }
                                }

                            }
                        }

                    }
                } catch (e: Exception) {

                }
            }
        }
        if (!hasValue) postExecute()
        return attributes
    }


    private fun addAttachment(arcGISFeature: ArcGISFeature, feature: Feature,
                              serviceFeatureTable: ServiceFeatureTable, application: DApplication) {
        for (image in application.images!!) {
            val attachmentName = String.format(Constant.AttachmentName.UPDATE,
                    application.user!!.username, System.currentTimeMillis())
            val addResult = arcGISFeature.addAttachmentAsync(
                    image, Constant.FileType.PNG, attachmentName)
        }
        val tableResult = serviceFeatureTable.updateFeatureAsync(arcGISFeature)
        //            tableResult.addDoneListener(() -> {
        val updatedServerResult = serviceFeatureTable.applyEditsAsync()
        updatedServerResult.addDoneListener {
            try {
                val edits = updatedServerResult.get()
                if (edits.size > 0) {
                    if (!edits[0].hasCompletedWithErrors()) {
                        postExecute(feature)
                    } else postExecute()
                } else postExecute()
            } catch (e: InterruptedException) {
                postExecute()
            } catch (e: ExecutionException) {
                postExecute()
            }
        }
    }
}