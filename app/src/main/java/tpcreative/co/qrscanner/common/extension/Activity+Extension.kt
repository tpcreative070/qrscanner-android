package tpcreative.co.qrscanner.common.extension

import android.app.Activity
import android.content.res.Configuration
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.elconfidencial.bubbleshowcase.BubbleShowCaseListener
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.view.showcase.BubbleShowCase
import tpcreative.co.qrscanner.common.view.showcase.BubbleShowCaseBuilder
import tpcreative.co.qrscanner.model.EnumActivity
import tpcreative.co.qrscanner.ui.review.initUI

fun Activity.isPortrait() : Boolean{
    try {
        val orientation = this.resources?.configuration?.orientation
        return orientation == Configuration.ORIENTATION_PORTRAIT
    }catch (e : Exception){
        e.printStackTrace()
    }
    return false
}

fun AppCompatActivity.onShowGuide(view : View,message :String,enumActivity: EnumActivity,icon : Int, position : BubbleShowCase.ArrowPosition = BubbleShowCase.ArrowPosition.BOTTOM){
    when(enumActivity){
        EnumActivity.REVIEW_ACTIVITY ->{
           if (Utils.isShowGuideReview()){
               return
           }
        }
        EnumActivity.SCANNER_RESULT_ACTIVITY ->{
            if (Utils.isShowGuideScannerResult()){
                return
            }
        }
        EnumActivity.CHANGE_DESIGN ->{
            if (Utils.isShowChangeDesignIcon()){
                return
            }
        }
        EnumActivity.TEXT_ACTIVITY ->{
            if (Utils.isShowChangeDesignText()){
                return
            }
        }
        else -> {}
    }
    ContextCompat.getDrawable(this, icon)?.let {
        BubbleShowCaseBuilder(this) //Activity instance
            .arrowPosition(position) //You can force the position of the arrow to change the location of the bubble.
            .backgroundColor(R.color.colorAccent.fromColorIntRes) //Bubble background color
            .textColor(R.color.white.fromColorIntRes) //Bubble Text color
            .image(it)
            .title(message) //Any title for the bubble view
            .targetView(view) //View to point out
            .listener(object : BubbleShowCaseListener { //Listener for user actions
                override fun onTargetClick(bubbleShowCase: BubbleShowCase) {
                    onUpdate(enumActivity)
                }
                override fun onCloseActionImageClick(bubbleShowCase: BubbleShowCase) {
                    //Called when the user clicks the close button
                    onUpdate(enumActivity)
                }

                override fun onBubbleClick(bubbleShowCase: BubbleShowCase) {
                    //Called when the user clicks on the bubble
                    onUpdate(enumActivity)
                }

                override fun onBackgroundDimClick(bubbleShowCase: BubbleShowCase) {
                    //Called when the user clicks on the background dim
                    onUpdate(enumActivity)
                }
            })
            .show()
    }
}

fun AppCompatActivity.onUpdate(enumActivity: EnumActivity){
    when(enumActivity) {
        EnumActivity.REVIEW_ACTIVITY -> {
            Utils.putShowGuideReview(true)
        }
        EnumActivity.SCANNER_RESULT_ACTIVITY -> {
            Utils.putShowGuideScannerResult(true)
        }
        EnumActivity.CHANGE_DESIGN -> {
            Utils.putShowChangeDesignIcon(true)
        }
        EnumActivity.TEXT_ACTIVITY ->{
            Utils.putShowChangeDesignText(true)
        }
        else -> {}
    }
}