package mekong.ditagis.com.qlts.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import mekong.ditagis.com.qlts.R
import mekong.ditagis.com.qlts.databinding.ItemAddAttachmentBinding
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
            holder = DHolder(ItemAddAttachmentBinding.inflate(inflater))
            holder.view.tag = holder
        }
        else{
            holder = convertView.tag as DHolder
        }
            val item = items[position]
            if (item.image != null) {
                holder.binding.imgAddAttachment.visibility = View.INVISIBLE
                val background = BitmapDrawable(mContext.resources, item.image)
                holder.binding.layoutAddAttachment.background = background
            } else {
                holder.binding.imgAddAttachment.visibility = View.VISIBLE
                holder.binding.layoutAddAttachment.background = mContext.getDrawable(R.drawable.layout_border_dashed)
            }
            holder.binding.txtAddAttachmentTitle.text = item.title
            return holder.view

    }

    private class DHolder( var binding: ItemAddAttachmentBinding) {
        var view: View = binding.root
    }
}

