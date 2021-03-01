package vinhlong.ditagis.com.qlts.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.item_tracuu.view.*
import vinhlong.ditagis.com.qlts.R

/**
 * Created by ThanLe on 04/10/2017.
 */

class TraCuuAdapter(private val mContext: Context, private val items: MutableList<Item>) : ArrayAdapter<TraCuuAdapter.Item>(mContext, 0, items) {

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
            convertView = inflater.inflate(R.layout.item_tracuu, null)
        }
        val item = items[position]

        val layout = convertView!!.layout_tracuu
        when (item.trangThai) {
            //chưa sửa chữa
            0 -> layout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.color_chua_sua_chua))
            //đã sửa chữa
            1 -> layout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.color_da_sua_chua))
            //đang sửa chữa
            2 -> layout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.color_dang_sua_chua))
        }

        val txtID = convertView.txt_tracuu_id
        //todo
        txtID.text = item.id

        val txtDiaChi = convertView.txt_tracuu_diachi
        //todo
        txtDiaChi.text = item.diaChi

        val txtNgayCapNhat = convertView.txt_tracuu_ngaycapnhat
        //todo
        txtNgayCapNhat.text = item.ngayCapNhat


        return convertView
    }


    class Item(var objectID: Int, var id: String, var trangThai: Int, var ngayCapNhat: String, var diaChi: String) {
        var latitude: Double = 0.toDouble()
        var longtitude: Double = 0.toDouble()

        override fun toString(): String {
            return "Item{objectID=$objectID, typeSearch='$id', trangThai=$trangThai, ngayCapNhat='$ngayCapNhat', diaChi='$diaChi'}"
        }
    }
}