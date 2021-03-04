package mekong.ditagis.com.qlts.tools

import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.esri.arcgisruntime.data.QueryParameters
import mekong.ditagis.com.qlts.MainActivity
import mekong.ditagis.com.qlts.adapter.ThongKeAdapter
import mekong.ditagis.com.qlts.databinding.LayoutTitleListviewBinding
import mekong.ditagis.com.qlts.libs.FeatureLayerDTG
import java.util.*
import java.util.concurrent.ExecutionException


class ThongKe {
    lateinit var mainActivity: MainActivity
    private var mFeatureLayerDTGS: List<FeatureLayerDTG>? = null
    private lateinit var items: MutableList<ThongKeAdapter.Item>
    private var thongKeAdapter: ThongKeAdapter? = null
    private var selectTimeDialog: AlertDialog? = null

    constructor() {}

    constructor(mainActivity: MainActivity, mFeatureLayerDTGS: List<FeatureLayerDTG>) {
        this.mainActivity = mainActivity
        this.mFeatureLayerDTGS = mFeatureLayerDTGS
        setup()
    }

    fun getmFeatureLayerDTGS(): List<FeatureLayerDTG>? {
        return mFeatureLayerDTGS
    }

    fun setmFeatureLayerDTGS(mFeatureLayerDTGS: List<FeatureLayerDTG>) {
        this.mFeatureLayerDTGS = mFeatureLayerDTGS
    }

    fun setup() {
        items = ArrayList()
        thongKeAdapter = ThongKeAdapter(mainActivity, items)
        val builder = AlertDialog.Builder(mainActivity!!, android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen)
        val bindingLayout = LayoutTitleListviewBinding.inflate(mainActivity.layoutInflater)
        val imageView = bindingLayout.imgRefresh
        imageView.setOnClickListener {
            refresh()
            Toast.makeText(mainActivity, "Đã làm mới dữ liệu", Toast.LENGTH_LONG).show()
        }
        val listView = bindingLayout.listview
        listView.adapter = thongKeAdapter
        builder.setView(bindingLayout.root)
        selectTimeDialog = builder.create()
        selectTimeDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        refresh()
    }

    fun start() {
        selectTimeDialog!!.show()
    }

    fun refresh() {

        items!!.clear()
        val queryParameters = QueryParameters()
        val queryClause = "1=1"
        queryParameters.whereClause = queryClause
        queryParameters.isReturnGeometry = false
        for (featureLayerDTG in mFeatureLayerDTGS!!) {
            if (featureLayerDTG.action != null && featureLayerDTG.action!!.isView) {
                val longListenableFuture = featureLayerDTG.featureLayer.featureTable.queryFeatureCountAsync(queryParameters)
                longListenableFuture.addDoneListener {
                    try {

                        val aLong = longListenableFuture.get()
                        val item = ThongKeAdapter.Item(featureLayerDTG.featureLayer.name, aLong)
                        items!!.add(item)
                        thongKeAdapter!!.notifyDataSetChanged()

                    } catch (e: InterruptedException) {
                    } catch (e: ExecutionException) {
                    }
                }
            }
        }
    }
}
