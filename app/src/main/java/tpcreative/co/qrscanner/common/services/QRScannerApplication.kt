package tpcreative.co.qrscanner.common.services

import android.content.Context
import android.provider.Settings
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.common.api.Scope
import com.snatik.storage.Storage
import tpcreative.co.qrscanner.BuildConfig
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.controller.ServiceManager
import tpcreative.co.qrscanner.common.network.Dependencies
import java.util.*

/**
 *
 */
class QRScannerApplication : MultiDexApplication(), DependenciesListener<Any?>, ActivityLifecycleCallbacks {
    private var pathFolder: String? = null
    private var storage: Storage? = null
    private var isLive = false
    private var activity: MainActivity? = null
    private var adView: AdView? = null
    private var adLargeView: AdView? = null
    private var isRequestAds = true
    private var isRequestLargeAds = true
    private val authorization: String? = null
    private var options: GoogleSignInOptions.Builder? = null
    private var requiredScopes: MutableSet<Scope?>? = null
    private var requiredScopesString: MutableList<String?>? = null
    override fun onCreate() {
        super.onCreate()
        mInstance = this
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        InstanceGenerator.Companion.getInstance(this)
        PrefsController.Builder()
                .setContext(getApplicationContext())
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build()
        isLive = true
        if (!Utils.isPremium() && Utils.isLiveAds()) {
            Utils.Log(TAG, "Start ads")
            MobileAds.initialize(this, object : OnInitializationCompleteListener {
                override fun onInitializationComplete(initializationStatus: InitializationStatus?) {}
            })
        }
        ServiceManager.Companion.getInstance().setContext(this)
        storage = Storage(getApplicationContext())
        pathFolder = storage.getExternalStorageDirectory() + "/Pictures/QRScanner"
        storage.createDirectory(pathFolder)
        val first_Running: Boolean = PrefsController.getBoolean(getString(R.string.key_not_first_running), false)
        if (!first_Running) {
            PrefsController.putBoolean(getString(R.string.key_not_first_running), true)
        }
        registerActivityLifecycleCallbacks(this)
        /*Init own service api*/dependencies = Dependencies.Companion.getsInstance(getApplicationContext(), getUrl())
        dependencies.dependenciesListener(this)
        dependencies.init()
        serverAPI = Dependencies.Companion.serverAPI as RootAPI
        serverDriveApi = RetrofitHelper().getCityService(RootAPI.Companion.ROOT_GOOGLE_DRIVE)
        options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestScopes(Scope(DriveScopes.DRIVE_FILE))
                .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
        requiredScopes = HashSet(2)
        requiredScopes.add(Scope(DriveScopes.DRIVE_FILE))
        requiredScopes.add(Scope(DriveScopes.DRIVE_APPDATA))
        requiredScopesString = ArrayList()
        requiredScopesString.add(DriveScopes.DRIVE_APPDATA)
        requiredScopesString.add(DriveScopes.DRIVE_FILE)
        ThemeHelper.applyTheme(EnumThemeMode.Companion.byPosition(Utils.getPositionTheme()))
    }

    fun getGoogleSignInOptions(account: Account?): GoogleSignInOptions? {
        if (options != null) {
            if (account != null) {
                options.setAccountName(account.name)
            }
            return options.build()
        }
        return options.build()
    }

    fun getRequiredScopesString(): MutableList<String?>? {
        return requiredScopesString
    }

    protected override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onActivityCreated(activity: Activity?, bundle: Bundle?) {
        if (activity is MainActivity) {
            this.activity = activity as MainActivity?
        }
    }

    override fun onActivityStarted(activity: Activity?) {
        if (activity is MainActivity) {
            this.activity = activity as MainActivity?
        }
    }

    override fun onActivityResumed(activity: Activity?) {
        if (activity is MainActivity) {
            this.activity = activity as MainActivity?
        }
    }

    fun getActivity(): MainActivity? {
        return activity
    }

