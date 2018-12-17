package tpcreative.co.qrscanner.ui.pro;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.print.PrintHelper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import butterknife.BindView;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.activity.BaseActivity;
import tpcreative.co.qrscanner.common.services.QRScannerApplication;


public class ProVersionActivity extends BaseActivity implements View.OnClickListener{

    @BindView(R.id.imgArrowBack)
    ImageView imgArrowBack;
    @BindView(R.id.btnUpgradeNow)
    Button btnUpgradeNow;
    @BindView(R.id.tvTittle)
    TextView tvTittle;
    private Animation mAnim = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pro_version);
        imgArrowBack.setOnClickListener(this);
        btnUpgradeNow.setOnClickListener(this);
        tvTittle.setText(getString(R.string.pro_version));
        imgArrowBack.setColorFilter(getResources().getColor(R.color.colorBlueLight), PorterDuff.Mode.SRC_ATOP);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.imgArrowBack : {
                mAnim = AnimationUtils.loadAnimation(this, R.anim.anomation_click_item);
                mAnim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        Log.d(TAG,"start");
                    }
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        finish();
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                view.startAnimation(mAnim);
                break;
            }
            case R.id.btnUpgradeNow :{
                onUpgradeNow();
                break;
            }
        }
    }


    public void onUpgradeNow() {
        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName("Upgrade to pro version")
                .putContentType("Premium")
                .putContentId(System.currentTimeMillis() + "-"+QRScannerApplication.getInstance().getDeviceId()));
        Uri uri = Uri.parse("market://details?id=" + getString(R.string.qrscanner_pro_release));
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + getString(R.string.qrscanner_pro_release))));
        }
    }

}
