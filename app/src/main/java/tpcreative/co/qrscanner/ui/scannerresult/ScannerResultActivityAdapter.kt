package tpcreative.co.qrscanner.ui.scannerresult
import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import co.tpcreative.supersafe.common.adapter.BaseAdapter
import co.tpcreative.supersafe.common.adapter.BaseHolder
import kotlinx.android.synthetic.main.item_navigation.view.*
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Navigator
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.model.EnumAction
import tpcreative.co.qrscanner.model.EnumItem
import tpcreative.co.qrscanner.model.ItemNavigation
import tpcreative.co.qrscanner.ui.review.ReviewActivity

class ScannerResultActivityAdapter(inflater: LayoutInflater, private val context: Context, private val itemSelectedListener: ItemSelectedListener?)  : BaseAdapter<ItemNavigation, BaseHolder<ItemNavigation>>(inflater){
    private val TAG = ScannerResultActivityAdapter::class.java.simpleName
    override fun getItemCount(): Int {
        return mDataSource.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<ItemNavigation> {
        return ItemHolder(inflater!!.inflate(R.layout.item_navigation, parent, false))
    }

    interface ItemSelectedListener {
        fun onClickItem(position: Int,action : EnumAction)
    }

    inner class ItemHolder(itemView: View) : BaseHolder<ItemNavigation>(itemView) {
        private var mPosition = 0
        override fun bind(data: ItemNavigation, position: Int) {
            super.bind(data, position)
            this.mPosition = position
            if (data.enumAction == EnumAction.DO_ADVANCE){
                itemView.rlBasic.visibility = View.GONE
                itemView.rlAdvance.visibility = View.VISIBLE
                if (data.isFavorite == true){
                    itemView.imgMarkFavorite.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_baseline_favorite_24))
                }else{
                    itemView.imgMarkFavorite.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_baseline_unfavorite_24))
                }
            }else{
                itemView.rlAdvance.visibility = View.GONE
                itemView.rlBasic.visibility = View.VISIBLE
                itemView.tvTitle.text = data.value
                itemView.imgAction.setImageDrawable(ContextCompat.getDrawable(context, data.res))
                itemView.imgAction.setColorFilter(ContextCompat.getColor(context, R.color.colorAccent), PorterDuff.Mode.SRC_ATOP)
            }
            itemView.rlHome.setOnClickListener {
                itemSelectedListener?.onClickItem(mPosition,EnumAction.VIEW_CODE)
            }
            itemView.imgTakeNote.setOnClickListener {
                itemSelectedListener?.onClickItem(mPosition,EnumAction.TAKE_NOTE)
            }
            itemView.imgMarkFavorite.setOnClickListener {
                itemSelectedListener?.onClickItem(mPosition,EnumAction.MARK_FAVORITE)
            }
            itemView.rlViewCode.setOnClickListener {
                itemSelectedListener?.onClickItem(mPosition,EnumAction.VIEW_CODE)
            }
        }

    }
}