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

fun AppCompatActivity.onShowGuide(view : View,activity : EnumActivity){
    when(activity){
        EnumActivity.REVIEW_ACTIVITY -> {
            if (Utils.isShowGuideReview()){
                return
            }
            ContextCompat.getDrawable(this, R.drawable.ic_skype_template)?.let {
                BubbleShowCaseBuilder(this) //Activity instance
                    .arrowPosition(BubbleShowCase.ArrowPosition.BOTTOM) //You can force the position of the arrow to change the location of the bubble.
                    .backgroundColor(R.color.colorAccent.fromColorIntRes) //Bubble background color
                    .textColor(R.color.white.fromColorIntRes) //Bubble Text color
                    .image(it)
                    .title(R.string.click_to_change_design_qr_code.toText()) //Any title for the bubble view
                    .targetView(view) //View to point out
                    .listener(object : BubbleShowCaseListener { //Listener for user actions
                        override fun onTargetClick(bubbleShowCase: BubbleShowCase) {
                            //Called when the user clicks the target
                            Utils.putShowGuideReview(true)
                        }

                        override fun onCloseActionImageClick(bubbleShowCase: BubbleShowCase) {
                            //Called when the user clicks the close button
                            Utils.putShowGuideReview(true)
                        }

                        override fun onBubbleClick(bubbleShowCase: BubbleShowCase) {
                            //Called when the user clicks on the bubble
                            Utils.putShowGuideReview(true)
                        }

                        override fun onBackgroundDimClick(bubbleShowCase: BubbleShowCase) {
                            //Called when the user clicks on the background dim
                            Utils.putShowGuideReview(true)
                        }
                    })
                    .show()
            }
        }
        EnumActivity.SCANNER_RESULT_ACTIVITY -> {
            if (Utils.isShowGuideScannerResult()){
                return
            }
            ContextCompat.getDrawable(this, R.drawable.ic_qrcode_bg)?.let {
                BubbleShowCaseBuilder(this) //Activity instance
                    .arrowPosition(BubbleShowCase.ArrowPosition.BOTTOM) //You can force the position of the arrow to change the location of the bubble.
                    .backgroundColor(R.color.colorAccent.fromColorIntRes) //Bubble background color
                    .textColor(R.color.white.fromColorIntRes) //Bubble Text color
                    .image(it)
                    .title(R.string.view_code.toText()) //Any title for the bubble view
                    .targetView(view) //View to point out
                    .listener(object : BubbleShowCaseListener { //Listener for user actions
                        override fun onTargetClick(bubbleShowCase: BubbleShowCase) {
                            //Called when the user clicks the target
                            Utils.putShowGuideScannerResult(true)
                        }

                        override fun onCloseActionImageClick(bubbleShowCase: BubbleShowCase) {
                            //Called when the user clicks the close button
                            Utils.putShowGuideScannerResult(true)
                        }

                        override fun onBubbleClick(bubbleShowCase: BubbleShowCase) {
                            //Called when the user clicks on the bubble
                            Utils.putShowGuideScannerResult(true)
                        }

                        override fun onBackgroundDimClick(bubbleShowCase: BubbleShowCase) {
                            //Called when the user clicks on the background dim
                            Utils.putShowGuideScannerResult(true)
                        }
                    })
                    .show()
            }
        }
        else ->{

        }
    }
}