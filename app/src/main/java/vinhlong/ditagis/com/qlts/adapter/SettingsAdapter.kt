package vinhlong.ditagis.com.qlts.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.item_search.view.*
import vinhlong.ditagis.com.qlts.R

/**
 * Created by ThanLe on 04/10/2017.
 */

class SettingsAdapter(private val mContext: Context, val items: Array<Item>) : ArrayAdapter<SettingsAdapter.Item>(mContext, 0, items) {

    fun setItemSubtitle(position: Int, subTitle: String) {
        items[position].subTitle = subTitle
    }

    override fun getCount(): Int {
        return items.size
    }

    override fun getItemId(position: Int): Long {
        return 0
    }


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.item_search, null)
        }
        val item = items[position]


        val txtTitle = convertView!!.txt_settings_title
        //todo
        txtTitle.text = item.title

        val txtSubTitle = convertView.txt_settings_subtitle
        //todo
        txtSubTitle.text = item.subTitle


        return convertView
    }

    class Item(var title: String?, var subTitle: String?)
}
