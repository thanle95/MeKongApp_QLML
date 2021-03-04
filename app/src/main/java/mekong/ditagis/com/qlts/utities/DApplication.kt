package mekong.ditagis.com.qlts.utities

import android.app.Application
import android.graphics.Bitmap
import com.esri.arcgisruntime.data.Attachment
import com.esri.arcgisruntime.data.Feature
import mekong.ditagis.com.qlts.entities.DAppInfo
import mekong.ditagis.com.qlts.entities.DLayerInfo
import mekong.ditagis.com.qlts.entities.LayerLegend
import mekong.ditagis.com.qlts.entities.entitiesDB.User
import java.util.*

class DApplication : Application() {
    var user: User? = null
    var progressDialog= DProgressDialog ()
    var layerVisible: HashMap<Any, Boolean> = HashMap()
    var bitmaps: ArrayList<Bitmap>? = null
    var selectedFeature: Feature? = null
    var layerLegendList: MutableList<LayerLegend> = ArrayList()
    var selectedAttachment: Attachment? = null
    var selectedBitmap: Bitmap? = null
    var appInfo: DAppInfo? = null
    var layerInfos: List<DLayerInfo>? = null
    private val objectIDAddFeature: Long = 0
    lateinit var alertDialog: DAlertDialog

}
