package tpcreative.co.qrscanner.ui.filecolor

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import butterknife.BindView
import butterknife.OnClick
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.adapter.BaseAdapter
import tpcreative.co.qrscanner.common.adapter.BaseHolder
import tpcreative.co.qrscanner.common.view.CircleImageView
import tpcreative.co.qrscanner.model.Theme

class ChangeFileColorAdapter(inflater: LayoutInflater?, private val context: Context?, private val itemSelectedListener: ItemSelectedListener?) : BaseAdapter<Theme?, BaseHolder<*>?>(inflater) {
    private val TAG = ChangeFileColorAdapter::class.java.simpleName
    override fun getItemCount(): Int {
        return dataSource.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): BaseHolder<*>? {
        return ItemHolder(inflater.inflate(R.layout.theme_item, parent, false))
    }

    interface ItemSelectedListener {
        open fun onClickItem(position: Int)
    }

    inner class ItemHolder(itemView: View?) : BaseHolder<Theme?>(itemView) {
        @BindView(R.id.imgTheme)
        var imgTheme: CircleImageView? = null

        @BindView(R.id.imgChecked)
        var imgChecked: ImageView? = null
        var mPosition = 0
        override fun bind(data: Theme?, position: Int) {
            super.bind(data, position)
            mPosition = position
            //imgTheme.setBackgroundColor(data.getPrimaryColor());
            imgTheme.setImageResource(data.getPrimaryDarkColor())
            if (data.isCheck) {
                imgChecked.setVisibility(View.VISIBLE)
            } else {
                imgChecked.setVisibility(View.INVISIBLE)
            }
        }

        @OnClick(R.id.rlHome)
        fun onClicked(view: View?) {
            itemSelectedListener?.onClickItem(mPosition)
        }
    }
}