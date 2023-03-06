package tpcreative.co.qrscanner.common.controller
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.BillingProcessor.IPurchasesResponseListener
import com.anjlab.android.iab.v3.PurchaseInfo
import com.google.gson.Gson
import tpcreative.co.qrscanner.BuildConfig
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.ConstantKey
import tpcreative.co.qrscanner.common.SettingsSingleton
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.Utils.Log
import tpcreative.co.qrscanner.common.Utils.isAlreadyCheckout
import tpcreative.co.qrscanner.common.Utils.isRealCheckedOut
import tpcreative.co.qrscanner.common.api.request.CheckoutRequest
import tpcreative.co.qrscanner.common.extension.toText
import tpcreative.co.qrscanner.common.services.QRScannerApplication


class PremiumManager : BillingProcessor.IBillingHandler {
    val TAG = PremiumManager::class.java.simpleName
    private var bp: BillingProcessor? = null

    fun onStartInAppPurchase() {
        if (Utils.isInnovation()){
            if (BuildConfig.APPLICATION_ID == R.string.super_qrscanner_free_innovation.toText()){
                bp = BillingProcessor(QRScannerApplication.getInstance(), ConstantKey.GooglePlaySuperInnovationPublicKey, this)
            }else{
                bp = BillingProcessor(QRScannerApplication.getInstance(), ConstantKey.GooglePlayInnovationPublicKey, this)
            }
        }else{
            bp = BillingProcessor(QRScannerApplication.getInstance(), ConstantKey.GooglePlayPublicKey, this)
        }
        bp?.initialize()
    }

    fun onStop() {
        if (bp != null) {
            bp?.release()
        }
    }

    override fun onProductPurchased(productId: String, details: PurchaseInfo?) {
        TODO("Not yet implemented")
    }

    override fun onPurchaseHistoryRestored() {}

    override fun onBillingError(errorCode: Int, error: Throwable?) {}

    override fun onBillingInitialized() {
        if (bp?.isPurchased(Utils.getInAppId()) == true) {
            val details = bp?.getPurchaseInfo(Utils.getInAppId())
            /*Testing...*/
//            bp?.consumePurchaseAsync(QRScannerApplication.getInstance().getString(R.string.lifetime),object :IPurchasesResponseListener{
//                override fun onPurchasesSuccess() {
//                }
//                override fun onPurchasesError() {
//                }
//            })
            if (details != null) {
                Log(TAG, Gson().toJson(details))
                val mPurchaseData = details.purchaseData
                if (mPurchaseData != null) {
                    if (isRealCheckedOut(mPurchaseData.orderId)) {
                        Utils.setCheckoutValue(true)
                        SettingsSingleton.getInstance()?.onUpdatedPremiumVersion()
                    } else {
                        Utils.setCheckoutValue(false)
                    }
                }else{
                    Utils.setCheckoutValue(false)
                }
            }else{
                Utils.setCheckoutValue(false)
            }
        }else{
            Utils.setCheckoutValue(false)
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
