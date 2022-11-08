package tpcreative.co.qrscanner.ui.create
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import tpcreative.co.qrscanner.common.network.base.ViewModelFactory
import tpcreative.co.qrscanner.viewmodel.GenerateViewModel

fun GenerateActivity.initUI(){
    setupViewModel()
    getDataList()
}


fun GenerateActivity.getDataList(){
    viewModel.getDataList().observe(this, Observer {
        bindData(it)
    })
}

private fun GenerateActivity.setupViewModel() {
    viewModel = ViewModelProvider(
            this,
            ViewModelFactory()
    ).get(GenerateViewModel::class.java)
}