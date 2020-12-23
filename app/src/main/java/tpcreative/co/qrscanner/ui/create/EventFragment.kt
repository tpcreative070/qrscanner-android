package tpcreative.co.qrscanner.ui.create

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import androidx.appcompat.widget.*
import butterknife.BindView
import com.basgeekball.awesomevalidation.AwesomeValidation
import com.basgeekball.awesomevalidation.ValidationStyle
import com.basgeekball.awesomevalidation.utility.RegexTemplate
import com.google.zxing.client.result.ParsedResultType
import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment
import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment.OnButtonWithNeutralClickListener
import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment.SimpleDateMonthAndDayFormatException
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.GenerateSingleton.SingletonGenerateListener
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide
import tpcreative.co.qrscanner.model.*
import tpcreative.co.qrscanner.ui.create.EventFragment
import java.text.SimpleDateFormat
import java.util.*

class EventFragment : BaseActivitySlide(), View.OnClickListener, SingletonGenerateListener {
    @BindView(R.id.llBeginTime)
    var llBeginTime: LinearLayout? = null

    @BindView(R.id.llEndTime)
    var llEndTime: LinearLayout? = null

    @BindView(R.id.edtTitle)
    var edtTitle: AppCompatEditText? = null

    @BindView(R.id.edtLocation)
    var edtLocation: AppCompatEditText? = null

    @BindView(R.id.edtDescription)
    var edtDescription: AppCompatEditText? = null

    @BindView(R.id.tvBeginTime)
    var tvBeginTime: AppCompatTextView? = null

