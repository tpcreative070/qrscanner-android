package tpcreative.co.qrscanner.ui.scannerresult;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.zxing.client.result.AddressBookParsedResult;
import com.google.zxing.client.result.CalendarParsedResult;
import com.google.zxing.client.result.EmailAddressParsedResult;
import com.google.zxing.client.result.GeoParsedResult;
import com.google.zxing.client.result.ParsedResultType;
import com.google.zxing.client.result.SMSParsedResult;
import com.google.zxing.client.result.TelParsedResult;
import com.google.zxing.client.result.TextParsedResult;
import com.google.zxing.client.result.URIParsedResult;
import com.google.zxing.client.result.WifiParsedResult;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.SingletonScanner;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.model.Create;
import tpcreative.co.qrscanner.model.History;
import tpcreative.co.qrscanner.model.room.InstanceGenerator;

public class ScannerResultFragment extends Fragment implements ScannerResultView{

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
        return view;
    }

    @OnClick(R.id.imgArrowBack)
    public void CloseWindow(){
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.remove(this).commit();
        SingletonScanner.getInstance().setVisible();
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


                onShowUI(llWifi);
                tvTitle.setText("Wifi");
                break;

            case GEO:
                locationLatitude.setText(""+create.lat);
                locationLongitude.setText(""+create.lon);
                locationQuery.setText(""+create.query);


                history = new History();
                history.lat = create.lat;
                history.lon = create.lon;
                history.query = create.query;
                history.createType = create.createType.name();


                onShowUI(llLocation);
                tvTitle.setText("Location");
                break;
            case TEL:
                telephoneNumber.setText(create.phone);

                history = new History();
                history.phone = create.phone;
                history.createType = create.createType.name();

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

                onShowUI(llSMS);
                tvTitle.setText("SMS");
                break;
            case CALENDAR:

                eventTitle.setText(create.title);
                eventLocation.setText(create.location);
                eventDescription.setText(create.description);
                eventBeginTime.setText(create.startEvent);
                eventEndTime.setText(create.endEvent);

                history = new History();
                history.title = create.title;
                history.location = create.location;
                history.description = create.description;
                history.startEvent = create.startEvent;
                history.endEvent = create.endEvent;
                history.createType = create.createType.name();

                onShowUI(llEvent);
                tvTitle.setText("Calendar");
                break;
            case ISBN:

                break;
            default:

                textMessage.setText(create.text);
                history = new History();
                history.text = create.text;
                history.createType = create.createType.name();

                onShowUI(llText);
                tvTitle.setText("Text");
                break;
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
        history.createDatetime = Utils.getCurrentDateTime();
        history.key = InstanceGenerator.getInstance(getContext()).getUUId();
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
