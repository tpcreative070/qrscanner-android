package tpcreative.co.qrscanner.common.controller;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.gson.Gson;
import com.google.zxing.client.result.ParsedResultType;
import com.opencsv.CSVWriter;
import java.io.FileWriter;
import java.util.List;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.Navigator;
import tpcreative.co.qrscanner.common.SingletonResponse;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.activity.BaseGoogleApi;
import tpcreative.co.qrscanner.common.api.response.DriveResponse;
import tpcreative.co.qrscanner.common.presenter.BaseView;
import tpcreative.co.qrscanner.common.services.QRScannerApplication;
import tpcreative.co.qrscanner.common.services.QRScannerService;
import tpcreative.co.qrscanner.helper.SQLiteHelper;
import tpcreative.co.qrscanner.model.EnumFragmentType;
import tpcreative.co.qrscanner.model.EnumStatus;
import tpcreative.co.qrscanner.model.HistoryModel;
import tpcreative.co.qrscanner.model.SaveModel;

public class ServiceManager implements BaseView {

    private static final String TAG = ServiceManager.class.getSimpleName();
    private static ServiceManager instance;
    private QRScannerService myService;
    private Context mContext;
    private Disposable subscriptions;
    ServiceConnection myConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder binder) {
            Utils.Log(TAG, "connected");
            myService = ((QRScannerService.LocalBinder) binder).getService();
            myService.bindView(ServiceManager.this);
            myService.onSyncAuthor();
            myService.onCheckVersion();
            ServiceManager.getInstance().onPreparingSyncData();
        }

        //binder comes from server to communicate with method's of
        public void onServiceDisconnected(ComponentName className) {
            Utils.Log(TAG, "disconnected");
            myService = null;
        }
    };

    public void onPickUpNewEmail(Activity context) {
        try {
            String value = String.format(QRScannerApplication.getInstance().getString(R.string.choose_an_new_account));
            Account account1 = new Account("abc@gmail.com", GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
            Intent intent = AccountManager.newChooseAccountIntent(account1, null,
                    new String[] { GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE }, value, null, null, null);
            intent.putExtra("overrideTheme", 1);
            context.startActivityForResult(intent, Navigator.REQUEST_CODE_EMAIL);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static ServiceManager getInstance() {
        if (instance == null) {
            instance = new ServiceManager();
        }
        return instance;
    }


    public void setContext(Context mContext) {
        this.mContext = mContext;
    }

    private void doBindService() {
        if (myService != null) {
            return;
        }
        Intent intent = null;
        intent = new Intent(mContext, QRScannerService.class);
        intent.putExtra(TAG, "Message");
        mContext.bindService(intent, myConnection, Context.BIND_AUTO_CREATE);
        Utils.Log(TAG, "onStartService");
    }

    public void onStartService() {
        if (myService == null) {
            doBindService();
        }
    }

    public void onStopService() {
        if (myService != null) {
            mContext.unbindService(myConnection);
            myService = null;
        }
    }

    public QRScannerService getMyService() {
        return myService;
    }

    private String getString(int res) {
        String value = QRScannerApplication.getInstance().getString(res);
        return value;
    }

    /*Sync data*/
    public void onPreparingSyncData(){
        if (!Utils.isPremium()){
            Utils.Log(TAG,"Please upgrade to premium version");
            return;
        }
        if (myService==null){
            Utils.Log(TAG,"Request service");
            onStartService();
            return;
        }
        if (Utils.getAccessToken()==null){
            Utils.Log(TAG,"Need to sign in with Google drive first");
            return;
        }
        if (!Utils.isConnectedToGoogleDrive()){
            Utils.Log(TAG,"Need to connect to Google drive");
            Navigator.onRefreshAccessToken(QRScannerApplication.getInstance());
            return;
        }
        Utils.Log(TAG,"Starting sync data");
        onGetItemList();
    }

    public void onGetItemList(){
        myService.getFileListInApp(new QRScannerService.BaseListener<DriveResponse>() {
            @Override
            public void onShowListObjects(List<DriveResponse> list) {
                Utils.Log(TAG,"response data " + new Gson().toJson(list));
                Navigator.onRefreshAccessToken(QRScannerApplication.getInstance());
            }
            @Override
            public void onShowObjects(DriveResponse object) {

            }
            @Override
            public void onError(String message, EnumStatus status) {
                Utils.Log(TAG,"response error " + message);
                switch (status){
                    case REQUEST_REFRESH_ACCESS_TOKEN:
                        Navigator.onRefreshAccessToken(QRScannerApplication.getInstance());
                        break;
                    default:
                        break;
                }
            }
            @Override
            public void onSuccessful(String message, EnumStatus status) {

            }
        });
    }

    /*onPreparingDownload*/
    public void onPreparingDownloadItemData(){

    }

    public void onDownloadItemData(){

    }

    /*onPreparingDownload*/
    public void onPreparingDeleteItemData(){

    }

    public void onDeleteItemData(){

    }


    /*onPreparingDownload*/
    public void onPreparingUploadItemData(){

    }

    public void onUploadItemData(){

    }

    /*User info*/
    public void onAuthorSync() {
        if (myService != null) {
            myService.onSyncAuthor();
        } else {
            Utils.Log(TAG, "My services is null");
        }
    }

    /*Author info*/
    public void onCheckVersion() {
        if (myService != null) {
            myService.onCheckVersion();
        } else {
            Utils.Log(TAG, "My services is null");
        }
    }

    public void onDismissServices() {
        onStopService();
        if (myService != null) {
            myService.unbindView();
        }

        if (subscriptions != null) {
            subscriptions.dispose();
        }
        Utils.Log(TAG, "Dismiss Service manager");
    }

    @Override
    public void onError(String message, EnumStatus status) {
        Utils.Log(TAG, "onError response :" + message + " - " + status.name());
    }

    @Override
    public void onSuccessful(String message) {
        Utils.Log(TAG, "onSuccessful Response  :" + message);
    }

    @Override
    public void onStartLoading(EnumStatus status) {

    }

    @Override
    public void onStopLoading(EnumStatus status) {

    }

    @Override
    public void onError(String message) {

    }

    @Override
    public void onSuccessful(String message, EnumStatus status, Object object) {

    }

    @Override
    public void onSuccessful(String message, EnumStatus status, List list) {

    }

    @Override
    public Context getContext() {
        return QRScannerApplication.getInstance();
    }

    @Override
    public Activity getActivity() {
        return null;
    }


    @Override
    public void onSuccessful(String message, EnumStatus status) {
        switch (status) {
            case CONNECTED: {
                onAuthorSync();
                onCheckVersion();
                SingletonResponse.getInstance().onNetworkConnectionChanged(true);
                break;
            }
        }
    }

    public void onExportDatabaseCSVTask(EnumFragmentType enumFragmentType, ServiceManagerListener ls) {
        subscriptions = Observable.create(subscriber -> {
            String path = QRScannerApplication.getInstance().getPathFolder() + "/" +enumFragmentType.name()+"_"+ System.currentTimeMillis() + ".csv";
            CSVWriter csvWrite = null;
            try {
                csvWrite = new CSVWriter(new FileWriter(path));
                switch (enumFragmentType) {
                    case HISTORY: {
                        final List<HistoryModel> listHistory = SQLiteHelper.getList();
                        String arrStr1[] = {
                                "FormatType",
                                "Url",
                                "Text",
                                "ProductId",
                                "ISBN",
                                "Phone",
                                "Email",
                                "Subject",
                                "Message",
                                "Latitude",
                                "Longitude",
                                "Query",
                                "Title",
                                "Location",
                                "Description",
                                "StartEvent",
                                "EndEvent",
                                "FullName",
                                "Address",
                                "SSId",
                                "Password",
                                "NetworkEncryption",
                                "CreatedDateTime",
                        };

                        csvWrite.writeNext(arrStr1);
                        for (HistoryModel index : listHistory) {
                            String value[] = {
                                    index.createType,
                                    index.url,
                                    index.createType.equalsIgnoreCase(ParsedResultType.TEXT.name()) ? index.text : "",
                                    index.createType.equalsIgnoreCase(ParsedResultType.PRODUCT.name()) ? index.text : "" ,
                                    index.createType.equalsIgnoreCase(ParsedResultType.ISBN.name()) ? index.text : "",
                                    index.phone,
                                    index.email,
                                    index.subject,
                                    index.message,
                                    (index.lat == 0) ? "" : index.lat +"",
                                    (index.lon == 0) ? "" : index.lon +"",
                                    index.query,
                                    index.title,
                                    index.location,
                                    index.description,
                                    index.startEvent.equals("") ? "" : Utils.convertMillisecondsToDateTime(index.startEventMilliseconds),
                                    index.endEvent.equals("") ? "" :Utils.convertMillisecondsToDateTime(index.endEventMilliseconds),
                                    index.fullName,
                                    index.address,
                                    index.ssId,
                                    index.password,
                                    index.networkEncryption,
                                    index.createDatetime};
                            csvWrite.writeNext(value);
                        }
                        break;
                    }
                    case SAVER: {
                        final List<SaveModel> listSaver = SQLiteHelper.getListSave();
                        String arrStr1[] = {
                                "FormatType",
                                "Url",
                                "Text",
                                "ProductId",
                                "ISBN",
                                "Phone",
                                "Email",
                                "Subject",
                                "Message",
                                "Latitude",
                                "Longitude",
                                "Query",
                                "Title",
                                "Location",
                                "Description",
                                "StartEvent",
                                "EndEvent",
                                "FullName",
                                "Address",
                                "SSId",
                                "Password",
                                "NetworkEncryption",
                                "CreatedDateTime"
                        };
                        csvWrite.writeNext(arrStr1);
                        for (SaveModel index : listSaver) {
                            String value[] = {
                                    index.createType,
                                    index.url,
                                    index.createType.equalsIgnoreCase(ParsedResultType.TEXT.name()) ? index.text : "",
                                    index.createType.equalsIgnoreCase(ParsedResultType.PRODUCT.name()) ? index.text : "" ,
                                    index.createType.equalsIgnoreCase(ParsedResultType.ISBN.name()) ? index.text : "",
                                    index.phone,
                                    index.email,
                                    index.subject,
                                    index.message,
                                    (index.lat == 0) ? "" : index.lat +"",
                                    (index.lon == 0) ? "" : index.lon +"",
                                    index.query,
                                    index.title,
                                    index.location,
                                    index.description,
                                    index.startEvent.equals("") ? "" : Utils.convertMillisecondsToDateTime(index.startEventMilliseconds),
                                    index.endEvent.equals("") ? "" : Utils.convertMillisecondsToDateTime(index.endEventMilliseconds),
                                    index.fullName,
                                    index.address,
                                    index.ssId,
                                    index.password,
                                    index.networkEncryption,
                                    index.createDatetime};
                            csvWrite.writeNext(value);
                        }
                        break;
                    }
                    default:{
                        Utils.Log(TAG,"NoThing");
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    subscriber.onNext(true);
                    subscriber.onComplete();
                    if (csvWrite!=null){
                        csvWrite.flush();
                        csvWrite.close();
                        ls.onExportingSVCCompleted(path);
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .subscribe(response -> {
                    Utils.Log(TAG, "Exporting cvs done");
                });
    }

    public interface ServiceManagerListener {
        void onExportingSVCCompleted(String path);
    }

    public interface ServiceManagerClickedListener {
        void onYes();
        void onNo();
    }
}
