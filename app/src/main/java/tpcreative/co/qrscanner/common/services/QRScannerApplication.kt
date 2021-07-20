package tpcreative.co.qrscanner.common.services
import android.accounts.Account
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.AdSettings
import com.google.android.gms.ads.*
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.snatik.storage.Storage
import tpcreative.co.qrscanner.BuildConfig
import tpcreative.co.qrscanner.BuildConfig.DEBUG
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.api.RetrofitBuilder
import tpcreative.co.qrscanner.common.api.RootAPI
import tpcreative.co.qrscanner.common.controller.PrefsController
import tpcreative.co.qrscanner.common.controller.ServiceManager
import tpcreative.co.qrscanner.helper.ThemeHelper
import tpcreative.co.qrscanner.model.EnumThemeMode
import tpcreative.co.qrscanner.model.EnumTypeServices
import tpcreative.co.qrscanner.ui.main.MainActivity
import java.util.*

class QRScannerApplication : MultiDexApplication(), Application.ActivityLifecycleCallbacks {
    private var pathFolder: String? = null
    private lateinit var storage: Storage
    private var isLive = false
    private var activity: MainActivity? = null
    private var adView: AdView? = null
    private var adLargeView: AdView? = null
    private var adAudienceLargeView : com.facebook.ads.AdView? = null
    private var isRequestAds = true
    private var isRequestLargeAds = true
    private var isRequestAudienceAds = true
    private var options: GoogleSignInOptions.Builder? = null
    private var requiredScopes: MutableSet<Scope>? = null
    private var requiredScopesString: MutableList<String>? = null
    override fun onCreate() {
        super.onCreate()
        mInstance = this
        isLive = true
        if (DEBUG) {
            AdSettings.setTestMode(true);
        }
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        serverAPI = RetrofitBuilder.getService(typeService = EnumTypeServices.SYSTEM)
        serverDriveApi = RetrofitBuilder.getService(getString(R.string.url_google), typeService = EnumTypeServices.GOOGLE_DRIVE)
        PrefsController.Builder()
                .setContext(applicationContext)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(packageName)
                .setUseDefaultSharedPreference(true)
                .build()
        if (!Utils.isPremium() && isLiveAds()) {
            Utils.Log(TAG, "Start ads")
            MobileAds.initialize(this) { }
        }
        ServiceManager.getInstance().setContext(this)
        storage = Storage(applicationContext)
        pathFolder = storage.externalStorageDirectory + "/Pictures/QRScanner"
        storage.createDirectory(pathFolder)
        val firstRunning: Boolean = PrefsController.getBoolean(getString(R.string.key_not_first_running), false)
        if (!firstRunning) {
            PrefsController.putBoolean(getString(R.string.key_not_first_running), true)
        }
        registerActivityLifecycleCallbacks(this)
        options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestScopes(Scope(DriveScopes.DRIVE_FILE))
                .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
        requiredScopes = HashSet(2)
        requiredScopes?.add(Scope(DriveScopes.DRIVE_FILE))
        requiredScopes?.add(Scope(DriveScopes.DRIVE_APPDATA))
        requiredScopesString = ArrayList()
        requiredScopesString?.add(DriveScopes.DRIVE_APPDATA)
        requiredScopesString?.add(DriveScopes.DRIVE_FILE)
        EnumThemeMode.byPosition(Utils.getPositionTheme())?.let { ThemeHelper.applyTheme(it) }
    }

    fun getGoogleSignInOptions(account: Account?): GoogleSignInOptions? {
        if (options != null) {
            if (account != null) {
                options?.setAccountName(account.name)
            }
            return options?.build()
        }
        return options?.build()
    }

    fun getRequiredScopesString(): MutableList<String>? {
        return requiredScopesString
    }

