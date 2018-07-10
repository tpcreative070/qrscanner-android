package tpcreative.co.qrscanner.common;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import tpcreative.co.qrscanner.ui.review.ReviewActivity;

public class Navigator {


    public static void onMoveToReview(Fragment fragment){
        Intent intent = new Intent(fragment.getContext(), ReviewActivity.class);
        fragment.startActivityForResult(intent,100);
    }


}
