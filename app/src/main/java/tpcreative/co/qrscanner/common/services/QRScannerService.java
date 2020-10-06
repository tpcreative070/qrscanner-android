package tpcreative.co.qrscanner.common.services;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import com.google.gson.Gson;
import com.snatik.storage.Storage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;
import tpcreative.co.qrscanner.BuildConfig;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.SingletonResponse;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.api.response.DriveResponse;
import tpcreative.co.qrscanner.common.controller.PrefsController;
import tpcreative.co.qrscanner.common.network.NetworkUtil;
import tpcreative.co.qrscanner.common.presenter.BaseView;
import tpcreative.co.qrscanner.common.presenter.PresenterService;
import tpcreative.co.qrscanner.common.services.upload.ProgressRequestBody;
import tpcreative.co.qrscanner.helper.SQLiteHelper;
import tpcreative.co.qrscanner.model.Author;
import tpcreative.co.qrscanner.model.DriveAbout;
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
                            Utils.setAuthor(author);
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

    public void getDriveAbout(GoogleDriveListener view) {
        Utils.Log(TAG, "getDriveAbout");
        if (!Utils.isConnectedToGoogleDrive()) {
            view.onError("User is null",EnumStatus.REQUEST_ACCESS_TOKEN);
            return;
        }
        String access_token = Utils.getAccessToken();
        if (access_token == null) {
            view.onError("Access token is null",EnumStatus.REQUEST_ACCESS_TOKEN);
            return;
        }

        Utils.Log(TAG, "access_token : " + access_token);
        subscriptions.add(QRScannerApplication.serverDriveApi.onGetDriveAbout(access_token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> Utils.Log(TAG,""))
                .subscribe(onResponse -> {
                    if (onResponse.error != null) {
                        final Author mAuthor = Author.getInstance().getAuthorInfo();
                        if (mAuthor != null) {
                            mAuthor.isConnectedToGoogleDrive = false;
                            Utils.setAuthor(mAuthor);
                        }
                        view.onError(new Gson().toJson(onResponse.error), EnumStatus.REQUEST_ACCESS_TOKEN);
                    } else {
                        final Author mAuthor = Author.getInstance().getAuthorInfo();
                        if (mAuthor != null) {
                            mAuthor.isConnectedToGoogleDrive = true;
                            Utils.setAuthor(mAuthor);
                            view.onSuccessful("Successful",EnumStatus.GET_DRIVE_ABOUT_SUCCESSFULLY);
                            Utils.Log(TAG,new Gson().toJson(onResponse));
                        }
                    }
                }, throwable -> {
                    if (view == null) {
                        Utils.Log(TAG, "View is null");
                        return;
                    }
                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        try {
                            if (view==null){
                                return;
                            }
                            final String value = bodys.string();
                            final DriveAbout driveAbout = new Gson().fromJson(value, DriveAbout.class);
                            if (driveAbout != null) {
                                if (driveAbout.error != null) {
                                    final Author mAuthor = Author.getInstance().getAuthorInfo();
                                    if (mAuthor != null) {
                                        mAuthor.isConnectedToGoogleDrive = false;
                                        Utils.setAuthor(mAuthor);
                                    }
                                    view.onError(new Gson().toJson(driveAbout.error), EnumStatus.REQUEST_ACCESS_TOKEN);
                                }
                            } else {
                                final Author mAuthor = Author.getInstance().getAuthorInfo();
                                if (mAuthor != null) {
                                    mAuthor.isConnectedToGoogleDrive = false;
                                    Utils.setAuthor(mAuthor);
                                }
                                view.onError("Error null ", EnumStatus.REQUEST_ACCESS_TOKEN);
                            }
                        } catch (IOException e) {
                            final Author mAuthor = Author.getInstance().getAuthorInfo();
                            if (mAuthor != null) {
                                mAuthor.isConnectedToGoogleDrive = false;
                                Utils.setAuthor(mAuthor);
                            }
                        }
                    } else {
                        final Author mAuthor = Author.getInstance().getAuthorInfo();
                        if (mAuthor != null) {
                            mAuthor.isConnectedToGoogleDrive = false;
                            Utils.setAuthor(mAuthor);
                        }
                    }
                }));
    }

    public void onUploadFileInAppFolder() {
        Utils.Log(TAG, "onUploadFileInAppFolder");
        MediaType contentType = MediaType.parse("application/json; charset=UTF-8");
        HashMap<String, Object> content = new HashMap<>();
        File file = null;
        try {
           file =  Utils.writeToJson(new Gson().toJson(SQLiteHelper.getList()),File.createTempFile("history",".json"));
        }catch (Exception e){
            Utils.Log(TAG,"Could not generate temporary file");
            return;
        }
        List<String> list = new ArrayList<>();
        list.add(getString(R.string.key_appDataFolder));
        content.put(getString(R.string.key_name),"history.json");
        content.put(getString(R.string.key_parents), list);
        MultipartBody.Part metaPart = MultipartBody.Part.create(RequestBody.create(contentType, new Gson().toJson(content)));
        ProgressRequestBody fileBody = new ProgressRequestBody(file, new ProgressRequestBody.UploadCallbacks() {
            @Override
            public void onProgressUpdate(int percentage) {
                Utils.Log(TAG, "Progressing uploaded " + percentage + "%");
            }
            @Override
            public void onError() {
                Utils.Log(TAG, "onError");
            }
            @Override
            public void onFinish() {
                Utils.Log(TAG, "onFinish");
            }
        });
        fileBody.setContentType("application/json");
        MultipartBody.Part dataPart = MultipartBody.Part.create(fileBody);
        Call<DriveResponse> request = QRScannerApplication.serverDriveApi.uploadFileMultipleInAppFolder(Utils.getAccessToken(), metaPart, dataPart, "application/json");
        request.enqueue(new Callback<DriveResponse>() {
            @Override
            public void onResponse(Call<DriveResponse> call, Response<DriveResponse> response) {
                Utils.Log(TAG, "response successful :" + new Gson().toJson(response.body()));;
            }
            @Override
            public void onFailure(Call<DriveResponse> call, Throwable t) {
                Utils.Log(TAG, "response failed :" + t.getMessage());
            }
        });
    }

    public void getFileListInApp() {
        Utils.Log(TAG, "getFileListInApp");
        if (!Utils.isConnectedToGoogleDrive()){
            Utils.Log(TAG,"Request to update access token of Google drive");
            return;
        }
        subscriptions.add(QRScannerApplication.serverDriveApi.onGetListFileInAppFolder(Utils.getAccessToken(),QRScannerApplication.getInstance().getString(R.string.key_appDataFolder))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(__ -> Utils.Log(TAG,""))
                .subscribe(onResponse -> {
                    Utils.Log(TAG, "Response data from items " + new Gson().toJson(onResponse));
                    if (onResponse.error != null) {
                        Utils.Log(TAG, "onError:" + new Gson().toJson(onResponse));
                    } else {
                        final int count = onResponse.files.size();
                        Utils.Log(TAG,"Total count request :" + count);
                        Utils.Log(TAG,new Gson().toJson(onResponse));
                    }
                }, throwable -> {
                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        int code  = ((HttpException) throwable).response().code();
                        try {
                            final String value = bodys.string();
                            final DriveAbout driveAbout = new Gson().fromJson(value, DriveAbout.class);
                            if (driveAbout != null) {
                                if (driveAbout.error != null) {
                                    final Author mAuthor = Author.getInstance().getAuthorInfo();
                                    if (mAuthor!=null){
                                        mAuthor.isConnectedToGoogleDrive = false;
                                        Utils.setAuthor(mAuthor);
                                    }
                                }
                            } else {
                                Utils.Log(TAG,"Fetching data has issue");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Utils.Log(TAG, "Can not call " + throwable.getMessage());
                    }
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



    public interface GoogleDriveListener {
        void onError(String message,EnumStatus enumStatus);
        void onSuccessful(String message,EnumStatus enumStatus);
    }

    public interface ServiceManagerSyncDataListener {
        void onCompleted();
        void onError();
        void onCancel();
    }
}
