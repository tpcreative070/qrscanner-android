package tpcreative.co.qrscanner.ui.history
import android.Manifest
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.*
import com.karumi.dexter.listener.PermissionRequest
import de.mrapp.android.dialog.MaterialDialog
import tpcreative.co.qrscanner.BuildConfig
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.controller.ServiceManager
import tpcreative.co.qrscanner.model.Create
import java.io.File
import java.util.*

class HistoryFragment : BaseFragment(), HistoryView, HistoryCell.ItemSelectedListener, HistorySingleton.SingletonHistoryListener, MainSingleton.SingleTonMainListener {
    @BindView(R.id.rlRoot)
    var rlRoot: RelativeLayout? = null

    @BindView(R.id.tvNotFoundItems)
    var tvNotFoundItems: AppCompatTextView? = null

    @BindView(R.id.recyclerView)
    var recyclerView: SimpleRecyclerView? = null
    private var presenter: HistoryPresenter? = null
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
            val i = item.getItemId()
            when (item.getItemId()) {
                R.id.menu_item_select_all -> {
                    val list: MutableList<HistoryModel?>? = presenter.getListGroup()
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
                    val listHistory: MutableList<HistoryModel?> = SQLiteHelper.getHistoryList()
                            ?: return false
                    if (listHistory.size == 0) {
                        return false
                    }
                    Log.d(TAG, "start")
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
            val list: MutableList<HistoryModel?>? = presenter.getListGroup()
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
        return inflater.inflate(R.layout.fragment_history, viewGroup, false)
    }

    protected override fun work() {
        super.work()
        HistorySingleton.Companion.getInstance().setListener(this)
        presenter = HistoryPresenter()
        presenter.bindView(this)
        presenter.getListGroup()
        addRecyclerHeaders()
        bindData()
    }

    override fun isDeleted(): Boolean {
        return isDeleted
    }

    private fun addRecyclerHeaders() {
        val sh: SectionHeaderProvider<HistoryModel?> = object : SimpleSectionHeaderProvider<HistoryModel?>() {
            override fun getSectionHeaderView(history: HistoryModel, i: Int): View {
                val view: View = LayoutInflater.from(context).inflate(R.layout.history_item_header, null, false)
                val textView: TextView = view.findViewById<TextView?>(R.id.tvHeader)
                textView.setText(history.getCategoryName())
                return view
            }

            override fun isSameSection(history: HistoryModel, nextHistory: HistoryModel): Boolean {
                return history.getCategoryId() == nextHistory.getCategoryId()
            }

            // Optional, whether the header is sticky, default false
            override fun isSticky(): Boolean {
                return false
            }
        }
        recyclerView.setSectionHeader(sh)
    }

    private fun bindData() {
        val mListItems: MutableList<HistoryModel?>? = presenter.mList
        val cells: MutableList<HistoryCell?> = ArrayList()
        //LOOP THROUGH GALAXIES INSTANTIATING THEIR CELLS AND ADDING TO CELLS COLLECTION
        for (items in mListItems) {
            val cell = HistoryCell(items)
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

    override fun getContext(): Context? {
        return getActivity()
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

    override fun isShowDeleteAction(isDelete: Boolean) {
        val listHistory: MutableList<HistoryModel?> = SQLiteHelper.getHistoryList()
        if (isDelete) {
            if (actionMode == null) {
                actionMode = QRScannerApplication.Companion.getInstance().getActivity().getToolbar().startActionMode(callback)
            }
            if (listHistory == null) {
                return
            }
            if (listHistory.size == 0) {
                return
            }
            val list: MutableList<HistoryModel?>? = presenter.getListGroup()
            presenter.mList.clear()
            for (index in list) {
                index.setDeleted(true)
                presenter.mList.add(index)
            }
            recyclerView.removeAllCells()
            bindData()
            isDeleted = true
        } else {
            if (listHistory == null) {
                return
            }
            if (listHistory.size == 0) {
                return
            }
            onAddPermissionSave()
        }
    }

    override fun onLongClickItem(position: Int) {
        if (actionMode == null) {
            actionMode = QRScannerApplication.Companion.getInstance().getActivity().getToolbar().startActionMode(callback)
        }
        val list: MutableList<HistoryModel?>? = presenter.getListGroup()
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
        val history: HistoryModel? = presenter.mList[position]
        if (history.createType.equals(ParsedResultType.ADDRESSBOOK.name, ignoreCase = true)) {
            create.address = history.address
            create.fullName = history.fullName
            create.email = history.email
            create.phone = history.phone
            create.createType = ParsedResultType.ADDRESSBOOK
        } else if (history.createType.equals(ParsedResultType.EMAIL_ADDRESS.name, ignoreCase = true)) {
            create.email = history.email
            create.subject = history.subject
            create.message = history.message
            create.createType = ParsedResultType.EMAIL_ADDRESS
        } else if (history.createType.equals(ParsedResultType.PRODUCT.name, ignoreCase = true)) {
            create.createType = ParsedResultType.PRODUCT
            create.productId = history.text
        } else if (history.createType.equals(ParsedResultType.URI.name, ignoreCase = true)) {
            create.url = history.url
            create.createType = ParsedResultType.URI
        } else if (history.createType.equals(ParsedResultType.WIFI.name, ignoreCase = true)) {
            create.hidden = history.hidden
            create.ssId = history.ssId
            create.networkEncryption = history.networkEncryption
            create.password = history.password
            create.createType = ParsedResultType.WIFI
        } else if (history.createType.equals(ParsedResultType.GEO.name, ignoreCase = true)) {
            create.lat = history.lat
            create.lon = history.lon
            create.query = history.query
            create.createType = ParsedResultType.GEO
        } else if (history.createType.equals(ParsedResultType.TEL.name, ignoreCase = true)) {
            create.phone = history.phone
            create.createType = ParsedResultType.TEL
        } else if (history.createType.equals(ParsedResultType.SMS.name, ignoreCase = true)) {
            create.phone = history.phone
            create.message = history.message
            create.createType = ParsedResultType.SMS
        } else if (history.createType.equals(ParsedResultType.CALENDAR.name, ignoreCase = true)) {
            create.title = history.title
            create.description = history.description
            create.location = history.location
            create.startEvent = history.startEvent
            create.endEvent = history.endEvent
            create.startEventMilliseconds = history.startEventMilliseconds
            create.endEventMilliseconds = history.endEventMilliseconds
            create.createType = ParsedResultType.CALENDAR
        } else if (history.createType.equals(ParsedResultType.ISBN.name, ignoreCase = true)) {
            create.ISBN = history.text
            create.createType = ParsedResultType.ISBN
        } else {
            create.text = history.text
            create.createType = ParsedResultType.TEXT
        }
        create.fragmentType = EnumFragmentType.HISTORY
        Navigator.onResultView<ScannerResultFragment?>(getActivity(), create, ScannerResultFragment::class.java)
    }

    override fun onClickShare(position: Int) {
        val history: HistoryModel? = presenter.mList[position]
        val sb = StringBuilder()
        if (history.createType.equals(ParsedResultType.ADDRESSBOOK.name, ignoreCase = true)) {
            sb.append("Address :" + history.address)
            sb.append("\n")
            sb.append("FullName :" + history.fullName)
            sb.append("\n")
            sb.append("Email :" + history.email)
            sb.append("\n")
            sb.append("Phone :" + history.phone)
        } else if (history.createType.equals(ParsedResultType.EMAIL_ADDRESS.name, ignoreCase = true)) {
            sb.append("Email :" + history.email)
            sb.append("\n")
            sb.append("Subject :" + history.subject)
            sb.append("\n")
            sb.append("Message :" + history.message)
        } else if (history.createType.equals(ParsedResultType.PRODUCT.name, ignoreCase = true)) {
            sb.append("ProductId :" + history.text)
        } else if (history.createType.equals(ParsedResultType.URI.name, ignoreCase = true)) {
            sb.append("Url :" + history.url)
        } else if (history.createType.equals(ParsedResultType.WIFI.name, ignoreCase = true)) {
            sb.append("SSId :" + history.ssId)
            sb.append("\n")
            sb.append("Password :" + history.password)
            sb.append("\n")
            sb.append("Network encryption :" + history.networkEncryption)
            sb.append("\n")
            sb.append("Hidden :" + history.hidden)
        } else if (history.createType.equals(ParsedResultType.GEO.name, ignoreCase = true)) {
            sb.append("Latitude :" + history.lat)
            sb.append("\n")
            sb.append("Longitude :" + history.lon)
            sb.append("\n")
            sb.append("Query :" + history.query)
        } else if (history.createType.equals(ParsedResultType.TEL.name, ignoreCase = true)) {
            sb.append("Phone :" + history.phone)
        } else if (history.createType.equals(ParsedResultType.SMS.name, ignoreCase = true)) {
            sb.append("Phone :" + history.phone)
            sb.append("\n")
            sb.append("Message :" + history.message)
        } else if (history.createType.equals(ParsedResultType.CALENDAR.name, ignoreCase = true)) {
            sb.append("Title :" + history.title)
            sb.append("\n")
            sb.append("Description :" + history.description)
            sb.append("\n")
            sb.append("Location :" + history.location)
            sb.append("\n")
            sb.append("Start event :" + history.startEvent)
            sb.append("\n")
            sb.append("End event :" + history.endEvent)
        } else if (history.createType.equals(ParsedResultType.ISBN.name, ignoreCase = true)) {
            sb.append("ISBN :" + history.text)
        } else {
            sb.append("Text :" + history.text)
        }
        shareToSocial(sb.toString())
    }

    fun shareToSocial(value: String?) {
        val intent = Intent()
        intent.setAction(Intent.ACTION_SEND)
        intent.setType("text/plain")
        intent.putExtra(Intent.EXTRA_TEXT, value)
        startActivity(Intent.createChooser(intent, "Share"))
    }

    fun shareToSocial(value: Uri?) {
        Log.d(TAG, "path call")
        val intent = Intent()
        intent.setAction(Intent.ACTION_SEND)
        intent.setType("image/*")
        intent.putExtra(Intent.EXTRA_STREAM, value)
        startActivity(Intent.createChooser(intent, "Share"))
    }

    override fun reloadData() {
        if (presenter != null) {
            if (recyclerView != null) {
                presenter.getListGroup()
                recyclerView.removeAllCells()
                bindData()
            }
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
                            ServiceManager.Companion.getInstance().onExportDatabaseCSVTask(EnumFragmentType.HISTORY, object : ServiceManagerListener {
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
            QRScannerApplication.Companion.getInstance().getActivity().onShowFloatingButton(this@HistoryFragment, true)
        } else {
            MainSingleton.Companion.getInstance().setListener(null)
            Log.d(TAG, "isInVisible")
            if (actionMode != null) {
                actionMode.finish()
            }
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
        Log.d(TAG, "onStop")
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }

    companion object {
        private val TAG = HistoryFragment::class.java.simpleName
        fun newInstance(index: Int): HistoryFragment? {
            val fragment = HistoryFragment()
            val b = Bundle()
            b.putInt("index", index)
            fragment.setArguments(b)
            return fragment
        }
    }
}