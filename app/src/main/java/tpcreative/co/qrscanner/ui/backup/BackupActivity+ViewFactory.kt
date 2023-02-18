package tpcreative.co.qrscanner.ui.backup
import android.os.Build
import android.view.View
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.core.text.HtmlCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_backup.*
import kotlinx.android.synthetic.main.activity_backup.imgRemove
import kotlinx.android.synthetic.main.activity_backup.rlAdsRoot
import kotlinx.android.synthetic.main.activity_backup.rlBannerLarger
import kotlinx.android.synthetic.main.activity_backup.toolbar
import kotlinx.android.synthetic.main.activity_wifi.*
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.BackupSingleton
import tpcreative.co.qrscanner.common.Navigator
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.controller.ServiceManager
import tpcreative.co.qrscanner.common.extension.onDisplayLatTimeSyncedCompletely
import tpcreative.co.qrscanner.common.network.NetworkUtil
import tpcreative.co.qrscanner.common.network.base.ViewModelFactory
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.helper.SQLiteHelper
import tpcreative.co.qrscanner.ui.filecolor.*

fun BackupActivity.initUI(){
    TAG = this::class.java.simpleName
    setupViewModel()
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    if(Utils.isHiddenAds()){
        rlAdsRoot.visibility = View.GONE
        rlBannerLarger.visibility = View.GONE
    }
    BackupSingleton.getInstance()?.setListener(this)
    val email = Utils.getDriveEmail()
    if (email != null) {
        val mValue = String.format(getString(R.string.current_email), email)
        val newText = mValue.replace(email, "<font color=#e19704><b>$email</b></font>")
        tvEmail.text = HtmlCompat.fromHtml(newText, HtmlCompat.FROM_HTML_MODE_LEGACY)
        tvEmail.visibility = View.VISIBLE
        btnEnable.text = getText(R.string.switch_account)
        Utils.Log(TAG,"calling here...???")
        if (Utils.isConnectedToGoogleDrive()) {
            Utils.Log(TAG,"calling here...")
            val mSaveSyncedList = SQLiteHelper.getSaveList(true)
            val mHistorySyncedList = SQLiteHelper.getHistoryList(true)
            tvUsedSpace?.visibility = View.VISIBLE
            val mTextSynced: String = if (Utils.isInnovation()){
                String.format(getString(R.string.synced_innovation_data), mHistorySyncedList.size.toString() + "")
            }else{
                String.format(getString(R.string.synced_data), mSaveSyncedList.size.toString() + "", mHistorySyncedList.size.toString() + "")
            }
            tvUsedSpace?.text = HtmlCompat.fromHtml(mTextSynced, HtmlCompat.FROM_HTML_MODE_LEGACY)
            tvLastTimeSynced?.text = HtmlCompat.fromHtml(Utils.onDisplayLatTimeSyncedCompletely(), HtmlCompat.FROM_HTML_MODE_LEGACY)
            requestSyncData()
        }
    }
    if (NetworkUtil.pingIpAddress(this)) {
        onShowConnectionAlert()
    }

    if (QRScannerApplication.getInstance().isRequestInterstitialAd() && QRScannerApplication.getInstance().isLiveAds() && QRScannerApplication.getInstance().isEnableInterstitialAd() && !Utils.isHiddenAds()) {
        QRScannerApplication.getInstance().requestInterstitialAd()
    }

    if (QRScannerApplication.getInstance().isBackupSmallView() && QRScannerApplication.getInstance().isLiveAds() && QRScannerApplication.getInstance().isEnableBackupSmallView() && !Utils.isHiddenAds()) {
        QRScannerApplication.getInstance().requestBackupSmallView(this)
    }

    if (QRScannerApplication.getInstance().isBackupLargeView() && QRScannerApplication.getInstance().isLiveAds() && QRScannerApplication.getInstance().isEnableBackupLargeView() && !Utils.isHiddenAds()) {
        QRScannerApplication.getInstance().requestBackupLargeView(this)
    }
    checkingShowAds()

    btnEnable.setOnClickListener {
        if (NetworkUtil.pingIpAddress(this)) {
            onShowConnectionAlert()
            return@setOnClickListener
        }
        Utils.Log(ServiceManager::class.java, "isSyncingData 74 " + ServiceManager.Companion.getInstance().isSyncingData())
        if (!ServiceManager.getInstance().isSyncingData()) {
            ServiceManager.getInstance().onPickUpNewEmail(this)
        } else {
            Utils.Log(ServiceManager::class.java, "isSyncingData 78 is running")
            requestSyncData()
        }
    }

    /*Press back button*/
    if (Build.VERSION.SDK_INT >= 33) {
        onBackInvokedDispatcher.registerOnBackInvokedCallback(
            OnBackInvokedDispatcher.PRIORITY_DEFAULT
        ) {
            showAds()
        }
    } else {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    showAds()
                }
            })
    }

    imgRemove.setOnClickListener {
        Navigator.onMoveProVersion(this)
    }
}

fun BackupActivity.showAds(){
    if (QRScannerApplication.getInstance().isRequestInterstitialAd() || Utils.isHiddenAds()){
        // Back is pressed... Finishing the activity
        finish()
    }else{
        QRScannerApplication.getInstance().loadInterstitialAd(this)
    }
}

fun BackupActivity.checkingShowAds(){
    viewModel.doShowAds().observe(this, Observer {
        doShowAds(it)
    })
}

private fun BackupActivity.setupViewModel() {
    viewModel = ViewModelProvider(
        this,
        ViewModelFactory()
    ).get(BackupViewModel::class.java)
}
