package tpcreative.co.qrscanner.ui.review;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.core.widget.NestedScrollView;
import androidx.print.PrintHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import tpcreative.co.qrscanner.BuildConfig;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.SingletonGenerate;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide;
import tpcreative.co.qrscanner.common.adapter.DividerItemDecoration;
import tpcreative.co.qrscanner.common.services.QRScannerApplication;
import tpcreative.co.qrscanner.model.Create;
import tpcreative.co.qrscanner.model.EnumAction;
import tpcreative.co.qrscanner.model.EnumImplement;
import tpcreative.co.qrscanner.model.ItemNavigation;
import tpcreative.co.qrscanner.model.Save;
import tpcreative.co.qrscanner.model.Theme;
import tpcreative.co.qrscanner.model.room.InstanceGenerator;
import tpcreative.co.qrscanner.ui.scannerresult.ScannerResultAdapter;

public class ReviewActivity extends BaseActivitySlide implements ReviewView, Utils.UtilsListener,ScannerResultAdapter.ItemSelectedListener {

    protected static final String TAG = ReviewActivity.class.getSimpleName();
    @BindView(R.id.imgResult)
    ImageView imgResult;
    private ReviewPresenter presenter;
    private Create create;
    private Bitmap bitmap;
    private String code;
    private Save save = new Save();
    private boolean isComplete;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.scrollView)
    NestedScrollView scrollView;

    private ScannerResultAdapter adapter;
    LinearLayoutManager llm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        scrollView.smoothScrollTo(0,0);
        presenter = new ReviewPresenter();
        setupRecyclerViewItem();
        presenter.bindView(this);
        presenter.getIntent(this);
    }


    public void setupRecyclerViewItem() {
        llm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(llm);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        adapter = new ScannerResultAdapter(getLayoutInflater(), this, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setNestedScrollingEnabled(false);
    }


    @Override
    public void onClickItem(int position) {
        final ItemNavigation itemNavigation = presenter.mListItemNavigation.get(position);
        if (itemNavigation!=null){
            switch (itemNavigation.enumAction){
                case SHARE:{
                    if (code != null) {
                        Log.d(TAG, "Share");
                        onGenerateCode(code, EnumAction.SHARE);
                    }
                    break;
                }
                case SAVE:{
                    if (code != null) {
                        onAddPermissionSave(EnumAction.SAVE);
                    }
                }
            }
        }
    }

    @Override
    public void onCatch() {
        onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isComplete){
            SingletonGenerate.getInstance().onCompletedGenerate();
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

        presenter.mListItemNavigation.add(new ItemNavigation(create.createType,create.fragmentType,EnumAction.SHARE,R.drawable.baseline_share_white_48,"Share"));
        presenter.mListItemNavigation.add(new ItemNavigation(create.createType,create.fragmentType,EnumAction.SAVE,R.drawable.baseline_save_alt_white_48,"Save"));
        onReloadData();
    }

    @Override
    public void onReloadData() {
        adapter.setDataSource(presenter.mListItemNavigation);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_review, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_item_print:{
                if (code != null) {
                    onAddPermissionSave(EnumAction.PRINT);
                }
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
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

    @Override
    public void onSaved(String path, EnumAction enumAction) {
        Utils.Log(TAG, "Saved successful");
        isComplete = true;
        switch (enumAction) {
            case SAVE: {
                /*Adding new columns*/
                save.barcodeFormat = BarcodeFormat.QR_CODE.name();
                save.favorite = false;
                Toast.makeText(this, "Saved code successful => Path: " + path, Toast.LENGTH_LONG).show();
                save.createDatetime = Utils.getCurrentDateTime();
                if (create.enumImplement == EnumImplement.CREATE) {
                    InstanceGenerator.getInstance(getContext()).onInsert(save);
                } else if (create.enumImplement == EnumImplement.EDIT) {
                    save.id = create.id;
                    InstanceGenerator.getInstance(getContext()).onUpdate(save);
                }
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
                    Toast.makeText(ReviewActivity.this,getString(R.string.no_items_found),Toast.LENGTH_SHORT).show();
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
