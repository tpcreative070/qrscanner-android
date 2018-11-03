package tpcreative.co.qrscanner.ui.scannerresult;
import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
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
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import tpcreative.co.qrscanner.BuildConfig;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.SingletonHistory;
import tpcreative.co.qrscanner.common.SingletonSave;
import tpcreative.co.qrscanner.common.SingletonScanner;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.model.Create;
import tpcreative.co.qrscanner.model.EnumAction;
import tpcreative.co.qrscanner.model.EnumFragmentType;
import tpcreative.co.qrscanner.model.History;
import tpcreative.co.qrscanner.model.Theme;
import tpcreative.co.qrscanner.model.room.InstanceGenerator;

public class ScannerResultFragment extends Fragment implements ScannerResultView,Utils.UtilsListener{

    private static final String TAG = ScannerResultFragment.class.getSimpleName();
    private Unbinder unbinder;
    private ScannerResultPresenter presenter;
    private Create create;
    @BindView(R.id.tvTitle)
    TextView tvTitle;


    List<LinearLayout> mList = new ArrayList<>();
    private History history = new History();

    /*Email*/
    @BindView(R.id.llEmail)
    LinearLayout llEmail;
    @BindView(R.id.emailTo)
    TextView emailTo;
    @BindView(R.id.emailSubject)
    TextView emailSubject;
    @BindView(R.id.emailMessage)
    TextView emailMessage;

    /*SMS*/
    @BindView(R.id.llSMS)
    LinearLayout llSMS;
    @BindView(R.id.smsTo)
    TextView smsTo;
    @BindView(R.id.smsMessage)
    TextView smsMessage;

    /*Contact*/
    @BindView(R.id.llContact)
    LinearLayout llContact;
    @BindView(R.id.contactFullName)
    TextView contactFullName;
    @BindView(R.id.contactAddress)
    TextView contactAddress;
    @BindView(R.id.contactPhone)
    TextView contactPhone;
    @BindView(R.id.contactEmail)
    TextView contactEmail;


    /*Location*/
    @BindView(R.id.llLocation)
    LinearLayout llLocation;
    @BindView(R.id.locationLatitude)
    TextView locationLatitude;
    @BindView(R.id.locationLongitude)
    TextView locationLongitude;
    @BindView(R.id.locationQuery)
    TextView locationQuery;

    /*Event*/
    @BindView(R.id.llEvent)
    LinearLayout llEvent;
    @BindView(R.id.eventTitle)
    TextView eventTitle;
    @BindView(R.id.eventLocation)
    TextView eventLocation;
    @BindView(R.id.eventDescription)
    TextView eventDescription;
    @BindView(R.id.eventBeginTime)
    TextView eventBeginTime;
    @BindView(R.id.eventEndTime)
    TextView eventEndTime;

    /*Wifi*/
    @BindView(R.id.llWifi)
    LinearLayout llWifi;
    @BindView(R.id.wifiSSID)
    TextView wifiSSID;
    @BindView(R.id.wifiPassword)
    TextView wifiPassword;
    @BindView(R.id.wifiNetworkEncryption)
    TextView wifiNetworkEncryption;
    @BindView(R.id.wifiHidden)
    TextView wifiHidden;

    /*Telephone*/
    @BindView(R.id.llTelephone)
    LinearLayout llTelephone;
    @BindView(R.id.telephoneNumber)
    TextView telephoneNumber;

    /*Text*/
    @BindView(R.id.llText)
    LinearLayout llText;
    @BindView(R.id.textMessage)
    TextView textMessage;

    /*URL*/
    @BindView(R.id.llURL)
    LinearLayout llURL;
    @BindView(R.id.urlAddress)
    TextView urlAddress;

    /*Open application*/
    @BindView(R.id.imgOpenApplication)
    ImageView imgOpenApplication;

    /*Back button*/
    @BindView(R.id.imgArrowBack)
    ImageView imgArrowBack;

    private  String code ;
    private Bitmap bitmap;
    private Animation mAnim = null;



