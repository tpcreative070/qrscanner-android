package tpcreative.co.qrscanner.ui.create

import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.fragment_barcode.*
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.network.base.ViewModelFactory
import tpcreative.co.qrscanner.model.SaveModel
import tpcreative.co.qrscanner.viewmodel.GenerateViewModel

fun BarcodeFragment.initUI(){
    TAG = this::class.java.name
    setupViewModel()
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    doInitView()
    val bundle = intent.extras
    val mData = bundle?.get(getString(R.string.key_data)) as SaveModel?
    if (mData != null) {
        save = mData
        onSetData()
    } else {
        Utils.Log(TAG, "Data is null")
    }
    btnRandom.setOnClickListener {
        val mValue = Utils.generateRandomDigits(viewModel.mLength - 1).toString() + ""
        val mResult = Utils.generateEAN(mValue)
        edtText.setText(mResult)
    }
}

fun BarcodeFragment.doInitView(){
    viewModel.doInitView().observe(this, Observer {
        onInitView()
    })
}

fun BarcodeFragment.getBarcodeFormat(){
    viewModel.getBarcodeFormat().observe(this, Observer {
        onSetView()
    })
}


private fun BarcodeFragment.setupViewModel() {
    viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory()
    ).get(GenerateViewModel::class.java)
}