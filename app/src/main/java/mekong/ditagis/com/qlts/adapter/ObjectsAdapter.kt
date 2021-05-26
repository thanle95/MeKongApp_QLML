package mekong.ditagis.com.qlts.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.item_tracuu.view.*
import mekong.ditagis.com.qlts.R

class ObjectsAdapter(private var mContext: Context, items: MutableList<Item>)
    : ArrayAdapter<ObjectsAdapter.Item>(mContext, 0, items) {
    private var items: MutableList<Item>? = null


    init {
        this.items = items
    }

    fun setContext(context: Context) {
        this.mContext = context
    }

    fun getItems(): List<Item>? {
        return items
    }

    fun setItems(items: MutableList<Item>) {
        this.items = items
    }

    override fun clear() {
        items!!.clear()
    }

    override fun getCount(): Int {
        return items!!.size
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    @SuppressLint("ResourceAsColor")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: DHolder
        if (convertView == null) {
            val inflater = mContext!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            holder = DHolder(inflater.inflate(R.layout.item_tracuu, null))
            holder.view.tag = holder
        }
        else{
            holder = convertView.tag as DHolder
        }
        val item = items!![position]
        holder.layout.txtTracuuId.text = item.idSuCo
        holder.layout.txtTracuuNgaycapnhat.text = item.ngayXayRa
        holder.layout.txtTracuuDiachi.text = item.diaChi
        return holder.view
    }

    class Item {
        var objectID: String? = null
        var idSuCo: String? = null
        var ngayXayRa: String? = null
        var diaChi: String? = null
        var latitude: Double = 0.toDouble()
        var longtitude: Double = 0.toDouble()

        constructor() {}

        constructor(objectID: String, idSuCo: String, ngayXayRa: String) {
            this.objectID = objectID
            this.idSuCo = idSuCo
            this.ngayXayRa = ngayXayRa
        }
    }
    private class DHolder( var layout: View) {
        var view: View = layout
    }
}
