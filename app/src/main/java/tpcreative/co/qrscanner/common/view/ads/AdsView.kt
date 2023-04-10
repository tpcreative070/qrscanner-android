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
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.afollestad.materialdialogs.utils.MDUtil.isLandscape
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Navigator
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
        parent.addView(mRemoveSmallAds)
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
        tv.setPadding(0, QRScannerApplication.getInstance().pxToDp(5F).toInt(),0,QRScannerApplication.getInstance().pxToDp(5F).toInt())
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
        parent.addView(mLargeAds)
        parent.addView(mRemoveLargeAds)
        return parent
    }

    private fun createLargeAdsLayout() : LinearLayout {
        val parent = LinearLayout(QRScannerApplication.getInstance())
        parent.layoutParams = LinearLayout.LayoutParams(context.pxToDp(300F).toInt(),
            context.pxToDp(250F).toInt())
        parent.orientation = LinearLayout.VERTICAL
        parent.gravity = Gravity.CENTER
        if (context.isLandscape()){
            parent.margin(left = (QRScannerApplication.getInstance().getWidth()/3.5).toFloat(),top = context.pxToDp(5f), bottom = context.pxToDp(10f))
        }else{
            parent.margin(left = context.pxToDp(5f),top = context.pxToDp(5f), bottom = context.pxToDp(10f))
        }

        parent.setBackgroundColor(ContextCompat.getColor(QRScannerApplication.getInstance(),R.color.lightAds))
        return parent
    }

    private fun createUnderLine() : View {
        val parent = View(QRScannerApplication.getInstance())
        parent.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
            context.pxToDp(1F).toInt())
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
        tv.setPadding(0, QRScannerApplication.getInstance().pxToDp(5F).toInt(),0,QRScannerApplication.getInstance().pxToDp(5F).toInt())
        tv.layoutParams =
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        tv.gravity = Gravity.CENTER
        tv.setTypeface(tv.typeface, Typeface.BOLD)
        tv.setTextColor(ContextCompat.getColor(context,R.color.black))
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
        params.setMargins(0,  context.pxToDp(10f).toInt(),0,0)
        parent.layoutParams = params
        val mImageView = ImageView(QRScannerApplication.getInstance())
        mImageView.setPadding(QRScannerApplication.getInstance().pxToDp(7F).toInt(), QRScannerApplication.getInstance().pxToDp(7F).toInt(),QRScannerApplication.getInstance().pxToDp(7F).toInt(),QRScannerApplication.getInstance().pxToDp(7F).toInt())
        mImageView.layoutParams =
            ViewGroup.LayoutParams(context.pxToDp(35f).toInt(),context.pxToDp(35f).toInt() )
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