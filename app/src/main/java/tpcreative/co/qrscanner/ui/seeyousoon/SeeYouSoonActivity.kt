package tpcreative.co.qrscanner.ui.seeyousoon

import android.os.Bundle
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.activity.BaseActivity

class SeeYouSoonActivity : BaseActivity() {
    private val DELAY_TO_SHOW_UI = 3000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_see_you_soon)
        Utils.onObserveVisitView(DELAY_TO_SHOW_UI.toLong()) {
            finish()
            Utils.Log(TAG, "See you soon")
        }
    }

    override fun onBackPressed() {}

    companion object {
        private val TAG = SeeYouSoonActivity::class.java.simpleName
    }
}