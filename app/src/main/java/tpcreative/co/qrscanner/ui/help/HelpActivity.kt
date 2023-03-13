package tpcreative.co.qrscanner.ui.help
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.activity_help.*
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Constant
import tpcreative.co.qrscanner.common.Navigator
import tpcreative.co.qrscanner.common.ScannerSingleton
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.common.view.ads.AdsView
import tpcreative.co.qrscanner.model.EnumAction
import tpcreative.co.qrscanner.model.EnumScreens
import tpcreative.co.qrscanner.ui.supportedcode.SupportedCodeActivity
import tpcreative.co.qrscanner.ui.tipsscanning.TipsScanningActivity

class HelpActivity : BaseActivitySlide(), HelpAdapter.ItemSelectedListener {
    lateinit var viewModel : HelpViewModel
    var adapter: HelpAdapter? = null
    lateinit var llSmallAds : LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)
        llSmallAds = AdsView().createLayout()
        initUI()
    }

    override fun onDestroy() {
        super.onDestroy()
        ScannerSingleton.getInstance()?.setVisible()
    }

    override fun onResume() {
        super.onResume()
        QRScannerApplication.getInstance().onResumeAds(EnumScreens.HELP_FEEDBACK_LARGE)
        QRScannerApplication.getInstance().onResumeAds(EnumScreens.HELP_FEEDBACK_LARGE)
    }

    override fun onPause() {
        QRScannerApplication.getInstance().onPauseAds(EnumScreens.HELP_FEEDBACK_SMALL)
        QRScannerApplication.getInstance().onPauseAds(EnumScreens.HELP_FEEDBACK_LARGE)
        super.onPause()
    }


    /*show ads*/
    fun doShowAds(isShow: Boolean) {
        if (isShow) {
            QRScannerApplication.getInstance().loadHelpFeedbackSmallView(llSmallAds)
            QRScannerApplication.getInstance().loadHelpFeedbackLargeView(llLargeAds)
        }
    }

    override fun onClickItem(position: Int) {
        val mItem = viewModel.mList.get(position)
        when(mItem.type){
            EnumAction.SUPPORTED_CODES ->{
                Navigator.onIntent(this,SupportedCodeActivity::class.java)
            }
            EnumAction.TIPS_SCANNING ->{
                Navigator.onIntent(this,TipsScanningActivity::class.java)
            }
            EnumAction.SEND_US_AN_EMAIL ->{
                onAlertSendEmail()
            }
            EnumAction.GUIDES_VIDEO ->{
                Utils.watchYoutubeVideo(this,getString(R.string.youtubeid))
            }
            else -> {}
        }
    }
}