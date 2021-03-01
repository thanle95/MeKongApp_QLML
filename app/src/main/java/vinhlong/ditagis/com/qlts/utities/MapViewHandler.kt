package vinhlong.ditagis.com.qlts.utities

import android.app.Activity
import android.view.MotionEvent
import android.widget.ListView
import android.widget.Toast
import com.esri.arcgisruntime.data.ArcGISFeature
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.tasks.geocode.LocatorTask
import kotlinx.android.synthetic.main.activity_quan_ly_tai_san.*
import vinhlong.ditagis.com.qlts.MainActivity
import vinhlong.ditagis.com.qlts.adapter.ObjectsAdapter
import vinhlong.ditagis.com.qlts.async.QuerySearchAsycn
import vinhlong.ditagis.com.qlts.async.SingleTapAddFeatureAsync
import vinhlong.ditagis.com.qlts.async.SingleTapMapViewAsync
import vinhlong.ditagis.com.qlts.libs.FeatureLayerDTG
import java.util.concurrent.ExecutionException


/**
 * Created by ThanLe on 2/2/2018.
 */

class MapViewHandler(private val mMapView: MapView, private val mActivity: MainActivity) : Activity() {
    internal var loc = LocatorTask("http://geocode.arcgis.com/arcgis/rest/services/World/GeocodeServer")
    private var identifyFeatureLayer: FeatureLayer? = null
    private var mClickPoint: android.graphics.Point? = null
    private var mSelectedArcGISFeature: ArcGISFeature? = null
    private var searchSFT: ServiceFeatureTable? = null
    private var addSFT: ServiceFeatureTable? = null
    private var mPopUp: Popup? = null
    private var mApplication: DApplication = mActivity.application as DApplication
    private var mFeatureLayerDTGs: List<FeatureLayerDTG>? = null

    fun setFeatureLayerDTGs(mFeatureLayerDTGs: List<FeatureLayerDTG>) {
        this.mFeatureLayerDTGs = mFeatureLayerDTGs
    }

    fun setmPopUp(mPopUp: Popup) {
        this.mPopUp = mPopUp
    }

    fun setSearchSFT(searchSFT: ServiceFeatureTable) {
        this.searchSFT = searchSFT
    }

    fun setAddSFT(addSFT: ServiceFeatureTable) {
        this.addSFT = addSFT
    }

    fun setIdentifyFeatureLayer(identifyFeatureLayer: FeatureLayer) {
        this.identifyFeatureLayer = identifyFeatureLayer
    }

    private fun getFeatureLayerDTG(featureLayer: FeatureLayer?): FeatureLayerDTG? {
        for (featureLayerDTG in mFeatureLayerDTGs!!) {

            if (featureLayerDTG.featureLayer.id == featureLayer!!.id)
                return featureLayerDTG
        }
        return null
    }

    fun addFeature() {
        val singleTapAdddFeatureAsync = SingleTapAddFeatureAsync(mActivity, addSFT!!,
                mMapView,object: SingleTapAddFeatureAsync.AsyncResponse {
            override fun processFinish(feature: Feature?) {
                if (feature != null) {
                    try {
                        mPopUp!!.setFeatureLayerDTG(getFeatureLayerDTG(identifyFeatureLayer)!!)
                        mPopUp!!.showPopup(feature as ArcGISFeature, false)
                    } catch (e: Exception) {
                        DAlertDialog().show(mActivity, "Có lỗi xảy ra", e.toString())
                    }

                }
            }
        })

        val add_point = mMapView.getCurrentViewpoint(Viewpoint.Type.CENTER_AND_SCALE).targetGeometry.extent.center
        singleTapAdddFeatureAsync.execute(add_point)
        mActivity.closeAddFeature()
    }

    fun onSingleTapMapView(e: MotionEvent) {
        if (identifyFeatureLayer == null) {
            Toast.makeText(this.mActivity, "Vui lòng chọn lớp thao tác bản đồ", Toast.LENGTH_LONG).show()
            mActivity.showDialogSelectLayer()
            return
        }
        mClickPoint = android.graphics.Point(e.x.toInt(), e.y.toInt())
        mApplication.progressDialog.changeTitle(mActivity, mActivity.main_activity_drawer_layout, "Đang xác định đối tượng...")
        val singleTapMapViewAsync = SingleTapMapViewAsync( mFeatureLayerDTGs!!, mPopUp!!, mMapView,
                object: SingleTapMapViewAsync.AsyncResponse{
                    override fun processFinish(output: Void?) {
                        mApplication.progressDialog.dismiss()
                    }
                })
        singleTapMapViewAsync.setFeatureLayer(identifyFeatureLayer!!)
        singleTapMapViewAsync.execute(mClickPoint)
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
        val feature = searchSFT!!.queryFeaturesAsync(queryParameters, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL)
        feature.addDoneListener {
            try {
                val result = feature.get()
                if (result.iterator().hasNext()) {
                    mSelectedArcGISFeature = result.iterator().next() as ArcGISFeature
                    if (mSelectedArcGISFeature != null) {
                        val layerID = mSelectedArcGISFeature!!.featureTable.featureLayer.id
                        val featureLayerDTG = getmFeatureLayerDTG(layerID)
                        mPopUp!!.setFeatureLayerDTG(featureLayerDTG!!)
                        mPopUp!!.showPopup(mSelectedArcGISFeature!!, false)
                    }
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: ExecutionException) {
                e.printStackTrace()
            }
        }

    }

    fun getmFeatureLayerDTG(layerID: String): FeatureLayerDTG? {
        for (featureLayerDTG in mFeatureLayerDTGs!!) {
            val id = featureLayerDTG.featureLayer.id
            if (id == layerID) return featureLayerDTG
        }
        return null
    }

    fun querySearch(searchStr: String, listView: ListView, adapter: ObjectsAdapter) {
        mApplication.progressDialog.changeTitle(mActivity, mActivity.main_activity_drawer_layout, "Đang tìm kiếm...")
        QuerySearchAsycn(searchSFT!!, object : QuerySearchAsycn.AsyncResponse {
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