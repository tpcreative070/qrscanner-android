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
import kotlinx.android.synthetic.main.fragment_review.*
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
import tpcreative.co.qrscanner.viewmodel.ScannerResultViewModel
import java.io.File
import java.net.URLEncoder
import java.util.*

class ScannerResultFragment : BaseActivitySlide(), Utils.UtilsListener, ScannerResultAdapter.ItemSelectedListener {
    lateinit var viewModel : ScannerResultViewModel
    private var create: Create? = null
    var mList: MutableList<LinearLayout> = mutableListOf()
    private var history: HistoryModel? = HistoryModel()
    var adapter: ScannerResultAdapter? = null
    var llm: LinearLayoutManager? = null
    private var code: String? = null
    private var bitmap: Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_review)
        initUI()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_result, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
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
                    else -> Utils.Log(TAG,"Nothing")
                }
            }
            else -> {
                onAddPermissionSave()
            }
        }
    }

    private fun onReloadData() {
        adapter?.setDataSource(viewModel.mListNavigation)
    }

    fun onAddPermissionPhoneCall() {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.CALL_PHONE)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report?.areAllPermissionsGranted() == true) {
                            Utils.Log(TAG, "Action here phone call")
                            if (create?.fragmentType == EnumFragmentType.HISTORY || create?.fragmentType == EnumFragmentType.SCANNER) {
                                val intentPhoneCall = Intent(Intent.ACTION_CALL, Uri.parse("tel:" + create?.phone))
                                startActivity(intentPhoneCall)
                            } else {
                                code = "tel:" + create?.phone + ""
                                onGenerateCode(code, EnumAction.SHARE)
                            }
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

    private fun onAddPermissionSave() {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report?.areAllPermissionsGranted() == true) {
                            create = dataResult
                            try {
                                when (create?.createType) {
                                    ParsedResultType.ADDRESSBOOK -> if (create?.fragmentType == EnumFragmentType.HISTORY || create?.fragmentType == EnumFragmentType.SCANNER) {
                                        val intentContact = Intent()
                                        intentContact.action = ContactsContract.Intents.SHOW_OR_CREATE_CONTACT
                                        intentContact.data = Uri.fromParts("tel", create?.phone, null)
                                        intentContact.putExtra(ContactsContract.Intents.Insert.NAME, create?.fullName)
                                        intentContact.putExtra(ContactsContract.Intents.Insert.POSTAL, create?.address)
                                        intentContact.putExtra(ContactsContract.Intents.Insert.PHONE, create?.phone)
                                        intentContact.putExtra(ContactsContract.Intents.Insert.EMAIL, create?.email)
                                        startActivity(intentContact)
                                    } else {
                                        code = "MECARD:N:" + create?.fullName + ";TEL:" + create?.phone + ";EMAIL:" + create?.email + ";ADR:" + create?.address + ";"
                                        onGenerateCode(code, EnumAction.SHARE)
                                    }
                                    ParsedResultType.EMAIL_ADDRESS -> if (create?.fragmentType == EnumFragmentType.HISTORY || create?.fragmentType == EnumFragmentType.SCANNER) {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + create?.email))
                                            intent.putExtra(Intent.EXTRA_SUBJECT, create?.subject)
                                            intent.putExtra(Intent.EXTRA_TEXT, create?.message)
                                            startActivity(intent)
                                        } catch (e: ActivityNotFoundException) {
                                            //TODO smth
                                        }
                                    } else {
                                        code = "MATMSG:TO:" + create?.email + ";SUB:" + create?.subject + ";BODY:" + create?.message + ";"
                                        onGenerateCode(code, EnumAction.SHARE)
                                    }
                                    ParsedResultType.PRODUCT -> if (create?.fragmentType == EnumFragmentType.HISTORY || create?.fragmentType == EnumFragmentType.SCANNER) {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:"))
                                        intent.putExtra("sms_body", create?.productId)
                                        startActivity(intent)
                                    } else {
                                        code = create?.productId
                                        onGenerateCode(code, EnumAction.SHARE)
                                    }
                                    ParsedResultType.URI -> if (create?.fragmentType == EnumFragmentType.HISTORY || create?.fragmentType == EnumFragmentType.SCANNER) {
                                        onOpenWebSites(create?.url)
                                    } else {
                                        code = create?.url
                                        onGenerateCode(code, EnumAction.SHARE)
                                    }
                                    ParsedResultType.WIFI -> if (create?.fragmentType == EnumFragmentType.HISTORY || create?.fragmentType == EnumFragmentType.SCANNER) {
                                        startActivity(Intent(WifiManager.ACTION_PICK_WIFI_NETWORK))
                                    } else {
                                        code = "WIFI:S:" + create?.ssId + ";T:" + create?.password + ";P:" + create?.networkEncryption + ";H:" + create?.hidden + ";"
                                        onGenerateCode(code, EnumAction.SHARE)
                                    }
                                    ParsedResultType.GEO -> if (create?.fragmentType == EnumFragmentType.HISTORY || create?.fragmentType == EnumFragmentType.SCANNER) {
                                        val uri = "geo:" + create?.lat + "," + create?.lon + ""
                                        val intentMap = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                                        intentMap.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity")
                                        startActivity(intentMap)
                                    } else {
                                        code = "geo:" + create?.lat + "," + create?.lon + "?q=" + create?.query + ""
                                        onGenerateCode(code, EnumAction.SHARE)
                                    }
                                    ParsedResultType.TEL -> onAddPermissionPhoneCall()
                                    ParsedResultType.SMS -> if (create?.fragmentType == EnumFragmentType.HISTORY || create?.fragmentType == EnumFragmentType.SCANNER) {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + create?.phone))
                                        intent.putExtra("sms_body", create?.message)
                                        startActivity(intent)
                                    } else {
                                        code = "smsto:" + create?.phone + ":" + create?.message
                                        onGenerateCode(code, EnumAction.SHARE)
                                    }
                                    ParsedResultType.CALENDAR -> if (create?.fragmentType == EnumFragmentType.HISTORY || create?.fragmentType == EnumFragmentType.SCANNER) {
                                        val intentCalendar = Intent(Intent.ACTION_INSERT)
                                        intentCalendar.data = CalendarContract.Events.CONTENT_URI
                                        intentCalendar.putExtra(CalendarContract.Events.TITLE, create?.title)
                                        intentCalendar.putExtra(CalendarContract.Events.DESCRIPTION, create?.description)
                                        intentCalendar.putExtra(CalendarContract.Events.EVENT_LOCATION, create?.location)
                                        intentCalendar.putExtra(CalendarContract.Events.ALL_DAY, false)
                                        intentCalendar.putExtra(
                                                CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                                                create?.startEventMilliseconds)
                                        intentCalendar.putExtra(
                                                CalendarContract.EXTRA_EVENT_END_TIME, create?.endEventMilliseconds)
                                        startActivity(intentCalendar)
                                    } else {
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
                                        onGenerateCode(code, EnumAction.SHARE)
                                    }
                                    ParsedResultType.ISBN -> if (create?.fragmentType == EnumFragmentType.HISTORY || create?.fragmentType == EnumFragmentType.SCANNER) {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:"))
                                        intent.putExtra("sms_body", create?.ISBN)
                                        startActivity(intent)
                                    } else {
                                        code = create?.ISBN
                                        onGenerateCode(code, EnumAction.SHARE)
                                    }
                                    else -> if (create?.fragmentType == EnumFragmentType.HISTORY || create?.fragmentType == EnumFragmentType.SCANNER) {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:"))
                                        intent.putExtra("sms_body", create?.text)
                                        startActivity(intent)
                                    } else {
                                        code = create?.text
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
                if (create?.fragmentType == EnumFragmentType.HISTORY || create?.fragmentType == EnumFragmentType.SCANNER) {
                    viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.Other, R.drawable.baseline_perm_contact_calendar_white_48, "AddressBook"))
                } else {
                    viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.SHARE, R.drawable.baseline_share_white_48, "Share"))
                }
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
                if (create?.fragmentType == EnumFragmentType.HISTORY || create?.fragmentType == EnumFragmentType.SCANNER) {
                    viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.Other, R.drawable.baseline_email_white_48, "Email"))
                } else {
                    viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.SHARE, R.drawable.baseline_share_white_48, "Share"))
                }
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
                viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.SEARCH, R.drawable.baseline_search_white_48, "Search"))
                if (create?.fragmentType == EnumFragmentType.HISTORY || create?.fragmentType == EnumFragmentType.SCANNER) {
                    viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.Other, R.drawable.baseline_textsms_white_48, "Product"))
                } else {
                    viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.SHARE, R.drawable.baseline_share_white_48, "Share"))
                }
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
                viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.SEARCH, R.drawable.baseline_search_white_48, "Search"))
                if (create?.fragmentType == EnumFragmentType.HISTORY || create?.fragmentType == EnumFragmentType.SCANNER) {
                    viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.Other, R.drawable.baseline_language_white_48, "Url"))
                } else {
                    viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.SHARE, R.drawable.baseline_share_white_48, "Share"))
                }
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
                if (create?.fragmentType == EnumFragmentType.HISTORY || create?.fragmentType == EnumFragmentType.SCANNER) {
                    viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.Other, R.drawable.baseline_network_wifi_white_48, "Wifi"))
                } else {
                    viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.SHARE, R.drawable.baseline_share_white_48, "Share"))
                }
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
                if (create?.fragmentType == EnumFragmentType.HISTORY || create?.fragmentType == EnumFragmentType.SCANNER) {
                    viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.Other, R.drawable.baseline_location_on_white_48, "Location"))
                } else {
                    viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.SHARE, R.drawable.baseline_share_white_48, "Share"))
                }
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
                if (create?.fragmentType == EnumFragmentType.HISTORY || create?.fragmentType == EnumFragmentType.SCANNER) {
                    viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.Other, R.drawable.baseline_phone_white_48, "Telephone"))
                } else {
                    viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.SHARE, R.drawable.baseline_share_white_48, "Share"))
                }
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
                if (create?.fragmentType == EnumFragmentType.HISTORY || create?.fragmentType == EnumFragmentType.SCANNER) {
                    viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.Other, R.drawable.baseline_textsms_white_48, "SMS"))
                } else {
                    viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.SHARE, R.drawable.baseline_share_white_48, "Share"))
                }
                onShowUI(llSMS)
                title = "SMS"
            }
            ParsedResultType.CALENDAR -> {
                /*Put item to HashClipboard*/
                viewModel.hashClipboard["title"] = create?.title
                viewModel.hashClipboard["location"] = create?.location
                viewModel.hashClipboard["description"] = create?.description
                viewModel.hashClipboard["startEventMilliseconds"] = Utils.convertMillisecondsToDateTime(create?.startEventMilliseconds ?: 0)
                viewModel.hashClipboard["endEventMilliseconds"] = Utils.convertMillisecondsToDateTime(create?.endEventMilliseconds ?: 0)
                eventTitle.text = create?.title
                eventLocation.text = create?.location
                eventDescription.text = create?.description
                eventBeginTime.text = Utils.convertMillisecondsToDateTime(create?.startEventMilliseconds ?: 0)
                eventEndTime.text = Utils.convertMillisecondsToDateTime(create?.endEventMilliseconds ?: 0)
                history = HistoryModel()
                history?.title = create?.title
                history?.location = create?.location
                history?.description = create?.description
                history?.startEvent = create?.startEvent
                history?.endEvent = create?.endEvent
                history?.startEventMilliseconds = create?.startEventMilliseconds
                history?.endEventMilliseconds = create?.endEventMilliseconds
                history?.createType = create?.createType?.name
                if (create?.fragmentType == EnumFragmentType.HISTORY || create?.fragmentType == EnumFragmentType.SCANNER) {
                    viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.Other, R.drawable.baseline_event_white_48, "Calendar"))
                } else {
                    viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.SHARE, R.drawable.baseline_share_white_48, "Share"))
                }
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
                viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.SEARCH, R.drawable.baseline_search_white_48, "Search"))
                if (create?.fragmentType == EnumFragmentType.HISTORY || create?.fragmentType == EnumFragmentType.SCANNER) {
                    viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.Other, R.drawable.baseline_textsms_white_48, "Share"))
                } else {
                    viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.SHARE, R.drawable.baseline_share_white_48, "ISBN"))
                }
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
                viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.SEARCH, R.drawable.baseline_search_white_48, "Search"))
                if (create?.fragmentType == EnumFragmentType.HISTORY || create?.fragmentType == EnumFragmentType.SCANNER) {
                    viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.Other, R.drawable.baseline_textsms_white_48, "Text"))
                } else {
                    viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.SHARE, R.drawable.baseline_share_white_48, "Share"))
                }
                onShowUI(llText)
                title = "Text"
            }
        }
        viewModel.mListNavigation.add(ItemNavigation(create?.createType, create?.fragmentType, EnumAction.CLIPBOARD, R.drawable.baseline_file_copy_white_48, "Clipboard"))
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
                        Utils.onAlertNotify(this@ScannerResultFragment,getString(R.string.no_items_found))
                    }
                }
                run {}
            }
            else -> {
            }
        }
    }

    private fun shareToSocial(value: Uri?) {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_STREAM, value)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(intent, "Share"))
    }

    fun onGenerateCode(code: String?, enumAction: EnumAction?) {
        try {
            val barcodeEncoder = BarcodeEncoder()
            val hints: MutableMap<EncodeHintType?, Any?> = EnumMap<EncodeHintType, Any?>(EncodeHintType::class.java)
            hints[EncodeHintType.MARGIN] = 2
            val theme: Theme? = Theme.getInstance()?.getThemeInfo()
            bitmap = if (history?.createType === ParsedResultType.PRODUCT.name) {
                barcodeEncoder.encodeBitmap(this, theme?.getPrimaryDarkColor() ?: 0, code, BarcodeFormat.valueOf(history?.barcodeFormat ?:""), 400, 400, hints)
            } else {
                barcodeEncoder.encodeBitmap(this, theme?.getPrimaryDarkColor() ?: 0, code, BarcodeFormat.QR_CODE, 400, 400, hints)
            }
            Utils.saveImage(bitmap, enumAction, create?.createType?.name, code, this)
        } catch (e: Exception) {
            Utils.Log(TAG, e.message)
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
            else -> Utils.Log(TAG,"Nothing")
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
                Utils.onAlertNotify(this@ScannerResultFragment, getString(R.string.copied_successful))
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
                Utils.onAlertNotify(this,"Can not open the link")
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
                    Utils.onAlertNotify(this,"Can not open the link")
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

    fun doShowAudienceAds(isShow : Boolean){
        if (isShow) {
            QRScannerApplication.getInstance().loadLargeAudienceAd(llAds)
        } else {
            rlAdsRoot.visibility = View.GONE
        }
    }

    val dataResult: Create
        get() {
            return viewModel.result ?: Create()
        }

    private val dataSource : MutableList<ItemNavigation>
        get() {
            return adapter?.getDataSource() ?: mutableListOf()
        }

    companion object {
        private val TAG = ScannerResultFragment::class.java.simpleName
    }
}