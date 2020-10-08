package tpcreative.co.qrscanner.common.services;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.snatik.storage.Storage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
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
import tpcreative.co.qrscanner.common.ResponseSingleton;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.api.request.DownloadFileRequest;
import tpcreative.co.qrscanner.common.api.response.DriveResponse;
import tpcreative.co.qrscanner.common.network.NetworkUtil;
import tpcreative.co.qrscanner.common.presenter.BaseView;
import tpcreative.co.qrscanner.common.presenter.PresenterService;
import tpcreative.co.qrscanner.common.services.download.DownloadService;
import tpcreative.co.qrscanner.common.services.upload.ProgressRequestBody;
import tpcreative.co.qrscanner.model.Author;
import tpcreative.co.qrscanner.model.DriveAbout;
import tpcreative.co.qrscanner.model.EnumStatus;
import tpcreative.co.qrscanner.model.SyncDataModel;

public class QRScannerService extends PresenterService<BaseView> implements QRScannerReceiver.ConnectivityReceiverListener {

    private static final String TAG = QRScannerService.class.getSimpleName();
    private final IBinder mBinder = new LocalBinder(); // Binder given to clients
    protected Storage storage;
    private Intent mIntent;
    private QRScannerReceiver androidReceiver;
    private DownloadService downloadService;

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.Log(TAG, "onCreate");
        storage = new Storage(this);
        onInitReceiver();
        QRScannerApplication.getInstance().setConnectivityListener(this);
        downloadService = new DownloadService();
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
        Utils.Log(TAG, "onDestroy");
        if (androidReceiver != null) {
            unregisterReceiver(androidReceiver);
        }
        /*Delete files and folders of temporary*/
        deleteTempFiles(getCacheDir());
    }

    private boolean deleteTempFiles(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) {
                        deleteTempFiles(f);
                    } else {
                        f.delete();
                    }
                }
            }
        }
        return file.delete();
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
        Utils.Log(TAG, "onStartCommand");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Bundle extras = intent.getExtras();
        Utils.Log(TAG, "onBind");
        // Get messager from the Activity
        if (extras != null) {
            Utils.Log("service", "onBind with extra");
        }
        return mBinder;
    }


    public void onSyncAuthor(){
        Utils.Log(TAG,"onSyncAuthor");
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
                    Utils.Log(TAG, "Author body: " + new Gson().toJson(onResponse));
                }, throwable -> {
                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        try {
                            Utils.Log(TAG,"Author error" +bodys.string());
                            String msg = new Gson().toJson(bodys.string());
                            Utils.Log(TAG, msg);
                        } catch (IOException e) {
                            Utils.Log(TAG,"Author IOException" +e.getMessage());
                            e.printStackTrace();
                        }
                    } else {
                        Utils.Log(TAG, "Author Can not call" + throwable.getMessage());
                    }
                    view.onStopLoading(EnumStatus.AUTHOR_SYNC);
                }));
    }

    public void onCheckVersion(){
        Utils.Log(TAG,"onCheckVersion");
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
                            ResponseSingleton.getInstance().onAlertLatestVersion();
                        }
                    }
                    view.onStopLoading(EnumStatus.CHECK_VERSION);
                    Utils.Log(TAG, "Body : " + new Gson().toJson(onResponse));
                }, throwable -> {
                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        try {
                            Utils.Log(TAG,"error" +bodys.string());
                            String msg = new Gson().toJson(bodys.string());
                            Utils.Log(TAG, msg);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Utils.Log(TAG, "Can not call" + throwable.getMessage());
                    }
                    view.onStopLoading(EnumStatus.CHECK_VERSION);
                }));
    }

    public void getDriveAbout(GoogleDriveListener view) {
        Utils.Log(TAG, "getDriveAbout");
        if (!Utils.isConnectedToGoogleDrive()) {
            view.onError("User is null",EnumStatus.DRIVE_CONNECTED_DISABLE);
            return;
        }
        String access_token = Utils.getAccessToken();
        if (access_token == null) {
            view.onError("Access token is null",EnumStatus.DRIVE_CONNECTED_DISABLE);
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
                        view.onError(new Gson().toJson(onResponse.error), EnumStatus.REQUEST_REFRESH_ACCESS_TOKEN);
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
                                    view.onError(new Gson().toJson(driveAbout.error), EnumStatus.REQUEST_REFRESH_ACCESS_TOKEN);
                                }
                            } else {
                                final Author mAuthor = Author.getInstance().getAuthorInfo();
                                if (mAuthor != null) {
                                    mAuthor.isConnectedToGoogleDrive = false;
                                    Utils.setAuthor(mAuthor);
                                }
                                view.onError("Error null ", EnumStatus.REQUEST_REFRESH_ACCESS_TOKEN);
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

    public void onUploadFileInAppFolder(BaseListener listener) {
        Utils.Log(TAG, "onUploadFileInAppFolder");
        MediaType contentType = MediaType.parse("application/json; charset=UTF-8");
        HashMap<String, Object> content = new HashMap<>();
        File file = null;
        try {
           file =  Utils.writeToJson(new SyncDataModel().toJson(),File.createTempFile("backup",".json"));
        }catch (Exception e){
            Utils.Log(TAG,"Could not generate temporary file");
            return;
        }
        List<String> list = new ArrayList<>();
        list.add(getString(R.string.key_appDataFolder));
        content.put(getString(R.string.key_name),"backup.json");
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
                listener.onSuccessful("Response data uploaded :" +new Gson().toJson(response.body()),EnumStatus.UPLOADED_SUCCESSFULLYY);
            }
            @Override
            public void onFailure(Call<DriveResponse> call, Throwable t) {
                Utils.Log(TAG, "response failed :" + t.getMessage());
                listener.onError(t.getMessage(),EnumStatus.UPLOADING_FAILED);;
            }
        });
    }

    public void onDownloadFile(String id, BaseListener<SyncDataModel> listener) {
        Utils.Log(TAG, "onDownloadFile !!!!");
        final DownloadFileRequest request = new DownloadFileRequest();
        File output = null;
        try {
            File outputDir = getExternalCacheDir(); // context being the Activity pointer
            output = File.createTempFile("backup",".json",outputDir);
            request.path_folder_output = outputDir.getAbsolutePath();
            request.file_name = output.getName();
            request.id = id;
            request.Authorization = Utils.getAccessToken();
        }
        catch (Exception e){
            e.getMessage();
            return;
        }
        downloadService.onProgressingDownload(new DownloadService.DownLoadServiceListener() {
            @Override
            public void onDownLoadCompleted(File file_name, DownloadFileRequest request) {
                Utils.Log(TAG, "onDownLoadCompleted " + file_name.getAbsolutePath());
                final String mValue = loadFromTempFile(file_name);
                if (mValue!=null){
                    final SyncDataModel mDataValue = new Gson().fromJson(mValue,new TypeToken<SyncDataModel>(){}.getType());
                    if (mDataValue!=null){
                        Utils.Log(TAG,"List value "+ new Gson().toJson(mDataValue));
                        listener.onShowObjects(mDataValue);
                        listener.onSuccessful("Downloaded successfully",EnumStatus.DOWNLOADED_SUCCESSFULLY);
                    }
                }
            }
            @Override
            public void onDownLoadError(String error) {
                Utils.Log(TAG, "onDownLoadError " + error);
                listener.onError(error,EnumStatus.DOWNLOADING_FAILED);
            }
            @Override
            public void onProgressingDownloading(int percent) {
                Utils.Log(TAG, "Progressing downloaded " + percent + "%");
            }
            @Override
            public void onAttachmentElapsedTime(long elapsed) {
            }
            @Override
            public void onAttachmentAllTimeForDownloading(long all) {
            }
            @Override
            public void onAttachmentRemainingTime(long all) {
            }
            @Override
            public void onAttachmentSpeedPerSecond(double all) {
            }
            @Override
            public void onAttachmentTotalDownload(long totalByte, long totalByteDownloaded) {
            }
            @Override
            public void onSavedCompleted() {
                Utils.Log(TAG, "onSavedCompleted ");
            }
            @Override
            public void onErrorSave(String name) {
                Utils.Log(TAG, "onErrorSave");
            }
            @Override
            public void onCodeResponse(int code, DownloadFileRequest request) {
                if (code == 404) {
                    Utils.Log(TAG,"Request delete id");
                    listener.onError("Downloading not found id",EnumStatus.DOWNLOADING_NOT_FOUND_ID);
                }
            }
            @Override
            public Map<String, String> onHeader() {
                return new HashMap<>();
            }
        });
        downloadService.downloadFileFromGoogleDrive(request);
    }

    public String loadFromTempFile(File file){
        String jString = null;
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(file);
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            /* Instead of using default, pass in a decoder. */
            jString = Charset.defaultCharset().decode(bb).toString();
            return jString;
        }
        catch (FileNotFoundException e){

        }
        catch (IOException e){

        }
        finally {
            try {
                stream.close();
            }catch (IOException e){

            }
        }
        return null;
    }

    public void getFileListInApp(BaseListener<DriveResponse> listener) {
        Utils.Log(TAG, "getFileListInApp");
        if (!Utils.isConnectedToGoogleDrive()){
            Utils.Log(TAG,"Request to update access token of Google drive");
            listener.onError("Request to update access token of Google drive",EnumStatus.DRIVE_CONNECTED_DISABLE);
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
                        listener.onShowListObjects(onResponse.files);
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
                                    listener.onError("Request refresh access token",EnumStatus.REQUEST_REFRESH_ACCESS_TOKEN);
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


    /*Get List Categories*/
    public void onDeleteCloudItems(final String id,BaseListener listener) {
        Utils.Log(TAG, "onDeleteCloudItems");
        subscriptions.add(QRScannerApplication.serverDriveApi.onDeleteCloudItem(Utils.getAccessToken(), id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onResponse -> {
                    Utils.Log(TAG,"Deleted cloud response code " + onResponse.code());
                    if (onResponse.code() == 204) {
                        Utils.Log(TAG,"Deleted id successfully");
                        listener.onSuccessful("Deleted successfully",EnumStatus.DELETED_SUCCESSFULLY);
                    } else if (onResponse.code() == 404) {
                        Utils.Log(TAG,"This id is not exiting");
                        listener.onError("Not found id",EnumStatus.DELETING_NOT_FOUND_ID);
                    } else {
                        Utils.Log(TAG,"Not found for this case");
                    }
                }, throwable -> {
                    if (throwable instanceof HttpException) {
                        ResponseBody bodys = ((HttpException) throwable).response().errorBody();
                        try {
                            final String value = bodys.string();
                            final DriveAbout driveAbout = new Gson().fromJson(value, DriveAbout.class);
                            if (driveAbout != null) {
                                if (driveAbout.error != null) {
                                    Utils.Log(TAG,"Request refresh access token");
                                    listener.onError("Request refresh access token",EnumStatus.REQUEST_REFRESH_ACCESS_TOKEN);
                                }
                            } else {
                                Utils.Log(TAG,"Request refresh access token");
                                listener.onError("Request refresh access token",EnumStatus.REQUEST_REFRESH_ACCESS_TOKEN);
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

    public interface BaseListener <T> {
        void onShowListObjects(List<T>list);
        void onShowObjects(T object);
        void onError(String message, EnumStatus status);
        void onSuccessful(String message,EnumStatus status);
    }

}