    public static ScannerResultFragment newInstance(int index) {
        ScannerResultFragment fragment = new ScannerResultFragment();
        Bundle b = new Bundle();
        b.putInt("index", index);
        fragment.setArguments(b);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_review, container,false);
        unbinder = ButterKnife.bind(this, view);
        mList.add(llEmail);
        mList.add(llSMS);
        mList.add(llContact);
        mList.add(llLocation);
        mList.add(llEvent);
        mList.add(llWifi);
        mList.add(llTelephone);
        mList.add(llText);
        mList.add(llURL);
        presenter = new ScannerResultPresenter();
        presenter.bindView(this);
        presenter.getIntent(getArguments());
        SingletonHistory.getInstance().setUpdateData(true);
        imgArrowBack.setColorFilter(getContext().getResources().getColor(R.color.colorBlueLight), PorterDuff.Mode.SRC_ATOP);
        imgOpenApplication.setColorFilter(getContext().getResources().getColor(R.color.colorBlueLight), PorterDuff.Mode.SRC_ATOP);
        return view;
    }

    @OnClick(R.id.imgArrowBack)
    public void CloseWindow(View view){

        mAnim = AnimationUtils.loadAnimation(getContext(), R.anim.anomation_click_item);
        mAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                Log.d(TAG,"start");
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.remove(ScannerResultFragment.this).commit();
                if (create!=null){
                    if (create.fragmentType == EnumFragmentType.SCANNER){
                        SingletonScanner.getInstance().setVisible();
                        SingletonHistory.getInstance().setUpdateData(true);
                    }
                    else if (create.fragmentType == EnumFragmentType.SAVER){
                        SingletonSave.getInstance().setVisible();
                    }
                    else if (create.fragmentType == EnumFragmentType.HISTORY){
                        SingletonHistory.getInstance().setVisible();
                    }
                }
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(mAnim);
    }

    @OnClick(R.id.imgOpenApplication)
    public void openApplication(){
       onAddPermissionSave();
    }

    public void onAddPermissionPhoneCall() {
        Dexter.withActivity(getActivity())
                .withPermissions(
                        Manifest.permission.CALL_PHONE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            Utils.Log(TAG,"Action here phone call");
                            if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER) {
                                Intent intentPhoneCall = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + create.phone));
                                startActivity(intentPhoneCall);
                            } else {
                                code = "tel:" + create.phone + "";
                                onGenerateCode(code, EnumAction.SHARE);
                            }
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

    public void onAddPermissionSave() {
        Dexter.withActivity(getActivity())
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            create = presenter.result;
                            try {
                                switch (create.createType) {
                                    case ADDRESSBOOK:
                                        if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER) {
                                            Intent intentContact = new Intent();
                                            intentContact.setAction(ContactsContract.Intents.SHOW_OR_CREATE_CONTACT);
                                            intentContact.setData(Uri.fromParts("tel", create.phone, null));
                                            intentContact.putExtra(ContactsContract.Intents.Insert.NAME, create.fullName);
                                            intentContact.putExtra(ContactsContract.Intents.Insert.POSTAL, create.address);
                                            intentContact.putExtra(ContactsContract.Intents.Insert.PHONE, create.phone);
                                            intentContact.putExtra(ContactsContract.Intents.Insert.EMAIL, create.email);
                                            startActivity(intentContact);
                                        } else {
                                            code = "MECARD:N:" + create.fullName + ";TEL:" + create.phone + ";EMAIL:" + create.email + ";ADR:" + create.address + ";";
                                            onGenerateCode(code, EnumAction.SHARE);
                                        }

                                        break;
                                    case EMAIL_ADDRESS:

                                        if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER) {
                                            try {
                                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + create.email));
                                                intent.putExtra(Intent.EXTRA_SUBJECT, create.subject);
                                                intent.putExtra(Intent.EXTRA_TEXT, create.message);
                                                startActivity(intent);
                                            } catch (ActivityNotFoundException e) {
                                                //TODO smth
                                            }
                                        } else {
                                            code = "MATMSG:TO:" + create.email + ";SUB:" + create.subject + ";BODY:" + create.message + ";";
                                            onGenerateCode(code, EnumAction.SHARE);
                                        }

                                        break;
                                    case PRODUCT:
                                        break;
                                    case URI:

                                        if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER) {
                                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(create.url));
                                            startActivity(browserIntent);
                                        } else {
                                            code = create.url;
                                            onGenerateCode(code, EnumAction.SHARE);
                                        }


                                        break;
                                    case WIFI:

                                        if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER) {
                                            startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
                                        } else {
                                            code = "WIFI:S:" + create.ssId + ";T:" + create.password + ";P:" + create.networkEncryption + ";H:" + create.hidden + ";";
                                            onGenerateCode(code, EnumAction.SHARE);
                                        }
                                        break;
                                    case GEO:

                                        if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER) {
                                            String uri = "geo:" + create.lat + "," + create.lon + "";
                                            Intent intentMap = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
                                            intentMap.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                                            startActivity(intentMap);
                                        } else {
                                            code = "geo:" + create.lat + "," + create.lon + "?q=" + create.query + "";
                                            onGenerateCode(code, EnumAction.SHARE);
                                        }
                                        break;
                                    case TEL:
                                        onAddPermissionPhoneCall();
                                        break;
                                    case SMS:

                                        if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER) {
                                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + create.phone));
                                            intent.putExtra("sms_body", create.message);
                                            startActivity(intent);
                                        } else {
                                            code = "smsto:" + create.phone + ":" + create.message;
                                            onGenerateCode(code, EnumAction.SHARE);
                                        }

                                        break;
                                    case CALENDAR:

                                        if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER) {
                                            Intent intentCalendar = new Intent(Intent.ACTION_INSERT);
                                            intentCalendar.setData(CalendarContract.Events.CONTENT_URI);
                                            intentCalendar.putExtra(CalendarContract.Events.TITLE, create.title);
                                            intentCalendar.putExtra(CalendarContract.Events.DESCRIPTION, create.description);
                                            intentCalendar.putExtra(CalendarContract.Events.EVENT_LOCATION, create.location);
                                            intentCalendar.putExtra(CalendarContract.Events.ALL_DAY, false);
                                            intentCalendar.putExtra(
                                                    CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                                                    create.startEventMilliseconds);
                                            intentCalendar.putExtra(
                                                    CalendarContract.EXTRA_EVENT_END_TIME, create.endEventMilliseconds);
                                            startActivity(intentCalendar);
                                        } else {
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
                                            code = builder.toString();
                                            onGenerateCode(code, EnumAction.SHARE);
                                        }

                                        break;
                                    case ISBN:

                                        break;
                                    default:
                                        if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER) {
                                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:"));
                                            intent.putExtra("sms_body", create.text);
                                            startActivity(intent);
                                        } else {
                                            code = create.text;
                                            onGenerateCode(code, EnumAction.SHARE);
                                        }
                                        break;
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
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
    public void setView() {
        create = presenter.result;
        switch (create.createType){
            case ADDRESSBOOK:
                contactFullName.setText(create.fullName);
                contactAddress.setText(create.address);
                contactPhone.setText(create.phone);
                contactEmail.setText(create.email);

                history = new History();
                history.fullName = create.fullName;
                history.address = create.address;
                history.phone = create.phone;
                history.email = create.email;
                history.createType = create.createType.name();
                if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER){
                    imgOpenApplication.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.baseline_perm_contact_calendar_white_48));
                }
                else{
                    imgOpenApplication.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.baseline_share_white_48));
                }
                onShowUI(llContact);
                tvTitle.setText("AddressBook");
                break;
            case EMAIL_ADDRESS:

                emailTo.setText(create.email);
                emailSubject.setText(create.subject);
                emailMessage.setText(create.message);

                history = new History();
                history.email = create.email;
                history.subject = create.subject;
                history.message = create.message;
                history.createType = create.createType.name();
                if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER){
                    imgOpenApplication.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.baseline_email_white_48));
                }
                else{
                    imgOpenApplication.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.baseline_share_white_48));
                }

                onShowUI(llEmail);
                tvTitle.setText("Email");
                break;
            case PRODUCT:

                break;
            case URI:

                urlAddress.setText(create.url);
                history = new History();
                history.url = create.url;
                history.createType = create.createType.name();

                if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER){
                    imgOpenApplication.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.baseline_language_white_48));
                }
                else{
                    imgOpenApplication.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.baseline_share_white_48));
                }
                onShowUI(llURL);
                tvTitle.setText("Url");
                break;

            case WIFI:

                wifiSSID.setText(create.ssId);
                wifiPassword.setText(create.password);
                wifiNetworkEncryption.setText(create.networkEncryption);
                wifiHidden.setText(create.hidden ? "Yes" : "No");


                history = new History();
                history.ssId = create.ssId;
                history.password = create.password;
                history.networkEncryption = create.networkEncryption;
                history.hidden = create.hidden;
                history.createType = create.createType.name();
                if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER){

                    imgOpenApplication.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.baseline_network_wifi_white_48));

                }
                else{
                    imgOpenApplication.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.baseline_share_white_48));
                }

                onShowUI(llWifi);
                tvTitle.setText("Wifi");
                break;

            case GEO:

                locationLatitude.setText("" + create.lat);
                locationLongitude.setText("" + create.lon);
                locationQuery.setText("" + create.query);

                history = new History();
                history.lat = create.lat;
                history.lon = create.lon;
                history.query = create.query;
                history.createType = create.createType.name();

                if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER){
                    imgOpenApplication.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.baseline_location_on_white_48));

                }else{
                    imgOpenApplication.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.baseline_share_white_48));
                }

                onShowUI(llLocation);
                tvTitle.setText("Location");

                break;
            case TEL:

                telephoneNumber.setText(create.phone);
                history = new History();
                history.phone = create.phone;
                history.createType = create.createType.name();

                if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER) {
                    imgOpenApplication.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.baseline_phone_white_48));

                }else{
                    imgOpenApplication.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.baseline_share_white_48));
                }

                onShowUI(llTelephone);
                tvTitle.setText("Telephone");

                break;
            case SMS:

                smsTo.setText(create.phone);
                smsMessage.setText(create.message);

                history = new History();
                history.phone = create.phone;
                history.message = create.message;
                history.createType = create.createType.name();

                if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER){
                    imgOpenApplication.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.baseline_textsms_white_48));

                }
                else{
                    imgOpenApplication.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.baseline_share_white_48));
                }

                onShowUI(llSMS);
                tvTitle.setText("SMS");
                break;
            case CALENDAR:

                eventTitle.setText(create.title);
                eventLocation.setText(create.location);
                eventDescription.setText(create.description);
                eventBeginTime.setText(Utils.convertMillisecondsToDateTime(create.startEventMilliseconds));
                eventEndTime.setText(Utils.convertMillisecondsToDateTime(create.endEventMilliseconds));


                history = new History();
                history.title = create.title;
                history.location = create.location;
                history.description = create.description;
                history.startEvent = create.startEvent;
                history.endEvent = create.endEvent;
                history.startEventMilliseconds = create.startEventMilliseconds;
                history.endEventMilliseconds = create.endEventMilliseconds;
                history.createType = create.createType.name();

                if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER){
                    imgOpenApplication.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.baseline_event_white_48));
                }
                else{
                    imgOpenApplication.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.baseline_share_white_48));
                }
                onShowUI(llEvent);
                tvTitle.setText("Calendar");
                Log.d(TAG,"start milliseconds : " + create.startEventMilliseconds);

                break;
            case ISBN:

                break;
            default:
                textMessage.setText(create.text);
                history = new History();
                history.text = create.text;
                history.createType = create.createType.name();
                if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER){
                    imgOpenApplication.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.baseline_textsms_white_48));
                }
                else{
                    imgOpenApplication.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.baseline_share_white_48));
                }
                onShowUI(llText);
                tvTitle.setText("Text");
                break;
        }
    }

    @Override
    public void onSaved(String path, EnumAction enumAction) {
        switch (enumAction){
            case SHARE:{
                Log.d(TAG,"path : " + path);
                File file = new File(path);
                if (file.isFile()){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Uri uri = FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID + ".provider", file);
                        shareToSocial(uri);
                    }
                    else{
                        Uri uri = Uri.fromFile(file);
                        shareToSocial(uri);
                    }
                }
                else{
                    Toast.makeText(getActivity(),"No Found File",Toast.LENGTH_SHORT).show();
                }
            }
            default:{
                break;
            }
        }
    }

    public void shareToSocial(final Uri value) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, value);
        startActivity(Intent.createChooser(intent, "Share"));
    }


    public void onGenerateCode(String code,EnumAction enumAction){
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.MARGIN, 2);
            Theme theme = Theme.getInstance().getThemeInfo();
            bitmap = barcodeEncoder.encodeBitmap(getContext(),theme.getPrimaryColor(),code, BarcodeFormat.QR_CODE, 400, 400,hints);
            Utils.saveImage(bitmap,enumAction,create.createType.name(),code,this);
        } catch(Exception e) {
            Log.d(TAG,e.getMessage());
        }
    }

    public void onShowUI(View view){
        for (LinearLayout index : mList){
            if (view == index){
                index.setVisibility(View.VISIBLE);
            }
            else {
                index.setVisibility(View.GONE);
            }
        }
        if (create!=null){
            if (create.fragmentType == EnumFragmentType.SAVER){
                return;
            }
        }

        Log.d(TAG,"History :" + (history != null ? true : false));
        Log.d(TAG,"Create :" + (create != null ? true : false));
        Log.d(TAG,"fragmentType :" + (create.fragmentType));

        history.createDatetime = Utils.getCurrentDateTime();
        InstanceGenerator.getInstance(getContext()).onInsert(history);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG,"onStart");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG,"onStop");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG,"onPause");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy");
        unbinder.unbind();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
    }
}
