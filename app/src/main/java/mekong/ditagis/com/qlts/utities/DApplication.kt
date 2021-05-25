package mekong.ditagis.com.qlts.utities

import android.app.Application
import android.graphics.Bitmap
import com.esri.arcgisruntime.data.Attachment
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.Point
import mekong.ditagis.com.qlts.entities.*
import java.util.*

class DApplication : Application() {
    var idFeatureLayerToAdd: HashMap<Int, FeatureLayerValueIDField> = hashMapOf()
    var address: String? = null
    var center: Point? = null
    var user: User? = null
    var progressDialog= DProgressDialog ()
    var layerVisible: HashMap<Any, Boolean> = HashMap()
    var bitmaps: ArrayList<Bitmap>? = null
    var selectedFeature: Feature? = null
    var layerLegendList: MutableList<LayerLegend> = ArrayList()
    var selectedAttachment: Attachment? = null
    var selectedBitmap: Bitmap? = null
    var SFTAdministrator: ServiceFeatureTable? = null
    var appInfo: DAppInfo? = null
    var layerInfos: List<DLayerInfo>? = null
     var objectIDAddFeature: Long = 0
    lateinit var alertDialog: DAlertDialog
    var images: List<ByteArray>? = null
    var statusCode = Constant.StatusCode.NORMAL.value
}
