package tpcreative.co.qrscanner.ui.review;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v4.print.PrintHelper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.disposables.Disposable;
import tpcreative.co.qrscanner.BuildConfig;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.Navigator;
import tpcreative.co.qrscanner.common.SingletonCloseFragment;
import tpcreative.co.qrscanner.common.SingletonSave;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.activity.BaseActivity;
import tpcreative.co.qrscanner.common.controller.PrefsController;
import tpcreative.co.qrscanner.common.services.QRScannerApplication;
import tpcreative.co.qrscanner.model.Ads;
import tpcreative.co.qrscanner.model.Author;
import tpcreative.co.qrscanner.model.Create;
import tpcreative.co.qrscanner.model.EnumAction;
import tpcreative.co.qrscanner.model.EnumImplement;
import tpcreative.co.qrscanner.model.Save;
import tpcreative.co.qrscanner.model.Theme;
import tpcreative.co.qrscanner.model.room.InstanceGenerator;

public class ReviewActivity extends BaseActivity implements ReviewView, View.OnClickListener, Utils.UtilsListener {

    protected static final String TAG = ReviewActivity.class.getSimpleName();
    @BindView(R.id.imgResult)
    ImageView imgResult;
    @BindView(R.id.btnSave)
    Button btnSave;
    @BindView(R.id.btnShare)
    Button btnShare;
    @BindView(R.id.imgArrowBack)
    ImageView imgArrowBack;
    @BindView(R.id.imgPrint)
    ImageView imgPrint;
    private ReviewPresenter presenter;
    private Create create;
    private Bitmap bitmap;
    private String code;
    private Animation mAnim = null;
    private Save save = new Save();
    private Disposable subscriptions;
    @BindView(R.id.rlAds)
    RelativeLayout rlAds;
    AdView adViewBanner;
    @BindView(R.id.rlAdsRoot)
    RelativeLayout rlAdsRoot;

