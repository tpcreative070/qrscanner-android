package tpcreative.co.qrscanner.common.services;
import android.app.Application;
import android.content.ContextWrapper;
import android.support.multidex.MultiDex;
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

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        MultiDex.install(getApplicationContext());
        storage = new Storage(getApplicationContext());
        pathFolder = storage.getExternalStorageDirectory()+"/Pictures/QRScanner";
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

    public String getPathFolder(){
        return pathFolder;
    }

    public static synchronized QRScannerApplication getInstance() {
        return mInstance;
    }
}