    @BindView(R.id.tvEndTime)
    var tvEndTime: AppCompatTextView? = null
    private var mAwesomeValidation: AwesomeValidation? = null
    private var beginDateTimeMilliseconds: Long = 0
    private var endDateTimeMilliseconds: Long = 0
    private var currentMilliseconds: Long = 0
    private var dateTimeFragment: SwitchDateTimeDialogFragment? = null
    private var isClick = false
    private var isBegin = false
    private var save: SaveModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_event)
        val toolbar = findViewById<Toolbar?>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar.setDisplayHomeAsUpEnabled(true)
        llEndTime.setOnClickListener(this)
        llBeginTime.setOnClickListener(this)
        tvBeginTime.setOnClickListener(this)
        tvEndTime.setOnClickListener(this)
        initDateTimePicker()
        val bundle = intent.extras
        val mData = bundle.get(getString(R.string.key_data)) as SaveModel?
        if (mData != null) {
            save = mData
            onSetData()
        } else {
            Utils.Log(TAG, "Data is null")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_select, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item.getItemId()) {
            R.id.menu_item_select -> {
                if (mAwesomeValidation.validate()) {
                    if (beginDateTimeMilliseconds == 0L) {
                        isBegin = true
                        isClick = true
                        dateTimeFragment.startAtCalendarView()
                        dateTimeFragment.setAlertStyle(R.style.Theme_SwitchDateTime)
                        val date = Date()
                        val cal = Calendar.getInstance()
                        currentMilliseconds = date.time
                        cal.time = date
                        val year = cal[Calendar.YEAR]
                        val month = cal[Calendar.MONTH]
                        val days = cal[Calendar.DAY_OF_MONTH]
                        val hours = cal[Calendar.HOUR_OF_DAY]
                        val minutes = cal[Calendar.MINUTE]
                        dateTimeFragment.setDefaultDateTime(GregorianCalendar(year, month, days, hours, minutes).time)
                        dateTimeFragment.show(supportFragmentManager, TAG_DATETIME_FRAGMENT)
                        return true
                    }
                    if (endDateTimeMilliseconds == 0L) {
                        isBegin = false
                        isClick = true
                        dateTimeFragment.startAtCalendarView()
                        dateTimeFragment.setAlertStyle(R.style.Theme_SwitchDateTime)
                        val date = Date()
                        val cal = Calendar.getInstance()
                        currentMilliseconds = date.time
                        cal.time = date
                        val year = cal[Calendar.YEAR]
                        val month = cal[Calendar.MONTH]
                        val days = cal[Calendar.DAY_OF_MONTH]
                        val hours = cal[Calendar.HOUR_OF_DAY]
                        val minutes = cal[Calendar.MINUTE]
                        dateTimeFragment.setDefaultDateTime(GregorianCalendar(year, month, days, hours, minutes).time)
                        dateTimeFragment.show(supportFragmentManager, TAG_DATETIME_FRAGMENT)
                        return true
                    }
                    if (beginDateTimeMilliseconds > endDateTimeMilliseconds) {
                        //Utils.showGotItSnackbar(edtTitle,"Ending event data time must be greater than begin date time");
                        Utils.onDropDownAlert(this, "Ending event data time must be greater than begin date time")
                        return true
                    }
                    val currentTime = System.currentTimeMillis()
                    if (beginDateTimeMilliseconds <= currentTime) {
                        //Utils.showGotItSnackbar(edtTitle,"Starting event data time must be greater than current date time");
                        Utils.onDropDownAlert(this, "Starting event data time must be greater than current date time")
                        return true
                    }
                    val create = Create(save)
                    create.title = edtTitle.getText().toString()
                    create.location = edtLocation.getText().toString()
                    create.description = edtDescription.getText().toString()
                    create.startEvent = Utils.getCurrentDatetimeEvent(beginDateTimeMilliseconds)
                    create.endEvent = Utils.getCurrentDatetimeEvent(endDateTimeMilliseconds)
                    create.startEventMilliseconds = beginDateTimeMilliseconds
                    create.endEventMilliseconds = endDateTimeMilliseconds
                    create.createType = ParsedResultType.CALENDAR
                    Navigator.onMoveToReview(this, create)
                } else {
                    Utils.Log(TAG, "error")
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(view: View?) {
        when (view.getId()) {
            R.id.llBeginTime -> {
                if (!isClick) {
                    isBegin = true
                    isClick = true
                    dateTimeFragment.startAtCalendarView()
                    dateTimeFragment.setAlertStyle(R.style.Theme_SwitchDateTime)
                    val date = Date()
                    val cal = Calendar.getInstance()
                    currentMilliseconds = date.time
                    cal.time = date
                    val year = cal[Calendar.YEAR]
                    val month = cal[Calendar.MONTH]
                    val days = cal[Calendar.DAY_OF_MONTH]
                    val hours = cal[Calendar.HOUR_OF_DAY]
                    val minutes = cal[Calendar.MINUTE]
                    dateTimeFragment.setDefaultDateTime(GregorianCalendar(year, month, days, hours, minutes).time)
                    dateTimeFragment.show(supportFragmentManager, TAG_DATETIME_FRAGMENT)
                }
            }
            R.id.llEndTime -> {
                if (!isClick) {
                    isBegin = false
                    isClick = true
                    dateTimeFragment.startAtCalendarView()
                    dateTimeFragment.setAlertStyle(R.style.Theme_SwitchDateTime)
                    val date = Date()
                    val cal = Calendar.getInstance()
                    currentMilliseconds = date.time
                    cal.time = date
                    val year = cal[Calendar.YEAR]
                    val month = cal[Calendar.MONTH]
                    val days = cal[Calendar.DAY_OF_MONTH]
                    val hours = cal[Calendar.HOUR_OF_DAY]
                    val minutes = cal[Calendar.MINUTE]
                    dateTimeFragment.setDefaultDateTime(GregorianCalendar(year, month, days, hours, minutes).time)
                    dateTimeFragment.show(supportFragmentManager, TAG_DATETIME_FRAGMENT)
                }
            }
            R.id.tvBeginTime -> {
                if (!isClick) {
                    isBegin = true
                    isClick = true
                    dateTimeFragment.startAtCalendarView()
                    dateTimeFragment.setAlertStyle(R.style.Theme_SwitchDateTime)
                    val date = Date()
                    val cal = Calendar.getInstance()
                    currentMilliseconds = date.time
                    cal.time = date
                    val year = cal[Calendar.YEAR]
                    val month = cal[Calendar.MONTH]
                    val days = cal[Calendar.DAY_OF_MONTH]
                    val hours = cal[Calendar.HOUR_OF_DAY]
                    val minutes = cal[Calendar.MINUTE]
                    dateTimeFragment.setDefaultDateTime(GregorianCalendar(year, month, days, hours, minutes).time)
                    dateTimeFragment.show(supportFragmentManager, TAG_DATETIME_FRAGMENT)
                }
            }
            R.id.tvEndTime -> {
                if (!isClick) {
                    isBegin = false
                    isClick = true
                    dateTimeFragment.startAtCalendarView()
                    dateTimeFragment.setAlertStyle(R.style.Theme_SwitchDateTime)
                    val date = Date()
                    val cal = Calendar.getInstance()
                    currentMilliseconds = date.time
                    cal.time = date
                    val year = cal[Calendar.YEAR]
                    val month = cal[Calendar.MONTH]
                    val days = cal[Calendar.DAY_OF_MONTH]
                    val hours = cal[Calendar.HOUR_OF_DAY]
                    val minutes = cal[Calendar.MINUTE]
                    dateTimeFragment.setDefaultDateTime(GregorianCalendar(year, month, days, hours, minutes).time)
                    dateTimeFragment.show(supportFragmentManager, TAG_DATETIME_FRAGMENT)
                }
            }
            else -> {
            }
        }
    }

    /*Init Date time picker*/
    fun initDateTimePicker() {
        // Construct SwitchDateTimePicker
        dateTimeFragment = supportFragmentManager.findFragmentByTag(TAG_DATETIME_FRAGMENT) as SwitchDateTimeDialogFragment?
        if (dateTimeFragment == null) {
            dateTimeFragment = SwitchDateTimeDialogFragment.newInstance(
                    getString(R.string.label_datetime_dialog),
                    getString(android.R.string.ok),
                    getString(android.R.string.cancel),
                    getString(R.string.clean) // Optional
            )
        }

        // Optionally define a timezone
        dateTimeFragment.setTimeZone(TimeZone.getDefault())

        // Init format
        val myDateFormat = SimpleDateFormat("d MMM yyyy HH:mm", Locale.getDefault())
        // Assign unmodifiable values
        dateTimeFragment.set24HoursMode(false)
        dateTimeFragment.setHighlightAMPMSelection(false)
        dateTimeFragment.setMinimumDateTime(GregorianCalendar(2015, Calendar.JANUARY, 1).time)
        dateTimeFragment.setMaximumDateTime(GregorianCalendar(2025, Calendar.DECEMBER, 31).time)

        // Define new day and month format
        try {
            dateTimeFragment.setSimpleDateMonthAndDayFormat(SimpleDateFormat("MMMM dd", Locale.getDefault()))
        } catch (e: SimpleDateMonthAndDayFormatException) {
            Log.e(TAG, e.message)
        }

        // Set listener for date
        // Or use dateTimeFragment.setOnButtonClickListener(new SwitchDateTimeDialogFragment.OnButtonClickListener() {
        dateTimeFragment.setOnButtonClickListener(object : OnButtonWithNeutralClickListener {
            override fun onPositiveButtonClick(date: Date?) {
                if (isBegin) {
                    if (currentMilliseconds > date.getTime()) {
                        //Utils.showGotItSnackbar(edtTitle,"Starting event data time must be greater than current date time");
                        Utils.onDropDownAlert(this@EventFragment, "Starting event data time must be greater than current date time")
                    } else {
                        beginDateTimeMilliseconds = date.getTime()
                        tvBeginTime.setText(myDateFormat.format(date))
                    }
                } else {
                    if (currentMilliseconds > date.getTime()) {
                        //Utils.showGotItSnackbar(edtTitle,"Ending event data time must be greater than current date time");
                        Utils.onDropDownAlert(this@EventFragment, "Ending event data time must be greater than current date time")
                    } else if (beginDateTimeMilliseconds >= date.getTime()) {
                        //Utils.showGotItSnackbar(edtTitle,"Ending event data time must be greater than begin date time");
                        Utils.onDropDownAlert(this@EventFragment, "Ending event data time must be greater than begin date time")
                    } else {
                        endDateTimeMilliseconds = date.getTime()
                        tvEndTime.setText(myDateFormat.format(date))
                    }
                }
                isClick = false
            }

            override fun onNegativeButtonClick(date: Date?) {
                // Do nothing
                isClick = false
            }

            override fun onNeutralButtonClick(date: Date?) {
                // Optional if neutral button does'nt exists
                isClick = false
            }

            override fun onDismiss() {
                isClick = false
            }
        })
    }

    private fun addValidationForEditText() {
        mAwesomeValidation.addValidation(this, R.id.edtTitle, RegexTemplate.NOT_EMPTY, R.string.err_title)
        mAwesomeValidation.addValidation(this, R.id.edtLocation, RegexTemplate.NOT_EMPTY, R.string.err_location)
        mAwesomeValidation.addValidation(this, R.id.edtDescription, RegexTemplate.NOT_EMPTY, R.string.err_description)
    }

    fun FocusUI() {
        edtTitle.requestFocus()
    }

    fun onSetData() {
        edtTitle.setText("" + save.title)
        edtDescription.setText("" + save.description)
        edtLocation.setText(save.location)
        tvBeginTime.setText(Utils.convertMillisecondsToDateTime(save.startEventMilliseconds))
        tvEndTime.setText(Utils.convertMillisecondsToDateTime(save.endEventMilliseconds))
        beginDateTimeMilliseconds = save.startEventMilliseconds
        endDateTimeMilliseconds = save.endEventMilliseconds
    }

    public override fun onStart() {
        super.onStart()
        Utils.Log(TAG, "onStart")
        mAwesomeValidation = AwesomeValidation(ValidationStyle.BASIC)
        mAwesomeValidation.clear()
        addValidationForEditText()
        if (save != null) {
            onSetData()
        }
        FocusUI()
        Utils.Log(TAG, "current time : " + Utils.getCurrentDatetimeEvent())
    }

    public override fun onStop() {
        super.onStop()
        Utils.Log(TAG, "onStop")
    }

    public override fun onPause() {
        super.onPause()
        isClick = false
        Utils.Log(TAG, "onPause")
    }

    public override fun onDestroy() {
        super.onDestroy()
        GenerateSingleton.Companion.getInstance().setListener(null)
        Log.d(TAG, "onDestroy")
    }

    public override fun onResume() {
        super.onResume()
        GenerateSingleton.Companion.getInstance().setListener(this)
        Log.d(TAG, "onResume")
    }

    override fun onCompletedGenerate() {
        SaveSingleton.Companion.getInstance().reloadData()
        Utils.Log(TAG, "Finish...........")
        finish()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == Navigator.CREATE) {
            Utils.Log(TAG, "Finish...........")
            SaveSingleton.Companion.getInstance().reloadData()
            finish()
        }
    }

    companion object {
        private val TAG = EventFragment::class.java.simpleName

        /*Date time picker*/
        private val TAG_DATETIME_FRAGMENT: String? = "TAG_DATETIME_FRAGMENT"
    }
}