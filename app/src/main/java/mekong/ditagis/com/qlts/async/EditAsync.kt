package mekong.ditagis.com.qlts.async

import android.annotation.SuppressLint
import android.app.Activity
import android.os.AsyncTask
import android.util.Log
import android.view.View
import com.esri.arcgisruntime.data.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.layout_progress_dialog.view.*
import mekong.ditagis.com.qlts.R
import mekong.ditagis.com.qlts.utities.Constant
import mekong.ditagis.com.qlts.utities.DApplication
import java.util.*
import java.util.concurrent.ExecutionException

/**
 * Created by ThanLe on 4/16/2018.
 */
@SuppressLint("StaticFieldLeak")
class EditAsync(private val mView: View, private val mActivity: Activity,
                private val mServiceFeatureTable: ServiceFeatureTable,
                selectedArcGISFeature: ArcGISFeature,
                private val mDelegate: AsyncResponse) : AsyncTask<HashMap<String, Any>, Boolean, Void>() {
    private lateinit var mDialog: BottomSheetDialog
    private var mSelectedArcGISFeature: ArcGISFeature = selectedArcGISFeature
    private val mApplication: DApplication = mActivity.application as DApplication

    interface AsyncResponse {
        fun processFinish(feature: Boolean?)
    }

    @SuppressLint("SetTextI18n")
    override fun onPreExecute() {
        super.onPreExecute()
        mDialog = BottomSheetDialog(this.mActivity)
        val layoutView = mActivity.layoutInflater.inflate(R.layout.layout_progress_dialog, null)
        layoutView.txtProgressDialogTitle.text = "Đang cập nhật thông tin..."
        mDialog!!.setContentView(layoutView)
        mDialog.setCancelable(false)

        mDialog.show()

    }

    override fun doInBackground(vararg params: HashMap<String, Any>): Void? {

        if (params.isNotEmpty()) {
            val attributes = params[0]
            for (fieldName in attributes.keys) {
                try {
                    val value = attributes[fieldName]
                    if (value == null)
                        mSelectedArcGISFeature.attributes[fieldName] = null
                    else {
                        val valueString = value.toString().trim { it <= ' ' }
                        val field = mServiceFeatureTable.getField(fieldName)
                        when (field.fieldType) {
                            Field.Type.TEXT -> mSelectedArcGISFeature.attributes[fieldName] = valueString
                            Field.Type.DOUBLE -> {
                                mSelectedArcGISFeature.attributes[fieldName] = java.lang.Double.parseDouble(valueString)
                            }
                            Field.Type.FLOAT -> {
                                mSelectedArcGISFeature.attributes[fieldName] = java.lang.Float.parseFloat(valueString)
                            }
                            Field.Type.INTEGER -> {
                                mSelectedArcGISFeature.attributes[fieldName] = Integer.parseInt(valueString)
                            }
                            Field.Type.SHORT -> mSelectedArcGISFeature.attributes[fieldName] = java.lang.Short.parseShort(valueString)
                            Field.Type.DATE -> {
                                val calendar = Calendar.getInstance()
                                calendar.time = Constant.DATE_FORMAT.parse(valueString)
                                mSelectedArcGISFeature.attributes[fieldName] = calendar
                            }
                            else -> {
                            }
                        }
                    }
                    when (fieldName) {
                        Constant.Field.CREATED_DATE, Constant.Field.LAST_EDITED_DATE,
                        Constant.Field.NGAY_CAP_NHAT -> mSelectedArcGISFeature.attributes[fieldName] = Calendar.getInstance()
                        Constant.Field.CREATED_USER, Constant.Field.LAST_EDITED_USER,
                        Constant.Field.NGUOI_CAP_NHAT
                        -> mSelectedArcGISFeature.attributes[fieldName] = mApplication.user!!.username

                    }

                } catch (e: Exception) {
                    mSelectedArcGISFeature.attributes[fieldName] = null
                    Log.e("Lỗi thêm điểm", e.toString())

                }
            }
        }
        val voidListenableFuture = mServiceFeatureTable.updateFeatureAsync(mSelectedArcGISFeature)
        voidListenableFuture.addDoneListener {
            try {
                voidListenableFuture.get()
                val listListenableFuture = mServiceFeatureTable.applyEditsAsync()
                listListenableFuture.addDoneListener {
                    try {
                        val featureEditResults = listListenableFuture.get()
                        if (featureEditResults.size > 0) {
                            if (!featureEditResults[0].hasCompletedWithErrors()) {
                                publishProgress(true)
                            } else {
                                publishProgress()
                            }
                        } else {
                            publishProgress()
                        }
                    } catch (e: InterruptedException) {
                        publishProgress()
                        e.printStackTrace()
                    } catch (e: ExecutionException) {
                        publishProgress()
                        e.printStackTrace()
                    }


                }
            } catch (e: InterruptedException) {
                publishProgress()
                e.printStackTrace()
            } catch (e: ExecutionException) {
                publishProgress()
                e.printStackTrace()
            }
        }
        return null
    }

    private fun notifyError() {
        publishProgress()
        Snackbar.make(mView, "Đã xảy ra lỗi", Snackbar.LENGTH_SHORT).show()
    }


    override fun onProgressUpdate(vararg values: Boolean?) {
        super.onProgressUpdate(*values)
        if (values[0] != null) {
            mDelegate.processFinish(true)
        } else {
            notifyError()
            mDelegate.processFinish(false)
        }
        if (mDialog.isShowing) {
            mDialog.dismiss()
        }
    }


}
