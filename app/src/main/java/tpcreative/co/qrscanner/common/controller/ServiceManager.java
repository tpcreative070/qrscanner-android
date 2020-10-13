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
import com.anjlab.android.iab.v3.PurchaseData;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.gson.Gson;
import com.google.zxing.client.result.ParsedResultType;
import com.opencsv.CSVWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.BackupSingleton;
import tpcreative.co.qrscanner.common.HistorySingleton;
import tpcreative.co.qrscanner.common.Navigator;
import tpcreative.co.qrscanner.common.RefreshTokenSingleton;
import tpcreative.co.qrscanner.common.ResponseSingleton;
import tpcreative.co.qrscanner.common.SaveSingleton;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.api.response.DriveResponse;
import tpcreative.co.qrscanner.common.presenter.BaseView;
import tpcreative.co.qrscanner.common.services.QRScannerApplication;
import tpcreative.co.qrscanner.common.services.QRScannerService;
import tpcreative.co.qrscanner.helper.SQLiteHelper;
import tpcreative.co.qrscanner.model.EnumFragmentType;
import tpcreative.co.qrscanner.model.EnumStatus;
import tpcreative.co.qrscanner.model.HistoryModel;
import tpcreative.co.qrscanner.model.SaveModel;
import tpcreative.co.qrscanner.model.SyncDataModel;

public class ServiceManager implements BaseView {

    private static final String TAG = ServiceManager.class.getSimpleName();
    private static ServiceManager instance;
    private QRScannerService myService;
    private Context mContext;
    private Disposable subscriptions;
    private Map<String,String>mMapDelete = new HashMap<>();
    private List<DriveResponse> mDriveIdList = new ArrayList<>();
    private boolean isDismiss ;
    private boolean isSyncingData;

