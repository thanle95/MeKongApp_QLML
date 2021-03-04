package mekong.ditagis.com.qlts.entities.entitiesDB


class ListObjectDB private constructor() {
    var dmas: List<String>? = null
    var lstFeatureLayerDTG: List<LayerInfoDTG>? = null

    companion object {

        private var instance: ListObjectDB? = null

        fun getInstance(): ListObjectDB {
            if (instance == null)
                instance = ListObjectDB()
            return instance!!
        }
    }
}
