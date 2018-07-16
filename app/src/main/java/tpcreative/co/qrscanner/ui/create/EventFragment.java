package tpcreative.co.qrscanner.ui.create;
import android.graphics.PorterDuff;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;
import com.google.zxing.client.result.ParsedResultType;
import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.Navigator;
import tpcreative.co.qrscanner.common.SingletonCloseFragment;
import tpcreative.co.qrscanner.common.SingletonGenerate;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.model.Create;

public class EventFragment extends Fragment implements View.OnClickListener  {

    private static final String TAG = EventFragment.class.getSimpleName();
    private Unbinder unbinder;
    @BindView(R.id.llBeginTime)
    LinearLayout llBeginTime;
    @BindView(R.id.llEndTime)
    LinearLayout llEndTime;
    @BindView(R.id.edtTitle)
    EditText edtTitle;
    @BindView(R.id.edtLocation)
    EditText edtLocation;
    @BindView(R.id.edtDescription)
    EditText edtDescription;
    @BindView(R.id.tvBeginTime)
    TextView tvBeginTime;
    @BindView(R.id.tvEndTime)
    TextView tvEndTime;
    @BindView(R.id.imgArrowBack)
    ImageView imgArrowBack;
    @BindView(R.id.imgReview)
    ImageView imgReview;
    private AwesomeValidation mAwesomeValidation;
    private long beginDateTimeMilliseconds = 0;
    private long endDateTimeMilliseconds = 0;
    private long currentMilliseconds = 0;


    /*Date time picker*/
    private static final String TAG_DATETIME_FRAGMENT = "TAG_DATETIME_FRAGMENT";
    private static final String STATE_TEXTVIEW = "STATE_TEXTVIEW";
    private SwitchDateTimeDialogFragment dateTimeFragment;

    private boolean isClick ;
    private boolean isBegin;


    public static EventFragment newInstance(int index) {
        EventFragment fragment = new EventFragment();
        Bundle b = new Bundle();
        b.putInt("index", index);
        fragment.setArguments(b);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event, container, false);
        unbinder = ButterKnife.bind(this, view);
        llEndTime.setOnClickListener(this);
        llBeginTime.setOnClickListener(this);
        tvBeginTime.setOnClickListener(this);
        tvEndTime.setOnClickListener(this);
        initDateTimePicker();
        SingletonCloseFragment.getInstance().setUpdateData(false);
        imgArrowBack.setColorFilter(getContext().getResources().getColor(R.color.colorBlueLight), PorterDuff.Mode.SRC_ATOP);
        imgReview.setColorFilter(getContext().getResources().getColor(R.color.colorBlueLight), PorterDuff.Mode.SRC_ATOP);

