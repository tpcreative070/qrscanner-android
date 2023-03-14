package tpcreative.co.qrscanner.ui.tipsscanning

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
        private val tvTitle : AppCompatTextView = itemView.findViewById(R.id.tvBarTitle)
        private val imgCode: ImageView = itemView.findViewById(R.id.imgBarCode)
        private val imgCodeStatus : ImageView = itemView.findViewById(R.id.imgBarCodeStatus)
        private val imgShadow : ImageView = itemView.findViewById(R.id.imgBarcodeShadow)
        private val imgLedWhenDark : ImageView = itemView.findViewById(R.id.imgBarcodeLedWhenDark)
        private val imgLed: ImageView = itemView.findViewById(R.id.imgBarcodeLed)
        private val imgLowContrast : ImageView = itemView.findViewById(R.id.imgBarcodoLowContrast)
        private val imgCircleCodeStatus: ImageView = itemView.findViewById(R.id.imgCircleBarCodeStatus)
        private val rlBarcode: RelativeLayout = itemView.findViewById(R.id.rlBarcode)
        override fun bind(data: TipsScanningModel, position: Int) {
            super.bind(data, position)
            tvTitle.text = Utils.onFormatBarcodeDisplay(data.enumAction)
            imgCodeStatus.setImageDrawable(ContextCompat.getDrawable(context,data.iconStatus))
            imgCircleCodeStatus.setImageResource(data.tintColor)
            imgCode.setImageDrawable(ContextCompat.getDrawable(context,data.icon))
            when(data.enumAction){
                EnumAction.SHADOW  ->{
                    imgShadow.visibility = View.VISIBLE
                }
                EnumAction.TOO_CLOSE_BLURRY ->{
                   imgCode.scaleType = ImageView.ScaleType.CENTER_CROP
                }
                EnumAction.LED_WHEN_DARK ->{
                    imgLedWhenDark.visibility = View.VISIBLE
                    imgLed.visibility = View.VISIBLE
                }
                EnumAction.LOW_CONTRAST ->{
                    imgLowContrast.visibility = View.VISIBLE
                }
                else -> {}
            }
            rlBarcode.setOnClickListener {
                itemSelectedListener?.onClickItem(position)
            }
        }
    }

    inner class ItemHolderPortrait(itemView: View) : BaseHolder<TipsScanningModel>(itemView) {
        private val tvTitle : AppCompatTextView = itemView.findViewById(R.id.tvPortraitTitle)
        private val imgCode: ImageView = itemView.findViewById(R.id.imgPortraitCode)
        private val imgCodeStatus : ImageView = itemView.findViewById(R.id.imgPortraitCodeStatus)
        private val imgCircleCodeStatus: ImageView = itemView.findViewById(R.id.imgCirclePortraitCodeStatus)
        private val rlPortraitCode: RelativeLayout = itemView.findViewById(R.id.rlPortraitCode)
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
            rlPortraitCode.setOnClickListener {
                itemSelectedListener?.onClickItem(position)
            }
        }
    }

}