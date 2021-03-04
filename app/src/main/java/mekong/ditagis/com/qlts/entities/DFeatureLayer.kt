package mekong.ditagis.com.qlts.entities

import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.layers.FeatureLayer

/**
 * Created by NGUYEN HONG on 3/14/2018.
 */
class DFeatureLayer(val serviceFeatureTable: ServiceFeatureTable, val layer: FeatureLayer, private val dLayerInfo: DLayerInfo) {

    fun getdLayerInfo(): DLayerInfo {
        return dLayerInfo
    }

}