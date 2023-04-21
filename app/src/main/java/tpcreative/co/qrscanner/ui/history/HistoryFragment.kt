package tpcreative.co.qrscanner.ui.history
import android.app.Activity
import android.app.Dialog
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
import com.afollestad.materialdialogs.MaterialDialog
import com.jaychang.srv.decoration.SectionHeaderProvider
import com.jaychang.srv.decoration.SimpleSectionHeaderProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tpcreative.co.qrscanner.BuildConfig
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.controller.ServiceManager
import tpcreative.co.qrscanner.common.extension.toText
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.databinding.FragmentHistoryBinding
import tpcreative.co.qrscanner.helper.SQLiteHelper
import tpcreative.co.qrscanner.model.*
import tpcreative.co.qrscanner.ui.scannerresult.ScannerResultActivity
import java.io.File
import java.util.*

class HistoryFragment : BaseFragment(), HistoryCell.ItemSelectedListener, HistorySingleton.SingletonHistoryListener{
    lateinit var viewModel : HistoryViewModel
    var misDeleted = false
    var isSelectedAll = false
    var actionMode: ActionMode? = null
    var dialog : Dialog? = null
    var dialogExport : Dialog? = null
    lateinit var binding : FragmentHistoryBinding
    private val callback: ActionMode.Callback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            val menuInflater: MenuInflater? = mode?.menuInflater
            menuInflater?.inflate(R.menu.menu_select_all, menu)
            actionMode = mode
            val window: Window? = QRScannerApplication.getInstance().getActivity()?.window
            window?.statusBarColor = ContextCompat.getColor(context!!, R.color.colorAccentDark)
            binding.rlDelete.visibility = View.INVISIBLE
            binding.rlCSV.visibility = View.INVISIBLE
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
                    binding.recyclerView.removeAllCells()
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
                        dialog?.show()
                        deleteItem()
                    }
                    return true
                }
            }
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            actionMode = null
            isSelectedAll = false
            binding.rlDelete.visibility = View.VISIBLE
            binding.rlCSV.visibility = View.VISIBLE
            val list: MutableList<HistoryModel> = viewModel.getListGroup()
            viewModel.mList.clear()
            for (index in list) {
                index.setDeleted(false)
                viewModel.mList.add(index)
            }
            binding.recyclerView.removeAllCells()
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
        binding = FragmentHistoryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun work() {
        super.work()
        initUI()
        dialog = ProgressDialog.progressDialog(requireContext(),R.string.waiting_for_delete.toText())
        dialogExport = ProgressDialog.progressDialog(requireContext(),R.string.waiting_for_export.toText())
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
        binding.recyclerView.setSectionHeader(sh)
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
            binding.tvNotFoundItems.visibility = View.INVISIBLE
        } else {
            binding.tvNotFoundItems.visibility = View.VISIBLE
        }
        binding.recyclerView.addCells(cells)
        binding.tvCount.text = "${viewModel.count()}"
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
        binding.recyclerView.removeAllCells()
        bindData()
        misDeleted = true
    }

    override fun onClickItem(position: Int) {
        if (actionMode != null) {
            return
        }
        val history: HistoryModel = viewModel.mList[position]
        val create = GeneralModel(history,EnumFragmentType.HISTORY,EnumImplement.VIEW)
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
            if (binding.recyclerView != null) {
                viewModel.getListGroup()
                binding.recyclerView.removeAllCells()
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
                dialogExport?.dismiss()
            }else -> {
            Utils.Log(TAG,"")
            dialogExport?.dismiss()
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

    fun dialogEntireDelete() {
        MaterialDialog(requireContext()).show {
            title(R.string.delete)
            message(text = getString(R.string.delete_entire_history))
            positiveButton(R.string.yes){
                dialog?.show()
                deleteEntireItem()
            }
            negativeButton (R.string.no){
            }
        }
    }

    fun updateView() {
        binding.recyclerView.removeAllCells()
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