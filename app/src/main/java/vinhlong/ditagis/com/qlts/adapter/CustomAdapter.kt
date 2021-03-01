package vinhlong.ditagis.com.qlts.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import kotlinx.android.synthetic.main.item_tracuu.view.*

import vinhlong.ditagis.com.qlts.R

/**
 * Created by ThanLe on 04/10/2017.
 */

class CustomAdapter(private val mContext: Context, private val items: MutableList<Item>) : ArrayAdapter<CustomAdapter.Item>(mContext, 0, items) {

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


    //    public Item getItem(String mlt) {
    //        for (Item item : this.items)
    //            if (item.getTieuThu().equals(mlt))
    //                return item;
    //        return null;
    //    }
    //
    //    public boolean removeItem(String mlt) {
    //        for (Item item : this.items)
    //            if (item.getTieuThu().equals(mlt)) {
    //                this.items.remove(item);
    //                return true;
    //            }
    //        return false;
    //    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.item_search, null)
        }
        val item = items[position]


        val txtDanhBo = convertView!!.txt_tracuu_id
        //todo
        txtDanhBo.text = item.nam + "_" + item.ky + "_" + item.dot + "_" + item.may




        return convertView
    }

    class Item(ky: String, dot: String, nam: String, soLuong: String, may: String, flag: Int) {
        var ky: String
            internal set
        var dot: String
            internal set
        var nam: String
            internal set
        var soLuong: String
            internal set
        var may: String
            internal set
        var flag: Int = 0
            internal set

        init {
            this.ky = ky
            this.dot = dot
            this.nam = nam
            this.soLuong = soLuong
            this.may = may
            this.flag = flag
        }
    }
}
