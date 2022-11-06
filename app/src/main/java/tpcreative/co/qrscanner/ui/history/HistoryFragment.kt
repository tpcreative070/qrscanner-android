package tpcreative.co.qrscanner.ui.history
import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import co.tpcreative.supersafe.common.network.Status
import com.google.zxing.client.result.ParsedResultType
import com.jaychang.srv.decoration.SectionHeaderProvider
import com.jaychang.srv.decoration.SimpleSectionHeaderProvider
import de.mrapp.android.dialog.MaterialDialog
import kotlinx.android.synthetic.main.fragment_history.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tpcreative.co.qrscanner.BuildConfig
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.controller.ServiceManager
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.helper.SQLiteHelper
import tpcreative.co.qrscanner.model.CreateModel
import tpcreative.co.qrscanner.model.EnumFragmentType
import tpcreative.co.qrscanner.model.EnumImplement
import tpcreative.co.qrscanner.model.HistoryModel
import tpcreative.co.qrscanner.ui.scannerresult.ScannerResultActivity
import tpcreative.co.qrscanner.viewmodel.HistoryViewModel
import java.io.File
import java.util.*

class HistoryFragment : BaseFragment(), HistoryCell.ItemSelectedListener, HistorySingleton.SingletonHistoryListener{
    lateinit var viewModel : HistoryViewModel
    var misDeleted = false
    var isSelectedAll = false
    var actionMode: ActionMode? = null
    val callback: ActionMode.Callback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            val menuInflater: MenuInflater? = mode?.menuInflater
            menuInflater?.inflate(R.menu.menu_select_all, menu)
            actionMode = mode
            val window: Window? = QRScannerApplication.getInstance().getActivity()?.window
            window?.statusBarColor = ContextCompat.getColor(context!!, R.color.colorAccentDark)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            when (item?.itemId) {
                R.id.menu_item_select_all -> {
                    val list: MutableList<HistoryModel> = viewModel.getListGroup()
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
                    val listHistory: MutableList<HistoryModel> = SQLiteHelper.getHistoryList()
                    if (listHistory.size == 0) {
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
            val list: MutableList<HistoryModel> = viewModel.getListGroup()
            viewModel.mList.clear()
            for (index in list) {
                index.setDeleted(false)
                viewModel.mList.add(index)
            }
            recyclerView.removeAllCells()
            bindData()
            misDeleted = false
            val window: Window? = QRScannerApplication.getInstance().getActivity()?.window
            window?.statusBarColor = ContextCompat.getColor(context!!, R.color.colorPrimaryDark)
        }
    }

    override fun getLayoutId(): Int {
        return 0
    }

    override fun getLayoutId(inflater: LayoutInflater?, viewGroup: ViewGroup?): View? {
        return inflater?.inflate(R.layout.fragment_history, viewGroup, false)
    }

    override fun work() {
        super.work()
        initUI()
    }

    override fun isDeleted(): Boolean {
        return misDeleted
    }

    fun addRecyclerHeaders() {
        val sh: SectionHeaderProvider<HistoryModel?> = object : SimpleSectionHeaderProvider<HistoryModel?>() {
            override fun getSectionHeaderView(history: HistoryModel, i: Int): View {
                val view: View = LayoutInflater.from(context).inflate(R.layout.history_item_header, null, false)
                val textView: TextView = view.findViewById<TextView?>(R.id.tvHeader)
                textView.text = history.getCategoryName()
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

    fun bindData() {
        val mListItems: MutableList<HistoryModel> = viewModel.mList
        val cells: MutableList<HistoryCell?> = ArrayList()
        //LOOP THROUGH GALAXIES INSTANTIATING THEIR CELLS AND ADDING TO CELLS COLLECTION
        for (items in mListItems) {
            val cell = HistoryCell(items)
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

    override fun getContext(): Context? {
        return activity
    }

    override fun onClickItem(position: Int, isChecked: Boolean) {
        Utils.Log(TAG, "position : $position - $isChecked")
        val result = viewModel.mList[position].isDeleted()
        viewModel.mList[position].setChecked(isChecked)
        Utils.Log(TAG,"isChecked $result")
        Utils.Log(TAG,"isDeleted ${viewModel.mList[position].isChecked()}")
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
        val list: MutableList<HistoryModel> = viewModel.getListGroup()
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
        misDeleted = true
    }

    override fun onClickItem(position: Int) {
        if (actionMode != null) {
            return
        }
        val create = CreateModel()
        val history: HistoryModel = viewModel.mList[position]
        create.id = history.id ?: 0
        create.favorite = history.favorite ?: false
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
            create.hidden = history.hidden ?: false
            create.ssId = history.ssId
            create.networkEncryption = history.networkEncryption
            create.password = history.password
            create.createType = ParsedResultType.WIFI
        } else if (history.createType.equals(ParsedResultType.GEO.name, ignoreCase = true)) {
            create.lat = history.lat ?:0.0
            create.lon = history.lon ?:0.0
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
            create.startEventMilliseconds = history.startEventMilliseconds ?:0
            create.endEventMilliseconds = history.endEventMilliseconds ?:0
            create.createType = ParsedResultType.CALENDAR
        } else if (history.createType.equals(ParsedResultType.ISBN.name, ignoreCase = true)) {
            create.ISBN = history.text
            create.createType = ParsedResultType.ISBN
        } else {
            create.text = history.text
            create.createType = ParsedResultType.TEXT
        }
        create.barcodeFormat = history.barcodeFormat
        create.noted = history.noted
        Utils.Log(TAG,"Format type ${history.barcodeFormat}")
        create.fragmentType = EnumFragmentType.HISTORY
        create.enumImplement = EnumImplement.VIEW
        viewForResult.launch(Navigator.onResultView(activity, create, ScannerResultActivity::class.java))
    }

    private val viewForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            Utils.Log(TAG,"${result.resultCode}")
        }
    }

    private fun shareToSocial(value: Uri?) {
        Utils.Log(TAG, "path call")
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "*/*"
        intent.putExtra(Intent.EXTRA_STREAM, value)
        intent.clipData = ClipData.newRawUri("", value);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(intent, "Share"))
    }

    override fun reloadData() {
        CoroutineScope(Dispatchers.Main).launch {
            if (recyclerView != null) {
                viewModel.getListGroup()
                recyclerView.removeAllCells()
                bindData()
            }
        }
    }

    fun exportData() = CoroutineScope(Dispatchers.Main).launch {
        val mResult =  ServiceManager.getInstance().onExportDatabaseCSVTask(requireContext(),EnumFragmentType.HISTORY)
        when(mResult.status){
            Status.SUCCESS -> {
                val path = mResult.data ?: ""
                val file = File(path)
                if (file.isFile) {
                    Utils.Log(TAG, "path : $path")
                    val uri: Uri = FileProvider.getUriForFile(requireContext(), BuildConfig.APPLICATION_ID + ".provider", file)
                    shareToSocial(uri)
                }
            }else -> {
                Utils.Log(TAG,"")
            }
        }
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        if (menuVisible) {
            if (actionMode != null) {
                actionMode?.finish()
            }
        }
    }

    fun dialogDelete() {
        val builder = MaterialDialog.Builder(requireContext(), Utils.getCurrentTheme())
        builder.setTitle(getString(R.string.delete))
        builder.setMessage(kotlin.String.format(getString(R.string.dialog_delete), viewModel.getCheckedCount().toString() + ""))
        builder.setNegativeButton(getString(R.string.no)) { dialogInterface, i -> }
        builder.setPositiveButton(getString(R.string.yes)) { dialogInterface, i ->
            deleteItem()
        }
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
        private val TAG = HistoryFragment::class.java.simpleName
        fun newInstance(index: Int): HistoryFragment {
            val fragment = HistoryFragment()
            val b = Bundle()
            b.putInt("index", index)
            fragment.arguments = b
            return fragment
        }
    }
}