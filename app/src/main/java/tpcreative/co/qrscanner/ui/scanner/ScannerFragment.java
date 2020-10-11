package tpcreative.co.qrscanner.ui.scanner;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.google.zxing.client.result.AddressBookParsedResult;
import com.google.zxing.client.result.CalendarParsedResult;
import com.google.zxing.client.result.EmailAddressParsedResult;
import com.google.zxing.client.result.GeoParsedResult;
import com.google.zxing.client.result.ISBNParsedResult;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ParsedResultType;
import com.google.zxing.client.result.ProductParsedResult;
import com.google.zxing.client.result.ResultParser;
import com.google.zxing.client.result.SMSParsedResult;
import com.google.zxing.client.result.TelParsedResult;
import com.google.zxing.client.result.TextParsedResult;
import com.google.zxing.client.result.URIParsedResult;
import com.google.zxing.client.result.WifiParsedResult;
import com.google.zxing.common.HybridBinarizer;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.camera.CameraSettings;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import butterknife.BindView;
import butterknife.OnClick;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.BaseFragment;
import tpcreative.co.qrscanner.common.DelayShowUIListener;
import tpcreative.co.qrscanner.common.Navigator;
import tpcreative.co.qrscanner.common.ResponseSingleton;
import tpcreative.co.qrscanner.common.ScannerSingleton;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.controller.PrefsController;
import tpcreative.co.qrscanner.common.services.QRScannerApplication;
import tpcreative.co.qrscanner.model.Create;
import tpcreative.co.qrscanner.model.EnumFragmentType;
import tpcreative.co.qrscanner.ui.scannerresult.ScannerResultFragment;
import tpcreative.co.qrscanner.ui.settings.SettingsFragment;


public class ScannerFragment extends BaseFragment implements ScannerSingleton.SingletonScannerListener ,ScannerView{

