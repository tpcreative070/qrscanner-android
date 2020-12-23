package tpcreative.co.qrscanner.ui.scannerresult

import android.Manifest
import android.R
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.appcompat.widget.Toolbar
import com.karumi.dexter.listener.PermissionRequest
import de.mrapp.android.dialog.MaterialDialog
import tpcreative.co.qrscanner.BuildConfig
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.adapter.DividerItemDecoration
import tpcreative.co.qrscanner.model.Create
import tpcreative.co.qrscanner.model.Theme
import java.io.File
import java.net.URLEncoder
import java.util.*

class ScannerResultFragment : BaseActivitySlide(), ScannerResultView, UtilsListener, ScannerResultAdapter.ItemSelectedListener {
    private var presenter: ScannerResultPresenter? = null
    private var create: Create? = null
    var mList: MutableList<LinearLayout?>? = ArrayList<LinearLayout?>()
    private var history: HistoryModel? = HistoryModel()

    /*Email*/
    @BindView(R.id.llEmail)
    var llEmail: LinearLayout? = null

    @BindView(R.id.emailTo)
    var emailTo: AppCompatTextView? = null

    @BindView(R.id.emailSubject)
    var emailSubject: AppCompatTextView? = null

    @BindView(R.id.emailMessage)
    var emailMessage: AppCompatTextView? = null

    /*SMS*/
    @BindView(R.id.llSMS)
    var llSMS: LinearLayout? = null

    @BindView(R.id.smsTo)
    var smsTo: AppCompatTextView? = null

    @BindView(R.id.smsMessage)
    var smsMessage: AppCompatTextView? = null

    /*Contact*/
    @BindView(R.id.llContact)
    var llContact: LinearLayout? = null

    @BindView(R.id.contactFullName)
    var contactFullName: AppCompatTextView? = null

    @BindView(R.id.contactAddress)
    var contactAddress: AppCompatTextView? = null

    @BindView(R.id.contactPhone)
    var contactPhone: AppCompatTextView? = null

    @BindView(R.id.contactEmail)
    var contactEmail: AppCompatTextView? = null

    /*Location*/
    @BindView(R.id.llLocation)
    var llLocation: LinearLayout? = null

    @BindView(R.id.locationLatitude)
    var locationLatitude: AppCompatTextView? = null

    @BindView(R.id.locationLongitude)
    var locationLongitude: AppCompatTextView? = null

    @BindView(R.id.locationQuery)
    var locationQuery: AppCompatTextView? = null

    /*Event*/
    @BindView(R.id.llEvent)
    var llEvent: LinearLayout? = null

    @BindView(R.id.eventTitle)
    var eventTitle: AppCompatTextView? = null

    @BindView(R.id.eventLocation)
    var eventLocation: AppCompatTextView? = null

    @BindView(R.id.eventDescription)
    var eventDescription: AppCompatTextView? = null

    @BindView(R.id.eventBeginTime)
    var eventBeginTime: AppCompatTextView? = null

    @BindView(R.id.eventEndTime)
    var eventEndTime: AppCompatTextView? = null

    /*Wifi*/
    @BindView(R.id.llWifi)
    var llWifi: LinearLayout? = null

    @BindView(R.id.wifiSSID)
    var wifiSSID: AppCompatTextView? = null

    @BindView(R.id.wifiPassword)
    var wifiPassword: AppCompatTextView? = null

    @BindView(R.id.wifiNetworkEncryption)
    var wifiNetworkEncryption: AppCompatTextView? = null

    @BindView(R.id.wifiHidden)
    var wifiHidden: AppCompatTextView? = null

    /*Telephone*/
    @BindView(R.id.llTelephone)
    var llTelephone: LinearLayout? = null

    @BindView(R.id.telephoneNumber)
    var telephoneNumber: AppCompatTextView? = null

    /*Text*/
    @BindView(R.id.llText)
    var llText: LinearLayout? = null

    @BindView(R.id.textMessage)
    var textMessage: AppCompatTextView? = null

    /*URL*/
    @BindView(R.id.llURL)
    var llURL: LinearLayout? = null

    @BindView(R.id.urlAddress)
    var urlAddress: AppCompatTextView? = null

    /*ISBN*/
    @BindView(R.id.llISBN)
    var llISBN: LinearLayout? = null

    @BindView(R.id.textISBN)
    var textISBN: AppCompatTextView? = null

