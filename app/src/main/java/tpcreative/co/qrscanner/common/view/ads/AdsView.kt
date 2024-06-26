package tpcreative.co.qrscanner.common.view.ads
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Navigator
import tpcreative.co.qrscanner.common.controller.ServiceManager
import tpcreative.co.qrscanner.common.extension.*
import tpcreative.co.qrscanner.common.services.QRScannerApplication


class AdsView  : View{

    private lateinit var mRootSmallAds : LinearLayout
    private lateinit var mRootLargeAds : LinearLayout
    private lateinit var mSmallAds : LinearLayout
    private lateinit var mRemoveSmallAds : LinearLayout
    private lateinit var mLargeAds : LinearLayout
    private lateinit var mLargeTextAds : View
    private lateinit var mLargeContentAds : LinearLayout
    private lateinit var mViewUnderLine : View
    private lateinit var mRemoveLargeAds : LinearLayout

    constructor(context: Context) : super(context) {
        mRemoveSmallAds = createRemoveSmallAds()
        mSmallAds = createSmallAdsLayout()
        mRootSmallAds = createRootSmallLayout()

        mViewUnderLine = createUnderLine()
        mLargeTextAds = createLargeTextAds()
        mLargeAds = createLargeAdsLayout()
        mRemoveLargeAds = createRemoveLargeAds()
        mLargeContentAds = createLargeAdsContentLayout()
        mRootLargeAds = createRootLargeLayout()

        mRemoveLargeAds.addRipple()
        mRemoveLargeAds.setOnClickListener {
            Navigator.onFromAdsMoveToProVersion(QRScannerApplication.getInstance())
        }
        mRemoveSmallAds.addRipple()
        mRemoveSmallAds.setOnClickListener {
            Navigator.onFromAdsMoveToProVersion(QRScannerApplication.getInstance())
        }
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {

    }
    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0) {

    }

