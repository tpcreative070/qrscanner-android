package tpcreative.co.qrscanner.common.controller;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.presenter.BaseView;
import tpcreative.co.qrscanner.common.services.QRScannerApplication;
import tpcreative.co.qrscanner.common.services.QRScannerService;
import tpcreative.co.qrscanner.model.EnumAction;
import tpcreative.co.qrscanner.model.EnumStatus;

public class ServiceManager implements BaseView {

    private static final String TAG = ServiceManager.class.getSimpleName();
    private static ServiceManager instance;
    private QRScannerService myService;
    private Context mContext;
    private Disposable subscriptions;
    ServiceConnection myConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder binder) {
            Log.d(TAG, "connected");
            myService = ((QRScannerService.LocalBinder) binder).getService();
            myService.bindView(ServiceManager.this);
            myService.onSyncAuthor();
        }
        //binder comes from server to communicate with method's of
        public void onServiceDisconnected(ComponentName className) {
            Log.d(TAG, "disconnected");
            myService = null;
        }
    };

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
       if (myService!=null){
           return;
       }
        Intent intent = null;
        intent = new Intent(mContext, QRScannerService.class);
        intent.putExtra(TAG, "Message");
        mContext.bindService(intent, myConnection, Context.BIND_AUTO_CREATE);
        Log.d(TAG, "onStartService");
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

    protected void showMessage(String message) {
        Toast.makeText(QRScannerApplication.getInstance(), message, Toast.LENGTH_LONG).show();
    }

    private String getString(int res) {
        String value = QRScannerApplication.getInstance().getString(res);
        return value;
    }


    /*User info*/
    public void onAuthorSync() {
        if (myService != null) {
            myService.onSyncAuthor();
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
        Log.d(TAG, "onError response :" + message + " - " + status.name());
    }

    @Override
    public void onSuccessful(String message) {
        Log.d(TAG, "onSuccessful Response  :" + message);
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
                break;
            }
        }
    }

    public void saveImage(final Bitmap finalBitmap, final EnumAction enumAction, final String type, final String code, Utils.UtilsListener listenner) {
        subscriptions = Observable.create(subscriber -> {
            String root = QRScannerApplication.getInstance().getPathFolder();
            File myDir = new File(root);
            myDir.mkdirs();
            String fName = "Image_"+ type + code +".jpg";
            fName = fName.replace("/","");
            fName = fName.replace(":","");
            File file = new File (myDir, fName);
            try {
                Log.d(TAG,"path :" + file.getAbsolutePath());
                FileOutputStream out = new FileOutputStream(file);
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                listenner.onSaved(file.getAbsolutePath(),enumAction);
            }
        })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .subscribe(response -> {
                });
    }

    public static void saveImage() {


    }


}
