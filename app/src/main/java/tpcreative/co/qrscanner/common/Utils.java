package tpcreative.co.qrscanner.common;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.util.Log;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.client.result.ParsedResultType;
import com.snatik.storage.Storage;
import com.tapadoo.alerter.Alerter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import tpcreative.co.qrscanner.BuildConfig;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.api.response.DriveResponse;
import tpcreative.co.qrscanner.common.controller.PrefsController;
import tpcreative.co.qrscanner.common.services.QRScannerApplication;
import tpcreative.co.qrscanner.helper.SQLiteHelper;
import tpcreative.co.qrscanner.model.Author;
import tpcreative.co.qrscanner.model.EnumAction;
import tpcreative.co.qrscanner.model.HistoryEntityModel;
import tpcreative.co.qrscanner.model.HistoryModel;
import tpcreative.co.qrscanner.model.PremiumModel;
import tpcreative.co.qrscanner.model.SaveEntityModel;
import tpcreative.co.qrscanner.model.SaveModel;

public class Utils {
    private static final String TAG = Utils.class.getSimpleName();
    final static String mStandardSortedDateTime = "ddMMYYYYHHmmss";
    final static String FORMAT_DISPLAY = "EE dd MMM, yyyy HH:mm:ss a";
    final public static String GOOGLE_CONSOLE_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxToUe5+7Xy+Q7YYZfuMofqZmNe0021vMBJ32VQVPa8+Hd0z9YWPWTVvplslRX4rKU2TQ1l93yMzPVIHVxLIwPuo9OC9I8sO7LpOi91pyPk9fT0IjVaWDTSv1h/qLUE6m3OS5/LVPYQNbHCp3yqujSmj6bIj7AvbjhF36XjxZaESfJI3KhtXy/RD+ZaM255TgY6g1vwN3ObsrXZ3e98VrT8ehJrry8u8RTpiZ6NWTgcsk/riMPYZiwebf6fUHQgidAtwdBfZx94hYgldt5kPN3hB2LcG4KVj9jI2QY9Y4WsOPQ643I9fP8e9VbYW8/uAOTZnvUeUW9qb9qIw3NHyV6wIDAQAB";

    public static void writeLogs(String responseJson) {
        if (!BuildConfig.DEBUG){
            return;
        }
        if (ContextCompat.checkSelfPermission(QRScannerApplication.getInstance(),android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ) {
            appendLog(responseJson);
            Utils.Log(TAG,"write logs...");
        }
    }

    private static void appendLog(String text) {
        File logFile = new File(logPath());
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text+"\n");
            buf.newLine();
            buf.close();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static String getUUId(){
        try {
            return UUID.randomUUID().toString();
        }
        catch (Exception e){
            return ""+System.currentTimeMillis();
        }
    }

    public static String convertMillisecondsToHMmSs(long millisecond) {
        Date date = new Date(millisecond);
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss:SSS", Locale.getDefault());
        String dateFormatted = formatter.format(date);
        return dateFormatted;
    }

    public static String convertMillisecondsToHMS(long millisecond) {
        Date date = new Date(millisecond);
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss a", Locale.getDefault());
        String dateFormatted = formatter.format(date);
        Log.d(TAG, "Millisecond : " + millisecond + " data formatted :" + dateFormatted);
        return dateFormatted;
    }

    public static String convertMillisecondsToDateTime(long millisecond) {
        Date date = new Date(millisecond);
        SimpleDateFormat dateFormat = new SimpleDateFormat("EE dd MMM, yyyy HH:mm:ss a", Locale.getDefault());
        String result = dateFormat.format(date);
        return result;
    }

    public static String getCurrentDate() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EE dd MMM, yyyy", Locale.getDefault());
        String result = dateFormat.format(date);
        return result;
    }

