package tpcreative.co.qrscanner.ui.save
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
import com.google.zxing.BarcodeFormat
import com.google.zxing.client.result.ParsedResultType
import com.jaychang.srv.decoration.SectionHeaderProvider
import com.jaychang.srv.decoration.SimpleSectionHeaderProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tpcreative.co.qrscanner.BuildConfig
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.controller.ServiceManager
import tpcreative.co.qrscanner.common.extension.isQRCode
import tpcreative.co.qrscanner.common.extension.onBarCodeId
import tpcreative.co.qrscanner.common.extension.toText
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.databinding.FragmentSaverBinding
import tpcreative.co.qrscanner.helper.SQLiteHelper
import tpcreative.co.qrscanner.model.*
import tpcreative.co.qrscanner.ui.changedesign.NewChangeDesignActivity
import tpcreative.co.qrscanner.ui.create.*
import tpcreative.co.qrscanner.ui.scannerresult.ScannerResultActivity
import java.io.File
import java.util.*

class SaveFragment : BaseFragment(), SaveCell.ItemSelectedListener, SaveSingleton.SingletonSaveListener, Utils.UtilsListener {
    private var edit: SaveModel? = null
    var misDeleted = false
    var isSelectedAll = false
    var actionMode: ActionMode? = null
    var dialog : Dialog? = null
    var dialogExport : Dialog? = null
    lateinit var viewModel : SaveViewModel
    lateinit var binding : FragmentSaverBinding
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
                    binding.recyclerView.removeAllCells()
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
            val list: MutableList<SaveModel> = viewModel.getListGroup()
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
        binding = FragmentSaverBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun work() {
        super.work()
        SaveSingleton.getInstance()?.setListener(this)
        initUI()
        dialog = ProgressDialog.progressDialog(requireContext(),R.string.waiting_for_delete.toText())
        dialogExport = ProgressDialog.progressDialog(requireContext(),R.string.waiting_for_export.toText())
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
        binding.recyclerView.setSectionHeader(sh)
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
            binding.tvNotFoundItems.visibility = View.INVISIBLE
        } else {
            binding.tvNotFoundItems.visibility = View.VISIBLE
        }
        binding.recyclerView.addCells(cells)
        binding.tvCount.text = "${viewModel.count()}"
    }

    override fun isDeleted(): Boolean {
        return misDeleted
    }

    override fun getContext(): Context? {
        return activity
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
        binding.recyclerView.removeAllCells()
        bindData()
        misDeleted = true
    }

    override fun onClickItem(position: Int) {
        if (actionMode != null) {
            return
        }
        val saver: SaveModel = viewModel.mList[position]
        val create = GeneralModel(saver,EnumFragmentType.SAVER,EnumImplement.VIEW)
        viewForResult.launch(Navigator.onResultView(activity, create, ScannerResultActivity::class.java))
    }

    override fun onClickItemImage(position: Int) {
        val save: SaveModel = viewModel.mList[position]
        if (actionMode != null || !Utils.isQRCode(save.barcodeFormat)) {
            return
        }
        val create = GeneralModel(save,EnumFragmentType.SAVER,EnumImplement.VIEW)
        viewForChangeDesignResult.launch(Navigator.onResultView(requireActivity(),create, NewChangeDesignActivity::class.java))
    }

    private val viewForChangeDesignResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            Utils.Log(TAG,"${result.resultCode}")
            SaveSingleton.getInstance()?.reloadDataChangeDesign()
        }
    }

    private val viewForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            Utils.Log(TAG,"${result.resultCode}")
        }
    }

    override fun onClickEdit(position: Int) {
        edit = viewModel.mList[position]
        edit?.let {
            if (edit?.createType.equals(ParsedResultType.PRODUCT.name, ignoreCase = true)) {
                Navigator.onGenerateView(activity,GeneralModel(it,EnumFragmentType.SAVER,EnumImplement.EDIT), BarcodeActivity::class.java)
            } else if (edit?.createType.equals(ParsedResultType.ADDRESSBOOK.name, ignoreCase = true)) {
                Navigator.onGenerateView(activity,GeneralModel(it,EnumFragmentType.SAVER,EnumImplement.EDIT), ContactActivity::class.java)
            } else if (edit?.createType.equals(ParsedResultType.EMAIL_ADDRESS.name, ignoreCase = true)) {
                Navigator.onGenerateView(activity,GeneralModel(it,EnumFragmentType.SAVER,EnumImplement.EDIT), EmailActivity::class.java)
            }else if (edit?.createType.equals(ParsedResultType.URI.name, ignoreCase = true)) {
                Navigator.onGenerateView(activity,GeneralModel(it,EnumFragmentType.SAVER,EnumImplement.EDIT), UrlActivity::class.java)
            } else if (edit?.createType.equals(ParsedResultType.WIFI.name, ignoreCase = true)) {
                Navigator.onGenerateView(activity,GeneralModel(it,EnumFragmentType.SAVER,EnumImplement.EDIT), WifiActivity::class.java)
            } else if (edit?.createType.equals(ParsedResultType.GEO.name, ignoreCase = true)) {
                Navigator.onGenerateView(activity,GeneralModel(it,EnumFragmentType.SAVER,EnumImplement.EDIT), LocationActivity::class.java)
            } else if (edit?.createType.equals(ParsedResultType.TEL.name, ignoreCase = true)) {
                Navigator.onGenerateView(activity,GeneralModel(it,EnumFragmentType.SAVER,EnumImplement.EDIT), TelephoneActivity::class.java)
            } else if (edit?.createType.equals(ParsedResultType.SMS.name, ignoreCase = true)) {
                Navigator.onGenerateView(activity,GeneralModel(it,EnumFragmentType.SAVER,EnumImplement.EDIT), MessageActivity::class.java)
            } else if (edit?.createType.equals(ParsedResultType.CALENDAR.name, ignoreCase = true)) {
                Navigator.onGenerateView(activity,GeneralModel(it,EnumFragmentType.SAVER,EnumImplement.EDIT), EventActivity::class.java)
            }
            else if (edit?.createType.equals(ParsedResultType.PRODUCT.name, ignoreCase = true)) {
                Navigator.onGenerateView(activity,GeneralModel(it,EnumFragmentType.SAVER,EnumImplement.EDIT), BarcodeActivity::class.java)
            }
            else if (edit?.createType.equals(ParsedResultType.ISBN.name, ignoreCase = true)) {
                Navigator.onGenerateView(activity,GeneralModel(it,EnumFragmentType.SAVER,EnumImplement.EDIT), BarcodeActivity::class.java)
            }
            else {
                if (edit?.barcodeFormat == BarcodeFormat.QR_CODE.name){
                    Navigator.onGenerateView(activity,GeneralModel(it,EnumFragmentType.SAVER,EnumImplement.EDIT), TextActivity::class.java)
                }else{
                    Navigator.onGenerateView(activity,GeneralModel(it,EnumFragmentType.SAVER,EnumImplement.EDIT), BarcodeActivity::class.java)
                }
            }
        }
    }

    private fun shareToSocial(value: Uri?) {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "*/*"
        intent.putExtra(Intent.EXTRA_STREAM, value)
        intent.clipData = ClipData.newRawUri("", value);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(intent, "Share"))
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
        CoroutineScope(Dispatchers.Main).launch {
            viewModel.getListGroup()
            if (binding.recyclerView!=null){
                binding.recyclerView.removeAllCells()
                bindData()
            }
        }
    }

    private fun exportData() = CoroutineScope(Dispatchers.Main).launch {
        val mResult =  ServiceManager.getInstance().onExportDatabaseCSVTask(requireContext(),EnumFragmentType.SAVER)
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

    fun onAddPermissionSave() {
        exportData()
        dialogExport?.show()
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        if (!menuVisible) {
            if (actionMode != null) {
                actionMode?.finish()
            }
        }
    }

    fun dialogEntireDelete() {
        MaterialDialog(requireContext()).show {
            title(R.string.delete)
            message(text = getString(R.string.delete_entire_save))
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