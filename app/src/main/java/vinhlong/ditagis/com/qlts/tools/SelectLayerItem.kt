package vinhlong.ditagis.com.qlts.tools

import java.util.ArrayList

import vinhlong.ditagis.com.qlts.R
import vinhlong.ditagis.com.qlts.MainActivity
import vinhlong.ditagis.com.qlts.adapter.FeatureLayerAdapter
import vinhlong.ditagis.com.qlts.libs.FeatureLayerDTG

/**
 * Created by NGUYEN HONG on 4/26/2018.
 */

class SelectLayerItem(private val mFeatureLayerDTGS: List<FeatureLayerDTG>, quanLyTaiSan: MainActivity) {
    private var items: MutableList<FeatureLayerAdapter.Item>? = null

    init {
        items = ArrayList()
        for (featureLayerDTG in mFeatureLayerDTGS) {
            if (featureLayerDTG.action != null && featureLayerDTG.action!!.isView && featureLayerDTG.featureLayer.isVisible)
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
