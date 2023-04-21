package tpcreative.co.qrscanner.ui.changedesign.fragment

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import co.tpcreative.supersafe.common.adapter.BaseAdapter
import co.tpcreative.supersafe.common.adapter.BaseHolder
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.extension.findImageName
import tpcreative.co.qrscanner.common.view.MyDrawableCompat
import tpcreative.co.qrscanner.model.*

const val NORMAL = 1
const val VIP = 2
class LogoFragmentAdapter (inflater: LayoutInflater, private val context: Context,val uuId : String, private val itemSelectedListener: ItemSelectedListener?) : BaseAdapter<LogoModel, BaseHolder<LogoModel>>(inflater) {
    private val TAG = LogoFragmentAdapter::class.java.simpleName
    override fun getItemCount(): Int {
        return mDataSource.size
    }

    override fun getItemViewType(position: Int): Int {
        if (mDataSource[position].enumChangeDesignType == EnumChangeDesignType.NORMAL)  {
            return NORMAL
        }
        return VIP
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<LogoModel> {
        if (viewType == NORMAL){
            return ItemHolderStandard(inflater!!.inflate(R.layout.logo_item, parent, false))
        }else{
            return ItemHolderStandardVip(inflater!!.inflate(R.layout.layout_vip, parent, false))
        }
    }

    interface ItemSelectedListener {
        fun onClickItem(position: Int)
    }

    private inner class ItemHolderStandard(itemView: View) : BaseHolder<LogoModel>(itemView) {
        private val imgDisplay: ImageView = itemView.findViewById(R.id.imgIcon)
        private val rlRoot : RelativeLayout = itemView.findViewById(R.id.rlRoot)
        private val viewSelected : View = itemView.findViewById(R.id.viewSelected)

        override fun bind(data: LogoModel, position: Int) {
            super.bind(data, position)
            imgDisplay.setImageDrawable(ContextCompat.getDrawable(context,data.icon))
            //imgDisplay.setColorFilter(data.tint,PorterDuff.Mode.SRC_ATOP)
            MyDrawableCompat.setColorFilter(imgDisplay.drawable,ContextCompat.getColor(context,
                data.tint
            ))
            viewSelected.visibility = if(data.isSelected) View.VISIBLE else View.INVISIBLE
            Utils.Log("TAG","Selected ${data.isSelected}")
            rlRoot.setOnClickListener {
                itemSelectedListener?.onClickItem(position)
                //data.isSelected = !data.isSelected
            }
        }
    }

    private inner class ItemHolderStandardVip(itemView: View) : BaseHolder<LogoModel>(itemView) {
        private val imgDisplay: ImageView = itemView.findViewById(R.id.imgIconVip)
        private val rlRoot : RelativeLayout = itemView.findViewById(R.id.rlRootVip)
        private val viewSelected : View = itemView.findViewById(R.id.viewSelectedVip)
        private val imgCircleCodeStatus: ImageView = itemView.findViewById(R.id.imgCircleCodeStatus)
        override fun bind(data: LogoModel, position: Int) {
            super.bind(data, position)
            imgDisplay.setImageDrawable(ContextCompat.getDrawable(context,data.icon))
            //imgDisplay.setColorFilter(data.tint,PorterDuff.Mode.SRC_ATOP)
            MyDrawableCompat.setColorFilter(imgDisplay.drawable,ContextCompat.getColor(context,
                data.tint
            ))
            viewSelected.visibility = if(data.isSelected) View.VISIBLE else View.INVISIBLE
            imgCircleCodeStatus.setImageResource(R.color.black)
            Utils.Log("TAG","Selected ${data.isSelected}")
            rlRoot.setOnClickListener {
                itemSelectedListener?.onClickItem(position)
                //data.isSelected = !data.isSelected
            }
        }
    }
}