package tpcreative.co.qrscanner.common
import android.Manifest
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.webkit.URLUtil
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.zxing.BarcodeFormat
import com.google.zxing.client.result.ParsedResultType
import com.journeyapps.barcodescanner.Size
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.snatik.storage.Storage
import com.tapadoo.alerter.Alerter
import tpcreative.co.qrscanner.BuildConfig
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.api.response.DriveResponse
import tpcreative.co.qrscanner.common.controller.PrefsController
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.helper.SQLiteHelper
import tpcreative.co.qrscanner.model.*
import java.io.*
import java.net.URLEncoder
import java.nio.charset.Charset
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil
import kotlin.math.exp
import kotlin.math.roundToInt

object Utils {
    val TAG = Utils::class.java.simpleName
    const val CODE_EXCEPTION = 1111
    const val mStandardSortedDateTime: String = "ddMMYYYYHHmmss"
    const val FORMAT_DISPLAY: String = "EE dd MMM, yyyy HH:mm:ss a"

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

    fun getCurrentDateTime(): String? {
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

    fun geTimeFileName(): String? {
        val millisecond = System.currentTimeMillis()
        val formatter = SimpleDateFormat(mStandardSortedDateTime)
        return formatter.format(Date(millisecond))
    }

    fun saveImage(finalBitmap: Bitmap?, enumAction: EnumAction?, type: String?, code: String?, listenner: UtilsListener?) {
        val root: String? = QRScannerApplication.getInstance().getPathFolder()
        val myDir = File(root)
        myDir.mkdirs()
        var fname = "Image_" + type + "_" + geTimeFileName() + ".png"
        fname = fname.replace("/", "")
        fname = fname.replace(":", "")
        val file = File(myDir, fname)
        try {
            Log(TAG, "path :" + file.absolutePath)
            val out = FileOutputStream(file)
            finalBitmap?.compress(Bitmap.CompressFormat.PNG, 90, out)
            out.flush()
            out.close()
            listenner?.onSaved(file.absolutePath, enumAction)
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
        return PrefsController.getBoolean(QRScannerApplication.Companion.getInstance().getString(R.string.key_multiple_scan), false)
    }

    fun isSkipDuplicates(): Boolean {
        return PrefsController.getBoolean(QRScannerApplication.Companion.getInstance().getString(R.string.key_skip_duplicates), false)
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

    fun checkSum(code: String?): Int {
        var `val` = 0
        for (i in 0 until (code?.length?.minus(1) ?: 0 )) {
            `val` += (code?.get(i).toString() + "").toInt() * if (i % 2 == 0) 1 else 3
        }
        return (10 - `val` % 10) % 10
    }

    fun checkGTIN(gtin: String?): Boolean {
        val checkDigitArray = intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
        val gTinMaths = intArrayOf(3, 1, 3, 1, 3, 1, 3, 1, 3, 1, 3, 1, 3, 1, 3, 1, 3)
        val barcodeArray: Array<String>? = gtin?.split("(?!^)".toRegex())?.toTypedArray()
        val gTinLength = gtin?.length ?: 0
        val modifier = 17 - (gTinLength - 1)
        val gTinCheckDigit = gtin?.substring(gTinLength - 1)?.toInt()
        var tmpCheckDigit = 0
        var tmpCheckSum = 0
        var i = 0
        var ii = 0

        // Run through and put digits into multiplication table
        i = 0
        while (i < gTinLength - 1) {
            checkDigitArray[modifier + i] = barcodeArray?.get(i)?.toInt() ?: 0 // Add barcode digits to Multiplication Table
            i++
        }

        // Calculate "Sum" of barcode digits
        ii = modifier
        while (ii < 17) {
            tmpCheckSum += checkDigitArray[ii] * gTinMaths[ii]
            ii++
        }

        // Difference from Rounded-Up-To-Nearest-10 - Fianl Check Digit Calculation
        tmpCheckDigit = ((ceil((tmpCheckSum.toFloat() / 10.toFloat()).toDouble()) * 10) - tmpCheckSum.toFloat()).toInt()

        // Check if last digit is same as calculated check digit
        return gTinCheckDigit == tmpCheckDigit
    }

    fun checkITF(gtin : String?) : Boolean{
        if (gtin?.length?.rem(2) ?: 0 ==0){
            return true
        }
        return  false
    }

    fun onLogAds(eventCode: String?): String? {
        val idAds: String = QRScannerApplication.Companion.getInstance().getString(R.string.admob_app_id)
        val banner_id: String = QRScannerApplication.Companion.getInstance().getString(R.string.banner_main)
        return "event-code:" + eventCode + "; id-ads:" + idAds + "; banner-id:" + banner_id + " ;app id: " + BuildConfig.APPLICATION_ID + " ;variant: " + QRScannerApplication.Companion.getInstance().getString(R.string.qrscanner_free_release)
    }

    fun onSetCountRating(count: Int) {
        Log(TAG, "rating.......set$count")
        PrefsController.putInt(QRScannerApplication.getInstance().getString(R.string.count_rating), count)
    }

    fun onGetCountRating(): Int {
        return PrefsController.getInt(QRScannerApplication.getInstance().getString(R.string.count_rating), 0)
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
            .setIcon(R.drawable.baseline_warning_white_24)
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
        if (((mGlobal?.favorite != mLocal?.favorite) || (mGlobal?.noted != mLocal?.noted)) &&  getMilliseconds(mGlobal?.hiddenDatetime) > getMilliseconds(mLocal?.hiddenDatetime)){
            return true
        }
        return false
    }

    private fun checkSaveFavoriteOrNotedUpdateToLocal(mGlobal : SaveModel?, mLocal : SaveModel?) : Boolean{
        if (((mGlobal?.favorite != mLocal?.favorite) || (mGlobal?.noted != mLocal?.noted)) && getMilliseconds(mGlobal?.hiddenDatetime) > getMilliseconds(mLocal?.hiddenDatetime)){
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

    fun getSaveDeletedMap(): MutableMap<String?, String?> {
        val mValue: String? = PrefsController.getString(QRScannerApplication.Companion.getInstance().getString(R.string.key_save_deleted_list), null)
        if (mValue != null) {
            val mData: MutableMap<String?, String?>? = Gson().fromJson(mValue, object : TypeToken<MutableMap<String?, String?>?>() {}.type)
            if (mData != null) {
                return mData
            }
        }
        return HashMap()
    }

    fun getHistoryDeletedMap(): MutableMap<String?, String> {
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

    fun convertSaveListToMap(list: MutableList<SaveModel>): MutableMap<String?, SaveModel> {
        val mMap: MutableMap<String?, SaveModel> = HashMap<String?, SaveModel>()
        for (index in list) {
            mMap[index.uuId] = index
        }
        return mMap
    }

    fun convertHistoryListToMap(list: MutableList<HistoryModel>?): MutableMap<String?, HistoryModel> {
        val mMap: MutableMap<String?, HistoryModel> = HashMap<String?, HistoryModel>()
        if (list != null) {
            for (index in list) {
                mMap[index.uuId] = index
            }
        }
        return mMap
    }

    fun logPath(): String {
        val storage: Storage = QRScannerApplication.getInstance().getStorage()
        return storage.externalStorageDirectory + "/logsData.txt"
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

    /*Merge list to hash map for upload, download and delete*/
    fun mergeListToHashMap(mList: MutableList<DriveResponse>?): MutableMap<String?, String>? {
        val map: MutableMap<String?, String> = HashMap()
        if (mList != null) {
            for (index in mList) {
                map[index.id] = index.id ?: ""
            }
        }
        return map
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

    fun setFrameRect(mRect : RectF){
        PrefsController.putString(QRScannerApplication.getInstance().getString(R.string.key_frame_rect),Gson().toJson(mRect))
    }

    private fun getFrameRect() : RectF?{
        val json =  PrefsController.getString(QRScannerApplication.getInstance().getString(R.string.key_frame_rect),null)
        return Gson().fromJson(json, RectF::class.java)
    }

    fun getBeep():Boolean{
        return PrefsController.getBoolean(QRScannerApplication.getInstance().getString(R.string.key_beep), true)
    }

    fun getVibrate() : Boolean{
        return PrefsController.getBoolean(QRScannerApplication.getInstance().getString(R.string.key_vibrate), false)
    }

    fun setQRCodeThemePosition(position : Int){
        PrefsController.putInt(QRScannerApplication.getInstance().getString(R.string.key_theme_object), position)
    }

    fun getQRCodeThemePosition() : Int{
        return PrefsController.getInt(QRScannerApplication.getInstance().getString(R.string.key_theme_object), 0)
    }


    fun getFrameSize() : Size? {
        val mRect = getFrameRect()
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
        return PrefsController.getInt(QRScannerApplication.Companion.getInstance().getString(R.string.key_position_theme), 0)
    }

    fun setPositionTheme(positionTheme: Int) {
        PrefsController.putInt(QRScannerApplication.Companion.getInstance().getString(R.string.key_position_theme), positionTheme)
    }

    fun getCurrentThemeName(): String? {
        val myResArray: Array<String?> = QRScannerApplication.getInstance().resources.getStringArray(R.array.themeEntryArray)
        return if (getPositionTheme() == 0) {
            myResArray[0]
        } else myResArray[1]
    }

    fun isAlreadyCheckout(): Boolean {
        return PrefsController.getBoolean(QRScannerApplication.Companion.getInstance().getString(R.string.key_already_checkout), false)
    }

    fun setCheckoutValue(value: Boolean) {
        PrefsController.putBoolean(QRScannerApplication.Companion.getInstance().getString(R.string.key_already_checkout), value)
    }

    fun onAlertNotify(activity: Activity, message: String) {
        Alerter.create(activity)
                .setTitle("Alert")
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
        context.startActivity(Intent.createChooser(intent, ConstantValue.SHARE))
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

    fun getCodeUri(code : String) : Uri? {
        val fileFolder = File(QRScannerApplication.getInstance().cacheDir,  Constant.files_folder)
        try {
            fileFolder.mkdirs()
            val file = File(fileFolder, "vcard.vcf")
            val outputStream = FileOutputStream(file)
            outputStream.write(code.toByteArray());
            outputStream.close()
            outputStream.flush()
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

    interface UtilsListener {
        fun onSaved(path: String?, action: EnumAction?)
    }
}