package mekong.ditagis.com.qlts.async

import android.R
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.DialogInterface
import android.graphics.Point
import android.os.AsyncTask
import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.data.ArcGISFeature
import com.esri.arcgisruntime.data.ArcGISFeatureTable
import com.esri.arcgisruntime.mapping.GeoElement
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult
import com.esri.arcgisruntime.mapping.view.MapView
import mekong.ditagis.com.qlts.MainActivity
import mekong.ditagis.com.qlts.entities.DFeatureLayer
import mekong.ditagis.com.qlts.utities.DApplication
import mekong.ditagis.com.qlts.utities.Popup
import java.util.concurrent.ExecutionException

/**
 * Created by ThanLe on 4/16/2018.
 */
class SingleTapMapViewAsync constructor(activity: MainActivity,  @field:SuppressLint("StaticFieldLeak") private val mPopUp: Popup,
                                        private val mClickPoint: Point, @field:SuppressLint("StaticFieldLeak") private val mMapView: MapView) : AsyncTask<com.esri.arcgisruntime.geometry.Point?, ArcGISFeature?, Void?>() {
    private val mDialog: ProgressDialog?

    @SuppressLint("StaticFieldLeak")
    private val mActivity: Activity

    private var mSelectedArcGISFeature: ArcGISFeature? = null

    private var isFound: Boolean = false
    private val mApplication: DApplication
    override fun doInBackground(vararg points: com.esri.arcgisruntime.geometry.Point?): Void? {
        val listListenableFuture: ListenableFuture<List<IdentifyLayerResult>> = mMapView
                .identifyLayersAsync(mClickPoint, 5.0, false)
        listListenableFuture.addDoneListener {
            val identifyLayerResults: List<IdentifyLayerResult>
            try {
                identifyLayerResults = listListenableFuture.get()
                for (identifyLayerResult: IdentifyLayerResult in identifyLayerResults) {
                    run {
                        val elements: List<GeoElement> = identifyLayerResult.elements
                        if ((elements.isNotEmpty()) && elements[0] is ArcGISFeature && !isFound) {
                            isFound = true
                            mSelectedArcGISFeature = elements[0] as ArcGISFeature?
                            publishProgress(mSelectedArcGISFeature)
                        }
                    }
                }
                publishProgress()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: ExecutionException) {
                e.printStackTrace()
            }
        }
        return null
    }


    override fun onPreExecute() {
        super.onPreExecute()
        mDialog!!.setMessage("Đang xử lý...")
        mDialog.setCancelable(false)
        mDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Hủy", DialogInterface.OnClickListener({ dialogInterface: DialogInterface?, i: Int -> publishProgress() }))
        mDialog.show()
    }

    override fun onProgressUpdate(vararg values: ArcGISFeature?) {
        super.onProgressUpdate(*values)
        if ((values.isNotEmpty()) && (mSelectedArcGISFeature != null)) {
//            HoSoVatTuSuCoAsync hoSoVatTuSuCoAsync = new HoSoVatTuSuCoAsync(mActivity, object -> {
//                if (object != null) {
            mApplication.selectedFeature = mSelectedArcGISFeature
            mPopUp.showPopup(mSelectedArcGISFeature!!)
        }
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss()
        }
        //            });
//            hoSoVatTuSuCoAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Constant.HOSOVATTUSUCO_METHOD.FIND, mSelectedArcGISFeature.getAttributes()
//                    .get(mActivity.getString(R.string.Field_SuCo_IDSuCo)));
    }

    init {
        mActivity = activity
        mApplication = activity.getApplication() as DApplication
        mDialog = ProgressDialog(activity, R.style.Theme_Material_Dialog_Alert)
    }
}