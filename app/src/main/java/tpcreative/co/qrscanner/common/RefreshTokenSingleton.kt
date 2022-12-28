package tpcreative.co.qrscanner.common
import android.accounts.Account
import co.tpcreative.supersafe.common.network.Status
import com.google.android.gms.auth.GoogleAuthException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.controller.PrefsController
import tpcreative.co.qrscanner.common.controller.ServiceManager
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.model.Author

class RefreshTokenSingleton private constructor() {
    private var mSignInAccount: GoogleSignInAccount? = null
    private var mGoogleSignInClient: GoogleSignInClient?
    private fun getGoogleSignInClient(account: Account?): GoogleSignInClient? {
        mGoogleSignInClient = QRScannerApplication.getInstance().getGoogleSignInOptions(account)?.let { GoogleSignIn.getClient(QRScannerApplication.Companion.getInstance(), it) }
        return mGoogleSignInClient
    }

    fun <T> onStart(tClass: Class<T>) {
        if (tClass != null) {
            TAG = tClass.simpleName
        }
        val account = GoogleSignIn.getLastSignedInAccount(QRScannerApplication.getInstance())
        if (account != null && GoogleSignIn.hasPermissions(account, Scope(DriveScopes.DRIVE_FILE), Scope(DriveScopes.DRIVE_APPDATA))) {
            getGoogleSignInClient(account.account)
            initializeDriveClient(account)
            mSignInAccount = account
        } else {
            val mAuthor: Author? = Author.getInstance()?.getAuthorInfo()
            if (mAuthor != null) {
                mAuthor.isConnectedToGoogleDrive = false
                Utils.setAuthor(mAuthor)
            }
        }
    }

    private fun onRefreshAccessToken(accounts: Account?)  = CoroutineScope(Dispatchers.IO).launch{
        try {
            if (accounts == null) {
               return@launch
            }
            val credential = GoogleAccountCredential.usingOAuth2(
                    QRScannerApplication.getInstance(), QRScannerApplication.getInstance().getRequiredScopesString())
            credential.selectedAccount = accounts
            try {
                val value = credential?.token
                if (value != null) {
                    val mAuthor: Author? = Author.getInstance()?.getAuthorInfo()
                    if (mAuthor != null) {
                        mAuthor.isConnectedToGoogleDrive = true
                        mAuthor.access_token = kotlin.String.format(QRScannerApplication.Companion.getInstance().getString(R.string.access_token), value)
                        Utils.Log(TAG, "Refresh access token value: " + mAuthor.access_token)
                        mAuthor.email = credential.selectedAccount.name
                        Utils.setAuthor(mAuthor)
                        Utils.Log(TAG, "onRefreshAccessToken")
                        Utils.Log(TAG, "isSyncingData 99 " + ServiceManager.getInstance().isSyncingData())
                        ServiceManager.getInstance().onPreparingSyncData(false)
                    }
                    val mResultDriveAbout = ServiceManager.getInstance().getDriveAbout()
                    when(mResultDriveAbout.status){
                        Status.SUCCESS ->{
                            Utils.Log(TAG, "Call onDriveClientReady")
                        }
                        else -> {
                            Utils.Log(TAG,mResultDriveAbout.message)
                            revokeAccess()
                        }
                    }
                }
            } catch (e: GoogleAuthException) {
                Utils.Log(TAG, "Error occurred on GoogleAuthException")
            }
        } catch (recoverableException: UserRecoverableAuthIOException) {
            Utils.Log(TAG, "Error occurred on UserRecoverableAuthIOException")
        }
    }

    /**
     * Continues the sign-in process, initializing the Drive clients with the current
     * user's account.
     */
    private fun initializeDriveClient(signInAccount: GoogleSignInAccount?) {
        mSignInAccount = signInAccount
        Utils.Log(TAG, "Request refresh access token")
        onRefreshAccessToken(mSignInAccount?.getAccount())
        //new RefreshTokenSingleton.GetAccessToken().execute(mSignInAccount.getAccount());
    }

    protected fun revokeAccess() {
        if (mGoogleSignInClient == null) {
            return
        }
        if (mSignInAccount == null) {
            return
        }
        Utils.Log(TAG, "onRevokeAccess")
        mGoogleSignInClient?.revokeAccess()?.addOnCompleteListener { PrefsController.putBoolean(QRScannerApplication.Companion.getInstance().getString(R.string.key_request_sign_out_google_drive), false) }
    }

    companion object {
        private var TAG = RefreshTokenSingleton::class.java.simpleName
        private var instance: RefreshTokenSingleton? = null
        fun getInstance(): RefreshTokenSingleton? {
            if (instance == null) {
                synchronized(RefreshTokenSingleton::class.java) {
                    if (instance == null) {
                        instance = RefreshTokenSingleton()
                    }
                }
            }
            return instance
        }
    }

    init {
        mGoogleSignInClient = QRScannerApplication.getInstance().getGoogleSignInOptions(null)?.let { GoogleSignIn.getClient(QRScannerApplication.getInstance(), it) }
    }
}