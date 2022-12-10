package tpcreative.co.qrscanner.ui.create
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import com.basgeekball.awesomevalidation.AwesomeValidation
import com.basgeekball.awesomevalidation.ValidationStyle
import com.basgeekball.awesomevalidation.utility.RegexTemplate
import com.google.zxing.BarcodeFormat
import com.google.zxing.client.result.ParsedResultType
import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment
import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment.OnButtonWithNeutralClickListener
import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment.SimpleDateMonthAndDayFormatException
import kotlinx.android.synthetic.main.activity_event.*
import kotlinx.android.synthetic.main.activity_event.toolbar
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.GenerateSingleton.SingletonGenerateListener
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide
import tpcreative.co.qrscanner.common.extension.serializable
import tpcreative.co.qrscanner.model.*
import java.text.SimpleDateFormat
import java.util.*

class EventActivity : BaseActivitySlide(), View.OnClickListener, SingletonGenerateListener,OnEditorActionListener {
    private var mAwesomeValidation: AwesomeValidation? = null
    private var beginDateTimeMilliseconds: Long = 0
    private var endDateTimeMilliseconds: Long = 0
    private var currentMilliseconds: Long = 0
    private var dateTimeFragment: SwitchDateTimeDialogFragment? = null
    private var isClick = false
    private var isBegin = false
    private var save: GeneralModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        llEndTime.setOnClickListener(this)
        llBeginTime.setOnClickListener(this)
        tvBeginTime.setOnClickListener(this)
        tvEndTime.setOnClickListener(this)
        initDateTimePicker()
        val mData = intent?.serializable(getString(R.string.key_data),GeneralModel::class.java)
        if (mData != null) {
            save = mData
            onSetData()
        } else {
            Utils.Log(TAG, "Data is null")
        }
        edtTitle.setOnEditorActionListener(this)
        edtLocation.setOnEditorActionListener(this)
        edtDescription.setOnEditorActionListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_select, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_select -> {
                onSave()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onEditorAction(p0: TextView?, p1: Int, p2: KeyEvent?): Boolean {
        if (p1 == EditorInfo.IME_ACTION_DONE) {
            onSave()
            return  true
        }
        return false
    }

    private fun onSave(){
        hideSoftKeyBoard()
        if (mAwesomeValidation?.validate() == true) {
            if (beginDateTimeMilliseconds == 0L) {
                isBegin = true
                isClick = true
                dateTimeFragment?.startAtCalendarView()
                dateTimeFragment?.setAlertStyle(R.style.Theme_SwitchDateTime)
                val date = Date()
                val cal = Calendar.getInstance()
                currentMilliseconds = date.time
                cal.time = date
                val year = cal[Calendar.YEAR]
                val month = cal[Calendar.MONTH]
                val days = cal[Calendar.DAY_OF_MONTH]
                val hours = cal[Calendar.HOUR_OF_DAY]
                val minutes = cal[Calendar.MINUTE]
                dateTimeFragment?.setDefaultDateTime(GregorianCalendar(year, month, days, hours, minutes).time)
                dateTimeFragment?.show(supportFragmentManager, TAG_DATETIME_FRAGMENT)
                return
            }
            if (endDateTimeMilliseconds == 0L) {
                isBegin = false
                isClick = true
                dateTimeFragment?.startAtCalendarView()
                dateTimeFragment?.setAlertStyle(R.style.Theme_SwitchDateTime)
                val date = Date()
                val cal = Calendar.getInstance()
                currentMilliseconds = date.time
                cal.time = date
                val year = cal[Calendar.YEAR]
                val month = cal[Calendar.MONTH]
                val days = cal[Calendar.DAY_OF_MONTH]
                val hours = cal[Calendar.HOUR_OF_DAY]
                val minutes = cal[Calendar.MINUTE]
                dateTimeFragment?.setDefaultDateTime(GregorianCalendar(year, month, days, hours, minutes).time)
                dateTimeFragment?.show(supportFragmentManager, TAG_DATETIME_FRAGMENT)
                return
            }
            if (beginDateTimeMilliseconds > endDateTimeMilliseconds) {
                //Utils.showGotItSnackbar(edtTitle,"Ending event data time must be greater than begin date time");
                Utils.onDropDownAlert(this, "Ending event data time must be greater than begin date time")
                return
            }
            val currentTime = System.currentTimeMillis()
            if (beginDateTimeMilliseconds <= currentTime) {
                //Utils.showGotItSnackbar(edtTitle,"Starting event data time must be greater than current date time");
                Utils.onDropDownAlert(this, "Starting event data time must be greater than current date time")
                return
            }
            val create = GeneralModel(save)
            create.title = edtTitle.text.toString()
            create.location = edtLocation.text.toString()
            create.description = edtDescription.text.toString()
            create.startEvent = Utils.getCurrentDatetimeEvent(beginDateTimeMilliseconds)
            create.endEvent = Utils.getCurrentDatetimeEvent(endDateTimeMilliseconds)
            create.startEventMilliseconds = beginDateTimeMilliseconds
            create.endEventMilliseconds = endDateTimeMilliseconds
            create.createType = ParsedResultType.CALENDAR
            create.barcodeFormat = BarcodeFormat.QR_CODE.name
            Navigator.onMoveToReview(this, create)
        } else {
            Utils.Log(TAG, "error")
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.llBeginTime -> {
                if (!isClick) {
                    isBegin = true
                    isClick = true
                    dateTimeFragment?.startAtCalendarView()
                    dateTimeFragment?.setAlertStyle(R.style.Theme_SwitchDateTime)
                    val date = Date()
                    val cal = Calendar.getInstance()
                    currentMilliseconds = date.time
                    cal.time = date
                    val year = cal[Calendar.YEAR]
                    val month = cal[Calendar.MONTH]
                    val days = cal[Calendar.DAY_OF_MONTH]
                    val hours = cal[Calendar.HOUR_OF_DAY]
                    val minutes = cal[Calendar.MINUTE]
                    dateTimeFragment?.setDefaultDateTime(GregorianCalendar(year, month, days, hours, minutes).time)
                    dateTimeFragment?.show(supportFragmentManager, TAG_DATETIME_FRAGMENT)
                }
            }
            R.id.llEndTime -> {
                if (!isClick) {
                    isBegin = false
                    isClick = true
                    dateTimeFragment?.startAtCalendarView()
                    dateTimeFragment?.setAlertStyle(R.style.Theme_SwitchDateTime)
                    val date = Date()
                    val cal = Calendar.getInstance()
                    currentMilliseconds = date.time
                    cal.time = date
                    val year = cal[Calendar.YEAR]
                    val month = cal[Calendar.MONTH]
                    val days = cal[Calendar.DAY_OF_MONTH]
                    val hours = cal[Calendar.HOUR_OF_DAY]
                    val minutes = cal[Calendar.MINUTE]
                    dateTimeFragment?.setDefaultDateTime(GregorianCalendar(year, month, days, hours, minutes).time)
                    dateTimeFragment?.show(supportFragmentManager, TAG_DATETIME_FRAGMENT)
                }
            }
            R.id.tvBeginTime -> {
                if (!isClick) {
                    isBegin = true
                    isClick = true
                    dateTimeFragment?.startAtCalendarView()
                    dateTimeFragment?.setAlertStyle(R.style.Theme_SwitchDateTime)
                    val date = Date()
                    val cal = Calendar.getInstance()
                    currentMilliseconds = date.time
                    cal.time = date
                    val year = cal[Calendar.YEAR]
                    val month = cal[Calendar.MONTH]
                    val days = cal[Calendar.DAY_OF_MONTH]
                    val hours = cal[Calendar.HOUR_OF_DAY]
                    val minutes = cal[Calendar.MINUTE]
                    dateTimeFragment?.setDefaultDateTime(GregorianCalendar(year, month, days, hours, minutes).time)
                    dateTimeFragment?.show(supportFragmentManager, TAG_DATETIME_FRAGMENT)
                }
            }
            R.id.tvEndTime -> {
                if (!isClick) {
                    isBegin = false
                    isClick = true
                    dateTimeFragment?.startAtCalendarView()
                    dateTimeFragment?.setAlertStyle(R.style.Theme_SwitchDateTime)
                    val date = Date()
                    val cal = Calendar.getInstance()
                    currentMilliseconds = date.time
                    cal.time = date
                    val year = cal[Calendar.YEAR]
                    val month = cal[Calendar.MONTH]
                    val days = cal[Calendar.DAY_OF_MONTH]
                    val hours = cal[Calendar.HOUR_OF_DAY]
                    val minutes = cal[Calendar.MINUTE]
                    dateTimeFragment?.setDefaultDateTime(GregorianCalendar(year, month, days, hours, minutes).time)
                    dateTimeFragment?.show(supportFragmentManager, TAG_DATETIME_FRAGMENT)
                }
            }
            else -> {
            }
        }
    }

    /*Init Date time picker*/
    private fun initDateTimePicker() {
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
        dateTimeFragment?.setTimeZone(TimeZone.getDefault())

        // Init format
        val myDateFormat = SimpleDateFormat("d MMM yyyy HH:mm", Locale.getDefault())
        // Assign unmodifiable values
        dateTimeFragment?.set24HoursMode(false)
        dateTimeFragment?.setHighlightAMPMSelection(false)
        dateTimeFragment?.minimumDateTime = GregorianCalendar(2015, Calendar.JANUARY, 1).time
        dateTimeFragment?.maximumDateTime = GregorianCalendar(2025, Calendar.DECEMBER, 31).time

        // Define new day and month format
        try {
            dateTimeFragment?.simpleDateMonthAndDayFormat = SimpleDateFormat("MMMM dd", Locale.getDefault())
        } catch (e: SimpleDateMonthAndDayFormatException) {
            Utils.Log(TAG,"${e.message}")
        }

        // Set listener for date
        // Or use dateTimeFragment.setOnButtonClickListener(new SwitchDateTimeDialogFragment.OnButtonClickListener() {
        dateTimeFragment?.setOnButtonClickListener(object : OnButtonWithNeutralClickListener {
            override fun onPositiveButtonClick(date: Date?) {
                if (isBegin) {
                    if (currentMilliseconds > date?.time ?: 0) {
                        //Utils.showGotItSnackbar(edtTitle,"Starting event data time must be greater than current date time");
                        Utils.onDropDownAlert(this@EventActivity, "Starting event data time must be greater than current date time")
                    } else {
                        beginDateTimeMilliseconds = date?.time ?: 0
                        tvBeginTime.text = myDateFormat.format(date)
                    }
                } else {
                    if (currentMilliseconds > date?.time ?: 0) {
                        //Utils.showGotItSnackbar(edtTitle,"Ending event data time must be greater than current date time");
                        Utils.onDropDownAlert(this@EventActivity, "Ending event data time must be greater than current date time")
                    } else if (beginDateTimeMilliseconds >= date?.time ?: 0) {
                        //Utils.showGotItSnackbar(edtTitle,"Ending event data time must be greater than begin date time");
                        Utils.onDropDownAlert(this@EventActivity, "Ending event data time must be greater than begin date time")
                    } else {
                        endDateTimeMilliseconds = date?.time ?:0
                        tvEndTime.text = myDateFormat.format(date)
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
        mAwesomeValidation?.addValidation(this, R.id.edtTitle, RegexTemplate.NOT_EMPTY, R.string.err_title)
        mAwesomeValidation?.addValidation(this, R.id.edtLocation, RegexTemplate.NOT_EMPTY, R.string.err_location)
        mAwesomeValidation?.addValidation(this, R.id.edtDescription, RegexTemplate.NOT_EMPTY, R.string.err_description)
    }

    private fun focusUI() {
        edtTitle.requestFocus()
    }

    fun onSetData() {
        edtTitle.setText("${save?.title}")
        edtDescription.setText("${save?.description}")
        edtLocation.setText("${save?.location}")
        tvBeginTime.text = Utils.convertMillisecondsToDateTime(save?.startEventMilliseconds ?: 0)
        tvEndTime.text = Utils.convertMillisecondsToDateTime(save?.endEventMilliseconds ?: 0)
        beginDateTimeMilliseconds = save?.startEventMilliseconds ?: 0
        endDateTimeMilliseconds = save?.endEventMilliseconds ?: 0
        edtTitle.setSelection(edtTitle.text?.length ?: 0)
        hideSoftKeyBoard()
    }

    public override fun onStart() {
        super.onStart()
        Utils.Log(TAG, "onStart")
        mAwesomeValidation = AwesomeValidation(ValidationStyle.BASIC)
        mAwesomeValidation?.clear()
        addValidationForEditText()
        focusUI()
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
        GenerateSingleton.getInstance()?.setListener(null)
        Utils.Log(TAG, "onDestroy")
    }

    public override fun onResume() {
        super.onResume()
        GenerateSingleton.getInstance()?.setListener(this)
        Utils.Log(TAG, "onResume")
    }

    override fun onCompletedGenerate() {
        SaveSingleton.getInstance()?.reloadData()
        Utils.Log(TAG, "Finish...........")
        //finish()
    }

    companion object {
        private val TAG = EventActivity::class.java.simpleName

        /*Date time picker*/
        private val TAG_DATETIME_FRAGMENT: String = "TAG_DATETIME_FRAGMENT"
    }
}