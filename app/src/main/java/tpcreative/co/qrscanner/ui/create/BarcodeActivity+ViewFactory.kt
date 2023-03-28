package tpcreative.co.qrscanner.ui.create

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.network.base.ViewModelFactory
import tpcreative.co.qrscanner.viewmodel.GenerateViewModel

fun BarcodeActivity.initUI(){
    TAG = this::class.java.name
    setupViewModel()
    setSupportActionBar(binding.toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    doInitView()
    getIntentData()
    binding.btnRandom.setOnClickListener {
        val mValue = Utils.generateRandomDigits(viewModel.mLength - 1).toString() + ""
        val mResult = Utils.generateEAN(mValue)
        binding.edtBarCode.setText(mResult)
    }
}

private fun BarcodeActivity.getIntentData(){
    viewModel.getIntent(this).observe(this, Observer {
        if (it!=null){
            this.save = it
            onSetData()
        }
    })
}

fun BarcodeActivity.doInitView(){
    viewModel.doInitView().observe(this, Observer {
        onInitView()
    })
}

fun BarcodeActivity.getBarcodeFormat(){
    viewModel.getBarcodeFormat().observe(this, Observer {
        onSetView()
    })
}


private fun BarcodeActivity.setupViewModel() {
    viewModel = ViewModelProvider(
            this,
            ViewModelFactory()
    ).get(GenerateViewModel::class.java)
}