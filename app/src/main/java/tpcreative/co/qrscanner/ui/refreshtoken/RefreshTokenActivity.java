package tpcreative.co.qrscanner.ui.refreshtoken;
import android.accounts.Account;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.drive.DriveScopes;
import com.google.gson.Gson;
import java.io.IOException;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.controller.PrefsController;
import tpcreative.co.qrscanner.common.controller.ServiceManager;
import tpcreative.co.qrscanner.common.services.QRScannerApplication;
import tpcreative.co.qrscanner.common.services.QRScannerService;
import tpcreative.co.qrscanner.model.Author;
import tpcreative.co.qrscanner.model.EnumStatus;

public class RefreshTokenActivity extends AppCompatActivity {
    private static final String TAG = RefreshTokenActivity.class.getSimpleName();
    protected static final int REQUEST_CODE_SIGN_IN = 0;
    private GoogleSignInAccount mSignInAccount;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGoogleSignInClient = GoogleSignIn.getClient(this, QRScannerApplication.getInstance().getGoogleSignInOptions(null));
    }

    protected void signIn(final String email) {
        Utils.Log(TAG,"Sign in");
        Account account = new Account(email, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
        mGoogleSignInClient = GoogleSignIn.getClient(this, QRScannerApplication.getInstance().getGoogleSignInOptions(account));
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
    }

    private GoogleSignInClient getGoogleSignInClient(Account account){
        mGoogleSignInClient = GoogleSignIn.getClient(this, QRScannerApplication.getInstance().getGoogleSignInOptions(account));
        return mGoogleSignInClient;
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
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

    /**
     * Handles resolution callbacks.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SIGN_IN: {
                if (resultCode != RESULT_OK) {
                    // Sign-in may fail or be cancelled by the user. For this sample, sign-in is
                    // required and is fatal. For apps where sign-in is optional, handle
                    // appropriately
                    Utils.Log(TAG, "Sign-in failed.");
                    return;
                }
                Task<GoogleSignInAccount> getAccountTask =
                        GoogleSignIn.getSignedInAccountFromIntent(data);
                if (getAccountTask.isSuccessful()) {
                    Utils.Log(TAG, "sign in successful");
                    initializeDriveClient(getAccountTask.getResult());
                } else {
                    Utils.Log(TAG, "Sign-in failed..");
                }
                break;
            }
        }
    }

    private class GetAccessToken extends AsyncTask<Account, Void, String> {
        @Override
        protected String doInBackground(Account... accounts) {
            try {
                if (accounts==null){
                    return null;
                }
                if (accounts[0]==null){
                    return null;
                }
                GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                        QRScannerApplication.getInstance(), QRScannerApplication.getInstance().getRequiredScopesString());
                Utils.Log(TAG,"Account :"+ new Gson().toJson(accounts));
                credential.setSelectedAccount(accounts[0]);
                try {
                    String value = credential.getToken();
                    if (value!=null){
                        Utils.Log(TAG,"access token  start "+ value);
                        final Author mAuthor = Author.getInstance().getAuthorInfo();
                        if (mAuthor!=null){
                            mAuthor.isConnectedToGoogleDrive = true;
                            mAuthor.access_token = String.format(getString(R.string.access_token),value);
                            mAuthor.email = credential.getSelectedAccount().name;
                            Utils.setAuthor(mAuthor);
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
        @Override
        protected void onPostExecute(String accessToken) {
            super.onPostExecute(accessToken);
            try {
                if (accessToken != null) {
                    final Author mUser = Author.getInstance().getAuthorInfo();
                    if (mUser != null) {
                        //Log.d(TAG, "Call getDriveAbout " + new Gson().toJson(mUser));
                        if (ServiceManager.getInstance().getMyService()==null){
                            Utils.Log(TAG,"SuperSafeService is null");
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
                            }
                            @Override
                            public void onSuccessful(String message, EnumStatus status) {
                                Utils.Log(TAG,"onSuccessful " +message + " - " +status.name());
                                final Author mAuthor = Author.getInstance().getAuthorInfo();
                                finish();
                            }
                        });
                    }
                }
            }
            catch (Exception e){
                e.printStackTrace();
                Utils.Log(TAG,"Call onDriveClientReady");
            }
        }
    }

    /**
     * Continues the sign-in process, initializing the Drive clients with the current
     * user's account.
     */

    private void initializeDriveClient(GoogleSignInAccount signInAccount) {
        mSignInAccount = signInAccount;
        Utils.Log(TAG,"Google client ready");
        Utils.Log(TAG,"Account :"+ mSignInAccount.getAccount());
        new RefreshTokenActivity.GetAccessToken().execute(mSignInAccount.getAccount());
    }

    protected void revokeAccess() {
        if (mGoogleSignInClient==null){
            return;
        }
        if (mSignInAccount==null){
            return;
        }
        Utils.Log(TAG,"onRevokeAccess");
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        PrefsController.putBoolean(getString(R.string.key_request_sign_out_google_drive),false);
                    }
                });
    }
}