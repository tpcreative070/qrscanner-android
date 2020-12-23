package tpcreative.co.qrscanner.common

import android.accounts.Account
import com.google.android.gms.auth.GoogleAuthException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.services.drive.DriveScopes
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.controller.PrefsController
import tpcreative.co.qrscanner.common.controller.ServiceManager
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.common.services.QRScannerService.GoogleDriveListener
import tpcreative.co.qrscanner.model.Author
import tpcreative.co.qrscanner.model.EnumStatus
import java.io.IOException
import java.util.concurrent.Callable

class RefreshTokenSingleton private constructor() {
    private var mSignInAccount: GoogleSignInAccount? = null
    private var mGoogleSignInClient: GoogleSignInClient?
    var compositeDisposable: CompositeDisposable? = null
    private fun getGoogleSignInClient(account: Account?): GoogleSignInClient? {
        mGoogleSignInClient = GoogleSignIn.getClient(QRScannerApplication.Companion.getInstance(), QRScannerApplication.Companion.getInstance().getGoogleSignInOptions(account))
        return mGoogleSignInClient
    }

    fun <T> onStart(tClass: Class<T?>?) {
        if (tClass != null) {
            TAG = tClass.simpleName
        }
        val account = GoogleSignIn.getLastSignedInAccount(QRScannerApplication.Companion.getInstance())
        if (account != null && GoogleSignIn.hasPermissions(account, Scope(DriveScopes.DRIVE_FILE), Scope(DriveScopes.DRIVE_APPDATA))) {
            getGoogleSignInClient(account.account)
            initializeDriveClient(account)
            mSignInAccount = account
        } else {
            val mAuthor: Author = Author.Companion.getInstance().getAuthorInfo()
            if (mAuthor != null) {
                mAuthor.isConnectedToGoogleDrive = false
                Utils.setAuthor(mAuthor)
            }
        }
    }

    fun onRefreshAccessToken(accounts: Account?) {
        compositeDisposable = CompositeDisposable()
        compositeDisposable.add(Observable.fromCallable(Callable {
            try {
                if (accounts == null) {
                    return@Callable null
                }
                val credential = GoogleAccountCredential.usingOAuth2(
                        QRScannerApplication.Companion.getInstance(), QRScannerApplication.Companion.getInstance().getRequiredScopesString())
                credential.selectedAccount = accounts
                try {
                    val value = credential.token
                    if (value != null) {
                        val mAuthor: Author = Author.Companion.getInstance().getAuthorInfo()
                        if (mAuthor != null) {
                            mAuthor.isConnectedToGoogleDrive = true
                            mAuthor.access_token = kotlin.String.format(QRScannerApplication.Companion.getInstance().getString(R.string.access_token), value)
                            Utils.Log(TAG, "Refresh access token value: " + mAuthor.access_token)
                            mAuthor.email = credential.selectedAccount.name
                            Utils.setAuthor(mAuthor)
                            Utils.Log(ServiceManager::class.java, "onRefreshAccessToken")
                            Utils.Log(ServiceManager::class.java, "isSyncingData 99 " + ServiceManager.Companion.getInstance().isSyncingData())
                            ServiceManager.Companion.getInstance().onPreparingSyncData(false)
                        }
                    }
                    return@Callable value
                } catch (e: GoogleAuthException) {
                    Utils.Log(TAG, "Error occurred on GoogleAuthException")
                }
            } catch (recoverableException: UserRecoverableAuthIOException) {
                Utils.Log(TAG, "Error occurred on UserRecoverableAuthIOException")
            } catch (e: IOException) {
                Utils.Log(TAG, "Error occurred on IOException")
            }
            null
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe { response: String? ->
                    try {
                        if (response != null) {
                            val mUser: Author = Author.Companion.getInstance().getAuthorInfo()
                            if (mUser != null) {
                                //Log.d(TAG, "Call getDriveAbout " + new Gson().toJson(mUser));
                                if (ServiceManager.Companion.getInstance().getMyService() == null) {
                                    Utils.Log(TAG, "QRScannerService is null")
                                    compositeDisposable.dispose()
                                    return@subscribe
                                }
                                ServiceManager.Companion.getInstance().getMyService().getDriveAbout(object : GoogleDriveListener {
                                    override fun onError(message: String?, status: EnumStatus?) {
                                        Utils.Log(TAG, "onError " + message + " - " + status.name)
                                        when (status) {
                                            EnumStatus.REQUEST_REFRESH_ACCESS_TOKEN -> {
                                                revokeAccess()
                                            }
                                        }
                                        compositeDisposable.dispose()
                                    }

                                    override fun onSuccessful(message: String?, status: EnumStatus?) {
                                        Utils.Log(TAG, "onSuccessful " + message + " - " + status.name)
                                        compositeDisposable.dispose()
                                    }
                                })
                            } else {
                                compositeDisposable.dispose()
                            }
                        } else {
                            compositeDisposable.dispose()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Utils.Log(TAG, "Call onDriveClientReady")
                        compositeDisposable.dispose()
                    }
                })
    }

    /**
     * Continues the sign-in process, initializing the Drive clients with the current
     * user's account.
     */
    private fun initializeDriveClient(signInAccount: GoogleSignInAccount?) {
        mSignInAccount = signInAccount
        Utils.Log(TAG, "Request refresh access token")
        onRefreshAccessToken(mSignInAccount.getAccount())
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
        mGoogleSignInClient.revokeAccess().addOnCompleteListener { PrefsController.putBoolean(QRScannerApplication.Companion.getInstance().getString(R.string.key_request_sign_out_google_drive), false) }
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
        mGoogleSignInClient = GoogleSignIn.getClient(QRScannerApplication.Companion.getInstance(), QRScannerApplication.Companion.getInstance().getGoogleSignInOptions(null))
    }
}