package tpcreative.co.qrscanner.common.services;
import android.app.Application;
import android.support.multidex.MultiDex;

import com.snatik.storage.Storage;

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
    }

    public String getPathFolder(){
        return pathFolder;
    }

    public static synchronized QRScannerApplication getInstance() {
        return mInstance;
    }
}

