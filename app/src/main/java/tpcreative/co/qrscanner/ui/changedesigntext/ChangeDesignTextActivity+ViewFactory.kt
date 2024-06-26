package tpcreative.co.qrscanner.ui.changedesigntext

import androidx.lifecycle.ViewModelProvider
import tpcreative.co.qrscanner.common.network.base.ViewModelFactory


fun ChangeDesignTextActivity.initUI(){
    setupViewModel()
    viewModel.getIntent(this){

    }
}
private fun ChangeDesignTextActivity.setupViewModel() {
    viewModel = ViewModelProvider(
        this,
        ViewModelFactory()
    ).get(ChangeDesignTextViewModel::class.java)
}