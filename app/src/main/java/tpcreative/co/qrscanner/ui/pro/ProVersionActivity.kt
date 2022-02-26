package tpcreative.co.qrscanner.ui.pro
import android.os.Bundle
import android.view.View
//import com.anjlab.android.iab.v3.BillingProcessor
//import com.anjlab.android.iab.v3.PurchaseInfo
//import com.anjlab.android.iab.v3.SkuDetails
//import com.google.gson.Gson
//import de.mrapp.android.dialog.MaterialDialog
//import kotlinx.android.synthetic.main.activity_pro_version.*
//import tpcreative.co.qrscanner.R
//import tpcreative.co.qrscanner.common.GenerateSingleton
//import tpcreative.co.qrscanner.common.Utils
//import tpcreative.co.qrscanner.common.activity.BaseActivitySlide
//import tpcreative.co.qrscanner.common.api.request.CheckoutRequest
//import tpcreative.co.qrscanner.common.controller.ServiceManager
//
//class ProVersionActivity : BaseActivitySlide(), View.OnClickListener, BillingProcessor.IBillingHandler  {
//    private var bp: BillingProcessor? = null
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_pro_version)
//        setSupportActionBar(toolbar)
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        btnUpgradeNow.setOnClickListener(this)
//        title = getString(R.string.pro_version)
//        bp = BillingProcessor(this, Utils.GOOGLE_CONSOLE_KEY, this)
//        bp?.initialize()
//    }
//
//    override fun onClick(view: View?) {
//        when (view?.id) {
//            R.id.btnUpgradeNow -> {
//                onUpgradeNow()
//            }
//        }
//    }
//
//    private fun askWarningFakeCheckout() {
//        val dialogBuilder: MaterialDialog.Builder =  MaterialDialog.Builder(this, Utils.getCurrentTheme())
//        dialogBuilder.setTitle(R.string.alert)
//        dialogBuilder.setPadding(40, 40, 40, 0)
//        dialogBuilder.setMargin(60, 0, 60, 0)
//        dialogBuilder.setMessage(getString(R.string.warning_fake_checkout))
//        dialogBuilder.setPositiveButton(R.string.got_it, null)
//        val dialog: MaterialDialog = dialogBuilder.create()
//        dialogBuilder.setOnShowListener { finish() }
//        dialog.show()
//    }
//
//    private fun onUpgradeNow() {
//        if (BillingProcessor.isIabServiceAvailable(this)) {
//            Utils.Log(TAG, "purchase new")
//            if (bp?.isPurchased(getString(R.string.lifetime)) == true) {
//                Utils.Log(TAG, "Already charged")
//                bp?.consumePurchaseAsync(getString(R.string.lifetime),object : BillingProcessor.IPurchasesResponseListener{
//                    override fun onPurchasesSuccess() {
//                    }
//                    override fun onPurchasesError() {
//                    }
//                })
//                bp?.loadOwnedPurchasesFromGoogleAsync(object  : BillingProcessor.IPurchasesResponseListener{
//                    override fun onPurchasesSuccess() {
//                    }
//                    override fun onPurchasesError() {
//                    }
//                })
//            } else {
//                bp?.purchase(this, getString(R.string.lifetime))
//            }
//        }
//    }
//
//    override fun onDestroy() {
//        if (bp != null){
//            bp?.release()
//        }
//        super.onDestroy()
//    }
//
//    override fun onProductPurchased(productId: String, details: PurchaseInfo?) {
//        Utils.Log(TAG, Gson().toJson(details))
//        val mPurchaseData = details!!.purchaseData
//        if (mPurchaseData != null) {
//            ServiceManager.getInstance().onCheckout(mPurchaseData)
//            if (Utils.isRealCheckedOut(mPurchaseData.orderId)) {
//
//                ServiceManager.getInstance().onCheckout(CheckoutRequest(mPurchaseData))
//                GenerateSingleton.getInstance()?.onCompletedGenerate()
//                finish()
//            } else {
//                askWarningFakeCheckout()
//            }
//        }
//    }
//
//    override fun onPurchaseHistoryRestored() {
//
//    }
//
//    override fun onBillingError(errorCode: Int, error: Throwable?) {
//
//    }
//
//    override fun onBillingInitialized() {
//        Utils.Log(TAG, "Bill ready...")
//        bp?.getPurchaseListingDetailsAsync(getString(R.string.lifetime),object  : BillingProcessor.ISkuDetailsResponseListener{
//            override fun onSkuDetailsResponse(products: MutableList<SkuDetails>?) {
//                val mPrice = products?.get(0)?.priceText
//                tvPrice.text = mPrice
//            }
//            override fun onSkuDetailsError(error: String?) {
//            }
//        })
//    }
//}