package tpcreative.co.qrscanner.common;
import android.accounts.Account;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.drive.DriveScopes;
import java.io.IOException;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.controller.PrefsController;
import tpcreative.co.qrscanner.common.controller.ServiceManager;
import tpcreative.co.qrscanner.common.services.QRScannerApplication;
import tpcreative.co.qrscanner.common.services.QRScannerService;
import tpcreative.co.qrscanner.model.Author;
import tpcreative.co.qrscanner.model.EnumStatus;

public class RefreshTokenSingleton {
    private static String TAG = RefreshTokenSingleton.class.getSimpleName();
    private GoogleSignInAccount mSignInAccount;
    private GoogleSignInClient mGoogleSignInClient;
    private static RefreshTokenSingleton instance ;
    CompositeDisposable compositeDisposable = null;
    
    public static RefreshTokenSingleton getInstance(){
        if (instance==null){
            synchronized (RefreshTokenSingleton.class){
                if (instance==null){
                    instance = new RefreshTokenSingleton();
                }
            }
        }
        return instance;
    }
    
    private RefreshTokenSingleton(){
        mGoogleSignInClient = GoogleSignIn.getClient(QRScannerApplication.getInstance(), QRScannerApplication.getInstance().getGoogleSignInOptions(null));
    }
    
    private GoogleSignInClient getGoogleSignInClient(Account account){
        mGoogleSignInClient = GoogleSignIn.getClient(QRScannerApplication.getInstance(), QRScannerApplication.getInstance().getGoogleSignInOptions(account));
        return mGoogleSignInClient;
    }
    
    public <T>void onStart(Class<T>tClass) {
        if (tClass!=null){
            TAG = tClass.getSimpleName();
        }
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(QRScannerApplication.getInstance());
        if (account != null && GoogleSignIn.hasPermissions(account,new Scope(DriveScopes.DRIVE_FILE),new Scope(DriveScopes.DRIVE_APPDATA))) {
            getGoogleSignInClient(account.getAccount());
            initializeDriveClient(account);
            mSignInAccount = account;
        } else {
            final Author mAuthor = Author.getInstance().getAuthorInfo();
            if (mAuthor!=null){
                mAuthor.isConnectedToGoogleDrive = false;
                Utils.setAuthor(mAuthor);
            }
        }
    }

    public void onRefreshAccessToken(Account accounts){
        compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(Observable.fromCallable(new Callable<String>() {
            @Override
            public String call() throws Exception {
                try {
                    if (accounts==null){
                        return null;
                    }
                    GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                            QRScannerApplication.getInstance(), QRScannerApplication.getInstance().getRequiredScopesString());
                    credential.setSelectedAccount(accounts);
                    try {
                        String value = credential.getToken();
                        if (value!=null){
                            final Author mAuthor = Author.getInstance().getAuthorInfo();
                            if (mAuthor!=null){
                                mAuthor.isConnectedToGoogleDrive = true;
                                mAuthor.access_token = String.format(QRScannerApplication.getInstance().getString(R.string.access_token),value);
                                Utils.Log(TAG,"Refresh access token value: "+ mAuthor.access_token);
                                mAuthor.email = credential.getSelectedAccount().name;
                                Utils.setAuthor(mAuthor);
                                ServiceManager.getInstance().onPreparingSyncData(false);
                            }
                        }
                        return value;
                    }
                    catch (GoogleAuthException e){
                        Utils.Log(TAG,"Error occurred on GoogleAuthException");
                    }
                } catch (UserRecoverableAuthIOException recoverableException) {
                    Utils.Log(TAG,"Error occurred on UserRecoverableAuthIOException");
                } catch (IOException e) {
                    Utils.Log(TAG,"Error occurred on IOException");
                }
                return null;
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(response ->{
            try {
                if (response != null) {
                    final Author mUser = Author.getInstance().getAuthorInfo();
                    if (mUser != null) {
                        //Log.d(TAG, "Call getDriveAbout " + new Gson().toJson(mUser));
                        if (ServiceManager.getInstance().getMyService()==null){
                            Utils.Log(TAG,"QRScannerService is null");
                            compositeDisposable.dispose();
                            return;
                        }
                        ServiceManager.getInstance().getMyService().getDriveAbout(new QRScannerService.GoogleDriveListener() {
                            @Override
                            public void onError(String message, EnumStatus status) {
                                Utils.Log(TAG,"onError " +message + " - " +status.name());
                                switch (status){
                                    case REQUEST_REFRESH_ACCESS_TOKEN:{
                                        revokeAccess();
                                        break;
                                    }
                                }
                                compositeDisposable.dispose();
                            }
                            @Override
                            public void onSuccessful(String message, EnumStatus status) {
                                Utils.Log(TAG,"onSuccessful " +message + " - " +status.name());
                                compositeDisposable.dispose();
                            }
                        });
                    }else{
                        compositeDisposable.dispose();
                    }
                }else{
                    compositeDisposable.dispose();
                }
            }
            catch (Exception e){
                e.printStackTrace();
                Utils.Log(TAG,"Call onDriveClientReady");
                compositeDisposable.dispose();
            }
        }));
    }

    /**
     * Continues the sign-in process, initializing the Drive clients with the current
     * user's account.
     */

    private void initializeDriveClient(GoogleSignInAccount signInAccount) {
        mSignInAccount = signInAccount;
        Utils.Log(TAG,"Request refresh access token");
        onRefreshAccessToken(mSignInAccount.getAccount());
        //new RefreshTokenSingleton.GetAccessToken().execute(mSignInAccount.getAccount());
    }

    protected void revokeAccess() {
        if (mGoogleSignInClient==null){
            return;
        }
        if (mSignInAccount==null){
            return;
        }
        Utils.Log(TAG,"onRevokeAccess");
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        PrefsController.putBoolean(QRScannerApplication.getInstance().getString(R.string.key_request_sign_out_google_drive),false);
                    }
                });
    }
}