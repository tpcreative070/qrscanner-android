package tpcreative.co.qrscanner.ui.supportedcode

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide

class SupportedCodeActivity : BaseActivitySlide(), SupportedCodeAdapter.ItemSelectedListener {
    var adapter: SupportedCodeAdapter? = null
    lateinit var viewModel : SupportedCodeViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_suported_code)
        initUI()
    }

    override fun onClickItem(position: Int) {
    }
}