package tpcreative.co.qrscanner.ui.changedesign

import androidx.lifecycle.ViewModelProvider
import tpcreative.co.qrscanner.common.network.base.ViewModelFactory
import tpcreative.co.qrscanner.ui.review.ReviewViewModel


fun ChangeDesignActivity.initUI(){
    setupViewModel()
    getIntentData()
}
private fun ChangeDesignActivity.getIntentData(){
    viewModel.getIntent(this) {
        if (it){
            onGenerateQR()
        }
    }
}

private fun ChangeDesignActivity.setupViewModel() {
    viewModel = ViewModelProvider(
        this,
        ViewModelFactory()
    ).get(ReviewViewModel::class.java)
}