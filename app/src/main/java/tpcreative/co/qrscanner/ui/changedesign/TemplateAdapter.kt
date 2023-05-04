package tpcreative.co.qrscanner.ui.changedesign

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
import tpcreative.co.qrscanner.model.ChangeDesignCategoryModel

class TemplateAdapter (inflater: LayoutInflater, private val context: Context, private val itemSelectedListener: ItemSelectedListener?) : BaseAdapter<ChangeDesignCategoryModel, BaseHolder<ChangeDesignCategoryModel>>(inflater) {
    private val TAG = TemplateAdapter::class.java.simpleName
    override fun getItemCount(): Int {
        return mDataSource.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<ChangeDesignCategoryModel> {
        return  ItemHolderStandard(inflater!!.inflate(R.layout.change_design_item, parent, false))
    }

    interface ItemSelectedListener {
        fun onClickItem(position: Int)
    }

    private inner class ItemHolderStandard(itemView: View) : BaseHolder<ChangeDesignCategoryModel>(itemView) {
        private val tvTitle : AppCompatTextView = itemView.findViewById(R.id.tvTitle)
        private val imgDisplay: ImageView = itemView.findViewById(R.id.imgIcon)
        private val rlRoot : RelativeLayout = itemView.findViewById(R.id.rlRoot)

        override fun bind(data: ChangeDesignCategoryModel, position: Int) {
            super.bind(data, position)
            tvTitle.text = data.title
            imgDisplay.setImageDrawable(ContextCompat.getDrawable(context,data.icon))
            rlRoot.setOnClickListener {
                itemSelectedListener?.onClickItem(position)
            }
        }
    }
}