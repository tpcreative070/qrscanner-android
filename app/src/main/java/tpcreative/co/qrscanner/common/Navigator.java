package tpcreative.co.qrscanner.common;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import butterknife.ButterKnife;
import tpcreative.co.qrscanner.model.Create;
import tpcreative.co.qrscanner.ui.review.ReviewActivity;

public class Navigator {

    public static void onMoveToReview(Activity context, Create create){
        Intent intent = new Intent(context, ReviewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("create",create);
        intent.putExtras(bundle);
        context.startActivityForResult(intent,1000);
    }

}
