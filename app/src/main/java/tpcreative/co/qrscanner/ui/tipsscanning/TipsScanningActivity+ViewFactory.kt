package tpcreative.co.qrscanner.ui.tipsscanning
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_help.*
import tpcreative.co.qrscanner.common.network.base.ViewModelFactory



fun TipsScanningActivity.initUI(){
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    setupViewModel()
}


private fun TipsScanningActivity.setupViewModel() {
    viewModel = ViewModelProvider(
        this,
        ViewModelFactory()
    ).get(TipsScanningViewModel::class.java)
}