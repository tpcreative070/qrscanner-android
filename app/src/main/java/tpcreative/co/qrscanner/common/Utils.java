package tpcreative.co.qrscanner.common;

import android.content.Context;
import android.content.pm.PackageManager;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {

    private static final String TAG = Utils.class.getSimpleName();

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


}
