package tpcreative.co.qrscanner.common;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.StringRes;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.snatik.storage.helpers.SizeUnit;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import tpcreative.co.qrscanner.BuildConfig;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.controller.PrefsController;
import tpcreative.co.qrscanner.common.services.QRScannerApplication;
import tpcreative.co.qrscanner.model.EnumAction;
import tpcreative.co.qrscanner.model.Theme;
import tpcreative.co.qrscanner.model.ThemeUtil;

public class Utils {

    private static final String TAG = Utils.class.getSimpleName();
    private static final long flagValue = 7200000;
    private UtilsListener listenner;

    public static boolean mCreateAndSaveFileOverride(String fileName, String path_folder_name, String responseJson, boolean append) {
        final String newLine = System.getProperty("line.separator");
        try {
            File root = new File(path_folder_name + "/" + fileName);
            if (!root.exists()) {
                File parentFolder = new File(path_folder_name);
                if (!parentFolder.exists()) {
                    parentFolder.mkdirs();
                }
                root.createNewFile();
            }
            FileWriter file = new FileWriter(root, append);
            file.write("\r\n");
            file.write(responseJson);
            file.write("\r\n");

            file.flush();
            file.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void showGotItSnackbar(final View view, final @StringRes int text) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                multilineSnackbar(
                        Snackbar.make(
                                view, text, BaseTransientBottomBar.LENGTH_INDEFINITE)
                                .setAction(R.string.got_it, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                    }
                                })
                ).show();
            }
        }, 200);
    }

    public static void showGotItSnackbar(final View view, final String text) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                multilineSnackbar(
                        Snackbar.make(
                                view, text, BaseTransientBottomBar.LENGTH_INDEFINITE)
                                .setAction(R.string.got_it, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                    }
                                })
                ).show();
            }
        }, 200);
    }

    private static Snackbar multilineSnackbar(Snackbar snackbar) {
        TextView textView = (TextView) snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setMaxLines(5);
        textView.setTextSize(15);

        TextView snackbarActionTextView = (TextView) snackbar.getView().findViewById( com.google.android.material.R.id.snackbar_action);
        snackbarActionTextView.setTextSize(14);
        return snackbar;
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
                PackageManager.FEATURE_CAMERA)) {
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

    private static int screenWidth = 0;
    private static int screenHeight = 0;

    public static void hideSoftKeyboard(Activity context) {
        View view = context.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static int getScreenHeight(Context c) {
        if (screenHeight == 0) {
            WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            screenHeight = size.y;
        }

        return screenHeight;
    }

    public static int getScreenWidth(Context c) {
        if (screenWidth == 0) {
            WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            screenWidth = size.x;
        }

        return screenWidth;
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static void hideSoftKeyboardFragment(Activity context) {
        if (context != null) {
            context.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }
    }

    public static void showSoftKeyboardFragment(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    public static void showSoftKeyboard(final Context context,final View view) {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 300);

    }

    public static void hideSoftKeyboard(final Context context,final View view) {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }, 300);

    }

    public static double convertDollarsToCents(double dollars){
        return dollars * 100;
    }


    public static double convertCentsToDollars(double cents){
        return cents / 100;
    }

    @SuppressLint("HardwareIds")
    public static String getDeviceSerial(Context context) {
        return Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    @SuppressLint("SimpleDateFormat")
    public static String formatDateISO(Date date){
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd"); // Quoted "Z" to indicate UTC, no timezone offset
        df.setTimeZone(tz);
        return df.format(date);
    }

    @SuppressLint("SimpleDateFormat")
    public static Date convertStringToDate(String value){
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            date = format.parse(value);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return date;
    }

    public static String capitalize(String str) {
        return capitalize(str, (char[]) null);
    }

    public static String capitalize(String str, char... delimiters) {
        int delimLen = delimiters == null ? -1 : delimiters.length;
        if (!TextUtils.isEmpty(str) && delimLen != 0) {
            char[] buffer = str.toCharArray();
            boolean capitalizeNext = true;

            for (int i = 0; i < buffer.length; ++i) {
                char ch = buffer[i];
                if (isDelimiter(ch, delimiters)) {
                    capitalizeNext = true;
                } else if (capitalizeNext) {
                    buffer[i] = Character.toTitleCase(ch);
                    capitalizeNext = false;
                }
            }

            return new String(buffer);
        } else {
            return str;
        }
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

    public static String getEmijoByUnicode(int unicode){
        return new String(Character.toChars(unicode));
    }

    public static byte[] readFully(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for (int count; (count = in.read(buffer)) != -1; ) {
            out.write(buffer, 0, count);
        }
        return out.toByteArray();
    }

    public static double getDecimalFormat(double aDouble){
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.US);
        otherSymbols.setDecimalSeparator('.');
        otherSymbols.setGroupingSeparator(',');
        DecimalFormat format = new DecimalFormat("#.##", otherSymbols);
        return Double.valueOf(format.format(Math.round(aDouble*1e5)/1e5));
    }

    public static Drawable getDrawable(String normal, String selected,final int[] colors) {
        StateListDrawable states = new StateListDrawable() {
            @Override
            protected boolean onStateChange(int[] states) {
                if (colors != null) {
                    boolean isSelected = false;
                    for (int state : states) {
                        if (state == android.R.attr.state_selected) {
                            isSelected = true;
                        }
                    }
                    if (isSelected)
                        setColorFilter(colors[0], PorterDuff.Mode.SRC_ATOP);
                    else {
                        clearColorFilter();
                        setColorFilter(colors[1], PorterDuff.Mode.SRC_ATOP);
                    }
                }
                return super.onStateChange(states);
            }
        };
        Drawable selectedDrawable = Drawable.createFromPath(selected);
        Drawable normalDrawable = Drawable.createFromPath(normal);

        states.addState(new int[]{android.R.attr.state_selected}, selectedDrawable);
        states.addState(new int[]{}, normalDrawable);
        return states;
    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }


    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }


    public static void saveImage(final Bitmap finalBitmap,final EnumAction enumAction,final String type,final String code,UtilsListener listenner) {
        String root = QRScannerApplication.getInstance().getPathFolder();
        File myDir = new File(root);
        myDir.mkdirs();
        String fname = "Image_"+ type + code +".jpg";
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

    public static Bitmap changeBitmapColor(Bitmap sourceBitmap, int color) {
        try{
            Bitmap resultBitmap = sourceBitmap.copy(sourceBitmap.getConfig(),true);
            Paint paint = new Paint();
            ColorFilter filter = new LightingColorFilter(color, 1);
            paint.setColorFilter(filter);
            Canvas canvas = new Canvas(resultBitmap);
            canvas.drawBitmap(resultBitmap, 0, 0, paint);
            return resultBitmap;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static String convertStringArrayToString(String[] strArr, String delimiter) {
        if (strArr==null){
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String str : strArr)
            sb.append(str).append(delimiter);
        return sb.substring(0, sb.length() - 1);
    }

    public interface UtilsListener {
        void onSaved(String path, EnumAction action);
    }

    public static void Log(final String TAG,final String message){
        if (BuildConfig.DEBUG){
            Log.d(TAG,message);
        }
    }

    public static void copyToClipboard(String copyText) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager)
                QRScannerApplication.getInstance().getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData
                .newPlainText(QRScannerApplication.getInstance().getString(R.string.my_clipboad), copyText);
        clipboard.setPrimaryClip(clip);
    }


    public static void onUpdateVersionRateAlert(){
        try{
            final int current_code_version = PrefsController.getInt(QRScannerApplication.getInstance().getString(R.string.key_current_code_version),0);
            if (current_code_version != BuildConfig.VERSION_CODE){
                PrefsController.putBoolean(QRScannerApplication.getInstance().getString(R.string.we_are_a_team),false);
            }
        }
        catch (Exception e){
                e.printStackTrace();
        }
    }

    public static boolean isShowAds(){
        long currentValue = System.currentTimeMillis();
        long previousValue = PrefsController.getLong(QRScannerApplication.getInstance().getString(R.string.key_waiting_for_show_ads),0);
        if (currentValue>previousValue){
            PrefsController.putLong(QRScannerApplication.getInstance().getString(R.string.key_waiting_for_show_ads),currentValue+flagValue);
            return true;
        }
        return false;
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

}
