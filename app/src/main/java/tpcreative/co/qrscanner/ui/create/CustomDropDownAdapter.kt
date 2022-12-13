package tpcreative.co.qrscanner.ui.create

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.extension.tintColor
import tpcreative.co.qrscanner.model.FormatTypeModel

class CustomDropDownAdapter(val context: Context, var dataSource: MutableList<FormatTypeModel>?) : BaseAdapter() {
    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val view: View
        val vh: ItemHolder
        if (convertView == null) {
            view = inflater.inflate(R.layout.custom_spinner_item, parent, false)
            vh = ItemHolder(view)
            view?.tag = vh
        } else {
            view = convertView
            vh = view.tag as ItemHolder
        }
        vh.label.text = dataSource?.get(position)?.name
        dataSource?.get(position)?.res?.let{
            vh.img.setImageDrawable(it)
            vh.img.tintColor()
        }
        return view
    }

    override fun getItem(position: Int): FormatTypeModel? {
        return dataSource?.get(position)
    }

    override fun getCount(): Int {
        return dataSource?.size ?:0
    }

    override fun getItemId(position: Int): Long {
        return position.toLong();
    }

    private class ItemHolder(row: View?) {
        val label: TextView
        val img: ImageView
        init {
            label = row?.findViewById(R.id.text) as TextView
            img = row.findViewById(R.id.imgIcon) as ImageView
        }
    }
}