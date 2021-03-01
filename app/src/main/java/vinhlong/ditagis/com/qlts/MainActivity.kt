package vinhlong.ditagis.com.qlts

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
import android.widget.AdapterView
import android.widget.CheckBox
import android.widget.SeekBar
import android.widget.Toast
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
import kotlinx.android.synthetic.main.activity_quan_ly_tai_san.*
import kotlinx.android.synthetic.main.app_bar.*
import kotlinx.android.synthetic.main.content.*
import kotlinx.android.synthetic.main.layout_title_listview.view.*
import kotlinx.android.synthetic.main.nav_header.view.*
import vinhlong.ditagis.com.qlts.adapter.FeatureLayerAdapter
import vinhlong.ditagis.com.qlts.adapter.ObjectsAdapter
import vinhlong.ditagis.com.qlts.async.GetLegendAsync
import vinhlong.ditagis.com.qlts.async.PreparingAsycn
import vinhlong.ditagis.com.qlts.entities.entitiesDB.ListObjectDB
import vinhlong.ditagis.com.qlts.libs.Action
import vinhlong.ditagis.com.qlts.libs.FeatureLayerDTG
import vinhlong.ditagis.com.qlts.tools.*
import vinhlong.ditagis.com.qlts.utities.*
import java.io.IOException
import java.util.*
import java.util.concurrent.atomic.AtomicInteger


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
    private var mUri: Uri? = null
    private var popupInfos: Popup? = null
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

        setContentView(R.layout.activity_quan_ly_tai_san)
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
        setSupportActionBar(toolbar)
        // permisson
        requestPermisson()

        val toggle = ActionBarDrawerToggle(this, main_activity_drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        main_activity_drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)

        // khởi tạo chức năng tìm kiếm
        initListViewSearch()

        // ẩn hiện thị lớp dữ liệu
        initLayerListView()


        initMapView()
        setOnClickListener()
        setLoginInfos()
    }

    private fun setLoginInfos() {
        val application = application as DApplication
        val displayName = application.user!!.displayName
        nav_view.setNavigationItemSelectedListener(this)
        val headerLayout = nav_view.getHeaderView(0)
        val nav_name_nv = headerLayout.nav_name_nv
        nav_name_nv.text = displayName
    }

    private fun initMapView() {

        mMap = ArcGISMap(Basemap.Type.OPEN_STREET_MAP, 10.107553, 105.8461029, 12)

        mapView!!.map = mMap
        mApplication.progressDialog.changeTitle(this,main_activity_drawer_layout, "Đang lấy dữ liệu..." )
        val preparingAsycn = PreparingAsycn(this, mApplication!!, object : PreparingAsycn.AsyncResponse {
            override fun processFinish(output: Void?) {
                mApplication.progressDialog.changeTitle(this@MainActivity, main_activity_drawer_layout, "Đang tải bản đồ...")
                setFeatureService()
            }
        })
        if (CheckConnectInternet.isOnline(this))
            preparingAsycn.execute()

        changeStatusOfLocationDataSource()
        mLocationDisplay!!.addLocationChangedListener { locationChangedEvent -> }
        mGraphicsOverlay = GraphicsOverlay()
        mapView!!.graphicsOverlays.add(mGraphicsOverlay)
        mApplication.progressDialog.changeTitle(this, main_activity_drawer_layout, "Đang khởi tạo ứng dụng...")
    }

    private fun setFeatureService() {
        // config feature layer service
        mFeatureLayerDTGS = ArrayList()
        mCallout = mapView!!.callout
        mMapViewHandler = MapViewHandler(mapView!!, this@MainActivity)
        popupInfos = Popup(this@MainActivity, mapView!!, mCallout)
        val size = AtomicInteger(ListObjectDB.getInstance().lstFeatureLayerDTG!!.size)
        val layerVisible = HashMap<Any, Boolean>()
        for (layerInfoDTG in ListObjectDB.getInstance().lstFeatureLayerDTG!!) {
            if (layerInfoDTG.id!!.substring(layerInfoDTG.id!!.length - 3) == "TBL" || !layerInfoDTG.isView || layerInfoDTG.id == "diemsucoLYR") {
                size.decrementAndGet()
                continue
            }
            var url = layerInfoDTG.url
            if (!layerInfoDTG.url!!.startsWith("http"))
                url = "http:" + layerInfoDTG.url!!
            if (layerInfoDTG.id!!.toUpperCase() == getString(R.string.IDLayer_Basemap)) {
                hanhChinhImageLayers = ArcGISMapImageLayer(url!!)
                hanhChinhImageLayers!!.id = layerInfoDTG.id!!
                mapView!!.map.operationalLayers.add(hanhChinhImageLayers)
                val finalUrl = url
                hanhChinhImageLayers!!.addDoneLoadingListener {
                    size.decrementAndGet()
                    if (hanhChinhImageLayers!!.loadStatus == LoadStatus.LOADED) {
                        val sublayerList = hanhChinhImageLayers!!.sublayers
                        for (sublayer in sublayerList) {
                            addCheckBox_SubLayer(sublayer as ArcGISMapImageSublayer)
                            layerVisible[sublayer] = hanhChinhImageLayers!!.isVisible
                        }
                        val url_HanhChinh = "$finalUrl/5"
                        val serviceFeatureTable = ServiceFeatureTable(url_HanhChinh)
                        popupInfos!!.setmSFTHanhChinh(serviceFeatureTable)
                    }
                    if (size.get() == 0) {
                        mApplication!!.layerVisible = layerVisible
                        getLegend()
                    }
                }
                hanhChinhImageLayers!!.loadAsync()
            } else if (layerInfoDTG.isView) {
                val serviceFeatureTable = ServiceFeatureTable(url!!)
                val featureLayer = FeatureLayer(serviceFeatureTable)
                featureLayer.name = layerInfoDTG.titleLayer
                featureLayer.id = layerInfoDTG.id!!
                if (layerInfoDTG.definition != null && layerInfoDTG.definition != "null")
                    featureLayer.definitionExpression = layerInfoDTG.definition
                mMap!!.operationalLayers.add(featureLayer)
                val featureLayerDTG = FeatureLayerDTG(featureLayer)
                val action = Action(layerInfoDTG.isView, layerInfoDTG.isCreate, layerInfoDTG.isEdit, layerInfoDTG.isDelete)
                featureLayerDTG.action = action
                featureLayerDTG.outFields = getFieldsDTG(layerInfoDTG.outField)
                featureLayerDTG.queryFields = getFieldsDTG(layerInfoDTG.outField)
                featureLayerDTG.updateFields = getFieldsDTG(layerInfoDTG.outField)
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

        mMapViewHandler!!.setmPopUp(popupInfos!!)
        mMapViewHandler!!.setFeatureLayerDTGs(mFeatureLayerDTGS!!)
        thongKe = ThongKe(this, mFeatureLayerDTGS!!)
        mapViewEvent()
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun mapViewEvent() {
        mapView!!.onTouchListener = object : DefaultMapViewOnTouchListener(this, mapView) {
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
                    txt_toado.text = text
                    txt_toado_add.text = text
                }
                return super.onScroll(e1, e2, distanceX, distanceY)
            }

        }
        skbr_hanhchinh_layers.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
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
        mApplication.progressDialog.changeTitle(this, main_activity_drawer_layout, "Đang tải chú thích...")
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
        cb_Layer_TaiSan!!.setOnCheckedChangeListener { buttonView, isChecked ->
            for (i in 0 until linnearDisplayLayerTaiSan!!.childCount) {
                val view = linnearDisplayLayerTaiSan!!.getChildAt(i)
                if (view is CheckBox) {
                    view.isChecked = isChecked
                }
            }
        }
        cb_Layer_HanhChinh!!.setOnCheckedChangeListener { buttonView, isChecked ->
            for (i in 0 until linnearDisplayLayerBaseMap!!.childCount) {
                val view = linnearDisplayLayerBaseMap!!.getChildAt(i)
                if (view is CheckBox) {
                    if (isChecked)
                        view.isChecked = true
                    else
                        view.isChecked = false
                }
            }
        }
    }


    private fun initListViewSearch() {
        //đưa listview search ra phía sau
        this.lstview_search!!.invalidate()
        val items = ArrayList<ObjectsAdapter.Item>()
        this.mSearchAdapter = ObjectsAdapter(this@MainActivity, items)
        this.lstview_search!!.adapter = mSearchAdapter
        this.lstview_search!!.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
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

    private fun addCheckBox_SubLayer(layer: ArcGISMapImageSublayer) {
        val checkBox = CheckBox(linnearDisplayLayerBaseMap!!.context)
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
        linnearDisplayLayerBaseMap!!.addView(checkBox)
    }

    private fun addCheckBox_TaiSanLayer(featureLayer: FeatureLayer) {
        val checkBox = CheckBox(linnearDisplayLayerTaiSan!!.context)
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
        linnearDisplayLayerTaiSan!!.addView(checkBox)
    }

    private fun changeStatusOfLocationDataSource() {
        mLocationDisplay = mapView!!.locationDisplay
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
        mapView!!.setViewpointCenterAsync(geometry.extent.center)
    }

    private fun setViewPointCenterLongLat(position: Point) {
        val geometry = GeometryEngine.project(position, SpatialReferences.getWgs84())
        val geometry1 = GeometryEngine.project(geometry, SpatialReferences.getWebMercator())
        val point = geometry1.extent.center

        val symbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CROSS, Color.RED, 20f)
        val graphic = Graphic(point, symbol)
        mGraphicsOverlay!!.graphics.add(graphic)

        mapView!!.setViewpointCenterAsync(point)
    }

    override fun onBackPressed() {
        if (main_activity_drawer_layout.isDrawerOpen(GravityCompat.START)) {
            main_activity_drawer_layout.closeDrawer(GravityCompat.START)
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
                    mMapViewHandler!!.querySearch(query, lstview_search!!, mSearchAdapter!!)
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
                layout_tim_kiem!!.visibility = View.VISIBLE
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                showFloatButton()
                layout_tim_kiem!!.visibility = View.INVISIBLE
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
        @SuppressLint("InflateParams") val layout = layoutInflater.inflate(R.layout.layout_title_listview, null)
        val listView = layout.listview
        listView.adapter = featureLayerAdapter
        layout.txt_Title_Layout.text = "Chọn lớp dữ liệu cập nhật"
        builder.setView(layout)
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
            txt_title_search.text = itemAtPosition.titleLayer
            val featureLayer = getFeatureLayer(idLayer!!)
            mMapViewHandler!!.setAddSFT(featureLayer!!.featureTable as ServiceFeatureTable)
            mMapViewHandler!!.setIdentifyFeatureLayer(featureLayer)
            featureLayer.isVisible = true
            linear_addfeature.visibility = View.VISIBLE
            img_map_pin.visibility = View.VISIBLE
            floatBtnAdd.hide()
        }
    }

     fun showDialogSelectLayer() {
        val selectLayerItem = SelectLayerItem(mFeatureLayerDTGS!!, this)
        val items = selectLayerItem.getItems()
        val featureLayerAdapter = FeatureLayerAdapter(this, items!!)
        val builder = AlertDialog.Builder(this, android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen)
        @SuppressLint("InflateParams") val layout = layoutInflater.inflate(R.layout.layout_title_listview, null)
        val listView = layout.listview
        listView.adapter = featureLayerAdapter
        val txt_Title_Layout = layout.txt_Title_Layout
        layout.img_refresh.visibility = View.GONE
        txt_Title_Layout.text = "Chọn lớp dữ liệu thao tác trên bản đồ"
        builder.setView(layout)
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
        @SuppressLint("InflateParams") val layout = layoutInflater.inflate(R.layout.layout_title_listview, null)
        val listView = layout.listview
        listView.adapter = featureLayerAdapter
        val txt_Title_Layout = layout.txt_Title_Layout
        txt_Title_Layout.text = "Tìm kiếm theo"
        builder.setView(layout)
        val selectTimeDialog = builder.create()
        selectTimeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        selectTimeDialog.show()
        val finalItems = featureLayerAdapter.getItems()
        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            selectTimeDialog.dismiss()
            val itemAtPosition = parent.getItemAtPosition(position) as FeatureLayerAdapter.Item
            val idLayer = itemAtPosition.idLayer
            txt_title_search.text = itemAtPosition.titleLayer
            val featureLayer = getFeatureLayer(idLayer!!)
            featureLayer!!.isVisible = true
            mMapViewHandler!!.setSearchSFT(featureLayer.featureTable as ServiceFeatureTable)
            //                mapView.getMap().setMaxScale(featureLayer.getMaxScale());
            mMapViewHandler!!.setIdentifyFeatureLayer(featureLayer)
            isSearchingFeature = true
        }
    }

    fun closeAddFeature() {
        linear_addfeature.visibility = View.GONE
        img_map_pin.visibility = View.GONE
        floatBtnAdd.show()
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
            R.id.nav_logOut -> this.finish()
            R.id.nav_reload -> initMapView()
            R.id.nav_delete_searching -> {
                mGraphicsOverlay!!.graphics.clear()
                mSearchAdapter!!.clear()
                mSearchAdapter!!.notifyDataSetChanged()
            }
            else -> {
            }
        }


        main_activity_drawer_layout.closeDrawer(GravityCompat.START)
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
        floatBtnLayer.hide()
        floatBtnLocation.hide()
        floatBtnAdd.hide()
    }

    private fun showFloatButton() {
        floatBtnLayer.show()
        floatBtnLocation.show()
        floatBtnAdd.show()
    }

    private fun toogleFloatButton() {
        if(floatBtnLayer.isOrWillBeShown)
            floatBtnLayer.hide()
        else
            floatBtnLayer.show()

        if(floatBtnLocation.isOrWillBeShown)
            floatBtnLocation.hide()
        else
            floatBtnLocation.show()

        if(floatBtnAdd.isOrWillBeShown)
            floatBtnAdd.hide()
        else
            floatBtnAdd.show()

    }

    private fun setOnClickListener() {
       layout_layer_open_street_map.setOnClickListener(this)
     layout_layer_street_map.setOnClickListener(this)
     layout_layer_topo.setOnClickListener(this)
     floatBtnAdd.setOnClickListener(this)
     btn_add_feature_close.setOnClickListener(this)
     img_layvitri.setOnClickListener(this)


     img_selectLayer.setOnClickListener(this)
     img_clearSelectLayer.setOnClickListener(this)
     floatBtnLayer.setOnClickListener(this)
     btn_layer_close.setOnClickListener(this)
     floatBtnLocation.setOnClickListener(this)

    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.img_selectLayer -> showDialogSelectTypeSearch()
            R.id.img_clearSelectLayer -> {
               txt_title_search.text = getString(R.string.nav_find_address)
                isSearchingFeature = false
            }
            R.id.floatBtnLayer -> {
                toogleFloatButton()

              layout_layer.visibility = View.VISIBLE
                mCurrentPoint = mapView!!.getCurrentViewpoint(Viewpoint.Type.CENTER_AND_SCALE).targetGeometry.extent.center
            }
            R.id.layout_layer_open_street_map -> {
                //                mapView.getMap().setMaxScale(1128.497175);
                mapView!!.map.basemap = Basemap.createOpenStreetMap()
                handlingColorBackgroundLayerSelected(R.id.layout_layer_open_street_map)
                mapView!!.getCurrentViewpoint(Viewpoint.Type.CENTER_AND_SCALE)

                setViewPointCenter(mCurrentPoint)
            }
            R.id.layout_layer_street_map -> {
                //                mapView.getMap().setMaxScale(1128.497176);
                mapView!!.map.basemap = Basemap.createStreets()
                handlingColorBackgroundLayerSelected(R.id.layout_layer_street_map)

                setViewPointCenter(mCurrentPoint)
            }
            R.id.layout_layer_topo -> {
                mapView!!.map.maxScale = 5.0
                mapView!!.map.basemap = Basemap.createImageryWithLabels()
                handlingColorBackgroundLayerSelected(R.id.layout_layer_topo)

                setViewPointCenter(mCurrentPoint)
            }
            R.id.btn_layer_close -> {
             layout_layer.visibility = View.INVISIBLE
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
               img_layer_open_street_map.setBackgroundResource(R.drawable.layout_shape_basemap)
               txt_layer_open_street_map.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
               img_layer_street_map.setBackgroundResource(R.drawable.layout_shape_basemap_none)
               txt_layer_street_map.setTextColor(ContextCompat.getColor(this, R.color.colorBlack))
               img_layer_topo.setBackgroundResource(R.drawable.layout_shape_basemap_none)
               txt_layer_topo.setTextColor(ContextCompat.getColor(this, R.color.colorBlack))
            }
            R.id.layout_layer_street_map -> {
               img_layer_street_map.setBackgroundResource(R.drawable.layout_shape_basemap)
               txt_layer_street_map.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
               img_layer_open_street_map.setBackgroundResource(R.drawable.layout_shape_basemap_none)
               txt_layer_open_street_map.setTextColor(ContextCompat.getColor(this, R.color.colorBlack))
               img_layer_topo.setBackgroundResource(R.drawable.layout_shape_basemap_none)
               txt_layer_topo.setTextColor(ContextCompat.getColor(this, R.color.colorBlack))
            }
            R.id.layout_layer_topo -> {
               img_layer_topo.setBackgroundResource(R.drawable.layout_shape_basemap)
               txt_layer_topo.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
               img_layer_open_street_map.setBackgroundResource(R.drawable.layout_shape_basemap_none)
               txt_layer_open_street_map.setTextColor(ContextCompat.getColor(this, R.color.colorBlack))
               img_layer_street_map.setBackgroundResource(R.drawable.layout_shape_basemap_none)
               txt_layer_street_map.setTextColor(ContextCompat.getColor(this, R.color.colorBlack))
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            val objectid = data!!.getIntExtra(getString(R.string.ket_qua_objectid), 1)
            if (requestCode == 1) {
                if (resultCode == Activity.RESULT_OK) {
                    mMapViewHandler!!.queryByObjectID(objectid)
                }
            }
        } catch (ignored: Exception) {
        }

//        if (requestCode == resources.getInteger(R.integer.REQUEST_ID_UPDATE_ATTACHMENT)) {
//            if (resultCode == Activity.RESULT_OK) {
//                if (this.mUri != null) {
//                    val bitmap = getBitmap(mUri!!.path)
//                    try {
//                        if (bitmap != null) {
//                            val matrix = Matrix()
//                            //                            matrix.postRotate(90);
//                            val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
//                            val outputStream = ByteArrayOutputStream()
//                            rotatedBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
//                            val image = outputStream.toByteArray()
//
//                            val updateAttachmentAsync = UpdateAttachmentAsync(this, mSelectedArcGISFeature!!, image)
//                            updateAttachmentAsync.execute()
//                        }
//                    } catch (ignored: Exception) {
//                    }
//
//                }
//            } else if (resultCode == Activity.RESULT_CANCELED) {
//                MySnackBar.make(mapView!!, "Hủy chụp ảnh", false)
//            } else {
//                MySnackBar.make(mapView!!, "Lỗi khi chụp ảnh", false)
//            }
//        }
    }

    companion object {
        private val REQUEST_ID_IMAGE_CAPTURE = 55
        private val REQUEST_ID_IMAGE_CAPTURE_POPUP = 44
    }

}