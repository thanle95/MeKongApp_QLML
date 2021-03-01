package vinhlong.ditagis.com.qlts.utities

import android.app.Application
import android.graphics.Bitmap
import com.esri.arcgisruntime.data.Attachment
import com.esri.arcgisruntime.data.Feature
import vinhlong.ditagis.com.qlts.entities.DLayerInfo
import vinhlong.ditagis.com.qlts.entities.LayerLegend
import vinhlong.ditagis.com.qlts.entities.entitiesDB.User
import java.util.*

class DApplication : Application() {
    var user: User? = null
    var progressDialog= DProgressDialog ()
    var layerVisible: HashMap<Any, Boolean> = HashMap()
    var bitmaps: ArrayList<Bitmap>? = null
    private val layerInfos: List<DLayerInfo>? = null
    var selectedFeature: Feature? = null
    var layerLegendList: MutableList<LayerLegend> = ArrayList()
    var selectedAttachment: Attachment? = null
    var selectedBitmap: Bitmap? = null

    private val objectIDAddFeature: Long = 0

}
