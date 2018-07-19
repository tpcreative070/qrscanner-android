package tpcreative.co.qrscanner.common.services;
import android.app.Application;
import android.content.ContextWrapper;
import android.support.multidex.MultiDex;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.snatik.storage.Storage;

import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.controller.PrefsController;

/**
 *
 */

public class QRScannerApplication extends Application {

    private static QRScannerApplication mInstance;
    private String pathFolder;
    private Storage storage;
    private static final String TAG = QRScannerApplication.class.getSimpleName();

    /*Volley*/
    private RequestQueue mRequestQueue;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        MultiDex.install(getApplicationContext());
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

