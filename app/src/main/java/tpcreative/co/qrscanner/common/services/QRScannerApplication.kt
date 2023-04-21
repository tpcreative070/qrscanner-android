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
import android.util.DisplayMetrics
import android.view.Display
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.hardware.display.DisplayManagerCompat
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
import tpcreative.co.qrscanner.common.Configuration
import tpcreative.co.qrscanner.common.Navigator
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.api.RetrofitBuilder
import tpcreative.co.qrscanner.common.api.RootAPI
import tpcreative.co.qrscanner.common.controller.PrefsController
import tpcreative.co.qrscanner.common.controller.ServiceManager
import tpcreative.co.qrscanner.common.extension.*
import tpcreative.co.qrscanner.common.view.CircleImageView
import tpcreative.co.qrscanner.helper.ThemeHelper
import tpcreative.co.qrscanner.model.EnumScreens
import tpcreative.co.qrscanner.model.EnumThemeMode
import tpcreative.co.qrscanner.model.EnumTypeServices
import tpcreative.co.qrscanner.ui.main.MainActivity
import java.io.File
import java.util.*


class QRScannerApplication : MultiDexApplication(), Application.ActivityLifecycleCallbacks {
    private var qrPath: String? = null
    private var isLive = false
    private var activity: MainActivity? = null
    private var mWithAd : Int = 0
    private var mHeight : Int = 0
    private var mMaximumHeight : Int = 0
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
    private var allowRequestFailureCreateSmall  = 0
    private var allowRequestFailureCreateLarge  = 0
    private var allowRequestFailureReviewSmall  = 0
    private var allowRequestFailureReviewLarge  = 0
    private var allowRequestFailureResultSmall  = 0
    private var allowRequestFailureResultLarge  = 0
    private var allowRequestFailureHelpFeedbackSmall  = 0
    private var allowRequestFailureHelpFeedbackLarge  = 0
    private var allowRequestFailureChangeColorSmall  = 0
    private var allowRequestFailureChangeColorLarge  = 0
    private var allowRequestFailureBackupSmall  = 0
    private var allowRequestFailureBackupLarge  = 0
    private var allowRequestFailureViewCode  = 0
    private var allowRequestFailureAnywhere = 0
    override fun onCreate() {
        super.onCreate()
        mInstance = this
        isLive = true
        qrPath = getExternalFilesDir(null)?.absolutePath + "/QRScanner/"
        if (qrPath?.let { File(it).exists() } == true){
            Utils.Log(TAG,"Folder already created $qrPath")
        }else{
            Utils.Log(TAG,"Requesting create folder")
            qrPath?.createFolder()
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
        MobileAds.initialize(this) { }
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
        currentScreen(activity)
        if (Utils.onIsIntro()){
            if (Utils.getMillisecondsNewUser()<=0){
                Utils.setMillisecondsNewUser(System.currentTimeMillis())
            }
        }else{
            /*If current milliseconds less than two days show app immediately*/
            val mAfterTwoDays = Configuration.TWO_DAYS + Configuration.CURRENT_MILLISECONDS
            if (System.currentTimeMillis()>mAfterTwoDays){
                Utils.setMillisecondsNewUser(System.currentTimeMillis())
            }else{
                Utils.setMillisecondsNewUser(System.currentTimeMillis() + getInstance().getCurrentTimeUnit())
            }
        }
        if (DEBUG){
            Utils.Log(TAG,"Current milliseconds ${System.currentTimeMillis()}")
            val mAfterFourDays = Configuration.TWO_DAYS + Configuration.CURRENT_MILLISECONDS
            Utils.Log(TAG,"Four days $mAfterFourDays")
        }
        if (activity is MainActivity) {
            this.activity = activity
        }
        Utils.Log(TAG,"Call 123")
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
        return qrPath
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

    fun requestResultSmallView(context: Context){
        Utils.Log(TAG, "requestResultSmallView ads...")
        adResultSmallView = AdView(context)
        val mSize = adSize()
        adResultSmallView?.setAdSize(mSize)
        if (Utils.isDebug()) {
            Utils.Log(TAG, "show ads isDebug...")
            adResultSmallView?.adUnitId = getString(R.string.banner_home_footer_test)
        } else {
            if (Utils.isInnovation()){
                adResultSmallView?.adUnitId = getString(R.string.innovation_banner_result_small)
            }else{
                adResultSmallView?.adUnitId = getString(R.string.banner_result_small)
            }
        }
        val adRequest = AdRequest.Builder().build()
        adResultSmallView?.adListener = object : AdListener() {
            override fun onAdLoaded() {
                isResultSmallView = false
                Utils.Log(TAG, "Ads successful")
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                isResultSmallView = allowRequestFailureResultSmall <= Configuration.COUNT_ALLOW_REQUEST_FAILURE_EACH_AD_ID
                allowRequestFailureResultSmall+=1
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
        if (Utils.isDebug()) {
            Utils.Log(TAG, "show ads isDebug...")
            adResultLargeView?.adUnitId = getString(R.string.banner_home_footer_test)
        } else {
            if (Utils.isInnovation()){
                adResultLargeView?.adUnitId = getString(R.string.innovation_banner_result_large)
            }else{
                adResultLargeView?.adUnitId = getString(R.string.banner_result_large)
            }
        }
        val adRequest = AdRequest.Builder().build()
        adResultLargeView?.adListener = object : AdListener() {
            override fun onAdLoaded() {
                isResultLargeView = false
                Utils.Log(TAG, "Ads successful")
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                isResultLargeView = allowRequestFailureResultLarge <= Configuration.COUNT_ALLOW_REQUEST_FAILURE_EACH_AD_ID
                allowRequestFailureResultLarge+=1
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
        val mSize = adSize()
        adReviewSmallView?.setAdSize(mSize)
        if (Utils.isDebug()) {
            Utils.Log(TAG, "show ads isDebug...")
            adReviewSmallView?.adUnitId = getString(R.string.banner_home_footer_test)
        } else {
            if (Utils.isInnovation()){
                adReviewSmallView?.adUnitId = getString(R.string.innovation_banner_review_small)
            }else{
                adReviewSmallView?.adUnitId = getString(R.string.banner_review_small)
            }
        }
        val adRequest = AdRequest.Builder().build()
        adReviewSmallView?.adListener = object : AdListener() {
            override fun onAdLoaded() {
                isReviewSmallView = false
                Utils.Log(TAG, "Ads successful")
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                isReviewSmallView = allowRequestFailureReviewSmall <= Configuration.COUNT_ALLOW_REQUEST_FAILURE_EACH_AD_ID
                allowRequestFailureReviewSmall+=1
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


    fun requestReviewLargeView(context: Context){
        Utils.Log(TAG, "requestReviewLargeView ads...")
        adReviewLargeView = AdView(context)
        adReviewLargeView?.setAdSize(AdSize.MEDIUM_RECTANGLE)
        if (Utils.isDebug()) {
            Utils.Log(TAG, "show ads isDebug...")
            adReviewLargeView?.adUnitId = getString(R.string.banner_home_footer_test)
        } else {
            if (Utils.isInnovation()){
                adReviewLargeView?.adUnitId = getString(R.string.innovation_banner_review_large)
            }else{
                adReviewLargeView?.adUnitId = getString(R.string.banner_review_large)
            }
        }
        val adRequest = AdRequest.Builder().build()
        adReviewLargeView?.adListener = object : AdListener() {
            override fun onAdLoaded() {
                isReviewLargeView = false
                Utils.Log(TAG, "Ads successful")
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                isReviewLargeView = allowRequestFailureReviewLarge <= Configuration.COUNT_ALLOW_REQUEST_FAILURE_EACH_AD_ID
                allowRequestFailureReviewLarge+=1
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


    fun requestHelpFeedbackSmallView(context: Context){
        Utils.Log(TAG, "requestReviewSmallView ads...")
        adHelpFeedbackSmallView = AdView(context)
        val mSize = adSize()
        adHelpFeedbackSmallView?.setAdSize(mSize)
        if (Utils.isDebug()) {
            Utils.Log(TAG, "show ads isDebug...")
            adHelpFeedbackSmallView?.adUnitId = getString(R.string.banner_home_footer_test)
        } else {
            if (Utils.isInnovation()){
                adHelpFeedbackSmallView?.adUnitId = getString(R.string.innovation_banner_help_feedback_small)
            }else{
                adHelpFeedbackSmallView?.adUnitId = getString(R.string.banner_help_feedback_small)
            }
        }
        val adRequest = AdRequest.Builder().build()
        adHelpFeedbackSmallView?.adListener = object : AdListener() {
            override fun onAdLoaded() {
                isHelpFeedbackSmallView = false
                Utils.Log(TAG, "Ads successful")
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                isHelpFeedbackSmallView = allowRequestFailureHelpFeedbackSmall <= Configuration.COUNT_ALLOW_REQUEST_FAILURE_EACH_AD_ID
                allowRequestFailureHelpFeedbackSmall+=1
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
        if (Utils.isDebug()) {
            Utils.Log(TAG, "show ads isDebug...")
            adHelpFeedbackLargeView?.adUnitId = getString(R.string.banner_home_footer_test)
        } else {
            if (Utils.isInnovation()){
                adHelpFeedbackLargeView?.adUnitId = getString(R.string.innovation_banner_help_feedback_large)
            }else{
                adHelpFeedbackLargeView?.adUnitId = getString(R.string.banner_help_feedback_large)
            }
        }
        val adRequest = AdRequest.Builder().build()
        adHelpFeedbackLargeView?.adListener = object : AdListener() {
            override fun onAdLoaded() {
                isHelpFeedbackLargeView = false
                Utils.Log(TAG, "Ads successful")
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                isHelpFeedbackLargeView = allowRequestFailureHelpFeedbackLarge <= Configuration.COUNT_ALLOW_REQUEST_FAILURE_EACH_AD_ID
                allowRequestFailureHelpFeedbackLarge+=1
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
        val mSize = adSize()
        adChangeColorSmallView?.setAdSize(mSize)
        if (Utils.isDebug()) {
            Utils.Log(TAG, "show ads isDebug...")
            adChangeColorSmallView?.adUnitId = getString(R.string.banner_home_footer_test)
        } else {
            if (Utils.isInnovation()){
                adChangeColorSmallView?.adUnitId = getString(R.string.innovation_banner_change_color_small)
            }else{
                adChangeColorSmallView?.adUnitId = getString(R.string.banner_change_color_small)
            }
        }
        val adRequest = AdRequest.Builder().build()
        adChangeColorSmallView?.adListener = object : AdListener() {
            override fun onAdLoaded() {
                isChangeColorSmallView = false
                Utils.Log(TAG, "Ads successful")
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                isChangeColorSmallView = allowRequestFailureChangeColorSmall <= Configuration.COUNT_ALLOW_REQUEST_FAILURE_EACH_AD_ID
                allowRequestFailureChangeColorSmall+=1
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
        if (Utils.isDebug()) {
            Utils.Log(TAG, "show ads isDebug...")
            adChangeColorLargeView?.adUnitId = getString(R.string.banner_home_footer_test)
        } else {
            if (Utils.isInnovation()){
                adChangeColorLargeView?.adUnitId = getString(R.string.innovation_banner_change_color_large)
            }else{
                adChangeColorLargeView?.adUnitId = getString(R.string.banner_change_color_large)
            }
        }
        val adRequest = AdRequest.Builder().build()
        adChangeColorLargeView?.adListener = object : AdListener() {
            override fun onAdLoaded() {
                isChangeColorLargeView = false
                Utils.Log(TAG, "Ads successful")
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                isChangeColorLargeView = allowRequestFailureChangeColorLarge <= Configuration.COUNT_ALLOW_REQUEST_FAILURE_EACH_AD_ID
                allowRequestFailureChangeColorLarge+=1
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
        val mSize = adSize()
        adBackupSmallView?.setAdSize(mSize)
        if (Utils.isDebug()) {
            Utils.Log(TAG, "show ads isDebug...")
            adBackupSmallView?.adUnitId = getString(R.string.banner_home_footer_test)
        } else {
            if (Utils.isInnovation()){
                adBackupSmallView?.adUnitId = getString(R.string.innovation_banner_backup_small)
            }else{
                adBackupSmallView?.adUnitId = getString(R.string.banner_backup_small)
            }
        }
        val adRequest = AdRequest.Builder().build()
        adBackupSmallView?.adListener = object : AdListener() {
            override fun onAdLoaded() {
                isBackupSmallView = false
                Utils.Log(TAG, "Ads successful")
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                isBackupSmallView = allowRequestFailureBackupSmall <= Configuration.COUNT_ALLOW_REQUEST_FAILURE_EACH_AD_ID
                allowRequestFailureBackupSmall+=1
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
        if (Utils.isDebug()) {
            Utils.Log(TAG, "show ads isDebug...")
            adBackupLargeView?.adUnitId = getString(R.string.banner_home_footer_test)
        } else {
            if (Utils.isInnovation()){
                adBackupLargeView?.adUnitId = getString(R.string.innovation_banner_backup_large)
            }else{
                adBackupLargeView?.adUnitId = getString(R.string.banner_backup_large)
            }
        }
        val adRequest = AdRequest.Builder().build()
        adBackupLargeView?.adListener = object : AdListener() {
            override fun onAdLoaded() {
                isBackupLargeView = false
                Utils.Log(TAG, "Ads successful")
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                isBackupLargeView = allowRequestFailureBackupLarge <= Configuration.COUNT_ALLOW_REQUEST_FAILURE_EACH_AD_ID
                allowRequestFailureBackupLarge+=1
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

    fun requestCreateSmallView(context: Context){
        Utils.Log(TAG, "requestCreateSmallView ads...")
        adCreateSmallView = AdView(context)
        val mSize = adSize()
        adCreateSmallView?.setAdSize(mSize)
        if (Utils.isDebug()) {
            Utils.Log(TAG, "show ads isDebug...")
            adCreateSmallView?.adUnitId = getString(R.string.banner_home_footer_test)
        } else {
            if (Utils.isInnovation()){
                adCreateSmallView?.adUnitId = getString(R.string.innovation_banner_create_small)
            }else{
                adCreateSmallView?.adUnitId = getString(R.string.banner_create_small)
            }
        }
        val adRequest = AdRequest.Builder().build()
        adCreateSmallView?.adListener = object : AdListener() {
            override fun onAdLoaded() {
                isCreateSmallView = false
                Utils.Log(TAG, "Ads successful")
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                isCreateSmallView = allowRequestFailureCreateSmall <= Configuration.COUNT_ALLOW_REQUEST_FAILURE_EACH_AD_ID
                allowRequestFailureCreateSmall+=1
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
        if (Utils.isDebug()) {
            Utils.Log(TAG, "show ads isDebug...")
            adCreateLargeView?.adUnitId = getString(R.string.banner_home_footer_test)
        } else {
            if (Utils.isInnovation()){
                adCreateLargeView?.adUnitId = getString(R.string.innovation_banner_create_large)
            }else{
                adCreateLargeView?.adUnitId = getString(R.string.banner_create_large)
            }
        }
        val adRequest = AdRequest.Builder().build()
        adCreateLargeView?.adListener = object : AdListener() {
            override fun onAdLoaded() {
                isCreateLargeView = false
                Utils.Log(TAG, "Ads successful")
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                isCreateLargeView = allowRequestFailureCreateLarge <= Configuration.COUNT_ALLOW_REQUEST_FAILURE_EACH_AD_ID
                allowRequestFailureCreateLarge+=1
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
        val id: String
        id = if (Utils.isDebug()) {
            Utils.Log(TAG, "show ads isDebug...")
            getString(R.string.interstitial_test)
        } else {
            if (Utils.isInnovation()){
                getString(R.string.innovation_interstitial_anywhere)
            }else{
                getString(R.string.interstitial_anywhere)
            }
        }
        InterstitialAd.load(this,id, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                mInterstitialAd = null
                isRequestInterstitialAd = allowRequestFailureAnywhere <= Configuration.COUNT_ALLOW_REQUEST_FAILURE_EACH_AD_ID
                allowRequestFailureAnywhere+=1
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
                        context.finish()
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
        val id: String
        id = if (Utils.isDebug()) {
            Utils.Log(TAG, "show ads isDebug...")
            getString(R.string.interstitial_test)
        } else {
            if (Utils.isInnovation()){
                getString(R.string.innovation_interstitial_view_code)
            }else{
                getString(R.string.interstitial_view_code)
            }
        }
        InterstitialAd.load(this,id, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                mInterstitialViewCodeAd = null
                isRequestInterstitialViewCodeAd = allowRequestFailureViewCode <= Configuration.COUNT_ALLOW_REQUEST_FAILURE_EACH_AD_ID
                allowRequestFailureViewCode+=1
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
                        context.finish()
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

    fun loadResultSmallView(layAd: LinearLayout?) {
        if (!isOnline() || Utils.isRequestShowLocalAds()){
           layAd?.layout(R.layout.layout_content_small_offline).apply {
                layAd?.addView(this)
                this?.findViewById<CircleImageView?>(R.id.imgCircleAds).apply {
                    this?.setImageResource(R.color.colorAccent)
                }
                this?.findViewById<RelativeLayout>(R.id.rlNext).apply {
                   this?.setOnClickListener {
                       Navigator.onFromAdsMoveToProVersion(applicationContext)
                   }
               }
            }
            return
        }
        if (adResultSmallView == null) {
            Utils.Log(TAG, "ads null")
            return
        }
        if (adResultSmallView?.parent != null) {
            val tempVg: ViewGroup = adResultSmallView?.parent as ViewGroup
            tempVg.removeView(adResultSmallView)
        }
        Utils.Log(TAG,"Result small ${adResultSmallView?.height}")
        layAd?.addView(adResultSmallView)
    }

    fun loadResultLargeView(layAd: LinearLayout?) {
        if (!isOnline() || Utils.isRequestShowLocalAds()){
            layAd?.layout(R.layout.layout_content_large_offline).apply {
                layAd?.addView(this)
                this?.findViewById<LinearLayout>(R.id.rlProVersion).apply {
                    this?.setOnClickListener {
                        Navigator.onFromAdsMoveToProVersion(applicationContext)
                    }
                }
            }
            return
        }
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
        if (!isOnline() || Utils.isRequestShowLocalAds()){
           layAd?.layout(R.layout.layout_content_small_offline).apply {
                layAd?.addView(this)
                this?.findViewById<CircleImageView?>(R.id.imgCircleAds).apply {
                    this?.setImageResource(R.color.colorAccent)
                }
                this?.findViewById<RelativeLayout>(R.id.rlNext).apply {
                    this?.setOnClickListener {
                        Navigator.onFromAdsMoveToProVersion(applicationContext)
                    }
                }
            }
            return
        }
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
        if (!isOnline() || Utils.isRequestShowLocalAds()){
            layAd?.layout(R.layout.layout_content_large_offline).apply {
                layAd?.addView(this)
                this?.findViewById<LinearLayout>(R.id.rlProVersion).apply {
                    this?.setOnClickListener {
                        Navigator.onFromAdsMoveToProVersion(applicationContext)
                    }
                }
            }
            return
        }
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
        if (!isOnline() || Utils.isRequestShowLocalAds()){
            layAd?.layout(R.layout.layout_content_small_offline).apply {
                layAd?.addView(this)
                this?.findViewById<CircleImageView?>(R.id.imgCircleAds).apply {
                    this?.setImageResource(R.color.colorAccent)
                }
                this?.findViewById<RelativeLayout>(R.id.rlNext).apply {
                    this?.setOnClickListener {
                        Navigator.onFromAdsMoveToProVersion(applicationContext)
                    }
                }
            }
            return
        }
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
        if (!isOnline() || Utils.isRequestShowLocalAds()){
            layAd?.layout(R.layout.layout_content_large_offline).apply {
                layAd?.addView(this)
                this?.findViewById<LinearLayout>(R.id.rlProVersion).apply {
                    this?.setOnClickListener {
                        Navigator.onFromAdsMoveToProVersion(applicationContext)
                    }
                }
            }
            return
        }
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
        if (!isOnline() || Utils.isRequestShowLocalAds()){
           layAd?.layout(R.layout.layout_content_small_offline).apply {
                layAd?.addView(this)
                this?.findViewById<CircleImageView?>(R.id.imgCircleAds).apply {
                    this?.setImageResource(R.color.colorAccent)
                }
                this?.findViewById<RelativeLayout>(R.id.rlNext).apply {
                    this?.setOnClickListener {
                        Navigator.onFromAdsMoveToProVersion(applicationContext)
                    }
                }
            }
            return
        }
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
        if (!isOnline() || Utils.isRequestShowLocalAds()){
            layAd?.layout(R.layout.layout_content_large_offline).apply {
                layAd?.addView(this)
                this?.findViewById<LinearLayout>(R.id.rlProVersion).apply {
                    this?.setOnClickListener {
                        Navigator.onFromAdsMoveToProVersion(applicationContext)
                    }
                }
            }
            return
        }
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
        if (!isOnline() || Utils.isRequestShowLocalAds()){
           layAd?.layout(R.layout.layout_content_small_offline).apply {
                layAd?.addView(this)
                this?.findViewById<CircleImageView?>(R.id.imgCircleAds).apply {
                    this?.setImageResource(R.color.colorAccent)
                }
                this?.findViewById<RelativeLayout>(R.id.rlNext).apply {
                    this?.setOnClickListener {
                        Navigator.onFromAdsMoveToProVersion(applicationContext)
                    }
                }
            }
            return
        }
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
        if (!isOnline() || Utils.isRequestShowLocalAds()){
                layAd?.layout(R.layout.layout_content_large_offline).apply {
                layAd?.addView(this)
                this?.findViewById<LinearLayout>(R.id.rlProVersion).apply {
                    this?.setOnClickListener {
                        Navigator.onFromAdsMoveToProVersion(applicationContext)
                    }
                }
            }
            return
        }
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
        if (!isOnline() || Utils.isRequestShowLocalAds()){
            layAd?.layout(R.layout.layout_content_small_offline).apply {
                layAd?.addView(this)
                this?.findViewById<CircleImageView?>(R.id.imgCircleAds).apply {
                    this?.setImageResource(R.color.colorAccent)
                }
                this?.findViewById<RelativeLayout>(R.id.rlNext).apply {
                    this?.setOnClickListener {
                        Navigator.onFromAdsMoveToProVersion(applicationContext)
                    }
                }
            }

            return
        }
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
        if (!isOnline() || Utils.isRequestShowLocalAds()){
            layAd?.layout(R.layout.layout_content_large_offline).apply {
                layAd?.addView(this)
                this?.findViewById<LinearLayout>(R.id.rlProVersion).apply {
                    this?.setOnClickListener {
                        Navigator.onFromAdsMoveToProVersion(applicationContext)
                    }
                }
            }
            return
        }
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

    private fun isLiveAfterExpiredTime(): Boolean{
        val mCurrentMillisecond = System.currentTimeMillis()
        val latestDateTimeUpdatedApp = Utils.getMillisecondsUpdatedApp()
        val mCurrentTimeUnit = getCurrentTimeUnit()
        val mAfter4Day  = latestDateTimeUpdatedApp + mCurrentTimeUnit
        Utils.Log(TAG,"CurrentTimeUnit $mCurrentTimeUnit")
        Utils.Log(TAG,"Current time $mCurrentMillisecond")
        return (mCurrentMillisecond>mAfter4Day) && latestDateTimeUpdatedApp>0
    }

    fun isLiveExpiredTimeForNewUser() : Boolean {
        val mResult = Utils.getMillisecondsNewUser()
        Utils.Log(TAG,"get Current time new Users $mResult")
        if (System.currentTimeMillis()>mResult){
            return true
        }
        return false
    }

    private fun getCurrentTimeUnit(): Long {
        return if(DEBUG){
            Configuration.THREE_MINUTES
        }else{
            Configuration.THREE_MINUTES
        }
    }

    fun isLiveAds() : Boolean{
        if (DEBUG){
            val isLiveAds  = isLiveAfterExpiredTime()
            val isLiveAdsForNewUser  = isLiveExpiredTimeForNewUser()
            Utils.Log(TAG,"Live app after expired $isLiveAds")
            Utils.Log(TAG,"Live app after expire for new user $isLiveAdsForNewUser")
        }
        return Configuration.liveAds
    }

    fun isEnableHelpFeedbackSmallView() : Boolean {
        return Configuration.enableHelpFeedbackSmallView
    }

    fun isEnableHelpFeedbackLargeView() : Boolean {
        return Configuration.enableHelpFeedbackLargeView
    }

    fun isEnableChangeColorSmallView() : Boolean {
        return Configuration.enableChangeColorSmallView
    }

    fun isEnableChangeColorLargeView() : Boolean {
        return Configuration.enableChangeColorLargeView
    }

    fun isEnableBackupSmallView() : Boolean {
        return Configuration.enableBackupSmallView
    }

    fun isEnableBackupLargeView() : Boolean {
        return Configuration.enableBackupLargeView
    }

    fun isEnableResultSmallView() : Boolean {
        return Configuration.enableResultSmallView
    }

    fun isEnableResultLargeView() : Boolean {
        return Configuration.enableResultLargeView
    }

    fun isEnableReviewSmallView() : Boolean {
        return Configuration.enableReviewSmallView
    }

    fun isEnableReviewLargeView() : Boolean {
        return Configuration.enableReviewLargeView
    }

    fun isEnableCreateSmallView() : Boolean {
        return  Configuration.enableCreateSmallView
    }

    fun isEnableCreateLargeView() : Boolean {
        return  Configuration.enableCreateLargeView
    }

    fun isEnableMainView() : Boolean {
        return  Configuration.enableMainView
    }

    fun isEnableInterstitialAd() : Boolean {
        return  Configuration.enableInterstitialAd
    }

    fun isEnableInterstitialViewCodeAd() : Boolean {
        return  Configuration.enableInterstitialViewCodeAd
    }

    fun isHiddenFreeReleaseAds() : Boolean{
        return Configuration.hiddenFreeReleaseAds
    }

    fun isHiddenFreeInnovationAds(): Boolean {
        return Configuration.hiddenFreeInnovationAds
    }

    fun isHiddenSuperFreeInnovationAds(): Boolean {
        return Configuration.hiddenSuperFreeInnovationAds
    }

    fun setRequestClearCacheData(data : Boolean){
        this.requestClearCacheData = data
    }

    fun isRequestClearCacheData() : Boolean{
        return requestClearCacheData
    }

    fun refreshAds(){
        /*Condition to refresh ads is past 20 minutes*/
        var mLatestTime = Utils.getKeepAdsRefreshLatestTime()
        val mCurrentTime = System.currentTimeMillis()
        mLatestTime += Configuration.TWENTY_MINUTES
        if (mCurrentTime>mLatestTime){
            onDestroyAllAds()
            Utils.setKeepAdsRefreshLatestTime(mCurrentTime)
            Utils.Log(TAG,"Force refresh ads")
        }else{
            Utils.Log(TAG,"Waiting for refresh ads $mLatestTime - $mCurrentTime")
        }
    }

    private fun currentScreen(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val defaultDisplay =
                DisplayManagerCompat.getInstance(this).getDisplay(Display.DEFAULT_DISPLAY)
            val displayContext = createDisplayContext(defaultDisplay!!)
            val outMetrics = displayContext.resources.displayMetrics
            val density = outMetrics.density
            val adWidthPixels = outMetrics.widthPixels.toFloat()
            val adWidth = (adWidthPixels / density).toInt()
            mWithAd = adWidth
            Utils.Log(TAG,"width $mWithAd")
            Utils.Log(TAG,"width ${mWithAd.px}")
            mMaximumHeight = if (Utils.isTablet()){
                105F.px
            }else{
               90F.px
            }
        } else {
            val outMetrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            activity.windowManager.defaultDisplay.getMetrics(outMetrics)
            val density = outMetrics.density
            val adWidthPixels = outMetrics.widthPixels.toFloat()
            val adWidth = (adWidthPixels / density).toInt()
            mWithAd = adWidth
            Utils.Log(TAG,"width $mWithAd")
            Utils.Log(TAG,"width ${mWithAd.px}")
            mMaximumHeight = if (Utils.isTablet()){
                105F.px
            }else{
                90F.px
            }
        }

//        val display = activity.windowManager.defaultDisplay
//        val outMetrics = DisplayMetrics()
//        display.getMetrics(outMetrics)
//
//        val density = outMetrics.density
//
//        val adWidthPixels = outMetrics.widthPixels.toFloat()
//        val adWidth = (adWidthPixels / density).toInt()
//        mWithAd = adWidth
//        Utils.Log(TAG,"width $mWithAd")
//        Utils.Log(TAG,"width ${pxToDp(mWithAd.toFloat())}")
//
//        mMaximumHeight = if (Utils.isTablet()){
//            this.pxToDp(105F).toInt()
//        }else{
//            this.pxToDp(90F).toInt()
//        }
    }

    fun getMaximumBannerHeight() : Int{
        Utils.Log(TAG,"Height value $mMaximumHeight")
        return mMaximumHeight
    }

    fun getWidth() : Int{
        return  mWithAd
    }

    private fun  adSize() : AdSize{
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, mWithAd)
    }

    fun onPauseAds(enum : EnumScreens){
//        when(enum){
//            EnumScreens.SCANNER_RESULT_SMALL ->{
//                adResultSmallView?.pause()
//            }
//            EnumScreens.SCANNER_RESULT_LARGE ->{
//                adResultLargeView?.pause()
//            }
//            EnumScreens.REVIEW_SMALL ->{
//                adReviewSmallView?.pause()
//            }
//            EnumScreens.REVIEW_LARGE ->{
//                adReviewLargeView?.pause()
//            }
//            EnumScreens.CREATE_SMALL ->{
//                adCreateSmallView?.pause()
//            }
//            EnumScreens.CREATE_LARGE ->{
//                adCreateLargeView?.pause()
//            }
//            EnumScreens.HELP_FEEDBACK_SMALL ->{
//                adHelpFeedbackSmallView?.pause()
//            }
//            EnumScreens.HELP_FEEDBACK_LARGE ->{
//                adHelpFeedbackLargeView?.pause()
//            }
//            EnumScreens.CHANGE_COLOR_SMALL ->{
//                adChangeColorSmallView?.pause()
//            }
//            EnumScreens.CHANGE_COLOR_LARGE ->{
//                adChangeColorLargeView?.pause()
//            }
//            EnumScreens.BACKUP_SMALL ->{
//                adBackupSmallView?.pause()
//            }
//            EnumScreens.BACKUP_LARGE ->{
//                adBackupLargeView?.pause()
//            }
//            else -> {}
//        }
    }

    fun onResumeAds(enum : EnumScreens){
//        when(enum){
//            EnumScreens.SCANNER_RESULT_SMALL ->{
//                adResultSmallView?.resume()
//            }
//            EnumScreens.SCANNER_RESULT_LARGE ->{
//                adResultLargeView?.resume()
//            }
//            EnumScreens.REVIEW_SMALL ->{
//                adReviewSmallView?.resume()
//            }
//            EnumScreens.REVIEW_LARGE ->{
//                adReviewLargeView?.resume()
//            }
//            EnumScreens.CREATE_SMALL ->{
//                adCreateSmallView?.resume()
//            }
//            EnumScreens.CREATE_LARGE ->{
//                adCreateLargeView?.resume()
//            }
//            EnumScreens.HELP_FEEDBACK_SMALL ->{
//                adHelpFeedbackSmallView?.resume()
//            }
//            EnumScreens.HELP_FEEDBACK_LARGE ->{
//                adHelpFeedbackLargeView?.resume()
//            }
//            EnumScreens.CHANGE_COLOR_SMALL ->{
//                adChangeColorSmallView?.resume()
//            }
//            EnumScreens.CHANGE_COLOR_LARGE ->{
//                adChangeColorLargeView?.resume()
//            }
//            EnumScreens.BACKUP_SMALL ->{
//                adBackupSmallView?.resume()
//            }
//            EnumScreens.BACKUP_LARGE ->{
//                adBackupLargeView?.resume()
//            }
//            else -> {}
//        }
    }

    fun onDestroyAllAds(){
//        adResultSmallView?.destroy()
//        adResultLargeView?.destroy()
//        adReviewSmallView?.destroy()
//        adReviewLargeView?.destroy()
//        adCreateSmallView?.destroy()
//        adCreateLargeView?.destroy()
//        adHelpFeedbackSmallView?.destroy()
//        adHelpFeedbackLargeView?.destroy()
//        adChangeColorSmallView?.destroy()
//        adChangeColorLargeView?.destroy()
//        adBackupSmallView?.destroy()
//        adBackupLargeView?.destroy()

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
        allowRequestFailureCreateSmall  = 0
        allowRequestFailureCreateLarge  = 0
        allowRequestFailureReviewSmall  = 0
        allowRequestFailureReviewLarge  = 0
        allowRequestFailureResultSmall  = 0
        allowRequestFailureResultLarge  = 0
        allowRequestFailureHelpFeedbackSmall  = 0
        allowRequestFailureHelpFeedbackLarge  = 0
        allowRequestFailureChangeColorSmall  = 0
        allowRequestFailureChangeColorLarge  = 0
        allowRequestFailureBackupSmall  = 0
        allowRequestFailureBackupLarge  = 0
        allowRequestFailureViewCode  = 0
        allowRequestFailureAnywhere  = 0
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