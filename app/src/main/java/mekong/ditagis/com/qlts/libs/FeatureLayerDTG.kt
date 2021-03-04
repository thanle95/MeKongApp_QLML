package mekong.ditagis.com.qlts.libs


import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.view.MapView

/**
 * Created by NGUYEN HONG on 3/14/2018.
 */

class FeatureLayerDTG(val featureLayer: FeatureLayer) {


    private val mMapView: MapView? = null


    var outFields: Array<String>? = null
    var queryFields: Array<String>? = null
    var updateFields: Array<String>? = null
    var groupLayer: String? = null
    var action: Action? = null


}
