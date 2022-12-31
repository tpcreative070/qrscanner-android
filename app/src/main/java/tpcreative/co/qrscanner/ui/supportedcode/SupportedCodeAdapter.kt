package tpcreative.co.qrscanner.ui.supportedcode

import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import co.tpcreative.supersafe.common.adapter.BaseAdapter
import co.tpcreative.supersafe.common.adapter.BaseHolder
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.android.synthetic.main.supported_bar_code_items.view.*
import kotlinx.android.synthetic.main.supported_qr_code_items.view.*
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Constant
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.extension.onFormatBarcodeDisplay
import tpcreative.co.qrscanner.model.EnumAction
import tpcreative.co.qrscanner.model.SupportedCodeModel
import tpcreative.co.qrscanner.model.Theme
import java.util.*

const val VIEW_TYPE_QR_CODE = 1
const val VIEW_TYPE_BAR_CODE = 2
class SupportedCodeAdapter (inflater: LayoutInflater, private val context: Context, private val itemSelectedListener: ItemSelectedListener?) : BaseAdapter<SupportedCodeModel, BaseHolder<SupportedCodeModel>>(inflater) {
    private val TAG = SupportedCodeAdapter::class.java.simpleName
    override fun getItemCount(): Int {
        return mDataSource.size
    }

    override fun getItemViewType(position: Int): Int {
        if (mDataSource[position].barcodeFormat == BarcodeFormat.QR_CODE || mDataSource[position].barcodeFormat == BarcodeFormat.AZTEC || mDataSource[position].barcodeFormat == BarcodeFormat.DATA_MATRIX) {
            return VIEW_TYPE_QR_CODE
        }
        return VIEW_TYPE_BAR_CODE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<SupportedCodeModel> {
        return when(viewType){
            VIEW_TYPE_QR_CODE -> {
                ItemHolderQRCode(inflater!!.inflate(R.layout.supported_qr_code_items, parent, false))
            }else ->{
                ItemHolderBarCodes(inflater!!.inflate(R.layout.supported_bar_code_items, parent, false))
            }
        }
    }

    interface ItemSelectedListener {
        fun onClickItem(position: Int)
    }

    inner class ItemHolderQRCode(itemView: View) : BaseHolder<SupportedCodeModel>(itemView) {
        private val tvTitle : AppCompatTextView = itemView.tvTitle
        private val imgCode: ImageView = itemView.imgCode
        private val imgCodeStatus : ImageView = itemView.imgCodeStatus
        private val imgCircleCodeStatus: ImageView = itemView.imgCircleCodeStatus

        override fun bind(data: SupportedCodeModel, position: Int) {
            super.bind(data, position)
            tvTitle.text = Utils.onFormatBarcodeDisplay(data.barcodeFormat,data.enumAction)
            imgCodeStatus.setImageDrawable(ContextCompat.getDrawable(context,data.iconStatus))
            imgCircleCodeStatus.setImageResource(data.tintColor)
            imgCode.setImageDrawable(ContextCompat.getDrawable(context,data.icon))
            itemView.rlCode.setOnClickListener {
                itemSelectedListener?.onClickItem(position)
            }
        }
    }

    inner class ItemHolderBarCodes(itemView: View) : BaseHolder<SupportedCodeModel>(itemView) {
        private val tvTitle : AppCompatTextView = itemView.tvBarTitle
        private val imgCode: ImageView = itemView.imgBarCode
        private val imgCodeStatus : ImageView = itemView.imgBarCodeStatus
        private val imgCircleCodeStatus: ImageView = itemView.imgCircleBarCodeStatus
        override fun bind(data: SupportedCodeModel, position: Int) {
            super.bind(data, position)
            tvTitle.text = Utils.onFormatBarcodeDisplay(data.barcodeFormat, data.enumAction)
            imgCodeStatus.setImageDrawable(ContextCompat.getDrawable(context,data.iconStatus))
            imgCircleCodeStatus.setImageResource(data.tintColor)
            imgCode.setImageDrawable(ContextCompat.getDrawable(context,data.icon))
            itemView.rlBarcode.setOnClickListener {
                itemSelectedListener?.onClickItem(position)
            }
        }
    }
}