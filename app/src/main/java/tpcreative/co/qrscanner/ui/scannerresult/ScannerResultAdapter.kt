package tpcreative.co.qrscanner.ui.scannerresult
import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import co.tpcreative.supersafe.common.adapter.BaseAdapter
import co.tpcreative.supersafe.common.adapter.BaseHolder
import kotlinx.android.synthetic.main.item_navigation.view.*
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.model.ItemNavigation

class ScannerResultAdapter(inflater: LayoutInflater, private val context: Context, private val itemSelectedListener: ItemSelectedListener?)  : BaseAdapter<ItemNavigation, BaseHolder<ItemNavigation>>(inflater){
    private val TAG = ScannerResultAdapter::class.java.simpleName
    override fun getItemCount(): Int {
        return mDataSource.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<ItemNavigation> {
        return ItemHolder(inflater!!.inflate(R.layout.item_navigation, parent, false))
    }

    interface ItemSelectedListener {
        fun onClickItem(position: Int)
    }

    inner class ItemHolder(itemView: View) : BaseHolder<ItemNavigation>(itemView) {
        val imgAction: AppCompatImageView = itemView.imgAction
        val tvTitle: AppCompatTextView = itemView.tvTitle
        private var mPosition = 0
        override fun bind(data: ItemNavigation, position: Int) {
            super.bind(data, position)
            this.mPosition = position
            tvTitle.text = data.value
            imgAction.setImageDrawable(ContextCompat.getDrawable(context, data.res))
            imgAction.setColorFilter(ContextCompat.getColor(context, R.color.colorAccent), PorterDuff.Mode.SRC_ATOP)
            itemView.rlHome.setOnClickListener {
                itemSelectedListener?.onClickItem(mPosition)
            }
        }
    }
}