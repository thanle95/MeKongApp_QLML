package mekong.ditagis.com.qlts.tools

import java.util.ArrayList

import mekong.ditagis.com.qlts.MainActivity
import mekong.ditagis.com.qlts.adapter.FeatureLayerAdapter
import mekong.ditagis.com.qlts.libs.FeatureLayerDTG

/**
 * Created by NGUYEN HONG on 4/26/2018.
 */

class SearchItem(private val mFeatureLayerDTGS: List<FeatureLayerDTG>, quanLyTaiSan: MainActivity) {
    private var items: MutableList<FeatureLayerAdapter.Item>? = null

    init {
        items = ArrayList()
        for (featureLayerDTG in mFeatureLayerDTGS) {
            if (featureLayerDTG.action != null && featureLayerDTG.action!!.isView)
                items!!.add(FeatureLayerAdapter.Item(featureLayerDTG.featureLayer.name, featureLayerDTG.featureLayer.id,
                featureLayerDTG.featureLayer))
        }

    }

    fun getItems(): MutableList<FeatureLayerAdapter.Item>? {
        return items
    }

    fun setItems(items: MutableList<FeatureLayerAdapter.Item>) {
        this.items = items
    }
}
