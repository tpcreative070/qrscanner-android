package tpcreative.co.qrscanner.ui.filecolor
import android.graphics.Bitmap
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.android.synthetic.main.activity_chage_file_color.*
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Constant
import tpcreative.co.qrscanner.common.SettingsSingleton
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.common.view.ads.AdsView
import tpcreative.co.qrscanner.model.*
import java.util.*

class ChangeFileColorActivity : BaseActivitySlide(), ChangeFileColorAdapter.ItemSelectedListener {
    private var bitmap: Bitmap? = null
    lateinit var viewModel : ChangeFileColorViewModel
    var adapter: ChangeFileColorAdapter? = null
    lateinit var llSmallAds : LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chage_file_color)
        llSmallAds = AdsView().createLayout()
        initUI()

    }

    override fun onClickItem(position: Int) {
        viewModel.mTheme = dataSource[position]
        Utils.setQRCodeThemePosition(position)
        getData()
        SettingsSingleton.getInstance()?.onUpdated()
    }

    override fun onResume() {
        super.onResume()
        QRScannerApplication.getInstance().onResumeAds(EnumScreens.CHANGE_COLOR_SMALL)
        QRScannerApplication.getInstance().onResumeAds(EnumScreens.CHANGE_COLOR_LARGE)
    }

    override fun onPause() {
        QRScannerApplication.getInstance().onPauseAds(EnumScreens.CHANGE_COLOR_SMALL)
        QRScannerApplication.getInstance().onPauseAds(EnumScreens.CHANGE_COLOR_LARGE)
        super.onPause()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                showAds()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    /*show ads*/
    fun doShowAds(isShow: Boolean) {
        if (isShow) {
            QRScannerApplication.getInstance().loadChangeColorSmallView(llSmallAds)
            QRScannerApplication.getInstance().loadChangeColorLargeView(llLargeAds)
        }
    }

    fun onGenerateReview(code: String?) {
        try {
            val barcodeEncoder = BarcodeEncoder()
            val hints: MutableMap<EncodeHintType?, Any?> = EnumMap(EncodeHintType::class.java)
            hints[EncodeHintType.MARGIN] = 2
            val theme: Theme? = Theme.getInstance()?.getThemeInfo()
            bitmap = barcodeEncoder.encodeBitmap(this, theme?.getPrimaryDarkColor() ?:0, code, BarcodeFormat.QR_CODE, Constant.QRCodeViewWidth, Constant.QRCodeViewHeight, hints)
            imgResult.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val dataSource : MutableList<Theme>
        get() {
            return adapter?.getDataSource() ?: mutableListOf()
        }
}