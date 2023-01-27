package tpcreative.co.qrscanner.ui.scannerresult
import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.checkbox.checkBoxPrompt
import com.afollestad.materialdialogs.list.listItemsMultiChoice
import com.google.zxing.BarcodeFormat
import com.google.zxing.client.result.ParsedResultType
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_result.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide
import tpcreative.co.qrscanner.common.controller.PrefsController
import tpcreative.co.qrscanner.common.extension.*
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.common.view.crop.Log
import tpcreative.co.qrscanner.helper.SQLiteHelper
import tpcreative.co.qrscanner.model.*
import tpcreative.co.qrscanner.ui.review.ReviewActivity
import java.util.*


class ScannerResultActivity : BaseActivitySlide(), ScannerResultActivityAdapter.ItemSelectedListener {
    lateinit var viewModel : ScannerResultViewModel
    private var create: GeneralModel? = null
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
                onCopyDialog()
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

    override fun onClickItem(position: Int, contactKey: String,
                             contactValue: String,action: EnumAction) {
        val navigation: ItemNavigation = dataSource[position]
        val result = dataResult
        when (navigation.enumAction) {
            EnumAction.CLIPBOARD -> {
                onCopyDialog()
            }
            EnumAction.PHONE_CALL ->{
                Utils.onPhoneCall(this,result)
            }
            EnumAction.EMAIL_ADDRESS_BOOK ->{
                result.email = contactValue
                Utils.onSendMail(this,result)
            }
            EnumAction.PHONE_ADDRESS_BOOK ->{
                result.phone = contactValue
                Utils.onPhoneCall(this,result)
            }
            EnumAction.URL_ADDRESS_BOOK ->{
                Utils.onOpenWebSites(contactValue,this)
            }
            EnumAction.SEARCH_WEB ->{
                Utils.onSearch(contactValue,this)
            }
            EnumAction.SEARCH_AMAZON ->{
                Utils.onSearchMarketPlace("https://www.amazon.com/s?k=$contactValue",this)
            }
            EnumAction.SEARCH_EBAY ->{
                Utils.onSearchMarketPlace("https://www.ebay.com/sch/i.html?_nkw=$contactValue",this)
            }
            EnumAction.GEO_ADDRESS_BOOK ->{
                val url = "https://www.google.com/maps/search/?api=1&query=$contactValue"
                Utils.onShareMap(this,url)
            }
            EnumAction.EMAIL ->{
                Utils.onSendMail(this,result)
            }
            EnumAction.SEARCH -> {
                when (result.createType) {
                    ParsedResultType.URI -> {
                        Utils.onSearch(result.url,this)
                    }
                    ParsedResultType.PRODUCT -> {
                        Utils.onSearch(result.textProductIdISNB,this)
                    }
                    ParsedResultType.ISBN -> {
                        Utils.onSearch(result.textProductIdISNB,this)
                    }
                    ParsedResultType.TEXT -> {
                        Utils.onSearch(result.textProductIdISNB,this)
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
                    create?.let {
                        val intentContact = Intent()
                        intentContact.action = ContactsContract.Intents.SHOW_OR_CREATE_CONTACT
                        intentContact.data = Uri.fromParts("tel", create?.contact?.phones?.values?.firstOrNull() ?: "111", null)
                        intentContact.putExtra(ContactsContract.Intents.Insert.NAME, create?.contact?.fullName)
                        intentContact.putExtra(ContactsContract.Intents.Insert.JOB_TITLE, create?.contact?.jobTitle)
                        intentContact.putExtra(ContactsContract.Intents.Insert.COMPANY, create?.contact?.company)
                        intentContact.putExtra(ContactsContract.Intents.Insert.NOTES, create?.contact?.note)
                        intentContact.putExtra(ContactsContract.Intents.Insert.POSTAL, create?.contact?.addresses?.values?.firstOrNull()?.postalCode)
                        intentContact.putExtra(ContactsContract.Intents.Insert.PHONE, create?.contact?.phones?.values?.firstOrNull().orEmpty())
                            .putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_WORK)
                        intentContact.putExtra(ContactsContract.Intents.Insert.EMAIL, create?.contact?.emails?.values?.firstOrNull().orEmpty())
                            .putExtra(ContactsContract.Intents.Insert.EMAIL_TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                        startActivity(intentContact)
                    }
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
                    Utils.onOpenWebSites(create?.url,this)
                    return
                }
                ParsedResultType.WIFI -> {
                    if (!Utils.checkingWifiEnable(this)){
                        Utils.alert(this,getString(R.string.alert),getString(R.string.please_enable_wifi)){
                            startActivity( Intent(Settings.ACTION_WIFI_SETTINGS));
                        }
                        return
                    }
                    if (Utils.getDoNoAskAgain()){
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                            Utils.connectWifi(this@ScannerResultActivity ,create?.ssId ?:"",create?.password ?:"")
                            Utils.Log(TAG,"connection on new apis")
                        }else{
                            Utils.connectWifiOnOldVersion(this,create?.ssId?:"",create?.password?:"")
                            Utils.Log(TAG,"connection on old apis")
                        }
                    }else{
                        MaterialDialog(this@ScannerResultActivity).show {
                            message(text = getString(R.string.please_confirm_the_system_notification))
                            Utils.setDoNoAskAgain(true)
                            checkBoxPrompt (R.string.do_not_show_this_dialog_again, isCheckedDefault = true) { checked ->
                                Utils.setDoNoAskAgain(checked)
                            }
                            positiveButton(R.string.ok) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                                    Utils.connectWifi(this@ScannerResultActivity ,create?.ssId ?:"",create?.password ?:"")
                                    Utils.Log(TAG,"connection on new apis")
                                }else{
                                    Utils.connectWifiOnOldVersion(this@ScannerResultActivity,create?.ssId?:"",create?.password?:"")
                                    Utils.Log(TAG,"connection on old apis")
                                }
                                Utils.Log(TAG,"connection")
                            }
                        }
                    }
                    return
                }
                ParsedResultType.GEO -> {
                    val uri = "https://maps.google.com/?q=${create?.lat},${create?.lon}"
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
                    intent.putExtra("sms_body", create?.textProductIdISNB)
                    startActivity(intent)
                    return
                }
                else -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:"))
                    intent.putExtra("sms_body", create?.textProductIdISNB)
                    startActivity(intent)
                    return
                }
            }
    }

    fun setView() {
        create = dataResult
        create?.let {
            val mMap = Utils.onGeneralParse(it,HashMap::class)
            code = it.code
            tvContent.text = "${mMap[ConstantKey.CONTENT]}"
            tvBarCodeFormat.text =  "${mMap[ConstantKey.BARCODE_FORMAT]}"
            tvCreatedDatetime.text = "${mMap[ConstantKey.CREATED_DATETIME]}"
            val history = Utils.onGeneralParse(it,HistoryModel::class)
            history.code = code
            history.hashClipboard?.let {
                viewModel.hashCopy?.clear()
                viewModel.hashCopy = history.hashClipboard
            }
            history.navigationList?.let {
                viewModel.mListNavigation.clear()
                viewModel.mListNavigation.addAll(it)
            }
            if (history.isRequestOpenBrowser) {
                Utils.onOpenWebSites(create?.url,this)
            }
            onInsertUpdateHistory(history)
            title = history.titleDisplay
            onReloadData()
            onCheckFavorite()
            onCopy()
            onHandleBarCode()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun onHandleBarCode(){
       if(viewModel.result?.barcodeFormat?.isNotEmpty() == true){
           val mData = BarcodeFormat.valueOf(viewModel.result?.barcodeFormat ?: BarcodeFormat.QR_CODE.name)
           if (mData != BarcodeFormat.QR_CODE && mData != BarcodeFormat.DATA_MATRIX   && mData != BarcodeFormat.AZTEC){
               GlobalScope.launch(Dispatchers.IO) {
                   onGenerateReview(viewModel.result?.code ?:"",mData)
               }
           }
       }
    }

    private fun onCopy(){
        try {
            val autoCopy: Boolean = PrefsController.getBoolean(getString(R.string.key_copy_to_clipboard), false)
            if (autoCopy) {
                Utils.copyToClipboard(viewModel.getResult(viewModel.hashCopy))
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

    private fun onCopyDialog() {
        viewModel.hashCopyResult?.clear()
        MaterialDialog(this).show {
            listItemsMultiChoice(items = viewModel.hashCopy?.values?.toMutableList()?.map { it.orEmpty() }) { _, index, text ->
                Utils.Log(TAG,"Selected $text")
                text.forEachIndexed { index, i ->
                    viewModel.hashCopyResult?.put(index,i.toString())
                }
                Utils.copyToClipboard(viewModel.getResult(viewModel.hashCopyResult))
            }.positiveButton(R.string.done){
                Utils.Log(TAG,"Selection is checked")
            }
            negativeButton (R.string.cancel){
            }
            title(R.string.choose_which_items_you_want_to_copy)
        }
    }

    /*show ads*/
    fun doShowAds(isShow: Boolean) {
        if (isShow) {
            QRScannerApplication.getInstance().loadResultSmallView(llSmallAds)
            QRScannerApplication.getInstance().loadResultLargeView(llLargeAds)
        }
    }

    private val dataResult: GeneralModel
        get() {
            return viewModel.result ?: GeneralModel()
        }

    private val dataSource : MutableList<ItemNavigation>
        get() {
            return adapter?.getDataSource() ?: mutableListOf()
        }

    companion object {
        private val TAG = ScannerResultActivity::class.java.simpleName
    }
}