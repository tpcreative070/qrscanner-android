package tpcreative.co.qrscanner.ui.scannerresult
import android.Manifest
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.client.result.ParsedResultType
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import de.mrapp.android.dialog.MaterialDialog
import kotlinx.android.synthetic.main.activity_result.*
import tpcreative.co.qrscanner.BuildConfig
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.HistorySingleton
import tpcreative.co.qrscanner.common.ScannerSingleton
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide
import tpcreative.co.qrscanner.common.controller.PrefsController
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.helper.SQLiteHelper
import tpcreative.co.qrscanner.model.*
import java.io.File
import java.net.URLEncoder
import java.util.*

class ScannerResultActivity : BaseActivitySlide(), ScannerResultActivityAdapter.ItemSelectedListener {
    lateinit var viewModel : ScannerResultViewModel
    private var create: CreateModel? = null
    var mList: MutableList<LinearLayout> = mutableListOf()
    private var history: HistoryModel? = HistoryModel()
    var adapter: ScannerResultActivityAdapter? = null
    var llm: LinearLayoutManager? = null
    private var code: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        initUI()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_result, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                showAds()
                return true
            }
            R.id.menu_item_report -> {
                try {
                    val to = "care@tpcreative.me"
                    val subject = "Request new features||Need help"
                    val body = ""
                    val mailTo = "mailto:" + to +
                            "?&subject=" + Uri.encode(subject) +
                            "&body=" + Uri.encode(body)
                    val emailIntent = Intent(Intent.ACTION_VIEW)
                    emailIntent.data = Uri.parse(mailTo)
                    startActivity(emailIntent)
                } catch (e: ActivityNotFoundException) {
                    //TODO smth
                }
                return true
            }
            R.id.menu_item_txt_export ->{
                code?.let { shareToSocial(it) }
                return true
            }
            R.id.menu_item_delete ->{
                delete()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClickItem(position: Int) {
        val navigation: ItemNavigation = dataSource[position]
        when (navigation.enumAction) {
            EnumAction.CLIPBOARD -> {
                onClipboardDialog()
            }
            EnumAction.SEARCH -> {
                val result = dataResult ?: return
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
                    else -> Utils.Log(TAG, "Nothing")
                }
            }
            else -> {
                onShareIntent()
            }
        }
    }

    private fun onReloadData() {
        adapter?.setDataSource(viewModel.mListNavigation)
    }

    private fun onAddPermissionPhoneCall() {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.CALL_PHONE)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report?.areAllPermissionsGranted() == true) {
                            Utils.Log(TAG, "Action here phone call")
                            val intentPhoneCall = Intent(Intent.ACTION_CALL, Uri.parse("tel:" + create?.phone))
                            startActivity(intentPhoneCall)
                        } else {
                            Utils.Log(TAG, "Permission is denied")
                        }
                        // check for permanent denial of any permission
                        if (report?.isAnyPermissionPermanentlyDenied == true) {
                            /*Miss add permission in manifest*/
                            Utils.Log(TAG, "request permission is failed")
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest?>?, token: PermissionToken?) {
                        /* ... */
                        token?.continuePermissionRequest()
                    }
                })
                .withErrorListener { Utils.Log(TAG, "error ask permission") }.onSameThread().check()
    }

    private fun onShareIntent() {
        create = dataResult
            when (create?.createType) {
                ParsedResultType.ADDRESSBOOK -> {
                    val intentContact = Intent()
                    intentContact.action = ContactsContract.Intents.SHOW_OR_CREATE_CONTACT
                    intentContact.data = Uri.fromParts("tel", create?.phone, null)
                    intentContact.putExtra(ContactsContract.Intents.Insert.NAME, create?.fullName)
                    intentContact.putExtra(ContactsContract.Intents.Insert.POSTAL, create?.address)
                    intentContact.putExtra(ContactsContract.Intents.Insert.PHONE, create?.phone)
                    intentContact.putExtra(ContactsContract.Intents.Insert.EMAIL, create?.email)
                    startActivity(intentContact)
                    return
                }
                ParsedResultType.EMAIL_ADDRESS -> {
                    try {
                        val to = create?.email
                        val subject = create?.subject
                        val body = create?.message
                        val mailTo = "mailto:" + to +
                                "?&subject=" + Uri.encode(subject) +
                                "&body=" + Uri.encode(body)
                        val emailIntent = Intent(Intent.ACTION_VIEW)
                        emailIntent.data = Uri.parse(mailTo)
                        startActivity(emailIntent)
                        Utils.Log(TAG, "email object ${Gson().toJson(create)}")
                    } catch (e: ActivityNotFoundException) {
                        //TODO smth
                    }
                    return
                }
                ParsedResultType.PRODUCT -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:"))
                    intent.putExtra("sms_body", create?.productId)
                    startActivity(intent)
                    return
                }
                ParsedResultType.URI -> {
                    onOpenWebSites(create?.url)
                    return
                }
                ParsedResultType.WIFI -> {
                    startActivity(Intent(WifiManager.ACTION_PICK_WIFI_NETWORK))
                    return
                }
                ParsedResultType.GEO -> {
                    val uri = "geo:" + create?.lat + "," + create?.lon + ""
                    val intentMap = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                    intentMap.setClassName(
                        "com.google.android.apps.maps",
                        "com.google.android.maps.MapsActivity"
                    )
                    startActivity(intentMap)
                    return
                }
                ParsedResultType.TEL -> onAddPermissionPhoneCall()
                ParsedResultType.SMS -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + create?.phone))
                    intent.putExtra("sms_body", create?.message)
                    startActivity(intent)
                    return
                }
                ParsedResultType.CALENDAR -> {
                    val intentCalendar = Intent(Intent.ACTION_INSERT)
                    intentCalendar.data = CalendarContract.Events.CONTENT_URI
                    intentCalendar.putExtra(CalendarContract.Events.TITLE, create?.title)
                    intentCalendar.putExtra(
                        CalendarContract.Events.DESCRIPTION,
                        create?.description
                    )
                    intentCalendar.putExtra(
                        CalendarContract.Events.EVENT_LOCATION,
                        create?.location
                    )
                    intentCalendar.putExtra(CalendarContract.Events.ALL_DAY, false)
                    intentCalendar.putExtra(
                        CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                        create?.startEventMilliseconds
                    )
                    intentCalendar.putExtra(
                        CalendarContract.EXTRA_EVENT_END_TIME, create?.endEventMilliseconds
                    )
                    startActivity(intentCalendar)
                    return
                }
                ParsedResultType.ISBN -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:"))
                    intent.putExtra("sms_body", create?.ISBN)
                    startActivity(intent)
                    return
                }
                else -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:"))
                    intent.putExtra("sms_body", create?.text)
                    startActivity(intent)
                }
            }
    }

    fun setView() {
        create = dataResult
        when (create?.createType) {
            ParsedResultType.ADDRESSBOOK -> {
                /*Put item to HashClipboard*/
                viewModel.hashClipboard["fullName"] = create?.fullName
                viewModel.hashClipboard["address"] = create?.address
                viewModel.hashClipboard["phone"] = create?.phone
                viewModel.hashClipboard["email"] = create?.email
                contactFullName.text = create?.fullName
                contactAddress.text = create?.address
                contactPhone.text = create?.phone
                contactEmail.text = create?.email
                history = HistoryModel()
                history?.fullName = create?.fullName
                history?.address = create?.address
                history?.phone = create?.phone
                history?.email = create?.email
                history?.createType = create?.createType?.name
                code = "MECARD:N:" + create?.fullName + ";TEL:" + create?.phone + ";EMAIL:" + create?.email + ";ADR:" + create?.address + ";"
                viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.Other, R.drawable.baseline_perm_contact_calendar_white_48, "AddressBook"))
                onShowUI(llContact)
                title = "AddressBook"
            }
            ParsedResultType.EMAIL_ADDRESS -> {
                /*Put item to HashClipboard*/
                viewModel.hashClipboard["email"] = create?.email
                viewModel.hashClipboard["subject"] = create?.subject
                viewModel.hashClipboard["message"] = create?.message
                emailTo.text = create?.email
                emailSubject.text = create?.subject
                emailMessage.text = create?.message
                history = HistoryModel()
                history?.email = create?.email
                history?.subject = create?.subject
                history?.message = create?.message
                history?.createType = create?.createType?.name
                code = "MATMSG:TO:" + create?.email + ";SUB:" + create?.subject + ";BODY:" + create?.message + ";"
                viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.Other, R.drawable.baseline_email_white_48, "Email"))
                onShowUI(llEmail)
                title = "Email"
            }
            ParsedResultType.PRODUCT -> {
                /*Put item to HashClipboard*/
                viewModel.hashClipboard["productId"] = create?.productId
                textProduct.text = create?.productId
                history = HistoryModel()
                history?.text = create?.productId
                history?.createType = create?.createType?.name
                history?.barcodeFormat = create?.barcodeFormat
                code = create?.productId
                viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.SEARCH, R.drawable.baseline_search_white_48, "Search"))
                viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.Other, R.drawable.baseline_textsms_white_48, "Product"))
                onShowUI(llProduct)
                title = "Product"
            }
            ParsedResultType.URI -> {
                /*Put item to HashClipboard*/
                viewModel.hashClipboard["url"] = create?.url
                urlAddress.text = create?.url
                history = HistoryModel()
                history?.url = create?.url
                history?.createType = create?.createType?.name
                code = create?.url
                viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.SEARCH, R.drawable.baseline_search_white_48, "Search"))
                viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.Other, R.drawable.baseline_language_white_48, "Url"))
                onShowUI(llURL)
                title = "Url"
                val isAutoOpening: Boolean = PrefsController.getBoolean(getString(R.string.key_auto_navigate_to_browser), false)
                if (isAutoOpening) {
                    onOpenWebSites(create?.url)
                }
            }
            ParsedResultType.WIFI -> {
                /*Put item to HashClipboard*/
                viewModel.hashClipboard["ssId"] = create?.ssId
                viewModel.hashClipboard["password"] = create?.password
                viewModel.hashClipboard["networkEncryption"] = create?.networkEncryption
                viewModel.hashClipboard["hidden"] = if (create?.hidden == true) "Yes" else "No"
                wifiSSID.text = create?.ssId
                wifiPassword.text = create?.password
                wifiNetworkEncryption.text = create?.networkEncryption
                wifiHidden.text = if (create?.hidden == true) "Yes" else "No"
                history = HistoryModel()
                history?.ssId = create?.ssId
                history?.password = create?.password
                history?.networkEncryption = create?.networkEncryption
                history?.hidden = create?.hidden
                history?.createType = create?.createType?.name
                code = "WIFI:S:" + create?.ssId + ";T:" + create?.password + ";P:" + create?.networkEncryption + ";H:" + create?.hidden + ";"
                viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.Other, R.drawable.baseline_network_wifi_white_48, "Wifi"))
                onShowUI(llWifi)
                title = "Wifi"
            }
            ParsedResultType.GEO -> {
                /*Put item to HashClipboard*/
                viewModel.hashClipboard["lat"] = create?.lat.toString() + ""
                viewModel.hashClipboard["lon"] = create?.lon.toString() + ""
                viewModel.hashClipboard["query"] = create?.query
                locationLatitude.text = "${create?.lat}"
                locationLongitude.text = "${create?.lon}"
                locationQuery.text = "${create?.query}"
                history = HistoryModel()
                history?.lat = create?.lat
                history?.lon = create?.lon
                history?.query = create?.query
                history?.createType = create?.createType?.name
                code = "geo:" + create?.lat + "," + create?.lon + "?q=" + create?.query + ""
                viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.Other, R.drawable.baseline_location_on_white_48, "Location"))
                onShowUI(llLocation)
                title = "Location"
            }
            ParsedResultType.TEL -> {
                /*Put item to HashClipboard*/
                viewModel.hashClipboard["phone"] = create?.phone
                telephoneNumber.text = create?.phone
                history = HistoryModel()
                history?.phone = create?.phone
                history?.createType = create?.createType?.name
                code = "tel:" + create?.phone + ""
                viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.Other, R.drawable.baseline_phone_white_48, "Telephone"))
                onShowUI(llTelephone)
                title = "Telephone"
            }
            ParsedResultType.SMS -> {
                /*Put item to HashClipboard*/
                viewModel.hashClipboard["phone"] = create?.phone
                viewModel.hashClipboard["message"] = create?.message
                smsTo.text = create?.phone
                smsMessage.text = create?.message
                history = HistoryModel()
                history?.phone = create?.phone
                history?.message = create?.message
                history?.createType = create?.createType?.name
                code = "smsto:" + create?.phone + ":" + create?.message
                viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.Other, R.drawable.baseline_textsms_white_48, "SMS"))
                onShowUI(llSMS)
                title = "SMS"
            }
            ParsedResultType.CALENDAR -> {
                /*Put item to HashClipboard*/
                viewModel.hashClipboard["title"] = create?.title
                viewModel.hashClipboard["location"] = create?.location
                viewModel.hashClipboard["description"] = create?.description
                viewModel.hashClipboard["startEventMilliseconds"] = Utils.convertMillisecondsToDateTime(create?.startEventMilliseconds
                        ?: 0)
                viewModel.hashClipboard["endEventMilliseconds"] = Utils.convertMillisecondsToDateTime(create?.endEventMilliseconds
                        ?: 0)
                eventTitle.text = create?.title
                eventLocation.text = create?.location
                eventDescription.text = create?.description
                eventBeginTime.text = Utils.convertMillisecondsToDateTime(create?.startEventMilliseconds
                        ?: 0)
                eventEndTime.text = Utils.convertMillisecondsToDateTime(create?.endEventMilliseconds
                        ?: 0)
                history = HistoryModel()
                history?.title = create?.title
                history?.location = create?.location
                history?.description = create?.description
                history?.startEvent = create?.startEvent
                history?.endEvent = create?.endEvent
                history?.startEventMilliseconds = create?.startEventMilliseconds
                history?.endEventMilliseconds = create?.endEventMilliseconds
                history?.createType = create?.createType?.name

                val builder = StringBuilder()
                builder.append("BEGIN:VEVENT")
                builder.append("\n")
                builder.append("SUMMARY:" + create?.title)
                builder.append("\n")
                builder.append("DTSTART:" + create?.startEvent)
                builder.append("\n")
                builder.append("DTEND:" + create?.endEvent)
                builder.append("\n")
                builder.append("LOCATION:" + create?.location)
                builder.append("\n")
                builder.append("DESCRIPTION:" + create?.description)
                builder.append("\n")
                builder.append("END:VEVENT")
                code = builder.toString()
                viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.Other, R.drawable.baseline_event_white_48, "Calendar"))
                onShowUI(llEvent)
                title = "Calendar"
            }
            ParsedResultType.ISBN -> {
                /*Put item to HashClipboard*/
                viewModel.hashClipboard["ISBN"] = create?.ISBN
                textISBN.text = create?.ISBN
                history = HistoryModel()
                history?.text = create?.ISBN
                history?.createType = create?.createType?.name
                code = create?.ISBN
                viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.SEARCH, R.drawable.baseline_search_white_48, "Search"))
                viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.Other, R.drawable.baseline_textsms_white_48, "Share"))
                onShowUI(llISBN)
                title = "ISBN"
            }
            else -> {
                /*Put item to HashClipboard*/
                viewModel.hashClipboard["text"] = create?.text
                textMessage.text = create?.text
                history = HistoryModel()
                history?.text = create?.text
                history?.createType = create?.createType?.name
                code = create?.text
                viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.SEARCH, R.drawable.baseline_search_white_48, "Search"))
                viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.Other, R.drawable.baseline_textsms_white_48, "Text"))
                onShowUI(llText)
                title = "Text"
            }
        }
        if (viewModel.isBarCode(create?.barcodeFormat)){
            tvFormatType.text = create?.barcodeFormat
            llFormatType.visibility = View.VISIBLE
        }
        viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.CLIPBOARD, R.drawable.baseline_file_copy_white_48, "Clipboard"))
        Utils.Log(TAG, "Format type ${create?.barcodeFormat}")
        onReloadData()
        checkFavorite()
        try {
            val autoCopy: Boolean = PrefsController.getBoolean(getString(R.string.key_copy_to_clipboard), false)
            if (autoCopy) {
                Utils.copyToClipboard(viewModel.getResult(viewModel.hashClipboard))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun onShowUI(view: View?) {
        for (index in mList) {
            if (view === index) {
                index.visibility = View.VISIBLE
            } else {
                index.visibility = View.GONE
            }
        }
        if (create != null) {
            if (create?.fragmentType != EnumFragmentType.SCANNER) {
                return
            }
        }
        Utils.Log(TAG, "History :" + (history != null))
        Utils.Log(TAG, "Create :" + (create != null))
        Utils.Log(TAG, "fragmentType :" + create?.fragmentType)

        /*Adding new columns*/
        history?.barcodeFormat = create?.barcodeFormat
        history?.favorite = create?.favorite
        val time = Utils.getCurrentDateTimeSort()
        history?.createDatetime = time
        history?.updatedDateTime = time
        SQLiteHelper.onInsert(history)
        HistorySingleton.getInstance()?.reloadData()
        rlMarkFavoriteAndTakeNote.visibility = View.GONE
        Utils.Log(TAG, "Parse result " + Utils.getCodeContentByHistory(history))
        Utils.Log(TAG, "Format type ${create?.barcodeFormat}")
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
        when (dataResult.fragmentType) {
            EnumFragmentType.SCANNER -> {
                ScannerSingleton.getInstance()?.setVisible()
                Utils.Log(TAG, "onDestroy.......")
            }
            else -> Utils.Log(TAG, "Nothing")
        }
    }

    override fun onResume() {
        super.onResume()
        checkingShowAds()
        Utils.Log(TAG, "onResume")
    }

    private fun onClipboardDialog() {
        viewModel.hashClipboardResult?.clear()
        val dialogBuilder = MaterialDialog.Builder(this, Utils.getCurrentTheme())
        dialogBuilder.setTitle(R.string.copy_items)
        dialogBuilder.setPadding(40, 40, 40, 0)
        dialogBuilder.setMargin(60, 0, 60, 0)
        dialogBuilder.setMessage(R.string.choose_which_items_you_want_to_copy)
        val list: MutableList<String?> = ArrayList()
        for ((_, value) in viewModel.hashClipboard) {
            list.add(value)
        }
        val cs = list.toTypedArray<CharSequence?>()
        Utils.Log(TAG, "Result " + Gson().toJson(list))
        Utils.Log(TAG, "show size of list " + cs.size)
        dialogBuilder.setMultiChoiceItems(cs, null, object : DialogInterface.OnMultiChoiceClickListener {
            override fun onClick(dialogInterface: DialogInterface?, i: Int, b: Boolean) {
                if (b) {
                    viewModel.hashClipboardResult?.set(i, list[i])
                } else {
                    viewModel.hashClipboardResult?.remove(i)
                }
            }
        })
        dialogBuilder.setPositiveButton(R.string.copy) { dialogInterface, i ->
            if (viewModel.hashClipboardResult != null && (viewModel.hashClipboardResult?.size ?: 0) > 0) {
                Utils.copyToClipboard(viewModel.getResult(viewModel.hashClipboardResult))
                Utils.onAlertNotify(this@ScannerResultActivity, getString(R.string.copied_successful))
            }
        }
        dialogBuilder.setNegativeButton(R.string.cancel, object : DialogInterface.OnClickListener {
            override fun onClick(dialogInterface: DialogInterface?, i: Int) {}
        })
        val dialog = dialogBuilder.show()
        dialogBuilder.setOnShowListener {
            Utils.Log(TAG, "action here")
            val positive = dialog.findViewById<Button?>(android.R.id.button1)
            val negative = dialog.findViewById<Button?>(android.R.id.button2)
            val title: TextView? = dialog.findViewById<TextView?>(android.R.id.title)
            val content: TextView? = dialog.findViewById<TextView?>(android.R.id.message)
            if (positive != null && negative != null && title != null && content != null) {
                title.setTextColor(ContextCompat.getColor(QRScannerApplication.getInstance(), R.color.black))
                content.setTextColor(ContextCompat.getColor(QRScannerApplication.getInstance(), R.color.material_gray_700))
                positive.textSize = 14f
                negative.textSize = 14f
                content.textSize = 18f
            }
        }
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
                Utils.onAlertNotify(this, "Can not open the link")
            }
        }
    }

    private fun onSearch(query: String?) {
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
                    Utils.onAlertNotify(this, "Can not open the link")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /*show ads*/
    fun doShowAds(isShow: Boolean) {
        if (isShow) {
            QRScannerApplication.getInstance().loadLargeAd(llAds)
        } else {
            rlAdsRoot.visibility = View.GONE
        }
    }

    val dataResult: CreateModel
        get() {
            return viewModel.result ?: CreateModel()
        }

    private val dataSource : MutableList<ItemNavigation>
        get() {
            return adapter?.getDataSource() ?: mutableListOf()
        }

    companion object {
        private val TAG = ScannerResultActivity::class.java.simpleName
    }
}