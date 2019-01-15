package tpcreative.co.qrscanner.ui.create;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.Navigator;
import tpcreative.co.qrscanner.common.SingletonGenerate;
import tpcreative.co.qrscanner.common.SingletonSave;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide;
import tpcreative.co.qrscanner.model.Create;
import tpcreative.co.qrscanner.model.EnumImplement;
import tpcreative.co.qrscanner.model.Save;

public class EventFragment extends BaseActivitySlide implements View.OnClickListener , SingletonGenerate.SingletonGenerateListener{

    private static final String TAG = EventFragment.class.getSimpleName();
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
    private AwesomeValidation mAwesomeValidation;
    private long beginDateTimeMilliseconds = 0;
    private long endDateTimeMilliseconds = 0;
    private long currentMilliseconds = 0;


    /*Date time picker*/
    private static final String TAG_DATETIME_FRAGMENT = "TAG_DATETIME_FRAGMENT";
    private SwitchDateTimeDialogFragment dateTimeFragment;

    private boolean isClick ;
    private boolean isBegin;
    private Save save;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_event);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        llEndTime.setOnClickListener(this);
        llBeginTime.setOnClickListener(this);
        tvBeginTime.setOnClickListener(this);
        tvEndTime.setOnClickListener(this);
        initDateTimePicker();
        Bundle bundle = getIntent().getExtras();
        final Save mData = (Save) bundle.get(getString(R.string.key_data));
        if (mData!=null){
            save = mData;
            onSetData();
        }
        else{
            Utils.Log(TAG,"Data is null");
        }
        onDrawOverLay(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_select, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_item_select:{
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
                        dateTimeFragment.show(getSupportFragmentManager(), TAG_DATETIME_FRAGMENT);
                        return true;
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
                        dateTimeFragment.show(getSupportFragmentManager(), TAG_DATETIME_FRAGMENT);
                        return true;
                    }

                    if (beginDateTimeMilliseconds>endDateTimeMilliseconds){
                        Utils.showGotItSnackbar(edtTitle,"Ending event data time must be greater than begin date time");
                        return true;
                    }

                    long currentTime = System.currentTimeMillis();
                    if (beginDateTimeMilliseconds <= currentTime){
                        Utils.showGotItSnackbar(edtTitle,"Starting event data time must be greater than current date time");
                        return true;
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
                    create.enumImplement = (save != null) ? EnumImplement.EDIT : EnumImplement.CREATE ;
                    create.id = (save != null) ? save.id : 0 ;
                    Navigator.onMoveToReview(this, create);
                } else {
                    Utils.Log(TAG, "error");
                }
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
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
                    dateTimeFragment.show(getSupportFragmentManager(), TAG_DATETIME_FRAGMENT);
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
                    dateTimeFragment.show(getSupportFragmentManager(), TAG_DATETIME_FRAGMENT);
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
                    dateTimeFragment.show(getSupportFragmentManager(), TAG_DATETIME_FRAGMENT);

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
                    dateTimeFragment.show(getSupportFragmentManager(), TAG_DATETIME_FRAGMENT);
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
        dateTimeFragment = (SwitchDateTimeDialogFragment) getSupportFragmentManager().findFragmentByTag(TAG_DATETIME_FRAGMENT);
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
                        Utils.showGotItSnackbar(edtTitle,"Starting event data time must be greater than current date time");
                    }
                    else {
                        beginDateTimeMilliseconds = date.getTime();
                        tvBeginTime.setText(myDateFormat.format(date));
                    }
                }
                else{
                    if (currentMilliseconds>date.getTime()){
                        Utils.showGotItSnackbar(edtTitle,"Ending event data time must be greater than current date time");
                    }
                    else if(beginDateTimeMilliseconds >= date.getTime()){
                        Utils.showGotItSnackbar(edtTitle,"Ending event data time must be greater than begin date time");
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

            @Override
            public void onDismiss() {
                isClick = false;
            }
        });
    }

    private void addValidationForEditText() {
        mAwesomeValidation.addValidation(this, R.id.edtTitle, RegexTemplate.NOT_EMPTY, R.string.err_title);
        mAwesomeValidation.addValidation(this, R.id.edtLocation, RegexTemplate.NOT_EMPTY, R.string.err_location);
        mAwesomeValidation.addValidation(this, R.id.edtDescription, RegexTemplate.NOT_EMPTY, R.string.err_description);
    }


    public void FocusUI(){
        edtTitle.requestFocus();
    }


    public void onSetData(){
        edtTitle.setText(""+save.title);
        edtDescription.setText(""+save.description);
        edtLocation.setText(save.location);
        tvBeginTime.setText(Utils.convertMillisecondsToDateTime(save.startEventMilliseconds));
        tvEndTime.setText(Utils.convertMillisecondsToDateTime(save.endEventMilliseconds));
        beginDateTimeMilliseconds = save.startEventMilliseconds;
        endDateTimeMilliseconds = save.endEventMilliseconds;
    }

    @Override
    public void onStart() {
        super.onStart();
        Utils.Log(TAG, "onStart");
        mAwesomeValidation = new AwesomeValidation(ValidationStyle.BASIC);
        mAwesomeValidation.clear();
        addValidationForEditText();
        if (save!=null){
            onSetData();
        }
        FocusUI();
        Utils.Log(TAG,"current time : " + Utils.getCurrentDatetimeEvent());
    }

    @Override
    public void onStop() {
        super.onStop();
        Utils.Log(TAG, "onStop");
    }

    @Override
    public void onPause() {
        super.onPause();
        isClick = false;
        Utils.Log(TAG, "onPause");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SingletonGenerate.getInstance().setListener(null);
        Log.d(TAG,"onDestroy");
    }

    @Override
    public void onResume() {
        super.onResume();
        SingletonGenerate.getInstance().setListener(this);
        Log.d(TAG,"onResume");
    }

    @Override
    public void onCompletedGenerate() {
        SingletonSave.getInstance().reLoadData();
        Utils.Log(TAG,"Finish...........");
        finish();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == Navigator.CREATE) {
            Utils.Log(TAG,"Finish...........");
            SingletonSave.getInstance().reLoadData();
            finish();
        }
    }


}
