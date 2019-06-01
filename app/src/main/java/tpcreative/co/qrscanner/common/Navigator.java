package tpcreative.co.qrscanner.common;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.services.QRScannerApplication;
import tpcreative.co.qrscanner.model.Create;
import tpcreative.co.qrscanner.model.Save;
import tpcreative.co.qrscanner.ui.filecolor.ChangeFileColorActivity;
import tpcreative.co.qrscanner.ui.help.HelpActivity;
import tpcreative.co.qrscanner.ui.main.MainActivity;
import tpcreative.co.qrscanner.ui.pro.ProVersionActivity;
import tpcreative.co.qrscanner.ui.review.ReviewActivity;

public class Navigator {
    public static final int CREATE = 1000;
    public static final int SCANNER = 1001;
    public static void onMoveToReview(Activity context, Create create){
        Intent intent = new Intent(context, ReviewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(QRScannerApplication.getInstance().getString(R.string.key_create_intent),create);
        intent.putExtras(bundle);
        context.startActivityForResult(intent,CREATE);
    }

    public static void onMoveToHelp(Context context){
        Intent intent = new Intent(context, HelpActivity.class);
        context.startActivity(intent);
    }

    public static void onMoveToChangeFileColor(Context context){
        Intent intent = new Intent(context, ChangeFileColorActivity.class);
        context.startActivity(intent);
    }

    public static void onMoveProVersion(Context context){
        Intent intent = new Intent(context,ProVersionActivity.class);
        context.startActivity(intent);
    }

    public static  <T> void onGenerateView(Activity context, Save save,Class<T> clazz ){
        Intent intent = new Intent(context,clazz);
        Bundle bundle = new Bundle();
        bundle.putSerializable(QRScannerApplication.getInstance().getString(R.string.key_data),save);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    public static  <T> void onResultView(Activity context, Create save,Class<T> clazz ){
        Intent intent = new Intent(context,clazz);
        Bundle bundle = new Bundle();
        bundle.putSerializable(QRScannerApplication.getInstance().getString(R.string.key_data),save);
        intent.putExtras(bundle);
        context.startActivityForResult(intent,SCANNER);
    }

    public static void onMoveMainTab(Context context){
        Intent intent = new Intent(context,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}
