package tpcreative.co.qrscanner.common.services;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.MobileAds;
import com.snatik.storage.Storage;
import io.fabric.sdk.android.Fabric;
import tpcreative.co.qrscanner.BuildConfig;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.controller.PrefsController;

/**
 *
 */

public class QRScannerApplication extends MultiDexApplication implements  MultiDexApplication.ActivityLifecycleCallbacks {

    private static QRScannerApplication mInstance;
    private String pathFolder;
    private Storage storage;
    private static final String TAG = QRScannerApplication.class.getSimpleName();

    /*Volley*/
    private RequestQueue mRequestQueue;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        if (!BuildConfig.BUILD_TYPE.equals(getResources().getString(R.string.release))){
            MobileAds.initialize(this, getString(R.string.admob_app_id));
        }

        mInstance = this;
        storage = new Storage(getApplicationContext());
        pathFolder = storage.getExternalStorageDirectory()+"/Pictures/QRScanner";
        storage.createDirectory(pathFolder);
        new PrefsController.Builder()
                .setContext(getApplicationContext())
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();

        boolean first_Running = PrefsController.getBoolean(getString(R.string.key_not_first_running),false);
        if (!first_Running){
            PrefsController.putBoolean(getString(R.string.key_not_first_running),true);
        }
        registerActivityLifecycleCallbacks(this);
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

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public String getPathFolder(){
        return pathFolder;
    }

    public static synchronized QRScannerApplication getInstance() {
        return mInstance;
    }

    public void setConnectivityListener(QRScannerReceiver.ConnectivityReceiverListener listener) {
        QRScannerReceiver.connectivityReceiverListener = listener;
    }
}