    override fun onActivityPaused(activity: Activity?) {}
    override fun onActivityStopped(activity: Activity?) {}
    override fun onActivitySaveInstanceState(activity: Activity?, bundle: Bundle?) {}
    override fun onActivityDestroyed(activity: Activity?) {}
    fun getPathFolder(): String? {
        return pathFolder
    }

    fun setConnectivityListener(listener: ConnectivityReceiverListener?) {
        QRScannerReceiver.Companion.connectivityReceiverListener = listener
    }

    override fun onObject(): Class<*>? {
        return RootAPI::class.java
    }

    override fun onAuthorToken(): String? {
        return null
    }

    override fun onCustomHeader(): HashMap<String?, String?>? {
        return null
    }

    override fun isXML(): Boolean {
        return false
    }

    fun getUrl(): String? {
        if (!BuildConfig.DEBUG || isLive) {
            url = getString(R.string.url_live)
        } else {
            url = getString(R.string.url_developer)
        }
        return url
    }

    fun getDeviceId(): String? {
        return Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID)
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

    fun getPackageId(): String? {
        return BuildConfig.APPLICATION_ID
    }

    fun getStorage(): Storage? {
        return storage
    }

    fun getAdsView(context: Context?): AdView? {
        Utils.Log(TAG, "show ads...")
        adView = AdView(context)
        adView.setAdSize(AdSize.SMART_BANNER)
        if (Utils.isFreeRelease()) {
            if (Utils.isDebug()) {
                adView.setAdUnitId(getString(R.string.banner_home_footer_test))
            } else {
                adView.setAdUnitId(getString(R.string.banner_footer))
            }
        } else {
            adView.setAdUnitId(getString(R.string.banner_home_footer_test))
        }
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
        adView.setAdListener(object : AdListener() {
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
        })
        return adView
    }

    fun getAdsLargeView(context: Context?): AdView? {
        Utils.Log(TAG, "show ads...")
        adLargeView = AdView(context)
        adLargeView.setAdSize(AdSize.MEDIUM_RECTANGLE)
        if (Utils.isFreeRelease()) {
            if (Utils.isDebug()) {
                adLargeView.setAdUnitId(getString(R.string.banner_home_footer_test))
            } else {
                adLargeView.setAdUnitId(getString(R.string.banner_review))
            }
        } else {
            adLargeView.setAdUnitId(getString(R.string.banner_home_footer_test))
        }
        val adRequest = AdRequest.Builder().build()
        adLargeView.loadAd(adRequest)
        adLargeView.setAdListener(object : AdListener() {
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
        })
        return adLargeView
    }

    fun loadAd(layAd: LinearLayout?) {
        if (adView == null) {
            Utils.Log(TAG, "ads null")
            return
        }
        if (adView.getParent() != null) {
            val tempVg: ViewGroup = adView.getParent() as ViewGroup
            tempVg.removeView(adView)
        }
        layAd.addView(adView)
    }

    fun loadLargeAd(layAd: LinearLayout?) {
        if (adLargeView == null) {
            Utils.Log(TAG, "ads null")
            return
        }
        if (adLargeView.getParent() != null) {
            val tempVg: ViewGroup = adLargeView.getParent() as ViewGroup
            tempVg.removeView(adLargeView)
        }
        layAd.addView(adLargeView)
    }

    fun isRequestAds(): Boolean {
        return isRequestAds
    }

    fun isRequestLargeAds(): Boolean {
        return isRequestLargeAds
    }

    companion object {
        private var mInstance: QRScannerApplication? = null
        protected var dependencies: Dependencies<*>? = null
        var serverAPI: RootAPI? = null
        private var url: String? = null
        var serverDriveApi: RootAPI? = null
        private val TAG = QRScannerApplication::class.java.simpleName
        @Synchronized
        fun getInstance(): QRScannerApplication? {
            return mInstance
        }
    }
}