    protected override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
        if (activity is MainActivity) {
            this.activity = activity
        }
    }

    override fun onActivityStarted(activity: Activity) {
        if (activity is MainActivity) {
            this.activity = activity
        }
    }

    override fun onActivityResumed(activity: Activity) {
        if (activity is MainActivity) {
            this.activity = activity
        }
    }

    fun getActivity(): MainActivity? {
        return activity
    }

    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
    fun getPathFolder(): String? {
        return pathFolder
    }

    fun setConnectivityListener(listener: QRScannerReceiver.ConnectivityReceiverListener?) {
        QRScannerReceiver.connectivityReceiverListener = listener
    }

    fun getUrl(): String? {
        if (!DEBUG || isLive) {
            url = getString(R.string.url_live)
        } else {
            url = getString(R.string.url_developer)
        }
        return url
    }

    @SuppressLint("HardwareIds")
    fun getDeviceId(): String? {
        return Settings.Secure.getString(applicationContext.contentResolver, Settings.Secure.ANDROID_ID)
    }

    fun getManufacturer(): String? {
        return Build.MANUFACTURER
    }

    fun getModel(): String? {
        return Build.MODEL
    }

    fun getVersion(): Int {
        return Build.VERSION.SDK_INT
    }

    fun getVersionRelease(): String? {
        return Build.VERSION.RELEASE
    }

    fun getPackageId(): String {
        return BuildConfig.APPLICATION_ID
    }

    fun getStorage(): Storage {
        return storage
    }

    fun getAdsView(context: Context?): AdView? {
        Utils.Log(TAG, "show ads...")
        adView = AdView(context)
        adView?.adSize = AdSize.SMART_BANNER
        if (Utils.isFreeRelease()) {
            if (Utils.isDebug()) {
                Utils.Log(TAG, "show ads isDebug...")
                adView?.adUnitId = getString(R.string.banner_home_footer_test)
            } else {
                adView?.adUnitId = getString(R.string.banner_footer)
            }
        } else {
            adView?.adUnitId = getString(R.string.banner_home_footer_test)
        }
        val adRequest = AdRequest.Builder().build()
        adView?.adListener = object : AdListener() {
            override fun onAdLoaded() {
                isRequestAds = false
                Utils.Log(TAG, "Ads successful")
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError?) {
                super.onAdFailedToLoad(loadAdError)
                isRequestAds = true
                Utils.Log(TAG, "Ads failed")
            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen
            }

            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }
        }
        adView?.loadAd(adRequest)
        return adView
    }

    fun getAdsLargeView(context: Context?): AdView? {
        Utils.Log(TAG, "show ads...")
        adLargeView = AdView(context)
        adLargeView?.adSize = AdSize.MEDIUM_RECTANGLE
        if (Utils.isFreeRelease()) {
            if (Utils.isDebug()) {
                Utils.Log(TAG, "show ads isDebug...")
                adLargeView?.adUnitId = getString(R.string.banner_home_footer_test)
            } else {
                adLargeView?.adUnitId = getString(R.string.banner_review)
            }
        } else {
            adLargeView?.adUnitId = getString(R.string.banner_home_footer_test)
        }
        val adRequest = AdRequest.Builder().build()
        adLargeView?.adListener = object : AdListener() {
            override fun onAdLoaded() {
                isRequestLargeAds = false
                Utils.Log(TAG, "Ads successful")
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError?) {
                super.onAdFailedToLoad(loadAdError)
                isRequestLargeAds = true
                Utils.Log(TAG, "Ads failed")
            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen
            }

            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }
        }
        adLargeView?.loadAd(adRequest)
        return adLargeView
    }

    fun getAudienceAdsLargeView(context: Context?): com.facebook.ads.AdView? {
        adAudienceLargeView?.destroy()
        adAudienceLargeView = null
        adAudienceLargeView = com.facebook.ads.AdView(context, getString(R.string.banner_audience_review), com.facebook.ads.AdSize.RECTANGLE_HEIGHT_250)
        // Initiate a request to load an ad.
        if (DEBUG){
            AdSettings.addTestDevice("63434fe6-a4de-4b70-aac4-97852c36074e")
        }
        val mBuild = adAudienceLargeView?.buildLoadAdConfig()?.withAdListener(object : com.facebook.ads.AdListener {
            override fun onError(p0: Ad?, p1: AdError?) {
                isRequestAudienceAds = true
                Utils.Log(TAG,"Audience error...")
            }
            override fun onAdClicked(p0: Ad?) {
            }
            override fun onLoggingImpression(p0: Ad?) {
            }
            override fun onAdLoaded(p0: Ad?) {
                Utils.Log(TAG,"Audience loaded...")
                isRequestAudienceAds = false
            }
        })
        adAudienceLargeView?.loadAd(mBuild?.build())
        return  adAudienceLargeView
    }

    fun loadAd(layAd: LinearLayout?) {
        if (adView == null) {
            Utils.Log(TAG, "ads null")
            return
        }
        if (adView?.parent != null) {
            val tempVg: ViewGroup = adView?.parent as ViewGroup
            tempVg.removeView(adView)
        }
        layAd?.addView(adView)
    }

    fun loadLargeAd(layAd: LinearLayout?) {
        if (adLargeView == null) {
            Utils.Log(TAG, "ads null")
            return
        }
        if (adLargeView?.parent != null) {
            val tempVg: ViewGroup = adLargeView?.parent as ViewGroup
            tempVg.removeView(adLargeView)
        }
        layAd?.addView(adLargeView)
    }

    fun loadLargeAudienceAd(layAd: LinearLayout?){
        if (adAudienceLargeView == null) {
            Utils.Log(TAG, "ads null")
            return
        }
        if (adAudienceLargeView?.parent != null) {
            val tempVg: ViewGroup = adAudienceLargeView?.parent as ViewGroup
            tempVg.removeView(adAudienceLargeView)
        }
        layAd?.addView(adAudienceLargeView)
    }

    fun isRequestAds(): Boolean {
        return isRequestAds
    }

    fun isRequestLargeAds(): Boolean {
        return isRequestLargeAds
    }

    fun isRequestLargeAudienceAds(): Boolean {
        return  isRequestAudienceAds
    }

    fun isLiveMigration(): Boolean {
        if (!DEBUG){
            return true
        }
        return true
    }

    fun isLiveAds() : Boolean{
        return true
    }

    fun isEnableReviewAds() : Boolean {
        return false
    }

    fun isEnableReviewAudienceAds() : Boolean {
        return true
    }

    fun isEnableBannerAds() : Boolean {
        return  true
    }

    fun isDebugPremium(): Boolean {
        if (DEBUG){
            return false
        }
        return false
    }

    companion object {
        @Volatile
        private var mInstance: QRScannerApplication? = null
        var serverAPI: RootAPI? = null
        private var url: String? = null
        var serverDriveApi: RootAPI? = null
        private val TAG = QRScannerApplication::class.java.simpleName
        @Synchronized
        fun getInstance(): QRScannerApplication {
            return mInstance as QRScannerApplication
        }
    }
}