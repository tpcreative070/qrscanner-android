package tpcreative.co.qrscanner.ui.backup
import android.view.View
import androidx.core.text.HtmlCompat
import kotlinx.android.synthetic.main.activity_backup.*
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.BackupSingleton
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.controller.ServiceManager
import tpcreative.co.qrscanner.common.network.NetworkUtil
import tpcreative.co.qrscanner.helper.SQLiteHelper

fun BackupActivity.initUI(){
    TAG = this::class.java.simpleName
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
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
            val mTextSynced = String.format(getString(R.string.synced_data), mSaveSyncedList.size.toString() + "", mHistorySyncedList.size.toString() + "")
            tvUsedSpace?.text = HtmlCompat.fromHtml(mTextSynced, HtmlCompat.FROM_HTML_MODE_LEGACY)
            requestSyncData()
        }
    }
    if (NetworkUtil.pingIpAddress(this)) {
        onShowConnectionAlert()
    }

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
}