    private static final String TAG = ScannerFragment.class.getSimpleName();
    @BindView(R.id.zxing_status_view)
    TextView zxing_status_view;
    @BindView(R.id.switch_flashlight)
    AppCompatImageView switch_flashlight;
    @BindView(R.id.imgGallery)
    AppCompatImageView imgGallery;
    @BindView(R.id.switch_camera)
    AppCompatImageView switch_camera;
    @BindView(R.id.imgCreate)
    AppCompatImageView imgCreate;
    @BindView(R.id.zxing_barcode_scanner)
    DecoratedBarcodeView barcodeScannerView;
    @BindView(R.id.btnDone)
    Button btnDone;
    @BindView(R.id.tvCount)
    TextView tvCount;
    private BeepManager beepManager;
    private CameraSettings cameraSettings = new CameraSettings();
    private int typeCamera = 0 ;
    private boolean isTurnOnFlash;
    private Animation mAnim = null;
    private ScannerPresenter presenter;
    private boolean isRunning;
    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            try {
            Utils.Log(TAG,"Call back :" + result.getText() + "  type :"  +result.getBarcodeFormat().name());
            if (getActivity() ==null){
                return;
            }
           // ResultHan resultHandler = ResultHandlerFactory.makeResultHandler(getActivity(), result.getResult());
                final ParsedResult parsedResult = ResultParser.parseResult(result.getResult());
                final Create create = new Create();
                String address = "" ;
                String fullName = "";
                String email = "";
                String phone = "";
                String subject = "";
                String message = "";
                String url = "";
                String ssId = "";
                String networkEncryption = "";
                String password = "";
                double lat = 0;
                double lon = 0;
                long startEventMilliseconds = 0;
                long endEventMilliseconds = 0;
                String query  = "";
                String title = "";
                String location = "";
                String description = "";
                String startEvent = "";
                String endEvent = "";
                String text = "";
                String productId = "";
                String ISBN = "";
                boolean hidden = false;


                Utils.Log(TAG,"Type response "+ parsedResult.getType());
                switch (parsedResult.getType()) {
                    case ADDRESSBOOK:
                        create.createType = ParsedResultType.ADDRESSBOOK;
                        AddressBookParsedResult addressResult = (AddressBookParsedResult)parsedResult;
                        if (addressResult!=null){
                             address = Utils.convertStringArrayToString(addressResult.getAddresses(), ",");
                             fullName = Utils.convertStringArrayToString(addressResult.getNames(), ",");
                             email = Utils.convertStringArrayToString(addressResult.getEmails(), ",");
                             phone = Utils.convertStringArrayToString(addressResult.getPhoneNumbers(), ",");
                        }
                        break;
                    case EMAIL_ADDRESS:
                        create.createType = ParsedResultType.EMAIL_ADDRESS;
                        EmailAddressParsedResult emailAddress = (EmailAddressParsedResult) parsedResult;
                        if (emailAddress!=null){
                            email = Utils.convertStringArrayToString(emailAddress.getTos(), ",");
                            subject = (emailAddress.getSubject())==null ? "" : emailAddress.getSubject() ;
                            message = (emailAddress.getBody()) == null ? "" : emailAddress.getBody();
                        }
                        break;
                    case PRODUCT:
                        create.createType = ParsedResultType.PRODUCT;
                        ProductParsedResult productResult = (ProductParsedResult)parsedResult;
                        productId = (productResult.getProductID()) == null ? "" : productResult.getProductID();
                        Utils.Log(TAG,"Product "+new Gson().toJson(productResult));
                        break;
                    case URI:
                        create.createType = ParsedResultType.URI;
                        URIParsedResult urlResult = (URIParsedResult)parsedResult;
                        if (urlResult!=null){
                            url = (urlResult.getURI())==null ? "" : urlResult.getURI() ;
                        }
                        break;

                    case WIFI:
                        create.createType = ParsedResultType.WIFI;
                        WifiParsedResult wifiResult = (WifiParsedResult)parsedResult;
                        hidden = wifiResult.isHidden();
                        ssId = (wifiResult.getSsid()) == null ? "" : wifiResult.getSsid();
                        networkEncryption = (wifiResult.getNetworkEncryption())==null ? "" : wifiResult.getNetworkEncryption();
                        password = (wifiResult.getPassword()) == null ? "" : wifiResult.getPassword();
                        Utils.Log(TAG,"method : " + wifiResult.getNetworkEncryption() + " :" + wifiResult.getPhase2Method() + " :" +wifiResult.getPassword());
                        break;

                    case GEO:
                        create.createType = ParsedResultType.GEO;
                        try{
                            GeoParsedResult geoParsedResult = (GeoParsedResult)parsedResult;
                            lat = geoParsedResult.getLatitude();
                            lon = geoParsedResult.getLongitude();
                            query = geoParsedResult.getQuery();
                            String strNew = query.replace("q=", "");
                            query = strNew;
                        }
                        catch (Exception e){

                        }
                        break;
                    case TEL:
                        create.createType = ParsedResultType.TEL;
                        TelParsedResult telParsedResult = (TelParsedResult) parsedResult;
                        phone = telParsedResult.getNumber();
                        break;
                    case SMS:
                        create.createType = ParsedResultType.SMS;
                        SMSParsedResult smsParsedResult = (SMSParsedResult) parsedResult;
                        phone = Utils.convertStringArrayToString(smsParsedResult.getNumbers(), ",");
                        message = (smsParsedResult.getBody()) == null ? "" : smsParsedResult.getBody();
                        break;
                    case CALENDAR:
                        create.createType = ParsedResultType.CALENDAR;
                        CalendarParsedResult calendarParsedResult = (CalendarParsedResult) parsedResult;

                        String startTime = Utils.convertMillisecondsToDateTime(calendarParsedResult.getStartTimestamp());
                        String endTime = Utils.convertMillisecondsToDateTime(calendarParsedResult.getEndTimestamp());

                        title = (calendarParsedResult.getSummary()) == null ? "" : calendarParsedResult.getSummary();
                        description = (calendarParsedResult.getDescription()) == null ? "" : calendarParsedResult.getDescription();
                        location = (calendarParsedResult.getLocation()) == null ? "" : calendarParsedResult.getLocation();
                        startEvent = startTime;
                        endEvent = endTime;
                        startEventMilliseconds = calendarParsedResult.getStartTimestamp();
                        endEventMilliseconds = calendarParsedResult.getEndTimestamp();

                        Utils.Log(TAG,startTime + " : " + endTime);

                        break;
                    case ISBN:
                        create.createType = ParsedResultType.ISBN;
                        ISBNParsedResult isbParsedResult = (ISBNParsedResult) parsedResult;
                        ISBN = (isbParsedResult.getISBN()) == null ? "" : isbParsedResult.getISBN();
                        Utils.Log(TAG,"Result filter "+ new Gson().toJson(isbParsedResult));
                        break;
                    default:
                        try {
                            Utils.Log(TAG,"Default value");
                            create.createType = ParsedResultType.TEXT;
                            TextParsedResult textParsedResult = (TextParsedResult)parsedResult;
                            text = (textParsedResult.getText()) == null ? "" : textParsedResult.getText();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        break;
                }

                create.address = address;
                create.fullName = fullName;
                create.email = email;
                create.phone = phone;
                create.subject = subject;
                create.message = message;
                create.url = url;
                create.hidden = hidden;
                create.ssId = ssId;
                create.networkEncryption = networkEncryption;
                create.password = password;
                create.lat = lat;
                create.lon = lon;
                create.query = query;
                create.title = title;
                create.location = location;
                create.description = description;
                create.startEvent = startEvent;
                create.endEvent = endEvent;
                create.startEventMilliseconds = startEventMilliseconds;
                create.endEventMilliseconds = endEventMilliseconds;


                create.text = text;
                create.productId = productId;
                create.ISBN = ISBN;

                 /*Adding new columns*/
                create.barcodeFormat = BarcodeFormat.QR_CODE.name();
                create.favorite = false;
                if (result.getBarcodeFormat()!=null){
                    create.barcodeFormat = result.getBarcodeFormat().name();
                }
                doNavigation(create);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

        public void doNavigation(Create create){
            if (Utils.isMultipleScan()){
                btnDone.setVisibility(View.VISIBLE);
                tvCount.setVisibility(View.VISIBLE);
                presenter.updateValue(1);
                presenter.doSaveItems(create);
                if (barcodeScannerView!=null){
                    barcodeScannerView.pauseAndWait();
                    Utils.onObserveVisitView(1000, new DelayShowUIListener() {
                        @Override
                        public void onSetVisitView() {
                            barcodeScannerView.resume();
                        }
                    });
                }
            }else{
                Navigator.onResultView(getActivity(),create,ScannerResultFragment.class);
                if (barcodeScannerView!=null){
                    barcodeScannerView.pauseAndWait();
                }
            }
            beepManager.playBeepSoundAndVibrate();
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    public static ScannerFragment newInstance(int index) {
        ScannerFragment fragment = new ScannerFragment();
        Bundle b = new Bundle();
        b.putInt("index", index);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return 0;
    }

    @Override
    protected View getLayoutId(LayoutInflater inflater, ViewGroup viewGroup) {
        View view = inflater.inflate(R.layout.fragment_scanner, viewGroup, false);
        return view;
    }

    @Override
    protected void work() {
        super.work();
        ScannerSingleton.getInstance().setListener(this);
        presenter = new ScannerPresenter();
        presenter.bindView(this);
        barcodeScannerView.decodeContinuous(callback);
        zxing_status_view.setVisibility(View.INVISIBLE);
        imgCreate.setColorFilter(ContextCompat.getColor(QRScannerApplication.getInstance(),R.color.white), PorterDuff.Mode.SRC_ATOP);
        imgGallery.setColorFilter(ContextCompat.getColor(QRScannerApplication.getInstance(),R.color.white), PorterDuff.Mode.SRC_ATOP);
        switch_camera.setColorFilter(ContextCompat.getColor(QRScannerApplication.getInstance(),R.color.white), PorterDuff.Mode.SRC_ATOP);
        switch_flashlight.setColorFilter(ContextCompat.getColor(QRScannerApplication.getInstance(),R.color.white), PorterDuff.Mode.SRC_ATOP);

        if (Utils.checkCameraBack(getContext())){
            cameraSettings.setRequestedCameraId(Camera.CameraInfo.CAMERA_FACING_BACK);
            typeCamera = 0;
        }
        else{
            if (Utils.checkCameraFront(getContext())){
                cameraSettings.setRequestedCameraId(Camera.CameraInfo.CAMERA_FACING_FRONT);
                typeCamera = 1;
            }
            else{
                typeCamera = 2;
            }
        }
        barcodeScannerView.getBarcodeView().setCameraSettings(cameraSettings);
        beepManager = new BeepManager(getActivity());
        onHandlerIntent();

        if (barcodeScannerView!=null){
            if (!barcodeScannerView.isActivated()) {
                barcodeScannerView.resume();
            }
        }
        onBeepAndVibrate();
    }

    public void switchCamera(final int type){
        if (typeCamera==2){
            return;
        }
        cameraSettings.setRequestedCameraId(type); // front/back/etc
        barcodeScannerView.getBarcodeView().setCameraSettings(cameraSettings);
        barcodeScannerView.resume();
    }

    public void onAddPermissionGallery() {
        Dexter.withContext(getActivity())
                .withPermissions(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            if (barcodeScannerView!=null){
                                barcodeScannerView.pauseAndWait();
                            }
                            onGetGallery();
                        }
                        else{
                            Utils.Log(TAG,"Permission is denied");
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            /*Miss add permission in manifest*/
                            Utils.Log(TAG, "request permission is failed");
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
                        Utils.Log(TAG, "error ask permission");
                    }
                }).onSameThread().check();
    }

    public void onBeepAndVibrate(){
        if (beepManager==null){
            return;
        }
        boolean isBeep = PrefsController.getBoolean(getString(R.string.key_beep),false);
        boolean isVibrate = PrefsController.getBoolean(getString(R.string.key_vibrate),false);
        beepManager.setBeepEnabled(isBeep);
        beepManager.setVibrateEnabled(isVibrate);
    }

    @Override
    public void setVisible() {
        if (barcodeScannerView!=null){
            barcodeScannerView.resume();
        }
    }

    @Override
    public void setInvisible() {
        if (barcodeScannerView!=null){
            barcodeScannerView.pauseAndWait();
        }
    }

    @OnClick(R.id.switch_camera)
    public void switchCamera(View view){
        Utils.Log(TAG,"on clicked here : " + cameraSettings.getRequestedCameraId());
        mAnim = AnimationUtils.loadAnimation(getContext(), R.anim.anomation_click_item);
        mAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                Utils.Log(TAG,"start");
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                barcodeScannerView.pauseAndWait();
                if (cameraSettings.getRequestedCameraId()==0){
                    switchCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
                }
                else{
                    switchCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
                }
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(mAnim);
    }

    @OnClick(R.id.switch_flashlight)
    public void switchFlash(final View view){
        mAnim = AnimationUtils.loadAnimation(getContext(), R.anim.anomation_click_item);
        mAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                Utils.Log(TAG,"start");
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                if (isTurnOnFlash){
                    barcodeScannerView.setTorchOff();
                    isTurnOnFlash = false;
                    switch_flashlight.setImageDrawable(ContextCompat.getDrawable(QRScannerApplication.getInstance(),R.drawable.baseline_flash_off_white_48));
                    switch_flashlight.setColorFilter(ContextCompat.getColor(QRScannerApplication.getInstance(),R.color.white), PorterDuff.Mode.SRC_ATOP);
                }
                else{
                    barcodeScannerView.setTorchOn();
                    isTurnOnFlash = true;
                    switch_flashlight.setImageDrawable(ContextCompat.getDrawable(QRScannerApplication.getInstance(),R.drawable.baseline_flash_on_white_48));
                    switch_flashlight.setColorFilter(ContextCompat.getColor(QRScannerApplication.getInstance(),R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
                }
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(mAnim);
    }

    @OnClick(R.id.imgCreate)
    public void onClickCreate(final View view){
        mAnim = AnimationUtils.loadAnimation(getContext(), R.anim.anomation_click_item);
        mAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                Utils.Log(TAG,"start");
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                if (barcodeScannerView!=null){
                    barcodeScannerView.pauseAndWait();
                }
                Navigator.onMoveToHelp(getContext());
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(mAnim);
    }

    @OnClick(R.id.imgGallery)
    public void onClickGallery(final View view){
        mAnim = AnimationUtils.loadAnimation(getContext(), R.anim.anomation_click_item);
        mAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                Utils.Log(TAG,"start");
            }
            @Override
            public void onAnimationEnd(Animation animation) {
               onAddPermissionGallery();
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(mAnim);
    }

    @OnClick(R.id.btnDone)
    public void onclickDone(){
        Log.d(TAG,"Done");
        ResponseSingleton.getInstance().onScannerDone();
        if (barcodeScannerView!=null){
            barcodeScannerView.pause();
        }
        presenter.doRefreshView();
    }

    @Override
    public void doRefreshView() {
        btnDone.setVisibility(View.INVISIBLE);
        tvCount.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (barcodeScannerView!=null){
            barcodeScannerView.pauseAndWait();
        }
        Utils.Log(TAG,"onStop");
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!isRunning){
            ResponseSingleton.getInstance().setScannerPosition();
            isRunning= true;
        }
        ResponseSingleton.getInstance().onResumeAds();
        Utils.Log(TAG,"onStart");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.Log(TAG,"onDestroy");
        if (typeCamera!=2){
            if (barcodeScannerView!=null){
                barcodeScannerView.pause();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.Log(TAG,"onResume");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Utils.Log(TAG,"onActivityResult : " + requestCode + " - " + resultCode);
        if (resultCode == Activity.RESULT_OK && requestCode == 9999) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getActivity().getContentResolver().openInputStream(imageUri);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream,null,options);
                Handler handler =  new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onRenderCode(selectedImage,imageStream);
                    }
                },1000);
            } catch (FileNotFoundException  e) {
                e.printStackTrace();
                Utils.Log(TAG,"Something went wrong");
                setVisible();
            }

        }
        else if (resultCode == Activity.RESULT_OK && requestCode == Navigator.SCANNER){
            setVisible();
            Utils.Log(TAG,"Resume camera");
        }
        else {
            Utils.Log(TAG,"You haven't picked Image");
            setVisible();
            Utils.Log(TAG,"Resume camera!!!");
        }
    }

    public void onRenderCode(final Bitmap bitmap,final InputStream inputStream){
        try{
            if(bitmap==null){
                return;
            }
            if (inputStream==null){
                return;
            }
            int[] intArray = new int[bitmap.getWidth()*bitmap.getHeight()];
            bitmap.getPixels(intArray, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
            LuminanceSource source = new RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), intArray);
            BinaryBitmap mBitmap = new BinaryBitmap(new HybridBinarizer(source));
            Reader reader = new MultiFormatReader();
            try {
                Result result = reader.decode(mBitmap);
                onFilterResult(result);
                inputStream.close();
            }
            catch (NotFoundException | IOException |ChecksumException e){
                e.printStackTrace();
                barcodeScannerView.resume();
                //Utils.showGotItSnackbar(getView(),R.string.please_choose_correctly_format);
                Utils.onDropDownAlert(getActivity(),getString(R.string.please_choose_correctly_format));
            }
        }
        catch (FormatException e){
           e.printStackTrace();
            barcodeScannerView.resume();
        }
    }

    public void onGetGallery(){
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, 9999);
    }

    public void onFilterResult(Result result){
        if (getActivity() ==null){
            return;
        }
        //ResultHandler resultHandler = ResultHandlerFactory.makeResultHandler(getActivity(), result);
        final ParsedResult parsedResult = ResultParser.parseResult(result);
        final Create create = new Create();
        String address = "" ;
        String fullName = "";
        String email = "";
        String phone = "";
        String subject = "";
        String message = "";
        String url = "";
        String ssId = "";
        String networkEncryption = "";
        String password = "";
        String productId = "";
        String ISBN = "";
        double lat = 0;
        double lon = 0;
        long startEventMilliseconds = 0;
        long endEventMilliseconds = 0;
        String query  = "";
        String title = "";
        String location = "";
        String description = "";
        String startEvent = "";
        String endEvent = "";
        String text = "";
        boolean hidden = false;

        Utils.Log(TAG,"Type "+parsedResult.getType().name());
        switch (parsedResult.getType()) {
            case ADDRESSBOOK:
                create.createType = ParsedResultType.ADDRESSBOOK;
                AddressBookParsedResult addressResult = (AddressBookParsedResult) parsedResult;
                if (addressResult!=null){
                    address = Utils.convertStringArrayToString(addressResult.getAddresses(), ",");
                    fullName = Utils.convertStringArrayToString(addressResult.getNames(), ",");
                    email = Utils.convertStringArrayToString(addressResult.getEmails(), ",");
                    phone = Utils.convertStringArrayToString(addressResult.getPhoneNumbers(), ",");
                }
                break;
            case EMAIL_ADDRESS:
                create.createType = ParsedResultType.EMAIL_ADDRESS;
                EmailAddressParsedResult emailAddress = (EmailAddressParsedResult) parsedResult;
                if (emailAddress!=null){
                    email = Utils.convertStringArrayToString(emailAddress.getTos(), ",");
                    subject = (emailAddress.getSubject())==null ? "" : emailAddress.getSubject() ;
                    message = (emailAddress.getBody()) == null ? "" : emailAddress.getBody();
                }
                break;
            case PRODUCT:
                create.createType = ParsedResultType.PRODUCT;
                ProductParsedResult productResult = (ProductParsedResult) parsedResult;
                productId = (productResult.getProductID()) == null ? "" : productResult.getProductID();
                break;
            case URI:
                create.createType = ParsedResultType.URI;
                URIParsedResult urlResult = (URIParsedResult) parsedResult;
                if (urlResult!=null){
                    url = (urlResult.getURI())==null ? "" : urlResult.getURI() ;
                }
                break;

            case WIFI:
                create.createType = ParsedResultType.WIFI;
                WifiParsedResult wifiResult = (WifiParsedResult)parsedResult;
                hidden = wifiResult.isHidden();
                ssId = (wifiResult.getSsid()) == null ? "" : wifiResult.getSsid();
                networkEncryption = (wifiResult.getNetworkEncryption())==null ? "" : wifiResult.getNetworkEncryption();
                password = (wifiResult.getPassword()) == null ? "" : wifiResult.getPassword();
                Utils.Log(TAG,"method : " + wifiResult.getNetworkEncryption() + " :" + wifiResult.getPhase2Method() + " :" +wifiResult.getPassword());
                break;

            case GEO:
                create.createType = ParsedResultType.GEO;
                try{
                    GeoParsedResult geoParsedResult = (GeoParsedResult)parsedResult;
                    lat = geoParsedResult.getLatitude();
                    lon = geoParsedResult.getLongitude();
                    query = geoParsedResult.getQuery();
                    String strNew = query.replace("q=", "");
                    query = strNew;
                }
                catch (Exception e){

                }
                break;
            case TEL:
                create.createType = ParsedResultType.TEL;
                TelParsedResult telParsedResult = (TelParsedResult) parsedResult;
                phone = telParsedResult.getNumber();
                break;
            case SMS:
                create.createType = ParsedResultType.SMS;
                SMSParsedResult smsParsedResult = (SMSParsedResult) parsedResult;
                phone = Utils.convertStringArrayToString(smsParsedResult.getNumbers(), ",");
                message = (smsParsedResult.getBody()) == null ? "" : smsParsedResult.getBody();
                break;
            case CALENDAR:
                create.createType = ParsedResultType.CALENDAR;
                CalendarParsedResult calendarParsedResult = (CalendarParsedResult) parsedResult;

                String startTime = Utils.convertMillisecondsToDateTime(calendarParsedResult.getStartTimestamp());
                String endTime = Utils.convertMillisecondsToDateTime(calendarParsedResult.getEndTimestamp());

                title = (calendarParsedResult.getSummary()) == null ? "" : calendarParsedResult.getSummary();
                description = (calendarParsedResult.getDescription()) == null ? "" : calendarParsedResult.getDescription();
                location = (calendarParsedResult.getLocation()) == null ? "" : calendarParsedResult.getLocation();
                startEvent = startTime;
                endEvent = endTime;
                startEventMilliseconds = calendarParsedResult.getStartTimestamp();
                endEventMilliseconds = calendarParsedResult.getEndTimestamp();
                Utils.Log(TAG,startTime + " : " + endTime);
                break;
            case ISBN:
                create.createType = ParsedResultType.ISBN;
                ISBNParsedResult isbParsedResult = (ISBNParsedResult) parsedResult;
                ISBN = (isbParsedResult.getISBN()) == null ? "" : isbParsedResult.getISBN();
                Utils.Log(TAG,"Result filter "+ new Gson().toJson(isbParsedResult));
                break;
            default:
                create.createType = ParsedResultType.TEXT;
                TextParsedResult textParsedResult = (TextParsedResult) parsedResult;
                text = (textParsedResult.getText()) == null ? "" : textParsedResult.getText();
                break;
        }

        create.address = address;
        create.fullName = fullName;
        create.email = email;
        create.phone = phone;
        create.subject = subject;
        create.message = message;
        create.url = url;
        create.hidden = hidden;
        create.ssId = ssId;
        create.networkEncryption = networkEncryption;
        create.password = password;
        create.lat = lat;
        create.lon = lon;
        create.query = query;
        create.title = title;
        create.location = location;
        create.description = description;
        create.startEvent = startEvent;
        create.endEvent = endEvent;
        create.startEventMilliseconds = startEventMilliseconds;
        create.endEventMilliseconds = endEventMilliseconds;
        create.text = text;
        create.productId = productId;
        create.ISBN = ISBN;

        create.fragmentType = EnumFragmentType.SCANNER;
        beepManager.playBeepSoundAndVibrate();
        if (barcodeScannerView!=null){
            barcodeScannerView.pauseAndWait();
        }
        Navigator.onResultView(getActivity(),create,ScannerResultFragment.class);
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if (menuVisible) {
            QRScannerApplication.getInstance().getActivity().onShowFloatingButton(ScannerFragment.this,true);
            Utils.Log(TAG, "isVisible");
        } else {
            QRScannerApplication.getInstance().getActivity().onShowFloatingButton(ScannerFragment.this,false);
            Utils.Log(TAG, "isInVisible");
        }
        if (barcodeScannerView != null) {
            if (menuVisible) {
                if (typeCamera!=2){
                    onBeepAndVibrate();
                    barcodeScannerView.resume();
                }
            } else {
                if (typeCamera!=2){
                    barcodeScannerView.pause();
                }
            }
        }
        Utils.Log(TAG,"Fragment visit..." + menuVisible);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void updateValue(String value) {
        tvCount.setText(value);
    }

    /*Share File To QRScanner*/
    void onHandlerIntent(){
        try {
            Intent intent = getActivity().getIntent();
            String action = intent.getAction();
            String type = intent.getType();
            Utils.Log(TAG,"original type :"+ type);
            if (Intent.ACTION_SEND.equals(action) && type != null ) {
                handleSendSingleItem(intent);
            }
        }
        catch (Exception e){
            //Utils.showGotItSnackbar(getView(),R.string.error_occurred_importing);
            Utils.onDropDownAlert(getActivity(),getString(R.string.error_occurred_importing));
            e.printStackTrace();
        }
    }

    void handleSendSingleItem(Intent intent) {
        try {
            Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (imageUri != null) {
                final InputStream imageStream = getActivity().getContentResolver().openInputStream(imageUri);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream,null,options);
                Handler handler =  new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onRenderCode(selectedImage,imageStream);
                    }
                },1000);
            }
            else{
                //Utils.showGotItSnackbar(getView(),R.string.can_not_support_this_format);
                Utils.onDropDownAlert(getActivity(),getString(R.string.can_not_support_this_format));
            }
        }
        catch (Exception e){
            //Utils.showGotItSnackbar(getView(),R.string.error_occurred_importing);
            Utils.onDropDownAlert(getActivity(),getString(R.string.error_occurred_importing));
            e.printStackTrace();
        }
    }
}
