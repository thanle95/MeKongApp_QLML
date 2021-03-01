package vinhlong.ditagis.com.qlts.utities

import android.content.Context

/**
 * Created by NGUYEN HONG on 3/20/2018.
 */

class Config {
    var url: String? = null
    var queryField: Array<String>? = null
    var outField: Array<String>? = null
    var updateField: Array<String>? = null
    var titleLayer: String? = null
    var idLayer: String? = null
    var minScale: Int = 0
    private val mContext: Context? = null
    var groupLayer: String? = null

    private constructor() {}

    //    CONSTRUCTOR OF BASEMAP
    constructor(url: String, idLayer: String, titleLayer: String, minScale: Int, groupLayer: String) {
        this.idLayer = idLayer
        this.url = url
        this.titleLayer = titleLayer
        this.minScale = minScale
        this.groupLayer = groupLayer
    }

    constructor(url: String, idLayer: String, titleLayer: String, minScale: Int, groupLayer: String, updateField: Array<String>) {
        this.idLayer = idLayer
        this.url = url
        this.titleLayer = titleLayer
        this.minScale = minScale
        this.groupLayer = groupLayer
        this.updateField = updateField
    }

    constructor(url: String, titleLayer: String, minScale: Int, groupLayer: String, updateField: Array<String>) {
        this.url = url
        this.titleLayer = titleLayer
        this.minScale = minScale
        this.groupLayer = groupLayer
        this.updateField = updateField
    }

    constructor(url: String, queryField: Array<String>, outField: Array<String>, titleLayer: String, minScale: Int, updateField: Array<String>) {
        this.url = url
        this.queryField = queryField
        this.outField = outField
        this.updateField = updateField
        this.titleLayer = titleLayer
        this.minScale = minScale
    }

    constructor(url: String, queryField: Array<String>, outField: Array<String>, idLayer: String, titleLayer: String, minScale: Int, updateField: Array<String>, groupLayer: String) {
        this.url = url
        this.queryField = queryField
        this.outField = outField
        this.updateField = updateField
        this.titleLayer = titleLayer
        this.minScale = minScale
        this.idLayer = idLayer
        this.groupLayer = groupLayer
    }

    companion object {
        private var instance: Config? = null


        fun getInstance(): Config {
            if (instance == null) instance = Config()
            return instance!!
        }
    }
}
