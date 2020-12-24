package tpcreative.co.qrscanner.common.api.request
import tpcreative.co.qrscanner.BuildConfig
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import java.io.Serializable

class CheckoutRequest : Serializable {
    var device_id: String?
    var autoRenewing: Boolean
    var orderId: String?
    var packageName: String?
    var sku: String?
    var state: String?
    var token: String?
    var device_type: String?
    var manufacturer: String?
    var name_model: String?
    var version: String?
    var versionRelease: String?
    var appVersionRelease: String?

    constructor(autoRenewing: Boolean, orderId: String?, sku: String?, state: String?, token: String?) {
        this.autoRenewing = autoRenewing
        this.sku = sku
        this.orderId = orderId
        this.state = state
        this.token = token
        device_id = QRScannerApplication.getInstance().getDeviceId()
        device_type = QRScannerApplication.getInstance().getString(R.string.device_type)
        manufacturer = QRScannerApplication.getInstance().getManufacturer()
        name_model = QRScannerApplication.getInstance().getModel()
        version = "" + QRScannerApplication.getInstance().getVersion()
        versionRelease = QRScannerApplication.getInstance().getVersionRelease()
        appVersionRelease = BuildConfig.VERSION_NAME
        packageName = QRScannerApplication.getInstance().getPackageId()
    }

    constructor() {
        autoRenewing = false
        sku = "Pro version"
        orderId = "Pro version"
        state = "Pro version"
        token = "Pro version"
        device_id = QRScannerApplication.getInstance().getDeviceId()
        device_type = QRScannerApplication.getInstance().getString(R.string.device_type)
        manufacturer = QRScannerApplication.getInstance().getManufacturer()
        name_model = QRScannerApplication.getInstance().getModel()
        version = "" + QRScannerApplication.getInstance().getVersion()
        versionRelease = QRScannerApplication.getInstance().getVersionRelease()
        appVersionRelease = BuildConfig.VERSION_NAME
        packageName = QRScannerApplication.getInstance().getPackageId()
    }
}