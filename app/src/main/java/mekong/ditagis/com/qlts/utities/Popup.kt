package mekong.ditagis.com.qlts.utities

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.data.*
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.view.Callout
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.symbology.UniqueValueRenderer
import mekong.ditagis.com.qlts.AttachmentActivity
import mekong.ditagis.com.qlts.MainActivity
import mekong.ditagis.com.qlts.R
import mekong.ditagis.com.qlts.UpdateActivity
import mekong.ditagis.com.qlts.adapter.FeatureViewInfoAdapter
import mekong.ditagis.com.qlts.async.QueryHanhChinhAsync
import mekong.ditagis.com.qlts.databinding.LayoutPopupInfosBinding
import java.util.*


class Popup(private val mMainActivity: MainActivity, private val mMapView: MapView, private val mCallout: Callout?) : AppCompatActivity() {
    private var lstUniqueValues: MutableList<String>? = null
    private var fieldNameDrawInfo: String? = null
    private lateinit var mBindingLayoutInfos: LayoutPopupInfosBinding
    private var quanhuyen_features: ArrayList<Feature>? = null
    private var quanhuyen_feature: Feature? = null

    private val mApplication: DApplication = mMainActivity.application as DApplication

    private val isDeleteFeature: Boolean
        get() {
            var isPast1DateOfCurrentDate = false
            var isSameUser = false
            val attr = mApplication.selectedFeature!!.attributes
            val fields = mApplication.selectedFeature!!.featureTable.fields
            for (field in fields) {
                if (field.name.toUpperCase() == this.mMainActivity.resources.getString(R.string.NGAYTHEMMOI)) {
                    val ngayThemMoi = attr[field.name]
                    if (ngayThemMoi != null) {
                        val calendar = ngayThemMoi as Calendar?
                        val currentDate = Calendar.getInstance()
                        isPast1DateOfCurrentDate = isPast1DateOfCurrentDate(calendar, currentDate)
                    }
                }
                if (field.name.toUpperCase() == this.mMainActivity.resources.getString(R.string.NGUOICAPNHAT)) {
                    val nguoiCapNhat = attr[field.name]
                    if (nguoiCapNhat != null) {
                        isSameUser = nguoiCapNhat.toString() == this.mApplication.user!!.username
                    }
                }
            }
            return isPast1DateOfCurrentDate && isSameUser
        }

    private fun longLatToPoint(lon: Double, lat: Double): Point {
        val pointLongLat = Point(lon, lat)
        val geometryWgs84 = GeometryEngine.project(pointLongLat, SpatialReferences.getWgs84())
        val geometryWebMercator = GeometryEngine.project(geometryWgs84, SpatialReferences.getWebMercator())
        return geometryWebMercator.extent.center
    }

    fun setmSFTHanhChinh(SFTHanhChinh: ServiceFeatureTable) {
        mApplication.SFTAdministrator = SFTHanhChinh
        mApplication.progressDialog.changeTitle(mMainActivity, mMainActivity.mBinding.drawerLayout, "Đang lấy dữ liệu hành chính...")
        QueryHanhChinhAsync(SFTHanhChinh, object : QueryHanhChinhAsync.AsyncResponse {
            override fun processFinish(output: ArrayList<Feature>?) {
                mApplication.progressDialog.dismiss()
                quanhuyen_features = output
            }
        }).execute()
    }

