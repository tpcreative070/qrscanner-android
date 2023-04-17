package tpcreative.co.qrscanner.ui.backup
import android.accounts.AccountManager
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.tapadoo.alerter.Alerter
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.BackupSingleton.BackupSingletonListener
import tpcreative.co.qrscanner.common.activity.BaseGoogleApi
import tpcreative.co.qrscanner.common.controller.ServiceManager
import tpcreative.co.qrscanner.common.extension.onDisplayLatTimeSyncedCompletely
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.common.services.QRScannerService.ServiceManagerSyncDataListener
import tpcreative.co.qrscanner.common.view.ads.AdsView
import tpcreative.co.qrscanner.databinding.ActivityBackupBinding
import tpcreative.co.qrscanner.helper.SQLiteHelper
import tpcreative.co.qrscanner.model.EnumScreens

class BackupActivity : BaseGoogleApi(), BackupSingletonListener {
    lateinit var viewModel : BackupViewModel
    var viewAds : AdsView? = null
    lateinit var binding: ActivityBackupBinding
    private var isLoaded : Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBackupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
    }

    fun requestSyncData() {
        if (Utils.isRequestSyncData() || ServiceManager.getInstance().isSyncingData()) {
            binding.tvUsedSpace.text = getText(R.string.syncing_data)
            binding.tvUsedSpace.visibility = View.VISIBLE
            binding.btnEnable.setTextColor(ContextCompat.getColor(this, R.color.material_gray_400))
            binding.btnEnable.isEnabled = false
            Utils.Log(TAG,"Calling sync data")
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
                val accountName = data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                Utils.Log(TAG, "account name $accountName")
                if (Utils.getDriveEmail() != null) {
                    if (Utils.getDriveEmail() != accountName) {
                        /*Updated lifecycle for sync data*/
                        Utils.setLastTimeSynced(Utils.getCurrentDateTimeSort())
                        Utils.setRequestSync(true)
                        requestSyncData()
                        Utils.Log(TAG, "isSyncingData 92 " + ServiceManager.getInstance().isSyncingData())
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
                Utils.Log(TAG, "isSyncingData 109 " + ServiceManager.getInstance().isSyncingData())
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

    /*show ads*/
    fun doShowAds(isShow: Boolean) {
        if (isShow) {
            QRScannerApplication.getInstance().loadBackupSmallView(viewAds?.getSmallAds())
            QRScannerApplication.getInstance().loadBackupLargeView(viewAds?.getLargeAds())
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                showAds()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDriveClientReady() {
        runOnUiThread {
            Utils.Log(TAG, "onDriveClientReady...")
            val email = Utils.getDriveEmail()
            if (email != null) {
                val mValue = String.format(getString(R.string.current_email), email)
                val newText = mValue.replace(email, "<font color=#e19704><b>$email</b></font>")
                binding.tvEmail.text = HtmlCompat.fromHtml(newText, HtmlCompat.FROM_HTML_MODE_LEGACY)
                binding.tvEmail.visibility = View.VISIBLE
                binding.btnEnable.text = getText(R.string.switch_account)
            }
            Utils.Log(ServiceManager::class.java, "onDriveClientReady")
            Utils.Log(ServiceManager::class.java, "isSyncingData 143" + ServiceManager.getInstance().isSyncingData())
            ServiceManager.getInstance().onPreparingSyncData(false)
        }
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
        BackupSingleton.getInstance()?.setListener(null)
        SettingsSingleton.getInstance()?.onSyncDataRequest()
    }

    override fun onResume() {
        super.onResume()
        if(Utils.isHiddenAds(EnumScreens.BACKUP_SMALL)){
            binding.rlAdsRoot.visibility = View.GONE
        }
        if(Utils.isHiddenAds(EnumScreens.BACKUP_LARGE)){
            binding.rlBannerLarger.visibility = View.GONE
        }
        if (isLoaded){
            checkingShowAds()
        }
        isLoaded = true
        QRScannerApplication.getInstance().onResumeAds(EnumScreens.BACKUP_SMALL)
        QRScannerApplication.getInstance().onResumeAds(EnumScreens.BACKUP_LARGE)
    }

    override fun onPause() {
        QRScannerApplication.getInstance().onPauseAds(EnumScreens.BACKUP_SMALL)
        QRScannerApplication.getInstance().onPauseAds(EnumScreens.BACKUP_LARGE)
        super.onPause()
    }

    override fun startServiceNow() {
        ServiceManager.getInstance().onStartService()
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
        Utils.Log(TAG,"reloadData")
        runOnUiThread {
            Utils.Log(TAG, "reloadData...")
            val mSaveSyncedList = SQLiteHelper.getSaveList(true)
            val mHistorySyncedList = SQLiteHelper.getHistoryList(true)
            binding.tvUsedSpace.visibility = View.VISIBLE
            val mTextSynced: String = if (Utils.isInnovation()){
                String.format(getString(R.string.synced_innovation_data), mHistorySyncedList.size.toString() + "")
            }else{
                String.format(getString(R.string.synced_data), mSaveSyncedList.size.toString() + "", mHistorySyncedList.size.toString() + "")
            }
            binding.tvUsedSpace.text = HtmlCompat.fromHtml(mTextSynced, HtmlCompat.FROM_HTML_MODE_LEGACY)
            binding.tvLastTimeSynced.text = HtmlCompat.fromHtml(Utils.onDisplayLatTimeSyncedCompletely(), HtmlCompat.FROM_HTML_MODE_LEGACY)
            binding.btnEnable.isEnabled = true
            binding.btnEnable.setTextColor(ContextCompat.getColor(this@BackupActivity, R.color.white))
        }
    }

    companion object {
        private val TAG = BackupActivity::class.java.simpleName
    }
}