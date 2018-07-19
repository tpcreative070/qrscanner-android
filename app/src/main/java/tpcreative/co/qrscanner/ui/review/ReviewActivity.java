package tpcreative.co.qrscanner.ui.review;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import java.io.File;
import butterknife.BindView;
import tpcreative.co.qrscanner.BuildConfig;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.SingletonCloseFragment;
import tpcreative.co.qrscanner.common.SingletonSave;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.activity.BaseActivity;
import tpcreative.co.qrscanner.model.Create;
import tpcreative.co.qrscanner.model.EnumAction;
import tpcreative.co.qrscanner.model.EnumImplement;
import tpcreative.co.qrscanner.model.Save;
import tpcreative.co.qrscanner.model.room.InstanceGenerator;

public class ReviewActivity extends BaseActivity implements ReviewView , View.OnClickListener ,Utils.UtilsListener {

    protected static final String TAG = ReviewActivity.class.getSimpleName();
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
    private Save save = new Save();
    InterstitialAd mInterstitialAd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        btnSave.setOnClickListener(this);
        btnShare.setOnClickListener(this);
        imgArrowBack.setOnClickListener(this);
        presenter = new ReviewPresenter();
        presenter.bindView(this);
        presenter.getIntent(this);
        imgArrowBack.setColorFilter(getContext().getResources().getColor(R.color.colorBlueLight), PorterDuff.Mode.SRC_ATOP);
        onInitAds();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void onInitAds(){
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_full_screen_test));
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(adRequest);
        mInterstitialAd.setAdListener(new AdListener() {
            public void onAdLoaded() {
                showInterstitial();
            }
        });
    }

    private void showInterstitial() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
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
                code =   "MECARD:N:"+create.fullName+";TEL:"+create.phone+";EMAIL:"+create.email+";ADR:"+create.address+";";
                save = new Save();
                save.fullName = create.fullName;
                save.phone = create.phone;
                save.email = create.email;
                save.address = create.address;
                save.createType = create.createType.name();
                onGenerateReview(code);
                break;

            case EMAIL_ADDRESS:
                 code = "MATMSG:TO:"+create.email+";SUB:"+create.subject+";BODY:"+create.message+";";
                 save = new Save();
                 save.email = create.email;
                 save.subject = create.subject;
                 save.message = create.message;
                 save.createType = create.createType.name();
                 onGenerateReview(code);
                break;

            case PRODUCT:

                break;
            case URI:
                code = create.url;
                save = new Save();
                save.url = create.url;
                save.createType = create.createType.name();
                onGenerateReview(code);
                break;

            case WIFI:
                code = "WIFI:S:"+create.ssId+";T:"+create.password+";P:"+create.networkEncryption+";H:"+create.hidden+";";
                save = new Save();
                save.ssId = create.ssId;
                save.password = create.password;
                save.networkEncryption = create.networkEncryption;
                save.hidden = create.hidden;
                save.createType = create.createType.name();
                onGenerateReview(code);
                break;

            case GEO:
                code =  "geo:"+create.lat+","+create.lon+"?q="+create.query+"";
                save = new Save();
                save.lat = create.lat;
                save.lon = create.lon;
                save.query = create.query;
                save.createType = create.createType.name();
                onGenerateReview(code);
                break;

            case TEL:
                code = "tel:"+create.phone+"";
                save = new Save();
                save.phone = create.phone;
                save.createType = create.createType.name();
                onGenerateReview(code);
                break;

            case SMS:
                code =  "smsto:"+create.phone+":"+create.message;
                save = new Save();
                save.phone = create.phone;
                save.message = create.message;
                save.createType = create.createType.name();
                onGenerateReview(code);
                break;

            case CALENDAR:
                StringBuilder builder = new StringBuilder();
                builder.append("BEGIN:VEVENT");
                builder.append("\n");
                builder.append("SUMMARY:"+create.title);
                builder.append("\n");
                builder.append("DTSTART:"+create.startEvent);
                builder.append("\n");
                builder.append("DTEND:"+create.endEvent);
                builder.append("\n");
                builder.append("LOCATION:"+create.location);
                builder.append("\n");
                builder.append("DESCRIPTION:"+create.description);
                builder.append("\n");
                builder.append("END:VEVENT");

                save = new Save();
                save.title = create.title;
                save.startEvent = create.startEvent;
                save.endEvent = create.endEvent;
                save.startEventMilliseconds = create.startEventMilliseconds;
                save.endEventMilliseconds = create.endEventMilliseconds;
                save.location = create.location;
                save.description = create.description;
                save.createType = create.createType.name();

                code =  builder.toString();
                onGenerateReview(code);
                break;

            case ISBN:
                break;

            default:
                code = create.text;
                save = new Save();
                save.text = create.text;
                save.createType = create.createType.name();
                onGenerateReview(code);
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
                            onGenerateCode(code,EnumAction.SAVE);
                        }
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                view.startAnimation(mAnim);
                break;
            }
            case R.id.btnShare :{
                mAnim = AnimationUtils.loadAnimation(getContext(), R.anim.anomation_click_item);
                mAnim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        Log.d(TAG,"start");
                    }
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (code!=null){
                           Log.d(TAG,"Share");
                           onGenerateCode(code,EnumAction.SHARE);
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
                        SingletonCloseFragment.getInstance().setUpdateData(true);
                        SingletonSave.getInstance().setUpdateData(true);
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                view.startAnimation(mAnim);
            }
        }
    }

    public void onGenerateCode(String code,EnumAction enumAction){
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            bitmap = barcodeEncoder.encodeBitmap(code, BarcodeFormat.QR_CODE, 400, 400);
            imgResult.setImageBitmap(bitmap);
            Utils.saveImage(bitmap,enumAction,create.createType.name(),code,this);
        } catch(Exception e) {
            Log.d(TAG,e.getMessage());
        }
    }

    @Override
    public void onSaved(String path, EnumAction enumAction) {
        switch (enumAction){
            case SAVE: {
                Toast.makeText(this,"Saved image successfully",Toast.LENGTH_SHORT).show();
                save.createDatetime = Utils.getCurrentDateTime();
                if (create.enumImplement == EnumImplement.CREATE){
                    InstanceGenerator.getInstance(getContext()).onInsert(save);
                }
                else if (create.enumImplement == EnumImplement.EDIT){
                    save.id = create.id;
                    InstanceGenerator.getInstance(getContext()).onUpdate(save);
                }
                SingletonSave.getInstance().setUpdateData(true);
                break;
            }
            case SHARE:{

                File file = new File(path);
                if (file.isFile()){
                    Log.d(TAG,"path : " + path);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Uri uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file);
                        shareToSocial(uri);
                    }
                    else{
                        Uri uri = Uri.fromFile(file);
                        shareToSocial(uri);
                    }
                }
                else{
                    Toast.makeText(this,"No Found File",Toast.LENGTH_SHORT).show();
                }
                break;
            }
            default:{
                Log.d(TAG,"Other case");
                break;
            }
        }

    }

    public void shareToSocial(final Uri value) {
        Log.d(TAG,"path call");
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, value);
        startActivity(Intent.createChooser(intent, "Share"));
    }

    public void onGenerateReview(String code){
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            bitmap = barcodeEncoder.encodeBitmap(code, BarcodeFormat.QR_CODE, 400, 400);
            imgResult.setImageBitmap(bitmap);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
