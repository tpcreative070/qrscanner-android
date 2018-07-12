package tpcreative.co.qrscanner.ui.scanner;
import android.content.Intent;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.gson.Gson;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.google.zxing.client.result.AddressBookParsedResult;
import com.google.zxing.client.result.CalendarParsedResult;
import com.google.zxing.client.result.EmailAddressParsedResult;
import com.google.zxing.client.result.GeoParsedResult;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ParsedResultType;
import com.google.zxing.client.result.SMSParsedResult;
import com.google.zxing.client.result.TelParsedResult;
import com.google.zxing.client.result.TextParsedResult;
import com.google.zxing.client.result.URIParsedResult;
import com.google.zxing.client.result.WifiParsedResult;
import com.google.zxing.common.StringUtils;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.Util;
import com.journeyapps.barcodescanner.camera.CameraSettings;
import com.journeyapps.barcodescanner.result.EmailAddressResultHandler;
import com.journeyapps.barcodescanner.result.ResultHandler;
import com.journeyapps.barcodescanner.result.ResultHandlerFactory;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.SingletonResponse;
import tpcreative.co.qrscanner.common.SingletonScanner;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.model.Create;

public class ScannerFragment extends Fragment implements SingletonScanner.SingletonScannerListener ,ScannerView{

    private static final String TAG = ScannerFragment.class.getSimpleName();
    private CaptureManager capture;
    private DecoratedBarcodeView barcodeScannerView;
    private BeepManager beepManager;
    private CameraSettings cameraSettings = new CameraSettings();
    private int typeCamera = 0 ;
    private Fragment fragment;
    private Unbinder butterKnife;
    @BindView(R.id.zxing_status_view)
    TextView zxing_status_view;
    @BindView(R.id.switch_flashlight)
    ImageView switch_flashlight;
    private boolean isTurnOnFlash;
    private Animation mAnim = null;
    private ScannerPresenter presenter;
    private boolean isRunning;

