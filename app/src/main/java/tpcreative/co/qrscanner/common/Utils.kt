package tpcreative.co.qrscanner.common

import android.Manifest
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.RectF
import android.net.Uri
import android.util.TypedValue
import android.webkit.URLUtil
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.zxing.client.result.ParsedResultType
import com.journeyapps.barcodescanner.Size
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.tapadoo.alerter.Alerter
import tpcreative.co.qrscanner.BuildConfig
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.controller.PrefsController
import tpcreative.co.qrscanner.common.extension.getContext
import tpcreative.co.qrscanner.common.extension.toText
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.helper.SQLiteHelper
import tpcreative.co.qrscanner.model.*
import java.io.*
import java.net.URLEncoder
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt


object Utils {
    val TAG = Utils::class.java.simpleName
    const val CODE_EXCEPTION = 1111
    const val FORMAT_DISPLAY: String = "dd/MM/yyyy HH:mm:ss a"

    fun getUUId(): String? {
        return try {
            UUID.randomUUID().toString()
        } catch (e: Exception) {
            "" + System.currentTimeMillis()
        }
    }

    fun convertMillisecondsToDateTime(millisecond: Long): String? {
        val date = Date(millisecond)
        val dateFormat = SimpleDateFormat("EE dd MMM, yyyy HH:mm:ss a", Locale.getDefault())
        return dateFormat.format(date)
    }

    fun getCurrentDate(): String? {
        val date = Date()
        val dateFormat = SimpleDateFormat("EE dd MMM, yyyy", Locale.getDefault())
        return dateFormat.format(date)
    }

    private fun getCurrentDateTime(): String? {
        val date = Date()
        val dateFormat = SimpleDateFormat("EE dd MMM, yyyy HH:mm:ss a", Locale.getDefault())
        return dateFormat.format(date)
    }

    fun getCurrentDateTimeSort(): String? {
        val date = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(date)
    }

