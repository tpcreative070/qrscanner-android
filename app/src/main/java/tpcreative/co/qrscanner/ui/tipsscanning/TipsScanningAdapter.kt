package tpcreative.co.qrscanner.ui.tipsscanning

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import co.tpcreative.supersafe.common.adapter.BaseAdapter
import co.tpcreative.supersafe.common.adapter.BaseHolder
import kotlinx.android.synthetic.main.tips_scanning_items.view.*
import kotlinx.android.synthetic.main.tips_scanning_items_portrait.view.*
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.extension.onFormatBarcodeDisplay
import tpcreative.co.qrscanner.model.EnumAction
import tpcreative.co.qrscanner.model.TipsScanningModel
const val VIEW_TYPE_PORTRAIT = 1
const val VIEW_TYPE_LANDSCAPE = 2
class TipsScanningAdapter (inflater: LayoutInflater, private val context: Context, private val itemSelectedListener: ItemSelectedListener?) : BaseAdapter<TipsScanningModel, BaseHolder<TipsScanningModel>>(inflater) {
    private val TAG = TipsScanningAdapter::class.java.simpleName
    override fun getItemCount(): Int {
        return mDataSource.size
    }

    override fun getItemViewType(position: Int): Int {
        if (mDataSource[position].enumAction == EnumAction.DEGREE_90 || mDataSource[position].enumAction == EnumAction.DEGREE_270 || mDataSource[position].enumAction == EnumAction.OTHER_ORIENTATION)  {
            return VIEW_TYPE_PORTRAIT
        }
        return VIEW_TYPE_LANDSCAPE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<TipsScanningModel> {
        if (viewType == VIEW_TYPE_PORTRAIT){
            return ItemHolderPortrait(inflater!!.inflate(R.layout.tips_scanning_items_portrait, parent, false))
        }else{
            return ItemHolder(inflater!!.inflate(R.layout.tips_scanning_items, parent, false))
        }
    }

    interface ItemSelectedListener {
        fun onClickItem(position: Int)
    }

    inner class ItemHolder(itemView: View) : BaseHolder<TipsScanningModel>(itemView) {
        private val tvTitle : AppCompatTextView = itemView.tvBarTitle
        private val imgCode: ImageView = itemView.imgBarCode
        private val imgCodeStatus : ImageView = itemView.imgBarCodeStatus
        private val imgCircleCodeStatus: ImageView = itemView.imgCircleBarCodeStatus
        override fun bind(data: TipsScanningModel, position: Int) {
            super.bind(data, position)
            tvTitle.text = Utils.onFormatBarcodeDisplay(data.enumAction)
            imgCodeStatus.setImageDrawable(ContextCompat.getDrawable(context,data.iconStatus))
            imgCircleCodeStatus.setImageResource(data.tintColor)
            imgCode.setImageDrawable(ContextCompat.getDrawable(context,data.icon))
            itemView.rlBarcode.setOnClickListener {
                itemSelectedListener?.onClickItem(position)
            }
        }
    }

    inner class ItemHolderPortrait(itemView: View) : BaseHolder<TipsScanningModel>(itemView) {
        private val tvTitle : AppCompatTextView = itemView.tvPortraitTitle
        private val imgCode: ImageView = itemView.imgPortraitCode
        private val imgCodeStatus : ImageView = itemView.imgPortraitCodeStatus
        private val imgCircleCodeStatus: ImageView = itemView.imgCirclePortraitCodeStatus
        override fun bind(data: TipsScanningModel, position: Int) {
            super.bind(data, position)
            tvTitle.text = Utils.onFormatBarcodeDisplay(data.enumAction)
            imgCodeStatus.setImageDrawable(ContextCompat.getDrawable(context,data.iconStatus))
            imgCircleCodeStatus.setImageResource(data.tintColor)
            imgCode.setImageDrawable(ContextCompat.getDrawable(context,data.icon))
            when(data.enumAction){
                EnumAction.OTHER_ORIENTATION  ->{
                    imgCode.rotation = 45F
                }
                else -> {}
            }
            itemView.rlPortraitCode.setOnClickListener {
                itemSelectedListener?.onClickItem(position)
            }
        }
    }

}