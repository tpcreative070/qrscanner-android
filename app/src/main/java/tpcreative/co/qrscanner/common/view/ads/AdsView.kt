package tpcreative.co.qrscanner.common.view.ads
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.services.QRScannerApplication


class AdsView  : View{

    private lateinit var mSmallAds : LinearLayout

    constructor(context: Context) : super(context) {
        mSmallAds = createLayout()
    }
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
    }
    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0) {

    }

//    fun createLayout() : RelativeLayout {
//        val parent = RelativeLayout(QRScannerApplication.getInstance())
//        val params = RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
//            QRScannerApplication.getInstance().getMaximumBannerHeight())
//        parent.gravity = Gravity.BOTTOM or Gravity.CENTER
//        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
//        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
//        parent.layoutParams = params;
//        return parent
//    }

    fun getSmallAds() :LinearLayout{
        return mSmallAds
    }

    private fun createLayout() : LinearLayout {
        val parent = LinearLayout(QRScannerApplication.getInstance())
        parent.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
            QRScannerApplication.getInstance().getMaximumBannerHeight())
        parent.orientation = LinearLayout.VERTICAL
        parent.gravity = Gravity.BOTTOM or Gravity.CENTER
        return parent
    }

    private fun createSmallAdsLayout() : LinearLayout {
        val parent = LinearLayout(QRScannerApplication.getInstance())
        parent.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
            QRScannerApplication.getInstance().getMaximumBannerHeight())
        parent.orientation = LinearLayout.VERTICAL
        parent.gravity = Gravity.BOTTOM or Gravity.CENTER
        return parent
    }

    private fun createRemoteSmallAds() : LinearLayout {
        val parent = LinearLayout(QRScannerApplication.getInstance())
        parent.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
            R.dimen.icon_size_50)
        parent.orientation = LinearLayout.VERTICAL
        parent.gravity = Gravity.BOTTOM or Gravity.CENTER

        val tv = TextView(QRScannerApplication.getInstance())
        tv.text = QRScannerApplication.getInstance().getString(R.string.remove_ads)
        tv.layoutParams =
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        parent.addView(tv)
        return parent
    }

    private fun createLargeLayout() : LinearLayout {
        val parent = LinearLayout(QRScannerApplication.getInstance())
        parent.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
            QRScannerApplication.getInstance().getMaximumBannerHeight())
        parent.orientation = LinearLayout.HORIZONTAL
        parent.gravity = Gravity.BOTTOM or Gravity.CENTER
        return parent
    }
}