    ServiceConnection myConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder binder) {
            Utils.Log(TAG, "connected");
            myService = ((QRScannerService.LocalBinder) binder).getService();
            myService.bindView(ServiceManager.this);
            ServiceManager.getInstance().onPreparingSyncData(false);
            if (Utils.isProVersion() && !Utils.isAlreadyCheckout()){
                ServiceManager.getInstance().onCheckout(null);
            }
        }
        //binder comes from server to communicate with method's of
        public void onServiceDisconnected(ComponentName className) {
            Utils.Log(TAG, "disconnected");
            myService = null;
        }
    };

    public boolean isSyncingData() {
        return isSyncingData;
    }

    public void onPickUpNewEmail(Activity context) {
        try {
            String email = Utils.getDriveEmail();
            if (email==null){
                email = "a@gmail.com";
            }
            String value = String.format(QRScannerApplication.getInstance().getString(R.string.choose_an_new_account));
            Account account1 = new Account(email, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
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
    public void onPreparingSyncData(boolean isDismissApp){
        if (!Utils.isPremium()){
            Utils.Log(TAG,"Please upgrade to premium version");
            if (isDismissApp){
                onDismissServices();
            }
            return;
        }
        if (Utils.isPremium()){
           if (!Utils.isTurnedOnBackup()){
               if (isDismissApp){
                   onDismissServices();
               }
               Utils.Log(TAG,"Backup status is turn off. Please turn on it don't lose data");
               return;
           }
        }
        if (myService==null){
            Utils.Log(TAG,"Request service");
            onStartService();
            return;
        }
        if (Utils.getAccessToken()==null){
            Utils.Log(TAG,"Need to sign in with Google drive first");
            if (isDismissApp){
                onDismissServices();
            }
            return;
        }
        if (!Utils.isConnectedToGoogleDrive()){
            Utils.Log(TAG,"Need to connect to Google drive");
            RefreshTokenSingleton.getInstance().onStart(ServiceManager.class);
            if (isDismissApp){
                onDismissServices();
            }
            return;
        }
        if (isSyncingData){
            Utils.Log(TAG,"Syncing data. Please wait...");
            return;
        }
        Utils.Log(TAG,"Starting sync data");
        this.isDismiss = isDismissApp;
        onGetItemList();
    }

    public void onGetItemList(){
        isSyncingData = true;
        myService.getFileListInApp(new QRScannerService.BaseListener<DriveResponse>() {
            @Override
            public void onShowListObjects(List<DriveResponse> list) {
                Utils.Log(TAG,"Response data " + new Gson().toJson(list));
                mDriveIdList.clear();
                mDriveIdList.addAll(list);
                if (mDriveIdList.size()>0){
                    final DriveResponse mData = mDriveIdList.get(0);
                    onPreparingDownloadItemData(mData.id);
                }else{
                    onPreparingUploadItemData();
                }
            }
            @Override
            public void onShowObjects(DriveResponse object) {
            }
            @Override
            public void onError(String message, EnumStatus status) {
                isSyncingData = false;
                Utils.Log(TAG,"response error " + message);
                switch (status){
                    case REQUEST_REFRESH_ACCESS_TOKEN:
                        RefreshTokenSingleton.getInstance().onStart(ServiceManager.class);
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
    public void onPreparingDownloadItemData(String id){
        myService.onDownloadFile(id, new QRScannerService.BaseListener<SyncDataModel>() {
            @Override
            public void onShowListObjects(List<SyncDataModel> list) {
                Utils.Log(TAG,new Gson().toJson(list));
            }

            @Override
            public void onShowObjects(SyncDataModel object) {
                Utils.Log(TAG,new Gson().toJson(object));
                onCheckingDataToSyncToLocalDB(object);
            }

            @Override
            public void onError(String message, EnumStatus status) {
                Utils.Log(TAG,message);
                isSyncingData = false;
            }

            @Override
            public void onSuccessful(String message, EnumStatus status) {
                Utils.Log(TAG,message);
            }
        });
    }

    /*Checking data to insert to local db*/
    public void onCheckingDataToSyncToLocalDB(SyncDataModel mObject){
        final List<SaveModel> mSaveList = mObject.saveList;
        final List<HistoryModel> mHistoryList = mObject.historyList;

        /*Checking data to insert to local db*/
        final List<SaveModel> mSaveAddResultList = Utils.checkSaveItemToInsertToLocal(mSaveList);
        final List<HistoryModel> mHistoryAddResultList = Utils.checkHistoryItemToInsertToLocal(mHistoryList);

        /*Checking data to update to local db*/
        final List<SaveModel> mSaveUpdateResultList = Utils.checkSaveItemToUpdateToLocal(mSaveList);
        final List<HistoryModel> mHistoryUpdateResultList = Utils.checkHistoryItemToUpdateToLocal(mHistoryList);

        /*Checking data to delete to local db*/
        final List<SaveModel> mSaveDeleteResultList = Utils.checkSaveDeleteSyncedLocal(mSaveList);
        final List<HistoryModel> mHistoryDeleteResultList = Utils.checkHistoryDeleteSyncedLocal(mHistoryList);


        Utils.Log(TAG,"Some items of save need to be inserting "+mSaveAddResultList.size());
        Utils.Log(TAG,"Some items of history need to be inserting "+mHistoryAddResultList.size());

        Utils.Log(TAG,"Some items of save need to be updating "+mSaveUpdateResultList.size());
        Utils.Log(TAG,"Some items of history need to be updating "+mHistoryUpdateResultList.size());

        Utils.Log(TAG,"Some items of save need to be deleting "+mSaveDeleteResultList.size());
        Utils.Log(TAG,"Some items of history need to be deleting "+mHistoryDeleteResultList.size());

        /*Inserting to local db*/
        for (SaveModel index : mSaveAddResultList){
            SQLiteHelper.onInsert(index);
        }
        for (HistoryModel index : mHistoryAddResultList){
            SQLiteHelper.onInsert(index);
        }

        /*Updating to local db*/
        for (SaveModel index : mSaveUpdateResultList){
            SQLiteHelper.onUpdate(index,true);
        }
        for (HistoryModel index : mHistoryUpdateResultList){
            SQLiteHelper.onUpdate(index,true);
        }

        /*Deleting to local db*/
        for (SaveModel index : mSaveDeleteResultList){
            SQLiteHelper.onDelete(index);
        }
        for (HistoryModel index : mHistoryDeleteResultList){
            SQLiteHelper.onDelete(index);
        }
        /*Final step sync upload file*/
        if (Utils.isEqualTimeSynced(mObject.updatedDateTime)){
            Utils.Log(TAG,"The session of previous already synced");
            if (isDismiss){
                onDismissServices();
            }
            isSyncingData = false;
        }else {
            Utils.Log(TAG,"Preparing delete old file...");
            Utils.Log(TAG,"Last time from cloud..." +mObject.updatedDateTime);
            Utils.Log(TAG,"Last time from local..." +Utils.getLastTimeSynced());
            onPreparingDeleteItemData();
        }
    }

    /*Updated history and save after upload file*/
    public void onUpdatedHistoryAndSaveToSyncedItem(){
        final List<SaveModel> mSaveList = SQLiteHelper.getSaveList(false);
        final List<HistoryModel> mHistoryList = SQLiteHelper.getHistoryList(false);
        for (SaveModel index : mSaveList){
            if (!index.isSynced){
                index.isSynced = true;
                SQLiteHelper.onUpdate(index,false);
            }
        }
        for (HistoryModel index : mHistoryList){
            if (!index.isSynced){
                index.isSynced = true;
                SQLiteHelper.onUpdate(index,false);
            }
        }
        /*Final step*/
        if (isDismiss){
            onDismissServices();
            isSyncingData = false;
        }else{
            SaveSingleton.getInstance().reloadData();
            HistorySingleton.getInstance().reloadData();
            BackupSingleton.getInstance().reloadData();
            isSyncingData = false;
            Utils.Log(TAG,"Syncing data completed");
        }
    }

    /*onPreparingDownload*/
    public void onPreparingDeleteItemData(){
        if (mDriveIdList.size()>0){
            mMapDelete.clear();
            mMapDelete = Utils.mergeListToHashMap(mDriveIdList);
            final String id = Utils.getIndexOfHashMap(mMapDelete);
            if (id!=null){
                Utils.Log(TAG,"onPreparingDeleteItemData total: "+ mMapDelete.size());
                onDeleteItemData(id);
            }
        }else{
            if (isDismiss){
                onDismissServices();
            }
            isSyncingData = false;
            Utils.Log(TAG,"Not found data to delete");
        }
    }

    private void onDeleteItemData(String id){
        myService.onDeleteCloudItems(id, new QRScannerService.BaseListener() {
            @Override
            public void onShowListObjects(List list) {

            }
            @Override
            public void onShowObjects(Object object) {

            }
            @Override
            public void onError(String message, EnumStatus status) {
                Utils.Log(TAG,message + ": "+id);
                if (status==EnumStatus.DELETING_NOT_FOUND_ID){
                    if (Utils.deletedIndexOfHashMap(id,mMapDelete)){
                        final String id = Utils.getIndexOfHashMap(mMapDelete);
                        if (id!=null){
                            onDeleteItemData(id);
                        }else{
                            Utils.Log(TAG,"Deleted item completely");
                            onPreparingUploadItemData();
                        }
                    }
                }
            }
            @Override
            public void onSuccessful(String message, EnumStatus status) {
                Utils.Log(TAG,message + ": "+id);
                if (status==EnumStatus.DELETED_SUCCESSFULLY){
                    if (Utils.deletedIndexOfHashMap(id,mMapDelete)){
                        final String id = Utils.getIndexOfHashMap(mMapDelete);
                        if (id!=null){
                            onDeleteItemData(id);
                        }else{
                            Utils.Log(TAG,"Deleted item completely");
                            onPreparingUploadItemData();
                        }
                    }
                }
            }
        });
    }

    /*onPreparingDownload*/
    public void onPreparingUploadItemData(){
        myService.onUploadFileInAppFolder(new QRScannerService.BaseListener() {
            @Override
            public void onShowListObjects(List list) {

            }

            @Override
            public void onShowObjects(Object object) {

            }

            @Override
            public void onError(String message, EnumStatus status) {
                Utils.Log(TAG,message);
                if (isDismiss){
                    onDismissServices();
                }
                isSyncingData = false;
            }
            @Override
            public void onSuccessful(String message, EnumStatus status) {
                Utils.Log(TAG,message);
                if (status==EnumStatus.UPLOADED_SUCCESSFULLYY){
                    onUpdatedHistoryAndSaveToSyncedItem();
                }
            }
        });
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
        isDismiss = false;
        Utils.setDefaultSaveHistoryDeletedKey();
        Utils.Log(TAG, "Dismiss Service manager");
    }

    public void onCheckout(PurchaseData purchaseData){
        if (myService!=null){
            Utils.Log(TAG,"Call checkcout here");
            myService.onAddCheckout(purchaseData, new QRScannerService.BaseListener() {
                @Override
                public void onShowListObjects(List list) {

                }
                @Override
                public void onShowObjects(Object object) {

                }

                @Override
                public void onError(String message, EnumStatus status) {
                    Utils.Log(TAG,message);
                }

                @Override
                public void onSuccessful(String message, EnumStatus status) {
                    Utils.Log(TAG,message);
                }
            });
        }
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
                isSyncingData = false;
                onPreparingSyncData(false);
                ResponseSingleton.getInstance().onNetworkConnectionChanged(true);
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
                        final List<HistoryModel> listHistory = SQLiteHelper.getHistoryList();
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
                        final List<SaveModel> listSaver = SQLiteHelper.getSaveList();
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

    public interface ServiceManagerClickedItemsListener {
        void onYes();
    }
}
