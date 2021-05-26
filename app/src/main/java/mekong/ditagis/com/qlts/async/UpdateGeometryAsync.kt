package mekong.ditagis.com.qlts.async

import android.annotation.SuppressLint
import android.app.Activity
import android.os.AsyncTask
import android.util.Log
import android.view.View
import com.esri.arcgisruntime.data.*
import com.esri.arcgisruntime.geometry.Point
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
class UpdateGeometryAsync(private val mView: View, private val mActivity: Activity,
                          private val mServiceFeatureTable: ServiceFeatureTable,
                          selectedArcGISFeature: ArcGISFeature,
                          private val mDelegate: AsyncResponse) : AsyncTask<Point, Boolean, Void>() {
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

    override fun doInBackground(vararg params: Point): Void? {

        if (params.isNotEmpty()) {
            mSelectedArcGISFeature.geometry = params[0]
            var queryParameters = QueryParameters()
            queryParameters.geometry = params[0]
            var listenableFuture = mApplication.SFTAdministrator!!.queryFeaturesAsync(queryParameters, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL)
            listenableFuture.addDoneListener {
                try {
                    var featureQueryResult = listenableFuture.get()
                    val iterator = featureQueryResult.iterator()

                    while (iterator.hasNext()) {
                        val featureHanhChinh = iterator.next() as Feature
                        for (field in mServiceFeatureTable.fields) {
                            when (field.name) {
                                Constant.Field.MA_HUYEN -> mSelectedArcGISFeature.attributes[field.name] = featureHanhChinh.attributes[
                                        mApplication.appInfo!!.config.MaHuyen]
                                Constant.Field.MA_XA -> mSelectedArcGISFeature.attributes[field.name] = featureHanhChinh.attributes[
                                        mApplication.appInfo!!.config.IDHanhChinh]
                            }
                        }
                    }
                    applyEdit()
                } catch (e: Exception) {
                    applyEdit()
                }
            }
        }

        return null
    }
    private fun applyEdit(){
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
