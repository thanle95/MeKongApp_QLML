package mekong.ditagis.com.qlts.utities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.view.MotionEvent
import android.widget.EditText
import android.widget.Toast
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.GeometryType
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import kotlinx.android.synthetic.main.activity_quan_ly_tai_san.*
import kotlinx.android.synthetic.main.app_bar.view.*
import kotlinx.android.synthetic.main.content.view.*
import mekong.ditagis.com.qlts.AddFeatureActivity
import mekong.ditagis.com.qlts.MainActivity
import mekong.ditagis.com.qlts.R
import mekong.ditagis.com.qlts.async.FindLocationAsycn
import mekong.ditagis.com.qlts.async.GetFeatureLegendAsync
import mekong.ditagis.com.qlts.entities.DAddress
import mekong.ditagis.com.qlts.entities.DBookmark
import mekong.ditagis.com.qlts.entities.FeatureLayerValueIDField
import ru.whalemare.sheetmenu.ActionItem
import ru.whalemare.sheetmenu.SheetMenu
import ru.whalemare.sheetmenu.layout.LinearLayoutProvider
import kotlin.math.roundToInt

class AddHandlingOrChangeGeometry(private val mMainActivity: MainActivity) {
    private val mApplication: DApplication = mMainActivity.application as DApplication
    private val mMapView = mMainActivity.appBar.content.mapView
    fun selectOptionAdd(e: MotionEvent?) {
        val center: Point = if (e != null) {
            mMapView.screenToLocation(android.graphics.Point(e.x.roundToInt(), e.y.roundToInt()))
        } else {
            mMapView.getCurrentViewpoint(Viewpoint.Type.CENTER_AND_SCALE).targetGeometry.extent.center
        }
        addGraphics(center)

        try {
            @SuppressLint("InflateParams") val findLocationAsync = FindLocationAsycn(mMainActivity, false,
                    object : FindLocationAsycn.AsyncResponse {
                        override fun processFinish(output: List<DAddress>?) {

                            if (output != null && output.isNotEmpty()) {
                                val dAddress = output[0]
                                val addressLine = dAddress.location
                                mApplication.center = center
                                selectLayerToAdd(addressLine)
//                                val actions = mutableListOf<ActionItem>()
//                                val drawableBookmark = ResourcesCompat.getDrawable(mMainActivity.resources, R.drawable.ic_bookmark, null)
//                                val wrappedDrawableBookmark = DrawableCompat.wrap(drawableBookmark!!)
//                                DrawableCompat.setTint(wrappedDrawableBookmark, Color.BLUE)
//
//                                val drawableFeature = ResourcesCompat.getDrawable(mMainActivity.resources, R.drawable.ic_layer, null)
//                                val wrappedDrawableFeature = DrawableCompat.wrap(drawableFeature!!)
//                                DrawableCompat.setTint(wrappedDrawableFeature, Color.CYAN)
//
//                                actions.add(ActionItem(Constant.RequestCode.ADD_BOOKMARK, "Thêm bookmark",
//                                        wrappedDrawableBookmark))
//                                actions.add(ActionItem(Constant.RequestCode.ADD_FEATURE, "Thêm đối tượng kiểm tra", wrappedDrawableFeature))
//                                SheetMenu(
//                                        title = "Địa chỉ: $addressLine",
//                                        layoutProvider = LinearLayoutProvider(), // linear layout enabled by default
//                                        actions = actions,
//                                        onClick = { actionItem: ActionItem ->
//                                            when (actionItem.id) {
//                                                Constant.RequestCode.ADD_BOOKMARK -> addBookmark(addressLine, center)
//                                                Constant.RequestCode.ADD_FEATURE -> {
////                        val pointLongLat = Point(longtitude.get(), latitdue.get())
////                        val geometry = GeometryEngine.project(pointLongLat, SpatialReferences.getWgs84())
////                        val geometry1 = GeometryEngine.project(geometry, SpatialReferences.getWebMercator())
////                        val point = geometry1.extent.center
//                                                    mApplication.addFeaturePoint = center
//                                                    selectLayerToAdd()
//                                                }
//                                            }
//                                        },
//                                        onCancel = {
//                                            handlingCancelAdd()
//                                        }
//                                ).show(mMainActivity)
                            }
                        }
                    })
            val project = GeometryEngine.project(center, SpatialReferences.getWgs84())
            val location = doubleArrayOf(project.extent.center.x, project.extent.center.y)
            findLocationAsync.setLongtitude(location[0])
            findLocationAsync.setLatitude(location[1])
            findLocationAsync.execute()
        } catch (e: Exception) {
            Log.e("Popup tìm kiếm", e.toString())
        }
    }

