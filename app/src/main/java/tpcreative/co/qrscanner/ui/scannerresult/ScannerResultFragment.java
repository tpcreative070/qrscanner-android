package tpcreative.co.qrscanner.ui.scannerresult;
import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.result.ParsedResultType;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import butterknife.BindView;
import de.mrapp.android.dialog.MaterialDialog;
import tpcreative.co.qrscanner.BuildConfig;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.SingletonHistory;
import tpcreative.co.qrscanner.common.SingletonScanner;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide;
import tpcreative.co.qrscanner.common.adapter.DividerItemDecoration;
import tpcreative.co.qrscanner.common.controller.PrefsController;
import tpcreative.co.qrscanner.common.services.QRScannerApplication;
import tpcreative.co.qrscanner.helper.SQLiteHelper;
import tpcreative.co.qrscanner.model.Create;
import tpcreative.co.qrscanner.model.EnumAction;
import tpcreative.co.qrscanner.model.EnumFragmentType;
import tpcreative.co.qrscanner.model.HistoryModel;
import tpcreative.co.qrscanner.model.ItemNavigation;
import tpcreative.co.qrscanner.model.Theme;


public class ScannerResultFragment extends BaseActivitySlide implements ScannerResultView,Utils.UtilsListener,ScannerResultAdapter.ItemSelectedListener {

    private static final String TAG = ScannerResultFragment.class.getSimpleName();
    private ScannerResultPresenter presenter;
    private Create create;
    List<LinearLayout> mList = new ArrayList<>();
    private HistoryModel history = new HistoryModel();

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

    /*ISBN*/
    @BindView(R.id.llISBN)
    LinearLayout llISBN;
    @BindView(R.id.textISBN)
    TextView textISBN;



