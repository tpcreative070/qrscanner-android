package tpcreative.co.qrscanner.ui.save
import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import co.tpcreative.supersafe.common.network.Status
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.client.result.ParsedResultType
import com.jaychang.srv.decoration.SectionHeaderProvider
import com.jaychang.srv.decoration.SimpleSectionHeaderProvider
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import de.mrapp.android.dialog.MaterialDialog
import kotlinx.android.synthetic.main.fragment_saver.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tpcreative.co.qrscanner.BuildConfig
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.controller.ServiceManager
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.helper.SQLiteHelper
import tpcreative.co.qrscanner.model.*
import tpcreative.co.qrscanner.ui.create.*
import tpcreative.co.qrscanner.ui.scannerresult.ScannerResultFragment
import tpcreative.co.qrscanner.viewmodel.SaveViewModel
import java.io.File
import java.util.*

class SaveFragment : BaseFragment(), SaveCell.ItemSelectedListener, SaveSingleton.SingletonSaveListener, Utils.UtilsListener, MainSingleton.SingleTonMainListener {
    private var bitmap: Bitmap? = null
    private var code: String? = null
    private var share: SaveModel? = null
    private var edit: SaveModel? = null
    private var isDeleted = false
    private var isSelectedAll = false
    private var actionMode: ActionMode? = null
    lateinit var viewModel : SaveViewModel
    private val callback: ActionMode.Callback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            val menuInflater: MenuInflater? = mode?.getMenuInflater()
            menuInflater?.inflate(R.menu.menu_select_all, menu)
            actionMode = mode
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val window: Window? = QRScannerApplication.getInstance().getActivity()?.window
                window?.statusBarColor = ContextCompat.getColor(context!!, R.color.colorAccentDark)
            }
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            when (item?.itemId) {
                R.id.menu_item_select_all -> {
                    val list: MutableList<SaveModel> = viewModel.getListGroup()
                    viewModel.mList.clear()
                    isSelectedAll = if (isSelectedAll) {
                        for (index in list) {
                            index.setDeleted(true)
                            index.setChecked(false)
                            viewModel.mList.add(index)
                        }
                        false
                    } else {
                        for (index in list) {
                            index.setDeleted(true)
                            index.setChecked(true)
                            viewModel.mList.add(index)
                        }
                        true
                    }
                    if (actionMode != null) {
                        actionMode?.title = viewModel.getCheckedCount().toString() + " " + getString(R.string.selected)
                    }
                    recyclerView.removeAllCells()
                    bindData()
                    return true
                }
                R.id.menu_item_delete -> {
                    Utils.Log(TAG, "Delete call here")
                    val listSave: MutableList<SaveModel> = SQLiteHelper.getSaveList()
                    if (listSave.size == 0) {
                        Utils.Log(TAG, "Delete call here ???")
                        return false
                    }
                    Utils.Log(TAG, "start")
                    if (viewModel.getCheckedCount() > 0) {
                        dialogDelete()
                    }
                    return true
                }
            }
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            actionMode = null
            isSelectedAll = false
            val list: MutableList<SaveModel> = viewModel.getListGroup()
            viewModel.mList.clear()
            for (index in list) {
                index.setDeleted(false)
                viewModel.mList.add(index)
            }
            recyclerView.removeAllCells()
            bindData()
            isDeleted = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val window: Window? = QRScannerApplication.getInstance().getActivity()?.window
                window?.statusBarColor = ContextCompat.getColor(context!!, R.color.colorPrimaryDark)
            }
        }
    }

    override fun getLayoutId(): Int {
        return 0
    }

    override fun getLayoutId(inflater: LayoutInflater?, viewGroup: ViewGroup?): View? {
        return inflater?.inflate(R.layout.fragment_saver, viewGroup, false)
    }

    override fun work() {
        super.work()
        SaveSingleton.getInstance()?.setListener(this)
        initUI()
    }

    fun addRecyclerHeaders() {
        val sh: SectionHeaderProvider<SaveModel?> = object : SimpleSectionHeaderProvider<SaveModel?>() {
            override fun getSectionHeaderView(save: SaveModel, i: Int): View {
                val view: View = LayoutInflater.from(context).inflate(R.layout.save_item_header, null, false)
                val textView: TextView = view.findViewById(R.id.tvHeader)
                textView.text = save.getCategoryName()
                return view
            }

            override fun isSameSection(save: SaveModel, nextSave: SaveModel): Boolean {
                return save.getCategoryId() == nextSave.getCategoryId()
            }

            // Optional, whether the header is sticky, default false
            override fun isSticky(): Boolean {
                return false
            }
        }
        recyclerView.setSectionHeader(sh)
    }

    fun bindData() {
        val mListItems: MutableList<SaveModel> = viewModel.mList
        val cells: MutableList<SaveCell?> = ArrayList()
        for (items in mListItems) {
            val cell = SaveCell(items)
            cell.setListener(this)
            cells.add(cell)
        }
        if (mListItems.size > 0) {
            tvNotFoundItems.visibility = View.INVISIBLE
        } else {
            tvNotFoundItems.visibility = View.VISIBLE
        }
        recyclerView.addCells(cells)
    }

    override fun isDeleted(): Boolean {
        return isDeleted
    }

    override fun getContext(): Context? {
        return activity
    }

    override fun isShowDeleteAction(isDelete: Boolean) {
        val listSave: MutableList<SaveModel> = SQLiteHelper.getSaveList()
        if (isDelete) {
            if (actionMode == null) {
                actionMode = QRScannerApplication.getInstance().getActivity()?.getToolbar()?.startActionMode(callback)
            }
            if (listSave.size == 0) {
                return
            }
            val list: MutableList<SaveModel> = viewModel.getListGroup()
            viewModel.mList.clear()
            for (index in list) {
                index.setDeleted(true)
                viewModel.mList.add(index)
            }
            recyclerView.removeAllCells()
            bindData()
            isDeleted = true
        } else {
            if (listSave.size == 0) {
                return
            }
            onAddPermissionSave()
        }
    }

    override fun onClickItem(position: Int, isChecked: Boolean) {
        val result = viewModel.mList[position].isDeleted()
        viewModel.mList[position].setChecked(isChecked)
        if (result) {
            if (actionMode != null) {
                actionMode?.title = viewModel.getCheckedCount().toString() + " " + getString(R.string.selected)
            }
        }
    }

    override fun onLongClickItem(position: Int) {
        if (actionMode == null) {
            actionMode = QRScannerApplication.getInstance().getActivity()?.getToolbar()?.startActionMode(callback)
        }
        val list: MutableList<SaveModel> = viewModel.getListGroup()
        viewModel.mList.clear()
        for (index in list) {
            index.setDeleted(true)
            viewModel.mList.add(index)
        }
        viewModel.mList[position].setChecked(true)
        if (actionMode != null) {
            actionMode?.title = viewModel.getCheckedCount().toString() + " " + getString(R.string.selected)
        }
        recyclerView.removeAllCells()
        bindData()
        isDeleted = true
    }

    override fun onClickItem(position: Int) {
        if (actionMode != null) {
            return
        }
        val create = Create()
        val save: SaveModel = viewModel.mList[position]
        if (save.createType.equals(ParsedResultType.PRODUCT.name, ignoreCase = true)) {
            create.productId = save.text
            create.barcodeFormat = save.barcodeFormat
            Utils.Log(TAG, "Show..." + save.barcodeFormat)
            create.createType = ParsedResultType.PRODUCT
        } else if (save.createType.equals(ParsedResultType.ADDRESSBOOK.name, ignoreCase = true)) {
            create.address = save.address
            create.fullName = save.fullName
            create.email = save.email
            create.phone = save.phone
            create.createType = ParsedResultType.ADDRESSBOOK
        } else if (save.createType.equals(ParsedResultType.EMAIL_ADDRESS.name, ignoreCase = true)) {
            create.email = save.email
            create.subject = save.subject
            create.message = save.message
            create.createType = ParsedResultType.EMAIL_ADDRESS
        } else if (save.createType.equals(ParsedResultType.PRODUCT.name, ignoreCase = true)) {
            create.createType = ParsedResultType.PRODUCT
        } else if (save.createType.equals(ParsedResultType.URI.name, ignoreCase = true)) {
            create.url = save.url
            create.createType = ParsedResultType.URI
        } else if (save.createType.equals(ParsedResultType.WIFI.name, ignoreCase = true)) {
            create.hidden = save.hidden == true
            create.ssId = save.ssId
            create.networkEncryption = save.networkEncryption
            create.password = save.password
            create.createType = ParsedResultType.WIFI
        } else if (save.createType.equals(ParsedResultType.GEO.name, ignoreCase = true)) {
            create.lat = save.lat ?:0.0
            create.lon = save.lon ?:0.0
            create.query = save.query
            create.createType = ParsedResultType.GEO
        } else if (save.createType.equals(ParsedResultType.TEL.name, ignoreCase = true)) {
            create.phone = save.phone
            create.createType = ParsedResultType.TEL
        } else if (save.createType.equals(ParsedResultType.SMS.name, ignoreCase = true)) {
            create.phone = save.phone
            create.message = save.message
            create.createType = ParsedResultType.SMS
        } else if (save.createType.equals(ParsedResultType.CALENDAR.name, ignoreCase = true)) {
            create.title = save.title
            create.description = save.description
            create.location = save.location
            create.startEvent = save.startEvent
            create.endEvent = save.endEvent
            create.startEventMilliseconds = save.startEventMilliseconds ?:0
            create.endEventMilliseconds = save.endEventMilliseconds ?:0
            create.createType = ParsedResultType.CALENDAR
        } else {
            create.text = save.text
            create.createType = ParsedResultType.TEXT
        }
        Utils.Log(TAG, "Call intent")
        create.fragmentType = EnumFragmentType.SAVER
        Navigator.onResultView(activity, create, ScannerResultFragment::class.java)
    }

    override fun onClickShare(position: Int) {
        share = viewModel.mList[position]
        if (share?.createType.equals(ParsedResultType.ADDRESSBOOK.name, ignoreCase = true)) {
            code = "MECARD:N:" + share?.fullName + ";TEL:" + share?.phone + ";EMAIL:" + share?.email + ";ADR:" + share?.address + ";"
        } else if (share?.createType.equals(ParsedResultType.EMAIL_ADDRESS.name, ignoreCase = true)) {
            code = "MATMSG:TO:" + share?.email + ";SUB:" + share?.subject + ";BODY:" + share?.message + ";"
        } else if (share?.createType.equals(ParsedResultType.PRODUCT.name, ignoreCase = true)) {
        } else if (share?.createType.equals(ParsedResultType.URI.name, ignoreCase = true)) {
            code = share?.url
        } else if (share?.createType.equals(ParsedResultType.WIFI.name, ignoreCase = true)) {
            code = "WIFI:S:" + share?.ssId + ";T:" + share?.password + ";P:" + share?.networkEncryption + ";H:" + share?.hidden + ";"
        } else if (share?.createType.equals(ParsedResultType.GEO.name, ignoreCase = true)) {
            code = "geo:" + share?.lat + "," + share?.lon + "?q=" + share?.query + ""
        } else if (share?.createType.equals(ParsedResultType.TEL.name, ignoreCase = true)) {
            code = "tel:" + share?.phone + ""
        } else if (share?.createType.equals(ParsedResultType.SMS.name, ignoreCase = true)) {
            code = "smsto:" + share?.phone + ":" + share?.message
        } else if (share?.createType.equals(ParsedResultType.CALENDAR.name, ignoreCase = true)) {
            val builder = StringBuilder()
            builder.append("BEGIN:VEVENT")
            builder.append("\n")
            builder.append("SUMMARY:" + share?.title)
            builder.append("\n")
            builder.append("DTSTART:" + share?.startEvent)
            builder.append("\n")
            builder.append("DTEND:" + share?.endEvent)
            builder.append("\n")
            builder.append("LOCATION:" + share?.location)
            builder.append("\n")
            builder.append("DESCRIPTION:" + share?.description)
            builder.append("\n")
            builder.append("END:VEVENT")
            code = builder.toString()
        } else if (share?.createType.equals(ParsedResultType.PRODUCT.name, ignoreCase = true)) {
            code = share?.text
        } else {
            code = share?.text
        }
        onGenerateCode(code)
    }

    override fun onClickEdit(position: Int) {
        edit = viewModel.mList[position]
        if (edit?.createType.equals(ParsedResultType.PRODUCT.name, ignoreCase = true)) {
            Navigator.onGenerateView(activity, edit, BarcodeFragment::class.java)
        } else if (edit?.createType.equals(ParsedResultType.ADDRESSBOOK.name, ignoreCase = true)) {
            Navigator.onGenerateView(activity, edit, ContactFragment::class.java)
        } else if (edit?.createType.equals(ParsedResultType.EMAIL_ADDRESS.name, ignoreCase = true)) {
            Navigator.onGenerateView(activity, edit, EmailFragment::class.java)
        } else if (edit?.createType.equals(ParsedResultType.PRODUCT.name, ignoreCase = true)) {
        } else if (edit?.createType.equals(ParsedResultType.URI.name, ignoreCase = true)) {
            Navigator.onGenerateView(activity, edit, UrlFragment::class.java)
        } else if (edit?.createType.equals(ParsedResultType.WIFI.name, ignoreCase = true)) {
            Navigator.onGenerateView(activity, edit, WifiFragment::class.java)
        } else if (edit?.createType.equals(ParsedResultType.GEO.name, ignoreCase = true)) {
            Navigator.onGenerateView(activity, edit, LocationFragment::class.java)
        } else if (edit?.createType.equals(ParsedResultType.TEL.name, ignoreCase = true)) {
            Navigator.onGenerateView(activity, edit, TelephoneFragment::class.java)
        } else if (edit?.createType.equals(ParsedResultType.SMS.name, ignoreCase = true)) {
            Navigator.onGenerateView(activity, edit, MessageFragment::class.java)
        } else if (edit?.createType.equals(ParsedResultType.CALENDAR.name, ignoreCase = true)) {
            Navigator.onGenerateView(activity, edit, EventFragment::class.java)
        } else {
            Navigator.onGenerateView(activity, edit, TextFragment::class.java)
        }
    }

    fun shareToSocial(value: Uri?) {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_STREAM, value)
        startActivity(Intent.createChooser(intent, "Share"))
    }

    fun onGenerateCode(code: String?) {
        try {
            val barcodeEncoder = BarcodeEncoder()
            val hints: MutableMap<EncodeHintType?, Any?> = EnumMap<EncodeHintType, Any?>(EncodeHintType::class.java)
            hints[EncodeHintType.MARGIN] = 2
            val theme: Theme? = Theme.getInstance()?.getThemeInfo()
            bitmap = if (share?.createType === ParsedResultType.PRODUCT.name) {
                barcodeEncoder.encodeBitmap(context, theme?.getPrimaryDarkColor() ?:0, code, BarcodeFormat.valueOf(share?.barcodeFormat ?: ""), 400, 400, hints)
            } else {
                barcodeEncoder.encodeBitmap(context, theme?.getPrimaryDarkColor() ?: 0, code, BarcodeFormat.QR_CODE, 400, 400, hints)
            }
            Utils.saveImage(bitmap, EnumAction.SHARE, share?.createType, code, this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onSaved(path: String?, enumAction: EnumAction?) {
        val file = File(path)
        if (file.isFile) {
            val uri = Uri.fromFile(file)
            shareToSocial(uri)
        } else {
            Utils.onDropDownAlert(activity, getString(R.string.no_items_found))
        }
    }

    override fun reloadData() {
        viewModel.getListGroup()
        if (recyclerView!=null){
            recyclerView.removeAllCells()
            bindData()
        }
    }

    fun exportData() = CoroutineScope(Dispatchers.Main).launch {
        val mResult =  ServiceManager.getInstance().onExportDatabaseCSVTask(EnumFragmentType.SAVER)
        when(mResult.status){
            Status.SUCCESS -> {
                val path = mResult.data ?: ""
                val file = File(path)
                if (file.isFile) {
                    Utils.Log(TAG, "path : $path")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        val uri: Uri = FileProvider.getUriForFile(context!!, BuildConfig.APPLICATION_ID + ".provider", file)
                        shareToSocial(uri)
                    } else {
                        val uri = Uri.fromFile(file)
                        shareToSocial(uri)
                    }
                }
            }else -> {
            Utils.Log(TAG,"")
        }
        }
    }

    fun onAddPermissionSave() {
        Dexter.withContext(activity)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report?.areAllPermissionsGranted() == true) {
                           exportData()
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

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        if (menuVisible) {
            Utils.Log(TAG, "isVisible")
            MainSingleton.getInstance()?.setListener(this)
            QRScannerApplication.getInstance().getActivity()?.onShowFloatingButton(this@SaveFragment, true)
        } else {
            MainSingleton.getInstance()?.setListener(null)
            if (actionMode != null) {
                actionMode?.finish()
            }
            Utils.Log(TAG, "isInVisible")
        }
    }

    fun dialogDelete() {
        val builder = MaterialDialog.Builder(context!!, Utils.getCurrentTheme())
        builder.setTitle(getString(R.string.delete))
        builder.setMessage(kotlin.String.format(getString(R.string.dialog_delete), viewModel.getCheckedCount().toString() + ""))
        builder.setNegativeButton(getString(R.string.no), object : DialogInterface.OnClickListener {
            override fun onClick(dialogInterface: DialogInterface?, i: Int) {}
        })
        builder.setPositiveButton(getString(R.string.yes), object : DialogInterface.OnClickListener {
            override fun onClick(dialogInterface: DialogInterface?, i: Int) {
                deleteItem()
                isSelectedAll = false
                isDeleted = false
                if (actionMode != null) {
                    actionMode?.finish()
                }
            }
        })
        builder.show()
    }

    fun updateView() {
        recyclerView.removeAllCells()
        bindData()
    }

    override fun onStop() {
        super.onStop()
        Utils.Log(TAG, "onStop")
    }

    override fun onStart() {
        super.onStart()
        Utils.Log(TAG, "onStart")
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.Log(TAG, "onDestroy")
    }

    override fun onResume() {
        super.onResume()
        Utils.Log(TAG, "onResume")
    }

    companion object {
        private val TAG = SaveFragment::class.java.simpleName
        fun newInstance(index: Int): SaveFragment {
            val fragment = SaveFragment()
            val b = Bundle()
            b.putInt("index", index)
            fragment.arguments = b
            return fragment
        }
    }
}