    fun addGraphics(center: Point) {
        mMapView.setViewpointCenterAsync(center)
        val color: Int = Color.YELLOW
        val symbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CROSS, color, 20f)
        val graphic = Graphic(center, symbol)
        mMainActivity.graphicsOverlay!!.graphics.clear()
        mMainActivity.graphicsOverlay!!.graphics.add(graphic)
    }

    fun addBookmark(address: String, center: Point) {
        val project = GeometryEngine.project(center, SpatialReferences.getWgs84())
        val location = doubleArrayOf(project.extent.center.x, project.extent.center.y)
        val builder = AlertDialog.Builder(mMainActivity, R.style.DDialogBuilder)
        builder.setTitle("Nhập tên bookmark")
        builder.setCancelable(false)
        val editText = EditText(builder.context)
        builder.setPositiveButton("OK") { _, _ ->
            val bookmark = DBookmark(location[1], location[0], address, editText.text.toString())
            if (LocalDatabase.getInstance(mMainActivity).addDBookmark(bookmark))
            //nếu thành công
                handlingAddBookmarkSuccess(bookmark)
        }.setNegativeButton("Hủy") { _, _ -> handlingCancelAdd() }.setView(editText)
        val dialog = builder.create()
        dialog.show()
    }

    private fun handlingAddBookmarkSuccess(bookmark: DBookmark) {
        Toast.makeText(mMainActivity, "Bạn vừa thêm một bookmark", Toast.LENGTH_SHORT).show()
        mMainActivity.graphicsOverlay!!.graphics.clear()
        if (mMainActivity.locationDisplay!!.isStarted)
            mMainActivity.locationDisplay!!.stop()
        if ((mMainActivity.callout != null) and mMainActivity.callout!!.isShowing)
            mMainActivity.callout!!.dismiss()

        //show bookmark
//        mApplication.selectedBookmark = bookmark
//        mMainActivity.popUp!!.showPopupBookmark(mApplication.selectedBookmark)

    }

    fun handlingCancelAdd() {
        if (mMainActivity.callout != null && mMainActivity.callout!!.isShowing) {
            mMainActivity.callout!!.dismiss()
        }
        mMainActivity.graphicsOverlay!!.graphics.clear()
        mApplication.statusCode = Constant.StatusCode.NORMAL.value
    }

    fun selectLayerToAdd(address: String) {
        var id = 0
        val actions = mutableListOf<ActionItem>()
        var count = 0
        var sum = 0
        mMapView.map.operationalLayers.forEach { layer ->
            if (layer.loadStatus == LoadStatus.LOADED && layer is FeatureLayer) {

                if (layer.featureTable.geometryType == GeometryType.POINT) {
                    sum++
                    GetFeatureLegendAsync(mMainActivity, object : GetFeatureLegendAsync.AsyncResponse {
                        override fun processFinish(o: Any) {
                            when (o) {
                                is Exception -> {
                                    DAlertDialog().show(mMainActivity, o)

                                }
                                is Boolean -> {
                                    DAlertDialog().show(mMainActivity, "Thông báo", Constant.Message.UNDEFINED)
                                }
                                is String -> {

                                    DAlertDialog().show(mMainActivity, "Thông báo", o)
                                }
                                is HashMap<*, *> -> {
                                    count++
                                    o.keys.forEach { key ->
                                        val bitmap = key as Bitmap
                                        val value = o[key] as String
                                        var title = layer.name + ": " + value
                                        if (value.isNullOrEmpty())
                                            title = layer.name
                                        id++
                                        mApplication.idFeatureLayerToAdd[id] = FeatureLayerValueIDField(layer, value)
                                        actions.add(ActionItem(id, title, BitmapDrawable(mMainActivity.resources,
                                                bitmap)))
                                    }
                                    if (count == sum) {
                                        actions.sortBy { actionItem -> actionItem.title.toString() }
                                        SheetMenu(
                                                title = "Thêm điểm tại: ${address}",
                                                layoutProvider = LinearLayoutProvider(), // linear layout enabled by default
                                                actions = actions,
                                                onClick = { actionItem: ActionItem ->
                                                    addFeature(actionItem.id)

                                                }
                                        ).show(mMainActivity)
                                    }
                                }
                                else -> {
                                    DAlertDialog().show(mMainActivity, "Thông báo", Constant.Message.UNDEFINED)
                                }
                            }
                        }

                    }).execute(layer)
                }


            }
        }

    }

    private fun addFeature(id: Int) {
        val intentAdd = Intent(mMainActivity, AddFeatureActivity::class.java)
        intentAdd.putExtra(Constant.IntentExtra.ID_ADD_FEATURE, id)
        mMainActivity.startActivityForResult(intentAdd, Constant.RequestCode.ADD)
    }
}