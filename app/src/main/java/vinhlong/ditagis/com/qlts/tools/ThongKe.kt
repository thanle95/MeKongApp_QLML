package vinhlong.ditagis.com.qlts.tools

import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.esri.arcgisruntime.data.QueryParameters
import kotlinx.android.synthetic.main.layout_title_listview.view.*
import vinhlong.ditagis.com.qlts.MainActivity
import vinhlong.ditagis.com.qlts.R
import vinhlong.ditagis.com.qlts.adapter.ThongKeAdapter
import vinhlong.ditagis.com.qlts.libs.FeatureLayerDTG
import java.util.*
import java.util.concurrent.ExecutionException

/**
 * Created by NGUYEN HONG on 6/15/2018.
 */

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
        val layout = mainActivity!!.layoutInflater.inflate(R.layout.layout_title_listview, null)
        val imageView = layout.img_refresh
        imageView.setOnClickListener {
            refress()
            Toast.makeText(mainActivity, "Đã làm mới dữ liệu", Toast.LENGTH_LONG).show()
        }
        val listView = layout.listview
        listView.adapter = thongKeAdapter
        builder.setView(layout)
        selectTimeDialog = builder.create()
        selectTimeDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        refress()
    }

    fun start() {
        selectTimeDialog!!.show()
    }

    fun refress() {

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
                        e.printStackTrace()
                    } catch (e: ExecutionException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}
