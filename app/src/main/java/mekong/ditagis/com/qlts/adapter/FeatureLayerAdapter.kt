package mekong.ditagis.com.qlts.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.esri.arcgisruntime.layers.FeatureLayer
import kotlinx.android.synthetic.main.item_search_type.view.*
import mekong.ditagis.com.qlts.R

/**
 * Created by ThanLe on 04/10/2017.
 */
class FeatureLayerAdapter(private val mContext: Context, private val items: MutableList<Item>) : ArrayAdapter<FeatureLayerAdapter.Item>(mContext, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: DHolder
        if (convertView == null) {
            val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            holder = DHolder(inflater.inflate(R.layout.item_search_type, null))
            holder.view.tag = holder
        }
        else{
            holder = convertView.tag as DHolder
        }
        val item = items[position]
        holder.view.txtTitle.text = item.titleLayer
        return holder.view
    }

    fun getItems(): List<Item> {
        return items
    }

    override fun clear() {
        items.clear()
    }

    override fun getCount(): Int {
        return items.size
    }

    override fun getItemId(position: Int): Long {
        return 0
    }


    class Item(
            var titleLayer: String? = null,
            var idLayer: String? = null,
            var featureLayer: FeatureLayer? = null
//        constructor()
//
//        constructor(typeSearch: String, titleLayer: String, idLayer: String) {
//            this.typeSearch = typeSearch
//            this.titleLayer = titleLayer
//            this.idLayer = idLayer
//        }
    ){}
    private class DHolder( var view: View) {
    }
}
