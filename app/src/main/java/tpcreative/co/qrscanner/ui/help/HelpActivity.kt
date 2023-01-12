package tpcreative.co.qrscanner.ui.help
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_help.llSmallAds
import kotlinx.android.synthetic.main.activity_help.rlAdsRoot
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Constant
import tpcreative.co.qrscanner.common.Navigator
import tpcreative.co.qrscanner.common.ScannerSingleton
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.model.EnumAction
import tpcreative.co.qrscanner.ui.supportedcode.SupportedCodeActivity
import tpcreative.co.qrscanner.ui.tipsscanning.TipsScanningActivity

class HelpActivity : BaseActivitySlide(), HelpAdapter.ItemSelectedListener {
    lateinit var viewModel : HelpViewModel
    var adapter: HelpAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)
        initUI()
    }

    override fun onDestroy() {
        super.onDestroy()
        ScannerSingleton.getInstance()?.setVisible()
    }

    override fun onResume() {
        super.onResume()
    }

    /*show ads*/
    fun doShowAds(isShow: Boolean) {
        if (isShow) {
            QRScannerApplication.getInstance().loadHelpFeedbackSmallView(llSmallAds)
        } else {
            rlAdsRoot.visibility = View.GONE
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
                Utils.watchYoutubeVideo(this,Constant.youtube_id)
            }
            else -> {}
        }
    }
}