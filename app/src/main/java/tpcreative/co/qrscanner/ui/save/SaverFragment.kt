package tpcreative.co.qrscanner.ui.save

import android.Manifest
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.*
import com.karumi.dexter.listener.PermissionRequest
import de.mrapp.android.dialog.MaterialDialog
import tpcreative.co.qrscanner.BuildConfig
import tpcreative.co.qrscanner.common.Navigator
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.controller.ServiceManager
import tpcreative.co.qrscanner.model.Create
import tpcreative.co.qrscanner.model.Theme
import java.io.File
import java.util.*

class SaverFragment : BaseFragment(), SaveView, SaveCell.ItemSelectedListener, SingletonSaveListener, UtilsListener, SingleTonMainListener {
    @BindView(R.id.rlRoot)
    var rlRoot: RelativeLayout? = null

    @BindView(R.id.tvNotFoundItems)
    var tvNotFoundItems: AppCompatTextView? = null

    @BindView(R.id.recyclerView)
    var recyclerView: SimpleRecyclerView? = null
    private var presenter: SavePresenter? = null
    private var bitmap: Bitmap? = null
    private var code: String? = null
    private var share: SaveModel? = null
    private var edit: SaveModel? = null
    private var isDeleted = false
    private var isSelectedAll = false
    private var actionMode: ActionMode? = null
    private val callback: ActionMode.Callback? = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            val menuInflater: MenuInflater? = mode.getMenuInflater()
            menuInflater.inflate(R.menu.menu_select_all, menu)
            actionMode = mode
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val window: Window = QRScannerApplication.Companion.getInstance().getActivity().getWindow()
                window.statusBarColor = ContextCompat.getColor(context, R.color.colorAccentDark)
            }
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            when (item.getItemId()) {
                R.id.menu_item_select_all -> {
                    val list: MutableList<SaveModel?>? = presenter.getListGroup()
                    presenter.mList.clear()
                    isSelectedAll = if (isSelectedAll) {
                        for (index in list) {
                            index.setDeleted(true)
                            index.setChecked(false)
                            presenter.mList.add(index)
                        }
                        false
                    } else {
                        for (index in list) {
                            index.setDeleted(true)
                            index.setChecked(true)
                            presenter.mList.add(index)
                        }
                        true
                    }
                    if (actionMode != null) {
                        actionMode.setTitle(presenter.getCheckedCount().toString() + " " + getString(R.string.selected))
                    }
                    recyclerView.removeAllCells()
                    bindData()
                    return true
                }
                R.id.menu_item_delete -> {
                    Utils.Log(TAG, "Delete call here")
                    val listSave: MutableList<SaveModel?> = SQLiteHelper.getSaveList()
                    if (listSave.size == 0) {
                        Utils.Log(TAG, "Delete call here ???")
                        return false
                    }
                    Utils.Log(TAG, "start")
                    if (presenter.getCheckedCount() > 0) {
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
            val list: MutableList<SaveModel?>? = presenter.getListGroup()
            presenter.mList.clear()
            for (index in list) {
                index.setDeleted(false)
                presenter.mList.add(index)
            }
            recyclerView.removeAllCells()
            bindData()
            isDeleted = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val window: Window = QRScannerApplication.Companion.getInstance().getActivity().getWindow()
                window.statusBarColor = ContextCompat.getColor(context, R.color.colorPrimaryDark)
            }
        }
    }

    protected override fun getLayoutId(): Int {
        return 0
    }

    protected override fun getLayoutId(inflater: LayoutInflater?, viewGroup: ViewGroup?): View? {
        return inflater.inflate(R.layout.fragment_saver, viewGroup, false)
    }

    protected override fun work() {
        super.work()
        SaveSingleton.Companion.getInstance().setListener(this)
        presenter = SavePresenter()
        presenter.bindView(this)
        presenter.getListGroup()
        addRecyclerHeaders()
        bindData()
    }

    private fun addRecyclerHeaders() {
        val sh: SectionHeaderProvider<SaveModel?> = object : SimpleSectionHeaderProvider<SaveModel?>() {
            override fun getSectionHeaderView(save: SaveModel, i: Int): View {
                val view: View = LayoutInflater.from(context).inflate(R.layout.save_item_header, null, false)
                val textView: TextView = view.findViewById<TextView?>(R.id.tvHeader)
                textView.setText(save.getCategoryName())
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

    private fun bindData() {
        val mListItems: MutableList<SaveModel?>? = presenter.mList
        val cells: MutableList<SaveCell?> = ArrayList()
        for (items in mListItems) {
            val cell = SaveCell(items)
            cell.setListener(this)
            cells.add(cell)
        }
        if (mListItems != null) {
            if (mListItems.size > 0) {
                tvNotFoundItems.setVisibility(View.INVISIBLE)
            } else {
                tvNotFoundItems.setVisibility(View.VISIBLE)
            }
        }
        recyclerView.addCells(cells)
    }

    override fun isDeleted(): Boolean {
        return isDeleted
    }

    override fun getContext(): Context? {
        return getActivity()
    }

    override fun isShowDeleteAction(isDelete: Boolean) {
        val listSave: MutableList<SaveModel?> = SQLiteHelper.getSaveList()
        if (isDelete) {
            if (actionMode == null) {
                actionMode = QRScannerApplication.Companion.getInstance().getActivity().getToolbar().startActionMode(callback)
            }
            if (listSave.size == 0) {
                return
            }
            val list: MutableList<SaveModel?>? = presenter.getListGroup()
            presenter.mList.clear()
            for (index in list) {
                index.setDeleted(true)
                presenter.mList.add(index)
            }
            recyclerView.removeAllCells()
            bindData()
            isDeleted = true
        } else {
            if (listSave == null) {
                return
            }
            if (listSave.size == 0) {
                return
            }
            onAddPermissionSave()
        }
    }

    override fun onClickItem(position: Int, isChecked: Boolean) {
        Log.d(TAG, "position : $position - $isChecked")
        val result = presenter.mList[position].isDeleted
        presenter.mList[position].isChecked = isChecked
        if (result) {
            if (actionMode != null) {
                actionMode.setTitle(presenter.getCheckedCount().toString() + " " + getString(R.string.selected))
            }
        }
    }

    override fun onLongClickItem(position: Int) {
        if (actionMode == null) {
            actionMode = QRScannerApplication.Companion.getInstance().getActivity().getToolbar().startActionMode(callback)
        }
        val list: MutableList<SaveModel?>? = presenter.getListGroup()
        presenter.mList.clear()
        for (index in list) {
            index.setDeleted(true)
            presenter.mList.add(index)
        }
        presenter.mList[position].isChecked = true
        if (actionMode != null) {
            actionMode.setTitle(presenter.getCheckedCount().toString() + " " + getString(R.string.selected))
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
        val save: SaveModel? = presenter.mList[position]
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
            create.hidden = save.hidden
            create.ssId = save.ssId
            create.networkEncryption = save.networkEncryption
            create.password = save.password
            create.createType = ParsedResultType.WIFI
        } else if (save.createType.equals(ParsedResultType.GEO.name, ignoreCase = true)) {
            create.lat = save.lat
            create.lon = save.lon
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
            create.startEventMilliseconds = save.startEventMilliseconds
            create.endEventMilliseconds = save.endEventMilliseconds
            create.createType = ParsedResultType.CALENDAR
        } else {
            create.text = save.text
            create.createType = ParsedResultType.TEXT
        }
        Utils.Log(TAG, "Call intent")
        create.fragmentType = EnumFragmentType.SAVER
        Navigator.onResultView<ScannerResultFragment?>(getActivity(), create, ScannerResultFragment::class.java)
    }

    override fun onClickShare(position: Int) {
        share = presenter.mList[position]
        if (share.createType.equals(ParsedResultType.ADDRESSBOOK.name, ignoreCase = true)) {
            code = "MECARD:N:" + share.fullName + ";TEL:" + share.phone + ";EMAIL:" + share.email + ";ADR:" + share.address + ";"
        } else if (share.createType.equals(ParsedResultType.EMAIL_ADDRESS.name, ignoreCase = true)) {
            code = "MATMSG:TO:" + share.email + ";SUB:" + share.subject + ";BODY:" + share.message + ";"
        } else if (share.createType.equals(ParsedResultType.PRODUCT.name, ignoreCase = true)) {
        } else if (share.createType.equals(ParsedResultType.URI.name, ignoreCase = true)) {
            code = share.url
        } else if (share.createType.equals(ParsedResultType.WIFI.name, ignoreCase = true)) {
            code = "WIFI:S:" + share.ssId + ";T:" + share.password + ";P:" + share.networkEncryption + ";H:" + share.hidden + ";"
        } else if (share.createType.equals(ParsedResultType.GEO.name, ignoreCase = true)) {
            code = "geo:" + share.lat + "," + share.lon + "?q=" + share.query + ""
        } else if (share.createType.equals(ParsedResultType.TEL.name, ignoreCase = true)) {
            code = "tel:" + share.phone + ""
        } else if (share.createType.equals(ParsedResultType.SMS.name, ignoreCase = true)) {
            code = "smsto:" + share.phone + ":" + share.message
        } else if (share.createType.equals(ParsedResultType.CALENDAR.name, ignoreCase = true)) {
            val builder = StringBuilder()
            builder.append("BEGIN:VEVENT")
            builder.append("\n")
            builder.append("SUMMARY:" + share.title)
            builder.append("\n")
            builder.append("DTSTART:" + share.startEvent)
            builder.append("\n")
            builder.append("DTEND:" + share.endEvent)
            builder.append("\n")
            builder.append("LOCATION:" + share.location)
            builder.append("\n")
            builder.append("DESCRIPTION:" + share.description)
            builder.append("\n")
            builder.append("END:VEVENT")
            code = builder.toString()
        } else if (share.createType.equals(ParsedResultType.PRODUCT.name, ignoreCase = true)) {
            code = share.text
        } else {
            code = share.text
        }
        onGenerateCode(code)
    }

    override fun onClickEdit(position: Int) {
        edit = presenter.mList[position]
        if (edit.createType.equals(ParsedResultType.PRODUCT.name, ignoreCase = true)) {
            Navigator.onGenerateView<BarcodeFragment?>(getActivity(), edit, BarcodeFragment::class.java)
        } else if (edit.createType.equals(ParsedResultType.ADDRESSBOOK.name, ignoreCase = true)) {
            Navigator.onGenerateView<ContactFragment?>(getActivity(), edit, ContactFragment::class.java)
        } else if (edit.createType.equals(ParsedResultType.EMAIL_ADDRESS.name, ignoreCase = true)) {
            Navigator.onGenerateView<EmailFragment?>(getActivity(), edit, EmailFragment::class.java)
        } else if (edit.createType.equals(ParsedResultType.PRODUCT.name, ignoreCase = true)) {
        } else if (edit.createType.equals(ParsedResultType.URI.name, ignoreCase = true)) {
            Navigator.onGenerateView<UrlFragment?>(getActivity(), edit, UrlFragment::class.java)
        } else if (edit.createType.equals(ParsedResultType.WIFI.name, ignoreCase = true)) {
            Navigator.onGenerateView<WifiFragment?>(getActivity(), edit, WifiFragment::class.java)
        } else if (edit.createType.equals(ParsedResultType.GEO.name, ignoreCase = true)) {
            Navigator.onGenerateView<LocationFragment?>(getActivity(), edit, LocationFragment::class.java)
        } else if (edit.createType.equals(ParsedResultType.TEL.name, ignoreCase = true)) {
            Navigator.onGenerateView<TelephoneFragment?>(getActivity(), edit, TelephoneFragment::class.java)
        } else if (edit.createType.equals(ParsedResultType.SMS.name, ignoreCase = true)) {
            Navigator.onGenerateView<MessageFragment?>(getActivity(), edit, MessageFragment::class.java)
        } else if (edit.createType.equals(ParsedResultType.CALENDAR.name, ignoreCase = true)) {
            Navigator.onGenerateView<EventFragment?>(getActivity(), edit, EventFragment::class.java)
        } else {
            Navigator.onGenerateView<TextFragment?>(getActivity(), edit, TextFragment::class.java)
        }
    }

    fun shareToSocial(value: Uri?) {
        val intent = Intent()
        intent.setAction(Intent.ACTION_SEND)
        intent.setType("image/*")
        intent.putExtra(Intent.EXTRA_STREAM, value)
        startActivity(Intent.createChooser(intent, "Share"))
    }

    fun onGenerateCode(code: String?) {
        try {
            val barcodeEncoder = BarcodeEncoder()
            val hints: MutableMap<EncodeHintType?, Any?> = EnumMap<EncodeHintType?, Any?>(EncodeHintType::class.java)
            hints[EncodeHintType.MARGIN] = 2
            val theme: Theme = Theme.Companion.getInstance().getThemeInfo()
            bitmap = if (share.createType === ParsedResultType.PRODUCT.name) {
                barcodeEncoder.encodeBitmap(context, theme.primaryDarkColor, code, BarcodeFormat.valueOf(share.barcodeFormat), 400, 400, hints)
            } else {
                barcodeEncoder.encodeBitmap(context, theme.primaryDarkColor, code, BarcodeFormat.QR_CODE, 400, 400, hints)
            }
            Utils.saveImage(bitmap, EnumAction.SHARE, share.createType, code, this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onSaved(path: String?, enumAction: EnumAction?) {
        Log.d(TAG, "path : $path")
        val file = File(path)
        if (file.isFile) {
            val uri = Uri.fromFile(file)
            shareToSocial(uri)
        } else {
            //Utils.showGotItSnackbar(getView(), R.string.no_items_found);
            Utils.onDropDownAlert(getActivity(), getString(R.string.no_items_found))
        }
    }

    override fun reloadData() {
        if (presenter != null && recyclerView != null) {
            presenter.getListGroup()
            recyclerView.removeAllCells()
            bindData()
        }
    }

    fun onAddPermissionSave() {
        Dexter.withContext(getActivity())
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report.areAllPermissionsGranted()) {
                            ServiceManager.Companion.getInstance().onExportDatabaseCSVTask(EnumFragmentType.SAVER, object : ServiceManagerListener {
                                override fun onExportingSVCCompleted(path: String?) {
                                    val file = File(path)
                                    if (file.isFile) {
                                        Log.d(TAG, "path : $path")
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            val uri: Uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID.toString() + ".provider", file)
                                            shareToSocial(uri)
                                        } else {
                                            val uri = Uri.fromFile(file)
                                            shareToSocial(uri)
                                        }
                                    }
                                }
                            })
                        } else {
                            Log.d(TAG, "Permission is denied")
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            /*Miss add permission in manifest*/
                            Log.d(TAG, "request permission is failed")
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

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        if (menuVisible) {
            Log.d(TAG, "isVisible")
            MainSingleton.Companion.getInstance().setListener(this)
            QRScannerApplication.Companion.getInstance().getActivity().onShowFloatingButton(this@SaverFragment, true)
        } else {
            MainSingleton.Companion.getInstance().setListener(null)
            if (actionMode != null) {
                actionMode.finish()
            }
            Log.d(TAG, "isInVisible")
        }
    }

    fun dialogDelete() {
        val builder = MaterialDialog.Builder(context, Utils.getCurrentTheme())
        builder.setTitle(getString(R.string.delete))
        builder.setMessage(kotlin.String.format(getString(R.string.dialog_delete), presenter.getCheckedCount().toString() + ""))
        builder.setNegativeButton(getString(R.string.no), object : DialogInterface.OnClickListener {
            override fun onClick(dialogInterface: DialogInterface?, i: Int) {}
        })
        builder.setPositiveButton(getString(R.string.yes), object : DialogInterface.OnClickListener {
            override fun onClick(dialogInterface: DialogInterface?, i: Int) {
                presenter.deleteItem()
                isSelectedAll = false
                isDeleted = false
                if (actionMode != null) {
                    actionMode.finish()
                }
            }
        })
        builder.show()
    }

    override fun updateView() {
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
        private val TAG = SaverFragment::class.java.simpleName
        fun newInstance(index: Int): SaverFragment? {
            val fragment = SaverFragment()
            val b = Bundle()
            b.putInt("index", index)
            fragment.setArguments(b)
            return fragment
        }
    }
}