package mekong.ditagis.com.qlts.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import mekong.ditagis.com.qlts.databinding.ItemViewinfoBinding

/**
 * Created by ThanLe on 04/10/2017.
 */
class ThongKeAdapter(private val mContext: Context, private val items: MutableList<Item>) : ArrayAdapter<ThongKeAdapter.Item>(mContext, 0, items) {


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
        holder.binding.txtViewinfoAlias.text = item.titleLayer
        holder.binding.txtViewinfoValue.text = item.sumFeatures!!.toString()
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


    class Item(var titleLayer: String?, var sumFeatures: Long?)
    private class DHolder( var binding: ItemViewinfoBinding) {
        var view: View = binding.root
    }
}
