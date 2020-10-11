package tpcreative.co.qrscanner.common.controller;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.PurchaseData;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.google.gson.Gson;

import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.services.QRScannerApplication;

public class PremiumManager implements BillingProcessor.IBillingHandler {
    private static PremiumManager instance;
    private String TAG = PremiumManager.class.getSimpleName();
    BillingProcessor bp;
    public static PremiumManager getInstance() {
        if (instance == null) {
            instance = new PremiumManager();
        }
        return instance;
    }

    public void onStartInAppPurchase() {
        bp = new BillingProcessor(QRScannerApplication.getInstance(), Utils.GOOGLE_CONSOLE_KEY, this);
        bp.initialize();
    }

    public void onStop() {
        if (bp != null) {
            bp.release();
        }
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {

    }

    @Override
    public void onPurchaseHistoryRestored() {

    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {

    }

    @Override
    public void onBillingInitialized() {
        if (bp.isPurchased(QRScannerApplication.getInstance().getString(R.string.lifetime))){
            final TransactionDetails details = bp.getPurchaseTransactionDetails(QRScannerApplication.getInstance().getString(R.string.lifetime));
            if (details!=null){
                Utils.Log(TAG,new Gson().toJson(details));
                final PurchaseData mPurchaseData = details.purchaseInfo.purchaseData;
                if (mPurchaseData!=null){
                    if (Utils.isRealCheckedOut(mPurchaseData.orderId)){
                        Utils.setPremium(true);
                    }else{
                        Utils.setPremium(false);
                    }
                }
            }
        }
    }
}
