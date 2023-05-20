package tpcreative.co.qrscanner.ui.scannerresult
import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import co.tpcreative.supersafe.common.adapter.BaseAdapter
import co.tpcreative.supersafe.common.adapter.BaseHolder
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.extension.isQRCode
import tpcreative.co.qrscanner.common.extension.onBarCodeId
import tpcreative.co.qrscanner.common.extension.onShowGuide
import tpcreative.co.qrscanner.common.extension.toText
import tpcreative.co.qrscanner.model.EnumAction
import tpcreative.co.qrscanner.model.EnumActivity
import tpcreative.co.qrscanner.model.ItemNavigation

class ScannerResultActivityAdapter(inflater: LayoutInflater, private val context: AppCompatActivity, private val itemSelectedListener: ItemSelectedListener?)  : BaseAdapter<ItemNavigation, BaseHolder<ItemNavigation>>(inflater){
    private val TAG = ScannerResultActivityAdapter::class.java.simpleName
    override fun getItemCount(): Int {
        return mDataSource.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<ItemNavigation> {
        return ItemHolder(inflater!!.inflate(R.layout.item_navigation, parent, false))
    }

    interface ItemSelectedListener {
        fun onClickItem(position: Int,contactKey: String,contactValue :String,action : EnumAction)
    }

    inner class ItemHolder(itemView: View) : BaseHolder<ItemNavigation>(itemView) {
        private var mPosition = 0
        override fun bind(data: ItemNavigation, position: Int) {
            super.bind(data, position)
            this.mPosition = position
            val mContactKey = data.contactKey
            val mContactValue = data.contactValue
            val rlBasic : RelativeLayout = itemView.findViewById(R.id.rlBasic)
            val rlAdvance : RelativeLayout = itemView.findViewById(R.id.rlAdvance)
            val imgTypeQRCode : ImageView = itemView.findViewById(R.id.imgTypeQRCode)
            val imgMarkFavorite : ImageView = itemView.findViewById(R.id.imgMarkFavorite)
            val tvTitle : TextView = itemView.findViewById(R.id.tvTitle)
            val imgAction : ImageView = itemView.findViewById(R.id.imgAction)
            val rlHome : RelativeLayout = itemView.findViewById(R.id.rlHome)
            val imgTakeNote : ImageView = itemView.findViewById(R.id.imgTakeNote)
            if (data.enumAction == EnumAction.DO_ADVANCE){
                rlBasic.visibility = View.GONE
                rlAdvance.visibility = View.VISIBLE
                imgTypeQRCode.setImageDrawable(Utils.onBarCodeId(data.barcodeFormat))
                if (data.isFavorite == true){
                    imgMarkFavorite.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_baseline_favorite_24))
                }else{
                    imgMarkFavorite.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_baseline_unfavorite_24))
                }
                if (Utils.isQRCode(data.barcodeFormat)){
                    context.onShowGuide(rlAdvance,R.string.click_to_view_code.toText(),EnumActivity.SCANNER_RESULT_ACTIVITY,R.drawable.ic_qrcode_bg)
                }
            }else{
                rlAdvance.visibility = View.GONE
                rlBasic.visibility = View.VISIBLE
                tvTitle.text = data.value
                imgAction.setImageDrawable(ContextCompat.getDrawable(context, data.res))
                imgAction.setColorFilter(ContextCompat.getColor(context, R.color.colorAccent), PorterDuff.Mode.SRC_ATOP)
            }
            rlHome.setOnClickListener {
                itemSelectedListener?.onClickItem(mPosition,mContactKey,mContactValue,EnumAction.VIEW_CODE)
            }
            imgTakeNote.setOnClickListener {
                itemSelectedListener?.onClickItem(mPosition,mContactKey,mContactValue,EnumAction.TAKE_NOTE)
            }
            imgMarkFavorite.setOnClickListener {
                itemSelectedListener?.onClickItem(mPosition,mContactKey,mContactValue,EnumAction.MARK_FAVORITE)
            }
        }

    }
}