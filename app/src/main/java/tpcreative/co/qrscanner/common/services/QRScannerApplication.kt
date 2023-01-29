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
import androidx.appcompat.app.AppCompatActivity
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import com.google.firebase.crashlytics.FirebaseCrashlytics
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
    private var isLive = false
    private var activity: MainActivity? = null
    private var adMainView: AdView? = null
    private var adResultSmallView: AdView? = null
    private var adResultLargeView : AdView? = null
    private var adReviewSmallView : AdView? = null
    private var adReviewLargeView : AdView? = null
    private var adCreateSmallView : AdView? = null
    private var adCreateLargeView : AdView? = null
    private var adHelpFeedbackSmallView : AdView? = null
    private var adHelpFeedbackLargeView : AdView? = null
    private var adChangeColorSmallView : AdView? = null
    private var adChangeColorLargeView : AdView? = null
    private var adBackupSmallView : AdView? = null
    private var adBackupLargeView : AdView? = null
    private var mInterstitialAd: InterstitialAd? = null
    private var mInterstitialViewCodeAd: InterstitialAd? = null
    private var isRequestInterstitialAd : Boolean = true
    private var isRequestInterstitialViewCodeAd: Boolean = true
    private var isMainView = true
    private var isResultSmallView = true
    private var isResultLargeView = true
    private var isReviewSmallView = true
    private var isReviewLargeView = true
    private var isCreateSmallView = true
    private var isCreateLargeView = true
    private var isHelpFeedbackSmallView = true
    private var isHelpFeedbackLargeView = true
    private var isChangeColorSmallView = true
    private var isChangeColorLargeView = true
    private var isBackupSmallView = true
    private var isBackupLargeView = true
    private var requestClearCacheData = false
    private var options: GoogleSignInOptions.Builder? = null
    private var requiredScopes: MutableSet<Scope>? = null
    private var requiredScopesString: MutableList<String>? = null
    override fun onCreate() {
        super.onCreate()
        mInstance = this
        isLive = true
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        serverAPI = RetrofitBuilder.getService(typeService = EnumTypeServices.SYSTEM)
        serverDriveApi = RetrofitBuilder.getService(getString(R.string.url_google), typeService = EnumTypeServices.GOOGLE_DRIVE)
        PrefsController.Builder()
                .setContext(applicationContext)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(packageName)
                .setUseDefaultSharedPreference(true)
                .build()
        if (isLiveAds()) {
            Utils.Log(TAG, "Start ads")
            MobileAds.initialize(this) { }
        }
        ServiceManager.getInstance().setContext(this)
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

    fun requestMainView(context: Context){
        Utils.Log(TAG, "show ads...")
         adMainView = AdView(context)
        adMainView?.setAdSize(AdSize.BANNER)
        if (Utils.isFreeRelease()) {
            if (Utils.isDebug()) {
                Utils.Log(TAG, "show ads isDebug...")
                adMainView?.adUnitId = getString(R.string.banner_home_footer_test)
            } else {
                adMainView?.adUnitId = getString(R.string.banner_main)
            }
        } else {
            adMainView?.adUnitId = getString(R.string.banner_home_footer_test)
        }
        val adRequest = AdRequest.Builder().build()
        adMainView?.adListener = object : AdListener() {
            override fun onAdLoaded() {
                isMainView = false
                Utils.Log(TAG, "Ads successful")
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                isMainView = true
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
        adMainView?.loadAd(adRequest)
    }

    fun requestResultSmallView(context: Context){
        Utils.Log(TAG, "requestResultSmallView ads...")
        adResultSmallView = AdView(context)
        adResultSmallView?.setAdSize(AdSize.BANNER)
        if (Utils.isFreeRelease()) {
            if (Utils.isDebug()) {
                Utils.Log(TAG, "show ads isDebug...")
                adResultSmallView?.adUnitId = getString(R.string.banner_home_footer_test)
            } else {
                adResultSmallView?.adUnitId = getString(R.string.banner_result_small)
            }
        } else {
            adResultSmallView?.adUnitId = getString(R.string.banner_home_footer_test)
        }
        val adRequest = AdRequest.Builder().build()
        adResultSmallView?.adListener = object : AdListener() {
            override fun onAdLoaded() {
                isResultSmallView = false
                Utils.Log(TAG, "Ads successful")
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                isResultSmallView = true
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
        adResultSmallView?.loadAd(adRequest)
    }

    fun requestResultLargeView(context: Context){
        Utils.Log(TAG, "requestResultLargeView ads...")
        adResultLargeView = AdView(context)
        adResultLargeView?.setAdSize(AdSize.MEDIUM_RECTANGLE)
        if (Utils.isFreeRelease()) {
            if (Utils.isDebug()) {
                Utils.Log(TAG, "show ads isDebug...")
                adResultLargeView?.adUnitId = getString(R.string.banner_home_footer_test)
            } else {
                adResultLargeView?.adUnitId = getString(R.string.banner_result_large)
            }
        } else {
            adResultLargeView?.adUnitId = getString(R.string.banner_home_footer_test)
        }
        val adRequest = AdRequest.Builder().build()
        adResultLargeView?.adListener = object : AdListener() {
            override fun onAdLoaded() {
                isResultLargeView = false
                Utils.Log(TAG, "Ads successful")
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                isResultLargeView = true
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
        adResultLargeView?.loadAd(adRequest)
    }

    fun requestReviewSmallView(context: Context){
        Utils.Log(TAG, "requestReviewSmallView ads...")
        adReviewSmallView = AdView(context)
        adReviewSmallView?.setAdSize(AdSize.BANNER)
        if (Utils.isFreeRelease()) {
            if (Utils.isDebug()) {
                Utils.Log(TAG, "show ads isDebug...")
                adReviewSmallView?.adUnitId = getString(R.string.banner_home_footer_test)
            } else {
                adReviewSmallView?.adUnitId = getString(R.string.banner_review_small)
            }
        } else {
            adReviewSmallView?.adUnitId = getString(R.string.banner_home_footer_test)
        }
        val adRequest = AdRequest.Builder().build()
        adReviewSmallView?.adListener = object : AdListener() {
            override fun onAdLoaded() {
                isReviewSmallView = false
                Utils.Log(TAG, "Ads successful")
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                isReviewSmallView = true
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
        adReviewSmallView?.loadAd(adRequest)
    }

    fun requestHelpFeedbackSmallView(context: Context){
        Utils.Log(TAG, "requestReviewSmallView ads...")
        adHelpFeedbackSmallView = AdView(context)
        adHelpFeedbackSmallView?.setAdSize(AdSize.BANNER)
        if (Utils.isFreeRelease()) {
            if (Utils.isDebug()) {
                Utils.Log(TAG, "show ads isDebug...")
                adHelpFeedbackSmallView?.adUnitId = getString(R.string.banner_home_footer_test)
            } else {
                adHelpFeedbackSmallView?.adUnitId = getString(R.string.banner_help_feedback_small)
            }
        } else {
            adHelpFeedbackSmallView?.adUnitId = getString(R.string.banner_home_footer_test)
        }
        val adRequest = AdRequest.Builder().build()
        adHelpFeedbackSmallView?.adListener = object : AdListener() {
            override fun onAdLoaded() {
                isHelpFeedbackSmallView = false
                Utils.Log(TAG, "Ads successful")
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                isHelpFeedbackSmallView = true
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
        adHelpFeedbackSmallView?.loadAd(adRequest)
    }

    fun requestHelpFeedbackLargeView(context: Context){
        Utils.Log(TAG, "requestReviewSmallView ads...")
        adHelpFeedbackLargeView = AdView(context)
        adHelpFeedbackLargeView?.setAdSize(AdSize.MEDIUM_RECTANGLE)
        if (Utils.isFreeRelease()) {
            if (Utils.isDebug()) {
                Utils.Log(TAG, "show ads isDebug...")
                adHelpFeedbackLargeView?.adUnitId = getString(R.string.banner_home_footer_test)
            } else {
                adHelpFeedbackLargeView?.adUnitId = getString(R.string.banner_help_feedback_large)
            }
        } else {
            adHelpFeedbackLargeView?.adUnitId = getString(R.string.banner_home_footer_test)
        }
        val adRequest = AdRequest.Builder().build()
        adHelpFeedbackLargeView?.adListener = object : AdListener() {
            override fun onAdLoaded() {
                isHelpFeedbackLargeView = false
                Utils.Log(TAG, "Ads successful")
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                isHelpFeedbackLargeView = true
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
        adHelpFeedbackLargeView?.loadAd(adRequest)
    }


    fun requestChangeColorSmallView(context: Context){
        Utils.Log(TAG, "requestReviewSmallView ads...")
        adChangeColorSmallView = AdView(context)
        adChangeColorSmallView?.setAdSize(AdSize.BANNER)
        if (Utils.isFreeRelease()) {
            if (Utils.isDebug()) {
                Utils.Log(TAG, "show ads isDebug...")
                adChangeColorSmallView?.adUnitId = getString(R.string.banner_home_footer_test)
            } else {
                adChangeColorSmallView?.adUnitId = getString(R.string.banner_change_color_small)
            }
        } else {
            adChangeColorSmallView?.adUnitId = getString(R.string.banner_home_footer_test)
        }
        val adRequest = AdRequest.Builder().build()
        adChangeColorSmallView?.adListener = object : AdListener() {
            override fun onAdLoaded() {
                isChangeColorSmallView = false
                Utils.Log(TAG, "Ads successful")
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                isChangeColorSmallView = true
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
        adChangeColorSmallView?.loadAd(adRequest)
    }

    fun requestChangeColorLargeView(context: Context){
        Utils.Log(TAG, "requestReviewSmallView ads...")
        adChangeColorLargeView = AdView(context)
        adChangeColorLargeView?.setAdSize(AdSize.MEDIUM_RECTANGLE)
        if (Utils.isFreeRelease()) {
            if (Utils.isDebug()) {
                Utils.Log(TAG, "show ads isDebug...")
                adChangeColorLargeView?.adUnitId = getString(R.string.banner_home_footer_test)
            } else {
                adChangeColorLargeView?.adUnitId = getString(R.string.banner_change_color_large)
            }
        } else {
            adChangeColorLargeView?.adUnitId = getString(R.string.banner_home_footer_test)
        }
        val adRequest = AdRequest.Builder().build()
        adChangeColorLargeView?.adListener = object : AdListener() {
            override fun onAdLoaded() {
                isChangeColorLargeView = false
                Utils.Log(TAG, "Ads successful")
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                isChangeColorLargeView = true
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
        adChangeColorLargeView?.loadAd(adRequest)
    }


    fun requestBackupSmallView(context: Context){
        Utils.Log(TAG, "requestReviewSmallView ads...")
        adBackupSmallView = AdView(context)
        adBackupSmallView?.setAdSize(AdSize.BANNER)
        if (Utils.isFreeRelease()) {
            if (Utils.isDebug()) {
                Utils.Log(TAG, "show ads isDebug...")
                adBackupSmallView?.adUnitId = getString(R.string.banner_home_footer_test)
            } else {
                adBackupSmallView?.adUnitId = getString(R.string.banner_backup_small)
            }
        } else {
            adBackupSmallView?.adUnitId = getString(R.string.banner_home_footer_test)
        }
        val adRequest = AdRequest.Builder().build()
        adBackupSmallView?.adListener = object : AdListener() {
            override fun onAdLoaded() {
                isBackupSmallView = false
                Utils.Log(TAG, "Ads successful")
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                isBackupSmallView = true
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
        adBackupSmallView?.loadAd(adRequest)
    }

    fun requestBackupLargeView(context: Context){
        Utils.Log(TAG, "requestReviewSmallView ads...")
        adBackupLargeView = AdView(context)
        adBackupLargeView?.setAdSize(AdSize.MEDIUM_RECTANGLE)
        if (Utils.isFreeRelease()) {
            if (Utils.isDebug()) {
                Utils.Log(TAG, "show ads isDebug...")
                adBackupLargeView?.adUnitId = getString(R.string.banner_home_footer_test)
            } else {
                adBackupLargeView?.adUnitId = getString(R.string.banner_backup_large)
            }
        } else {
            adBackupLargeView?.adUnitId = getString(R.string.banner_home_footer_test)
        }
        val adRequest = AdRequest.Builder().build()
        adBackupLargeView?.adListener = object : AdListener() {
            override fun onAdLoaded() {
                isBackupLargeView = false
                Utils.Log(TAG, "Ads successful")
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                isBackupLargeView = true
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
        adBackupLargeView?.loadAd(adRequest)
    }



    fun requestReviewLargeView(context: Context){
        Utils.Log(TAG, "requestReviewLargeView ads...")
        adReviewLargeView = AdView(context)
        adReviewLargeView?.setAdSize(AdSize.MEDIUM_RECTANGLE)
        if (Utils.isFreeRelease()) {
            if (Utils.isDebug()) {
                Utils.Log(TAG, "show ads isDebug...")
                adReviewLargeView?.adUnitId = getString(R.string.banner_home_footer_test)
            } else {
                adReviewLargeView?.adUnitId = getString(R.string.banner_review_large)
            }
        } else {
            adReviewLargeView?.adUnitId = getString(R.string.banner_home_footer_test)
        }
        val adRequest = AdRequest.Builder().build()
        adReviewLargeView?.adListener = object : AdListener() {
            override fun onAdLoaded() {
                isReviewLargeView = false
                Utils.Log(TAG, "Ads successful")
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                isReviewLargeView = true
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
        adReviewLargeView?.loadAd(adRequest)
    }

    fun requestCreateSmallView(context: Context){
        Utils.Log(TAG, "requestCreateSmallView ads...")
        adCreateSmallView = AdView(context)
        adCreateSmallView?.setAdSize(AdSize.BANNER)
        if (Utils.isFreeRelease()) {
            if (Utils.isDebug()) {
                Utils.Log(TAG, "show ads isDebug...")
                adCreateSmallView?.adUnitId = getString(R.string.banner_home_footer_test)
            } else {
                adCreateSmallView?.adUnitId = getString(R.string.banner_create_small)
            }
        } else {
            adCreateSmallView?.adUnitId = getString(R.string.banner_home_footer_test)
        }
        val adRequest = AdRequest.Builder().build()
        adCreateSmallView?.adListener = object : AdListener() {
            override fun onAdLoaded() {
                isCreateSmallView = false
                Utils.Log(TAG, "Ads successful")
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                isCreateSmallView = true
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
        adCreateSmallView?.loadAd(adRequest)
    }

    fun requestCreateLargeView(context: Context){
        Utils.Log(TAG, "requestCreateLargeView ads...")
        adCreateLargeView = AdView(context)
        adCreateLargeView?.setAdSize(AdSize.MEDIUM_RECTANGLE)
        if (Utils.isFreeRelease()) {
            if (Utils.isDebug()) {
                Utils.Log(TAG, "show ads isDebug...")
                adCreateLargeView?.adUnitId = getString(R.string.banner_home_footer_test)
            } else {
                adCreateLargeView?.adUnitId = getString(R.string.banner_create_large)
            }
        } else {
            adCreateLargeView?.adUnitId = getString(R.string.banner_home_footer_test)
        }
        val adRequest = AdRequest.Builder().build()
        adCreateLargeView?.adListener = object : AdListener() {
            override fun onAdLoaded() {
                isCreateLargeView = false
                Utils.Log(TAG, "Ads successful")
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                isCreateLargeView = true
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
        adCreateLargeView?.loadAd(adRequest)
    }

    fun requestInterstitialAd(){
        Utils.Log(TAG, "Interstitial requesting...")
        val adRequest = AdRequest.Builder().build()
        var id = ""
        id = if (Utils.isFreeRelease()) {
            if (Utils.isDebug()) {
                Utils.Log(TAG, "show ads isDebug...")
                getString(R.string.interstitial_test)
            } else {
                getString(R.string.interstitial_anywhere)
            }
        } else {
            getString(R.string.interstitial_test)
        }
        InterstitialAd.load(this,id, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                mInterstitialAd = null
                isRequestInterstitialAd = true
                Utils.Log(TAG, "Interstitial was failed")
            }
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                mInterstitialAd = interstitialAd
                isRequestInterstitialAd = false
                Utils.Log(TAG, "Interstitial was loaded")
            }
        })
    }

    fun loadInterstitialAd(context: AppCompatActivity){
        if (mInterstitialAd != null) {
            mInterstitialAd?.fullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        Utils.Log(TAG, "Ad was dismissed.")
                        // Don't forget to set the ad reference to null so you
                        // don't show the ad a second time.
                        mInterstitialAd = null
                        isRequestInterstitialAd = true
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        Utils.Log(TAG, "Ad failed to show.")
                        // Don't forget to set the ad reference to null so you
                        // don't show the ad a second time.
                        mInterstitialAd = null
                        isRequestInterstitialAd = true
                    }

                    override fun onAdShowedFullScreenContent() {
                        mInterstitialAd = null
                        isRequestInterstitialAd = true
                        Utils.Log(TAG, "Ad showed fullscreen content.")
                        // Called when ad is dismissed.
                    }

                    override fun onAdClicked() {
                        mInterstitialAd = null
                        isRequestInterstitialAd = true
                    }
                }
            mInterstitialAd?.show(context)
            isRequestInterstitialAd = true
        }
    }

    fun requestInterstitialViewCodeAd(){
        Utils.Log(TAG, "Interstitial requesting...")
        val adRequest = AdRequest.Builder().build()
        var id = ""
        id = if (Utils.isFreeRelease()) {
            if (Utils.isDebug()) {
                Utils.Log(TAG, "show ads isDebug...")
                getString(R.string.interstitial_test)
            } else {
                getString(R.string.interstitial_view_code)
            }
        } else {
            getString(R.string.interstitial_test)
        }
        InterstitialAd.load(this,id, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                mInterstitialViewCodeAd = null
                isRequestInterstitialViewCodeAd = true
                Utils.Log(TAG, "Interstitial was failed")
            }
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                mInterstitialViewCodeAd = interstitialAd
                isRequestInterstitialViewCodeAd = false
                Utils.Log(TAG, "Interstitial was loaded")
            }
        })
    }

    fun loadInterstitialViewCodeAd(context: AppCompatActivity){
        if (mInterstitialViewCodeAd != null) {
            mInterstitialViewCodeAd?.fullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        Utils.Log(TAG, "Ad was dismissed.")
                        // Don't forget to set the ad reference to null so you
                        // don't show the ad a second time.
                        mInterstitialViewCodeAd = null
                        isRequestInterstitialViewCodeAd = true
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        Utils.Log(TAG, "Ad failed to show.")
                        // Don't forget to set the ad reference to null so you
                        // don't show the ad a second time.
                        mInterstitialViewCodeAd = null
                        isRequestInterstitialViewCodeAd = true
                    }

                    override fun onAdShowedFullScreenContent() {
                        mInterstitialViewCodeAd = null
                        isRequestInterstitialViewCodeAd = true
                        Utils.Log(TAG, "Ad showed fullscreen content.")
                        // Called when ad is dismissed.
                    }

                    override fun onAdClicked() {
                        mInterstitialViewCodeAd = null
                        isRequestInterstitialViewCodeAd = true
                    }
                }
            mInterstitialViewCodeAd?.show(context)
            isRequestInterstitialViewCodeAd = true
        }
    }

    fun loadMainView(layAd: LinearLayout?) {
        if (adMainView == null) {
            Utils.Log(TAG, "ads null")
            return
        }
        if (adMainView?.parent != null) {
            val tempVg: ViewGroup = adMainView?.parent as ViewGroup
            tempVg.removeView(adMainView)
        }
        layAd?.addView(adMainView)
    }

    fun loadResultSmallView(layAd: LinearLayout?) {
        if (adResultSmallView == null) {
            Utils.Log(TAG, "ads null")
            return
        }
        if (adResultSmallView?.parent != null) {
            val tempVg: ViewGroup = adResultSmallView?.parent as ViewGroup
            tempVg.removeView(adResultSmallView)
        }
        layAd?.addView(adResultSmallView)
    }

    fun loadResultLargeView(layAd: LinearLayout?) {
        if (adResultLargeView == null) {
            Utils.Log(TAG, "ads null")
            return
        }
        if (adResultLargeView?.parent != null) {
            val tempVg: ViewGroup = adResultLargeView?.parent as ViewGroup
            tempVg.removeView(adResultLargeView)
        }
        layAd?.addView(adResultLargeView)
    }


    fun loadReviewSmallView(layAd: LinearLayout?) {
        if (adReviewSmallView == null) {
            Utils.Log(TAG, "ads null")
            return
        }
        if (adReviewSmallView?.parent != null) {
            val tempVg: ViewGroup = adReviewSmallView?.parent as ViewGroup
            tempVg.removeView(adReviewSmallView)
        }
        layAd?.addView(adReviewSmallView)
    }

    fun loadReviewLargeView(layAd: LinearLayout?) {
        if (adReviewLargeView == null) {
            Utils.Log(TAG, "ads null")
            return
        }
        if (adReviewLargeView?.parent != null) {
            val tempVg: ViewGroup = adReviewLargeView?.parent as ViewGroup
            tempVg.removeView(adReviewLargeView)
        }
        layAd?.addView(adReviewLargeView)
    }

    fun loadCreateSmallView(layAd: LinearLayout?) {
        if (adCreateSmallView == null) {
            Utils.Log(TAG, "ads null")
            return
        }
        if (adCreateSmallView?.parent != null) {
            val tempVg: ViewGroup = adCreateSmallView?.parent as ViewGroup
            tempVg.removeView(adCreateSmallView)
        }
        layAd?.addView(adCreateSmallView)
    }

    fun loadCreateLargeView(layAd: LinearLayout?) {
        if (adCreateLargeView == null) {
            Utils.Log(TAG, "ads null")
            return
        }
        if (adCreateLargeView?.parent != null) {
            val tempVg: ViewGroup = adCreateLargeView?.parent as ViewGroup
            tempVg.removeView(adCreateLargeView)
        }
        layAd?.addView(adCreateLargeView)
    }

    fun loadHelpFeedbackSmallView(layAd: LinearLayout?) {
        if (adHelpFeedbackSmallView == null) {
            Utils.Log(TAG, "ads null")
            return
        }
        if (adHelpFeedbackSmallView?.parent != null) {
            val tempVg: ViewGroup = adHelpFeedbackSmallView?.parent as ViewGroup
            tempVg.removeView(adHelpFeedbackSmallView)
        }
        layAd?.addView(adHelpFeedbackSmallView)
    }

    fun loadHelpFeedbackLargeView(layAd: LinearLayout?) {
        if (adHelpFeedbackLargeView == null) {
            Utils.Log(TAG, "ads null")
            return
        }
        if (adHelpFeedbackLargeView?.parent != null) {
            val tempVg: ViewGroup = adHelpFeedbackLargeView?.parent as ViewGroup
            tempVg.removeView(adHelpFeedbackLargeView)
        }
        layAd?.addView(adHelpFeedbackLargeView)
    }

    fun loadChangeColorSmallView(layAd: LinearLayout?) {
        if (adChangeColorSmallView == null) {
            Utils.Log(TAG, "ads null")
            return
        }
        if (adChangeColorSmallView?.parent != null) {
            val tempVg: ViewGroup = adChangeColorSmallView?.parent as ViewGroup
            tempVg.removeView(adChangeColorSmallView)
        }
        layAd?.addView(adChangeColorSmallView)
    }

    fun loadChangeColorLargeView(layAd: LinearLayout?) {
        if (adChangeColorLargeView == null) {
            Utils.Log(TAG, "ads null")
            return
        }
        if (adChangeColorLargeView?.parent != null) {
            val tempVg: ViewGroup = adChangeColorLargeView?.parent as ViewGroup
            tempVg.removeView(adChangeColorLargeView)
        }
        layAd?.addView(adChangeColorLargeView)
    }

    fun loadBackupSmallView(layAd: LinearLayout?) {
        if (adBackupSmallView == null) {
            Utils.Log(TAG, "ads null")
            return
        }
        if (adBackupSmallView?.parent != null) {
            val tempVg: ViewGroup = adBackupSmallView?.parent as ViewGroup
            tempVg.removeView(adBackupSmallView)
        }
        layAd?.addView(adBackupSmallView)
    }

    fun loadBackupLargeView(layAd: LinearLayout?) {
        if (adBackupLargeView == null) {
            Utils.Log(TAG, "ads null")
            return
        }
        if (adBackupLargeView?.parent != null) {
            val tempVg: ViewGroup = adBackupLargeView?.parent as ViewGroup
            tempVg.removeView(adBackupLargeView)
        }
        layAd?.addView(adBackupLargeView)
    }

    fun isHelpFeedbackSmallView(): Boolean {
        return isHelpFeedbackSmallView
    }

    fun isHelpFeedbackLargeView(): Boolean {
        return isHelpFeedbackLargeView
    }


    fun isChangeColorSmallView(): Boolean {
        return isChangeColorSmallView
    }

    fun isChangeColorLargeView(): Boolean {
        return isChangeColorLargeView
    }

    fun isBackupSmallView(): Boolean {
        return isBackupSmallView
    }

    fun isBackupLargeView(): Boolean {
        return isBackupLargeView
    }

    fun isMainView(): Boolean {
        return isMainView
    }

    fun isResultSmallView(): Boolean {
        return isResultSmallView
    }

    fun isResultLargeView(): Boolean {
        return isResultLargeView
    }

    fun isReviewSmallView(): Boolean {
        return isReviewSmallView
    }

    fun isReviewLargeView(): Boolean {
        return isReviewLargeView
    }

    fun isCreateSmallView() : Boolean {
        return isCreateSmallView
    }

    fun isCreateLargeView() : Boolean {
        return isCreateLargeView
    }

    fun isRequestInterstitialAd() : Boolean {
        return isRequestInterstitialAd
    }

    fun isRequestInterstitialViewCodeAd() : Boolean {
        return isRequestInterstitialViewCodeAd
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

    fun isEnableHelpFeedbackSmallView() : Boolean {
        return true
    }

    fun isEnableHelpFeedbackLargeView() : Boolean {
        return true
    }

    fun isEnableChangeColorSmallView() : Boolean {
        return true
    }

    fun isEnableChangeColorLargeView() : Boolean {
        return true
    }

    fun isEnableBackupSmallView() : Boolean {
        return true
    }

    fun isEnableBackupLargeView() : Boolean {
        return true
    }

    fun isEnableResultSmallView() : Boolean {
        return true
    }

    fun isEnableResultLargeView() : Boolean {
        return true
    }

    fun isEnableReviewSmallView() : Boolean {
        return true
    }

    fun isEnableReviewLargeView() : Boolean {
        return true
    }

    fun isEnableCreateSmallView() : Boolean {
        return  true
    }

    fun isEnableCreateLargeView() : Boolean {
        return  true
    }

    fun isEnableMainView() : Boolean {
        return  false
    }

    fun isEnableInterstitialAd() : Boolean {
        return  true
    }

    fun isEnableInterstitialViewCodeAd() : Boolean {
        return  true
    }

    fun setRequestClearCacheData(data : Boolean){
        this.requestClearCacheData = data
    }

    fun isRequestClearCacheData() : Boolean{
        return requestClearCacheData
    }

    fun refreshAds(){
        isMainView = true
        isResultSmallView = true
        isResultLargeView = true
        isReviewSmallView = true
        isReviewLargeView = true
        isCreateSmallView = true
        isCreateLargeView = true
        isHelpFeedbackSmallView = true
        isHelpFeedbackLargeView = true
        isChangeColorSmallView = true
        isChangeColorLargeView = true
        isBackupSmallView = true
        isBackupLargeView = true
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