    @BindView(R.id.scrollView)
    ScrollView scrollView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        scrollView.smoothScrollTo(0,0);
        btnSave.setOnClickListener(this);
        btnShare.setOnClickListener(this);
        imgArrowBack.setOnClickListener(this);
        imgPrint.setOnClickListener(this);
        presenter = new ReviewPresenter();
        presenter.bindView(this);
        presenter.getIntent(this);
        imgArrowBack.setColorFilter(getContext().getResources().getColor(R.color.colorBlueLight), PorterDuff.Mode.SRC_ATOP);
        imgPrint.setColorFilter(getContext().getResources().getColor(R.color.colorBlueLight), PorterDuff.Mode.SRC_ATOP);
        initAds();
    }

    public void initAds() {
        if (BuildConfig.BUILD_TYPE.equals(getResources().getString(R.string.freedevelop))) {
            adViewBanner = new AdView(this);
            adViewBanner.setAdSize(AdSize.MEDIUM_RECTANGLE);
            adViewBanner.setAdUnitId(getString(R.string.banner_home_footer_test));
            rlAds.addView(adViewBanner);
            addGoogleAdmods();
        } else if (BuildConfig.BUILD_TYPE.equals(getResources().getString(R.string.freerelease))) {
            adViewBanner = new AdView(this);
            adViewBanner.setAdSize(AdSize.MEDIUM_RECTANGLE);

            final String preference = PrefsController.getString(getString(R.string.key_banner_review),null);
            if (preference!=null){
                adViewBanner.setAdUnitId(preference);
            }
            final Author author = Author.getInstance().getAuthorInfo();
            if (author!=null){
                if (author.version!=null){
                    final Ads ads = author.version.ads;
                    if (ads!=null){
                        String banner_review = ads.banner_review;
                        if (banner_review!=null){
                            if (preference!=null){
                                if (!banner_review.equals(preference)){
                                    PrefsController.putString(getString(R.string.key_banner_review),banner_review);
                                }
                            }
                        }
                    }
                }
            }
            rlAds.addView(adViewBanner);
            addGoogleAdmods();
        } else {
            Log.d(TAG, "Premium Version");
        }
    }

    @Override
    public void onCatch() {
        onBackPressed();
    }

    public void addGoogleAdmods() {
        AdRequest adRequest = new AdRequest.Builder().build();
        adViewBanner.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                Utils.Log(TAG,"Loaded successful");
                final Author author = Author.getInstance().getAuthorInfo();
                if (author != null) {
                    if (author.version != null) {
                        if (author.version.isAds) {
                            if (!BuildConfig.BUILD_TYPE.equals(getResources().getString(R.string.release))) {
                                rlAdsRoot.setVisibility(View.VISIBLE);
                            }
                            else{
                                rlAdsRoot.setVisibility(View.GONE);
                            }
                        } else {
                            rlAdsRoot.setVisibility(View.GONE);
                        }
                    } else {
                        rlAdsRoot.setVisibility(View.GONE);
                    }
                } else {
                    rlAdsRoot.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAdClosed() {
                Log.d(TAG, "Ad is closed!");
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                adViewBanner.setVisibility(View.GONE);
                rlAdsRoot.setVisibility(View.GONE);
                Log.d(TAG, "Ad failed to load! error code: " + errorCode);
            }

            @Override
            public void onAdLeftApplication() {
                Log.d(TAG, "Ad left application!");
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
            }
        });
        adViewBanner.loadAd(adRequest);
    }


    @OnClick(R.id.imgPrint)
    public void onPrint(){

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
        if (subscriptions != null) {
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
        switch (create.createType) {
            case ADDRESSBOOK:
                code = "MECARD:N:" + create.fullName + ";TEL:" + create.phone + ";EMAIL:" + create.email + ";ADR:" + create.address + ";";
                save = new Save();
                save.fullName = create.fullName;
                save.phone = create.phone;
                save.email = create.email;
                save.address = create.address;
                save.createType = create.createType.name();
                onGenerateReview(code);
                break;

            case EMAIL_ADDRESS:
                code = "MATMSG:TO:" + create.email + ";SUB:" + create.subject + ";BODY:" + create.message + ";";
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
                code = "WIFI:S:" + create.ssId + ";T:" + create.networkEncryption + ";P:" + create.password + ";H:" + create.hidden + ";";
                save = new Save();
                save.ssId = create.ssId;
                save.password = create.password;
                save.networkEncryption = create.networkEncryption;
                save.hidden = create.hidden;
                save.createType = create.createType.name();
                onGenerateReview(code);
                break;

            case GEO:
                code = "geo:" + create.lat + "," + create.lon + "?q=" + create.query + "";
                save = new Save();
                save.lat = create.lat;
                save.lon = create.lon;
                save.query = create.query;
                save.createType = create.createType.name();
                onGenerateReview(code);
                break;

            case TEL:
                code = "tel:" + create.phone + "";
                save = new Save();
                save.phone = create.phone;
                save.createType = create.createType.name();
                onGenerateReview(code);
                break;

            case SMS:
                code = "smsto:" + create.phone + ":" + create.message;
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
                builder.append("SUMMARY:" + create.title);
                builder.append("\n");
                builder.append("DTSTART:" + create.startEvent);
                builder.append("\n");
                builder.append("DTEND:" + create.endEvent);
                builder.append("\n");
                builder.append("LOCATION:" + create.location);
                builder.append("\n");
                builder.append("DESCRIPTION:" + create.description);
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

                code = builder.toString();
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

    public void onAddPermissionSave(final EnumAction enumAction) {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            onGenerateCode(code,enumAction);
                        } else {
                            Log.d(TAG, "Permission is denied");
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
        switch (view.getId()) {
            case R.id.btnSave: {
                mAnim = AnimationUtils.loadAnimation(getContext(), R.anim.anomation_click_item);
                mAnim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        Log.d(TAG, "start");
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (code != null) {
                            onAddPermissionSave(EnumAction.SAVE);
                        }
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                view.startAnimation(mAnim);
                break;
            }
            case R.id.btnShare: {
                mAnim = AnimationUtils.loadAnimation(getContext(), R.anim.anomation_click_item);
                mAnim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        Log.d(TAG, "start");
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (code != null) {
                            Log.d(TAG, "Share");
                            onGenerateCode(code, EnumAction.SHARE);
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                view.startAnimation(mAnim);
                break;
            }
            case R.id.imgArrowBack: {
                mAnim = AnimationUtils.loadAnimation(getContext(), R.anim.anomation_click_item);
                mAnim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        Log.d(TAG, "start");
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
                break;
            }
            case R.id.imgPrint:{
                mAnim = AnimationUtils.loadAnimation(getContext(), R.anim.anomation_click_item);
                mAnim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        Log.d(TAG, "start");
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (code != null) {
                            onAddPermissionSave(EnumAction.PRINT);
                        }
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                view.startAnimation(mAnim);
                break;
            }
        }
    }

    public void onGenerateCode(String code, EnumAction enumAction) {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.MARGIN, 2);
            Theme theme = Theme.getInstance().getThemeInfo();
            Utils.Log(TAG, "Starting save items 0");
            bitmap = barcodeEncoder.encodeBitmap(this, theme.getPrimaryDarkColor(), code, BarcodeFormat.QR_CODE, 400, 400, hints);
            Utils.saveImage(bitmap, enumAction, create.createType.name(), code, ReviewActivity.this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.rlRemove)
    public void onClickedRemoveAds(View view) {
        Navigator.onMoveProVersion(this);
        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName("Remove ads "+ReviewActivity.class.getSimpleName())
                .putContentType("Preparing remove ads")
                .putContentId(System.currentTimeMillis() + "-" + QRScannerApplication.getInstance().getDeviceId()));
    }

    @Override
    public void onSaved(String path, EnumAction enumAction) {
        Utils.Log(TAG, "Saved successful");
        switch (enumAction) {
            case SAVE: {
                Utils.showGotItSnackbar(btnSave, "Saved code successful => Path: " + path);
                save.createDatetime = Utils.getCurrentDateTime();
                if (create.enumImplement == EnumImplement.CREATE) {
                    InstanceGenerator.getInstance(getContext()).onInsert(save);
                } else if (create.enumImplement == EnumImplement.EDIT) {
                    save.id = create.id;
                    InstanceGenerator.getInstance(getContext()).onUpdate(save);
                }
                SingletonSave.getInstance().setUpdateData(true);
                break;
            }
            case SHARE: {
                File file = new File(path);
                if (file.isFile()) {
                    Log.d(TAG, "path : " + path);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Uri uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file);
                        shareToSocial(uri);
                    } else {
                        Uri uri = Uri.fromFile(file);
                        shareToSocial(uri);
                    }
                } else {
                    Utils.showGotItSnackbar(btnSave, R.string.no_items_found);
                }
                break;
            }
            case PRINT:{
                onPhotoPrint(path);
                break;
            }
            default: {
                Utils.Log(TAG, "Other case");
                break;
            }
        }
    }


    private void onPhotoPrint(String path) {
        try {
            PrintHelper photoPrinter = new PrintHelper(this);
            photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            photoPrinter.printBitmap(Utils.getCurrentDate(),bitmap);
        }catch (Exception e){
            Answers.getInstance().logContentView(new ContentViewEvent()
                    .putContentName("Printer "+ReviewActivity.class.getSimpleName())
                    .putContentType("Error "+e.getMessage())
                    .putContentId(System.currentTimeMillis() + "-" + QRScannerApplication.getInstance().getDeviceId()));
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
        Log.d(TAG, "path call");
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, value);
        startActivity(Intent.createChooser(intent, "Share"));
    }

    public void onGenerateReview(String code) {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.MARGIN, 2);
            Theme theme = Theme.getInstance().getThemeInfo();
            bitmap = barcodeEncoder.encodeBitmap(getContext(), theme.getPrimaryDarkColor(), code, BarcodeFormat.QR_CODE, 200, 200, hints);
            imgResult.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
