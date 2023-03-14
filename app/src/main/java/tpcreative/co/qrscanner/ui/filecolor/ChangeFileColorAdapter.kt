package tpcreative.co.qrscanner.ui.filecolor
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import co.tpcreative.supersafe.common.adapter.BaseAdapter
import co.tpcreative.supersafe.common.adapter.BaseHolder
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.view.CircleImageView
import tpcreative.co.qrscanner.model.Theme

class ChangeFileColorAdapter(inflater: LayoutInflater, private val context: Context, private val itemSelectedListener: ItemSelectedListener?) : BaseAdapter<Theme, BaseHolder<Theme>>(inflater) {
    private val TAG = ChangeFileColorAdapter::class.java.simpleName
    override fun getItemCount(): Int {
        return mDataSource.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<Theme> {

        return ItemHolder(inflater!!.inflate(R.layout.theme_item, parent, false))
    }

    interface ItemSelectedListener {
        fun onClickItem(position: Int)
    }

    inner class ItemHolder(itemView: View) : BaseHolder<Theme>(itemView) {
        val imgTheme: CircleImageView = itemView.findViewById(R.id.imgTheme)
        val imgChecked: ImageView =  itemView.findViewById(R.id.imgChecked)
        val rlHome : RelativeLayout = itemView.findViewById(R.id.rlHome)
        var mPosition = 0
        override fun bind(data: Theme, position: Int) {
            super.bind(data, position)
            mPosition = position
            imgTheme.setImageResource(data.getPrimaryDarkColor())
            if (data.isCheck) {
                imgChecked.visibility = View.VISIBLE
            } else {
                imgChecked.visibility = View.INVISIBLE
            }
            rlHome.setOnClickListener {
                itemSelectedListener?.onClickItem(mPosition)
            }
        }
    }
}