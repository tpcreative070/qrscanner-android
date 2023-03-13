package tpcreative.co.qrscanner.common.view.ads
import android.view.Gravity
import android.widget.LinearLayout
import tpcreative.co.qrscanner.common.services.QRScannerApplication

class AdsView {

    fun createLayout() : LinearLayout {
        val parent = LinearLayout(QRScannerApplication.getInstance())
        parent.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
            QRScannerApplication.getInstance().getMaximumBannerHeight())
        parent.orientation = LinearLayout.HORIZONTAL
        parent.gravity = Gravity.BOTTOM or Gravity.CENTER
        return parent
    }

}