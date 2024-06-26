package tpcreative.co.qrscanner.ui.help

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import co.tpcreative.supersafe.common.adapter.BaseAdapter
import co.tpcreative.supersafe.common.adapter.BaseHolder
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.model.EnumAction
import tpcreative.co.qrscanner.model.HelpModel
const val VIEW_TYPE_STANDARD = 1
const val VIEW_TYPE_CUSTOM = 2
class HelpAdapter (inflater: LayoutInflater, private val context: Context, private val itemSelectedListener: ItemSelectedListener?) : BaseAdapter<HelpModel, BaseHolder<HelpModel>>(inflater) {
    private val TAG = HelpAdapter::class.java.simpleName
    override fun getItemCount(): Int {
        return mDataSource.size
    }

    override fun getItemViewType(position: Int): Int {
        if (mDataSource[position].type == EnumAction.GUIDES_VIDEO) {
            return VIEW_TYPE_CUSTOM
        }
        return VIEW_TYPE_STANDARD
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<HelpModel> {
        return when(viewType){
            VIEW_TYPE_STANDARD -> {
                ItemHolderStandard(inflater!!.inflate(R.layout.help_item_standard, parent, false))
            }else ->{
                ItemHolderCustom(inflater!!.inflate(R.layout.help_item_custom, parent, false))
            }
        }
    }

    interface ItemSelectedListener {
        fun onClickItem(position: Int)
    }

    inner class ItemHolderStandard(itemView: View) : BaseHolder<HelpModel>(itemView) {
        private val tvTitle : AppCompatTextView = itemView.findViewById(R.id.tvStandardTitle)
        private val imgDisplay: ImageView = itemView.findViewById(R.id.imgStandardIcon)
        private val imgCircle: ImageView = itemView.findViewById(R.id.imgStandardCircle)
        private val rlStandard : RelativeLayout = itemView.findViewById(R.id.rlStandard)

        override fun bind(data: HelpModel, position: Int) {
            super.bind(data, position)
            tvTitle.text = data.title
            imgDisplay.setImageDrawable(ContextCompat.getDrawable(context,data.icon))
            imgCircle.setImageResource(data.color)
            rlStandard.setOnClickListener {
                itemSelectedListener?.onClickItem(position)
            }
        }
    }

    inner class ItemHolderCustom(itemView: View) : BaseHolder<HelpModel>(itemView) {
        private val tvTitle : AppCompatTextView = itemView.findViewById(R.id.tvTitle)
        private val imgDisplay: ImageView = itemView.findViewById(R.id.imgIcon)
        private val rlCustom : RelativeLayout = itemView.findViewById(R.id.rlCustom)
        override fun bind(data: HelpModel, position: Int) {
            super.bind(data, position)
            tvTitle.text = data.title
            imgDisplay.setImageDrawable(ContextCompat.getDrawable(context,data.icon))
            rlCustom.setOnClickListener {
                itemSelectedListener?.onClickItem(position)
            }
        }
    }
}