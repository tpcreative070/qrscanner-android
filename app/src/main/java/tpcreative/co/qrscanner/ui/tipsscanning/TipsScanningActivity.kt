package tpcreative.co.qrscanner.ui.tipsscanning

import android.os.Bundle
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide
import tpcreative.co.qrscanner.databinding.ActivitySuportedCodeBinding
import tpcreative.co.qrscanner.databinding.ActivityTipsScanningBinding

class TipsScanningActivity : BaseActivitySlide() , TipsScanningAdapter.ItemSelectedListener{
    var adapter: TipsScanningAdapter? = null
    lateinit var viewModel : TipsScanningViewModel
    lateinit var binding : ActivityTipsScanningBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTipsScanningBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
    }

    override fun onClickItem(position: Int) {
    }
}