    /*Product*/
    @BindView(R.id.llProduct)
    var llProduct: LinearLayout? = null

    @BindView(R.id.textProduct)
    var textProduct: AppCompatTextView? = null

    @BindView(R.id.recyclerView)
    var recyclerView: RecyclerView? = null

    @BindView(R.id.scrollView)
    var scrollView: NestedScrollView? = null

    @BindView(R.id.llAds)
    var llAds: LinearLayout? = null

    @BindView(R.id.rlAdsRoot)
    var rlAdsRoot: RelativeLayout? = null

    @BindView(R.id.viewSeparateLine)
    var viewSeparateLine: View? = null
    private var adapter: ScannerResultAdapter? = null
    var llm: LinearLayoutManager? = null
    private var code: String? = null
    private var bitmap: Bitmap? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_review)
        val toolbar: Toolbar = findViewById<Toolbar?>(R.id.toolbar)
        setSupportActionBar(toolbar)
        getSupportActionBar().setDisplayHomeAsUpEnabled(true)
        scrollView.smoothScrollTo(0, 0)
        mList.add(llEmail)
        mList.add(llSMS)
        mList.add(llContact)
        mList.add(llLocation)
        mList.add(llEvent)
        mList.add(llWifi)
        mList.add(llTelephone)
        mList.add(llText)
        mList.add(llURL)
        mList.add(llProduct)
        mList.add(llISBN)
        presenter = ScannerResultPresenter()
        presenter.bindView(this)
        setupRecyclerViewItem()
        presenter.getIntent(this)
        if (QRScannerApplication.Companion.getInstance().isRequestLargeAds() && !Utils.isPremium() && Utils.isLiveAds()) {
            QRScannerApplication.Companion.getInstance().getAdsLargeView(this)
        }
        presenter.doShowAds()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.menu_result, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item.getItemId()) {
            R.id.menu_item_report -> {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + "care@tpcreative.me"))
                    intent.putExtra(Intent.EXTRA_SUBJECT, "QRScanner App Support(Report issues)")
                    intent.putExtra(Intent.EXTRA_TEXT, "")
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    //TODO smth
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun setupRecyclerViewItem() {
        llm = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.setLayoutManager(llm)
        recyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        adapter = ScannerResultAdapter(getLayoutInflater(), this, this)
        recyclerView.setAdapter(adapter)
        recyclerView.setNestedScrollingEnabled(false)
    }

    override fun onClickItem(position: Int) {
        val navigation: ItemNavigation? = presenter.mListItemNavigation[position]
        if (navigation != null) {
            when (navigation.enumAction) {
                EnumAction.CLIPBOARD -> {
                    onClipboardDialog()
                }
                EnumAction.SEARCH -> {
                    val result = presenter.result ?: return
                    when (result.createType) {
                        ParsedResultType.URI -> {
                            onSearch(result.url)
                        }
                        ParsedResultType.PRODUCT -> {
                            onSearch(result.productId)
                        }
                        ParsedResultType.ISBN -> {
                            onSearch(result.ISBN)
                        }
                        ParsedResultType.TEXT -> {
                            onSearch(result.text)
                        }
                    }
                }
                else -> {
                    onAddPermissionSave()
                }
            }
        }
    }

    override fun onReloadData() {
        adapter.setDataSource(presenter.mListItemNavigation)
    }

    fun onAddPermissionPhoneCall() {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.CALL_PHONE)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report.areAllPermissionsGranted()) {
                            Utils.Log(TAG, "Action here phone call")
                            if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER) {
                                val intentPhoneCall = Intent(Intent.ACTION_CALL, Uri.parse("tel:" + create.phone))
                                startActivity(intentPhoneCall)
                            } else {
                                code = "tel:" + create.phone + ""
                                onGenerateCode(code, EnumAction.SHARE)
                            }
                        } else {
                            Utils.Log(TAG, "Permission is denied")
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            /*Miss add permission in manifest*/
                            Utils.Log(TAG, "request permission is failed")
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest?>?, token: PermissionToken?) {
                        /* ... */
                        token.continuePermissionRequest()
                    }
                })
                .withErrorListener(object : PermissionRequestErrorListener {
                    override fun onError(error: DexterError?) {
                        Utils.Log(TAG, "error ask permission")
                    }
                }).onSameThread().check()
    }

    fun onAddPermissionSave() {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report.areAllPermissionsGranted()) {
                            create = presenter.result
                            try {
                                when (create.createType) {
                                    ParsedResultType.ADDRESSBOOK -> if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER) {
                                        val intentContact = Intent()
                                        intentContact.setAction(ContactsContract.Intents.SHOW_OR_CREATE_CONTACT)
                                        intentContact.setData(Uri.fromParts("tel", create.phone, null))
                                        intentContact.putExtra(ContactsContract.Intents.Insert.NAME, create.fullName)
                                        intentContact.putExtra(ContactsContract.Intents.Insert.POSTAL, create.address)
                                        intentContact.putExtra(ContactsContract.Intents.Insert.PHONE, create.phone)
                                        intentContact.putExtra(ContactsContract.Intents.Insert.EMAIL, create.email)
                                        startActivity(intentContact)
                                    } else {
                                        code = "MECARD:N:" + create.fullName + ";TEL:" + create.phone + ";EMAIL:" + create.email + ";ADR:" + create.address + ";"
                                        onGenerateCode(code, EnumAction.SHARE)
                                    }
                                    ParsedResultType.EMAIL_ADDRESS -> if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER) {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + create.email))
                                            intent.putExtra(Intent.EXTRA_SUBJECT, create.subject)
                                            intent.putExtra(Intent.EXTRA_TEXT, create.message)
                                            startActivity(intent)
                                        } catch (e: ActivityNotFoundException) {
                                            //TODO smth
                                        }
                                    } else {
                                        code = "MATMSG:TO:" + create.email + ";SUB:" + create.subject + ";BODY:" + create.message + ";"
                                        onGenerateCode(code, EnumAction.SHARE)
                                    }
                                    ParsedResultType.PRODUCT -> if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER) {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:"))
                                        intent.putExtra("sms_body", create.productId)
                                        startActivity(intent)
                                    } else {
                                        code = create.productId
                                        onGenerateCode(code, EnumAction.SHARE)
                                    }
                                    ParsedResultType.URI -> if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER) {
                                        onOpenWebSites(create.url)
                                    } else {
                                        code = create.url
                                        onGenerateCode(code, EnumAction.SHARE)
                                    }
                                    ParsedResultType.WIFI -> if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER) {
                                        startActivity(Intent(WifiManager.ACTION_PICK_WIFI_NETWORK))
                                    } else {
                                        code = "WIFI:S:" + create.ssId + ";T:" + create.password + ";P:" + create.networkEncryption + ";H:" + create.hidden + ";"
                                        onGenerateCode(code, EnumAction.SHARE)
                                    }
                                    ParsedResultType.GEO -> if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER) {
                                        val uri = "geo:" + create.lat + "," + create.lon + ""
                                        val intentMap = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                                        intentMap.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity")
                                        startActivity(intentMap)
                                    } else {
                                        code = "geo:" + create.lat + "," + create.lon + "?q=" + create.query + ""
                                        onGenerateCode(code, EnumAction.SHARE)
                                    }
                                    ParsedResultType.TEL -> onAddPermissionPhoneCall()
                                    ParsedResultType.SMS -> if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER) {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + create.phone))
                                        intent.putExtra("sms_body", create.message)
                                        startActivity(intent)
                                    } else {
                                        code = "smsto:" + create.phone + ":" + create.message
                                        onGenerateCode(code, EnumAction.SHARE)
                                    }
                                    ParsedResultType.CALENDAR -> if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER) {
                                        val intentCalendar = Intent(Intent.ACTION_INSERT)
                                        intentCalendar.setData(CalendarContract.Events.CONTENT_URI)
                                        intentCalendar.putExtra(CalendarContract.Events.TITLE, create.title)
                                        intentCalendar.putExtra(CalendarContract.Events.DESCRIPTION, create.description)
                                        intentCalendar.putExtra(CalendarContract.Events.EVENT_LOCATION, create.location)
                                        intentCalendar.putExtra(CalendarContract.Events.ALL_DAY, false)
                                        intentCalendar.putExtra(
                                                CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                                                create.startEventMilliseconds)
                                        intentCalendar.putExtra(
                                                CalendarContract.EXTRA_EVENT_END_TIME, create.endEventMilliseconds)
                                        startActivity(intentCalendar)
                                    } else {
                                        val builder = StringBuilder()
                                        builder.append("BEGIN:VEVENT")
                                        builder.append("\n")
                                        builder.append("SUMMARY:" + create.title)
                                        builder.append("\n")
                                        builder.append("DTSTART:" + create.startEvent)
                                        builder.append("\n")
                                        builder.append("DTEND:" + create.endEvent)
                                        builder.append("\n")
                                        builder.append("LOCATION:" + create.location)
                                        builder.append("\n")
                                        builder.append("DESCRIPTION:" + create.description)
                                        builder.append("\n")
                                        builder.append("END:VEVENT")
                                        code = builder.toString()
                                        onGenerateCode(code, EnumAction.SHARE)
                                    }
                                    ParsedResultType.ISBN -> if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER) {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:"))
                                        intent.putExtra("sms_body", create.ISBN)
                                        startActivity(intent)
                                    } else {
                                        code = create.ISBN
                                        onGenerateCode(code, EnumAction.SHARE)
                                    }
                                    else -> if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER) {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:"))
                                        intent.putExtra("sms_body", create.text)
                                        startActivity(intent)
                                    } else {
                                        code = create.text
                                        onGenerateCode(code, EnumAction.SHARE)
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        } else {
                            Utils.Log(TAG, "Permission is denied")
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            /*Miss add permission in manifest*/
                            Utils.Log(TAG, "request permission is failed")
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest?>?, token: PermissionToken?) {
                        /* ... */
                        token.continuePermissionRequest()
                    }
                })
                .withErrorListener(object : PermissionRequestErrorListener {
                    override fun onError(error: DexterError?) {
                        Log.d(TAG, "error ask permission")
                    }
                }).onSameThread().check()
    }

    override fun setView() {
        create = presenter.result
        when (create.createType) {
            ParsedResultType.ADDRESSBOOK -> {
                /*Put item to HashClipboard*/presenter.hashClipboard["fullName"] = create.fullName
                presenter.hashClipboard["address"] = create.address
                presenter.hashClipboard["phone"] = create.phone
                presenter.hashClipboard["email"] = create.email
                contactFullName.setText(create.fullName)
                contactAddress.setText(create.address)
                contactPhone.setText(create.phone)
                contactEmail.setText(create.email)
                history = HistoryModel()
                history.fullName = create.fullName
                history.address = create.address
                history.phone = create.phone
                history.email = create.email
                history.createType = create.createType.name
                if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER) {
                    presenter.mListItemNavigation.add(ItemNavigation(create.createType, create.fragmentType, EnumAction.Other, R.drawable.baseline_perm_contact_calendar_white_48, "AddressBook"))
                } else {
                    presenter.mListItemNavigation.add(ItemNavigation(create.createType, create.fragmentType, EnumAction.SHARE, R.drawable.baseline_share_white_48, "Share"))
                }
                onShowUI(llContact)
                setTitle("AddressBook")
            }
            ParsedResultType.EMAIL_ADDRESS -> {
                /*Put item to HashClipboard*/presenter.hashClipboard["email"] = create.email
                presenter.hashClipboard["subject"] = create.subject
                presenter.hashClipboard["message"] = create.message
                emailTo.setText(create.email)
                emailSubject.setText(create.subject)
                emailMessage.setText(create.message)
                history = HistoryModel()
                history.email = create.email
                history.subject = create.subject
                history.message = create.message
                history.createType = create.createType.name
                if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER) {
                    presenter.mListItemNavigation.add(ItemNavigation(create.createType, create.fragmentType, EnumAction.Other, R.drawable.baseline_email_white_48, "Email"))
                } else {
                    presenter.mListItemNavigation.add(ItemNavigation(create.createType, create.fragmentType, EnumAction.SHARE, R.drawable.baseline_share_white_48, "Share"))
                }
                onShowUI(llEmail)
                setTitle("Email")
            }
            ParsedResultType.PRODUCT -> {
                /*Put item to HashClipboard*/presenter.hashClipboard["productId"] = create.productId
                textProduct.setText(create.productId)
                history = HistoryModel()
                history.text = create.productId
                history.createType = create.createType.name
                history.barcodeFormat = create.barcodeFormat
                presenter.mListItemNavigation.add(ItemNavigation(create.createType, create.fragmentType, EnumAction.SEARCH, R.drawable.baseline_search_white_48, "Search"))
                if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER) {
                    presenter.mListItemNavigation.add(ItemNavigation(create.createType, create.fragmentType, EnumAction.Other, R.drawable.baseline_textsms_white_48, "Product"))
                } else {
                    presenter.mListItemNavigation.add(ItemNavigation(create.createType, create.fragmentType, EnumAction.SHARE, R.drawable.baseline_share_white_48, "Share"))
                }
                onShowUI(llProduct)
                setTitle("Product")
            }
            ParsedResultType.URI -> {
                /*Put item to HashClipboard*/presenter.hashClipboard["url"] = create.url
                urlAddress.setText(create.url)
                history = HistoryModel()
                history.url = create.url
                history.createType = create.createType.name
                presenter.mListItemNavigation.add(ItemNavigation(create.createType, create.fragmentType, EnumAction.SEARCH, R.drawable.baseline_search_white_48, "Search"))
                if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER) {
                    presenter.mListItemNavigation.add(ItemNavigation(create.createType, create.fragmentType, EnumAction.Other, R.drawable.baseline_language_white_48, "Url"))
                } else {
                    presenter.mListItemNavigation.add(ItemNavigation(create.createType, create.fragmentType, EnumAction.SHARE, R.drawable.baseline_share_white_48, "Share"))
                }
                onShowUI(llURL)
                setTitle("Url")
                val isAutoOpening: Boolean = PrefsController.getBoolean(getString(R.string.key_auto_navigate_to_browser), false)
                if (isAutoOpening) {
                    onOpenWebSites(create.url)
                }
            }
            ParsedResultType.WIFI -> {
                /*Put item to HashClipboard*/presenter.hashClipboard["ssId"] = create.ssId
                presenter.hashClipboard["password"] = create.password
                presenter.hashClipboard["networkEncryption"] = create.networkEncryption
                presenter.hashClipboard["hidden"] = if (create.hidden) "Yes" else "No"
                wifiSSID.setText(create.ssId)
                wifiPassword.setText(create.password)
                wifiNetworkEncryption.setText(create.networkEncryption)
                wifiHidden.setText(if (create.hidden) "Yes" else "No")
                history = HistoryModel()
                history.ssId = create.ssId
                history.password = create.password
                history.networkEncryption = create.networkEncryption
                history.hidden = create.hidden
                history.createType = create.createType.name
                if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER) {
                    presenter.mListItemNavigation.add(ItemNavigation(create.createType, create.fragmentType, EnumAction.Other, R.drawable.baseline_network_wifi_white_48, "Wifi"))
                } else {
                    presenter.mListItemNavigation.add(ItemNavigation(create.createType, create.fragmentType, EnumAction.SHARE, R.drawable.baseline_share_white_48, "Share"))
                }
                onShowUI(llWifi)
                setTitle("Wifi")
            }
            ParsedResultType.GEO -> {
                /*Put item to HashClipboard*/presenter.hashClipboard["lat"] = create.lat.toString() + ""
                presenter.hashClipboard["lon"] = create.lon.toString() + ""
                presenter.hashClipboard["query"] = create.query
                locationLatitude.setText("" + create.lat)
                locationLongitude.setText("" + create.lon)
                locationQuery.setText("" + create.query)
                history = HistoryModel()
                history.lat = create.lat
                history.lon = create.lon
                history.query = create.query
                history.createType = create.createType.name
                if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER) {
                    presenter.mListItemNavigation.add(ItemNavigation(create.createType, create.fragmentType, EnumAction.Other, R.drawable.baseline_location_on_white_48, "Location"))
                } else {
                    presenter.mListItemNavigation.add(ItemNavigation(create.createType, create.fragmentType, EnumAction.SHARE, R.drawable.baseline_share_white_48, "Share"))
                }
                onShowUI(llLocation)
                setTitle("Location")
            }
            ParsedResultType.TEL -> {
                /*Put item to HashClipboard*/presenter.hashClipboard["phone"] = create.phone
                telephoneNumber.setText(create.phone)
                history = HistoryModel()
                history.phone = create.phone
                history.createType = create.createType.name
                if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER) {
                    presenter.mListItemNavigation.add(ItemNavigation(create.createType, create.fragmentType, EnumAction.Other, R.drawable.baseline_phone_white_48, "Telephone"))
                } else {
                    presenter.mListItemNavigation.add(ItemNavigation(create.createType, create.fragmentType, EnumAction.SHARE, R.drawable.baseline_share_white_48, "Share"))
                }
                onShowUI(llTelephone)
                setTitle("Telephone")
            }
            ParsedResultType.SMS -> {
                /*Put item to HashClipboard*/presenter.hashClipboard["phone"] = create.phone
                presenter.hashClipboard["message"] = create.message
                smsTo.setText(create.phone)
                smsMessage.setText(create.message)
                history = HistoryModel()
                history.phone = create.phone
                history.message = create.message
                history.createType = create.createType.name
                if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER) {
                    presenter.mListItemNavigation.add(ItemNavigation(create.createType, create.fragmentType, EnumAction.Other, R.drawable.baseline_textsms_white_48, "SMS"))
                } else {
                    presenter.mListItemNavigation.add(ItemNavigation(create.createType, create.fragmentType, EnumAction.SHARE, R.drawable.baseline_share_white_48, "Share"))
                }
                onShowUI(llSMS)
                setTitle("SMS")
            }
            ParsedResultType.CALENDAR -> {
                /*Put item to HashClipboard*/presenter.hashClipboard["title"] = create.title
                presenter.hashClipboard["location"] = create.location
                presenter.hashClipboard["description"] = create.description
                presenter.hashClipboard["startEventMilliseconds"] = Utils.convertMillisecondsToDateTime(create.startEventMilliseconds)
                presenter.hashClipboard["endEventMilliseconds"] = Utils.convertMillisecondsToDateTime(create.endEventMilliseconds)
                eventTitle.setText(create.title)
                eventLocation.setText(create.location)
                eventDescription.setText(create.description)
                eventBeginTime.setText(Utils.convertMillisecondsToDateTime(create.startEventMilliseconds))
                eventEndTime.setText(Utils.convertMillisecondsToDateTime(create.endEventMilliseconds))
                history = HistoryModel()
                history.title = create.title
                history.location = create.location
                history.description = create.description
                history.startEvent = create.startEvent
                history.endEvent = create.endEvent
                history.startEventMilliseconds = create.startEventMilliseconds
                history.endEventMilliseconds = create.endEventMilliseconds
                history.createType = create.createType.name
                if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER) {
                    presenter.mListItemNavigation.add(ItemNavigation(create.createType, create.fragmentType, EnumAction.Other, R.drawable.baseline_event_white_48, "Calendar"))
                } else {
                    presenter.mListItemNavigation.add(ItemNavigation(create.createType, create.fragmentType, EnumAction.SHARE, R.drawable.baseline_share_white_48, "Share"))
                }
                onShowUI(llEvent)
                setTitle("Calendar")
                Utils.Log(TAG, "start milliseconds : " + create.startEventMilliseconds)
            }
            ParsedResultType.ISBN -> {
                /*Put item to HashClipboard*/presenter.hashClipboard["ISBN"] = create.ISBN
                textISBN.setText(create.ISBN)
                history = HistoryModel()
                history.text = create.ISBN
                history.createType = create.createType.name
                presenter.mListItemNavigation.add(ItemNavigation(create.createType, create.fragmentType, EnumAction.SEARCH, R.drawable.baseline_search_white_48, "Search"))
                if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER) {
                    presenter.mListItemNavigation.add(ItemNavigation(create.createType, create.fragmentType, EnumAction.Other, R.drawable.baseline_textsms_white_48, "Share"))
                } else {
                    presenter.mListItemNavigation.add(ItemNavigation(create.createType, create.fragmentType, EnumAction.SHARE, R.drawable.baseline_share_white_48, "ISBN"))
                }
                onShowUI(llISBN)
                setTitle("ISBN")
            }
            else -> {
                /*Put item to HashClipboard*/presenter.hashClipboard["text"] = create.text
                textMessage.setText(create.text)
                history = HistoryModel()
                history.text = create.text
                history.createType = create.createType.name
                presenter.mListItemNavigation.add(ItemNavigation(create.createType, create.fragmentType, EnumAction.SEARCH, R.drawable.baseline_search_white_48, "Search"))
                if (create.fragmentType == EnumFragmentType.HISTORY || create.fragmentType == EnumFragmentType.SCANNER) {
                    presenter.mListItemNavigation.add(ItemNavigation(create.createType, create.fragmentType, EnumAction.Other, R.drawable.baseline_textsms_white_48, "Text"))
                } else {
                    presenter.mListItemNavigation.add(ItemNavigation(create.createType, create.fragmentType, EnumAction.SHARE, R.drawable.baseline_share_white_48, "Share"))
                }
                onShowUI(llText)
                setTitle("Text")
            }
        }
        presenter.mListItemNavigation.add(ItemNavigation(create.createType, create.fragmentType, EnumAction.CLIPBOARD, R.drawable.baseline_file_copy_white_48, "Clipboard"))
        onReloadData()
        try {
            val autoCopy: Boolean = PrefsController.getBoolean(getString(R.string.key_copy_to_clipboard), false)
            if (autoCopy) {
                Utils.copyToClipboard(presenter.getResult(presenter.hashClipboard))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onSaved(path: String?, enumAction: EnumAction?) {
        when (enumAction) {
            EnumAction.SHARE -> {
                run {
                    Utils.Log(TAG, "path : $path")
                    val file = File(path)
                    if (file.isFile) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            val uri: Uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID.toString() + ".provider", file)
                            shareToSocial(uri)
                        } else {
                            val uri = Uri.fromFile(file)
                            shareToSocial(uri)
                        }
                    } else {
                        Toast.makeText(this@ScannerResultFragment, getString(R.string.no_items_found), Toast.LENGTH_SHORT).show()
                    }
                }
                run {}
            }
            else -> {
            }
        }
    }

    fun shareToSocial(value: Uri?) {
        val intent = Intent()
        intent.setAction(Intent.ACTION_SEND)
        intent.setType("image/*")
        intent.putExtra(Intent.EXTRA_STREAM, value)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(intent, "Share"))
    }

    fun onGenerateCode(code: String?, enumAction: EnumAction?) {
        try {
            val barcodeEncoder = BarcodeEncoder()
            val hints: MutableMap<EncodeHintType?, Any?> = EnumMap<EncodeHintType?, Any?>(EncodeHintType::class.java)
            hints[EncodeHintType.MARGIN] = 2
            val theme: Theme = Theme.Companion.getInstance().getThemeInfo()
            bitmap = if (history.createType === ParsedResultType.PRODUCT.name) {
                barcodeEncoder.encodeBitmap(this, theme.primaryDarkColor, code, BarcodeFormat.valueOf(history.barcodeFormat), 400, 400, hints)
            } else {
                barcodeEncoder.encodeBitmap(this, theme.primaryDarkColor, code, BarcodeFormat.QR_CODE, 400, 400, hints)
            }
            Utils.saveImage(bitmap, enumAction, create.createType.name, code, this)
        } catch (e: Exception) {
            Utils.Log(TAG, e.message)
        }
    }

    fun onShowUI(view: View?) {
        for (index in mList) {
            if (view === index) {
                index.setVisibility(View.VISIBLE)
            } else {
                index.setVisibility(View.GONE)
            }
        }
        if (create != null) {
            if (create.fragmentType != EnumFragmentType.SCANNER) {
                return
            }
        }
        Utils.Log(TAG, "History :" + if (history != null) true else false)
        Utils.Log(TAG, "Create :" + if (create != null) true else false)
        Utils.Log(TAG, "fragmentType :" + create.fragmentType)

        /*Adding new columns*/history.barcodeFormat = create.barcodeFormat
        history.favorite = create.favorite
        val time = Utils.getCurrentDateTimeSort()
        history.createDatetime = time
        history.updatedDateTime = time
        SQLiteHelper.onInsert(history)
        HistorySingleton.Companion.getInstance().reloadData()
        Utils.Log(TAG, "Parse result " + Utils.getCodeContentByHistory(history))
    }

    override fun onStart() {
        super.onStart()
        Utils.Log(TAG, "onStart")
    }

    override fun onStop() {
        super.onStop()
        Utils.Log(TAG, "onStop")
    }

    override fun onPause() {
        super.onPause()
        Utils.Log(TAG, "onPause")
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.Log(TAG, "onDestroy")
        if (presenter.result != null) {
            when (presenter.result.fragmentType) {
                EnumFragmentType.SCANNER -> {
                    ScannerSingleton.Companion.getInstance().setVisible()
                    Utils.Log(TAG, "onDestroy.......")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.doShowAds()
        Utils.Log(TAG, "onResume")
    }

    fun onClipboardDialog() {
        presenter.hashClipboardResult.clear()
        val dialogBuilder = MaterialDialog.Builder(this, Utils.getCurrentTheme())
        dialogBuilder.setTitle(R.string.copy_items)
        dialogBuilder.setPadding(40, 40, 40, 0)
        dialogBuilder.setMargin(60, 0, 60, 0)
        dialogBuilder.setMessage(R.string.choose_which_items_you_want_to_copy)
        val list: MutableList<String?> = ArrayList()
        for ((_, value) in presenter.hashClipboard) {
            list.add(value)
        }
        val cs = list.toTypedArray<CharSequence?>()
        Utils.Log(TAG, "Result " + Gson().toJson(list))
        Utils.Log(TAG, "show size of list " + cs.size)
        dialogBuilder.setMultiChoiceItems(cs, null, object : OnMultiChoiceClickListener {
            override fun onClick(dialogInterface: DialogInterface?, i: Int, b: Boolean) {
                if (b) {
                    presenter.hashClipboardResult[i] = list[i]
                } else {
                    presenter.hashClipboardResult.remove(i)
                }
            }
        })
        dialogBuilder.setPositiveButton(R.string.copy, object : DialogInterface.OnClickListener {
            override fun onClick(dialogInterface: DialogInterface?, i: Int) {
                if (presenter.hashClipboardResult != null && presenter.hashClipboardResult.size > 0) {
                    Utils.copyToClipboard(presenter.getResult(presenter.hashClipboardResult))
                    Toast.makeText(this@ScannerResultFragment, getString(R.string.copied_successful), Toast.LENGTH_SHORT).show()
                }
            }
        })
        dialogBuilder.setNegativeButton(R.string.cancel, object : DialogInterface.OnClickListener {
            override fun onClick(dialogInterface: DialogInterface?, i: Int) {}
        })
        val dialog = dialogBuilder.show()
        dialogBuilder.setOnShowListener(object : OnShowListener {
            override fun onShow(dialogInterface: DialogInterface?) {
                Utils.Log(TAG, "action here")
                val positive = dialog.findViewById<Button?>(R.id.button1)
                val negative = dialog.findViewById<Button?>(R.id.button2)
                val title: TextView = dialog.findViewById<TextView?>(R.id.title)
                val content: TextView = dialog.findViewById<TextView?>(R.id.message)
                if (positive != null && negative != null && title != null && content != null) {
                    title.setTextColor(ContextCompat.getColor(QRScannerApplication.Companion.getInstance(), R.color.black))
                    content.setTextColor(ContextCompat.getColor(QRScannerApplication.Companion.getInstance(), R.color.material_gray_700))
                    positive.textSize = 14f
                    negative.textSize = 14f
                    content.setTextSize(18f)
                }
            }
        })
    }

    fun onOpenWebSites(url: String?) {
        val i = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        i.setPackage("com.android.chrome")
        try {
            startActivity(i)
        } catch (e: ActivityNotFoundException) {
            // Chrome is probably not installed
            // Try with the default browser
            try {
                i.setPackage(null)
                startActivity(i)
            } catch (ex: Exception) {
                Toast.makeText(this, "Can not open the link", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun onSearch(query: String?) {
        try {
            val escapedQuery = URLEncoder.encode(query, "UTF-8")
            val uri = Uri.parse("https://www.google.com/search?q=$escapedQuery")
            val i = Intent(Intent.ACTION_VIEW, uri)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            i.setPackage("com.android.chrome")
            try {
                startActivity(i)
            } catch (e: ActivityNotFoundException) {
                // Chrome is probably not installed
                // Try with the default browser
                try {
                    i.setPackage(null)
                    startActivity(i)
                } catch (ex: Exception) {
                    Toast.makeText(this, "Can not open the link", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /*show ads*/
    override fun doShowAds(isShow: Boolean) {
        if (isShow) {
            if (QRScannerApplication.Companion.getInstance().isRequestLargeAds()) {
                rlAdsRoot.setVisibility(View.GONE)
                viewSeparateLine.setVisibility(View.GONE)
            } else {
                QRScannerApplication.Companion.getInstance().loadLargeAd(llAds)
            }
        } else {
            rlAdsRoot.setVisibility(View.GONE)
            viewSeparateLine.setVisibility(View.GONE)
        }
    }

    companion object {
        private val TAG = ScannerResultFragment::class.java.simpleName
    }
}