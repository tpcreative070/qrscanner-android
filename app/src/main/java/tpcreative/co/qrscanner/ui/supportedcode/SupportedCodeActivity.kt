package tpcreative.co.qrscanner.ui.supportedcode

import android.os.Bundle
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide
import tpcreative.co.qrscanner.databinding.ActivitySuportedCodeBinding

class SupportedCodeActivity : BaseActivitySlide(), SupportedCodeAdapter.ItemSelectedListener {
    var adapter: SupportedCodeAdapter? = null
    lateinit var viewModel : SupportedCodeViewModel
    lateinit var binding : ActivitySuportedCodeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySuportedCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
    }

    override fun onClickItem(position: Int) {
    }
}