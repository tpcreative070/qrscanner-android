package tpcreative.co.qrscanner.ui.history
import android.Manifest
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.fragment_history.*
import tpcreative.co.qrscanner.common.HistorySingleton
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.network.base.ViewModelFactory
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.helper.SQLiteHelper
import tpcreative.co.qrscanner.model.HistoryModel
import tpcreative.co.qrscanner.viewmodel.HistoryViewModel

fun HistoryFragment.initUI(){
    setupViewModel()
    HistorySingleton.Companion.getInstance()?.setListener(this)
    viewModel.getListGroup()
    addRecyclerHeaders()
    bindData()
    onActionView()
}


private fun HistoryFragment.setupViewModel() {
    viewModel = ViewModelProvider(
            this,
            ViewModelFactory()
    ).get(HistoryViewModel::class.java)
}

fun HistoryFragment.deleteItem(){
    viewModel.deleteItem().observe(this, Observer {
        isSelectedAll = false
        misDeleted = false
        if (actionMode != null) {
            actionMode?.finish()
        }
        updateView()
    })
}

private fun HistoryFragment.onActionView() {
    rlSelect.setOnClickListener {
        val listHistory: MutableList<HistoryModel> = SQLiteHelper.getHistoryList()
        if (actionMode == null) {
            actionMode = QRScannerApplication.getInstance().getActivity()?.getToolbar()?.startActionMode(callback)
        }
        if (listHistory.size == 0) {
            return@setOnClickListener
        }
        val list: MutableList<HistoryModel> = viewModel.getListGroup()
        viewModel.mList.clear()
        for (index in list) {
            index.setDeleted(true)
            viewModel.mList.add(index)
        }
        recyclerView.removeAllCells()
        bindData()
        misDeleted = true
    }
    rlCSV.setOnClickListener {
        val listHistory: MutableList<HistoryModel> = SQLiteHelper.getHistoryList()
        if (listHistory.size == 0) {
            return@setOnClickListener
        }
        onAddPermissionSave()
        log(this::class.java,"action here")
    }
}

private fun HistoryFragment.onAddPermissionSave() {
    Dexter.withContext(requireActivity())
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

