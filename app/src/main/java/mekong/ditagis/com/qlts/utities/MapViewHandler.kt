package mekong.ditagis.com.qlts.utities

import android.annotation.SuppressLint
import android.app.Activity
import android.view.MotionEvent
import android.widget.ListView
import android.widget.Toast
import com.esri.arcgisruntime.data.*
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.tasks.geocode.LocatorTask
import mekong.ditagis.com.qlts.MainActivity
import mekong.ditagis.com.qlts.adapter.ObjectsAdapter
import mekong.ditagis.com.qlts.async.QuerySearchAsycn
import mekong.ditagis.com.qlts.async.SingleTapMapViewAsync
import java.util.concurrent.ExecutionException
import kotlin.math.roundToInt


/**
 * Created by ThanLe on 2/2/2018.
 */

class MapViewHandler(private val mMapView: MapView, private val mActivity: MainActivity) : Activity() {
    internal var loc = LocatorTask("http://geocode.arcgis.com/arcgis/rest/services/World/GeocodeServer")
    private var mClickPoint: android.graphics.Point? = null
    private var mPopUp: DCallout? = null
    private var mApplication: DApplication = mActivity.application as DApplication
    private lateinit var mFeatureLayer: FeatureLayer

    fun setmPopUp(mPopUp: DCallout) {
        this.mPopUp = mPopUp
    }

    fun setIdentifyFeatureLayer(featureLayer: FeatureLayer) {
       mFeatureLayer = featureLayer
    }

    fun onSingleTapMapView(e: MotionEvent) {
//        if (mApplication.selectedFeatureLayer == null) {
//            Toast.makeText(this.mActivity, "Vui lòng chọn lớp thao tác bản đồ", Toast.LENGTH_LONG).show()
//            mActivity.showDialogSelectLayer()
//            return
//        }
        mClickPoint = android.graphics.Point(e.x.toInt(), e.y.toInt())
//        mApplication.progressDialog.changeTitle(mActivity, mActivity.mBinding.drawerLayout, "Đang xác định đối tượng...")
        val singleTapMapViewAsync = SingleTapMapViewAsync(mActivity,  mPopUp!!,mClickPoint!!, mMapView)
        singleTapMapViewAsync.execute()
    }


    fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): DoubleArray? {
        val targetGeometry = mMapView.getCurrentViewpoint(Viewpoint.Type.CENTER_AND_SCALE).targetGeometry
        if (targetGeometry != null) {
            val center = targetGeometry.extent.center
            val project = GeometryEngine.project(center, SpatialReferences.getWgs84())

            //        Geometry geometry = GeometryEngine.project(project, SpatialReferences.getWebMercator());
            return doubleArrayOf(project.extent.center.x, project.extent.center.y)
        }
        return null
    }

    fun queryByObjectID(objectID: Int) {
        val queryParameters = QueryParameters()
        val query = "OBJECTID = $objectID"
        queryParameters.whereClause = query

        val feature = (mFeatureLayer.featureTable as ServiceFeatureTable)
                .queryFeaturesAsync(queryParameters, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL)
        feature.addDoneListener {
            try {
                val result = feature.get()
                if (result.iterator().hasNext()) {
                    mApplication.selectedFeature = result.iterator().next() as ArcGISFeature
                    if (mApplication.selectedFeature != null) {
                        mPopUp!!.showPopup(mApplication.selectedFeature!! as ArcGISFeature)
                    }
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: ExecutionException) {
                e.printStackTrace()
            }
        }

    }

    fun querySearch(searchStr: String, listView: ListView, adapter: ObjectsAdapter) {
        mApplication.progressDialog.changeTitle(mActivity, mActivity.mBinding.drawerLayout, "Đang tìm kiếm...")
        QuerySearchAsycn(mFeatureLayer.featureTable as ServiceFeatureTable, object : QuerySearchAsycn.AsyncResponse {
            override fun processFinish(output: List<ObjectsAdapter.Item>?) {
                mApplication.progressDialog.dismiss()
                if (output != null && output.isNotEmpty()) {
                    adapter.clear()
                    adapter.addAll(output)
                    adapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(mMapView.context, "Không tìm thấy thông tin!", Toast.LENGTH_SHORT).show()
                }

            }
        }).execute(searchStr)
    }
}