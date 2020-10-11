package tpcreative.co.qrscanner.ui.pro;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import com.google.gson.Gson;
import org.solovyev.android.checkout.ActivityCheckout;
import org.solovyev.android.checkout.Billing;
import org.solovyev.android.checkout.BillingRequests;
import org.solovyev.android.checkout.Checkout;
import org.solovyev.android.checkout.Inventory;
import org.solovyev.android.checkout.ProductTypes;
import org.solovyev.android.checkout.Purchase;
import org.solovyev.android.checkout.RequestListener;
import org.solovyev.android.checkout.Sku;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import butterknife.BindView;
import de.mrapp.android.dialog.MaterialDialog;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.GenerateSingleton;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide;
import tpcreative.co.qrscanner.common.controller.ServiceManager;
import tpcreative.co.qrscanner.common.services.QRScannerApplication;

public class ProVersionActivity extends BaseActivitySlide implements View.OnClickListener{

    @BindView(R.id.btnUpgradeNow)
    Button btnUpgradeNow;
    @BindView(R.id.tvPrice)
    TextView tvPrice;

    private ActivityCheckout mCheckout;
    private InventoryCallback mInventoryCallback;
    private Inventory.Product mProduct;
    private Sku mLifetime;
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
        onStartInAppPurchase();
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
        if (mProduct==null){
            return;
        }
        if (mProduct.getSkus().size()>0){
            if (mLifetime!=null){
                final Purchase purchase = mProduct.getPurchaseInState(mLifetime, Purchase.State.PURCHASED);
                if (purchase != null) {
                    Toast.makeText(getApplicationContext(),"Already charged",Toast.LENGTH_SHORT).show();
                    consume(purchase);
                } else {
                    Utils.Log(TAG,"value...?"+ new Gson().toJson(mLifetime));
                    purchase(mLifetime);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCheckout!=null){
            mCheckout.stop();
        }
    }

    /* Start in app purchase */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCheckout.onActivityResult(requestCode, resultCode, data);
    }

    private class InventoryCallback implements Inventory.Callback {
        @Override
        public void onLoaded(Inventory.Products products) {
            final Inventory.Product product = products.get(ProductTypes.IN_APP);
            if (!product.supported) {
                // billing is not supported, user can't purchase anything
                return;
            }
            mProduct = product;
            if (mProduct.getSkus().size()>0){
                for (int i=0;i<mProduct.getSkus().size();i++){
                    Sku index = mProduct.getSkus().get(i);
                    if (index.id.code.equals(getString(R.string.lifetime))){
                        tvPrice.setText(index.price);
                        mLifetime = index;
                    }
                }
            }
            Utils.Log(TAG,"value : "+ new Gson().toJson(product));
        }
    }

    /**
     * @return {@link RequestListener} that reloads inventory when the action is finished
     */

    private <T> RequestListener<T> makeRequestListener() {
        return new RequestListener<T>() {
            @Override
            public void onSuccess(@Nonnull T result) {
                try {
                    final Purchase purchase = (Purchase) result;
                    Utils.Log(TAG,new Gson().toJson(purchase));
                    ServiceManager.getInstance().onCheckout(purchase);
                    if (Utils.isRealCheckedOut(purchase.orderId)){
                        Utils.setPremium(true);
                        GenerateSingleton.getInstance().onCompletedGenerate();
                        finish();
                    }else{
                        Utils.setPremium(false);
                        askWarningFakeCheckout();
                    }
                }
                catch (Exception e){
                    Toast.makeText(getApplicationContext(),"Error "+e.getMessage(),Toast.LENGTH_SHORT).show();
                }
                reloadInventory();
            }
            @Override
            public void onError(int response, @Nonnull Exception e) {
                Utils.Log(TAG,"Error checkout " +e.getMessage());
                reloadInventory();
            }
        };
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

    private void consume(final Purchase purchase) {
        mCheckout.whenReady(new Checkout.EmptyListener() {
            @Override
            public void onReady(@Nonnull BillingRequests requests) {
                requests.consume(purchase.token, makeRequestListener());
            }
        });
    }

    public void onStartInAppPurchase(){
        final Billing billing = QRScannerApplication.getInstance().getBilling();
        mCheckout = Checkout.forActivity(this, billing);
        mInventoryCallback = new InventoryCallback();
        mCheckout.start();
        reloadInventory();
    }

    private void purchase(Sku sku) {
        final RequestListener<Purchase> listener = makeRequestListener();
        mCheckout.startPurchaseFlow(sku, null, listener);
    }

    private void reloadInventory() {
        List<String> mList = new ArrayList<>();
        mList.add(getString(R.string.lifetime));
        final Inventory.Request request = Inventory.Request.create();
        // load purchase info
        request.loadAllPurchases();
        // load SKU details
        request.loadSkus(ProductTypes.IN_APP,mList);
        mCheckout.loadInventory(request, mInventoryCallback);
    }
}
