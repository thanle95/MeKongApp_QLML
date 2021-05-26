package mekong.ditagis.com.qlts.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.item_add_attachment.view.*
import mekong.ditagis.com.qlts.R
import mekong.ditagis.com.qlts.entities.DAttachment


class AttachmentAdapter(private val mContext: Context, private var items: MutableList<DAttachment>) : ArrayAdapter<DAttachment>(mContext, 0, items) {

    fun getItems(): MutableList<DAttachment> {
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

    @SuppressLint("ResourceAsColor")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: DHolder
        if (convertView == null) {
            val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            holder = DHolder(inflater.inflate(R.layout.item_add_attachment, null))
            holder.view.tag = holder
        }
        else{
            holder = convertView.tag as DHolder
        }
            val item = items[position]
            if (item.image != null) {
                holder.view.imgAddAttachment.visibility = View.INVISIBLE
                val background = BitmapDrawable(mContext.resources, item.image)
                holder.view.layoutAddAttachment.background = background
            } else {
                holder.view.imgAddAttachment.visibility = View.VISIBLE
                holder.view.layoutAddAttachment.background = mContext.getDrawable(R.drawable.layout_border_dashed)
            }
            holder.view.txtAddAttachmentTitle.text = item.title
            return holder.view

    }

    private class DHolder( var view: View) {
    }
}

