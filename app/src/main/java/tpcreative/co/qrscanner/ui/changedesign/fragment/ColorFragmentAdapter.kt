package tpcreative.co.qrscanner.ui.changedesign.fragment

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import co.tpcreative.supersafe.common.adapter.BaseAdapter
import co.tpcreative.supersafe.common.adapter.BaseHolder
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.extension.debounceClick
import tpcreative.co.qrscanner.common.extension.findImageName
import tpcreative.co.qrscanner.common.extension.onClicked
import tpcreative.co.qrscanner.common.view.MyDrawableCompat
import tpcreative.co.qrscanner.model.*


class ColorFragmentAdapter (inflater: LayoutInflater, private val activity: AppCompatActivity,val uuId : String, private val itemSelectedListener: ItemSelectedListener?) : BaseAdapter<ColorModel, BaseHolder<ColorModel>>(inflater) {
    private val TAG = LogoFragmentAdapter::class.java.simpleName
    override fun getItemCount(): Int {
        return mDataSource.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<ColorModel> {
        return ItemHolderStandard(inflater!!.inflate(R.layout.color_item, parent, false))
    }

    interface ItemSelectedListener {
        fun onClickItem(position: Int)
    }

    private inner class ItemHolderStandard(itemView: View) : BaseHolder<ColorModel>(itemView) {
        private val imgDisplay: ImageView = itemView.findViewById(R.id.imgIcon)
        private val rlRoot : RelativeLayout = itemView.findViewById(R.id.rlRoot)
        private val viewSelected : View = itemView.findViewById(R.id.viewSelected)

        @OptIn(FlowPreview::class)
        override fun bind(data: ColorModel, position: Int) {
            super.bind(data, position)
            imgDisplay.setImageDrawable(ContextCompat.getDrawable(activity,data.icon))
            //imgDisplay.setColorFilter(data.tint,PorterDuff.Mode.SRC_ATOP)
            //viewSelected.visibility = if(data.isSelected) View.VISIBLE else View.INVISIBLE
            MyDrawableCompat.setColorFilter(imgDisplay.drawable,ContextCompat.getColor(activity,
                data.tint
            ))

            rlRoot.debounceClick {
                itemSelectedListener?.onClickItem(position)
            }
//            rlRoot
//                .onClicked()
//                .debounce(250)
//                .onEach {
//                    itemSelectedListener?.onClickItem(position)
//                }
//                .launchIn(activity.lifecycleScope)
//            rlRoot.setOnClickListener {
//                itemSelectedListener?.onClickItem(position)
//                //data.isSelected = !data.isSelected
//            }
        }
    }
}