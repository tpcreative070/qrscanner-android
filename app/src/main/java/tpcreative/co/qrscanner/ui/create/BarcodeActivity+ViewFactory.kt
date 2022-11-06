package tpcreative.co.qrscanner.ui.create

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_barcode.*
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.extension.serializable
import tpcreative.co.qrscanner.common.network.base.ViewModelFactory
import tpcreative.co.qrscanner.model.SaveModel
import tpcreative.co.qrscanner.viewmodel.GenerateViewModel

fun BarcodeActivity.initUI(){
    TAG = this::class.java.name
    setupViewModel()
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    doInitView()
    val mData = intent?.serializable(getString(R.string.key_data),SaveModel::class.java)
    if (mData != null) {
        save = mData
        onSetData()
    } else {
        Utils.Log(TAG, "Data is null")
    }
    btnRandom.setOnClickListener {
        val mValue = Utils.generateRandomDigits(viewModel.mLength - 1).toString() + ""
        val mResult = Utils.generateEAN(mValue)
        edtBarCode.setText(mResult)
    }
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