    fun getCurrentDatetimeEvent(): String? {
        val date = Date()
        val dateFormat = SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.getDefault())
        return dateFormat.format(date)
    }

    fun getCurrentDatetimeEvent(milliseconds: Long): String? {
        val date = Date(milliseconds)
        val dateFormat = SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.getDefault())
        return dateFormat.format(date)
    }

    fun checkCameraBack(context: Context?): Boolean {
        return context?.packageManager?.hasSystemFeature(
                        PackageManager.FEATURE_CAMERA_ANY) == true
    }

    fun checkCameraFront(context: Context?): Boolean {
        return context?.packageManager?.hasSystemFeature(
                        PackageManager.FEATURE_CAMERA_FRONT) == true
    }

    fun getMilliseconds(value: String?): Long {
        if (value == null) {
            return System.currentTimeMillis()
        }
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = dateFormat.parse(value)
            return date.time
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return System.currentTimeMillis()
    }

    fun getCurrentDateDisplay(value: String?): String? {
        if (value == null) {
            return getCurrentDateTime()
        }
        try {
            var dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = dateFormat.parse(value)
            dateFormat = SimpleDateFormat(FORMAT_DISPLAY, Locale.getDefault())
            return dateFormat.format(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return value
    }

    fun Log(TAG: String?, message: String?) {
        if (BuildConfig.DEBUG) {
            android.util.Log.d(TAG, message ?:"")
        }
    }

    fun <T> Log(clazz: Class<T>, content: Any?) {
        if (BuildConfig.DEBUG){
            if (content is String) {
                Log(clazz.simpleName, content)
            } else {
                Log(clazz.simpleName, Gson().toJson(content))
            }
        }
    }

    fun <T> Log(mClass: Class<T>, message: String) {
        if (BuildConfig.DEBUG) {
            android.util.Log.d(mClass.getSimpleName(), message)
        }
    }

    fun isDebug(): Boolean {
        return BuildConfig.DEBUG
    }

    fun isFreeRelease(): Boolean {
        return BuildConfig.APPLICATION_ID == QRScannerApplication.getInstance().getString(R.string.qrscanner_free_release)
    }

    fun copyToClipboard(copyText: String?) {
        val clipboard = QRScannerApplication.getInstance().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip: ClipData? = ClipData
                .newPlainText(QRScannerApplication.getInstance().getString(R.string.my_clipboad), copyText)
        if (clip != null) {
            clipboard.setPrimaryClip(clip)
        }
    }

    fun isMultipleScan(): Boolean {
        return PrefsController.getBoolean(QRScannerApplication.getInstance().getString(R.string.key_multiple_scan), false)
    }

    fun setMultipleScan(isValue: Boolean) {
        return PrefsController.putBoolean(QRScannerApplication.getInstance().getString(R.string.key_multiple_scan), isValue)
    }

    fun getMillisecondsNewUser(): Long {
        return PrefsController.getLong(QRScannerApplication.getInstance().getString(R.string.key_new_users_for_current_time), 0)
    }

    fun setMillisecondsNewUser(value: Long) {
        return PrefsController.putLong(QRScannerApplication.getInstance().getString(R.string.key_new_users_for_current_time), value)
    }

    fun getMillisecondsUpdatedApp(): Long {
        return PrefsController.getLong(QRScannerApplication.getInstance().getString(R.string.key_current_time_update_app), 0)
    }

    fun setMillisecondsUpdatedApp(value: Long) {
        return PrefsController.putLong(QRScannerApplication.getInstance().getString(R.string.key_current_time_update_app), value)
    }

    fun getCurrentCodeVersion() : Int {
        return PrefsController.getInt(QRScannerApplication.getInstance().getString(R.string.key_current_code_version), 0)
    }

    fun setCurrentCodeVersion(code : Int){
        PrefsController.putInt(QRScannerApplication.getInstance().getString(R.string.key_current_code_version), code)
    }

    fun setCurrentListThemeColor(value :  MutableList<Theme>){
        PrefsController.putString(QRScannerApplication.getInstance().getString(R.string.key_theme_list), Gson().toJson(value))
    }

    fun onCheckingNewApp(){
        try {
            val value = getCurrentListThemeColor()
            val currentCodeVersion: Int = getCurrentCodeVersion()
            return if (value!=null && getMillisecondsUpdatedApp() > 0 && currentCodeVersion == BuildConfig.VERSION_CODE) {
                Log(TAG, "Already install this version")
            } else {
                val mList: MutableList<Theme> = ArrayList(ThemeUtil.getThemeList())
                setCurrentListThemeColor(mList)
                setCurrentCodeVersion(BuildConfig.VERSION_CODE)
                setMillisecondsUpdatedApp(System.currentTimeMillis())
                Log(TAG, "New install this version")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getCurrentListThemeColor(): MutableList<Theme>? {
        try {
            val result: String? = PrefsController.getString(QRScannerApplication.getInstance().getString(R.string.key_theme_list), null)
            val listType = object : TypeToken<ArrayList<Theme>>() {}.type
            return Gson().fromJson<MutableList<Theme>?>(result, listType)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }


    fun isSkipDuplicates(): Boolean {
        return PrefsController.getBoolean(QRScannerApplication.Companion.getInstance().getString(R.string.key_skip_duplicates), false)
    }

    fun setSkipDuplicates(isValue : Boolean) {
        return PrefsController.putBoolean(QRScannerApplication.getInstance().getString(R.string.key_skip_duplicates), isValue)
    }

    fun generateEAN(barcode: String?): String? {
        var mBarcode = barcode
        var first = 0
        var second = 0
        if (mBarcode?.length == 7 || mBarcode?.length == 12) {
            var counter = 0
            while (counter < mBarcode.length - 1) {
                first += Integer.valueOf(mBarcode.substring(counter, counter + 1))
                counter++
                second += Integer.valueOf(mBarcode.substring(counter, counter + 1))
                counter++
            }
            second *= 3
            val total = second + first
            val roundedNum = ((total + 9) / 10 * 10).toFloat().roundToInt()
            mBarcode += (roundedNum - total).toString()
        }
        return mBarcode
    }

    fun generateRandomDigits(n: Int): Int {
        val m = Math.pow(10.0, (n - 1).toDouble()).toInt()
        return m + Random().nextInt(9 * m)
    }

    fun onSetCountRating(count: Int) {
        Log(TAG, "rating.......set $count")
        PrefsController.putInt(QRScannerApplication.getInstance().getString(R.string.key_count_rating), count)
    }

    fun onGetCountRating(): Int {
        return PrefsController.getInt(QRScannerApplication.getInstance().getString(R.string.key_count_rating), 0)
    }

    fun onIntro(intro: Boolean) {
        PrefsController.putBoolean(QRScannerApplication.getInstance().getString(R.string.key_intro), intro)
    }

    fun onIsIntro(): Boolean {
        return PrefsController.getBoolean(QRScannerApplication.getInstance().getString(R.string.key_intro), false)
    }

    fun getKeepAdsRefreshLatestTime() : Long {
        return PrefsController.getLong(QRScannerApplication.getInstance().getString(R.string.key_keep_ads_refresh_latest_time), 0)
    }

    fun setKeepAdsRefreshLatestTime(value : Long) {
        PrefsController.putLong(QRScannerApplication.getInstance().getString(R.string.key_keep_ads_refresh_latest_time), value)
    }

    fun getCodeContentByHistory(item: HistoryModel?): String? {
        /*Product id must be plus barcode format type*/
        var code : String? = ""
        var mData = ""
        if (item != null) {
            val mResult: ParsedResultType = item.createType?.let { ParsedResultType.valueOf(it) } ?: return null
            return when (mResult) {
                ParsedResultType.ADDRESSBOOK -> {
                    code = item.code
                    mResult.name + "-" + code
                }
                ParsedResultType.EMAIL_ADDRESS -> {
                    code = item.code
                    mData = mResult.name + "-" + code
                    mData
                }
                ParsedResultType.PRODUCT -> {
                    code = item.code
                    val barCodeType: String? = item.barcodeFormat
                    mData = mResult.name + "-" + barCodeType + "-" + code
                    mData
                }
                ParsedResultType.URI -> {
                    code = item.code
                    mData = mResult.name + "-" + code
                    mData
                }
                ParsedResultType.WIFI -> {
                    code = item.code
                    mData = mResult.name + "-" + code
                    mData
                }
                ParsedResultType.GEO -> {
                    code = item.code
                    mData = mResult.name + "-" + code
                    mData
                }
                ParsedResultType.TEL -> {
                    code = item.code
                    mData = mResult.name + "-" + code
                    mData
                }
                ParsedResultType.SMS -> {
                    code = item.code
                    mData = mResult.name + "-" + code
                    mData
                }
                ParsedResultType.CALENDAR -> {
                    code = item.code
                    mData = mResult.name + "-" + code
                    mData
                }
                ParsedResultType.ISBN -> {
                    code = item.textProductIdISNB
                    val barCodeType = item.barcodeFormat
                    mData = mResult.name + "-" + barCodeType + "-" + code
                    mData
                }
                else -> {
                    code = item.code
                    mData = mResult.name + "-" + code
                    mData
                }
            }
        }
        return null
    }

    fun getCodeContentByGenerate(item: SaveModel?): String? {
        /*Product id must be plus barcode format type*/
        var code : String? = ""
        var mData = ""
        if (item != null) {
            val mResult: ParsedResultType = item.createType?.let { ParsedResultType.valueOf(it) } ?: return null
            return when (mResult) {
                ParsedResultType.ADDRESSBOOK -> {
                    code = item.code
                    mResult.name + "-" + code
                }
                ParsedResultType.EMAIL_ADDRESS -> {
                    code = item.code
                    mData = mResult.name + "-" + code
                    mData
                }
                ParsedResultType.PRODUCT -> {
                    code = item.code
                    val barCodeType: String? = item.barcodeFormat
                    mData = mResult.name + "-" + barCodeType + "-" + code
                    mData
                }
                ParsedResultType.URI -> {
                    code = item.code
                    mData = mResult.name + "-" + code
                    mData
                }
                ParsedResultType.WIFI -> {
                    code = item.code
                    mData = mResult.name + "-" + code
                    mData
                }
                ParsedResultType.GEO -> {
                    code = item.code
                    mData = mResult.name + "-" + code
                    mData
                }
                ParsedResultType.TEL -> {
                    code = item.code
                    mData = mResult.name + "-" + code
                    mData
                }
                ParsedResultType.SMS -> {
                    code = item.code
                    mData = mResult.name + "-" + code
                    mData
                }
                ParsedResultType.CALENDAR -> {
                    code = item.code
                    mData = mResult.name + "-" + code
                    mData
                }
                ParsedResultType.ISBN -> {
                    code = item.code
                    val barCodeType = item.barcodeFormat
                    mData = mResult.name + "-" + barCodeType + "-" + code
                    mData
                }
                else -> {
                    code = item.code
                    mData = mResult.name + "-" + code
                    mData
                }
            }
        }
        return null
    }

    fun onDropDownAlert(activity: Activity?, content: String?) {
        Alerter.create(activity)
            .setTitle("Alert")
            .setText("$content")
            .setIcon(R.drawable.ic_warning)
            .setBackgroundColorRes(R.color.colorAccent) // or setBackgroundColorInt(Color.CYAN)
            .show()
    }

    fun filterDuplicationsSaveItems(list: MutableList<SaveModel>): MutableList<SaveModel> {
        val mMap: HashMap<String?, SaveModel?> = HashMap<String?, SaveModel?>()
        val mList: MutableList<SaveModel> = ArrayList<SaveModel>()
        for (index in list) {
            if (index.contentUnique.isNullOrEmpty()) {
                val mSave: SaveModel? = mMap[index.contentUnique]
                if (mSave == null) {
                    mMap[index.contentUnique] = index
                } else {
                    mList.add(index)
                }
            } else {
                val mCode = getCodeContentByGenerate(index)
                val mSave: SaveModel? = mMap[mCode]
                if (mSave == null) {
                    mMap[mCode] = index
                } else {
                    mList.add(index)
                }
            }
        }
        return mList
    }

    fun filterDuplicationsHistoryItems(list: MutableList<HistoryModel>): MutableList<HistoryModel> {
        val mMap: HashMap<String?, HistoryModel?> = HashMap<String?, HistoryModel?>()
        val mList: MutableList<HistoryModel> = ArrayList<HistoryModel>()
        for (index in list) {
            if (index.contentUnique.isNullOrEmpty()) {
                val mHistory: HistoryModel? = mMap[index.contentUnique]
                if (mHistory == null) {
                    mMap[index.contentUnique] = index
                } else {
                    mList.add(index)
                }
            } else {
                val mCode = getCodeContentByHistory(index)
                val mHistory: HistoryModel? = mMap[mCode]
                if (mHistory == null) {
                    mMap[mCode] = index
                } else {
                    mList.add(index)
                }
            }
        }
        return mList
    }

    fun checkHistoryItemToInsertToLocal(mSyncedList: MutableList<HistoryModel>): MutableList<HistoryModel> {
        /*Checking local items deleted*/
        val mHistoryMap = getHistoryDeletedMap()
        val mSyncedMap: MutableMap<String?, HistoryModel> = convertHistoryListToMap(SQLiteHelper.getHistoryList(true))
        val mList: MutableList<HistoryModel> = ArrayList<HistoryModel>()
        for (index in mSyncedList) {
            /*Checking item was deleted before*/
            val mValue = mHistoryMap.get(index.uuId)
            /*Checking item exiting before*/
            val mItem: HistoryModel? = mSyncedMap[index.uuId]
            if (mValue == null && mItem == null) {
                index.id = 0
                mList.add(index)
            }
        }
        return mList
    }

    fun checkSaveItemToInsertToLocal(mSyncedList: MutableList<SaveModel>): MutableList<SaveModel> {
        /*Checking local items deleted*/
        val mHistoryMap = getSaveDeletedMap()
        val mSyncedMap: MutableMap<String?, SaveModel> = convertSaveListToMap(SQLiteHelper.getSaveList(true))
        val mList: MutableList<SaveModel> = ArrayList<SaveModel>()
        for (index in mSyncedList) {
            /*Checking item was deleted before*/
            val mValue = mHistoryMap.get(index.uuId)
            /*Checking item exiting before*/
            val mItem: SaveModel? = mSyncedMap[index.uuId]
            if (mValue == null && mItem == null) {
                index.id = 0
                mList.add(index)
            }
        }
        return mList
    }

    fun checkSaveItemToUpdateToLocal(mSyncedList: MutableList<SaveModel>): MutableList<SaveModel> {
        /*Checking local items deleted*/
        val mSyncedMap: MutableMap<String?, SaveModel> = convertSaveListToMap(SQLiteHelper.getSaveList(true))
        val mList: MutableList<SaveModel> = ArrayList<SaveModel>()
        for (index in mSyncedList) {
            /*Checking item exiting before*/
            val mItem: SaveModel? = mSyncedMap[index.uuId]
            if ((mItem != null && index.contentUniqueForUpdatedTime != mItem.contentUniqueForUpdatedTime && getMilliseconds(index.updatedDateTime) > getMilliseconds(mItem.updatedDateTime)) || checkSaveFavoriteOrNotedUpdateToLocal(index,mItem)) {
                index.id = mItem?.id
                mList.add(index)
            }
        }
        return mList
    }

    fun checkHistoryItemToUpdateToLocal(mSyncedList: MutableList<HistoryModel>): MutableList<HistoryModel> {
        /*Checking local items deleted*/
        val mSyncedMap: MutableMap<String?, HistoryModel> = convertHistoryListToMap(SQLiteHelper.getHistoryList(true))
        val mList: MutableList<HistoryModel> = mutableListOf()
        for (index in mSyncedList) {
            /*Checking item exiting before*/
            val mItem: HistoryModel? = mSyncedMap[index.uuId]
            if ((mItem != null && index.contentUniqueForUpdatedTime != mItem.contentUniqueForUpdatedTime && getMilliseconds(index.updatedDateTime) > getMilliseconds(mItem.updatedDateTime)) || checkHistoryFavoriteOrNotedUpdateToLocal(index,mItem)) {
                index.id = mItem?.id
                mList.add(index)
            }
        }
        return mList
    }

    private fun checkHistoryFavoriteOrNotedUpdateToLocal(mGlobal : HistoryModel?, mLocal : HistoryModel?) : Boolean{
        if (((mGlobal?.favorite != mLocal?.favorite) || (mGlobal?.noted != mLocal?.noted)) && (mGlobal?.hiddenDatetime?.isNotEmpty()==true && getMilliseconds(mGlobal.hiddenDatetime) > getMilliseconds(mLocal?.hiddenDatetime))){
            return true
        }
        return false
    }

    private fun checkSaveFavoriteOrNotedUpdateToLocal(mGlobal : SaveModel?, mLocal : SaveModel?) : Boolean{
        if (((mGlobal?.favorite != mLocal?.favorite) || (mGlobal?.noted != mLocal?.noted)) && (mGlobal?.hiddenDatetime?.isNotEmpty()==true && getMilliseconds(mGlobal.hiddenDatetime) > getMilliseconds(mLocal?.hiddenDatetime))){
            return true
        }
        return false
    }

    fun checkHistoryDeleteSyncedLocal(mSyncedList: MutableList<HistoryModel>): MutableList<HistoryModel> {
        val mListResult: MutableList<HistoryModel> = mutableListOf()
        val mListLocal: MutableList<HistoryModel> = SQLiteHelper.getHistoryList(true)
        val mMap: MutableMap<String?, HistoryModel> = convertHistoryListToMap(mSyncedList)
        for (index in mListLocal) {
            val mValue: HistoryModel? = mMap.get(index.uuId)
            if (mValue == null) {
                mListResult.add(index)
            }
        }
        return mListResult
    }

    fun checkSaveDeleteSyncedLocal(mSyncedList: MutableList<SaveModel>): MutableList<SaveModel> {
        val mListResult: MutableList<SaveModel> = mutableListOf()
        val mListLocal: MutableList<SaveModel> = SQLiteHelper.getSaveList(true)
        val mMap: MutableMap<String?, SaveModel> = convertSaveListToMap(mSyncedList)
        for (index in mListLocal) {
            val mValue: SaveModel? = mMap.get(index.uuId)
            if (mValue == null) {
                mListResult.add(index)
            }
        }
        return mListResult
    }

    private fun getSaveDeletedMap(): MutableMap<String?, String?> {
        val mValue: String? = PrefsController.getString(QRScannerApplication.Companion.getInstance().getString(R.string.key_save_deleted_list), null)
        if (mValue != null) {
            val mData: MutableMap<String?, String?>? = Gson().fromJson(mValue, object : TypeToken<MutableMap<String?, String?>?>() {}.type)
            if (mData != null) {
                return mData
            }
        }
        return HashMap()
    }

    private fun getHistoryDeletedMap(): MutableMap<String?, String> {
        val mValue: String? = PrefsController.getString(QRScannerApplication.Companion.getInstance().getString(R.string.key_history_deleted_list), null)
        if (mValue != null) {
            val mData: MutableMap<String?, String>? = Gson().fromJson<MutableMap<String?, String>>(mValue, object : TypeToken<MutableMap<String?, String>>() {}.type)
            if (mData != null) {
                return mData
            }
        }
        return HashMap()
    }

    fun setSaveDeletedMap(item: SaveEntityModel?) {
        if (item?.isSynced ?: false) {
            val mMap = getSaveDeletedMap()
            mMap[item?.uuId] = item?.uuId
            PrefsController.putString(QRScannerApplication.Companion.getInstance().getString(R.string.key_save_deleted_list), Gson().toJson(mMap))
        }
    }

    fun setHistoryDeletedMap(item: HistoryEntityModel?) {
        if (item?.isSynced == true) {
            val mMap = getHistoryDeletedMap()
            mMap[item.uuId] = item.uuId ?: ""
            PrefsController.putString(QRScannerApplication.Companion.getInstance().getString(R.string.key_history_deleted_list), Gson().toJson(mMap))
        }
    }

    fun setDefaultSaveHistoryDeletedKey() {
        PrefsController.putString(QRScannerApplication.Companion.getInstance().getString(R.string.key_save_deleted_list), Gson().toJson(HashMap<String?, String?>()))
        PrefsController.putString(QRScannerApplication.Companion.getInstance().getString(R.string.key_history_deleted_list), Gson().toJson(HashMap<String?, String?>()))
    }

    fun cleanDataAlreadySynced() {
        val mSaveSyncedList: MutableList<SaveModel> = SQLiteHelper.getSaveList(true)
        val mHistorySyncedList: MutableList<HistoryModel> = SQLiteHelper.getHistoryList(true)
        for (index in mSaveSyncedList) {
            SQLiteHelper.onDelete(index)
        }
        for (index in mHistorySyncedList) {
            SQLiteHelper.onDelete(index)
        }
    }

    private fun convertSaveListToMap(list: MutableList<SaveModel>): MutableMap<String?, SaveModel> {
        val mMap: MutableMap<String?, SaveModel> = HashMap<String?, SaveModel>()
        for (index in list) {
            mMap[index.uuId] = index
        }
        return mMap
    }

    private fun convertHistoryListToMap(list: MutableList<HistoryModel>?): MutableMap<String?, HistoryModel> {
        val mMap: MutableMap<String?, HistoryModel> = HashMap<String?, HistoryModel>()
        if (list != null) {
            for (index in list) {
                mMap[index.uuId] = index
            }
        }
        return mMap
    }

    fun setAuthor(author: Author?) {
        PrefsController.putString(QRScannerApplication.Companion.getInstance().getString(R.string.key_author), Gson().toJson(author))
    }

    fun getAccessToken(): String? {
        val mAuthor: Author? = Author.getInstance()?.getAuthorInfo()
        if (mAuthor != null) {
            if (mAuthor.access_token != null) {
                return mAuthor.access_token
            }
        }
        return null
    }

    fun isConnectedToGoogleDrive(): Boolean {
        val mAuthor: Author? = Author.getInstance()?.getAuthorInfo()
        Utils.Log(TAG,"author ${Gson().toJson(mAuthor)}")
        return mAuthor?.isConnectedToGoogleDrive ?: false
    }

    fun isTurnedOnBackup(): Boolean {
        return PrefsController.getBoolean(QRScannerApplication.getInstance().getString(R.string.key_backup_data), false)
    }

    fun getDriveEmail(): String? {
        val mAuthor: Author? = Author.getInstance()?.getAuthorInfo()
        return mAuthor?.email
    }

    fun writeToJson(data: String?, file: File?): File? {
        try {
            //BufferedWriter for performance, true to set append to file flag
            val buf = BufferedWriter(FileWriter(file, false))
            buf.append(data)
            buf.newLine()
            buf.close()
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
        return file
    }

    fun setLastTimeSynced(value: String?) {
        PrefsController.putString(QRScannerApplication.Companion.getInstance().getString(R.string.key_last_time_synced), value)
    }

    fun getLastTimeSynced(): String? {
        return PrefsController.getString(QRScannerApplication.Companion.getInstance().getString(R.string.key_last_time_synced), "")
    }

    fun isRequestSyncData(): Boolean {
        return PrefsController.getBoolean(QRScannerApplication.getInstance().getString(R.string.key_is_request_sync), false)
    }

    fun setRequestSync(value: Boolean) {
        PrefsController.putBoolean(QRScannerApplication.getInstance().getString(R.string.key_is_request_sync), value)
    }

    fun setDoNoAskAgain(value: Boolean) {
        return PrefsController.putBoolean(QRScannerApplication.getInstance().getString(R.string.key_do_not_ask_again), value)
    }

    fun getDoNoAskAgain() : Boolean {
        return PrefsController.getBoolean(QRScannerApplication.getInstance().getString(R.string.key_do_not_ask_again), false)
    }

    fun setRequestHistoryReload(value: Boolean){
        PrefsController.putBoolean(QRScannerApplication.getInstance().getString(R.string.key_is_request_history_reload), value)
    }

    fun isRequestHistoryReload() : Boolean{
        return PrefsController.getBoolean(QRScannerApplication.getInstance().getString(R.string.key_is_request_history_reload), true)
    }

    fun setRequestSaverReload(value: Boolean){
        PrefsController.putBoolean(QRScannerApplication.getInstance().getString(R.string.key_is_request_saver_reload), value)
    }

    fun isRequestSaverReload() : Boolean{
       return PrefsController.getBoolean(QRScannerApplication.getInstance().getString(R.string.key_is_request_saver_reload), true)
    }

    fun setFrameRectPortrait(mRect : RectF){
        PrefsController.putString(QRScannerApplication.getInstance().getString(R.string.key_frame_rect_portrait),Gson().toJson(mRect))
    }

    private fun getFrameRectPortrait() : RectF?{
        val json =  PrefsController.getString(QRScannerApplication.getInstance().getString(R.string.key_frame_rect_portrait),null)
        return Gson().fromJson(json, RectF::class.java)
    }

    fun setFrameRectLandscape(mRect : RectF){
        PrefsController.putString(QRScannerApplication.getInstance().getString(R.string.key_frame_rect_landscape),Gson().toJson(mRect))
    }

    private fun getFrameRectLandscape() : RectF?{
        val json =  PrefsController.getString(QRScannerApplication.getInstance().getString(R.string.key_frame_rect_landscape),null)
        return Gson().fromJson(json, RectF::class.java)
    }

    fun getBeep():Boolean{
        return PrefsController.getBoolean(QRScannerApplication.getInstance().getString(R.string.key_beep), false)
    }

    fun getVibrate() : Boolean{
        return PrefsController.getBoolean(QRScannerApplication.getInstance().getString(R.string.key_vibrate), true)
    }

    fun isAutoComplete() : Boolean{
        return PrefsController.getBoolean(QRScannerApplication.getInstance().getString(R.string.key_scan_auto_complete), false)
    }

    fun setAutoComplete(isValue: Boolean) {
        return PrefsController.putBoolean(QRScannerApplication.getInstance().getString(R.string.key_scan_auto_complete), isValue)
    }

    fun setQRCodeThemePosition(position : Int){
        PrefsController.putInt(QRScannerApplication.getInstance().getString(R.string.key_theme_object), position)
    }

    fun getQRCodeThemePosition() : Int{
        return PrefsController.getInt(QRScannerApplication.getInstance().getString(R.string.key_theme_object), 0)
    }

    fun setLight(isLight : Boolean){
        PrefsController.putBoolean(R.string.key_is_light.toText(), isLight)
    }

    fun isLight() : Boolean{
        return PrefsController.getBoolean(R.string.key_is_light.toText(),false)
    }

    fun setCountContinueScan(count : Int){
        PrefsController.putInt(R.string.key_count_continue_scan.toText(), count)
    }

    fun getCountContinueScan() : Int{
        return PrefsController.getInt(R.string.key_count_continue_scan.toText(),0)
    }

    fun getFramePortraitSize() : Size? {
        val mRect = getFrameRectPortrait()
        mRect?.let { node ->
            val mWidth = node.right - node.left
            val mHeight = node.bottom - node.top
            return Size(mWidth.toInt(),mHeight.toInt())
        }
        return null
    }

    fun getFrameLandscapeSize() : Size? {
        val mRect = getFrameRectLandscape()
        mRect?.let { node ->
            val mWidth = node.right - node.left
            val mHeight = node.bottom - node.top
            return Size(mWidth.toInt(),mHeight.toInt())
        }
        return null
    }

    fun isEqualTimeSynced(value: String?): Boolean {
        return value == getLastTimeSynced()
    }

    fun isRealCheckedOut(orderId: String?): Boolean {
        return orderId?.contains("GPA") == true
    }

    fun getPositionTheme(): Int {
        return PrefsController.getInt(QRScannerApplication.getInstance().getString(R.string.key_position_theme), 0)
    }

    fun setPositionTheme(positionTheme: Int) {
        PrefsController.putInt(QRScannerApplication.getInstance().getString(R.string.key_position_theme), positionTheme)
    }

    fun getCurrentThemeName(): String? {
        val myResArray: Array<String?> = QRScannerApplication.getInstance().resources.getStringArray(R.array.themeEntryArray)
        return if (getPositionTheme() == 0) {
            myResArray[0]
        } else myResArray[1]
    }

    fun isAlreadyCheckout(): Boolean {
        return PrefsController.getBoolean(QRScannerApplication.getInstance().getString(R.string.key_already_checkout), false)
    }

    fun setCheckoutValue(value: Boolean) {
        PrefsController.putBoolean(QRScannerApplication.getInstance().getString(R.string.key_already_checkout), value)
    }

    fun isPremium() : Boolean{
        if (BuildConfig.APPLICATION_ID == R.string.qrscanner_pro_release.toText()){
            return true
        }
        return isAlreadyCheckout()
    }

    fun isHiddenAds(enumScreens: EnumScreens) : Boolean{
        when(enumScreens){
            EnumScreens.HELP_FEEDBACK_SMALL ->{
                if (Configuration.hiddenHelpFeedbackSmallAds){
                    return true
                }
            }
            EnumScreens.HELP_FEEDBACK_LARGE ->{
                if (Configuration.hiddenHelpFeedbackLargeAds){
                    return true
                }
            }
            EnumScreens.MAIN_SMALL ->{
                if (Configuration.hiddenMainSmallAds){
                   return true
                }
            }
            EnumScreens.MAIN_LARGE ->{
                if (Configuration.hiddenMainLargeAds){
                    return true
                }
            }
            EnumScreens.CREATE_SMALL ->{
                if (Configuration.hiddenCreateSmallAds){
                    return true
                }
            }
            EnumScreens.CREATE_LARGE ->{
                if (Configuration.hiddenCreateLargeAds){
                    return true
                }
            }
            EnumScreens.SCANNER_RESULT_SMALL ->{
                if (Configuration.hiddenScannerResultSmallAds){
                    return true
                }
            }
            EnumScreens.SCANNER_RESULT_LARGE ->{
                if (Configuration.hiddenScannerResultLargeAds){
                    return true
                }
            }
            EnumScreens.REVIEW_SMALL->{
                if (Configuration.hiddenReviewSmallAds){
                    return true
                }
            }
            EnumScreens.REVIEW_LARGE->{
                if (Configuration.hiddenReviewLargeAds){
                    return true
                }
            }
            EnumScreens.CHANGE_COLOR_SMALL ->{
                if (Configuration.hiddenChangeColorSmallAds){
                    return true
                }
            }
            EnumScreens.CHANGE_COLOR_LARGE ->{
                if (Configuration.hiddenChangeColorLargeAds){
                    return true
                }
            }
            EnumScreens.BACKUP_SMALL ->{
                if (Configuration.hiddenBackupSmallAds){
                    return true
                }
            }
            EnumScreens.BACKUP_LARGE ->{
                if (Configuration.hiddenBackupLargeAds){
                    return true
                }
            }
            else -> {}
        }
        return if (BuildConfig.APPLICATION_ID == R.string.qrscanner_free_release.toText()){
            (QRScannerApplication.getInstance().isHiddenFreeReleaseAds() || isPremium()) || !QRScannerApplication.getInstance().isLiveExpiredTimeForNewUser()
        } else if (BuildConfig.APPLICATION_ID == R.string.qrscanner_free_innovation.toText()){
            (QRScannerApplication.getInstance().isHiddenFreeInnovationAds() || isPremium()) || !QRScannerApplication.getInstance().isLiveExpiredTimeForNewUser()
        }
        else if (BuildConfig.APPLICATION_ID == R.string.super_qrscanner_free_innovation.toText()){
            (QRScannerApplication.getInstance().isHiddenSuperFreeInnovationAds() || isPremium()) || !QRScannerApplication.getInstance().isLiveExpiredTimeForNewUser()
        }
        else{
            isPremium()
        }
    }

    fun onAlertNotify(activity: Activity, message: String) {
        Alerter.create(activity)
                .setTitle(QRScannerApplication.getInstance().getString(R.string.alert))
                .setBackgroundColorInt(ContextCompat.getColor(activity, R.color.colorAccent))
                .setText(message)
                .show()
    }

    fun onSendMail(context: Context,create : GeneralModel?){
        val to = create?.email
        val subject = create?.subject
        val body = create?.message
        val mailTo = "mailto:" + to +
                "?&subject=" + Uri.encode(subject) +
                "&body=" + Uri.encode(body)
        val emailIntent = Intent(Intent.ACTION_VIEW)
        emailIntent.data = Uri.parse(mailTo)
        context.startActivity(emailIntent)
        Utils.Log(TAG, "email object ${Gson().toJson(create)}")
    }

    fun onPhoneCall(context: Context,create: GeneralModel?) {
        Dexter.withContext(context)
            .withPermissions(
                Manifest.permission.CALL_PHONE)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report?.areAllPermissionsGranted() == true) {
                        Utils.Log(TAG, "Action here phone call")
                        val intentPhoneCall = Intent(Intent.ACTION_CALL, Uri.parse("tel:" + create?.phone))
                        context.startActivity(intentPhoneCall)
                    } else {
                        Utils.Log(TAG, "Permission is denied")
                    }
                    // check for permanent denial of any permission
                    if (report?.isAnyPermissionPermanentlyDenied == true) {
                        /*Miss add permission in manifest*/
                        Utils.Log(TAG, "request permission is failed")
                    }
                }

                override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest?>?, token: PermissionToken?) {
                    /* ... */
                    token?.continuePermissionRequest()
                }
            })
            .withErrorListener { Utils.Log(TAG, "error ask permission") }.onSameThread().check()
    }

    fun onSendSMS(context: Context,phone : String?,message : String?){
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:$phone"))
        intent.putExtra("sms_body", message)
        context.startActivity(intent)
    }

    fun onShareText(context: Context,value: String?){
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type="text/plain"
        intent.putExtra(Intent.EXTRA_TEXT,value)
        context.startActivity(Intent.createChooser(intent, QRScannerApplication.getInstance().getString(R.string.share)))
    }

    fun onShareMap(context: Context,uri : String){
        val intentMap = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        intentMap.setClassName(
            "com.google.android.apps.maps",
            "com.google.android.maps.MapsActivity"
        )
        context.startActivity(intentMap)
    }

    fun getImageUri(bitmap : Bitmap?) : Uri? {
        val imageFolder = File(QRScannerApplication.getInstance().cacheDir,  Constant.images_folder)
        try {
            imageFolder.mkdirs()
            val file = File(imageFolder, "scanner.png")
            val outputStream = FileOutputStream(file)
            bitmap?.compress(Bitmap.CompressFormat.PNG, 80, outputStream)
            outputStream.flush()
            outputStream.close()
            bitmap?.recycle()
            return FileProvider.getUriForFile(QRScannerApplication.getInstance(), BuildConfig.APPLICATION_ID + ".provider", file)
        } catch (e: java.lang.Exception) {
            return null;
        }
    }

    fun onOpenWebSites(url: String?,activity: Activity) {
        var mUrl: String?
        mUrl = url
        if (!URLUtil.isHttpUrl(mUrl) && !URLUtil.isHttpsUrl(mUrl)){
            mUrl = "http://$url"
        }
        val i = Intent(Intent.ACTION_VIEW, Uri.parse(mUrl))
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        i.setPackage("com.android.chrome")
        try {
            activity.startActivity(i)
        } catch (e: ActivityNotFoundException) {
            // Chrome is probably not installed
            // Try with the default browser
            try {
                i.setPackage(null)
                activity.startActivity(i)
            } catch (ex: Exception) {
                ex.printStackTrace()
                Log(TAG,"Error message"+ ex.message)
                onAlertNotify(activity, "Can not open the link")
            }
            Log(TAG,"Error message"+ e.message)
            Log(TAG, e.message)
            e.printStackTrace()
        }
    }


    fun onSearchMarketPlace(url : String,context: Activity){
        try {
            val uri = Uri.parse(url)
            val i = Intent(Intent.ACTION_VIEW, uri)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            i.setPackage("com.android.chrome")
            try {
                context.startActivity(i)
            } catch (e: ActivityNotFoundException) {
                // Chrome is probably not installed
                // Try with the default browser
                try {
                    i.setPackage(null)
                    context.startActivity(i)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    Utils.onAlertNotify(context, "Can not open the link")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onSearch(query: String?,context: Activity) {
        try {
            val escapedQuery = URLEncoder.encode(query, "UTF-8")
            val uri = Uri.parse("https://www.google.com/search?q=$escapedQuery")
            val i = Intent(Intent.ACTION_VIEW, uri)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            i.setPackage("com.android.chrome")
            try {
                context.startActivity(i)
            } catch (e: ActivityNotFoundException) {
                // Chrome is probably not installed
                // Try with the default browser
                try {
                    i.setPackage(null)
                    context.startActivity(i)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    Utils.onAlertNotify(context, "Can not open the link")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onSentEmail(context: Context){
        try {
            val emailIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("mailto:${R.string.email_contact.toText()}")
                putExtra(Intent.EXTRA_SUBJECT, "QRScanner")
            }
            context.startActivity(Intent.createChooser(emailIntent, R.string.help_feedback.toText()))
        }catch (e: Exception){
            e.printStackTrace()
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:${R.string.email_contact.toText()}")
                putExtra(Intent.EXTRA_SUBJECT, "QRScanner")
            }
            context.startActivity(Intent.createChooser(emailIntent, R.string.help_feedback.toText()))
        }
    }


    fun checkPermission(permission:String): Boolean {
       if (ContextCompat.checkSelfPermission(QRScannerApplication.getInstance(),permission )
        == PackageManager.PERMISSION_GRANTED){
            return true
        }
        return false
    }

    private fun dpToPx(dp: Float, context: Context): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        ).toInt()
    }

    fun spToPx(sp: Float, context: Context): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            sp,
            context.resources.displayMetrics
        ).toInt()
    }

    fun dpToSp(dp: Float, context: Context): Int {
        return (dpToPx(dp, context) / context.resources.displayMetrics.scaledDensity).toInt()
    }

    fun watchYoutubeVideo(context: Context, id: String) {
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$id"))
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("http://www.youtube.com/watch?v=$id")
        )
        try {
            context.startActivity(appIntent)
        } catch (ex: ActivityNotFoundException) {
            context.startActivity(webIntent)
        }
    }

    fun isInnovation(): Boolean{
        if (BuildConfig.APPLICATION_ID == R.string.qrscanner_free_innovation.toText() || BuildConfig.APPLICATION_ID == R.string.super_qrscanner_free_innovation.toText()){
            return true
        }
        return false
    }

    fun getInAppId() : String{
        return if (isInnovation()){
            getContext().getString(R.string.innovation_lifetime)
        }else{
            getContext().getString(R.string.lifetime)
        }
    }

    interface UtilsListener {
        fun onSaved(path: String?, action: EnumAction?)
    }
}