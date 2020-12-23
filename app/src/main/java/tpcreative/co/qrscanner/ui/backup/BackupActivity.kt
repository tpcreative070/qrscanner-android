package tpcreative.co.qrscanner.ui.backup

import android.accounts.AccountManager
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.*
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import butterknife.BindView
import butterknife.OnClick
import com.tapadoo.alerter.Alerter
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.BackupSingleton.BackupSingletonListener
import tpcreative.co.qrscanner.common.activity.BaseGoogleApi
import tpcreative.co.qrscanner.common.controller.ServiceManager
import tpcreative.co.qrscanner.common.network.NetworkUtil
import tpcreative.co.qrscanner.common.services.QRScannerService.ServiceManagerSyncDataListener
import tpcreative.co.qrscanner.helper.SQLiteHelper

class BackupActivity : BaseGoogleApi(), BackupSingletonListener {
    @BindView(R.id.tvEmail)
    var tvEmail: AppCompatTextView? = null

    @BindView(R.id.btnEnable)
    var btnEnable: AppCompatButton? = null

    @BindView(R.id.tvUsedSpace)
    var tvUsedSpace: AppCompatTextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_backup)
        val toolbar = findViewById<Toolbar?>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar.setDisplayHomeAsUpEnabled(true)
        BackupSingleton.Companion.getInstance().setListener(this)
        val email = Utils.getDriveEmail()
        if (email != null) {
            val mValue = String.format(getString(R.string.current_email), email)
            val newText = mValue.replace(email, "<font color=#e19704><b>$email</b></font>")
            tvEmail.setText(HtmlCompat.fromHtml(newText, HtmlCompat.FROM_HTML_MODE_LEGACY))
            tvEmail.setVisibility(View.VISIBLE)
            btnEnable.setText(getText(R.string.switch_account))
            if (Utils.isConnectedToGoogleDrive()) {
                val mSaveSyncedList = SQLiteHelper.getSaveList(true)
                val mHistorySyncedList = SQLiteHelper.getHistoryList(true)
                tvUsedSpace.setVisibility(View.VISIBLE)
                val mTextSynced = String.format(getString(R.string.synced_data), mSaveSyncedList.size.toString() + "", mHistorySyncedList.size.toString() + "")
                tvUsedSpace.setText(HtmlCompat.fromHtml(mTextSynced, HtmlCompat.FROM_HTML_MODE_LEGACY))
                requestSyncData()
            }
        }
        if (NetworkUtil.pingIpAddress(this)) {
            onShowConnectionAlert()
        }
    }

    fun requestSyncData() {
        if (Utils.isRequestSyncData() || ServiceManager.Companion.getInstance().isSyncingData()) {
            tvUsedSpace.setText(getText(R.string.syncing_data))
            tvUsedSpace.setVisibility(View.VISIBLE)
            btnEnable.setTextColor(ContextCompat.getColor(this, R.color.material_gray_400))
            btnEnable.setEnabled(false)
        }
    }

    @OnClick(R.id.btnEnable)
    fun onClickedEnable() {
        if (NetworkUtil.pingIpAddress(this)) {
            onShowConnectionAlert()
            return
        }
        Utils.Log(ServiceManager::class.java, "isSyncingData 74 " + ServiceManager.Companion.getInstance().isSyncingData())
        if (!ServiceManager.Companion.getInstance().isSyncingData()) {
            ServiceManager.Companion.getInstance().onPickUpNewEmail(this)
        } else {
            Utils.Log(ServiceManager::class.java, "isSyncingData 78 is running")
            requestSyncData()
        }
    }

    fun onShowConnectionAlert() {
        Alerter.create(this)
                .setTitle("Warning")
                .setBackgroundColorInt(ContextCompat.getColor(this, R.color.colorAccent))
                .setText("No connection. Please check your internet connection")
                .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Navigator.REQUEST_CODE_EMAIL -> if (resultCode == RESULT_OK) {
                val accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                Utils.Log(TAG, "account name $accountName")
                if (Utils.getDriveEmail() != null) {
                    if (Utils.getDriveEmail() != accountName) {
                        /*Updated lifecycle for sync data*/
                        Utils.setLastTimeSynced(Utils.getCurrentDateTimeSort())
                        Utils.setRequestSync(true)
                        requestSyncData()
                        Utils.Log(TAG, "isSyncingData 92 " + ServiceManager.Companion.getInstance().isSyncingData())
                        signOut(object : ServiceManagerSyncDataListener {
                            override fun onCompleted() {
                                signIn(accountName)
                            }

                            override fun onError() {
                                signIn(accountName)
                            }

                            override fun onCancel() {}
                        })
                        return
                    }
                }
                Utils.Log(TAG, "isSyncingData 109 " + ServiceManager.Companion.getInstance().isSyncingData())
                Utils.setLastTimeSynced(Utils.getCurrentDateTimeSort())
                Utils.setRequestSync(true)
                signOut(object : ServiceManagerSyncDataListener {
                    override fun onCompleted() {
                        signIn(accountName)
                    }

                    override fun onError() {
                        signIn(accountName)
                    }

                    override fun onCancel() {}
                })
            }
            else -> {
            }
        }
    }

    override fun onDriveClientReady() {
        Utils.Log(TAG, "onDriveClientReady...")
        val email = Utils.getDriveEmail()
        if (email != null) {
            val mValue = String.format(getString(R.string.current_email), email)
            val newText = mValue.replace(email, "<font color=#e19704><b>$email</b></font>")
            tvEmail.setText(HtmlCompat.fromHtml(newText, HtmlCompat.FROM_HTML_MODE_LEGACY))
            tvEmail.setVisibility(View.VISIBLE)
            btnEnable.setText(getText(R.string.switch_account))
        }
        Utils.Log(ServiceManager::class.java, "onDriveClientReady")
        Utils.Log(ServiceManager::class.java, "isSyncingData 143" + ServiceManager.Companion.getInstance().isSyncingData())
        ServiceManager.Companion.getInstance().onPreparingSyncData(false)
    }

    override fun onDriveError() {}
    override fun onDriveSignOut() {}
    override fun onDriveRevokeAccess() {}
    override fun onSwitchedUser() {
        Utils.Log(ServiceManager::class.java, "onSwitchedUser and delete synced data " + ServiceManager.Companion.getInstance().isSyncingData())
        Utils.cleanDataAlreadySynced()
        Utils.setDefaultSaveHistoryDeletedKey()
    }

    override fun isSignIn(): Boolean {
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        BackupSingleton.Companion.getInstance().setListener(null)
        SettingsSingleton.Companion.getInstance().onSyncDataRequest()
    }

    override fun startServiceNow() {
        ServiceManager.Companion.getInstance().onStartService()
    }

    override fun onSignedInSuccessful() {
        requestSyncData()
    }

    override fun onStart() {
        super.onStart()
        if (Utils.isConnectedToGoogleDrive()) {
            requestSyncData()
        }
    }

    override fun reloadData() {
        Utils.Log(TAG, "reloadData...")
        val mSaveSyncedList = SQLiteHelper.getSaveList(true)
        val mHistorySyncedList = SQLiteHelper.getHistoryList(true)
        tvUsedSpace.setVisibility(View.VISIBLE)
        val mTextSynced = String.format(getString(R.string.synced_data), mSaveSyncedList.size.toString() + "", mHistorySyncedList.size.toString() + "")
        tvUsedSpace.setText(HtmlCompat.fromHtml(mTextSynced, HtmlCompat.FROM_HTML_MODE_LEGACY))
        btnEnable.setEnabled(true)
        btnEnable.setTextColor(ContextCompat.getColor(this, R.color.white))
    }

    companion object {
        private val TAG = BackupActivity::class.java.simpleName
    }
}