    private fun createRootSmallLayout() : LinearLayout {
        val parent = LinearLayout(QRScannerApplication.getInstance())
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
            QRScannerApplication.getInstance().getMaximumBannerHeight())
        params.gravity = Gravity.CENTER or Gravity.BOTTOM
        parent.orientation =  LinearLayout.VERTICAL
        val mVersion = ServiceManager.getInstance().mVersion
        if (mVersion?.hiddenRemoveSmallAds != true){
            parent.addView(mRemoveSmallAds)
        }
        parent.addView(mSmallAds)
        parent.layoutParams = params
        return parent
    }

    private fun createSmallAdsLayout() : LinearLayout {
        val parent = LinearLayout(QRScannerApplication.getInstance())
        parent.orientation = LinearLayout.VERTICAL
        parent.gravity = Gravity.BOTTOM or Gravity.CENTER
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        ).apply {
            gravity = Gravity.BOTTOM or  Gravity.CENTER
        }
        parent.layoutParams = params
        return parent
    }

    private fun createRemoveSmallAds() : LinearLayout {
        val parent = LinearLayout(QRScannerApplication.getInstance())
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM or  Gravity.CENTER
        }
        parent.orientation = LinearLayout.VERTICAL
        parent.layoutParams = params
        val tv = TextView(QRScannerApplication.getInstance())
        tv.text = QRScannerApplication.getInstance().getString(R.string.remove_ads)
        tv.setPadding(0, 5F.px,0,5F.px)
        tv.setTextColor(QRScannerApplication.getInstance().getColor(R.color.white))
        tv.layoutParams =
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        tv.gravity = Gravity.CENTER
        parent.addView(tv)
        return parent
    }

    private fun createRootLargeLayout() : LinearLayout {
        val parent = LinearLayout(QRScannerApplication.getInstance())
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.CENTER
        }
        parent.orientation =  LinearLayout.VERTICAL
        parent.addView(mViewUnderLine)
        parent.addView(mLargeTextAds)
        parent.addView(mLargeContentAds)
        parent.layoutParams = params
        return parent
    }

    private fun createLargeAdsContentLayout() : LinearLayout {
        val parent = LinearLayout(QRScannerApplication.getInstance())
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
        }
        parent.orientation = LinearLayout.HORIZONTAL
        parent.layoutParams = params
        val mVersion = ServiceManager.getInstance().mVersion
        if (mVersion?.hiddenRemoveLargeAds == true){
            val mLargeAdsNoRemoved = createLargeAdsLayoutWithNoRemoved()
            parent.addView(mLargeAdsNoRemoved)
        }else{
            parent.addView(mLargeAds)
            parent.addView(mRemoveLargeAds)
        }
        parent.margin(top = 5f, bottom = 10f.px.toFloat())
        return parent
    }

    private fun createLargeAdsLayoutWithNoRemoved() : RelativeLayout{
        val parent = RelativeLayout(QRScannerApplication.getInstance())
        val params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            addRule(RelativeLayout.CENTER_IN_PARENT,mLargeAds.id)
        }
        parent.gravity = Gravity.CENTER
        parent.layoutParams = params
        parent.addView(mLargeAds)
        return parent
    }

    private fun createLargeAdsLayout() : LinearLayout {
        val parent = LinearLayout(QRScannerApplication.getInstance())
        parent.layoutParams = LinearLayout.LayoutParams(300F.px,
            250F.px)
        parent.orientation = LinearLayout.VERTICAL
        parent.gravity = Gravity.CENTER
        val mVersion = ServiceManager.getInstance().mVersion
        if (mVersion?.hiddenRemoveLargeAds != true){
            if (context.isPortrait()){
                parent.margin(left = 5f.px.toFloat())
            }else{
                parent.margin(left = (QRScannerApplication.getInstance().getWidth()/3.5).toFloat())
            }
        }
        parent.setBackgroundColor(ContextCompat.getColor(QRScannerApplication.getInstance(),R.color.lightAds))
        return parent
    }

    private fun createUnderLine() : View {
        val parent = View(QRScannerApplication.getInstance())
        parent.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
            1F.px)
        parent.setBackgroundColor(ContextCompat.getColor(QRScannerApplication.getInstance(),R.color.grey_lighter))
        return parent
    }

    private fun createLargeTextAds() : View {
        val parent = LinearLayout(QRScannerApplication.getInstance())
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
        }
        parent.orientation = LinearLayout.VERTICAL
        parent.layoutParams = params
        val tv = TextView(QRScannerApplication.getInstance())
        tv.text = QRScannerApplication.getInstance().getString(R.string.advertising)
        tv.setPadding(0, 5F.px,0,5F.px)
        tv.gravity = Gravity.CENTER
        tv.setTypeface(tv.typeface, Typeface.NORMAL)
        tv.setTextColor(ContextCompat.getColor(context,R.color.black))
        val mTextParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        mTextParams.topMargin = 10f.px
        tv.layoutParams = mTextParams
        parent.addView(tv)
        return parent
    }

    private fun createRemoveLargeAds() : LinearLayout {
        val parent = LinearLayout(QRScannerApplication.getInstance())
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER or Gravity.TOP
        }
        parent.orientation = LinearLayout.VERTICAL
        parent.layoutParams = params
        val mImageView = ImageView(QRScannerApplication.getInstance())
        mImageView.setPadding(7F.px, 7F.px,7F.px,7F.px)
        mImageView.layoutParams =
            ViewGroup.LayoutParams(35f.px,35f.px)
        mImageView.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_close))
        mImageView.setColorFilter(ContextCompat.getColor(QRScannerApplication.getInstance(), R.color.material_black_1000), PorterDuff.Mode.SRC_ATOP)
        mImageView.setBackgroundColor(ContextCompat.getColor(context,R.color.lightAds))
        parent.addView(mImageView)
        return parent
    }

    fun getSmallAds() :LinearLayout{
        return mSmallAds
    }

    fun getRootSmallAds() : LinearLayout {
        return mRootSmallAds
    }

    fun getRootLargeAds() : LinearLayout {
        return mRootLargeAds
    }

    fun getLargeAds() : LinearLayout {
        return mLargeAds
    }
}