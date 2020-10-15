package tpcreative.co.qrscanner.ui.pro;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.PurchaseData;
import com.anjlab.android.iab.v3.SkuDetails;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.google.gson.Gson;
import butterknife.BindView;
import de.mrapp.android.dialog.MaterialDialog;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.GenerateSingleton;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide;
import tpcreative.co.qrscanner.common.controller.ServiceManager;

public class ProVersionActivity extends BaseActivitySlide implements View.OnClickListener, BillingProcessor.IBillingHandler {

    @BindView(R.id.btnUpgradeNow)
    AppCompatButton btnUpgradeNow;
    @BindView(R.id.tvPrice)
    AppCompatTextView tvPrice;
    private BillingProcessor bp;
    private static String TAG = ProVersionActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pro_version);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        btnUpgradeNow.setOnClickListener(this);
        setTitle(getString(R.string.pro_version));
        bp = new BillingProcessor(this,Utils.GOOGLE_CONSOLE_KEY, this);
        bp.initialize();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnUpgradeNow :{
                onUpgradeNow();
                break;
            }
        }
    }

    public void onUpgradeNow() {
        if (BillingProcessor.isIabServiceAvailable(this)){
            Utils.Log(TAG,"purchase new");
            if (bp.isPurchased(getString(R.string.lifetime))){
                Utils.Log(TAG,"Already charged");
                bp.consumePurchase(getString(R.string.lifetime));
            }else{
                bp.purchase(this,getString(R.string.lifetime));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bp != null) {
            bp.release();
        }
    }

    /* Start in app purchase */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void askWarningFakeCheckout() {
        MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(this,Utils.getCurrentTheme());
        dialogBuilder.setTitle(R.string.alert);
        dialogBuilder.setPadding(40,40,40,0);
        dialogBuilder.setMargin(60,0,60,0);
        dialogBuilder.setMessage(getString(R.string.warning_fake_checkout));
        dialogBuilder.setPositiveButton(R.string.got_it, null);
        MaterialDialog dialog = dialogBuilder.create();
        dialogBuilder.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
               finish();
            }
        });
        dialog.show();
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        Utils.Log(TAG,new Gson().toJson(details));
        final PurchaseData mPurchaseData = details.purchaseInfo.purchaseData;
        if (mPurchaseData!=null){
            ServiceManager.getInstance().onCheckout(mPurchaseData);
            if (Utils.isRealCheckedOut(mPurchaseData.orderId)){
                Utils.setPremium(true);
                GenerateSingleton.getInstance().onCompletedGenerate();
                finish();
            }else{
                Utils.setPremium(false);
                askWarningFakeCheckout();
            }
        }
    }

    @Override
    public void onPurchaseHistoryRestored() {

    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {

    }

    @Override
    public void onBillingInitialized() {
        Utils.Log(TAG,"Bill ready...");
        SkuDetails mProduction =  bp.getPurchaseListingDetails(getString(R.string.lifetime));
        if (mProduction!=null){
            tvPrice.setText(mProduction.priceText);
        }
        Utils.Log(TAG,new Gson().toJson(bp.getPurchaseTransactionDetails(getString(R.string.lifetime))));
    }
}
