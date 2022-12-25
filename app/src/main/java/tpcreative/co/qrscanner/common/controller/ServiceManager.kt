package tpcreative.co.qrscanner.common.controller
import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.content.*
import android.os.IBinder
import android.widget.Toast
import co.tpcreative.supersafe.common.network.Resource
import co.tpcreative.supersafe.common.network.Status
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.zxing.client.result.ParsedResultType
import com.opencsv.CSVWriter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.api.requester.DriveService
import tpcreative.co.qrscanner.common.api.requester.UserService
import tpcreative.co.qrscanner.common.extension.setDisplayLatTimeSyncedCompletely
import tpcreative.co.qrscanner.common.presenter.BaseView
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.common.services.QRScannerService
import tpcreative.co.qrscanner.common.services.QRScannerService.LocalBinder
import tpcreative.co.qrscanner.helper.SQLiteHelper
import tpcreative.co.qrscanner.model.EnumFragmentType
import tpcreative.co.qrscanner.model.EnumStatus
import tpcreative.co.qrscanner.model.SyncDataModel
import tpcreative.co.qrscanner.viewmodel.DriveViewModel
import tpcreative.co.qrscanner.viewmodel.UserViewModel
import java.io.File
import java.io.FileWriter

class ServiceManager : BaseView<Any?> {
    private var myService: QRScannerService? = null
    private var mContext: Context? = null
    private var isDismiss = false
    private var isSyncingData = false
    private val driveViewModel = DriveViewModel(DriveService())
    private val userViewModel = UserViewModel(UserService())
    var myConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName?, binder: IBinder?) {
            Utils.Log(TAG, "connected")
            myService = (binder as LocalBinder?)?.getService()
            myService?.bindView(this@ServiceManager)
            getInstance().onPreparingSyncData(false)
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
            context?.startActivityForResult(intent, Navigator.REQUEST_CODE_EMAIL)
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
        mContext?.bindService(intent, myConnection, Context.BIND_AUTO_CREATE)
        Utils.Log(TAG, "onStartService")
    }

    fun onStartService() {
        if (myService == null) {
            doBindService()
        }
    }

    fun onStopService() {
        if (myService != null) {
            mContext?.unbindService(myConnection)
            myService = null
        }
    }

    fun getMyService(): QRScannerService? {
        return myService
    }

    private fun getString(res: Int): String {
        return QRScannerApplication.getInstance().getString(res)
    }

    /*Sync data*/
    fun onPreparingSyncData(isDismissApp: Boolean) {
        if (!Utils.isTurnedOnBackup()) {
            if (isDismissApp) {
                onDismissServices()
            }
            Utils.Log(TAG, "Backup status is turn off. Please turn on it don't lose data")
            return
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
            RefreshTokenSingleton.getInstance()?.onStart(ServiceManager::class.java)
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
        CoroutineScope(Dispatchers.IO).launch {
            getItemList()
        }
    }

    private suspend fun getItemList() {
        isSyncingData = true
        val mResult = driveViewModel.getListFiles()
        when (mResult.status) {
            Status.SUCCESS -> {
                val mData = mResult.data
                mData?.let { mDataList ->
                    if (mDataList.size == 0) {
                        val mResultUploadedData = driveViewModel.uploadData()
                        when (mResultUploadedData.status) {
                            Status.SUCCESS -> {
                                onUpdatedHistoryAndSaveToSyncedItem()
                            }
                            else -> {
                                Utils.Log(TAG, "Uploaded data item occurred issue")
                            }
                        }
                    } else {
                        val mId = mDataList[0]
                        val mResultDownload = driveViewModel.downLoadData(mId.id ?: "")
                        when (mResultDownload.status) {
                            Status.SUCCESS -> {
                                val mObject = mResultDownload.data
                                mObject?.let { mObjectDownloaded ->
                                    onCheckingDataToSyncToLocalDB(mObjectDownloaded)
                                    /*Final step sync upload file*/
                                    if (Utils.isEqualTimeSynced(mObjectDownloaded.updatedDateTime)) {
                                        Utils.Log(TAG, "The session of previous already synced")
                                        if (isDismiss) {
                                            onDismissServices()
                                        }
                                        isSyncingData = false
                                        Utils.Log(TAG, "isSyncingData 308 $isSyncingData")
                                    } else {
                                        Utils.Log(TAG, "Preparing delete old file...")
                                        Utils.Log(TAG, "Last time from cloud..." + mObjectDownloaded.updatedDateTime)
                                        Utils.Log(TAG, "Last time from local..." + Utils.getLastTimeSynced())
                                        val mResultDeletedFromCloud = driveViewModel.deletedItems(mDataList)
                                        when (mResultDeletedFromCloud.status) {
                                            Status.SUCCESS -> {
                                                val mResultUploadedData = driveViewModel.uploadData()
                                                when (mResultUploadedData.status) {
                                                    Status.SUCCESS -> {
                                                        onUpdatedHistoryAndSaveToSyncedItem()
                                                    }
                                                    else -> {
                                                        Utils.Log(TAG, "Uploaded data item occurred issue")
                                                    }
                                                }
                                            }
                                            else -> {
                                                Utils.Log(TAG, "Deleted item occurred issue")
                                                if (isDismiss) {
                                                    onDismissServices()
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            else -> {
                                Utils.Log(TAG, "Downloaded item occurred issue")
                            }
                        }
                    }
                }
            }
            else -> {
                Utils.Log(TAG, mResult.message)
            }
        }
        isSyncingData = false
        Utils.setDisplayLatTimeSyncedCompletely()
        Utils.Log(TAG, "Already synced completely")
    }

    suspend fun getDriveAbout(): Resource<Boolean> = withContext(Dispatchers.IO) {
        try {
            val mResult = driveViewModel.getDriveAbout()
            when (mResult.status) {
                Status.SUCCESS -> {
                    Utils.Log(TAG, "Fetch drive about completed")
                    mResult
                }
                else -> {
                    Utils.Log(TAG, "Fetch drive about issue ${mResult.message}")
                    Resource.error(mResult.code ?: Utils.CODE_EXCEPTION, mResult.message
                            ?: "", null)
                }
            }
        } catch (e: Exception) {
            Resource.error(Utils.CODE_EXCEPTION, e.message ?: "", null)
        }
    }

    fun updatedDriveAccessToken() = CoroutineScope(Dispatchers.IO).launch {
        RefreshTokenSingleton.getInstance()?.onStart(ServiceManager::class.java)
    }
    /*Checking data to insert to local db*/
    private suspend fun onCheckingDataToSyncToLocalDB(mObject: SyncDataModel?) = withContext(Dispatchers.IO) {
        val mSaveList = mObject?.saveList ?: mutableListOf()
        val mHistoryList = mObject?.historyList ?: mutableListOf()

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

        /*Inserting to local db*/
        for (index in mSaveAddResultList) {
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
    }

    /*Updated history and save after upload file*/
    private suspend fun onUpdatedHistoryAndSaveToSyncedItem() = withContext(Dispatchers.IO) {
        val mSaveList = SQLiteHelper.getSaveList(false)
        val mHistoryList = SQLiteHelper.getHistoryList(false)
        for (index in mSaveList) {
            if (index.isSynced != true) {
                index.isSynced = true
                SQLiteHelper.onUpdate(index, false)
            }
        }
        for (index in mHistoryList) {
            if (index.isSynced != true) {
                index.isSynced = true
                SQLiteHelper.onUpdate(index, false)
            }
        }
        /*Final step*/if (isDismiss) {
        onDismissServices()
        isSyncingData = false
        Utils.Log(TAG, "isSyncingData 337 $isSyncingData")
    } else {
        SaveSingleton.getInstance()?.reloadData()
        HistorySingleton.getInstance()?.reloadData()
        BackupSingleton.getInstance()?.reloadData()
        isSyncingData = false
        Utils.Log(TAG, "isSyncingData 343 $isSyncingData")
        Utils.Log(TAG, "Syncing data completed")
    }
    }

    fun onDismissServices() {
        onStopService()
        if (myService != null) {
            myService?.unbindView()
        }
        isDismiss = false
        Utils.setDefaultSaveHistoryDeletedKey()
        driveViewModel.deleteTemporaryFiles()
        Utils.Log(TAG, "Dismiss Service manager")
    }

    override fun onError(message: String?, status: EnumStatus?) {
        Utils.Log(TAG, "onError response :" + message + " - " + status?.name)
    }

    override fun onSuccessful(message: String?) {
        Utils.Log(TAG, "onSuccessful Response  :$message")
    }

    override fun onStartLoading(status: EnumStatus?) {}
    override fun onStopLoading(status: EnumStatus?) {}
    override fun onError(message: String?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?, `object`: Any?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?, list: MutableList<Any?>?) {

    }

    override fun getContext(): Context {
        return QRScannerApplication.getInstance()
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
                ResponseSingleton.getInstance()?.onNetworkConnectionChanged(true)
            }
            else -> Utils.Log(TAG, "Nothing")
        }
    }

    suspend fun onExportDatabaseCSVTask(context : Context,enumFragmentType: EnumFragmentType?): Resource<String> {
        return withContext(Dispatchers.IO) {
            val imagefolder = File(context.cacheDir, "csvs")
            var file  : File? = null
            try {
                imagefolder.mkdirs()
                 file = File(imagefolder, enumFragmentType?.name + "_" + System.currentTimeMillis() + ".csv")
            } catch (e: java.lang.Exception) {
                Toast.makeText(context, "" + e.message, Toast.LENGTH_LONG).show()
            }
            var csvWrite: CSVWriter? = null
            try {
                csvWrite = CSVWriter(FileWriter(file?.absolutePath))
                when (enumFragmentType) {
                    EnumFragmentType.HISTORY -> {
                        val listHistory = SQLiteHelper.getHistoryList()
                        val arrStr1 = arrayOf<String?>(
                                getString(R.string.format_type),
                            getString(R.string.url),
                            getString(R.string.text),
                            getString(R.string.product_id),
                                "ISBN",
                            getString(R.string.phone),
                            getString(R.string.email),
                            getString(R.string.subject),
                            getString(R.string.message),
                            getString(R.string.latitude),
                            getString(R.string.longitude),
                            getString(R.string.place),
                            getString(R.string.title),
                            getString(R.string.location),
                            getString(R.string.description),
                            getString(R.string.csv_StartEvent),
                            getString(R.string.csv_EndEvent),
                            getString(R.string.fullName),
                            getString(R.string.address),
                                "SSId",
                            getString(R.string.password),
                            getString(R.string.networkEncryption),
                            getString(R.string.csv_CreatedDateTime))
                        csvWrite.writeNext(arrStr1)
                        for (index in listHistory) {
                            val value = arrayOf(
                                    index.createType,
                                    index.getUrls(ConstantValue.SEPARATORS_BREAK_LINE),
                                    if (index.createType.equals(ParsedResultType.TEXT.name, ignoreCase = true)) index.textProductIdISNB else "",
                                    if (index.createType.equals(ParsedResultType.PRODUCT.name, ignoreCase = true)) index.textProductIdISNB else "",
                                    if (index.createType.equals(ParsedResultType.ISBN.name, ignoreCase = true)) index.textProductIdISNB else "",
                                    index.getPhones(ConstantValue.SEPARATORS_BREAK_LINE),
                                    index.getEmails(ConstantValue.SEPARATORS_BREAK_LINE),
                                    index.subject,
                                    index.message,
                                    if (index.lat == 0.0) "" else index.lat.toString() + "",
                                    if (index.lon == 0.0) "" else index.lon.toString() + "",
                                    index.query,
                                    if (index.createType.equals(ParsedResultType.CALENDAR.name, ignoreCase = true)) index.title else "",
                                    index.location,
                                    index.description,
                                    if (index.startEvent == "") "" else Utils.getCurrentDatetimeEvent(index.startEventMilliseconds
                                            ?: 0),
                                    if (index.endEvent == "") "" else Utils.getCurrentDatetimeEvent(index.endEventMilliseconds
                                            ?: 0),
                                    index.getNames(),
                                    index.getAddresses(ConstantValue.SEPARATORS_BREAK_LINE),
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
                            getString(R.string.format_type),
                            getString(R.string.url),
                            getString(R.string.text),
                            getString(R.string.product_id),
                            "ISBN",
                            getString(R.string.phone),
                            getString(R.string.email),
                            getString(R.string.subject),
                            getString(R.string.message),
                            getString(R.string.latitude),
                            getString(R.string.longitude),
                            getString(R.string.place),
                            getString(R.string.title),
                            getString(R.string.location),
                            getString(R.string.description),
                            getString(R.string.csv_StartEvent),
                            getString(R.string.csv_EndEvent),
                            getString(R.string.fullName),
                            getString(R.string.address),
                            "SSId",
                            getString(R.string.password),
                            getString(R.string.networkEncryption),
                            getString(R.string.csv_CreatedDateTime)
                        )
                        csvWrite.writeNext(arrStr1)
                        for (index in listSaver) {
                            val value = arrayOf(
                                    index.createType,
                                    index.getUrls(ConstantValue.SEPARATORS_BREAK_LINE),
                                    if (index.createType.equals(ParsedResultType.TEXT.name, ignoreCase = true)) index.textProductIdISNB else "",
                                    if (index.createType.equals(ParsedResultType.PRODUCT.name, ignoreCase = true)) index.textProductIdISNB else "",
                                    if (index.createType.equals(ParsedResultType.ISBN.name, ignoreCase = true)) index.textProductIdISNB else "",
                                    index.getPhones(ConstantValue.SEPARATORS_BREAK_LINE),
                                    index.getEmails(ConstantValue.SEPARATORS_BREAK_LINE),
                                    index.subject,
                                    index.message,
                                    if (index.lat == 0.0) "" else index.lat.toString() + "",
                                    if (index.lon == 0.0) "" else index.lon.toString() + "",
                                    index.query,
                                    if (index.createType.equals(ParsedResultType.CALENDAR.name, ignoreCase = true)) index.title else "",
                                    index.location,
                                    index.description,
                                    if (index.startEvent == "") "" else Utils.getCurrentDatetimeEvent(index.startEventMilliseconds
                                            ?: 0),
                                    if (index.endEvent == "") "" else Utils.getCurrentDatetimeEvent(index.endEventMilliseconds
                                            ?: 0),
                                    index.getNames(),
                                    index.getAddresses(ConstantValue.SEPARATORS_BREAK_LINE),
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
                try {
                    csvWrite.flush()
                    csvWrite.close()
                    Resource.success(file?.absolutePath)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Resource.error(Utils.CODE_EXCEPTION, "${e.message}", null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Resource.error(Utils.CODE_EXCEPTION, "${e.message}", null)
            }
        }
    }

    interface ServiceManagerListener {
        fun onExportingSVCCompleted(path: String?)
    }

    interface ServiceManagerClickedListener {
        fun onYes()
        fun onNo()
    }

    interface ServiceManagerClickedItemsListener {
        fun onYes()
    }

    companion object {
        private val TAG = ServiceManager::class.java.simpleName
        private var instance: ServiceManager? = null
        fun getInstance(): ServiceManager {
            if (instance == null) {
                instance = ServiceManager()
            }
            return instance as ServiceManager
        }
    }
}