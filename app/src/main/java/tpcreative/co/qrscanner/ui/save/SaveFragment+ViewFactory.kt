package tpcreative.co.qrscanner.ui.save
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import tpcreative.co.qrscanner.common.network.base.ViewModelFactory
import tpcreative.co.qrscanner.helper.SQLiteHelper
import tpcreative.co.qrscanner.model.SaveModel

fun SaveFragment.initUI(){
    setupViewModel()
    viewModel.getListGroup()
    addRecyclerHeaders()
    bindData()
    onActionView()
}

private fun SaveFragment.setupViewModel() {
    viewModel = ViewModelProvider(
            this,
            ViewModelFactory()
    ).get(SaveViewModel::class.java)
}

fun SaveFragment.deleteEntireItem(){
    viewModel.deleteEntireItem().observe(this, Observer {
        updateView()
        dialog?.dismiss()
    })
}

private fun SaveFragment.onActionView() {
    binding.rlDelete.setOnClickListener {
        val listSave: MutableList<SaveModel> = SQLiteHelper.getSaveList()
        if (listSave.size == 0) {
            return@setOnClickListener
        }
        dialogEntireDelete()
    }

    binding.rlCSV.setOnClickListener {
        val listSave: MutableList<SaveModel> = SQLiteHelper.getSaveList()
        if (listSave.size == 0) {
            return@setOnClickListener
        }
        onAddPermissionSave()
    }
}

fun SaveFragment.deleteItem() {
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

