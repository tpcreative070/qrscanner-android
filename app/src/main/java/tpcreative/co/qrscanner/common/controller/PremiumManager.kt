package tpcreative.co.qrscanner.common.controller
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import com.google.gson.Gson
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.Utils.Log
import tpcreative.co.qrscanner.common.Utils.isAlreadyCheckout
import tpcreative.co.qrscanner.common.Utils.isRealCheckedOut
import tpcreative.co.qrscanner.common.Utils.setPremium
import tpcreative.co.qrscanner.common.api.request.CheckoutRequest
import tpcreative.co.qrscanner.common.services.QRScannerApplication

class PremiumManager : BillingProcessor.IBillingHandler {
    val TAG = PremiumManager::class.java.simpleName
    private var bp: BillingProcessor? = null

    fun onStartInAppPurchase() {
        bp = BillingProcessor(QRScannerApplication.getInstance(), Utils.GOOGLE_CONSOLE_KEY, this)
        bp?.initialize()
    }

    fun onStop() {
        if (bp != null) {
            bp?.release()
        }
    }

    override fun onProductPurchased(productId: String, details: TransactionDetails?) {
        TODO("Not yet implemented")
    }

    override fun onPurchaseHistoryRestored() {}

    override fun onBillingError(errorCode: Int, error: Throwable?) {}

    override fun onBillingInitialized() {
        if (bp?.isPurchased(QRScannerApplication.getInstance().getString(R.string.lifetime)) == true) {
            val details = bp?.getPurchaseTransactionDetails(QRScannerApplication.getInstance().getString(R.string.lifetime))
            if (details != null) {
                Log(TAG, Gson().toJson(details))
                val mPurchaseData = details.purchaseInfo.purchaseData
                if (mPurchaseData != null) {
                    if (isRealCheckedOut(mPurchaseData.orderId)) {
                        if (!isAlreadyCheckout()) {
                            ServiceManager.getInstance().onCheckout(CheckoutRequest(mPurchaseData))
                        }
                        setPremium(true)
                    } else {
                        setPremium(false)
                    }
                }else{
                    setPremium(false)
                }
            }else{
                setPremium(false)
            }
        }else{
            setPremium(false)
        }
    }

    companion object {
        private var instance: PremiumManager? = null
        fun getInstance(): PremiumManager {
            if (instance == null) {
                instance = PremiumManager()
            }
            return instance!!
        }
    }
}
