package tpcreative.co.qrscanner.ui.scannerresult

import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import butterknife.BindView
import butterknife.OnClick
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.model.ItemNavigation

class ScannerResultAdapter(inflater: LayoutInflater?, private val context: Context?, private val itemSelectedListener: ItemSelectedListener?) : BaseAdapter<ItemNavigation?, BaseHolder<*>?>(inflater) {
    private val TAG = ScannerResultAdapter::class.java.simpleName
    override fun getItemCount(): Int {
        return dataSource.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): BaseHolder<*>? {
        return ItemHolder(inflater.inflate(R.layout.item_navigation, parent, false))
    }

    interface ItemSelectedListener {
        open fun onClickItem(position: Int)
    }

    inner class ItemHolder(itemView: View?) : BaseHolder<ItemNavigation?>(itemView) {
        @BindView(R.id.imgAction)
        var imgAction: ImageView? = null

        @BindView(R.id.tvTitle)
        var tvTitle: TextView? = null
        private var mPosition = 0
        override fun bind(data: ItemNavigation?, position: Int) {
            super.bind(data, position)
            this.mPosition = position
            tvTitle.setText(data.value)
            imgAction.setImageDrawable(ContextCompat.getDrawable(context, data.res))
            imgAction.setColorFilter(ContextCompat.getColor(context, R.color.colorAccent), PorterDuff.Mode.SRC_ATOP)
        }

        @OnClick(R.id.rlHome)
        fun onClicked(view: View?) {
            itemSelectedListener?.onClickItem(mPosition)
        }
    }
}