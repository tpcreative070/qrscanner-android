package tpcreative.co.qrscanner.common;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import butterknife.ButterKnife;
import tpcreative.co.qrscanner.model.Create;
import tpcreative.co.qrscanner.ui.filecolor.ChangeFileColorActivity;
import tpcreative.co.qrscanner.ui.help.HelpActivity;
import tpcreative.co.qrscanner.ui.review.ReviewActivity;

public class Navigator {

    public static void onMoveToReview(Activity context, Create create){
        Intent intent = new Intent(context, ReviewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("create",create);
        intent.putExtras(bundle);
        context.startActivityForResult(intent,1000);
    }

    public static void onMoveToHelp(Context context){
        Intent intent = new Intent(context, HelpActivity.class);
        context.startActivity(intent);
    }

    public static void onMoveToChangeFileColor(Context context){
        Intent intent = new Intent(context, ChangeFileColorActivity.class);
        context.startActivity(intent);
    }

}
