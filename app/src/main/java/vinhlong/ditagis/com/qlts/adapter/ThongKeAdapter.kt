package vinhlong.ditagis.com.qlts.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.item_viewinfo.view.*
import vinhlong.ditagis.com.qlts.R

/**
 * Created by ThanLe on 04/10/2017.
 */
class ThongKeAdapter(private val mContext: Context, private val items: MutableList<Item>) : ArrayAdapter<ThongKeAdapter.Item>(mContext, 0, items) {


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.item_viewinfo, null)
        }
        val item = items[position]
        val txt_viewinfo_alias = convertView!!.txt_viewinfo_alias
        val txt_viewinfo_value = convertView.txt_viewinfo_value
        txt_viewinfo_alias.text = item.titleLayer
        txt_viewinfo_value.text = item.sumFeatures!!.toString()
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


    class Item(var titleLayer: String?, var sumFeatures: Long?)
}
