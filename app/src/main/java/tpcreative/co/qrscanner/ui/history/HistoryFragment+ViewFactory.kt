package tpcreative.co.qrscanner.ui.history
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.fragment_history.*
import kotlinx.android.synthetic.main.fragment_history.view.*
import tpcreative.co.qrscanner.common.HistorySingleton
import tpcreative.co.qrscanner.common.network.base.ViewModelFactory
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.helper.SQLiteHelper
import tpcreative.co.qrscanner.model.HistoryModel
import tpcreative.co.qrscanner.viewmodel.HistoryViewModel

fun HistoryFragment.initUI(){
    setupViewModel()
    HistorySingleton.getInstance()?.setListener(this)
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
        dialog?.dismiss()
    })
}

fun HistoryFragment.deleteEntireItem(){
    viewModel.deleteEntireItem().observe(this, Observer {
        updateView()
        dialog?.dismiss()
    })
}

private fun HistoryFragment.onActionView() {
    rlDelete.setOnClickListener {
        val listHistory: MutableList<HistoryModel> = SQLiteHelper.getHistoryList()
        if (listHistory.size == 0) {
            return@setOnClickListener
        }
        dialogEntireDelete()
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
    exportData()
    dialogExport?.show()
}

