package mekong.ditagis.com.qlts.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import com.esri.arcgisruntime.data.Field
import kotlinx.android.synthetic.main.item_viewinfo.view.*
import mekong.ditagis.com.qlts.R

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
            holder = DHolder(inflater.inflate(R.layout.item_viewinfo, null) as LinearLayout)
            holder.view.tag = holder
        }
        else{
            holder = convertView.tag as DHolder
        }
        val item = items[position]

        holder.view.txtViewinfoAlias.text = item.alias
        val txtValue = holder.view.txtViewinfoValue
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
    private class DHolder( var layout: LinearLayout ) {
        var view: View = layout
    }
}
