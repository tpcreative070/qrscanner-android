package tpcreative.co.qrscanner.common.services;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.MobileAds;
import com.snatik.storage.Storage;

import java.util.HashMap;

import io.fabric.sdk.android.Fabric;
import tpcreative.co.qrscanner.BuildConfig;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.api.RootAPI;
import tpcreative.co.qrscanner.common.controller.PrefsController;
import tpcreative.co.qrscanner.common.controller.ServiceManager;
import tpcreative.co.qrscanner.common.network.Dependencies;
import tpcreative.co.qrscanner.model.Ads;
import tpcreative.co.qrscanner.model.Author;

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
    private static final String TAG = QRScannerApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        isLive = true;

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

        PrefsController.putString(getString(R.string.key_admob_app_id), getString(R.string.admob_app_id));
        PrefsController.putString(getString(R.string.key_banner_home_footer),getString(R.string.banner_home_footer));
        PrefsController.putString(getString(R.string.key_banner_review),getString(R.string.banner_review));
        PrefsController.putString(getString(R.string.key_banner_result),getString(R.string.banner_result));
        PrefsController.putString(getString(R.string.key_interstitial_full_screen),getString(R.string.interstitial_full_screen));


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


    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

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

    public void onUpdatedAds() {
        final Author author = Author.getInstance().getAuthorInfo();
        if (author != null) {
            if (author.version != null) {
                final Ads ads = author.version.ads;
                if (ads != null) {
                    String app_id = ads.admob_app_id;
                    if (app_id != null) {
                        if (!BuildConfig.BUILD_TYPE.equals(getResources().getString(R.string.release))) {
                            final String preference = PrefsController.getString(getString(R.string.key_admob_app_id), null);
                            if (preference!=null){
                                if (!app_id.equals(preference)) {
                                    MobileAds.initialize(this, app_id);
                                    PrefsController.putString(getString(R.string.key_admob_app_id),app_id);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}