    /*Product*/
    @BindView(R.id.llProduct)
    LinearLayout llProduct;
    @BindView(R.id.textProduct)
    TextView textProduct;



    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.scrollView)
    NestedScrollView scrollView;

    @BindView(R.id.llAds)
    LinearLayout llAds;

    private ScannerResultAdapter adapter;
    LinearLayoutManager llm;


    private  String code ;
    private Bitmap bitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_review);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        scrollView.smoothScrollTo(0,0);
        mList.add(llEmail);
        mList.add(llSMS);
        mList.add(llContact);
        mList.add(llLocation);
        mList.add(llEvent);
        mList.add(llWifi);
        mList.add(llTelephone);
        mList.add(llText);
        mList.add(llURL);
        mList.add(llProduct);
        mList.add(llISBN);
        presenter = new ScannerResultPresenter();
        presenter.bindView(this);
        setupRecyclerViewItem();
        presenter.getIntent(this);
        if (!QRScannerApplication.getInstance().isLoaderLarge() && !Utils.isPremium()){
            QRScannerApplication.getInstance().getAdsLargeView();
        }
        presenter.doShowAds();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_result, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_item_report:{
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + "care@tpcreative.me"));
                    intent.putExtra(Intent.EXTRA_SUBJECT, "QRScanner App Support(Report issues)");
                    intent.putExtra(Intent.EXTRA_TEXT, "");
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    //TODO smth
                }
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
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
        final ItemNavigation navigation = presenter.mListItemNavigation.get(position);
        if (navigation!=null){
            switch (navigation.enumAction){
                case CLIPBOARD:{
                    onClipboardDialog();
                    break;
                }
                case SEARCH:{
                    final Create result = presenter.result;
                    if (result==null){
                        return;
                    }
                    switch (result.createType){
                        case URI:{
                            onSearch(result.url);
                            break;
                        }
                        case PRODUCT:{
                            onSearch(result.productId);
                            break;
                        }
                        case ISBN:{
                            onSearch(result.ISBN);
                            break;
                        }
                        case TEXT:{
                            onSearch(result.text);
                            break;
                        }
                    }
                    break;
                }
                default:{
                    onAddPermissionSave();
                    break;
                }
            }
        }
    }

    @Override
    public void onReloadData() {
        adapter.setDataSource(presenter.mListItemNavigation);
    }

    public void onAddPermissionPhoneCall() {
        Dexter.withActivity(this)
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

    public void onAddPermissionSave() {
        Dexter.withActivity(this)
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
                                        if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER) {
                                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:"));
                                            intent.putExtra("sms_body", create.productId);
                                            startActivity(intent);
                                        } else {
                                            code = create.productId;
                                            onGenerateCode(code, EnumAction.SHARE);
                                        }
                                        break;
                                    case URI:
                                        if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER) {
                                           onOpenWebSites(create.url);
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
                                        if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER) {
                                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:"));
                                            intent.putExtra("sms_body", create.ISBN);
                                            startActivity(intent);
                                        } else {
                                            code = create.ISBN;
                                            onGenerateCode(code, EnumAction.SHARE);
                                        }
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
                        Log.d(TAG, "error ask permission");
                    }
                }).onSameThread().check();
    }

    @Override
    public void setView() {
        create = presenter.result;
        switch (create.createType){
            case ADDRESSBOOK:
                /*Put item to HashClipboard*/
                presenter.hashClipboard.put("fullName",create.fullName);
                presenter.hashClipboard.put("address",create.address);
                presenter.hashClipboard.put("phone",create.phone);
                presenter.hashClipboard.put("email",create.email);
                contactFullName.setText(create.fullName);
                contactAddress.setText(create.address);
                contactPhone.setText(create.phone);
                contactEmail.setText(create.email);
                history = new HistoryModel();
                history.fullName = create.fullName;
                history.address = create.address;
                history.phone = create.phone;
                history.email = create.email;
                history.createType = create.createType.name();
                if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER){
                    presenter.mListItemNavigation.add(new ItemNavigation(create.createType,create.fragmentType,EnumAction.Other,R.drawable.baseline_perm_contact_calendar_white_48,"AddressBook"));
                }
                else{
                    presenter.mListItemNavigation.add(new ItemNavigation(create.createType,create.fragmentType,EnumAction.SHARE,R.drawable.baseline_share_white_48,"Share"));
                }
                onShowUI(llContact);
                setTitle("AddressBook");
                break;
            case EMAIL_ADDRESS:
                /*Put item to HashClipboard*/
                presenter.hashClipboard.put("email",create.email);
                presenter.hashClipboard.put("subject",create.subject);
                presenter.hashClipboard.put("message",create.message);
                emailTo.setText(create.email);
                emailSubject.setText(create.subject);
                emailMessage.setText(create.message);
                history = new HistoryModel();
                history.email = create.email;
                history.subject = create.subject;
                history.message = create.message;
                history.createType = create.createType.name();
                if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER){
                    presenter.mListItemNavigation.add(new ItemNavigation(create.createType,create.fragmentType,EnumAction.Other,R.drawable.baseline_email_white_48,"Email"));
                }
                else{
                    presenter.mListItemNavigation.add(new ItemNavigation(create.createType,create.fragmentType,EnumAction.SHARE,R.drawable.baseline_share_white_48,"Share"));
                }
                onShowUI(llEmail);
                setTitle("Email");
                break;
            case PRODUCT:
                /*Put item to HashClipboard*/
                presenter.hashClipboard.put("productId",create.productId);
                textProduct.setText(create.productId);
                history = new HistoryModel();
                history.text = create.productId;
                history.createType = create.createType.name();
                history.barcodeFormat = create.barcodeFormat;
                presenter.mListItemNavigation.add(new ItemNavigation(create.createType,create.fragmentType,EnumAction.SEARCH,R.drawable.baseline_search_white_48,"Search"));
                if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER){
                    presenter.mListItemNavigation.add(new ItemNavigation(create.createType,create.fragmentType,EnumAction.Other,R.drawable.baseline_textsms_white_48,"Product"));

                }
                else{
                    presenter.mListItemNavigation.add(new ItemNavigation(create.createType,create.fragmentType,EnumAction.SHARE,R.drawable.baseline_share_white_48,"Share"));
                }
                onShowUI(llProduct);
                setTitle("Product");
                break;
            case URI:
                /*Put item to HashClipboard*/
                presenter.hashClipboard.put("url",create.url);
                urlAddress.setText(create.url);
                history = new HistoryModel();
                history.url = create.url;
                history.createType = create.createType.name();
                presenter.mListItemNavigation.add(new ItemNavigation(create.createType,create.fragmentType,EnumAction.SEARCH,R.drawable.baseline_search_white_48,"Search"));
                if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER){
                    presenter.mListItemNavigation.add(new ItemNavigation(create.createType,create.fragmentType,EnumAction.Other,R.drawable.baseline_language_white_48,"Url"));
                }
                else{
                    presenter.mListItemNavigation.add(new ItemNavigation(create.createType,create.fragmentType,EnumAction.SHARE,R.drawable.baseline_share_white_48,"Share"));
                }
                onShowUI(llURL);
                setTitle("Url");
                final boolean isAutoOpening = PrefsController.getBoolean(getString(R.string.key_auto_navigate_to_browser),false);
                if (isAutoOpening){
                    onOpenWebSites(create.url);
                }
                break;
            case WIFI:
                /*Put item to HashClipboard*/
                presenter.hashClipboard.put("ssId",create.ssId);
                presenter.hashClipboard.put("password",create.password);
                presenter.hashClipboard.put("networkEncryption",create.networkEncryption);
                presenter.hashClipboard.put("hidden",create.hidden ? "Yes" : "No");
                wifiSSID.setText(create.ssId);
                wifiPassword.setText(create.password);
                wifiNetworkEncryption.setText(create.networkEncryption);
                wifiHidden.setText(create.hidden ? "Yes" : "No");
                history = new HistoryModel();
                history.ssId = create.ssId;
                history.password = create.password;
                history.networkEncryption = create.networkEncryption;
                history.hidden = create.hidden;
                history.createType = create.createType.name();
                if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER){
                    presenter.mListItemNavigation.add(new ItemNavigation(create.createType,create.fragmentType,EnumAction.Other,R.drawable.baseline_network_wifi_white_48,"Wifi"));
                }
                else{
                    presenter.mListItemNavigation.add(new ItemNavigation(create.createType,create.fragmentType,EnumAction.SHARE,R.drawable.baseline_share_white_48,"Share"));
                }
                onShowUI(llWifi);
                setTitle("Wifi");
                break;
            case GEO:
                /*Put item to HashClipboard*/
                presenter.hashClipboard.put("lat",create.lat+"");
                presenter.hashClipboard.put("lon",create.lon+"");
                presenter.hashClipboard.put("query",create.query);
                locationLatitude.setText("" + create.lat);
                locationLongitude.setText("" + create.lon);
                locationQuery.setText("" + create.query);
                history = new HistoryModel();
                history.lat = create.lat;
                history.lon = create.lon;
                history.query = create.query;
                history.createType = create.createType.name();
                if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER){
                    presenter.mListItemNavigation.add(new ItemNavigation(create.createType,create.fragmentType,EnumAction.Other,R.drawable.baseline_location_on_white_48,"Location"));

                }else{
                    presenter.mListItemNavigation.add(new ItemNavigation(create.createType,create.fragmentType,EnumAction.SHARE,R.drawable.baseline_share_white_48,"Share"));
                }
                onShowUI(llLocation);
                setTitle("Location");
                break;
            case TEL:
                /*Put item to HashClipboard*/
                presenter.hashClipboard.put("phone",create.phone);
                telephoneNumber.setText(create.phone);
                history = new HistoryModel();
                history.phone = create.phone;
                history.createType = create.createType.name();
                if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER) {
                    presenter.mListItemNavigation.add(new ItemNavigation(create.createType,create.fragmentType,EnumAction.Other,R.drawable.baseline_phone_white_48,"Telephone"));

                }else{
                    presenter.mListItemNavigation.add(new ItemNavigation(create.createType,create.fragmentType,EnumAction.SHARE,R.drawable.baseline_share_white_48,"Share"));
                }
                onShowUI(llTelephone);
                setTitle("Telephone");
                break;
            case SMS:
                /*Put item to HashClipboard*/
                presenter.hashClipboard.put("phone",create.phone);
                presenter.hashClipboard.put("message",create.message);
                smsTo.setText(create.phone);
                smsMessage.setText(create.message);
                history = new HistoryModel();
                history.phone = create.phone;
                history.message = create.message;
                history.createType = create.createType.name();
                if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER){
                    presenter.mListItemNavigation.add(new ItemNavigation(create.createType,create.fragmentType,EnumAction.Other,R.drawable.baseline_textsms_white_48,"SMS"));
                }
                else{
                    presenter.mListItemNavigation.add(new ItemNavigation(create.createType,create.fragmentType,EnumAction.SHARE,R.drawable.baseline_share_white_48,"Share"));
                }
                onShowUI(llSMS);
                setTitle("SMS");
                break;
            case CALENDAR:
                /*Put item to HashClipboard*/
                presenter.hashClipboard.put("title",create.title);
                presenter.hashClipboard.put("location",create.location);
                presenter.hashClipboard.put("description",create.description);
                presenter.hashClipboard.put("startEventMilliseconds",Utils.convertMillisecondsToDateTime(create.startEventMilliseconds));
                presenter.hashClipboard.put("endEventMilliseconds",Utils.convertMillisecondsToDateTime(create.endEventMilliseconds));
                eventTitle.setText(create.title);
                eventLocation.setText(create.location);
                eventDescription.setText(create.description);
                eventBeginTime.setText(Utils.convertMillisecondsToDateTime(create.startEventMilliseconds));
                eventEndTime.setText(Utils.convertMillisecondsToDateTime(create.endEventMilliseconds));
                history = new HistoryModel();
                history.title = create.title;
                history.location = create.location;
                history.description = create.description;
                history.startEvent = create.startEvent;
                history.endEvent = create.endEvent;
                history.startEventMilliseconds = create.startEventMilliseconds;
                history.endEventMilliseconds = create.endEventMilliseconds;
                history.createType = create.createType.name();

                if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER){
                    presenter.mListItemNavigation.add(new ItemNavigation(create.createType,create.fragmentType,EnumAction.Other,R.drawable.baseline_event_white_48,"Calendar"));
                }
                else{
                    presenter.mListItemNavigation.add(new ItemNavigation(create.createType,create.fragmentType,EnumAction.SHARE,R.drawable.baseline_share_white_48,"Share"));
                }
                onShowUI(llEvent);
                setTitle("Calendar");
                Utils.Log(TAG,"start milliseconds : " + create.startEventMilliseconds);
                break;
            case ISBN:
                /*Put item to HashClipboard*/
                presenter.hashClipboard.put("ISBN",create.ISBN);
                textISBN.setText(create.ISBN);
                history = new HistoryModel();
                history.text = create.ISBN;
                history.createType = create.createType.name();
                presenter.mListItemNavigation.add(new ItemNavigation(create.createType,create.fragmentType,EnumAction.SEARCH,R.drawable.baseline_search_white_48,"Search"));
                if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER){
                    presenter.mListItemNavigation.add(new ItemNavigation(create.createType,create.fragmentType,EnumAction.Other,R.drawable.baseline_textsms_white_48,"Share"));
                }
                else{
                    presenter.mListItemNavigation.add(new ItemNavigation(create.createType,create.fragmentType,EnumAction.SHARE,R.drawable.baseline_share_white_48,"ISBN"));
                }
                onShowUI(llISBN);
                setTitle("ISBN");
                break;
            default:
                /*Put item to HashClipboard*/
                presenter.hashClipboard.put("text",create.text);
                textMessage.setText(create.text);
                history = new HistoryModel();
                history.text = create.text;
                history.createType = create.createType.name();
                presenter.mListItemNavigation.add(new ItemNavigation(create.createType,create.fragmentType,EnumAction.SEARCH,R.drawable.baseline_search_white_48,"Search"));
                if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER){
                    presenter.mListItemNavigation.add(new ItemNavigation(create.createType,create.fragmentType,EnumAction.Other,R.drawable.baseline_textsms_white_48,"Text"));
                }
                else{
                    presenter.mListItemNavigation.add(new ItemNavigation(create.createType,create.fragmentType,EnumAction.SHARE,R.drawable.baseline_share_white_48,"Share"));
                }
                onShowUI(llText);
                setTitle("Text");
                break;
        }

        presenter.mListItemNavigation.add(new ItemNavigation(create.createType,create.fragmentType,EnumAction.CLIPBOARD,R.drawable.baseline_file_copy_white_48,"Clipboard"));
        onReloadData();

        try {
            final boolean autoCopy = PrefsController.getBoolean(getString(R.string.key_copy_to_clipboard),false);
            if (autoCopy){
                Utils.copyToClipboard(presenter.getResult(presenter.hashClipboard));
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onSaved(String path, EnumAction enumAction) {
        switch (enumAction){
            case SHARE:{
                Utils.Log(TAG,"path : " + path);
                File file = new File(path);
                if (file.isFile()){
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
                    Toast.makeText(ScannerResultFragment.this,getString(R.string.no_items_found),Toast.LENGTH_SHORT).show();
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
            if (history.createType == ParsedResultType.PRODUCT.name()){
                bitmap = barcodeEncoder.encodeBitmap(this,theme.getPrimaryDarkColor(),code, BarcodeFormat.valueOf(history.barcodeFormat), 400, 400,hints);
            }else{
                bitmap = barcodeEncoder.encodeBitmap(this,theme.getPrimaryDarkColor(),code, BarcodeFormat.QR_CODE, 400, 400,hints);
            }

            Utils.saveImage(bitmap,enumAction,create.createType.name(),code,this);
        } catch(Exception e) {
            Utils.Log(TAG,e.getMessage());
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
            if (create.fragmentType != EnumFragmentType.SCANNER){
                return;
            }
        }

        Utils.Log(TAG,"History :" + (history != null ? true : false));
        Utils.Log(TAG,"Create :" + (create != null ? true : false));
        Utils.Log(TAG,"fragmentType :" + (create.fragmentType));

        /*Adding new columns*/
        history.barcodeFormat = create.barcodeFormat;
        history.favorite = create.favorite;

        history.createDatetime = Utils.getCurrentDateTime();
        SQLiteHelper.onInsert(history);
        SingletonHistory.getInstance().reLoadData();
        Utils.Log(TAG,"Parse result " + Utils.getCodeContentByHistory(history));
    }

    @Override
    public void onStart() {
        super.onStart();
        Utils.Log(TAG,"onStart");
    }

    @Override
    public void onStop() {
        super.onStop();
        Utils.Log(TAG,"onStop");
    }

    @Override
    public void onPause() {
        super.onPause();
        Utils.Log(TAG,"onPause");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.Log(TAG,"onDestroy");
        if (presenter.result!=null){
            switch (presenter.result.fragmentType){
                case SCANNER:{
                    SingletonScanner.getInstance().setVisible();
                    Utils.Log(TAG,"onDestroy.......");
                    break;
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.Log(TAG,"onResume");
    }

    public void onClipboardDialog() {
        presenter.hashClipboardResult.clear();
        MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(this,R.style.LightDialogTheme);
        dialogBuilder.setTitle(R.string.copy_items);
        dialogBuilder.setPadding(40,40,40,0);
        dialogBuilder.setMargin(60,0,60,0);
        dialogBuilder.setMessage(R.string.choose_which_items_you_want_to_copy);
        final List<String>list = new ArrayList<>();
        for (Map.Entry<Object,String> hash : presenter.hashClipboard.entrySet()){
            list.add(hash.getValue());
        }

        CharSequence[] cs = list.toArray(new CharSequence[list.size()]);
        dialogBuilder.setMultiChoiceItems(cs, new boolean[]{false, false, false}, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                if (b){
                    presenter.hashClipboardResult.put(i,list.get(i));
                }
                else{
                    presenter.hashClipboardResult.remove(i);
                }
            }
        });
        dialogBuilder.setPositiveButton(R.string.copy, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (presenter.hashClipboardResult!=null &&presenter.hashClipboardResult.size()>0){
                    Utils.copyToClipboard(presenter.getResult(presenter.hashClipboardResult));
                    Toast.makeText(ScannerResultFragment.this,getString(R.string.copied_successful),Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialogBuilder.setNegativeButton(R.string.zxing_button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        MaterialDialog dialog = dialogBuilder.show();
        dialogBuilder.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Utils.Log(TAG,"action here");
                Button positive = dialog.findViewById(android.R.id.button1);
                Button negative = dialog.findViewById(android.R.id.button2);
                TextView title = dialog.findViewById(android.R.id.title);
                TextView content = dialog.findViewById(android.R.id.message);
                if (positive!=null && negative!=null && title!=null && content!=null){
                    title.setTextColor(QRScannerApplication.getInstance().getResources().getColor(R.color.black));
                    content.setTextColor(QRScannerApplication.getInstance().getResources().getColor(R.color.material_gray_700));
                    positive.setTextSize(14);
                    negative.setTextSize(14);
                    content.setTextSize(18);
                }
            }
        });
    }

    public void onOpenWebSites(String url){
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setPackage("com.android.chrome");
        try {
            startActivity(i);
        } catch (ActivityNotFoundException e) {
            // Chrome is probably not installed
            // Try with the default browser
            try {
                i.setPackage(null);
                startActivity(i);
            }
            catch (Exception ex){
                Toast.makeText(this,"Can not open the link",Toast.LENGTH_SHORT).show();
            }
        }
    }


    public void onSearch(String query){
        try {
            String escapedQuery = URLEncoder.encode(query, "UTF-8");
            Uri uri = Uri.parse("https://www.google.com/search?q=" + escapedQuery);
            Intent i = new Intent(Intent.ACTION_VIEW,uri);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.setPackage("com.android.chrome");
            try {
                startActivity(i);
            } catch (ActivityNotFoundException e) {
                // Chrome is probably not installed
                // Try with the default browser
                try {
                    i.setPackage(null);
                    startActivity(i);
                }
                catch (Exception ex){
                    Toast.makeText(this,"Can not open the link",Toast.LENGTH_SHORT).show();
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    /*show ads*/
    @Override
    public void doShowAds(boolean isShow) {
        if (isShow){
            Utils.Log(TAG,"ads...???");
            QRScannerApplication.getInstance().loadLargeAd(llAds);
            Utils.onWriteLogs(this,"logs_completed.txt",""+"1111");
            if (llAds!=null && !QRScannerApplication.getInstance().isLoaderLarge()){
                llAds.setVisibility(View.GONE);
            }
        }else{
            Utils.onWriteLogs(this,"logs_completed.txt",""+"2222");
            llAds.setVisibility(View.GONE);
        }
    }
}
