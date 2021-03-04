package mekong.ditagis.com.qlts

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.widget.CompoundButtonCompat
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.ArcGISRuntimeException
import com.esri.arcgisruntime.data.ArcGISFeature
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.ArcGISMapImageLayer
import com.esri.arcgisruntime.layers.ArcGISMapImageSublayer
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.*
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.google.android.material.navigation.NavigationView
import mekong.ditagis.com.qlts.adapter.FeatureLayerAdapter
import mekong.ditagis.com.qlts.adapter.ObjectsAdapter
import mekong.ditagis.com.qlts.async.GetLegendAsync
import mekong.ditagis.com.qlts.async.PreparingTask
import mekong.ditagis.com.qlts.databinding.ActivityQuanLyTaiSanBinding
import mekong.ditagis.com.qlts.databinding.LayoutTitleListviewBinding
import mekong.ditagis.com.qlts.entities.DLayerInfo
import mekong.ditagis.com.qlts.libs.Action
import mekong.ditagis.com.qlts.libs.FeatureLayerDTG
import mekong.ditagis.com.qlts.tools.*
import mekong.ditagis.com.qlts.utities.*
import java.io.IOException
import java.util.*
import java.util.concurrent.atomic.AtomicInteger


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
    private var mUri: Uri? = null
    private var mPopup: Popup? = null
    private var mMap: ArcGISMap? = null
    private var mCallout: Callout? = null
    private var mMapViewHandler: MapViewHandler? = null
    private var mSearchAdapter: ObjectsAdapter? = null
    private var mLocationDisplay: LocationDisplay? = null
    private val requestCode = 2
    private var mCurrentPoint: Point? = null
    private var mGeocoder: Geocoder? = null
    private var mGraphicsOverlay: GraphicsOverlay? = null
    private var isSearchingFeature = false
    private var mFeatureLayerDTGS: MutableList<FeatureLayerDTG>? = null
    private var thongKe: ThongKe? = null
    private var hanhChinhImageLayers: ArcGISMapImageLayer? = null
    private val taiSanImageLayers: ArcGISMapImageLayer? = null
    private var states: Array<IntArray>? = null
    private var colors: IntArray? = null
    private lateinit var mApplication: DApplication

    private var mSelectedArcGISFeature: ArcGISFeature? = null
    internal var reqPermissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    lateinit var mBinding: ActivityQuanLyTaiSanBinding
    fun setUri(uri: Uri) {
        this.mUri = uri
    }

    fun setSelectedArcGISFeature(selectedArcGISFeature: ArcGISFeature) {
        this.mSelectedArcGISFeature = selectedArcGISFeature
    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityQuanLyTaiSanBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mApplication = application as DApplication
        mGeocoder = Geocoder(this)
        //        // create an empty map instance
        setUp()
    }

    private fun setUp() {
        states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
        colors = intArrayOf(R.color.colorTextColor_1, R.color.colorTextColor_1)
        setLicense()
        //for camera
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        // for navigation
        setSupportActionBar(mBinding.appBar.toolbar)
        // permisson
        requestPermisson()

        val toggle = ActionBarDrawerToggle(this, mBinding.drawerLayout, mBinding.appBar.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        mBinding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        mBinding.navView.setNavigationItemSelectedListener(this)

        // khởi tạo chức năng tìm kiếm
        initListViewSearch()

        // ẩn hiện thị lớp dữ liệu
        initLayerListView()


        setOnClickListener()
        startSignIn()

    }

    private fun startSignIn() {
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        this@MainActivity.startActivityForResult(intent, Constant.RequestCode.LOGIN)
    }

    private fun setLoginInfos() {
        val application = application as DApplication
        val displayName = application.user!!.displayName
        mBinding.navView.setNavigationItemSelectedListener(this)
        val headerLayout = mBinding.navView.getHeaderView(0)
        val nav_name_nv = headerLayout.findViewById<TextView>(R.id.nav_name_nv)
        nav_name_nv.setText(displayName)
    }

    private fun startMain() {

        requestPermisson()
        setLoginInfos()
        initMapView()
    }

    private fun initMapView() {

        mMap = ArcGISMap(Basemap.Type.OPEN_STREET_MAP, 10.107553, 105.8461029, 12)

        mBinding.appBar.content.mapView.map = mMap
        mApplication.progressDialog.changeTitle(this, mBinding.drawerLayout, "Đang lấy dữ liệu...")
        PreparingTask(object : PreparingTask.Response {
            override fun post(output: List<DLayerInfo>?) {
                if (output != null && output.isNotEmpty()) {
                    mApplication!!.layerInfos = output
                    mApplication.progressDialog.changeTitle(this@MainActivity, mBinding.drawerLayout, "Đang tải bản đồ...")
                    setFeatureService()
                } else if (output == null) {
                    Toast.makeText(this@MainActivity, "Tài khoản của bạn không có quyền truy cập ứng dụng này", Toast.LENGTH_SHORT).show()
                    startSignIn()
                } else {
                    Toast.makeText(this@MainActivity, "Không tìm thấy lớp dữ liệu. Vui lòng liên hệ quản trị viên", Toast.LENGTH_SHORT).show()
                    startSignIn()
                }

            }
        }).execute(this@MainActivity, mApplication!!)

        changeStatusOfLocationDataSource()
        mLocationDisplay!!.addLocationChangedListener { locationChangedEvent -> }
        mGraphicsOverlay = GraphicsOverlay()
        mBinding.appBar.content.mapView.graphicsOverlays.add(mGraphicsOverlay)
        mApplication.progressDialog.changeTitle(this, mBinding.drawerLayout, "Đang khởi tạo ứng dụng...")
    }

    private fun setFeatureService() {
        // config feature layer service
        mFeatureLayerDTGS = ArrayList()
        mCallout = mBinding.appBar.content.mapView.callout
        mMapViewHandler = MapViewHandler(mBinding.appBar.content.mapView, this@MainActivity)
        mPopup = Popup(this@MainActivity, mBinding.appBar.content.mapView, mCallout)
        val size = AtomicInteger(mApplication.layerInfos!!.size)
        val layerVisible = HashMap<Any, Boolean>()
        for (layerInfo in mApplication.layerInfos!!) {
            if (layerInfo.layerId.substring(layerInfo.layerId.length - 3) == "TBL" ||
                    !layerInfo.isView || layerInfo.layerId == "diemsucoLYR") {
                size.decrementAndGet()
                continue
            }
            var url = layerInfo.url
            if (!layerInfo.url.startsWith("http"))
                url = "http:" + layerInfo.url
            if (layerInfo.layerId.toUpperCase().contains(Constant.LAYER_ID.BASEMAP)) {
                hanhChinhImageLayers = ArcGISMapImageLayer(url)
                hanhChinhImageLayers!!.id = layerInfo.layerId
                mBinding.appBar.content.mapView.map.operationalLayers.add(hanhChinhImageLayers)
                val finalUrl = url
                hanhChinhImageLayers!!.addDoneLoadingListener {
                    for (layer in mBinding.appBar.content.mapView.map.operationalLayers) {
                        if (layer is FeatureLayer) continue
                        val fullExtent = layer.fullExtent
                        if (fullExtent != null && fullExtent.xMin != 0.0 && fullExtent.yMin != 0.0
                                && fullExtent.xMax != 0.0 && fullExtent.yMax != 0.0) {
                            try {
                                mBinding.appBar.content.mapView.setViewpointGeometryAsync(fullExtent, 50.0)

                                break
                            } catch (e: Exception) {
                                DAlertDialog().show(this@MainActivity, "Thông báo", layer.name + ": " + e.toString())
                            }
                        }
                    }
                    size.decrementAndGet()
                    if (hanhChinhImageLayers!!.loadStatus == LoadStatus.LOADED) {
                        val sublayerList = hanhChinhImageLayers!!.sublayers
                        for (sublayer in sublayerList) {
                            addCheckBoxSubLayer(sublayer as ArcGISMapImageSublayer)
                            layerVisible[sublayer] = hanhChinhImageLayers!!.isVisible
                        }
                        val url_HanhChinh = "$finalUrl/5"
                        val serviceFeatureTable = ServiceFeatureTable(url_HanhChinh)
                        mPopup!!.setmSFTHanhChinh(serviceFeatureTable)
                    }
                    if (size.get() == 0) {
                        mApplication!!.layerVisible = layerVisible
                        getLegend()
                    }
                }
                hanhChinhImageLayers!!.loadAsync()
            } else if (layerInfo.isView) {
                val serviceFeatureTable = ServiceFeatureTable(url!!)
                val featureLayer = FeatureLayer(serviceFeatureTable)
                featureLayer.name = layerInfo.layerName
                featureLayer.id = layerInfo.layerId
                if (layerInfo.definition != null && layerInfo.definition != "null")
                    featureLayer.definitionExpression = layerInfo.definition
                mMap!!.operationalLayers.add(featureLayer)
                val featureLayerDTG = FeatureLayerDTG(featureLayer)
                val action = Action(layerInfo.isView, layerInfo.isCreate, layerInfo.isEdit, layerInfo.isDelete)
                featureLayerDTG.action = action
                mFeatureLayerDTGS!!.add(featureLayerDTG)
                featureLayer.addDoneLoadingListener {
                    size.decrementAndGet()
                    addCheckBox_TaiSanLayer(featureLayer)
                    layerVisible[featureLayer] = featureLayer.isVisible
                    if (size.get() == 0) {
                        mApplication!!.layerVisible = layerVisible
                        getLegend()
                    }
                }

                //            }
            }

        }

        mMapViewHandler!!.setmPopUp(mPopup!!)
        mMapViewHandler!!.setFeatureLayerDTGs(mFeatureLayerDTGS!!)
        thongKe = ThongKe(this, mFeatureLayerDTGS!!)
        mapViewEvent()
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun mapViewEvent() {
        mBinding.appBar.content.mapView.onTouchListener = object : DefaultMapViewOnTouchListener(this, mBinding.appBar.content.mapView) {
            override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                try {
                    mMapViewHandler!!.onSingleTapMapView(e!!)
                } catch (ex: ArcGISRuntimeException) {
                    Log.d("", ex.toString())
                }

                return super.onSingleTapConfirmed(e)
            }

            @SuppressLint("SetTextI18n")
            override fun onScroll(e1: MotionEvent, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
                if (mMapViewHandler != null) {
                    val location = mMapViewHandler!!.onScroll(e1, e2!!, distanceX, distanceY)
                    val log = Math.round(location!![0] * 100000).toFloat() / 100000
                    val lat = Math.round(location[1] * 100000).toFloat() / 100000
                    val text = "$lat, $log"
                    mBinding.appBar.content.txtToado.text = text
                }
                return super.onScroll(e1, e2, distanceX, distanceY)
            }

        }
        mBinding.appBar.skbrHanhchinhLayers.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                hanhChinhImageLayers!!.opacity = i.toFloat() / 100
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })
    }


    private fun getFieldsDTG(stringFields: String?): Array<String>? {
        var returnFields: Array<String>? = null
        if (stringFields != null) {
            if (stringFields === "*") {
                returnFields = arrayOf("*")
            } else {
                returnFields = stringFields.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            }

        }
        return returnFields
    }

    private fun getLegend() {
        mApplication.progressDialog.changeTitle(this, mBinding.drawerLayout, "Đang tải chú thích...")
        GetLegendAsync(this@MainActivity, object : GetLegendAsync.AsyncResponse {
            override fun processFinish() {
                endLoadingMap()
            }
        }).execute()
    }

    private fun endLoadingMap() {
        mApplication.progressDialog.dismiss()
    }

    private fun initLayerListView() {
        mBinding.appBar.cbLayerTaiSan.setOnCheckedChangeListener { buttonView, isChecked ->
            for (i in 0 until mBinding.appBar.linnearDisplayLayerTaiSan.childCount) {
                val view = mBinding.appBar.linnearDisplayLayerTaiSan.getChildAt(i)
                if (view is CheckBox) {
                    view.isChecked = isChecked
                }
            }
        }
        mBinding.appBar.cbLayerHanhChinh.setOnCheckedChangeListener { buttonView, isChecked ->
            for (i in 0 until mBinding.appBar.linnearDisplayLayerBaseMap.childCount) {
                val view = mBinding.appBar.linnearDisplayLayerBaseMap.getChildAt(i)
                if (view is CheckBox) {
                    view.isChecked = isChecked
                }
            }
        }
    }


    private fun initListViewSearch() {
        //đưa listview search ra phía sau
        mBinding.appBar.content.lstviewSearch.invalidate()
        val items = ArrayList<ObjectsAdapter.Item>()
        this.mSearchAdapter = ObjectsAdapter(this@MainActivity, items)
        mBinding.appBar.content.lstviewSearch.adapter = mSearchAdapter
        mBinding.appBar.content.lstviewSearch.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val item = parent.getItemAtPosition(position) as ObjectsAdapter.Item
            val objectID = Integer.parseInt(item.objectID!!)
            if (objectID != -1) {
                mMapViewHandler!!.queryByObjectID(objectID)
                mSearchAdapter!!.clear()
                mSearchAdapter!!.notifyDataSetChanged()
            } else {
                setViewPointCenterLongLat(Point(item.longtitude, item.latitude))
            }//tìm kiếm địa chỉ
        }
    }

    private fun setLicense() {
        //way 1
        ArcGISRuntimeEnvironment.setLicense(getString(R.string.license))
    }

    private fun addCheckBoxSubLayer(layer: ArcGISMapImageSublayer) {
        val checkBox = CheckBox(mBinding.appBar.linnearDisplayLayerBaseMap.context)
        checkBox.text = layer.name
        checkBox.isChecked = false
        layer.isVisible = false
        CompoundButtonCompat.setButtonTintList(checkBox, ColorStateList(states, colors))
        checkBox.setOnCheckedChangeListener { buttonView, isChecked ->

            if (checkBox.isChecked) {
                if (buttonView.text == layer.name) {
                }
                layer.isVisible = true


            } else {
                if (checkBox.text == layer.name) {
                }
                layer.isVisible = false
            }
        }
        mBinding.appBar.linnearDisplayLayerBaseMap.addView(checkBox)
    }

    private fun addCheckBox_TaiSanLayer(featureLayer: FeatureLayer) {
        val checkBox = CheckBox(mBinding.appBar.linnearDisplayLayerTaiSan.context)
        checkBox.text = featureLayer.name
        checkBox.isChecked = false
        featureLayer.isVisible = false
        CompoundButtonCompat.setButtonTintList(checkBox, ColorStateList(states, colors))
        checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (checkBox.isChecked) {
                if (buttonView.text == featureLayer.name)
                    featureLayer.isVisible = true
            } else {
                if (checkBox.text == featureLayer.name)
                    featureLayer.isVisible = false
            }
        }
        mBinding.appBar.linnearDisplayLayerTaiSan.addView(checkBox)
    }

    private fun changeStatusOfLocationDataSource() {
        mLocationDisplay = mBinding.appBar.content.mapView.locationDisplay
        //        changeStatusOfLocationDataSource();
        mLocationDisplay!!.addDataSourceStatusChangedListener(LocationDisplay.DataSourceStatusChangedListener { dataSourceStatusChangedEvent ->
            // If LocationDisplay started OK, then continue.
            if (dataSourceStatusChangedEvent.isStarted) return@DataSourceStatusChangedListener

            // No error is reported, then continue.
            if (dataSourceStatusChangedEvent.error == null) return@DataSourceStatusChangedListener

            // If an error is found, handle the failure to start.
            // Check permissions to see if failure may be due to lack of permissions.
            val permissionCheck1 = ContextCompat.checkSelfPermission(this@MainActivity, reqPermissions[0]) == PackageManager.PERMISSION_GRANTED
            val permissionCheck2 = ContextCompat.checkSelfPermission(this@MainActivity, reqPermissions[1]) == PackageManager.PERMISSION_GRANTED

            if (!(permissionCheck1 && permissionCheck2)) {
                // If permissions are not already granted, request permission from the user.
                ActivityCompat.requestPermissions(this@MainActivity, reqPermissions, requestCode)
            }  // Report other unknown failure types to the user - for example, location services may not // be enabled on the device. //                    String message = String.format("Error in DataSourceStatusChangedListener: %s", dataSourceStatusChangedEvent //                            .getSource().getLocationDataSource().getError().getMessage()); //                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
        })
    }

    private fun setViewPointCenter(position: Point?) {
        val geometry = GeometryEngine.project(position!!, SpatialReferences.getWebMercator())
        mBinding.appBar.content.mapView.setViewpointCenterAsync(geometry.extent.center)
    }

    private fun setViewPointCenterLongLat(position: Point) {
        val geometry = GeometryEngine.project(position, SpatialReferences.getWgs84())
        val geometry1 = GeometryEngine.project(geometry, SpatialReferences.getWebMercator())
        val point = geometry1.extent.center

        val symbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CROSS, Color.RED, 20f)
        val graphic = Graphic(point, symbol)
        mGraphicsOverlay!!.graphics.add(graphic)

        mBinding.appBar.content.mapView.setViewpointCenterAsync(point)
    }

    override fun onBackPressed() {
        if (mBinding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            mBinding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_search, menu)
        val mTxtSearch = menu.findItem(R.id.action_search).actionView as SearchView
        mTxtSearch.queryHint = getString(R.string.title_search)
        mTxtSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String): Boolean {
                mTxtSearch.clearFocus()
                if (isSearchingFeature)
                    mMapViewHandler!!.querySearch(query, mBinding.appBar.content.lstviewSearch, mSearchAdapter!!)
                else {
                    try {
                        mSearchAdapter!!.clear()
                        val addressList = mGeocoder!!.getFromLocationName(query, 1)
                        for (address in addressList) {
                            val item = ObjectsAdapter.Item("-1", "", address.getAddressLine(0))
                            item.latitude = address.latitude
                            item.longtitude = address.longitude
                            mSearchAdapter!!.add(item)
                        }
                        mSearchAdapter!!.notifyDataSetChanged()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                //                if (newText.length() == 0) {
                //                    mSearchAdapter.clear();
                //                    mSearchAdapter.notifyDataSetChanged();
                //                } else if (!isSearchingFeature && newText.length() > 10) {
                //
                //                    try {
                //                        mSearchAdapter.clear();
                //                        List<Address> addressList = mGeocoder.getFromLocationName(newText, 1);
                //                        for (Address address : addressList) {
                //                            ObjectsAdapter.Item item = new ObjectsAdapter.Item("-1", "", address.getAddressLine(0));
                //                            item.setLatitude(address.getLatitude());
                //                            item.setLongtitude(address.getLongitude());
                //                            mSearchAdapter.add(item);
                //                        }
                //                        mSearchAdapter.notifyDataSetChanged();
                //                    } catch (IOException e) {
                //                        e.printStackTrace();
                //                    }
                //                }
                return false
            }
        })
        menu.findItem(R.id.action_search).setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                hiddenFloatButton()
                mBinding.appBar.content.layoutTimKiem.visibility = View.VISIBLE
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                showFloatButton()
                mBinding.appBar.content.layoutTimKiem.visibility = View.INVISIBLE
                return true
            }
        })
        return true
    }

    private fun showDialogSelectAddFeatureLayer() {
        val addFeatureItem = AddFeatureItem(mFeatureLayerDTGS!!, this)
        val items = addFeatureItem.getItems()
        val featureLayerAdapter = FeatureLayerAdapter(this, items!!)
        val builder = AlertDialog.Builder(this, android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen)
        val bindingLayout = LayoutTitleListviewBinding.inflate(layoutInflater)
        val listView = bindingLayout.listview
        listView.adapter = featureLayerAdapter
        bindingLayout.txtTitleLayout.text = "Chọn lớp dữ liệu cập nhật"
        builder.setView(bindingLayout.root)
        val selectTimeDialog = builder.create()
        selectTimeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        selectTimeDialog.show()
        selectTimeDialog.setOnKeyListener { arg0, keyCode, event ->
            // TODO Auto-generated method stub
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                selectTimeDialog.dismiss()
                //                closeAddFeature();
            }
            true
        }
        listView.setOnItemClickListener { parent, view, position, id ->
            selectTimeDialog.dismiss()
            val itemAtPosition = parent.getItemAtPosition(position) as FeatureLayerAdapter.Item
            val idLayer = itemAtPosition.idLayer
            mBinding.appBar.content.txtTitleSearch.text = itemAtPosition.titleLayer
            val featureLayer = getFeatureLayer(idLayer!!)
            mMapViewHandler!!.setAddSFT(featureLayer!!.featureTable as ServiceFeatureTable)
            mMapViewHandler!!.setIdentifyFeatureLayer(featureLayer)
            featureLayer.isVisible = true
            mBinding.appBar.linearAddfeature.visibility = View.VISIBLE
            mBinding.appBar.imgMapPin.visibility = View.VISIBLE
            mBinding.appBar.floatBtnAdd.hide()
        }
    }

    fun showDialogSelectLayer() {
        val selectLayerItem = SelectLayerItem(mFeatureLayerDTGS!!, this)
        val items = selectLayerItem.getItems()
        val featureLayerAdapter = FeatureLayerAdapter(this, items!!)
        val builder = AlertDialog.Builder(this, android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen)
        val bindingLayout = LayoutTitleListviewBinding.inflate(layoutInflater)
        val listView = bindingLayout.listview
        listView.adapter = featureLayerAdapter
        val txt_Title_Layout = bindingLayout.txtTitleLayout
        bindingLayout.imgRefresh.visibility = View.GONE
        txt_Title_Layout.text = "Chọn lớp dữ liệu thao tác trên bản đồ"
        builder.setView(bindingLayout.root)
        val selectTimeDialog = builder.create()
        selectTimeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        selectTimeDialog.show()
        val finalItems = featureLayerAdapter.getItems()
        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            selectTimeDialog.dismiss()
            val itemAtPosition = parent.getItemAtPosition(position) as FeatureLayerAdapter.Item
            val idLayer = itemAtPosition.idLayer
            val featureLayer = getFeatureLayer(idLayer!!)
            mMapViewHandler!!.setIdentifyFeatureLayer(featureLayer!!)
            //                mapView.getMap().setMaxScale(featureLayer.getMaxScale());
        }
    }

    private fun showDialogSelectTypeSearch() {
        val searchItem = SearchItem(mFeatureLayerDTGS!!, this)
        val items = searchItem.getItems()
        val featureLayerAdapter = FeatureLayerAdapter(this, items!!)
        val builder = AlertDialog.Builder(this, android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen)
        //todo: xem layout này
        val layout = LayoutTitleListviewBinding.inflate(layoutInflater)
        val listView = layout.listview
        listView.adapter = featureLayerAdapter
        val txt_Title_Layout = layout.txtTitleLayout
        txt_Title_Layout.text = "Tìm kiếm theo"
        builder.setView(layout.root)
        val selectTimeDialog = builder.create()
        selectTimeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        selectTimeDialog.show()
        val finalItems = featureLayerAdapter.getItems()
        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            selectTimeDialog.dismiss()
            val itemAtPosition = parent.getItemAtPosition(position) as FeatureLayerAdapter.Item
            val idLayer = itemAtPosition.idLayer
            mBinding.appBar.content.txtTitleSearch.text = itemAtPosition.titleLayer
            val featureLayer = getFeatureLayer(idLayer!!)
            featureLayer!!.isVisible = true
            mMapViewHandler!!.setSearchSFT(featureLayer.featureTable as ServiceFeatureTable)
            //                mapView.getMap().setMaxScale(featureLayer.getMaxScale());
            mMapViewHandler!!.setIdentifyFeatureLayer(featureLayer)
            isSearchingFeature = true
        }
    }

    fun closeAddFeature() {
        mBinding.appBar.linearAddfeature.visibility = View.GONE
        mBinding.appBar.imgMapPin.visibility = View.GONE
        mBinding.appBar.floatBtnAdd.show()
    }

    private fun getFeatureLayer(idLayer: String): FeatureLayer? {
        for (featureLayerDTG in mFeatureLayerDTGS!!) {
            val featureLayer = featureLayerDTG.featureLayer
            val id = featureLayer.id
            if (id == idLayer)
                return featureLayer
        }
        return null
    }

    private fun getServiceFeatureTable(idLayer: String): ServiceFeatureTable? {
        for (featureLayerDTG in mFeatureLayerDTGS!!) {
            val id = featureLayerDTG.featureLayer.id
            if (id == idLayer)
                return featureLayerDTG.featureLayer.featureTable as ServiceFeatureTable
        }
        return null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_search -> {
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val intent: Intent
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_home -> {
                finish()
                startActivity(getIntent())
            }
            R.id.nav_thongke -> thongKe!!.start()
            R.id.nav_chuthich -> {
                intent = Intent(this, LayerActivity::class.java)
                this.startActivity(intent)
            }
            R.id.nav_thaotacbando -> showDialogSelectLayer()

            R.id.nav_visible_float_button -> toogleFloatButton()
            R.id.nav_logOut -> startSignIn()
            R.id.nav_reload -> initMapView()
            R.id.nav_delete_searching -> {
                mGraphicsOverlay!!.graphics.clear()
                mSearchAdapter!!.clear()
                mSearchAdapter!!.notifyDataSetChanged()
            }
            else -> {
            }
        }


        mBinding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    fun requestPermisson() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CALL_PHONE, Manifest.permission.READ_PHONE_STATE), REQUEST_ID_IMAGE_CAPTURE)
        }
        //        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this,
        //                android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
        //                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        //                        != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
        //                this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
        //        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mLocationDisplay!!.startAsync()

        } else {
            Toast.makeText(this@MainActivity, resources.getString(R.string.location_permission_denied), Toast.LENGTH_SHORT).show()
        }
    }

    private fun hiddenFloatButton() {
        mBinding.appBar.floatBtnLayer.hide()
        mBinding.appBar.floatBtnLocation.hide()
        mBinding.appBar.floatBtnAdd.hide()
    }

    private fun showFloatButton() {
        mBinding.appBar.floatBtnLayer.show()
        mBinding.appBar.floatBtnLocation.show()
        mBinding.appBar.floatBtnAdd.show()
    }

    private fun toogleFloatButton() {
        if (mBinding.appBar.floatBtnLayer.isOrWillBeShown)
            mBinding.appBar.floatBtnLayer.hide()
        else
            mBinding.appBar.floatBtnLayer.show()

        if (mBinding.appBar.floatBtnLocation.isOrWillBeShown)
            mBinding.appBar.floatBtnLocation.hide()
        else
            mBinding.appBar.floatBtnLocation.show()

        if (mBinding.appBar.floatBtnAdd.isOrWillBeShown)
            mBinding.appBar.floatBtnAdd.hide()
        else
            mBinding.appBar.floatBtnAdd.show()

    }

    private fun setOnClickListener() {
        mBinding.appBar.layoutLayerOpenStreetMap.setOnClickListener(this)
        mBinding.appBar.layoutLayerStreetMap.setOnClickListener(this)
        mBinding.appBar.layoutLayerTopo.setOnClickListener(this)
        mBinding.appBar.floatBtnAdd.setOnClickListener(this)
        mBinding.appBar.btnAddFeatureClose.setOnClickListener(this)
        mBinding.appBar.imgLayvitri.setOnClickListener(this)


        mBinding.appBar.content.imgSelectLayer.setOnClickListener(this)
        mBinding.appBar.content.imgClearSelectLayer.setOnClickListener(this)
        mBinding.appBar.floatBtnLayer.setOnClickListener(this)
        mBinding.appBar.btnLayerClose.setOnClickListener(this)
        mBinding.appBar.floatBtnLocation.setOnClickListener(this)

    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.img_selectLayer -> showDialogSelectTypeSearch()
            R.id.img_clearSelectLayer -> {
                mBinding.appBar.content.txtTitleSearch.text = getString(R.string.nav_find_address)
                isSearchingFeature = false
            }
            R.id.floatBtnLayer -> {
                toogleFloatButton()

                mBinding.appBar.layoutLayer.visibility = View.VISIBLE
                mCurrentPoint = mBinding.appBar.content.mapView.getCurrentViewpoint(Viewpoint.Type.CENTER_AND_SCALE).targetGeometry.extent.center
            }
            R.id.layout_layer_open_street_map -> {
                //                mapView.getMap().setMaxScale(1128.497175);
                mBinding.appBar.content.mapView.map.basemap = Basemap.createOpenStreetMap()
                handlingColorBackgroundLayerSelected(R.id.layout_layer_open_street_map)
                mBinding.appBar.content.mapView.getCurrentViewpoint(Viewpoint.Type.CENTER_AND_SCALE)

                setViewPointCenter(mCurrentPoint)
            }
            R.id.layout_layer_street_map -> {
                //                mapView.getMap().setMaxScale(1128.497176);
                mBinding.appBar.content.mapView.map.basemap = Basemap.createStreets()
                handlingColorBackgroundLayerSelected(R.id.layout_layer_street_map)

                setViewPointCenter(mCurrentPoint)
            }
            R.id.layout_layer_topo -> {
                mBinding.appBar.content.mapView.map.maxScale = 5.0
                mBinding.appBar.content.mapView.map.basemap = Basemap.createImageryWithLabels()
                handlingColorBackgroundLayerSelected(R.id.layout_layer_topo)

                setViewPointCenter(mCurrentPoint)
            }
            R.id.btn_layer_close -> {
                mBinding.appBar.layoutLayer.visibility = View.INVISIBLE
                toogleFloatButton()
            }
            R.id.floatBtnLocation -> if (!mLocationDisplay!!.isStarted) {
                mLocationDisplay!!.startAsync()
                setViewPointCenter(mLocationDisplay!!.mapLocation)
            } else
                mLocationDisplay!!.stop()
            R.id.floatBtnAdd -> showDialogSelectAddFeatureLayer()
            R.id.btn_add_feature_close -> closeAddFeature()
            R.id.img_layvitri -> mMapViewHandler!!.addFeature()
        }
    }


    @SuppressLint("ResourceAsColor")
    private fun handlingColorBackgroundLayerSelected(id: Int) {
        when (id) {
            R.id.layout_layer_open_street_map -> {
                mBinding.appBar.imgLayerOpenStreetMap.setBackgroundResource(R.drawable.layout_shape_basemap)
                mBinding.appBar.txtLayerOpenStreetMap.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                mBinding.appBar.imgLayerStreetMap.setBackgroundResource(R.drawable.layout_shape_basemap_none)
                mBinding.appBar.txtLayerStreetMap.setTextColor(ContextCompat.getColor(this, R.color.colorBlack))
                mBinding.appBar.imgLayerTopo.setBackgroundResource(R.drawable.layout_shape_basemap_none)
                mBinding.appBar.txtLayerTopo.setTextColor(ContextCompat.getColor(this, R.color.colorBlack))
            }
            R.id.layout_layer_street_map -> {
                mBinding.appBar.imgLayerStreetMap.setBackgroundResource(R.drawable.layout_shape_basemap)
                mBinding.appBar.txtLayerStreetMap.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                mBinding.appBar.imgLayerOpenStreetMap.setBackgroundResource(R.drawable.layout_shape_basemap_none)
                mBinding.appBar.txtLayerOpenStreetMap.setTextColor(ContextCompat.getColor(this, R.color.colorBlack))
                mBinding.appBar.imgLayerTopo.setBackgroundResource(R.drawable.layout_shape_basemap_none)
                mBinding.appBar.txtLayerTopo.setTextColor(ContextCompat.getColor(this, R.color.colorBlack))
            }
            R.id.layout_layer_topo -> {
                mBinding.appBar.imgLayerTopo.setBackgroundResource(R.drawable.layout_shape_basemap)
                mBinding.appBar.txtLayerTopo.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                mBinding.appBar.imgLayerOpenStreetMap.setBackgroundResource(R.drawable.layout_shape_basemap_none)
                mBinding.appBar.txtLayerOpenStreetMap.setTextColor(ContextCompat.getColor(this, R.color.colorBlack))
                mBinding.appBar.imgLayerStreetMap.setBackgroundResource(R.drawable.layout_shape_basemap_none)
                mBinding.appBar.txtLayerStreetMap.setTextColor(ContextCompat.getColor(this, R.color.colorBlack))
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            when (requestCode) {
                1 -> if (resultCode == Activity.RESULT_OK && mMapViewHandler != null) {
                    if (resultCode == Activity.RESULT_OK) {
                        val objectid = data!!.getIntExtra(getString(R.string.ket_qua_objectid), 1)
                        mMapViewHandler!!.queryByObjectID(objectid)
                    }
                }
                Constant.RequestCode.LOGIN -> if (resultCode == Activity.RESULT_OK) {
                    startMain()
                } else finish()
                Constant.RequestCode.UPDATE -> mPopup!!.refreshPopup()
//                Constant.RequestCode.LIST_TASK -> if (resultCode == Activity.RESULT_OK) handlingListTaskActivityResult()
//                Constant.RequestCode.ADD -> if (resultCode == Activity.RESULT_OK) {
//                    handlingAddFeatureSuccess()
//                } else {
//                    handlingCancelAdd()
//                }
//                Constant.RequestCode.UPDATE -> mPopUp!!.refreshPopup(mApplication!!.selectedArcGISFeature)
//                Constant.RequestCode.REQUEST_ID_UPDATE_ATTACHMENT -> if (resultCode == Activity.RESULT_OK) {
//                    if (mUri != null) {
//                        val bitmap = getBitmap(mUri!!.path)
//                        try {
//                            if (bitmap != null) {
//                                val matrix = Matrix()
//                                val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
//                                val outputStream = ByteArrayOutputStream()
//                                rotatedBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
//                                val image = outputStream.toByteArray()
//                                Toast.makeText(this, "Đã lưu ảnh", Toast.LENGTH_SHORT).show()
//                                val updateAttachmentAsync = UpdateAttachmentAsync(this, mSelectedArcGISFeature!!, image)
//                                updateAttachmentAsync.execute()
//                            }
//                        } catch (ignored: Exception) {
//                        }
//                    }
//                } else if (resultCode == Activity.RESULT_CANCELED) {
//                    make(mapView, "Hủy chụp ảnh", false)
//                } else {
//                    make(mapView, "Lỗi khi chụp ảnh", false)
//                }
//                else -> {
//                }
            }
        } catch (ignored: Exception) {
        }
    }

    companion object {
        private val REQUEST_ID_IMAGE_CAPTURE = 55
        private val REQUEST_ID_IMAGE_CAPTURE_POPUP = 44
    }

}