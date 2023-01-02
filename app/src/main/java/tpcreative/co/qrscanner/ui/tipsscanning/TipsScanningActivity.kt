package tpcreative.co.qrscanner.ui.tipsscanning

import android.os.Bundle
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide

class TipsScanningActivity : BaseActivitySlide() , TipsScanningAdapter.ItemSelectedListener{
    var adapter: TipsScanningAdapter? = null
    lateinit var viewModel : TipsScanningViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tips_scanning)
        initUI()
    }

    override fun onClickItem(position: Int) {

    }
}