    public static String getCurrentDateTime() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EE dd MMM, yyyy HH:mm:ss a", Locale.getDefault());
        String result = dateFormat.format(date);
        return result;
    }

    public static String getCurrentDateTimeSort() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String result = dateFormat.format(date);
        return result;
    }

    public static String getCurrentDatetimeEvent(){
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.getDefault());
        String result = dateFormat.format(date);
        return result;
    }

    public static String getCurrentDatetimeEvent(long milliseconds){
        Date date = new Date(milliseconds);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.getDefault());
        String result = dateFormat.format(date);
        return result;
    }

    public static boolean checkCameraBack(Context context) {
        if (context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA_ANY)) {
            return true;
        }
        return false;
    }

    public static boolean checkCameraFront(Context context) {
        if (context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA_FRONT)) {
            return true;
        }
        return false;
    }

    public static long getMilliseconds(String value){
        if (value==null){
            return System.currentTimeMillis();
        }
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = dateFormat.parse(value);
            return date.getTime();
        }catch (ParseException e){
            e.printStackTrace();
        }
        return System.currentTimeMillis();
    }

    public static String getCurrentDateDisplay(String value) {
        if (value==null){
            return getCurrentDateTime();
        }
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = dateFormat.parse(value);
            dateFormat = new SimpleDateFormat(FORMAT_DISPLAY,Locale.getDefault());
            String result = dateFormat.format(date);
            return result;
        }catch (ParseException e){
            e.printStackTrace();
        }
        return value;
    }

    private static boolean isDelimiter(char ch, char[] delimiters) {
        if (delimiters == null) {
            return Character.isWhitespace(ch);
        } else {
            char[] arr$ = delimiters;
            int len$ = delimiters.length;

            for (int i$ = 0; i$ < len$; ++i$) {
                char delimiter = arr$[i$];
                if (ch == delimiter) {
                    return true;
                }
            }

            return false;
        }
    }

    public static String geTimeFileName(){
        long millisecond = System.currentTimeMillis();
        SimpleDateFormat formatter = new SimpleDateFormat(mStandardSortedDateTime);
        String dateString = formatter.format(new Date(millisecond));
        return dateString;
    }

    public static void saveImage(final Bitmap finalBitmap,final EnumAction enumAction,final String type,final String code,UtilsListener listenner) {
        String root = QRScannerApplication.getInstance().getPathFolder();
        File myDir = new File(root);
        myDir.mkdirs();
        String fname = "Image_"+ type + "_"+geTimeFileName() +".jpg";
        fname = fname.replace("/","");
        fname = fname.replace(":","");
        File file = new File (myDir, fname);
        try {
            Log.d(TAG,"path :" + file.getAbsolutePath());
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            listenner.onSaved(file.getAbsolutePath(),enumAction);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String convertStringArrayToString(String[] strArr, String delimiter) {
        try {
            if (strArr==null){
                return "";
            }
            StringBuilder sb = new StringBuilder();
            for (String str : strArr)
                sb.append(str).append(delimiter);
            return sb.substring(0, sb.length() - 1);
        }catch (Exception e){

        }
        return "";
    }

    public interface UtilsListener {
        void onSaved(String path, EnumAction action);
    }

    public static void Log(final String TAG,final String message){
        if (BuildConfig.DEBUG){
            Log.d(TAG,message);
        }
    }

    public static <T>void Log(Class<T> mClass,final String message){
        if (BuildConfig.DEBUG){
            Log.d(mClass.getSimpleName(),message);
        }
    }


    public static boolean isDebug(){
        if (BuildConfig.DEBUG){
            return true;
        }
        return  false;
    }

    public static boolean isFreeRelease(){
        if (BuildConfig.APPLICATION_ID.equals(QRScannerApplication.getInstance().getString(R.string.qrscanner_free_release))){
            return true;
        }
        return false;
    }

    public static void copyToClipboard(String copyText) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager)
                QRScannerApplication.getInstance().getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData
                .newPlainText(QRScannerApplication.getInstance().getString(R.string.my_clipboad), copyText);
        clipboard.setPrimaryClip(clip);
    }

    public static void onObserveData(long second,Listener ls){
        Completable.timer(second, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                    @Override
                    public void onComplete() {
                        Utils.Log(TAG,"Completed");
                        ls.onStart();
                    }
                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    public static void onObserveVisitView(long second,DelayShowUIListener ls){
        Completable.timer(second, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                    @Override
                    public void onComplete() {
                        Utils.Log(TAG,"Completed");
                        ls.onSetVisitView();
                    }
                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    public static boolean isMultipleScan(){
        return PrefsController.getBoolean(QRScannerApplication.getInstance().getString(R.string.key_multiple_scan),false);
    }

    public static boolean isSkipDuplicates(){
        return PrefsController.getBoolean(QRScannerApplication.getInstance().getString(R.string.key_skip_duplicates),false);
    }

    public static String generateEAN(String barcode) {
        int first = 0;
        int second = 0;

        if(barcode.length() == 7 || barcode.length() == 12) {

            for (int counter = 0; counter < barcode.length() - 1; counter++) {
                first = (first + Integer.valueOf(barcode.substring(counter, counter + 1)));
                counter++;
                second = (second + Integer.valueOf(barcode.substring(counter, counter + 1)));
            }
            second = second * 3;
            int total = second + first;
            int roundedNum = Math.round((total + 9) / 10 * 10);

            barcode = barcode + String.valueOf(roundedNum - total);
        }
        return barcode;
    }

    public static int generateRandomDigits(int n) {
        int m = (int) Math.pow(10, n - 1);
        return m + new Random().nextInt(9 * m);
    }

    public static int checkSum(String code){
        int val=0;
        for(int i=0; i<code.length()-1; i++){
            val+=((int)Integer.parseInt(code.charAt(i)+""))*((i%2==0)?1:3);
        }
        int checksum_digit = (10 - (val % 10)) % 10;
        return checksum_digit;
    }

    public static boolean checkGTIN (String gtin) {
        int[] CheckDigitArray = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int[] gtinMaths       = {3, 1, 3, 1, 3, 1, 3, 1, 3, 1, 3, 1, 3, 1, 3, 1, 3};
        String[] BarcodeArray = gtin.split("(?!^)");
        int gtinLength = gtin.length();
        int modifier = (17 - (gtinLength - 1));
        int gtinCheckDigit = Integer.parseInt(gtin.substring(gtinLength - 1));
        int tmpCheckDigit = 0;
        int tmpCheckSum = 0;
        int tmpMath = 0;
        int i=0;
        int ii=0;

        // Run through and put digits into multiplication table
        for (i=0; i < (gtinLength - 1); i++) {
            CheckDigitArray[modifier + i] = Integer.parseInt(BarcodeArray[i]);  // Add barcode digits to Multiplication Table
        }

        // Calculate "Sum" of barcode digits
        for (ii=modifier; ii < 17; ii++) {
            tmpCheckSum += (CheckDigitArray[ii] * gtinMaths[ii]);
        }

        // Difference from Rounded-Up-To-Nearest-10 - Fianl Check Digit Calculation
        tmpCheckDigit = (int) ((Math.ceil((float) tmpCheckSum / (float) 10) * 10) - tmpCheckSum);

        // Check if last digit is same as calculated check digit
        if (gtinCheckDigit == tmpCheckDigit)
            return true;
        return false;
    }

    public static String onLogAds(String eventCode){
        String idAds = QRScannerApplication.getInstance().getString(R.string.admob_app_id);
        String banner_id = QRScannerApplication.getInstance().getString(R.string.banner_footer);
        return  "event-code:"+eventCode + "; id-ads:" + idAds + "; banner-id:" + banner_id + " ;app id: "+BuildConfig.APPLICATION_ID + " ;variant: "+ QRScannerApplication.getInstance().getString(R.string.qrscanner_free_release);
    }

    public static void onWriteLogs(Activity activity,String nameLogs,String errorCode){
        if (activity.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Utils.Log(TAG,"Granted permission....");
            final Storage storage = QRScannerApplication.getInstance().getStorage();
            if (storage!=null){
                storage.createFile(storage.getExternalStorageDirectory()+"/."+nameLogs,Utils.onLogAds(""+errorCode));
            }
        }else{
            Utils.Log(TAG,"No permission");
        }
    }

    public static boolean isPremium(){
        if (isProVersion()){
            return true;
        }
        Utils.Log(TAG,"isPremium");
        try{
            String value = PrefsController.getString(QRScannerApplication.getInstance().getString(R.string.key_is_premium),null);
            if (value!=null){
                final PremiumModel mPremium = new Gson().fromJson(value,PremiumModel.class);
                if (mPremium!=null){
                    return mPremium.isPremium;
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isProVersion(){
        if (BuildConfig.APPLICATION_ID.equals(QRScannerApplication.getInstance().getString(R.string.qrscanner_pro_release))){
            return true;
        }
        return false;
    }

    public static void setPremium(boolean isPremium){
        String value = PrefsController.getString(QRScannerApplication.getInstance().getString(R.string.key_is_premium),null);
        final PremiumModel mPremiumLocal = new PremiumModel(isPremium);
        if (value!=null){
            final PremiumModel mPremium = new Gson().fromJson(value,PremiumModel.class);
            if (mPremium!=null){
                mPremium.isPremium = isPremium;
                PrefsController.putString(QRScannerApplication.getInstance().getString(R.string.key_is_premium),new Gson().toJson(mPremium));
            }else{
                PrefsController.putString(QRScannerApplication.getInstance().getString(R.string.key_is_premium),new Gson().toJson(mPremiumLocal));
            }
        }else{
            PrefsController.putString(QRScannerApplication.getInstance().getString(R.string.key_is_premium),new Gson().toJson(mPremiumLocal));
        }
        Utils.Log(TAG,"setPremium");
    }

    public static void onSetCountRating(int count){
       PrefsController.putInt(QRScannerApplication.getInstance().getString(R.string.count_rating),count);
    }

    public static int onGetCountRating(){
        final int  mCountRating = PrefsController.getInt(QRScannerApplication.getInstance().getString(R.string.count_rating),0);
        return mCountRating;
    }

    public static void onScanFile(Context activity, String nameLogs){
        if (PermissionChecker.checkSelfPermission(activity,android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED) {
            Utils.Log(TAG,"Granted permission....");
            final Storage storage = QRScannerApplication.getInstance().getStorage();
            if (storage!=null){
                File file = new File(storage.getExternalStorageDirectory()+"/"+nameLogs);
                MediaScannerConnection.scanFile(activity, new String[]{file.getAbsolutePath()}, null, null);
                MediaScannerConnection.scanFile(activity, new String[]{storage.getExternalStorageDirectory()}, null, null);
                try {
                    storage.createFile(storage.getExternalStorageDirectory()+"/"+nameLogs,"");
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }else{
            Utils.Log(TAG,"No permission");
        }
    }

    public static String getCodeContentByHistory(final HistoryModel item){
        /*Product id must be plus barcode format type*/
        if (item!=null){
            final ParsedResultType mResult = ParsedResultType.valueOf(item.createType);
            if (mResult==null){
                return null;
            }
            switch (mResult){
                case ADDRESSBOOK:
                    String code = "MECARD:N:" + item.fullName + ";TEL:" + item.phone + ";EMAIL:" + item.email + ";ADR:" + item.address + ";";
                    String mData = mResult.name() +"-" +code;
                    return mData;
                case EMAIL_ADDRESS:
                    code = "MATMSG:TO:" + item.email + ";SUB:" + item.subject + ";BODY:" + item.message + ";";
                    mData = mResult.name() +"-" +code;
                    return mData;
                case PRODUCT:
                    code = item.text;
                    String barCodeType = item.barcodeFormat;
                    mData =  mResult.name() +"-"+barCodeType+"-"+code;
                    return mData;
                case URI:
                    code = item.url;
                    mData = mResult.name() +"-" +code;
                    return mData;
                case WIFI:
                    code = "WIFI:S:" + item.ssId + ";T:" + item.networkEncryption + ";P:" + item.password + ";H:" + item.hidden + ";";
                    mData = mResult.name() +"-" +code;
                    return mData;
                case GEO:
                    code = "geo:" + item.lat + "," + item.lon + "?q=" + item.query + "";
                    mData = mResult.name() +"-" +code;
                    return mData;
                case TEL:
                    code = "tel:" + item.phone + "";
                    mData = mResult.name() +"-" +code;
                    return mData;
                case SMS:
                    code = "smsto:" + item.phone + ":" + item.message;
                    mData = mResult.name() +"-" +code;
                    return mData;
                case CALENDAR:
                    StringBuilder builder = new StringBuilder();
                    builder.append("BEGIN:VEVENT");
                    builder.append("\n");
                    builder.append("SUMMARY:" + item.title);
                    builder.append("\n");
                    builder.append("DTSTART:" + item.startEvent);
                    builder.append("\n");
                    builder.append("DTEND:" + item.endEvent);
                    builder.append("\n");
                    builder.append("LOCATION:" + item.location);
                    builder.append("\n");
                    builder.append("DESCRIPTION:" + item.description);
                    builder.append("\n");
                    builder.append("END:VEVENT");
                    code = builder.toString();
                    mData = mResult.name() +"-" +code;
                    return mData;
                case ISBN:
                    code = item.text;
                    barCodeType = item.barcodeFormat;
                    mData =  mResult.name() +"-"+barCodeType+"-"+code;
                    return mData;
                default:
                    code = item.text;
                    mData = mResult.name() +"-" +code;
                    return mData;
            }
        }
        return null;
    }

    public static String getCodeContentByGenerate(final SaveModel item) {
        /*Product id must be plus barcode format type*/
        if (item != null) {
            final ParsedResultType mResult = ParsedResultType.valueOf(item.createType);
            if (mResult==null){
                return null;
            }
            switch (mResult) {
                case ADDRESSBOOK:
                    String code = "MECARD:N:" + item.fullName + ";TEL:" + item.phone + ";EMAIL:" + item.email + ";ADR:" + item.address + ";";
                    String mData = mResult.name() + "-" + code;
                    return mData;
                case EMAIL_ADDRESS:
                    code = "MATMSG:TO:" + item.email + ";SUB:" + item.subject + ";BODY:" + item.message + ";";
                    mData = mResult.name() + "-" + code;
                    return mData;
                case PRODUCT:
                    code = item.text;
                    String barCodeType = item.barcodeFormat;
                    mData =  mResult.name() +"-"+barCodeType+"-"+code;
                    return mData;
                case URI:
                    code = item.url;
                    mData = mResult.name() +"-" +code;
                    return mData;
                case WIFI:
                    code = "WIFI:S:" + item.ssId + ";T:" + item.networkEncryption + ";P:" + item.password + ";H:" + item.hidden + ";";
                    mData = mResult.name() +"-" +code;
                    return mData;
                case GEO:
                    code = "geo:" + item.lat + "," + item.lon + "?q=" + item.query + "";
                    mData = mResult.name() +"-" +code;
                    return mData;
                case TEL:
                    code = "tel:" + item.phone + "";
                    mData = mResult.name() +"-" +code;
                    return mData;
                case SMS:
                    code = "smsto:" + item.phone + ":" + item.message;
                    mData = mResult.name() +"-" +code;
                    return mData;
                case CALENDAR:
                    StringBuilder builder = new StringBuilder();
                    builder.append("BEGIN:VEVENT");
                    builder.append("\n");
                    builder.append("SUMMARY:" + item.title);
                    builder.append("\n");
                    builder.append("DTSTART:" + item.startEvent);
                    builder.append("\n");
                    builder.append("DTEND:" + item.endEvent);
                    builder.append("\n");
                    builder.append("LOCATION:" + item.location);
                    builder.append("\n");
                    builder.append("DESCRIPTION:" + item.description);
                    builder.append("\n");
                    builder.append("END:VEVENT");
                    code = builder.toString();
                    mData = mResult.name() +"-" +code;
                    return mData;
                case ISBN:
                    code = item.text;
                    barCodeType = item.barcodeFormat;
                    mData =  mResult.name() +"-"+barCodeType+"-"+code;
                    return mData;
                default:
                    code = item.text;
                    mData = mResult.name() + "-" + code;
                    return mData;
            }
        }
        return null;
    }


    public static void onDropDownAlert(Activity activity,String content){
        Alerter.create(activity)
       .setTitle("Alert")
                .setText(content)
                .setIcon(R.drawable.baseline_warning_white_24)
                .setBackgroundColorRes(R.color.colorAccent) // or setBackgroundColorInt(Color.CYAN)
                .show();
    }

    public static boolean isNotEmptyOrNull(String value) {
        if (value==null || value.equals("") || value.equals("null")){
            return false;
        }
        return  true;
    }

    public static List<SaveModel> filterDuplicationsSaveItems(List<SaveModel> list){
        HashMap<String,SaveModel> mMap = new HashMap<>();
        List<SaveModel> mList = new ArrayList<>();
        for (SaveModel index : list){
            if (Utils.isNotEmptyOrNull(index.contentUnique)){
                final SaveModel mSave = mMap.get(index.contentUnique);
                if (mSave==null){
                    mMap.put(index.contentUnique,index);
                }else{
                    mList.add(index);
                }
            }else {
                final String mCode = Utils.getCodeContentByGenerate(index);
                final SaveModel mSave = mMap.get(mCode);
                if (mSave==null){
                    mMap.put(mCode,index);
                }else{
                    mList.add(index);
                }
            }
        }
        return mList;
    }

    public static List<HistoryModel> filterDuplicationsHistoryItems(List<HistoryModel> list){
        HashMap<String,HistoryModel> mMap = new HashMap<>();
        List<HistoryModel> mList = new ArrayList<>();
        for (HistoryModel index : list){
            if (Utils.isNotEmptyOrNull(index.contentUnique)){
                final HistoryModel mHistory = mMap.get(index.contentUnique);
                if (mHistory==null){
                    mMap.put(index.contentUnique,index);
                }else{
                    mList.add(index);
                }
            }else{
                final String mCode = Utils.getCodeContentByHistory(index);
                final HistoryModel mHistory = mMap.get(mCode);
                if (mHistory==null){
                    mMap.put(mCode,index);
                }else{
                    mList.add(index);
                }
            }
        }
        return mList;
    }

    public static List<HistoryModel> checkHistoryItemToInsertToLocal(List<HistoryModel> mSyncedList){
        /*Checking local items deleted*/
        final Map<String,String> mHistoryMap = getHistoryDeletedMap();
        final Map<String,HistoryModel> mSyncedMap = convertHistoryListToMap(SQLiteHelper.getHistoryList(true));
        List<HistoryModel> mList = new ArrayList<>();
        for (HistoryModel index : mSyncedList){
            /*Checking item was deleted before*/
            final String mValue = mHistoryMap.get(index.uuId);
            /*Checking item exiting before*/
            final HistoryModel mItem = mSyncedMap.get(index.uuId);
            if (mValue==null && mItem == null){
                index.id = 0;
                mList.add(index);
            }
        }
        return mList;
    }

    public static List<SaveModel> checkSaveItemToInsertToLocal(List<SaveModel> mSyncedList){
        /*Checking local items deleted*/
        final Map<String,String> mHistoryMap = getSaveDeletedMap();
        final Map<String,SaveModel> mSyncedMap = convertSaveListToMap(SQLiteHelper.getSaveList(true));
        List<SaveModel> mList = new ArrayList<>();
        for (SaveModel index : mSyncedList){
            /*Checking item was deleted before*/
            final String mValue = mHistoryMap.get(index.uuId);
            /*Checking item exiting before*/
            final SaveModel mItem = mSyncedMap.get(index.uuId);
            if (mValue==null && mItem == null){
                index.id = 0;
                mList.add(index);
            }
        }
        return mList;
    }

    public static List<SaveModel> checkSaveItemToUpdateToLocal(List<SaveModel> mSyncedList){
        /*Checking local items deleted*/
        final Map<String,SaveModel> mSyncedMap = convertSaveListToMap(SQLiteHelper.getSaveList(true));
        List<SaveModel> mList = new ArrayList<>();
        for (SaveModel index : mSyncedList){
            /*Checking item exiting before*/
            final SaveModel mItem = mSyncedMap.get(index.uuId);
            if (mItem != null && !index.contentUniqueForUpdatedTime.equals(mItem.contentUniqueForUpdatedTime) && getMilliseconds(index.updatedDateTime) > getMilliseconds(mItem.updatedDateTime)){
                index.id = mItem.id;
                mList.add(index);
            }
        }
        return mList;
    }

    public static List<HistoryModel> checkHistoryItemToUpdateToLocal(List<HistoryModel> mSyncedList){
        /*Checking local items deleted*/
        final Map<String,HistoryModel> mSyncedMap = convertHistoryListToMap(SQLiteHelper.getHistoryList(true));
        List<HistoryModel> mList = new ArrayList<>();
        for (HistoryModel index : mSyncedList){
            /*Checking item exiting before*/
            final HistoryModel mItem = mSyncedMap.get(index.uuId);
            if (mItem != null && !index.contentUniqueForUpdatedTime.equals(mItem.contentUniqueForUpdatedTime) && getMilliseconds(index.updatedDateTime) > getMilliseconds(mItem.updatedDateTime)){
                index.id = mItem.id;
                mList.add(index);
            }
        }
        return mList;
    }

    public static List<HistoryModel> checkHistoryDeleteSyncedLocal(List<HistoryModel> mSyncedList){
        final List<HistoryModel> mListResult = new ArrayList<>();
        final List<HistoryModel> mListLocal = SQLiteHelper.getHistoryList(true);
        final Map<String,HistoryModel> mMap = convertHistoryListToMap(mSyncedList);
        for (HistoryModel index : mListLocal){
            final HistoryModel mValue = mMap.get(index.uuId);
            if (mValue==null){
                mListResult.add(index);
            }
        }
        return mListResult;
    }

    public static List<SaveModel> checkSaveDeleteSyncedLocal(List<SaveModel> mSyncedList){
        final List<SaveModel> mListResult = new ArrayList<>();
        final List<SaveModel> mListLocal = SQLiteHelper.getSaveList(true);
        final Map<String,SaveModel> mMap = convertSaveListToMap(mSyncedList);
        for (SaveModel index : mListLocal){
            final SaveModel mValue = mMap.get(index.uuId);
            if (mValue==null){
                mListResult.add(index);
            }
        }
        return mListResult;
    }

    public static Map<String,String> getSaveDeletedMap(){
        String mValue = PrefsController.getString(QRScannerApplication.getInstance().getString(R.string.key_save_deleted_list),null);
        if (mValue!=null){
            final Map<String,String> mData = new Gson().fromJson(mValue,new TypeToken<Map<String,String>>(){}.getType());
            if (mData!=null){
                return mData;
            }
        }
        return new HashMap<>();
    }

    public static Map<String,String> getHistoryDeletedMap(){
        String mValue = PrefsController.getString(QRScannerApplication.getInstance().getString(R.string.key_history_deleted_list),null);
        if (mValue!=null){
            final Map<String,String> mData = new Gson().fromJson(mValue,new TypeToken<Map<String,String>>(){}.getType());
            if (mData!=null){
                return mData;
            }
        }
        return new HashMap<>();
    }

    public static void setSaveDeletedMap(SaveEntityModel item){
        if (Utils.isPremium() && item.isSynced){
            Map<String,String> mMap = getSaveDeletedMap();
            mMap.put(item.uuId,item.uuId);
            PrefsController.putString(QRScannerApplication.getInstance().getString(R.string.key_save_deleted_list),new Gson().toJson(mMap));
        }
    }

    public static void setHistoryDeletedMap(HistoryEntityModel item){
        if (Utils.isPremium() && item.isSynced) {
            Map<String,String> mMap = getHistoryDeletedMap();
            mMap.put(item.uuId,item.uuId);
            PrefsController.putString(QRScannerApplication.getInstance().getString(R.string.key_history_deleted_list), new Gson().toJson(mMap));
        }
    }

    public static void setDefaultSaveHistoryDeletedKey(){
        PrefsController.putString(QRScannerApplication.getInstance().getString(R.string.key_save_deleted_list),new Gson().toJson(new HashMap<String,String>()));
        PrefsController.putString(QRScannerApplication.getInstance().getString(R.string.key_history_deleted_list), new Gson().toJson(new HashMap<String,String>()));
    }

    public static void cleanDataAlreadySynced(){
        final List<SaveModel> mSaveSyncedList = SQLiteHelper.getSaveList(true);
        final List<HistoryModel> mHistorySyncedList = SQLiteHelper.getHistoryList(true);
        for (SaveModel index : mSaveSyncedList){
            SQLiteHelper.onDelete(index);
        }
        for (HistoryModel index : mHistorySyncedList){
            SQLiteHelper.onDelete(index);
        }
    }

    public static Map<String,SaveModel> convertSaveListToMap(List<SaveModel> list){
        Map<String,SaveModel> mMap = new HashMap<>();
        for (SaveModel index: list){
            mMap.put(index.uuId,index);
        }
        return mMap;
    }

    public static Map<String,HistoryModel> convertHistoryListToMap(List<HistoryModel> list){
        Map<String,HistoryModel> mMap = new HashMap<>();
        for (HistoryModel index: list){
            mMap.put(index.uuId,index);
        }
        return mMap;
    }

    public static String logPath(){
        final Storage storage = QRScannerApplication.getInstance().getStorage();
        return storage.getExternalStorageDirectory()+"/logsData.txt";
    }

    public static void setAuthor(Author author){
        PrefsController.putString(QRScannerApplication.getInstance().getString(R.string.key_author),new Gson().toJson(author) );
    }

    public static String getAccessToken(){
        final Author mAuthor = Author.getInstance().getAuthorInfo();
        if (mAuthor!=null){
            if (mAuthor.access_token!=null){
                return mAuthor.access_token;
            }
        }
        return null;
    }

    public static boolean isConnectedToGoogleDrive(){
        final Author mAuthor = Author.getInstance().getAuthorInfo();
        if (mAuthor!=null){
            return mAuthor.isConnectedToGoogleDrive;
        }
       return false;
    }

    public static boolean isTurnedOnBackup(){
        return PrefsController.getBoolean(QRScannerApplication.getInstance().getString(R.string.key_backup_data),false);
    }

    public static String getDriveEmail(){
        final Author mAuthor = Author.getInstance().getAuthorInfo();
        if (mAuthor!=null){
            return mAuthor.email;
        }
        return null;
    }

    public static File writeToJson(String data, File file){
        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(file, false));
            buf.append(data);
            buf.newLine();
            buf.close();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return file;
    }

    /*Get the first of category data*/
    public static String getIndexOfHashMap(Map<String, String> mMapDelete){
        if (mMapDelete!=null){
            if (mMapDelete.size()>0){
                final String id = mMapDelete.get(mMapDelete.keySet().toArray()[0]);
                Utils.Log(TAG,"Id need to be deleting " + id);
                return  id;
            }
        }
        return null;
    }

    /*Delete hash map after delete Google drive or Server system*/
    public static boolean deletedIndexOfHashMap(String id, Map<String,String>map){
        try {
            if (map!=null){
                if (map.size()>0){
                    map.remove(id);
                    return  true;
                }
            }
        }
        catch (Exception e){
            Utils.Log(TAG,"Could not delete hash map==============================>");
        }
        return  false;
    }

    /*Merge list to hash map for upload, download and delete*/
    public static Map<String,String> mergeListToHashMap(List<DriveResponse>mList){
        Map<String,String> map = new HashMap<>();
        for (DriveResponse index : mList){
            map.put(index.id,index.id);
        }
        return map;
    }

    public static void setLastTimeSynced(String value){
        PrefsController.putString(QRScannerApplication.getInstance().getString(R.string.key_last_time_synced),value);
    }

    public static String getLastTimeSynced(){
        return PrefsController.getString(QRScannerApplication.getInstance().getString(R.string.key_last_time_synced),"");
    }

    public static boolean isRequestSyncData(){
        return PrefsController.getBoolean(QRScannerApplication.getInstance().getString(R.string.key_is_request_sync),false);
    }

    public static void setRequestSync(boolean value){
         PrefsController.putBoolean(QRScannerApplication.getInstance().getString(R.string.key_is_request_sync),value);
    }
    public static boolean isEqualTimeSynced(String value){
        if (value.equals(getLastTimeSynced())){
            return true;
        }
        return false;
    }

    public static boolean isRealCheckedOut(String orderId){
        if (orderId.contains("GPA")){
            return true;
        }
        return false;
    }

    public static int getPositionTheme(){
        return PrefsController.getInt(QRScannerApplication.getInstance().getString(R.string.key_position_theme),0);
    }

    public static void setPositionTheme(int positionTheme){
        PrefsController.putInt(QRScannerApplication.getInstance().getString(R.string.key_position_theme),positionTheme);
    }

    public static int getCurrentTheme(){
        if (Utils.getPositionTheme()==0){
            return R.style.LightDialogTheme;
        }
        return R.style.DarkDialogTheme;
    }

    public static String getCurrentThemeName(){
        String[] myResArray = QRScannerApplication.getInstance().getResources().getStringArray(R.array.themeEntryArray);
        if (Utils.getPositionTheme()==0){
            return myResArray[0];
        }
        return myResArray[1];
    }

    public static boolean isAlreadyCheckout(){
        return PrefsController.getBoolean(QRScannerApplication.getInstance().getString(R.string.key_already_checkout),false);
    }

    public static void setCheckoutValue(boolean value){
        PrefsController.putBoolean(QRScannerApplication.getInstance().getString(R.string.key_already_checkout),value);
    }

    public static boolean isLiveAds(){
        return false;
    }
}
