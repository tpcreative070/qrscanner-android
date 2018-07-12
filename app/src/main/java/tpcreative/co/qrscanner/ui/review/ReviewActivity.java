package tpcreative.co.qrscanner.ui.review;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import butterknife.BindView;
import butterknife.OnClick;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.activity.BaseActivity;
import tpcreative.co.qrscanner.model.Create;

public class ReviewActivity extends BaseActivity implements ReviewView , View.OnClickListener ,Utils.UtilsListenner {
    @BindView(R.id.imgResult)
    ImageView imgResult;
    @BindView(R.id.btnSave)
    Button btnSave;
    @BindView(R.id.btnShare)
    Button btnShare;
    @BindView(R.id.imgArrowBack)
    ImageView imgArrowBack;

    private ReviewPresenter presenter;
    private Create create;
    private Bitmap bitmap;
    private  String code ;
    private Animation mAnim = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        btnSave.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        imgArrowBack.setOnClickListener(this);
        presenter = new ReviewPresenter();
        presenter.bindView(this);
        presenter.getIntent(this);
    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    @Override
    public void setView() {
       create = presenter.create;
        switch (create.createType){
            case ADDRESSBOOK:
                break;
            case EMAIL_ADDRESS:
                 code = "MATMSG:TO:"+create.email+";SUB:"+create.subject+";BODY:"+create.message+";";
                 onGenerateReview(code);
                break;
            case PRODUCT:

                break;
            case URI:

                break;

            case WIFI:
                break;

            case GEO:

                break;
            case TEL:
                break;
            case SMS:
                code =  "smsto:"+create.phone+":"+create.message;
                onGenerateReview(code);
                break;
            case CALENDAR:
                break;
            case ISBN:
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnSave:{
                mAnim = AnimationUtils.loadAnimation(getContext(), R.anim.anomation_click_item);
                mAnim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        Log.d(TAG,"start");
                    }
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (code!=null){
                            onGenerateCode(code);
                        }
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                view.startAnimation(mAnim);
                break;
            }
            case R.id.imgArrowBack : {
                mAnim = AnimationUtils.loadAnimation(getContext(), R.anim.anomation_click_item);
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
            }
        }
    }

    public void onGenerateCode(String code){
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            bitmap = barcodeEncoder.encodeBitmap(code, BarcodeFormat.QR_CODE, 400, 400);
            imgResult.setImageBitmap(bitmap);
            Utils.saveImage(bitmap,create.createType.name(),this);
        } catch(Exception e) {
            Log.d(TAG,e.getMessage());
        }
    }

    @Override
    public void onSaved() {
        Toast.makeText(this,"Saved image successfully",Toast.LENGTH_SHORT).show();
    }

    public void onGenerateReview(String code){
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            bitmap = barcodeEncoder.encodeBitmap(code, BarcodeFormat.QR_CODE, 400, 400);
            imgResult.setImageBitmap(bitmap);
        } catch(Exception e) {

        }
    }
}