        return view;
    }

    @OnClick(R.id.imgArrowBack)
    public void CloseWindow() {
        onCloseWindow();
    }

    @OnClick(R.id.imgReview)
    public void onCheck() {
        if (mAwesomeValidation.validate()) {

            if (beginDateTimeMilliseconds == 0){
                isBegin = true;
                isClick = true;
                dateTimeFragment.startAtCalendarView();
                dateTimeFragment.setAlertStyle(R.style.Theme_SwitchDateTime);

                Date date = new Date();
                Calendar cal = Calendar.getInstance();
                currentMilliseconds = date.getTime();
                cal.setTime(date);
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int days = cal.get(Calendar.DAY_OF_MONTH);
                int hours = cal.get(Calendar.HOUR_OF_DAY);
                int minutes = cal.get(Calendar.MINUTE);
                dateTimeFragment.setDefaultDateTime(new GregorianCalendar(year, month, days, hours, minutes).getTime());
                dateTimeFragment.show(getChildFragmentManager(), TAG_DATETIME_FRAGMENT);
                return;
            }

            if (endDateTimeMilliseconds == 0){
                isBegin = false;
                isClick = true;
                dateTimeFragment.startAtCalendarView();
                dateTimeFragment.setAlertStyle(R.style.Theme_SwitchDateTime);

                Date date = new Date();
                Calendar cal = Calendar.getInstance();
                currentMilliseconds = date.getTime();
                cal.setTime(date);
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int days = cal.get(Calendar.DAY_OF_MONTH);
                int hours = cal.get(Calendar.HOUR_OF_DAY);
                int minutes = cal.get(Calendar.MINUTE);
                dateTimeFragment.setDefaultDateTime(new GregorianCalendar(year, month, days, hours, minutes).getTime());
                dateTimeFragment.show(getChildFragmentManager(), TAG_DATETIME_FRAGMENT);
                return;
            }

            if (beginDateTimeMilliseconds>endDateTimeMilliseconds){
                Toast.makeText(getContext(),"Ending event data time must be greater than begin date time",Toast.LENGTH_SHORT).show();
                return;
            }

            Create create = new Create();
            create.title = edtTitle.getText().toString();
            create.location = edtLocation.getText().toString();
            create.description = edtDescription.getText().toString();
            create.startEvent = Utils.getCurrentDatetimeEvent(beginDateTimeMilliseconds);
            create.endEvent = Utils.getCurrentDatetimeEvent(endDateTimeMilliseconds);
            create.startEventMilliseconds = beginDateTimeMilliseconds;
            create.endEventMilliseconds = endDateTimeMilliseconds;
            create.createType = ParsedResultType.CALENDAR;
            Navigator.onMoveToReview(getActivity(), create);
        } else {
            Log.d(TAG, "error");
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.llBeginTime:{

                if (!isClick) {
                    isBegin = true;
                    isClick = true;
                    dateTimeFragment.startAtCalendarView();
                    dateTimeFragment.setAlertStyle(R.style.Theme_SwitchDateTime);

                    Date date = new Date();
                    Calendar cal = Calendar.getInstance();
                    currentMilliseconds = date.getTime();
                    cal.setTime(date);
                    int year = cal.get(Calendar.YEAR);
                    int month = cal.get(Calendar.MONTH);
                    int days = cal.get(Calendar.DAY_OF_MONTH);
                    int hours = cal.get(Calendar.HOUR_OF_DAY);
                    int minutes = cal.get(Calendar.MINUTE);
                    dateTimeFragment.setDefaultDateTime(new GregorianCalendar(year, month, days, hours, minutes).getTime());
                    dateTimeFragment.show(getChildFragmentManager(), TAG_DATETIME_FRAGMENT);
                }
                break;
            }
            case R.id.llEndTime :{

                if (!isClick) {
                    isBegin = false;
                    isClick = true;
                    dateTimeFragment.startAtCalendarView();
                    dateTimeFragment.setAlertStyle(R.style.Theme_SwitchDateTime);

                    Date date = new Date();
                    Calendar cal = Calendar.getInstance();
                    currentMilliseconds = date.getTime();
                    cal.setTime(date);
                    int year = cal.get(Calendar.YEAR);
                    int month = cal.get(Calendar.MONTH);
                    int days = cal.get(Calendar.DAY_OF_MONTH);
                    int hours = cal.get(Calendar.HOUR_OF_DAY);
                    int minutes = cal.get(Calendar.MINUTE);
                    dateTimeFragment.setDefaultDateTime(new GregorianCalendar(year, month, days, hours, minutes).getTime());
                    dateTimeFragment.show(getChildFragmentManager(), TAG_DATETIME_FRAGMENT);
                }

                break;
            }
            case  R.id.tvBeginTime : {
                if (!isClick) {
                    isBegin = true;
                    isClick = true;
                    dateTimeFragment.startAtCalendarView();
                    dateTimeFragment.setAlertStyle(R.style.Theme_SwitchDateTime);

                    Date date = new Date();
                    Calendar cal = Calendar.getInstance();
                    currentMilliseconds = date.getTime();
                    cal.setTime(date);
                    int year = cal.get(Calendar.YEAR);
                    int month = cal.get(Calendar.MONTH);
                    int days = cal.get(Calendar.DAY_OF_MONTH);
                    int hours = cal.get(Calendar.HOUR_OF_DAY);
                    int minutes = cal.get(Calendar.MINUTE);
                    dateTimeFragment.setDefaultDateTime(new GregorianCalendar(year, month, days, hours, minutes).getTime());
                    dateTimeFragment.show(getChildFragmentManager(), TAG_DATETIME_FRAGMENT);

                }
                break;
            }
            case  R.id.tvEndTime : {
                if (!isClick) {
                    isBegin = false;
                    isClick = true;
                    dateTimeFragment.startAtCalendarView();
                    dateTimeFragment.setAlertStyle(R.style.Theme_SwitchDateTime);

                    Date date = new Date();
                    Calendar cal = Calendar.getInstance();
                    currentMilliseconds = date.getTime();
                    cal.setTime(date);
                    int year = cal.get(Calendar.YEAR);
                    int month = cal.get(Calendar.MONTH);
                    int days = cal.get(Calendar.DAY_OF_MONTH);
                    int hours = cal.get(Calendar.HOUR_OF_DAY);
                    int minutes = cal.get(Calendar.MINUTE);
                    dateTimeFragment.setDefaultDateTime(new GregorianCalendar(year, month, days, hours, minutes).getTime());
                    dateTimeFragment.show(getChildFragmentManager(), TAG_DATETIME_FRAGMENT);
                }
                break;
            }
            default:{
                break;
            }
        }
    }

    /*Init Date time picker*/

    public void initDateTimePicker(){
        // Construct SwitchDateTimePicker
        dateTimeFragment = (SwitchDateTimeDialogFragment) getChildFragmentManager().findFragmentByTag(TAG_DATETIME_FRAGMENT);
        if(dateTimeFragment == null) {
            dateTimeFragment = SwitchDateTimeDialogFragment.newInstance(
                    getString(R.string.label_datetime_dialog),
                    getString(android.R.string.ok),
                    getString(android.R.string.cancel),
                    getString(R.string.clean) // Optional
            );
        }

        // Optionally define a timezone
        dateTimeFragment.setTimeZone(TimeZone.getDefault());

        // Init format
        final SimpleDateFormat myDateFormat = new SimpleDateFormat("d MMM yyyy HH:mm", java.util.Locale.getDefault());
        // Assign unmodifiable values
        dateTimeFragment.set24HoursMode(false);
        dateTimeFragment.setHighlightAMPMSelection(false);
        dateTimeFragment.setMinimumDateTime(new GregorianCalendar(2015, Calendar.JANUARY, 1).getTime());
        dateTimeFragment.setMaximumDateTime(new GregorianCalendar(2025, Calendar.DECEMBER, 31).getTime());

        // Define new day and month format
        try {
            dateTimeFragment.setSimpleDateMonthAndDayFormat(new SimpleDateFormat("MMMM dd", Locale.getDefault()));
        } catch (SwitchDateTimeDialogFragment.SimpleDateMonthAndDayFormatException e) {
            Log.e(TAG, e.getMessage());
        }

        // Set listener for date
        // Or use dateTimeFragment.setOnButtonClickListener(new SwitchDateTimeDialogFragment.OnButtonClickListener() {
        dateTimeFragment.setOnButtonClickListener(new SwitchDateTimeDialogFragment.OnButtonWithNeutralClickListener() {
            @Override
            public void onPositiveButtonClick(Date date) {
                if (isBegin){
                    if (currentMilliseconds>date.getTime()){
                       Toast.makeText(getContext(),"Starting event data time must be greater than current date time",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        beginDateTimeMilliseconds = date.getTime();
                        tvBeginTime.setText(myDateFormat.format(date));
                    }
                }
                else{
                    if (currentMilliseconds>date.getTime()){
                        Toast.makeText(getContext(),"Ending event data time must be greater than current date time",Toast.LENGTH_SHORT).show();
                    }
                    else if(beginDateTimeMilliseconds >= date.getTime()){
                        Toast.makeText(getContext(),"Ending event data time must be greater than begin date time",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        endDateTimeMilliseconds = date.getTime();
                        tvEndTime.setText(myDateFormat.format(date));
                    }
                }
                isClick = false;
            }

            @Override
            public void onNegativeButtonClick(Date date) {
                // Do nothing
                isClick = false;
            }

            @Override
            public void onNeutralButtonClick(Date date) {
                // Optional if neutral button does'nt exists
                isClick = false;
            }
        });
    }

    private void addValidationForEditText() {
        mAwesomeValidation.addValidation(getActivity(), R.id.edtTitle, RegexTemplate.NOT_EMPTY, R.string.err_title);
        mAwesomeValidation.addValidation(getActivity(), R.id.edtLocation, RegexTemplate.NOT_EMPTY, R.string.err_location);
        mAwesomeValidation.addValidation(getActivity(), R.id.edtDescription, RegexTemplate.NOT_EMPTY, R.string.err_description);
    }

    public void clearUI() {
        edtTitle.requestFocus();
        edtTitle.setText("");
        edtLocation.setText("");
        edtDescription.setText("");
        tvBeginTime.setText("");
        tvEndTime.setText("");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        mAwesomeValidation = new AwesomeValidation(ValidationStyle.BASIC);
        mAwesomeValidation.clear();
        addValidationForEditText();
        clearUI();
        Log.d(TAG,"current time : " + Utils.getCurrentDatetimeEvent());
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        unbinder.unbind();
    }

    public void onCloseWindow(){
        Utils.hideSoftKeyboard(getActivity());
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.remove(this).commit();
        SingletonGenerate.getInstance().setVisible();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (SingletonCloseFragment.getInstance().isCloseWindow()){
            onCloseWindow();
            SingletonCloseFragment.getInstance().setUpdateData(false);
        }
        Log.d(TAG,"onResume");
    }

}
