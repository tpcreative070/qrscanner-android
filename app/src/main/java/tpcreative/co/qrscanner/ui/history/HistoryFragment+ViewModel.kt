package tpcreative.co.qrscanner.ui.history
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import tpcreative.co.qrscanner.common.HistorySingleton
import tpcreative.co.qrscanner.common.network.base.ViewModelFactory
import tpcreative.co.qrscanner.viewmodel.HistoryViewModel

fun HistoryFragment.initUI(){
    setupViewModel()
    HistorySingleton.Companion.getInstance()?.setListener(this)
    viewModel.getListGroup()
    addRecyclerHeaders()
    bindData()
}


private fun HistoryFragment.setupViewModel() {
    viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory()
    ).get(HistoryViewModel::class.java)
}

fun HistoryFragment.deleteItem(){
    viewModel.deleteItem().observe(this, Observer {
        updateView()
    })
}