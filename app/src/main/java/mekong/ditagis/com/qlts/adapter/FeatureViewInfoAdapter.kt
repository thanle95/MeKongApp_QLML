package mekong.ditagis.com.qlts.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.esri.arcgisruntime.data.Field
import mekong.ditagis.com.qlts.databinding.ItemViewinfoBinding


/**
 * Created by ThanLe on 04/10/2017.
 */

class FeatureViewInfoAdapter(private val mContext: Context, private val items: MutableList<Item>) : ArrayAdapter<FeatureViewInfoAdapter.Item>(mContext, 0, items) {

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

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: DHolder
        if (convertView == null) {
            val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            holder = DHolder(ItemViewinfoBinding.inflate(inflater))
            holder.view.tag = holder
        }
        else{
            holder = convertView.tag as DHolder
        }
        val item = items[position]

        holder.binding.txtViewinfoAlias.text = item.alias
        val txtValue = holder.binding.txtViewinfoValue
        txtValue.text = item.value
        if (item.value == null)
            txtValue.visibility = View.GONE
        else
            txtValue.visibility = View.VISIBLE
        return holder.view
    }


    class Item {
        var alias: String? = null
        var value: String? = null
        var fieldName: String? = null
        var isEdit: Boolean = false
        var fieldType: Field.Type? = null
    }
    private class DHolder( var binding: ItemViewinfoBinding) {
        var view: View = binding.root
    }
}
