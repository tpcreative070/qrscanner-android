package tpcreative.co.qrscanner.ui.scannerresult
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Bundle
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.client.result.ParsedResultType
import de.mrapp.android.dialog.MaterialDialog
import kotlinx.android.synthetic.main.activity_result.*
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide
import tpcreative.co.qrscanner.common.controller.PrefsController
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.helper.SQLiteHelper
import tpcreative.co.qrscanner.model.*
import tpcreative.co.qrscanner.ui.review.ReviewActivity
import java.net.URLEncoder
import java.util.*

class ScannerResultActivity : BaseActivitySlide(), ScannerResultActivityAdapter.ItemSelectedListener {
    lateinit var viewModel : ScannerResultViewModel
    private var create: CreateModel? = null
    var adapter: ScannerResultActivityAdapter? = null
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
            R.id.menu_item_txt_export ->{
                code?.let { Utils.onShareText(this,it) }
                return true
            }
            R.id.menu_item_delete ->{
                delete()
                return true
            }
            R.id.menu_item_view_code ->{
                viewForResult.launch(Navigator.onResultView(this,viewModel.result, ReviewActivity::class.java))
                 return true
            }
            R.id.menu_item_add_to_favorites -> {
                updatedFavorite()
                return true
            }
            R.id.menu_item_notes -> {
                enterTakeNote()
                return true
            }
            R.id.menu_item_copy -> {
                onClipboardDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private val viewForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            Utils.Log(TAG,"${result.resultCode}")
        }
    }

    override fun onClickItem(position: Int,action: EnumAction) {
        val navigation: ItemNavigation = dataSource[position]
        val result = dataResult
        when (navigation.enumAction) {
            EnumAction.CLIPBOARD -> {
                onClipboardDialog()
            }
            EnumAction.PHONE_CALL ->{
                Utils.onPhoneCall(this,result)
            }
            EnumAction.EMAIL ->{
                Utils.onSendMail(this,result)
            }
            EnumAction.SEARCH -> {
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
            EnumAction.DO_ADVANCE ->{
                when(action){
                    EnumAction.VIEW_CODE ->{
                        viewForResult.launch(Navigator.onResultView(this,viewModel.result,ReviewActivity::class.java))
                    }
                    EnumAction.MARK_FAVORITE ->{
                        updatedFavorite()
                    }
                    EnumAction.TAKE_NOTE ->{
                        enterTakeNote()
                    }
                    else -> {}
                }
            }
            else -> {
                /*This is other action*/
                create = dataResult
                onShareIntent()
            }
        }
    }

    fun onReloadData() {
        adapter?.setDataSource(viewModel.mListNavigation)
    }

    private fun onShareIntent() {
            when (dataResult.createType) {
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
                      Utils.onSendMail(this,create)
                    } catch (e: ActivityNotFoundException) {
                        //TODO smth
                    }
                    return
                }
                ParsedResultType.PRODUCT -> {
                    Utils.Log(TAG,"Nothing")
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
                    Utils.onShareMap(this,uri)
                    return
                }
                ParsedResultType.TEL -> Utils.onPhoneCall(this,create)
                ParsedResultType.SMS -> {
                    Utils.onSendSMS(this,create?.phone,create?.message)
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
                    return
                }
            }
    }

    fun setView() {
        val history : HistoryModel?
        create = dataResult
        val mMap = Utils.getCodeDisplay(create)
        code = Utils.getCode(create)
        tvContent.text = mMap?.get(ConstantKey.CONTENT) ?: ""
        tvBarCodeFormat.text = mMap?.get(ConstantKey.BARCODE_FORMAT) ?: ""
        tvCreatedDatetime.text = mMap?.get(ConstantKey.CREATED_DATETIME) ?: ""
        viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.DO_ADVANCE, R.drawable.baseline_location_on_white_48, ConstantValue.ADVANCE,create?.favorite))
        when (create?.createType) {
            ParsedResultType.ADDRESSBOOK -> {
                /*Put item to HashClipboard*/
                viewModel.hashClipboard[ConstantKey.FULL_NAME] = create?.fullName
                viewModel.hashClipboard[ConstantKey.ADDRESS] = create?.address
                viewModel.hashClipboard[ConstantKey.PHONE] = create?.phone
                viewModel.hashClipboard[ConstantKey.EMAIL] = create?.email
                history = HistoryModel()
                history.fullName = create?.fullName
                history.address = create?.address
                history.phone = create?.phone
                history.email = create?.email
                history.createType = create?.createType?.name
                viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.Other, R.drawable.baseline_perm_contact_calendar_white_48, ConstantValue.ADDRESS_BOOK,create?.favorite))
                viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.PHONE_CALL, R.drawable.baseline_phone_white_48, ConstantValue.PHONE_CALL,create?.favorite))
                viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.EMAIL, R.drawable.baseline_email_white_48, ConstantValue.EMAIL,create?.favorite))
                onInsertUpdateHistory(history)
                title = ConstantValue.ADDRESS_BOOK
            }
            ParsedResultType.EMAIL_ADDRESS -> {
                /*Put item to HashClipboard*/
                viewModel.hashClipboard[ConstantKey.EMAIL] = create?.email
                viewModel.hashClipboard[ConstantKey.SUBJECT] = create?.subject
                viewModel.hashClipboard[ConstantKey.MESSAGE] = create?.message
                history = HistoryModel()
                history.email = create?.email
                history.subject = create?.subject
                history.message = create?.message
                history.createType = create?.createType?.name
                viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.Other, R.drawable.baseline_email_white_48, "Email",create?.favorite))
                onInsertUpdateHistory(history)
                title = ConstantValue.EMAIL
            }
            ParsedResultType.PRODUCT -> {
                /*Put item to HashClipboard*/
                viewModel.hashClipboard[ConstantKey.PRODUCT_ID] = create?.productId
                history = HistoryModel()
                history.text = create?.productId
                history.createType = create?.createType?.name
                history.barcodeFormat = create?.barcodeFormat
                viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.SEARCH, R.drawable.baseline_search_white_48, "Search",create?.favorite))
                onInsertUpdateHistory(history)
                title = ConstantValue.PRODUCT
            }
            ParsedResultType.URI -> {
                /*Put item to HashClipboard*/
                viewModel.hashClipboard[ConstantKey.URL] = create?.url
                history = HistoryModel()
                history.url = create?.url
                history.createType = create?.createType?.name
                viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.SEARCH, R.drawable.baseline_search_white_48, "Search",create?.favorite))
                viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.Other, R.drawable.baseline_language_white_48, "Url",create?.favorite))
                onInsertUpdateHistory(history)
                title = ConstantValue.WEBSITE
                val isAutoOpening: Boolean = PrefsController.getBoolean(getString(R.string.key_auto_navigate_to_browser), false)
                if (isAutoOpening) {
                    onOpenWebSites(create?.url)
                }
            }
            ParsedResultType.WIFI -> {
                /*Put item to HashClipboard*/
                viewModel.hashClipboard[ConstantKey.SSID] = create?.ssId
                viewModel.hashClipboard[ConstantKey.PASSWORD] = create?.password
                viewModel.hashClipboard[ConstantKey.NETWORK_ENCRYPTION] = create?.networkEncryption
                viewModel.hashClipboard[ConstantKey.HIDDEN] = if (create?.hidden == true) "Yes" else "No"
                history = HistoryModel()
                history.ssId = create?.ssId
                history.password = create?.password
                history.networkEncryption = create?.networkEncryption
                history.hidden = create?.hidden
                history.createType = create?.createType?.name
                viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.Other, R.drawable.baseline_network_wifi_white_48, "Wifi",create?.favorite))
                onInsertUpdateHistory(history)
                title = ConstantValue.WIFI
            }
            ParsedResultType.GEO -> {
                /*Put item to HashClipboard*/
                viewModel.hashClipboard[ConstantKey.LAT] = create?.lat.toString() + ""
                viewModel.hashClipboard[ConstantKey.LON] = create?.lon.toString() + ""
                viewModel.hashClipboard[ConstantKey.QUERY] = create?.query
                history = HistoryModel()
                history.lat = create?.lat
                history.lon = create?.lon
                history.query = create?.query
                history.createType = create?.createType?.name
                viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.Other, R.drawable.baseline_location_on_white_48, "Location",create?.favorite))
                onInsertUpdateHistory(history)
                title = ConstantValue.LOCATION
            }
            ParsedResultType.TEL -> {
                /*Put item to HashClipboard*/
                viewModel.hashClipboard[ConstantKey.PHONE] = create?.phone
                history = HistoryModel()
                history.phone = create?.phone
                history.createType = create?.createType?.name
                viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.Other, R.drawable.baseline_phone_white_48, ConstantValue.PHONE_CALL,create?.favorite))
                onInsertUpdateHistory(history)
                title = "Telephone"
            }
            ParsedResultType.SMS -> {
                /*Put item to HashClipboard*/
                viewModel.hashClipboard[ConstantKey.PHONE] = create?.phone
                viewModel.hashClipboard[ConstantKey.MESSAGE] = create?.message
                history = HistoryModel()
                history.phone = create?.phone
                history.message = create?.message
                history.createType = create?.createType?.name
                viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.Other, R.drawable.baseline_textsms_white_48, ConstantValue.SMS,create?.favorite))
                onInsertUpdateHistory(history)
                title = ConstantValue.SMS
            }
            ParsedResultType.CALENDAR -> {
                /*Put item to HashClipboard*/
                viewModel.hashClipboard[ConstantKey.TITLE] = create?.title
                viewModel.hashClipboard[ConstantKey.LOCATION] = create?.location
                viewModel.hashClipboard[ConstantKey.DESCRIPTION] = create?.description
                viewModel.hashClipboard[ConstantKey.START_EVENT_MILLISECONDS] = Utils.getCurrentDatetimeEvent(create?.startEventMilliseconds
                        ?: 0)
                viewModel.hashClipboard[ConstantKey.END_EVENT_MILLISECONDS] = Utils.getCurrentDatetimeEvent(create?.endEventMilliseconds
                        ?: 0)
                history = HistoryModel()
                history.title = create?.title
                history.location = create?.location
                history.description = create?.description
                history.startEvent = create?.startEvent
                history.endEvent = create?.endEvent
                history.startEventMilliseconds = create?.startEventMilliseconds
                history.endEventMilliseconds = create?.endEventMilliseconds
                history.createType = create?.createType?.name
                viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.Other, R.drawable.baseline_event_white_48, "Calendar",create?.favorite))
                onInsertUpdateHistory(history)
                title = ConstantValue.CALENDAR
            }
            ParsedResultType.ISBN -> {
                /*Put item to HashClipboard*/
                viewModel.hashClipboard[ConstantKey.ISBN] = create?.ISBN
                history = HistoryModel()
                history.text = create?.ISBN
                history.createType = create?.createType?.name
                viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.SEARCH, R.drawable.baseline_search_white_48, ConstantValue.SEARCH,create?.favorite))
                viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.Other, R.drawable.baseline_textsms_white_48, ConstantValue.SHARE,create?.favorite))
                onInsertUpdateHistory(history)
                title = ConstantValue.ISBN
            }
            else -> {
                //Text query
                if (BarcodeFormat.QR_CODE ==  BarcodeFormat.valueOf(create?.barcodeFormat ?: "")){
                    viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.Other, R.drawable.baseline_textsms_white_48, ConstantValue.TEXT,create?.favorite))
                }
                /*Put item to HashClipboard*/
                viewModel.hashClipboard[ConstantKey.TEXT] = create?.text
                history = HistoryModel()
                history.text = create?.text
                history.createType = create?.createType?.name
                viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.SEARCH, R.drawable.baseline_search_white_48, ConstantValue.SEARCH,create?.favorite))
                onInsertUpdateHistory(history)
                title = ConstantValue.TEXT
            }
        }
        viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.CLIPBOARD, R.drawable.ic_baseline_content_copy_24, ConstantValue.CLIPBOARD,create?.favorite))
        onReloadData()
        onCheckFavorite()
        onCopy()
    }

    private fun onCopy(){
        try {
            val autoCopy: Boolean = PrefsController.getBoolean(getString(R.string.key_copy_to_clipboard), false)
            if (autoCopy) {
                Utils.copyToClipboard(viewModel.getResult(viewModel.hashClipboard))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun onInsertUpdateHistory(history : HistoryModel) {
        if (create != null) {
            if (create?.fragmentType != EnumFragmentType.SCANNER) {
                return
            }
        }
        Utils.Log(TAG, "Create :" + (create != null))
        Utils.Log(TAG, "fragmentType :" + create?.fragmentType)

        /*Adding new columns*/
        history.barcodeFormat = create?.barcodeFormat
        history.favorite = create?.favorite
        val time = Utils.getCurrentDateTimeSort()
        history.createDatetime = time
        history.updatedDateTime = time
        SQLiteHelper.onInsert(history)
        HistorySingleton.getInstance()?.reloadData()
        viewModel.updateId(history.uuId)
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
        dialogBuilder.setMultiChoiceItems(cs, null
        ) { dialogInterface, i, b ->
            if (b) {
                viewModel.hashClipboardResult?.set(i, list[i])
            } else {
                viewModel.hashClipboardResult?.remove(i)
            }
        }
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

    private fun onOpenWebSites(url: String?) {
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
            QRScannerApplication.getInstance().loadResultSmallView(llSmallAds)
            QRScannerApplication.getInstance().loadResultLargeView(llLargeAds)
        } else {
            rlAdsRoot.visibility = View.GONE
        }
    }

    private val dataResult: CreateModel
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