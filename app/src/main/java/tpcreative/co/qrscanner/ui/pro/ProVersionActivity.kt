package tpcreative.co.qrscanner.ui.pro
import android.os.Bundle
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.PurchaseInfo
import com.anjlab.android.iab.v3.SkuDetails
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_pro_version.*
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.ConstantKey
import tpcreative.co.qrscanner.common.GenerateSingleton
import tpcreative.co.qrscanner.common.SettingsSingleton
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide

class ProVersionActivity : BaseActivitySlide(), View.OnClickListener, BillingProcessor.IBillingHandler  {
    private var bp: BillingProcessor? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pro_version)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        btnUpgradeNow.setOnClickListener(this)
        title = getString(R.string.pro_version)
        bp = BillingProcessor(this, ConstantKey.GooglePlayPublicKey, this)
        bp?.initialize()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnUpgradeNow -> {
                onUpgradeNow()
            }
        }
    }

    private fun askWarningFakeCheckout() {
        MaterialDialog(this).show {
            title(R.string.alert)
            message(R.string.warning_fake_checkout)
            positiveButton(R.string.got_it){
                finish()
            }
        }
    }

    private fun onUpgradeNow() {
        if (BillingProcessor.isIabServiceAvailable(this)) {
            Utils.Log(TAG, "purchase new")
            if (bp?.isPurchased(getString(R.string.lifetime)) == true) {
                Utils.Log(TAG, "Already charged")
                bp?.consumePurchaseAsync(getString(R.string.lifetime),object : BillingProcessor.IPurchasesResponseListener{
                    override fun onPurchasesSuccess() {
                    }
                    override fun onPurchasesError() {
                    }
                })
                bp?.loadOwnedPurchasesFromGoogleAsync(object  : BillingProcessor.IPurchasesResponseListener{
                    override fun onPurchasesSuccess() {
                    }
                    override fun onPurchasesError() {
                    }
                })
            } else {
                bp?.purchase(this, getString(R.string.lifetime))
            }
        }
    }

    override fun onDestroy() {
        if (bp != null){
            bp?.release()
        }
        super.onDestroy()
    }

    override fun onProductPurchased(productId: String, details: PurchaseInfo?) {
        Utils.Log(TAG, Gson().toJson(details))
        val mPurchaseData = details?.purchaseData
        if (mPurchaseData != null) {
            if (Utils.isRealCheckedOut(mPurchaseData.orderId)) {
                Utils.setCheckoutValue(true)
                SettingsSingleton.getInstance()?.onUpdatedPremiumVersion()
                finish()
            } else {
                Utils.setCheckoutValue(false)
                askWarningFakeCheckout()
            }
        }
    }

    override fun onPurchaseHistoryRestored() {

    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {

    }

    override fun onBillingInitialized() {
        Utils.Log(TAG, "Bill ready...")
        bp?.getPurchaseListingDetailsAsync(getString(R.string.lifetime),object  : BillingProcessor.ISkuDetailsResponseListener{
            override fun onSkuDetailsResponse(products: MutableList<SkuDetails>?) {
                val mPrice = products?.get(0)?.priceText
                tvPrice.text = mPrice
            }
            override fun onSkuDetailsError(error: String?) {
            }
        })
    }
}