package tpcreative.co.qrscanner.ui.save
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import tpcreative.co.qrscanner.common.network.base.ViewModelFactory
import tpcreative.co.qrscanner.viewmodel.SaveViewModel

fun SaveFragment.initUI(){
    setupViewModel()
    viewModel.getListGroup()
    addRecyclerHeaders()
    bindData()
}

private fun SaveFragment.setupViewModel() {
    viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory()
    ).get(SaveViewModel::class.java)
}

fun SaveFragment.deleteItem() {
    viewModel.deleteItem().observe(this, Observer {
        isSelectedAll = false
        misDeleted = false
        if (actionMode != null) {
            actionMode?.finish()
        }
        updateView()
    })
}

