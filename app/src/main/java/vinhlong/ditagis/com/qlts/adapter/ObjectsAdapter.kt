package vinhlong.ditagis.com.qlts.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.item_tracuu.view.*
import vinhlong.ditagis.com.qlts.R

class ObjectsAdapter(private var mContext: Context, items: MutableList<ObjectsAdapter.Item>)
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
        var convertView = convertView
        if (convertView == null) {
            val inflater = mContext!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.item_tracuu, null)
        }
        val item = items!![position]
        val txt_tracuu_id = convertView!!.txt_tracuu_id
        val txt_tracuu_ngaycapnhat = convertView.txt_tracuu_ngaycapnhat
        val txt_tracuu_diachi = convertView.txt_tracuu_diachi
        txt_tracuu_id.text = item.idSuCo
        txt_tracuu_ngaycapnhat.text = item.ngayXayRa
        txt_tracuu_diachi.text = item.diaChi
        return convertView
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

}