    public static ScannerFragment newInstance(int index) {
        ScannerFragment fragment = new ScannerFragment();
        Bundle b = new Bundle();
        b.putInt("index", index);
        fragment.setArguments(b);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scanner, container, false);
        butterKnife = ButterKnife.bind(this, view);
        SingletonScanner.getInstance().setListener(this);
        presenter = new ScannerPresenter();
        presenter.bindView(this);
        presenter.setFragmentList();
        fragment = this;
        barcodeScannerView = (DecoratedBarcodeView)view.findViewById(R.id.zxing_barcode_scanner);
        barcodeScannerView.decodeContinuous(callback);
        zxing_status_view.setVisibility(View.INVISIBLE);
        Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.brandon_reg);

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
        return view;
    }

    public void switchCamera(final int type){
        if (typeCamera==2){
            return;
        }
        cameraSettings.setRequestedCameraId(type); // front/back/etc
        barcodeScannerView.getBarcodeView().setCameraSettings(cameraSettings);
        barcodeScannerView.resume();
    }

    public void replaceFragment(final int position,final Create create){
        setInvisible();
        FragmentManager fm = getFragmentManager();
        fragment = presenter.mFragment.get(position);
        Bundle arguments = new Bundle();
        arguments.putSerializable("data",create);
        fragment.setArguments(arguments);
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.flContainer_review,fragment);
        ft.commit();
    }

    @Override
    public void setVisible() {
        barcodeScannerView.setVisibility(View.VISIBLE);
        barcodeScannerView.resume();
    }


    @Override
    public void setInvisible() {
        barcodeScannerView.pauseAndWait();
        barcodeScannerView.setVisibility(View.INVISIBLE);
    }

    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            Log.d(TAG,"Call back :" + result.getText() + "  type :" );
            ResultHandler resultHandler = ResultHandlerFactory.makeResultHandler(getActivity(), result.getResult());
                final ParsedResult parsedResult = resultHandler.getResult();
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
                String query  = "";
                String title = "";
                String location = "";
                String description = "";
                String startEvent = "";
                String endEvent = "";
                String text = "";
                boolean hidden = false;

                switch (parsedResult.getType()) {
                    case ADDRESSBOOK:
                        create.createType = ParsedResultType.ADDRESSBOOK;
                        AddressBookParsedResult addressResult = (AddressBookParsedResult) resultHandler.getResult();
                        if (addressResult!=null){
                             address = Utils.convertStringArrayToString(addressResult.getAddresses(), ",");
                             fullName = Utils.convertStringArrayToString(addressResult.getNames(), ",");
                             email = Utils.convertStringArrayToString(addressResult.getEmails(), ",");
                             phone = Utils.convertStringArrayToString(addressResult.getPhoneNumbers(), ",");
                        }
                        break;
                    case EMAIL_ADDRESS:
                        create.createType = ParsedResultType.EMAIL_ADDRESS;
                        EmailAddressParsedResult emailAddress = (EmailAddressParsedResult) resultHandler.getResult();
                        if (emailAddress!=null){
                            email = Utils.convertStringArrayToString(emailAddress.getTos(), ",");
                            subject = (emailAddress.getSubject())==null ? "" : emailAddress.getSubject() ;
                            message = (emailAddress.getBody()) == null ? "" : emailAddress.getBody();
                        }
                        break;
                    case PRODUCT:
                        create.createType = ParsedResultType.PRODUCT;
                        break;
                    case URI:
                        create.createType = ParsedResultType.URI;
                        create.createType = ParsedResultType.EMAIL_ADDRESS;
                        URIParsedResult urlResult = (URIParsedResult) resultHandler.getResult();
                        if (urlResult!=null){
                            url = (urlResult.getURI())==null ? "" : urlResult.getURI() ;
                        }
                        break;

                    case WIFI:
                        create.createType = ParsedResultType.WIFI;
                        WifiParsedResult wifiResult = (WifiParsedResult)resultHandler.getResult();
                        hidden = wifiResult.isHidden();
                        ssId = (wifiResult.getSsid()) == null ? "" : wifiResult.getSsid();
                        networkEncryption = (wifiResult.getNetworkEncryption())==null ? "" : wifiResult.getNetworkEncryption();
                        password = (wifiResult.getPassword()) == null ? "" : wifiResult.getPassword();
                        Log.d(TAG,"method : " + wifiResult.getNetworkEncryption() + " :" + wifiResult.getPhase2Method() + " :" +wifiResult.getPassword());
                        break;

                    case GEO:
                        create.createType = ParsedResultType.GEO;
                        try{
                            GeoParsedResult geoParsedResult = (GeoParsedResult)resultHandler.getResult();
                            lat = geoParsedResult.getLatitude();
                            lon = geoParsedResult.getLongitude();
                            query = geoParsedResult.getQuery();
                        }
                        catch (Exception e){

                        }
                        break;
                    case TEL:
                        create.createType = ParsedResultType.TEL;
                        TelParsedResult telParsedResult = (TelParsedResult) resultHandler.getResult();
                        phone = telParsedResult.getNumber();
                        break;
                    case SMS:
                        create.createType = ParsedResultType.SMS;
                        SMSParsedResult smsParsedResult = (SMSParsedResult) resultHandler.getResult();
                        phone = Utils.convertStringArrayToString(smsParsedResult.getNumbers(), ",");
                        message = (smsParsedResult.getBody()) == null ? "" : smsParsedResult.getBody();
                        break;
                    case CALENDAR:
                        create.createType = ParsedResultType.CALENDAR;
                        CalendarParsedResult calendarParsedResult = (CalendarParsedResult) resultHandler.getResult();

                        String startTime = Utils.convertMillisecondsToDateTime(calendarParsedResult.getStartTimestamp());
                        String endTime = Utils.convertMillisecondsToDateTime(calendarParsedResult.getEndTimestamp());

                        title = (calendarParsedResult.getSummary()) == null ? "" : calendarParsedResult.getSummary();
                        description = (calendarParsedResult.getDescription()) == null ? "" : calendarParsedResult.getDescription();
                        location = (calendarParsedResult.getLocation()) == null ? "" : calendarParsedResult.getLocation();
                        startEvent = startTime;
                        endEvent = endTime;
                        Log.d(TAG,startTime + " : " + endTime);

                        break;
                    case ISBN:
                        create.createType = ParsedResultType.ISBN;
                        break;
                    default:
                        create.createType = ParsedResultType.TEXT;
                        TextParsedResult textParsedResult = (TextParsedResult) resultHandler.getResult();
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
                create.text = text;

                Log.d(TAG,new Gson().toJson(create));
                beepManager.playBeepSoundAndVibrate();
                replaceFragment(0,create);

        }
        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };


    @OnClick(R.id.switch_camera)
    public void switchCamera(View view){
        Log.d(TAG,"on clicked here : " + cameraSettings.getRequestedCameraId());
        mAnim = AnimationUtils.loadAnimation(getContext(), R.anim.anomation_click_item);
        mAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                Log.d(TAG,"start");
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
                Log.d(TAG,"start");
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                if (isTurnOnFlash){
                    barcodeScannerView.setTorchOff();
                    isTurnOnFlash = false;
                    switch_flashlight.setImageDrawable(getContext().getResources().getDrawable(R.drawable.baseline_flash_off_white_36));
                }
                else{
                    barcodeScannerView.setTorchOn();
                    isTurnOnFlash = true;
                    switch_flashlight.setImageDrawable(getContext().getResources().getDrawable(R.drawable.baseline_flash_on_white_36));
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
                Log.d(TAG,"start");
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                SingletonResponse.getInstance().setCreatePosition();
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
                Log.d(TAG,"start");
            }
            @Override
            public void onAnimationEnd(Animation animation) {

            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(mAnim);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG,"onStop");
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!isRunning){
            SingletonResponse.getInstance().setScannerPosition();
            isRunning= true;
        }
        Log.d(TAG,"onStart");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        butterKnife.unbind();
        Log.d(TAG,"onDestroy");
        if (typeCamera!=2){
            barcodeScannerView.pause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG,"onActivityResult");
        if (requestCode == 100) {
            Log.d(TAG,"onActivityResult : 100 : " + resultCode);
            //barcodeScannerView.resume();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (barcodeScannerView != null) {
            if (isVisibleToUser) {
                if (typeCamera!=2){
                    barcodeScannerView.resume();
                }
            } else {
                if (typeCamera!=2){
                    barcodeScannerView.pause();
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

}
