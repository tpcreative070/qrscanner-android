package tpcreative.co.qrscanner.common.controller;
import org.solovyev.android.checkout.Billing;
import org.solovyev.android.checkout.Checkout;
import org.solovyev.android.checkout.Inventory;
import org.solovyev.android.checkout.ProductTypes;
import org.solovyev.android.checkout.Purchase;
import org.solovyev.android.checkout.Sku;
import java.util.ArrayList;
import java.util.List;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.services.QRScannerApplication;

public class PremiumManager {
    private static PremiumManager instance;
    private Checkout mCheckout;
    private InventoryCallback mInventoryCallback;
    private Inventory.Product mProduct;
    private String TAG = PremiumManager.class.getSimpleName();

    public static PremiumManager getInstance() {
        if (instance == null) {
            instance = new PremiumManager();
        }
        return instance;
    }

    private class InventoryCallback implements Inventory.Callback {
        @Override
        public void onLoaded(Inventory.Products products) {
            final Inventory.Product product = products.get(ProductTypes.SUBSCRIPTION);
            if (!product.supported) {
                // billing is not supported, user can't purchase anything
                return;
            }
            mProduct = product;
            if (mProduct != null) {
                if (mProduct.getSkus().size() > 0) {
                    for (int i = 0; i < mProduct.getSkus().size(); i++) {
                        Sku index = mProduct.getSkus().get(i);
                       if (index.id.code.equals(QRScannerApplication.getInstance().getString(R.string.one_years))) {
                            final Purchase purchaseExpire = mProduct.getPurchaseInState(index, Purchase.State.EXPIRED);
                            if (purchaseExpire != null) {
                               Utils.setPremium(false);
                            }else{
                                final Purchase purchase = mProduct.getPurchaseInState(index, Purchase.State.PURCHASED);
                                if (purchase == null) {
                                   Utils.setPremium(false);
                                } else {
                                   Utils.setPremium(true);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void onStartInAppPurchase() {
        final Billing billing = QRScannerApplication.getInstance().getBilling();
        mCheckout = Checkout.forApplication(billing);
        mInventoryCallback = new InventoryCallback();
        mCheckout.start();
        reloadInventory();
    }

    private void reloadInventory() {
        List<String> mList = new ArrayList<>();
        mList.add(getString(R.string.one_years));
        final Inventory.Request request = Inventory.Request.create();
        request.loadAllPurchases();
        request.loadSkus(ProductTypes.SUBSCRIPTION, mList);
        mCheckout.loadInventory(request, mInventoryCallback);
    }

    public String getString(int value) {
        return QRScannerApplication.getInstance().getString(value);
    }

    public void onStop() {
        if (mCheckout != null) {
            mCheckout.stop();
        }
    }
}