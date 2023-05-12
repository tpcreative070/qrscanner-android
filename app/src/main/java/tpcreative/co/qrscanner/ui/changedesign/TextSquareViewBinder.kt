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
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.drakeet.multitype.ItemViewBinder
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Constant
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.extension.icon
import tpcreative.co.qrscanner.common.extension.toColorIntThrowDefaultColor
import tpcreative.co.qrscanner.common.view.MyDrawableCompat
import tpcreative.co.qrscanner.model.ColorModel
import tpcreative.co.qrscanner.model.EnumChangeDesignType
import tpcreative.co.qrscanner.model.TextModel

/**
 * @author Drakeet Xu
 */
class TextSquareViewBinder(val selectedSet: MutableSet<TextModel>, val context : Context, private val itemSelectedListener: ItemSelectedListener?) : ItemViewBinder<TextModel, TextSquareViewBinder.ViewHolder>() {

  override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
    return ViewHolder(inflater.inflate(R.layout.color_square_item, parent, false))
  }

  override fun onBindViewHolder(holder: ViewHolder, item: TextModel) {
    holder.square = item
    if (item.enumChangeDesignType == EnumChangeDesignType.NORMAL) {
      holder.squareView.setImageDrawable(ContextCompat.getDrawable(context, item.enumIcon.icon))
//      val mHex = item.tintColorHex?.toColorIntThrowDefaultColor() ?: Constant.defaultColor
//      Utils.Log("TAG", "Color result hex $mHex")
//      MyDrawableCompat.setColorFilter(holder.squareView.drawable, mHex)
      holder.layoutVip.visibility = View.GONE
      holder.layoutNormal.visibility = View.VISIBLE
    }else{
      holder.squareVipView.setImageDrawable(ContextCompat.getDrawable(context, item.enumIcon.icon))
//      val mHex = item.tintColorHex?.toColorIntThrowDefaultColor() ?: Constant.defaultColor
//      Utils.Log("TAG", "Color result hex $mHex")
//      MyDrawableCompat.setColorFilter(holder.squareVipView.drawable, mHex)
      holder.imgCircleCodeStatus.setImageResource(R.color.black)
      holder.layoutVip.visibility = View.VISIBLE
      holder.layoutNormal.visibility = View.GONE
    }
  }

  inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val squareView: ImageView = itemView.findViewById(R.id.imgIcon)
    val squareVipView: ImageView = itemView.findViewById(R.id.imgIconVip)
    val imgCircleCodeStatus: ImageView = itemView.findViewById(R.id.imgCircleCodeStatus)
    val layoutNormal : View = itemView.findViewById(R.id.layoutColorItem)
    val layoutVip : View = itemView.findViewById(R.id.layoutColorVipItem)
    lateinit var square: TextModel

    init {
      itemView.setOnClickListener {
        selectedSet.clear()
        selectedSet.add(square)
        itemSelectedListener?.onClickItem(bindingAdapterPosition)
      }
    }
  }

  interface ItemSelectedListener {
    fun onClickItem(position : Int)
  }
}
