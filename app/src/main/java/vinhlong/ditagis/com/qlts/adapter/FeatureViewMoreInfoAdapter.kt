package vinhlong.ditagis.com.qlts.adapter

import android.content.Context
import androidx.core.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView

import com.esri.arcgisruntime.data.Field
import kotlinx.android.synthetic.main.item_viewmoreinfo.view.*

import vinhlong.ditagis.com.qlts.R

/**
 * Created by ThanLe on 04/10/2017.
 */

class FeatureViewMoreInfoAdapter(private val mContext: Context, private val items: MutableList<Item>) : ArrayAdapter<FeatureViewMoreInfoAdapter.Item>(mContext, 0, items) {

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
        var convertView = convertView
        if (convertView == null) {
            val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.item_viewmoreinfo, null)
        }
        val item = items[position]

        val layout = convertView!!.layout_viewmoreinfo
        val txtAlias = convertView.txt_viewmoreinfo_alias
        //todo
        txtAlias.text = item.alias

        val txtValue = convertView.txt_viewmoreinfo_value
        if (item.fieldName == "ViTri" || item.fieldName == "GhiChu" || item.fieldName == "GhiChuVatTu") {
            txtValue.width = 550
        }
        //todo
        txtValue.text = item.value
        if (item.isEdit) {
            convertView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorAccent_1))
            convertView.img_viewmoreinfo_edit.visibility = View.VISIBLE
        } else {
            convertView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorBackground_1))
            convertView.img_viewmoreinfo_edit.visibility = View.INVISIBLE

        }
        if (item.value == null)
            txtValue.visibility = View.GONE
        else
            txtValue.visibility = View.VISIBLE
        return convertView
    }


    class Item {
        var alias: String? = null
        var value: String? = null
        var fieldName: String? = null
        var isEdit: Boolean = false
        var fieldType: Field.Type? = null

        init {
            isEdit = false
        }
    }
}
