/*
 * Copyright (c) 2016-present. Drakeet Xu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tpcreative.co.qrscanner.ui.changedesign

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import co.tpcreative.supersafe.common.adapter.BaseHolder
import com.drakeet.multitype.ItemViewBinder
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.view.MyDrawableCompat
import tpcreative.co.qrscanner.model.EnumChangeDesignType
import tpcreative.co.qrscanner.model.LogoModel

class LogoSquareViewBinder(val selectedSet: MutableSet<LogoModel>,val context : Context,private val itemSelectedListener: ItemSelectedListener?) : ItemViewBinder<LogoModel, LogoSquareViewBinder.ViewHolder>() {

  override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
    return ViewHolder(inflater.inflate(R.layout.logo_square_item, parent, false))
  }

  override fun onBindViewHolder(holder: ViewHolder, item: LogoModel) {
    if (item.enumChangeDesignType == EnumChangeDesignType.NORMAL){
      holder.square = item
      holder.squareView.setImageDrawable(ContextCompat.getDrawable(context,item.icon))
      MyDrawableCompat.setColorFilter(holder.squareView.drawable, ContextCompat.getColor(context,
        item.tint
      ))
      holder.selectedView.visibility = if(item.isSelected) View.VISIBLE else View.INVISIBLE
      holder.layoutVip.visibility = View.GONE
      holder.layoutNormal.visibility = View.VISIBLE
    }else{
      holder.square = item
      holder.squareVipView.setImageDrawable(ContextCompat.getDrawable(context,item.icon))
      MyDrawableCompat.setColorFilter(holder.squareVipView.drawable,ContextCompat.getColor(context,
        item.tint
      ))
      holder.selectedViewVip.visibility = if(item.isSelected) View.VISIBLE else View.INVISIBLE
      holder.imgCircleCodeStatus.setImageResource(R.color.black)
      holder.layoutVip.visibility = View.VISIBLE
      holder.layoutNormal.visibility = View.GONE
    }
  }

  inner class ViewHolder(itemView: View) : BaseHolder<LogoModel>(itemView) {
    val squareView: ImageView = itemView.findViewById(R.id.imgIcon)
    val squareVipView: ImageView = itemView.findViewById(R.id.imgIconVip)
    val selectedView: View = itemView.findViewById(R.id.viewSelected)
    val selectedViewVip: View = itemView.findViewById(R.id.viewSelectedVip)
    val imgCircleCodeStatus: ImageView = itemView.findViewById(R.id.imgCircleCodeStatus)
    val layoutNormal : View = itemView.findViewById(R.id.layoutLogoItem)
    val layoutVip : View = itemView.findViewById(R.id.layoutLogoVipItem)
    lateinit var square: LogoModel
    init {
      itemView.setOnClickListener {
        if (bindingAdapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
        square.apply {
          selectedSet.clear()
          selectedSet.add(square)
        }
        itemSelectedListener?.onClickItem(bindingAdapterPosition)
      }
    }
  }

  interface ItemSelectedListener {
    fun onClickItem(position : Int)
  }

}
