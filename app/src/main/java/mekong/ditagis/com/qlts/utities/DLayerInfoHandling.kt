package mekong.ditagis.com.qlts.utities

import android.app.Activity
import com.esri.arcgisruntime.data.ServiceFeatureTable
import mekong.ditagis.com.qlts.entities.DLayerInfo

object DLayerInfoHandling {
    fun getDLayerInfo(activity: Activity, serviceFeatureTable: ServiceFeatureTable): DLayerInfo? {
        val application = activity.application as DApplication
        for (layerInfo in application.layerInfos!!) {
            if (serviceFeatureTable.uri.contains(layerInfo.url)) {
                return layerInfo
            }
        }
        return null
    }
}
