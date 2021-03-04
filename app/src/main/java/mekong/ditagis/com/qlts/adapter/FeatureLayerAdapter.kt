package mekong.ditagis.com.qlts.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import mekong.ditagis.com.qlts.R
import mekong.ditagis.com.qlts.databinding.ItemSearchTypeBinding
import mekong.ditagis.com.qlts.databinding.ItemViewmoreinfoBinding

/**
 * Created by ThanLe on 04/10/2017.
 */
class FeatureLayerAdapter(private val mContext: Context, private val items: MutableList<Item>) : ArrayAdapter<FeatureLayerAdapter.Item>(mContext, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: DHolder
        if (convertView == null) {
            val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            holder = DHolder(ItemSearchTypeBinding.inflate(inflater))
            holder.view.tag = holder
        }
        else{
            holder = convertView.tag as DHolder
        }
        val item = items[position]
        holder.binding.txtTitleLayer.text = item.titleLayer
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
    private class DHolder( var binding: ItemSearchTypeBinding) {
        var view: View = binding.root
    }
}
