package tpcreative.co.qrscanner.common.services;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdActivity;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.snatik.storage.Storage;
import java.util.HashMap;
import io.fabric.sdk.android.Fabric;
import tpcreative.co.qrscanner.BuildConfig;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.api.RootAPI;
import tpcreative.co.qrscanner.common.controller.PrefsController;
import tpcreative.co.qrscanner.common.controller.ServiceManager;
import tpcreative.co.qrscanner.common.network.Dependencies;
import tpcreative.co.qrscanner.common.view.AdsLoader;
import tpcreative.co.qrscanner.model.room.InstanceGenerator;
import tpcreative.co.qrscanner.ui.main.MainActivity;
/**
 *
 */

public class QRScannerApplication extends MultiDexApplication implements Dependencies.DependenciesListener, MultiDexApplication.ActivityLifecycleCallbacks {
    private static QRScannerApplication mInstance;
    private String pathFolder;
    private Storage storage;
    protected static Dependencies dependencies;
    public static RootAPI serverAPI;
    private static String url;
    private boolean isLive;
    private MainActivity activity;
    private AdActivity adActivity;
    private InterstitialAd mInterstitialAd;
    private QRScannerAdListener listener ;
    private static final String TAG = QRScannerApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        InstanceGenerator.getInstance(this);
        isLive = false;
        if (!BuildConfig.BUILD_TYPE.equals(getResources().getString(R.string.release))) {
            MobileAds.initialize(this, getString(R.string.admob_app_id));
        }
        ServiceManager.getInstance().setContext(this);
        mInstance = this;
        storage = new Storage(getApplicationContext());
        pathFolder = storage.getExternalStorageDirectory() + "/Pictures/QRScanner";
        storage.createDirectory(pathFolder);
        new PrefsController.Builder()
                .setContext(getApplicationContext())
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();
        boolean first_Running = PrefsController.getBoolean(getString(R.string.key_not_first_running), false);
        if (!first_Running) {
            PrefsController.putBoolean(getString(R.string.key_not_first_running), true);
        }
        registerActivityLifecycleCallbacks(this);
        /*Init own service api*/
        dependencies = Dependencies.getsInstance(getApplicationContext(), getUrl());
        dependencies.dependenciesListener(this);
        dependencies.init();
        serverAPI = (RootAPI) Dependencies.serverAPI;
        Utils.Log(TAG,"Start ads");
       // AdsLoader.getInstance().iniAds();
    }

    public void onInitInterstitialAds(){
        /*Lock here...*/
        if (BuildConfig.BUILD_TYPE.equals(getResources().getString(R.string.freedevelop))) {
            mInterstitialAd = new InterstitialAd(this);
            mInterstitialAd.setAdUnitId(getString(R.string.interstitial_full_screen_test));
            mInterstitialAd.loadAd(new AdRequest.Builder().build());
            mInterstitialAd.setAdListener(new AdListener() {
                public void onAdLoaded() {
                    super.onAdLoaded();
                    if(listener!=null){
                        listener.onAdLoaded();
                    }
                    Utils.Log(TAG,"onAdLoaded");
                }
                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                    if(listener!=null){
                        listener.onAdClosed();
                    }
                    Utils.Log(TAG,"onAdClosed");
                }
                @Override
                public void onAdFailedToLoad(int i) {
                    super.onAdFailedToLoad(i);
                    if(listener!=null){
                        listener.onAdFailedToLoad(i);
                    }
                    Utils.Log(TAG,"onAdFailedToLoad");
                }
                @Override
                public void onAdLeftApplication() {
                    super.onAdLeftApplication();
                    if(listener!=null){
                        listener.onAdLeftApplication();
                    }
                    Utils.Log(TAG,"onAdLeftApplication");
                }
                @Override
                public void onAdOpened() {
                    super.onAdOpened();
                    if(listener!=null){
                        listener.onAdOpened();
                    }
                    Utils.Log(TAG,"onAdOpened");
                }
                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                    if(listener!=null){
                        listener.onAdClicked();
                    }
                    Utils.Log(TAG,"onAdClicked");
                }
                @Override
                public void onAdImpression() {
                    super.onAdImpression();
                    if(listener!=null){
                        listener.onAdImpression();
                    }
                    Utils.Log(TAG,"onAdImpression");
                }
            });
        }
        else if (BuildConfig.BUILD_TYPE.equals(getResources().getString(R.string.freerelease))) {
            mInterstitialAd = new InterstitialAd(this);
            mInterstitialAd.setAdUnitId(getString(R.string.interstitial_full_screen));
            mInterstitialAd.loadAd(new AdRequest.Builder().build());
            mInterstitialAd.setAdListener(new AdListener() {
                public void onAdLoaded() {
                    super.onAdLoaded();
                    if(listener!=null){
                        listener.onAdLoaded();
                    }
                    Utils.Log(TAG,"onAdLoaded");
                }
                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                    if(listener!=null){
                        listener.onAdClosed();
                    }
                    Utils.Log(TAG,"onAdClosed");
                }
                @Override
                public void onAdFailedToLoad(int i) {
                    super.onAdFailedToLoad(i);
                    if(listener!=null){
                        listener.onAdFailedToLoad(i);
                    }
                    Utils.Log(TAG,"onAdFailedToLoad");
                }
                @Override
                public void onAdLeftApplication() {
                    super.onAdLeftApplication();
                    if(listener!=null){
                        listener.onAdLeftApplication();
                    }
                    Utils.Log(TAG,"onAdLeftApplication");
                }
                @Override
                public void onAdOpened() {
                    super.onAdOpened();
                    if(listener!=null){
                        listener.onAdOpened();
                    }
                    Utils.Log(TAG,"onAdOpened");
                }
                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                    if(listener!=null){
                        listener.onAdClicked();
                    }
                    Utils.Log(TAG,"onAdClicked");
                }
                @Override
                public void onAdImpression() {
                    super.onAdImpression();
                    if(listener!=null){
                        listener.onAdImpression();
                    }
                    Utils.Log(TAG,"onAdImpression");
                }
            });
        }
    }

    public void showInterstitial() {
        if (mInterstitialAd !=null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
            if (listener!=null){
                listener.onShowAds();
            }
            Utils.Log(TAG,"show ads");
        }
        else{
            if(listener!=null){
                listener.onCouldNotShow();
            }
            Utils.Log(TAG,"could not show");
        }
    }

    public void reloadAds(){
        if (mInterstitialAd==null){
            Utils.Log(TAG,"mInterstitialAd is null");
            onInitInterstitialAds();
            return;
        }
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(adRequest);
        Utils.Log(TAG,"reloadAds");
    }

    public void setListener(QRScannerAdListener listener) {
        this.listener = listener;
    }

    public interface QRScannerAdListener {
        void onAdClosed();
        void onAdFailedToLoad(int var1) ;
        void onAdLeftApplication() ;
        void onAdOpened() ;
        void onAdLoaded() ;
        void onAdClicked();
        void onAdImpression() ;
        void onCouldNotShow();
        void onShowAds();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        if (activity instanceof MainActivity){
            this.activity = (MainActivity) activity;
        }
        if (activity instanceof AdActivity){
            this.adActivity = (AdActivity) activity;
            Utils.Log(TAG,"Start Activity ads");
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (activity instanceof MainActivity){
            this.activity = (MainActivity) activity;
        }
        if (activity instanceof AdActivity){
            this.adActivity = (AdActivity) activity;
            Utils.Log(TAG,"Start Activity ads");
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (activity instanceof MainActivity){
            this.activity = (MainActivity) activity;
        }
        if (activity instanceof AdActivity){
            this.adActivity = (AdActivity) activity;
            Utils.Log(TAG,"onActivityResumed AdActivity");
        }
    }

    public MainActivity getActivity() {
        return activity;
    }

    public AdActivity getAdActivity() {
        return adActivity;
    }

    public InterstitialAd getmInterstitialAd() {
        return mInterstitialAd;
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    public String getPathFolder() {
        return pathFolder;
    }

    public static synchronized QRScannerApplication getInstance() {
        return mInstance;
    }

    public void setConnectivityListener(QRScannerReceiver.ConnectivityReceiverListener listener) {
        QRScannerReceiver.connectivityReceiverListener = listener;
    }

    @Override
    public Class onObject() {
        return RootAPI.class;
    }

    @Override
    public String onAuthorToken() {
        return null;
    }

    @Override
    public HashMap<String, String> onCustomHeader() {
        return null;
    }

    @Override
    public boolean isXML() {
        return false;
    }

    public String getUrl() {
        if (!BuildConfig.DEBUG || isLive) {
            url = getString(R.string.url_live);
        } else {
            url = getString(R.string.url_developer);
        }
        return url;
    }

    public String getDeviceId() {
        return Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public String getManufacturer() {
        String manufacturer = Build.MANUFACTURER;
        return manufacturer;
    }

    public String getModel() {
        String model = Build.MODEL;
        return model;
    }

    public int getVersion() {
        int version = Build.VERSION.SDK_INT;
        return version;
    }

    public String getVersionRelease() {
        String versionRelease = Build.VERSION.RELEASE;
        return versionRelease;
    }

    public void onDismissAds(){
        AdActivity adActivity = QRScannerApplication.getInstance().getAdActivity();
        if (adActivity!=null){
            adActivity.finish();
            Utils.Log(TAG,"Showing onDismissAds");
        }
    }
}

