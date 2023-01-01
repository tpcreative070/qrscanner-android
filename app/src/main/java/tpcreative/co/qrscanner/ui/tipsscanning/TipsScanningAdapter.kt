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
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.extension.onFormatBarcodeDisplay
import tpcreative.co.qrscanner.model.EnumAction
import tpcreative.co.qrscanner.model.TipsScanningModel

class TipsScanningAdapter (inflater: LayoutInflater, private val context: Context, private val itemSelectedListener: ItemSelectedListener?) : BaseAdapter<TipsScanningModel, BaseHolder<TipsScanningModel>>(inflater) {
    private val TAG = TipsScanningAdapter::class.java.simpleName
    override fun getItemCount(): Int {
        return mDataSource.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<TipsScanningModel> {
        return ItemHolder(inflater!!.inflate(R.layout.tips_scanning_items, parent, false))
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
            when(data.enumAction){
                EnumAction.DEGREE_0 ->{
                    imgCode.rotation = 0F
                }
                EnumAction.DEGREE_90 ->{
                    imgCode.rotation = 90F
                }
                EnumAction.DEGREE_270 ->{
                    imgCode.rotation = 270F
                }
                else -> {}
            }
            imgCodeStatus.setImageDrawable(ContextCompat.getDrawable(context,data.iconStatus))
            imgCircleCodeStatus.setImageResource(data.tintColor)
            imgCode.setImageDrawable(ContextCompat.getDrawable(context,data.icon))
            itemView.rlBarcode.setOnClickListener {
                itemSelectedListener?.onClickItem(position)
            }
        }
    }
}