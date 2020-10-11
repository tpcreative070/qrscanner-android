package tpcreative.co.qrscanner.common.api.request;
import java.io.Serializable;
import tpcreative.co.qrscanner.BuildConfig;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.services.QRScannerApplication;

public class CheckoutRequest implements Serializable {
    public String device_id;
    public boolean autoRenewing;
    public String orderId;
    public String packageName;
    public String sku;
    public String state;
    public String token;
    public String device_type;
    public String manufacturer;
    public String name_model;
    public String version;
    public String versionRelease;
    public String appVersionRelease;

    public CheckoutRequest(boolean autoRenewing, String orderId, String sku, String state, String token){
        this.autoRenewing = autoRenewing;
        this.sku = sku;
        this.orderId = orderId;
        this.state = state;
        this.token = token;
        this.device_id = QRScannerApplication.getInstance().getDeviceId();
        this.device_type = QRScannerApplication.getInstance().getString(R.string.device_type);
        this.manufacturer =  QRScannerApplication.getInstance().getManufacturer();
        this.name_model = QRScannerApplication.getInstance().getModel();
        this.version = ""+QRScannerApplication.getInstance().getVersion();
        this.versionRelease = QRScannerApplication.getInstance().getVersionRelease();
        this.appVersionRelease =  BuildConfig.VERSION_NAME;
        this.packageName = QRScannerApplication.getInstance().getPackageId();
    }
    public CheckoutRequest(){
        this.autoRenewing = false;
        this.sku = "Pro version";
        this.orderId = "Pro version";
        this.state = "Pro version";
        this.token = "Pro version";
        this.device_id = QRScannerApplication.getInstance().getDeviceId();
        this.device_type = QRScannerApplication.getInstance().getString(R.string.device_type);
        this.manufacturer =  QRScannerApplication.getInstance().getManufacturer();
        this.name_model = QRScannerApplication.getInstance().getModel();
        this.version = ""+QRScannerApplication.getInstance().getVersion();
        this.versionRelease = QRScannerApplication.getInstance().getVersionRelease();
        this.appVersionRelease =  BuildConfig.VERSION_NAME;
        this.packageName = QRScannerApplication.getInstance().getPackageId();
    }
}
