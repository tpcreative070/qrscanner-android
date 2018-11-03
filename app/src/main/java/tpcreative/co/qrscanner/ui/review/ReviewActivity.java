package tpcreative.co.qrscanner.ui.review;
import android.Manifest;
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
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import java.io.File;
import java.io.FileOutputStream;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import tpcreative.co.qrscanner.BuildConfig;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.SingletonCloseFragment;
import tpcreative.co.qrscanner.common.SingletonSave;
import tpcreative.co.qrscanner.common.SingletonScanner;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.activity.BaseActivity;
import tpcreative.co.qrscanner.common.controller.ServiceManager;
import tpcreative.co.qrscanner.common.controller.SingletonManagerProcessing;
import tpcreative.co.qrscanner.common.services.QRScannerApplication;
import tpcreative.co.qrscanner.model.Create;
import tpcreative.co.qrscanner.model.EnumAction;
import tpcreative.co.qrscanner.model.EnumImplement;
import tpcreative.co.qrscanner.model.Save;
import tpcreative.co.qrscanner.model.Theme;
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
    private Disposable subscriptions;
    @BindView(R.id.rlAds)
    RelativeLayout rlAds;
    AdView adViewBanner;


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
        initAds();
    }

    public void initAds(){
        if (BuildConfig.BUILD_TYPE.equals(getResources().getString(R.string.freedevelop))){
            adViewBanner = new AdView(this);
            adViewBanner.setAdSize(AdSize.BANNER);
            adViewBanner.setAdUnitId(getString(R.string.banner_home_footer_test));
            rlAds.addView(adViewBanner);
            addGoogleAdmods();
        }
        else if (BuildConfig.BUILD_TYPE.equals(getResources().getString(R.string.freerelease))){
            adViewBanner = new AdView(this);
            adViewBanner.setAdSize(AdSize.BANNER);
            adViewBanner.setAdUnitId(getString(R.string.banner_home_footer));
            rlAds.addView(adViewBanner);
            addGoogleAdmods();
        }
        else{
            Log.d(TAG,"Premium Version");
        }
    }

    public void addGoogleAdmods(){
        AdRequest adRequest = new AdRequest.Builder().build();
        adViewBanner.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
            }
            @Override
            public void onAdClosed() {
                Log.d(TAG,"Ad is closed!");
            }
            @Override
            public void onAdFailedToLoad(int errorCode) {
                adViewBanner.setVisibility(View.GONE);
                Log.d(TAG,"Ad failed to load! error code: " + errorCode);
            }
            @Override
            public void onAdLeftApplication() {
                Log.d(TAG,"Ad left application!");
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
            }
        });
        adViewBanner.loadAd(adRequest);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adViewBanner != null) {
            adViewBanner.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (adViewBanner != null) {
            adViewBanner.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adViewBanner != null) {
            adViewBanner.destroy();
        }
        if (subscriptions!=null){
            subscriptions.dispose();
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

    public void onAddPermissionSave() {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            onGenerateCode(code,EnumAction.SAVE);
                        }
                        else{
                            Log.d(TAG,"Permission is denied");
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            /*Miss add permission in manifest*/
                            Log.d(TAG, "request permission is failed");
                        }
                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        /* ... */
                        token.continuePermissionRequest();
                    }
                })
                .withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Log.d(TAG, "error ask permission");
                    }
                }).onSameThread().check();
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
                            onAddPermissionSave();
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
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.MARGIN, 2);
            Theme theme = Theme.getInstance().getThemeInfo();
            Utils.Log(TAG,"Starting save items 0");
            bitmap = barcodeEncoder.encodeBitmap(this,theme.getPrimaryColor(),code, BarcodeFormat.QR_CODE, 400, 400,hints);
            Utils.saveImage(bitmap,enumAction,create.createType.name(),code,ReviewActivity.this);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    @Override
    public void onSaved(String path, EnumAction enumAction) {
        Utils.Log(TAG,"Saved successful");
        switch (enumAction){
            case SAVE: {
                Toast.makeText(ReviewActivity.this,"Saved image successfully :" + path,Toast.LENGTH_SHORT).show();
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

    @Override
    public void onBackPressed() {
        finish();
        SingletonCloseFragment.getInstance().setUpdateData(true);
        SingletonSave.getInstance().setUpdateData(true);
        super.onBackPressed();
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
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.MARGIN, 2);
            Theme theme = Theme.getInstance().getThemeInfo();
            bitmap = barcodeEncoder.encodeBitmap(getContext(),theme.getPrimaryColor(),code, BarcodeFormat.QR_CODE, 200, 200,hints);
            imgResult.setImageBitmap(bitmap);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }


}
