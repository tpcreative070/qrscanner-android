package tpcreative.co.qrscanner.ui.changedesign

import android.os.Bundle
import tpcreative.co.qrscanner.common.activity.BaseActivity
import tpcreative.co.qrscanner.databinding.ActivityTemplateBinding
import java.util.TreeSet

class TemplateActivity  : BaseActivity(), TemplateAdapter.ItemSelectedListener{
    lateinit var viewModel :TemplateViewModel
    lateinit var binding : ActivityTemplateBinding
    lateinit var adapter : TemplateAdapter
    lateinit var loadedList : TreeSet<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTemplateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
    }

    override fun onClickItem(position: Int) {
        NewChangeDesignActivity.mResultTemplate?.invoke(viewModel.mTemplateList[position])
        finish()
    }
}