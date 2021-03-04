package mekong.ditagis.com.qlts.tools

import com.esri.arcgisruntime.geometry.GeometryType

import java.util.ArrayList

import mekong.ditagis.com.qlts.MainActivity
import mekong.ditagis.com.qlts.R
import mekong.ditagis.com.qlts.adapter.FeatureLayerAdapter
import mekong.ditagis.com.qlts.libs.FeatureLayerDTG

class AddFeatureItem(private val mFeatureLayerDTGS: List<FeatureLayerDTG>, quanLyTaiSan: MainActivity) {
    private var items: MutableList<FeatureLayerAdapter.Item>? = null

    init {
        items = ArrayList()
        for (featureLayerDTG in mFeatureLayerDTGS) {
            val geometryType = featureLayerDTG.featureLayer.featureTable.geometryType
            if (featureLayerDTG.action != null && featureLayerDTG.action!!.isCreate && featureLayerDTG.action!!.isView && geometryType == GeometryType.POINT)
                items!!.add(FeatureLayerAdapter.Item(quanLyTaiSan.getString(R.string.type_search_feature_layer), featureLayerDTG.featureLayer.name, featureLayerDTG.featureLayer.id))
        }

    }

    fun getItems(): MutableList<FeatureLayerAdapter.Item>? {
        return items
    }

    fun setItems(items: MutableList<FeatureLayerAdapter.Item>) {
        this.items = items
    }
}

