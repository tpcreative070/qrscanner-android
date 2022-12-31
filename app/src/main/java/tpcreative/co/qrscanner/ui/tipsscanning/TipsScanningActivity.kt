package tpcreative.co.qrscanner.ui.tipsscanning

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.ui.supportedcode.SupportedCodeViewModel

class TipsScanningActivity : AppCompatActivity() {
    lateinit var viewModel : TipsScanningViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tips_scanning)
        initUI()
    }
}