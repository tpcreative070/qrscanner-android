package tpcreative.co.qrscanner.common.services;
import android.app.Application;
import android.support.multidex.MultiDex;

/**
 *
 */

public class QRScannerApplication extends Application {

    private static QRScannerApplication mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        MultiDex.install(getApplicationContext());
    }

    public static synchronized QRScannerApplication getInstance() {
        return mInstance;
    }
}

