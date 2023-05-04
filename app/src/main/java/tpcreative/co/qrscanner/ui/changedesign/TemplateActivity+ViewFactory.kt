package tpcreative.co.qrscanner.ui.changedesign

import android.view.View
import androidx.lifecycle.ViewModelProvider
import tpcreative.co.qrscanner.common.extension.addCircleRipple
import tpcreative.co.qrscanner.common.network.base.ViewModelFactory


fun TemplateActivity.initUI(){
    setupViewModel()
    binding.doneCancelBar.tvTemplate.visibility = View.GONE
    binding.doneCancelBar.tvCancel.visibility = View.VISIBLE
    binding.doneCancelBar.imgCancel.visibility = View.VISIBLE
    binding.doneCancelBar.imgDone.visibility = View.GONE
    binding.doneCancelBar.btnSave.visibility = View.GONE
    binding.doneCancelBar.imgCancel.addCircleRipple()
    binding.doneCancelBar.imgCancel.setOnClickListener {
        finish()
    }
}

private fun TemplateActivity.setupViewModel() {
    viewModel = ViewModelProvider(
        this,
        ViewModelFactory()
    ).get(TemplateViewModel::class.java)
}