package mekong.ditagis.com.qlts.async

import android.app.Activity
import android.app.ProgressDialog
import android.os.AsyncTask
import com.esri.arcgisruntime.data.*
import com.esri.arcgisruntime.symbology.UniqueValueRenderer
import mekong.ditagis.com.qlts.R
import mekong.ditagis.com.qlts.adapter.FeatureViewMoreInfoAdapter
import mekong.ditagis.com.qlts.utities.Constant
import mekong.ditagis.com.qlts.utities.DApplication
import java.text.ParseException
import java.util.*

/**
 * Created by ThanLe on 4/16/2018.
 */

class EditAsync(private val mMainActivity: Activity, private val mServiceFeatureTable: ServiceFeatureTable,
                selectedArcGISFeature: ArcGISFeature, private val delegate: AsyncResponse)
    : AsyncTask<FeatureViewMoreInfoAdapter, Boolean, Void>() {

    private val mDialog: ProgressDialog
    private var mSelectedArcGISFeature: ArcGISFeature? = null
    private val mDApplication: DApplication

    interface AsyncResponse {
        fun processFinish(isSuccess: Boolean)
    }

    init {
        mSelectedArcGISFeature = selectedArcGISFeature
        mDialog = ProgressDialog(mMainActivity, android.R.style.Theme_Material_Dialog_Alert)
        this.mDApplication = mMainActivity.application as DApplication
    }

    override fun onPreExecute() {
        super.onPreExecute()
        mDialog.setMessage(mMainActivity.getString(R.string.async_dang_xu_ly))
        mDialog.setCancelable(false)
        mDialog.setButton("Hủy") { dialogInterface, i -> publishProgress() }
        mDialog.show()

    }

    override fun doInBackground(vararg params: FeatureViewMoreInfoAdapter): Void? {
        val adapter = params[0]
        val renderer = mSelectedArcGISFeature!!.featureTable.layerInfo.drawingInfo.renderer
        var uniqueValues: List<UniqueValueRenderer.UniqueValue>? = null
        var fieldName: String? = null
        if (renderer is UniqueValueRenderer) {
            uniqueValues = renderer.uniqueValues
            fieldName = renderer.fieldNames[0]
        }
        try {
            for (item in adapter.getItems()) {
                if (item.value == null || !item.isEdit) continue
                val domain = mSelectedArcGISFeature!!.featureTable.getField(item.fieldName!!).domain
                var codeDomain: Any? = null
                var valueUniqueRenderer: Any? = null
                if (domain != null) {
                    val codedValues = (this.mSelectedArcGISFeature!!.featureTable.getField(item.fieldName!!).domain as CodedValueDomain).codedValues
                    codeDomain = getCodeDomain(codedValues, item.value)
                }
                if (uniqueValues != null && uniqueValues.size > 0 && item.fieldName == fieldName) {
                    valueUniqueRenderer = getValueUniqueRenderer(uniqueValues, item.value)
                }
                when (item.fieldType) {
                    Field.Type.DATE -> {
                        var date: Date? = null
                        try {
                            date = Constant.DATE_FORMAT.parse(item.value)
                            val c = Calendar.getInstance()
                            c.time = date
                            mSelectedArcGISFeature!!.attributes[item.fieldName] = c
                        } catch (e: ParseException) {
                        }

                    }

                    Field.Type.TEXT -> if (valueUniqueRenderer != null) {
                        mSelectedArcGISFeature!!.attributes[item.fieldName] = valueUniqueRenderer.toString()
                    } else if (codeDomain != null) {
                        mSelectedArcGISFeature!!.attributes[item.fieldName] = codeDomain.toString()
                    } else
                        mSelectedArcGISFeature!!.attributes[item.fieldName] = item.value
                    Field.Type.DOUBLE -> mSelectedArcGISFeature!!.attributes[item.fieldName] = java.lang.Double.parseDouble(item.value!!)
                    Field.Type.FLOAT -> mSelectedArcGISFeature!!.attributes[item.fieldName] = java.lang.Float.parseFloat(item.value!!)
                    Field.Type.INTEGER -> if (valueUniqueRenderer != null) {
                        mSelectedArcGISFeature!!.attributes[item.fieldName] = Integer.parseInt(valueUniqueRenderer.toString())
                    } else if (codeDomain != null) {
                        mSelectedArcGISFeature!!.attributes[item.fieldName] = Integer.parseInt(codeDomain.toString())
                    } else
                        mSelectedArcGISFeature!!.attributes[item.fieldName] = Integer.parseInt(item.value!!)
                    Field.Type.SHORT -> if (valueUniqueRenderer != null) {
                        mSelectedArcGISFeature!!.attributes[item.fieldName] = java.lang.Short.parseShort(valueUniqueRenderer.toString())
                    } else if (codeDomain != null) {
                        mSelectedArcGISFeature!!.attributes[item.fieldName] = java.lang.Short.parseShort(codeDomain.toString())
                    } else
                        mSelectedArcGISFeature!!.attributes[item.fieldName] = java.lang.Short.parseShort(item.value)
                }
            }
        } catch (e: Exception) {
            publishProgress(false)
        }

        val fields = mSelectedArcGISFeature!!.featureTable.fields
        for (field in fields) {
            if (field.name.toUpperCase() == mMainActivity.resources.getString(R.string.NGAYCAPNHAT)) {
                val currentTime = Calendar.getInstance()
                mSelectedArcGISFeature!!.attributes[field.name] = currentTime
            }
            if (field.name.toUpperCase() == mMainActivity.resources.getString(R.string.NGUOICAPNHAT)) {
                val username = this.mDApplication.user!!.username
                mSelectedArcGISFeature!!.attributes[field.name] = username
            }
        }


        mServiceFeatureTable.loadAsync()
        mServiceFeatureTable.addDoneLoadingListener {
            try {
                updateFeature(mSelectedArcGISFeature)
            } catch (e: Exception) {
                publishProgress(false)
            }
        }
        return null
    }

    private fun updateFeature(feature: Feature?) {
        val mapViewResult = mServiceFeatureTable.updateFeatureAsync(feature!!)
        mapViewResult.addDoneListener {
            val listListenableEditAsync = mServiceFeatureTable.applyEditsAsync()
            listListenableEditAsync.addDoneListener {
                try {
                    mDialog.dismiss()
                    val featureEditResults = listListenableEditAsync.get()
                    if (featureEditResults.size > 0) {
                        val objectId = featureEditResults[0].objectId
                        publishProgress(true)
                    } else publishProgress(false)

                } catch (e: Exception) {
                    publishProgress(false)
                }
            }
        }
    }


    private fun getIdFeatureTypes(featureTypes: List<FeatureType>, value: String): Any? {
        var code: Any? = null
        for (featureType in featureTypes) {
            if (featureType.name == value) {
                code = featureType.id
                break
            }
        }
        return code
    }

    private fun getValueUniqueRenderer(uniqueValues: List<UniqueValueRenderer.UniqueValue>, label: String?): Any? {
        var value: Any? = null
        for (uniqueValue in uniqueValues) {
            if (uniqueValue.label != null && uniqueValue.label.toString() == label) {
                value = uniqueValue.values[0].toString()
                break
            }
        }
        return value
    }

    private fun getCodeDomain(codedValues: List<CodedValue>, value: String?): Any? {
        var code: Any? = null
        for (codedValue in codedValues) {
            if (codedValue.name == value) {
                code = codedValue.code
                break
            }

        }
        return code
    }

    override fun onProgressUpdate(vararg values: Boolean?) {
        super.onProgressUpdate(*values)
        if (values.isNotEmpty())
            delegate!!.processFinish(values.first()!!)
        else
            delegate.processFinish(false)

    }


}

