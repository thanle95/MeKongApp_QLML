package vinhlong.ditagis.com.qlts.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.item_search_type.view.*
import vinhlong.ditagis.com.qlts.R

/**
 * Created by ThanLe on 04/10/2017.
 */
class FeatureLayerAdapter(private val mContext: Context, private val items: MutableList<Item>) : ArrayAdapter<FeatureLayerAdapter.Item>(mContext, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.item_search_type, null)
        }
        val item = items[position]
        val txt_title_layer = convertView!!.txt_title_layer
        //todo
        txt_title_layer.text = item.titleLayer
        return convertView
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


    class Item {
        var typeSearch: String? = null
        var titleLayer: String? = null
        var idLayer: String? = null

        constructor()

        constructor(typeSearch: String, titleLayer: String, idLayer: String) {
            this.typeSearch = typeSearch
            this.titleLayer = titleLayer
            this.idLayer = idLayer
        }
    }
}
