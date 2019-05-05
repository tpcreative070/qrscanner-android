package tpcreative.co.qrscanner.common.services;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import com.google.gson.Gson;
import com.snatik.storage.Storage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.HttpException;
import tpcreative.co.qrscanner.BuildConfig;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.SingletonResponse;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.controller.PrefsController;
import tpcreative.co.qrscanner.common.network.NetworkUtil;
import tpcreative.co.qrscanner.common.presenter.BaseView;
import tpcreative.co.qrscanner.common.presenter.PresenterService;
import tpcreative.co.qrscanner.model.Author;
import tpcreative.co.qrscanner.model.EnumStatus;

public class QRScannerService extends PresenterService<BaseView> implements QRScannerReceiver.ConnectivityReceiverListener {

    private static final String TAG = QRScannerService.class.getSimpleName();
    private final IBinder mBinder = new LocalBinder(); // Binder given to clients
    protected Storage storage;
    private Intent mIntent;
    private QRScannerReceiver androidReceiver;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        storage = new Storage(this);
        onInitReceiver();
        QRScannerApplication.getInstance().setConnectivityListener(this);
    }

    public Storage getStorage() {
        return storage;
    }

    public void onInitReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        androidReceiver = new QRScannerReceiver();
        registerReceiver(androidReceiver, intentFilter);
        QRScannerApplication.getInstance().setConnectivityListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if (androidReceiver != null) {
            unregisterReceiver(androidReceiver);
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        Utils.Log(TAG, "Connected :" + isConnected);
        BaseView view = view();
        if (view != null) {
            if (isConnected){
                view.onSuccessful("Connected network", EnumStatus.CONNECTED);
            }
            else{
                view.onSuccessful("Disconnected network",EnumStatus.DISCONNECTED);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // If we get killed, after returning from here, restart
        Log.d(TAG, "onStartCommand");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Bundle extras = intent.getExtras();
        Log.d(TAG, "onBind");
        // Get messager from the Activity
        if (extras != null) {
            Log.d("service", "onBind with extra");
        }
        return mBinder;
    }


    public void onSyncAuthor(){
        Log.d(TAG,"onSyncAuthor");
        if (BuildConfig.DEBUG){
            return;
        }
        BaseView view = view();
        if (view == null) {
            Utils.Log(TAG,"Author view is null");
            return;
        }
        if (NetworkUtil.pingIpAddress(view.getContext())) {
            return;
        }
        if (subscriptions == null) {
            Utils.Log(TAG,"Author Subscriptions is null");
            return;
        }

        boolean isPay = false;

        if (BuildConfig.APPLICATION_ID.equals(getString(R.string.qrscanner_pro_release))){
            isPay = true;
        }

        Map<String,String> hash = new HashMap<>();
        hash.put(getString(R.string.key_device_id), QRScannerApplication.getInstance().getDeviceId());
        hash.put(getString(R.string.key_device_type),getString(R.string.device_type));
        hash.put(getString(R.string.key_manufacturer), QRScannerApplication.getInstance().getManufacturer());
        hash.put(getString(R.string.key_name_model), QRScannerApplication.getInstance().getModel());
        hash.put(getString(R.string.key_version_sync),""+ QRScannerApplication.getInstance().getVersion());
        hash.put(getString(R.string.key_versionRelease), QRScannerApplication.getInstance().getVersionRelease());
        hash.put(getString(R.string.key_appVersionRelease), BuildConfig.VERSION_NAME);
        hash.put(getString(R.string.key_pay),""+isPay);
        subscriptions.add(QRScannerApplication.serverAPI.onAuthor(hash)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.onStartLoading(EnumStatus.AUTHOR_SYNC))
                .subscribe(onResponse -> {
                    view.onStopLoading(EnumStatus.AUTHOR_SYNC);
                    Log.d(TAG, "Author body: " + new Gson().toJson(onResponse));
                }, throwable -> {
                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        try {
                            Log.d(TAG,"Author error" +bodys.string());
                            String msg = new Gson().toJson(bodys.string());
                            Log.d(TAG, msg);
                        } catch (IOException e) {
                            Log.d(TAG,"Author IOException" +e.getMessage());
                            e.printStackTrace();
                        }
                    } else {
                        Log.d(TAG, "Author Can not call" + throwable.getMessage());
                    }
                    view.onStopLoading(EnumStatus.AUTHOR_SYNC);
                }));
    }

    public void onCheckVersion(){
        Log.d(TAG,"onCheckVersion");
        BaseView view = view();
        if (view == null) {
            return;
        }
        if (NetworkUtil.pingIpAddress(view.getContext())) {
            return;
        }
        if (subscriptions == null) {
            return;
        }

        subscriptions.add(QRScannerApplication.serverAPI.onCheckVersion()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> view.onStartLoading(EnumStatus.CHECK_VERSION))
                .subscribe(onResponse -> {
                    if (onResponse!=null){
                        if (onResponse.version!=null){
                            view.onSuccessful("Successful",EnumStatus.CHECK_VERSION);
                            final Author author = Author.getInstance().getAuthorInfo();
                            author.version = onResponse.version;
                            PrefsController.putString(getString(R.string.key_author),new Gson().toJson(author) );
                            SingletonResponse.getInstance().onAlertLatestVersion();
                        }
                    }
                    view.onStopLoading(EnumStatus.CHECK_VERSION);
                    Log.d(TAG, "Body : " + new Gson().toJson(onResponse));
                }, throwable -> {
                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        try {
                            Log.d(TAG,"error" +bodys.string());
                            String msg = new Gson().toJson(bodys.string());
                            Log.d(TAG, msg);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.d(TAG, "Can not call" + throwable.getMessage());
                    }
                    view.onStopLoading(EnumStatus.CHECK_VERSION);
                }));
    }


    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */

    public class LocalBinder extends Binder {
        public QRScannerService getService() {
            // Return this instance of SignalRService so clients can call public methods
            return QRScannerService.this;
        }
        public void setIntent(Intent intent) {
            mIntent = intent;
        }
    }


}
