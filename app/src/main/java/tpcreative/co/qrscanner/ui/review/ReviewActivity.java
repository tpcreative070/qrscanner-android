package tpcreative.co.qrscanner.ui.review;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.OnClick;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.activity.BaseActivity;

public class ReviewActivity extends BaseActivity implements View.OnClickListener{

    private Animation mAnim = null;
    @BindView(R.id.imgArrowBack)
    ImageView imgArrowBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        imgArrowBack.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent();
        setResult(RESULT_OK,intent);
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.imgArrowBack :{
                mAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anomation_click_item);
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
            default:{
                break;
            }
        }
    }

}
