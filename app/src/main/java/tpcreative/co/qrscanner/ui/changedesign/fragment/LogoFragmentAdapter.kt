package tpcreative.co.qrscanner.ui.changedesign.fragment

import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import co.tpcreative.supersafe.common.adapter.BaseAdapter
import co.tpcreative.supersafe.common.adapter.BaseHolder
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.view.MyDrawableCompat
import tpcreative.co.qrscanner.model.ChangeDesignModel

class LogoFragmentAdapter (inflater: LayoutInflater, private val context: Context, private val itemSelectedListener: ItemSelectedListener?) : BaseAdapter<ChangeDesignModel, BaseHolder<ChangeDesignModel>>(inflater) {
    private val TAG = LogoFragmentAdapter::class.java.simpleName
    override fun getItemCount(): Int {
        return mDataSource.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<ChangeDesignModel> {
        return  ItemHolderStandard(inflater!!.inflate(R.layout.logo_item, parent, false))
    }

    interface ItemSelectedListener {
        fun onClickItem(position: Int)
    }

    private inner class ItemHolderStandard(itemView: View) : BaseHolder<ChangeDesignModel>(itemView) {
        private val imgDisplay: ImageView = itemView.findViewById(R.id.imgIcon)
        private val rlRoot : RelativeLayout = itemView.findViewById(R.id.rlRoot)
        private val viewSelected : View = itemView.findViewById(R.id.viewSelected)

        override fun bind(data: ChangeDesignModel, position: Int) {
            super.bind(data, position)
            imgDisplay.setImageDrawable(ContextCompat.getDrawable(context,data.icon))
            //imgDisplay.setColorFilter(data.tint,PorterDuff.Mode.SRC_ATOP)
            MyDrawableCompat.setColorFilter(imgDisplay.drawable,ContextCompat.getColor(context, data.tint?: R.color.transparent))
            viewSelected.visibility = if(data.isSelected) View.VISIBLE else View.INVISIBLE
            Utils.Log("TAG","Selected ${data.isSelected}")
            rlRoot.setOnClickListener {
                itemSelectedListener?.onClickItem(position)
                //data.isSelected = !data.isSelected
            }
        }
    }
}