    fun refreshPopup() {
        val hiddenFields = mMainActivity.resources.getStringArray(R.array.hiddenFields)
        val attributes = mApplication.selectedFeature!!.attributes
        val listView = mBindingLayoutInfos.lstviewThongtinsuco
        val featureViewInfoAdapter = FeatureViewInfoAdapter(mMainActivity, ArrayList())
        listView.adapter = featureViewInfoAdapter

        val renderer = (mApplication.selectedFeature!!.featureTable as ServiceFeatureTable).layerInfo.drawingInfo.renderer
        var uniqueValueRenderer: UniqueValueRenderer? = null
        if (renderer is UniqueValueRenderer) {
            uniqueValueRenderer = renderer
            fieldNameDrawInfo = uniqueValueRenderer.fieldNames[0]

        }
        var checkHiddenField: Boolean
        val idHanhChinh = attributes[mMainActivity.getString(R.string.MAXA)]
        if (idHanhChinh != null) {
            getHanhChinhFeature(idHanhChinh.toString())
        }
        for (field in this.mApplication.selectedFeature!!.featureTable.fields) {
            checkHiddenField = false
            for (hiddenField in hiddenFields) {
                if (hiddenField == field.name) {
                    checkHiddenField = true
                    break
                }
            }
            val value = attributes[field.name]
            if (value != null && !checkHiddenField) {
                val item = FeatureViewInfoAdapter.Item()
                item.alias = field.alias
                item.fieldName = field.name
                if (item.fieldName!!.toUpperCase() == mMainActivity.getString(R.string.MAXA)) {
                    if (quanhuyen_feature != null)
                        item.value = quanhuyen_feature!!.attributes[mMainActivity.getString(R.string.TENHANHCHINH)].toString()
                    else
                        item.value = value.toString()
                } else if (item.fieldName!!.toUpperCase() == mMainActivity.getString(R.string.MAHUYEN)) {
                    if (quanhuyen_feature != null)
                        item.value = quanhuyen_feature!!.attributes[mMainActivity.getString(R.string.TENHUYEN)].toString()
                    else {
                        item.value = value.toString()
                    }
                } else if (item.fieldName == fieldNameDrawInfo) {
                    val uniqueValues = uniqueValueRenderer!!.uniqueValues
                    if (uniqueValues.size > 0) {
                        val valueFeatureType = getLabelUniqueRenderer(uniqueValues, value.toString())
                        if (valueFeatureType != null) item.value = valueFeatureType.toString()
                    } else
                        item.value = value.toString()

                } else if (field.domain != null) {
                    val codedValues = (this.mApplication.selectedFeature!!.featureTable.getField(item.fieldName!!).domain as CodedValueDomain).codedValues
                    val valueDomainObject = getValueDomain(codedValues, value.toString())
                    if (valueDomainObject != null) item.value = valueDomainObject.toString()
                } else
                    when (field.fieldType) {
                        Field.Type.DATE -> item.value = Constant.DATE_FORMAT.format((value as Calendar).time)
                        else -> item.value = value.toString()
                    }

                featureViewInfoAdapter.add(item)
                featureViewInfoAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun getValueDomain(codedValues: List<CodedValue>, code: String): Any? {
        var value: Any? = null
        for (codedValue in codedValues) {
            if (codedValue.code.toString() == code) {
                value = codedValue.name
                break
            }

        }
        return value
    }

    private fun getLabelUniqueRenderer(uniqueValues: List<UniqueValueRenderer.UniqueValue>, code: String): Any? {
        var value: Any? = null
        for (uniqueValue in uniqueValues) {
            if (uniqueValue.values != null && uniqueValue.values[0].toString() == code) {
                value = uniqueValue.label
                break
            }
        }
        return value
    }

    fun clearSelection() {
        mMapView.map.operationalLayers.forEach { layer ->
            if(layer is FeatureLayer)
                layer.clearSelection()
        }
    }

    fun dimissCallout() {
        this.clearSelection()
        if (mCallout != null && mCallout.isShowing) {
            mCallout.dismiss()
        }
    }

    private fun getHanhChinhFeature(idHanhChinh: String) {
        quanhuyen_feature = null
        if (quanhuyen_features != null) {
            for (feature in quanhuyen_features!!) {
                val maDonViHanhChinh = feature.attributes[mMainActivity.getString(R.string.IDHanhChinh)]
                if (maDonViHanhChinh != null && maDonViHanhChinh == idHanhChinh) {
                    quanhuyen_feature = feature
                }
            }
        }
    }

    fun showPopup( selectedFeature: ArcGISFeature) {
        dimissCallout()
        this.mApplication.selectedFeature = selectedFeature
        val featureLayer = selectedFeature.featureTable.layer as FeatureLayer
        featureLayer.selectFeature(mApplication.selectedFeature)
        lstUniqueValues = ArrayList()
        fieldNameDrawInfo = null
        val renderer = (mApplication.selectedFeature!!.featureTable as ServiceFeatureTable).layerInfo.drawingInfo.renderer
        var uniqueValues: List<UniqueValueRenderer.UniqueValue>? = null
        if (renderer is UniqueValueRenderer) {
            uniqueValues = renderer.uniqueValues
        }
        if (uniqueValues != null) {
            for (i in uniqueValues.indices) {
                lstUniqueValues!!.add(uniqueValues[i].label.toString())
            }
        }

        val inflater = LayoutInflater.from(this.mMainActivity.applicationContext)
        mBindingLayoutInfos = LayoutPopupInfosBinding.inflate(inflater)
        refreshPopup()
        mBindingLayoutInfos.txtTitleLayer.text = featureLayer.name
        val btnUpdate = mBindingLayoutInfos.imgBtnUpdate
        btnUpdate.setOnClickListener { v ->
            val updateIntent = Intent(mMainActivity, UpdateActivity::class.java)
            mMainActivity.startActivityForResult(updateIntent, Constant.RequestCode.UPDATE)
        }
        val imgBtn_view_attachment = mBindingLayoutInfos.imgBtnViewAttachment
        if ((this.mApplication.selectedFeature!! as ArcGISFeature).canEditAttachments()) {
            imgBtn_view_attachment.setOnClickListener { v ->
                val intent = Intent(mMainActivity, AttachmentActivity::class.java)
                mMainActivity.startActivity(intent)
            }
        } else
            imgBtn_view_attachment.visibility = View.GONE
        val imgBtn_delete = mBindingLayoutInfos.imgBtnDelete
        imgBtn_delete.setOnClickListener { v ->
            mApplication.selectedFeature!!.featureTable.featureLayer.clearSelection()
            deleteFeature()
        }
        mBindingLayoutInfos.btnLayerClose.setOnClickListener { v -> dimissCallout() }

        mBindingLayoutInfos.root.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val envelope = mApplication.selectedFeature!!.geometry.extent
//        if (!clickMap!!) mMapView.setViewpointGeometryAsync(envelope, 0.0)
//        mMapView.setViewpointCenterAsync(envelope.center)
//        if (mMapView.mapScale > featureLayer.minScale && featureLayer.minScale != 0.0)
//            mMapView.setViewpointScaleAsync(featureLayer.minScale)
//        // show CallOut
//        mCallout!!.location = envelope.center
//        mCallout.content = linearLayout!!
//        this.runOnUiThread {
//            mCallout.refresh()
//            mCallout.show()
//        }
        showCallout(envelope.center, mBindingLayoutInfos.root, mMainActivity.mBinding.appBar.content.mapView.mapScale)
    }

    private fun showCallout(point: Point?, view: View?, scale: Double) {
        mMainActivity.runOnUiThread {
            val viewpointCenterAsync = mMainActivity.mBinding.appBar.content.mapView.setViewpointCenterAsync(point, scale)
            viewpointCenterAsync.addDoneListener {
                val result = viewpointCenterAsync.get()
                if (result) {
                    mCallout!!.location = point
                    mCallout.content = view
                    mCallout.refresh()
                    mCallout.show()
                } else {
                    showCallout(point, view, scale)
                }
            }

        }
    }

    private fun deleteFeature() {
        val builder = AlertDialog.Builder(mMainActivity, android.R.style.Theme_Material_Light_Dialog_Alert)
        builder.setTitle("Xác nhận")
        builder.setMessage("Bạn có chắc chắn xóa đối tượng này không?")
        builder.setPositiveButton("Có") { dialog, which ->
            dialog.dismiss()
            (mApplication.selectedFeature!! as ArcGISFeature).loadAsync()

            // update the selected feature
            (mApplication.selectedFeature!! as ArcGISFeature).addDoneLoadingListener {
                if ((mApplication.selectedFeature!! as ArcGISFeature).loadStatus == LoadStatus.FAILED_TO_LOAD) {
                    Log.d(mMainActivity.resources.getString(R.string.app_name), "Error while loading feature")
                }
                try {
                    // update feature in the feature table
                    val serviceFeatureTable = mApplication.selectedFeature!!.featureTable as ServiceFeatureTable
                    val mapViewResult = serviceFeatureTable!!.deleteFeatureAsync(mApplication.selectedFeature!!)
                    mapViewResult.addDoneListener {
                        // apply change to the server
                        val serverResult = serviceFeatureTable!!.applyEditsAsync()
                        serverResult.addDoneListener {
                            var edits: List<FeatureEditResult>? = null
                            try {
                                edits = serverResult.get()
                                if (edits!!.size > 0) {
                                    if (!edits[0].hasCompletedWithErrors()) {
                                        Log.e("", "Feature successfully updated")
                                        DAlertDialog().show(mMainActivity, "Thông báo", "Xóa thành công")

                                    } else DAlertDialog().show(mMainActivity, "Thông báo", "Xóa thất bại")
                                } else DAlertDialog().show(mMainActivity, "Thông báo", "Xóa thất bại")
                            } catch (e: Exception) {
                                DAlertDialog().show(mMainActivity, e)
                            }
                        }
                    }

                } catch (e: Exception) {
                    DAlertDialog().show(mMainActivity, e)
                }
            }
            mCallout?.dismiss()
        }.setNegativeButton("Không") { dialog, which -> dialog.dismiss() }.setCancelable(false)
        val dialog = builder.create()
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.show()


    }


    companion object {


        fun isPast1DateOfCurrentDate(calendar: Calendar?, currentDate: Calendar?): Boolean {
            kotlin.require(!(calendar == null || currentDate == null)) { "The dates must not be null" }
            val deviationTime = currentDate!!.timeInMillis - calendar!!.timeInMillis
            val time = 24 * 60 * 60 * 1000
            return time - deviationTime > 0
        }
    }

}
