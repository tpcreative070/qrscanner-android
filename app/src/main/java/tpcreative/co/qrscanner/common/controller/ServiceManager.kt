package tpcreative.co.qrscanner.common.controller

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.content.*
import android.os.IBinder
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.gson.Gson
import com.google.zxing.client.result.ParsedResultType
import com.opencsv.CSVWriter
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.api.response.DriveResponse
import tpcreative.co.qrscanner.common.presenter.BaseView
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.common.services.QRScannerService
import tpcreative.co.qrscanner.common.services.QRScannerService.BaseListener
import tpcreative.co.qrscanner.common.services.QRScannerService.LocalBinder
import tpcreative.co.qrscanner.helper.SQLiteHelper
import tpcreative.co.qrscanner.model.EnumFragmentType
import tpcreative.co.qrscanner.model.EnumStatus
import tpcreative.co.qrscanner.model.SyncDataModel
import java.io.FileWriter
import java.util.*

class ServiceManager : BaseView<Any?> {
    private var myService: QRScannerService? = null
    private var mContext: Context? = null
    private var subscriptions: Disposable? = null
    private var mMapDelete: MutableMap<String?, String?>? = HashMap()
    private val mDriveIdList: MutableList<DriveResponse?>? = ArrayList()
    private var isDismiss = false
    private var isSyncingData = false
    var myConnection: ServiceConnection? = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName?, binder: IBinder?) {
            Utils.Log(TAG, "connected")
            myService = (binder as LocalBinder?).getService()
            myService.bindView(this@ServiceManager)
            getInstance().onPreparingSyncData(false)
            if (Utils.isProVersion() && !Utils.isAlreadyCheckout()) {
                getInstance().onCheckout()
            }
        }

        //binder comes from server to communicate with method's of
        override fun onServiceDisconnected(className: ComponentName?) {
            Utils.Log(TAG, "disconnected")
            myService = null
        }
    }

    fun isSyncingData(): Boolean {
        return isSyncingData
    }

    fun onPickUpNewEmail(context: Activity?) {
        try {
            var email = Utils.getDriveEmail()
            if (email == null) {
                email = "a@gmail.com"
            }
            val value = String.format(QRScannerApplication.Companion.getInstance().getString(R.string.choose_an_new_account))
            val account1 = Account(email, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE)
            val intent = AccountManager.newChooseAccountIntent(account1, null, arrayOf<String?>(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE), value, null, null, null)
            intent.putExtra("overrideTheme", 1)
            context.startActivityForResult(intent, Navigator.REQUEST_CODE_EMAIL)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    fun setContext(mContext: Context?) {
        this.mContext = mContext
    }

    private fun doBindService() {
        if (myService != null) {
            return
        }
        var intent: Intent? = null
        intent = Intent(mContext, QRScannerService::class.java)
        intent.putExtra(TAG, "Message")
        mContext.bindService(intent, myConnection, Context.BIND_AUTO_CREATE)
        Utils.Log(TAG, "onStartService")
    }

    fun onStartService() {
        if (myService == null) {
            doBindService()
        }
    }

    fun onStopService() {
        if (myService != null) {
            mContext.unbindService(myConnection)
            myService = null
        }
    }

    fun getMyService(): QRScannerService? {
        return myService
    }

    private fun getString(res: Int): String? {
        return QRScannerApplication.Companion.getInstance().getString(res)
    }

    /*Sync data*/
    fun onPreparingSyncData(isDismissApp: Boolean) {
        if (!Utils.isPremium()) {
            Utils.Log(TAG, "Please upgrade to premium version")
            if (isDismissApp) {
                onDismissServices()
            }
            return
        }
        if (Utils.isPremium()) {
            if (!Utils.isTurnedOnBackup()) {
                if (isDismissApp) {
                    onDismissServices()
                }
                Utils.Log(TAG, "Backup status is turn off. Please turn on it don't lose data")
                return
            }
        }
        if (myService == null) {
            Utils.Log(TAG, "Request service")
            onStartService()
            return
        }
        if (Utils.getAccessToken() == null) {
            Utils.Log(TAG, "Need to sign in with Google drive first")
            if (isDismissApp) {
                onDismissServices()
            }
            return
        }
        if (!Utils.isConnectedToGoogleDrive()) {
            Utils.Log(TAG, "Need to connect to Google drive")
            RefreshTokenSingleton.Companion.getInstance().onStart<ServiceManager?>(ServiceManager::class.java)
            if (isDismissApp) {
                onDismissServices()
            }
            return
        }
        if (isSyncingData) {
            Utils.Log(TAG, "Syncing data. Please wait...")
            return
        }
        Utils.Log(TAG, "Starting sync data")
        isDismiss = isDismissApp
        onGetItemList()
    }

    private fun onGetItemList() {
        isSyncingData = true
        Utils.Log(TAG, "isSyncingData 188 $isSyncingData")
        myService.getFileListInApp(object : BaseListener<DriveResponse?> {
            override fun onShowListObjects(list: MutableList<DriveResponse?>?) {
                Utils.Log(TAG, "Response data " + Gson().toJson(list))
                mDriveIdList.clear()
                mDriveIdList.addAll(list)
                if (mDriveIdList.size > 0) {
                    val mData = mDriveIdList.get(0)
                    onPreparingDownloadItemData(mData.id)
                } else {
                    onPreparingUploadItemData()
                }
            }

            override fun onShowObjects(`object`: DriveResponse?) {}
            override fun onError(message: String?, status: EnumStatus?) {
                isSyncingData = false
                Utils.Log(TAG, "isSyncingData 207 $isSyncingData")
                Utils.Log(TAG, "response error $message")
                when (status) {
                    EnumStatus.REQUEST_REFRESH_ACCESS_TOKEN -> RefreshTokenSingleton.Companion.getInstance().onStart<ServiceManager?>(ServiceManager::class.java)
                    else -> {
                    }
                }
            }

            override fun onSuccessful(message: String?, status: EnumStatus?) {}
        })
    }

    /*onPreparingDownload*/
    private fun onPreparingDownloadItemData(id: String?) {
        myService.onDownloadFile(id, object : BaseListener<SyncDataModel?> {
            override fun onShowListObjects(list: MutableList<SyncDataModel?>?) {
                Utils.Log(TAG, Gson().toJson(list))
            }

            override fun onShowObjects(`object`: SyncDataModel?) {
                Utils.Log(TAG, Gson().toJson(`object`))
                onCheckingDataToSyncToLocalDB(`object`)
            }

            override fun onError(message: String?, status: EnumStatus?) {
                Utils.Log(TAG, message)
                isSyncingData = false
                Utils.Log(TAG, "isSyncingData 401 $isSyncingData")
            }

            override fun onSuccessful(message: String?, status: EnumStatus?) {
                Utils.Log(TAG, message)
            }
        })
    }

    /*Checking data to insert to local db*/
    private fun onCheckingDataToSyncToLocalDB(mObject: SyncDataModel?) {
        val mSaveList = mObject.saveList
        val mHistoryList = mObject.historyList

        /*Checking data to insert to local db*/
        val mSaveAddResultList = Utils.checkSaveItemToInsertToLocal(mSaveList)
        val mHistoryAddResultList = Utils.checkHistoryItemToInsertToLocal(mHistoryList)

        /*Checking data to update to local db*/
        val mSaveUpdateResultList = Utils.checkSaveItemToUpdateToLocal(mSaveList)
        val mHistoryUpdateResultList = Utils.checkHistoryItemToUpdateToLocal(mHistoryList)

        /*Checking data to delete to local db*/
        val mSaveDeleteResultList = Utils.checkSaveDeleteSyncedLocal(mSaveList)
        val mHistoryDeleteResultList = Utils.checkHistoryDeleteSyncedLocal(mHistoryList)
        Utils.Log(TAG, "Some items of save need to be inserting " + mSaveAddResultList.size)
        Utils.Log(TAG, "Some items of history need to be inserting " + mHistoryAddResultList.size)
        Utils.Log(TAG, "Some items of save need to be updating " + mSaveUpdateResultList.size)
        Utils.Log(TAG, "Some items of history need to be updating " + mHistoryUpdateResultList.size)
        Utils.Log(TAG, "Some items of save need to be deleting " + mSaveDeleteResultList.size)
        Utils.Log(TAG, "Some items of history need to be deleting " + mHistoryDeleteResultList.size)

        /*Inserting to local db*/for (index in mSaveAddResultList) {
            SQLiteHelper.onInsert(index)
        }
        for (index in mHistoryAddResultList) {
            SQLiteHelper.onInsert(index)
        }

        /*Updating to local db*/for (index in mSaveUpdateResultList) {
            SQLiteHelper.onUpdate(index, true)
        }
        for (index in mHistoryUpdateResultList) {
            SQLiteHelper.onUpdate(index, true)
        }

        /*Deleting to local db*/for (index in mSaveDeleteResultList) {
            SQLiteHelper.onDelete(index)
        }
        for (index in mHistoryDeleteResultList) {
            SQLiteHelper.onDelete(index)
        }
        /*Final step sync upload file*/if (Utils.isEqualTimeSynced(mObject.updatedDateTime)) {
            Utils.Log(TAG, "The session of previous already synced")
            if (isDismiss) {
                onDismissServices()
            }
            isSyncingData = false
            Utils.Log(TAG, "isSyncingData 308 $isSyncingData")
        } else {
            Utils.Log(TAG, "Preparing delete old file...")
            Utils.Log(TAG, "Last time from cloud..." + mObject.updatedDateTime)
            Utils.Log(TAG, "Last time from local..." + Utils.getLastTimeSynced())
            onPreparingDeleteItemData()
        }
    }

    /*Updated history and save after upload file*/
    private fun onUpdatedHistoryAndSaveToSyncedItem() {
        val mSaveList = SQLiteHelper.getSaveList(false)
        val mHistoryList = SQLiteHelper.getHistoryList(false)
        for (index in mSaveList) {
            if (!index.isSynced) {
                index.isSynced = true
                SQLiteHelper.onUpdate(index, false)
            }
        }
        for (index in mHistoryList) {
            if (!index.isSynced) {
                index.isSynced = true
                SQLiteHelper.onUpdate(index, false)
            }
        }
        /*Final step*/if (isDismiss) {
            onDismissServices()
            isSyncingData = false
            Utils.Log(TAG, "isSyncingData 337 $isSyncingData")
        } else {
            SaveSingleton.Companion.getInstance().reloadData()
            HistorySingleton.Companion.getInstance().reloadData()
            BackupSingleton.Companion.getInstance().reloadData()
            isSyncingData = false
            Utils.Log(TAG, "isSyncingData 343 $isSyncingData")
            Utils.Log(TAG, "Syncing data completed")
        }
    }

    /*onPreparingDownload*/
    private fun onPreparingDeleteItemData() {
        if (mDriveIdList.size > 0) {
            mMapDelete.clear()
            mMapDelete = Utils.mergeListToHashMap(mDriveIdList)
            val id = Utils.getIndexOfHashMap(mMapDelete)
            if (id != null) {
                Utils.Log(TAG, "onPreparingDeleteItemData total: " + mMapDelete.size)
                onDeleteItemData(id)
            }
        } else {
            if (isDismiss) {
                onDismissServices()
            }
            isSyncingData = false
            Utils.Log(TAG, "isSyncingData 363 $isSyncingData")
            Utils.Log(TAG, "Not found data to delete")
        }
    }

    private fun onDeleteItemData(id: String?) {
        myService.onDeleteCloudItems(id, object : BaseListener<Any?> {
            override fun onShowListObjects(list: MutableList<*>?) {}
            override fun onShowObjects(`object`: Any?) {}
            override fun onError(message: String?, status: EnumStatus?) {
                Utils.Log(TAG, "$message: $id")
                if (status == EnumStatus.DELETING_NOT_FOUND_ID) {
                    if (Utils.deletedIndexOfHashMap(id, mMapDelete)) {
                        val id = Utils.getIndexOfHashMap(mMapDelete)
                        if (id != null) {
                            onDeleteItemData(id)
                        } else {
                            Utils.Log(TAG, "Deleted item completely")
                            onPreparingUploadItemData()
                        }
                    }
                }
            }

            override fun onSuccessful(message: String?, status: EnumStatus?) {
                Utils.Log(TAG, "$message: $id")
                if (status == EnumStatus.DELETED_SUCCESSFULLY) {
                    if (Utils.deletedIndexOfHashMap(id, mMapDelete)) {
                        val id = Utils.getIndexOfHashMap(mMapDelete)
                        if (id != null) {
                            onDeleteItemData(id)
                        } else {
                            Utils.Log(TAG, "Deleted item completely")
                            onPreparingUploadItemData()
                        }
                    }
                }
            }
        })
    }

    /*onPreparingDownload*/
    private fun onPreparingUploadItemData() {
        myService.onUploadFileInAppFolder(object : BaseListener<Any?> {
            override fun onShowListObjects(list: MutableList<*>?) {}
            override fun onShowObjects(`object`: Any?) {}
            override fun onError(message: String?, status: EnumStatus?) {
                Utils.Log(TAG, message)
                if (isDismiss) {
                    onDismissServices()
                }
                isSyncingData = false
                Utils.Log(TAG, "isSyncingData 431 $isSyncingData")
            }

            override fun onSuccessful(message: String?, status: EnumStatus?) {
                Utils.Log(TAG, message)
                if (status == EnumStatus.UPLOADED_SUCCESSFULLYY) {
                    onUpdatedHistoryAndSaveToSyncedItem()
                }
            }
        })
    }

    /*User info*/
    fun onAuthorSync() {
        if (myService != null) {
            myService.onSyncAuthor()
        } else {
            Utils.Log(TAG, "My services is null")
        }
    }

    /*Author info*/
    fun onCheckVersion() {
        if (myService != null) {
            myService.onCheckVersion()
        } else {
            Utils.Log(TAG, "My services is null")
        }
    }

    fun onDismissServices() {
        onStopService()
        if (myService != null) {
            myService.unbindView()
        }
        if (subscriptions != null) {
            subscriptions.dispose()
        }
        isDismiss = false
        Utils.setDefaultSaveHistoryDeletedKey()
        Utils.Log(TAG, "Dismiss Service manager")
    }

    fun onCheckout() {
        if (myService != null) {
            Utils.Log(TAG, "Call checkcout here")
            myService.onAddCheckout(object : BaseListener<Any?> {
                override fun onShowListObjects(list: MutableList<*>?) {}
                override fun onShowObjects(`object`: Any?) {}
                override fun onError(message: String?, status: EnumStatus?) {
                    Utils.Log(TAG, message)
                }

                override fun onSuccessful(message: String?, status: EnumStatus?) {
                    Utils.Log(TAG, message)
                }
            })
        }
    }

    override fun onError(message: String?, status: EnumStatus?) {
        Utils.Log(TAG, "onError response :" + message + " - " + status.name)
    }

    override fun onSuccessful(message: String?) {
        Utils.Log(TAG, "onSuccessful Response  :$message")
    }

    override fun onStartLoading(status: EnumStatus?) {}
    override fun onStopLoading(status: EnumStatus?) {}
    override fun onError(message: String?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?, `object`: Any?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?, list: MutableList<*>?) {}
    override fun getContext(): Context? {
        return QRScannerApplication.Companion.getInstance()
    }

    override fun getActivity(): Activity? {
        return null
    }

    override fun onSuccessful(message: String?, status: EnumStatus?) {
        when (status) {
            EnumStatus.CONNECTED -> {
                isSyncingData = false
                Utils.Log(TAG, "isSyncingData 551 $isSyncingData")
                Utils.Log(TAG, "Wifi connected changed")
                onPreparingSyncData(false)
                ResponseSingleton.Companion.getInstance().onNetworkConnectionChanged(true)
            }
        }
    }

    fun onExportDatabaseCSVTask(enumFragmentType: EnumFragmentType?, ls: ServiceManagerListener?) {
        subscriptions = Observable.create { subscriber: ObservableEmitter<Any?>? ->
            val path: String = QRScannerApplication.Companion.getInstance().getPathFolder() + "/" + enumFragmentType.name + "_" + System.currentTimeMillis() + ".csv"
            var csvWrite: CSVWriter? = null
            try {
                csvWrite = CSVWriter(FileWriter(path))
                when (enumFragmentType) {
                    EnumFragmentType.HISTORY -> {
                        val listHistory = SQLiteHelper.getHistoryList()
                        val arrStr1 = arrayOf<String?>(
                                "FormatType",
                                "Url",
                                "Text",
                                "ProductId",
                                "ISBN",
                                "Phone",
                                "Email",
                                "Subject",
                                "Message",
                                "Latitude",
                                "Longitude",
                                "Query",
                                "Title",
                                "Location",
                                "Description",
                                "StartEvent",
                                "EndEvent",
                                "FullName",
                                "Address",
                                "SSId",
                                "Password",
                                "NetworkEncryption",
                                "CreatedDateTime")
                        csvWrite.writeNext(arrStr1)
                        for (index in listHistory) {
                            val value = arrayOf(
                                    index.createType,
                                    index.url,
                                    if (index.createType.equals(ParsedResultType.TEXT.name, ignoreCase = true)) index.text else "",
                                    if (index.createType.equals(ParsedResultType.PRODUCT.name, ignoreCase = true)) index.text else "",
                                    if (index.createType.equals(ParsedResultType.ISBN.name, ignoreCase = true)) index.text else "",
                                    index.phone,
                                    index.email,
                                    index.subject,
                                    index.message,
                                    if (index.lat == 0.0) "" else index.lat.toString() + "",
                                    if (index.lon == 0.0) "" else index.lon.toString() + "",
                                    index.query,
                                    index.title,
                                    index.location,
                                    index.description,
                                    if (index.startEvent == "") "" else Utils.convertMillisecondsToDateTime(index.startEventMilliseconds),
                                    if (index.endEvent == "") "" else Utils.convertMillisecondsToDateTime(index.endEventMilliseconds),
                                    index.fullName,
                                    index.address,
                                    index.ssId,
                                    index.password,
                                    index.networkEncryption,
                                    index.createDatetime)
                            csvWrite.writeNext(value)
                        }
                    }
                    EnumFragmentType.SAVER -> {
                        val listSaver = SQLiteHelper.getSaveList()
                        val arrStr1 = arrayOf<String?>(
                                "FormatType",
                                "Url",
                                "Text",
                                "ProductId",
                                "ISBN",
                                "Phone",
                                "Email",
                                "Subject",
                                "Message",
                                "Latitude",
                                "Longitude",
                                "Query",
                                "Title",
                                "Location",
                                "Description",
                                "StartEvent",
                                "EndEvent",
                                "FullName",
                                "Address",
                                "SSId",
                                "Password",
                                "NetworkEncryption",
                                "CreatedDateTime"
                        )
                        csvWrite.writeNext(arrStr1)
                        for (index in listSaver) {
                            val value = arrayOf(
                                    index.createType,
                                    index.url,
                                    if (index.createType.equals(ParsedResultType.TEXT.name, ignoreCase = true)) index.text else "",
                                    if (index.createType.equals(ParsedResultType.PRODUCT.name, ignoreCase = true)) index.text else "",
                                    if (index.createType.equals(ParsedResultType.ISBN.name, ignoreCase = true)) index.text else "",
                                    index.phone,
                                    index.email,
                                    index.subject,
                                    index.message,
                                    if (index.lat == 0.0) "" else index.lat.toString() + "",
                                    if (index.lon == 0.0) "" else index.lon.toString() + "",
                                    index.query,
                                    index.title,
                                    index.location,
                                    index.description,
                                    if (index.startEvent == "") "" else Utils.convertMillisecondsToDateTime(index.startEventMilliseconds),
                                    if (index.endEvent == "") "" else Utils.convertMillisecondsToDateTime(index.endEventMilliseconds),
                                    index.fullName,
                                    index.address,
                                    index.ssId,
                                    index.password,
                                    index.networkEncryption,
                                    index.createDatetime)
                            csvWrite.writeNext(value)
                        }
                    }
                    else -> {
                        Utils.Log(TAG, "NoThing")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    subscriber.onNext(true)
                    subscriber.onComplete()
                    if (csvWrite != null) {
                        csvWrite.flush()
                        csvWrite.close()
                        ls.onExportingSVCCompleted(path)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .subscribe { response: Any? -> Utils.Log(TAG, "Exporting cvs done") }
    }

    interface ServiceManagerListener {
        open fun onExportingSVCCompleted(path: String?)
    }

    interface ServiceManagerClickedListener {
        open fun onYes()
        open fun onNo()
    }

    interface ServiceManagerClickedItemsListener {
        open fun onYes()
    }

    companion object {
        private val TAG = ServiceManager::class.java.simpleName
        private var instance: ServiceManager? = null
        fun getInstance(): ServiceManager? {
            if (instance == null) {
                instance = ServiceManager()
            }
            return instance
        }
    }
}