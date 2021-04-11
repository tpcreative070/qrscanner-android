package tpcreative.co.qrscanner.common.activity
import android.accounts.Account
import android.content.Intent
import android.os.Bundle
import co.tpcreative.supersafe.common.network.Status
import com.google.android.gms.auth.GoogleAuthException
import com.google.android.gms.auth.GoogleAuthUtil
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
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.controller.PrefsController
import tpcreative.co.qrscanner.common.controller.ServiceManager
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.common.services.QRScannerService.ServiceManagerSyncDataListener
import tpcreative.co.qrscanner.model.Author
import java.io.IOException

abstract class BaseGoogleApi : BaseActivitySlide() {
    private var mSignInAccount: GoogleSignInAccount? = null
    private var mGoogleSignInClient: GoogleSignInClient? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mGoogleSignInClient = QRScannerApplication.getInstance().getGoogleSignInOptions(null)?.let { GoogleSignIn.getClient(this, it) }
    }

    protected fun signIn(email: String?) {
        Utils.Log(TAG, "Sign in")
        val account = Account(email, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE)
        mGoogleSignInClient = QRScannerApplication.getInstance().getGoogleSignInOptions(account)?.let { GoogleSignIn.getClient(this, it) }
        startActivityForResult(mGoogleSignInClient?.signInIntent, REQUEST_CODE_SIGN_IN)
    }

    private fun getGoogleSignInClient(account: Account?): GoogleSignInClient? {
        mGoogleSignInClient = QRScannerApplication.getInstance().getGoogleSignInOptions(account)?.let { GoogleSignIn.getClient(this, it) }
        return mGoogleSignInClient
    }

    override fun onStart() {
        super.onStart()
        Utils.Log(ServiceManager::class.java, "onStart " + Utils.isRequestSyncData())
        if (Utils.isRequestSyncData() || ServiceManager.getInstance().isSyncingData()) {
            Utils.Log(TAG,"Checking isSyncingData is true ")
            val account = GoogleSignIn.getLastSignedInAccount(this)
            if (account != null && GoogleSignIn.hasPermissions(account, Scope(DriveScopes.DRIVE_FILE), Scope(DriveScopes.DRIVE_APPDATA))) {
                getGoogleSignInClient(account.account)
                initializeDriveClient(account)
                mSignInAccount = account
            } else {
                val mAuthor: Author? = Author.getInstance()?.getAuthorInfo()
                if (mAuthor != null) {
                    mAuthor.isConnectedToGoogleDrive = false
                    Utils.setAuthor(mAuthor)
                    onDriveError()
                }
            }
        }
    }

    /**
     * Handles resolution callbacks.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_SIGN_IN -> {
                if (resultCode != RESULT_OK) {
                    // Sign-in may fail or be cancelled by the user. For this sample, sign-in is
                    // required and is fatal. For apps where sign-in is optional, handle
                    // appropriately
                    Utils.Log(TAG, "Sign-in failed.")
                    onDriveError()
                    return
                }
                val getAccountTask = GoogleSignIn.getSignedInAccountFromIntent(data)
                if (getAccountTask.isSuccessful) {
                    Utils.Log(TAG, "sign in successful")
                    onSignedInSuccessful()
                    initializeDriveClient(getAccountTask.result)
                } else {
                    onDriveError()
                    Utils.Log(TAG, "Sign-in failed..")
                }
            }
        }
    }

    private fun onRefreshAccessToken(accounts: Account?) = CoroutineScope(Dispatchers.IO).launch {
        try {
            if (accounts == null) {
                Utils.Log(TAG, "Account is null")
                return@launch
            }
            val credential = GoogleAccountCredential.usingOAuth2(
                    QRScannerApplication.getInstance(), QRScannerApplication.getInstance().getRequiredScopesString())
            credential.selectedAccount = accounts
            try {
                val value = credential.token
                if (value != null) {
                    val mAuthor: Author? = Author.getInstance()?.getAuthorInfo()
                    if (mAuthor != null) {
                        mAuthor.isConnectedToGoogleDrive = true
                        mAuthor.access_token = kotlin.String.format(QRScannerApplication.getInstance().getString(R.string.access_token), value)
                        Utils.Log(TAG, "Refresh access token value: " + mAuthor.access_token)
                        mAuthor.email = credential.selectedAccount.name
                        Utils.Log(TAG, "isSyncingData 136 " + ServiceManager.getInstance().isSyncingData())
                        Utils.Log(TAG, "onRefreshAccessToken!!!")
                        if (Utils.getDriveEmail() != null && mAuthor.email != Utils.getDriveEmail()) {
                            onSwitchedUser()
                        }
                        Utils.setAuthor(mAuthor)
                        if (ServiceManager.getInstance().getMyService() == null) {
                            Utils.Log(TAG, "SuperSafeService is null")
                            startServiceNow()
                            return@launch
                        }

                        val mResultDriveAbout = ServiceManager.getInstance().getDriveAbout()
                        when(mResultDriveAbout.status){
                            Status.SUCCESS ->{
                                if (isSignIn()) {
                                    Utils.Log(TAG, "Call onDriveClientReady")
                                    onDriveClientReady()
                                }
                            }
                            else -> {
                                Utils.Log(TAG,mResultDriveAbout.message)
                                revokeAccess()
                            }
                        }
                    }
                }
            } catch (e: GoogleAuthException) {
                Utils.Log(TAG, "Error occurred on GoogleAuthException")
            }
        } catch (recoverableException: UserRecoverableAuthIOException) {
            Utils.Log(TAG, "Error occurred on UserRecoverableAuthIOException")
        } catch (e: IOException) {
            Utils.Log(TAG, "Error occurred on IOException")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    /**
     * Continues the sign-in process, initializing the Drive clients with the current
     * user's account.
     */
    private fun initializeDriveClient(signInAccount: GoogleSignInAccount?) {
        mSignInAccount = signInAccount
        Utils.Log(TAG, "Google client ready")
        Utils.Log(TAG, "Account :" + mSignInAccount?.getAccount())
        onRefreshAccessToken(mSignInAccount?.getAccount())
    }

    /**
     * Called after the user has signed in and the Drive client has been initialized.
     */
    protected abstract fun onSwitchedUser()
    protected abstract fun onDriveClientReady()
    protected abstract fun onDriveError()
    protected abstract fun onDriveSignOut()
    protected abstract fun onDriveRevokeAccess()
    protected abstract fun onSignedInSuccessful()
    protected abstract fun isSignIn(): Boolean
    protected abstract fun startServiceNow()
    protected fun signOut(ls: ServiceManagerSyncDataListener?) {
        Utils.Log(TAG, "Call signOut")
        if (mGoogleSignInClient == null) {
            return
        }
        mGoogleSignInClient?.signOut()?.addOnCompleteListener(this) {
            onDriveSignOut()
            ls?.onCompleted()
        }?.addOnFailureListener { ls?.onError() }
    }

    protected fun revokeAccess() {
        if (mGoogleSignInClient == null) {
            return
        }
        Utils.Log(TAG, "onRevokeAccess")
        mGoogleSignInClient?.revokeAccess()?.addOnCompleteListener(this
        ) {
            onDriveRevokeAccess()
            PrefsController.putBoolean(getString(R.string.key_request_sign_out_google_drive), false)
        }
    }

    companion object {
        private val TAG = BaseGoogleApi::class.java.simpleName
        protected const val REQUEST_CODE_SIGN_IN = 0
    }
}