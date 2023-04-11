package tpcreative.co.qrscanner.ui.changedesign.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import tpcreative.co.qrscanner.common.BaseFragment
import tpcreative.co.qrscanner.databinding.FragmentColorBinding

class ColorFragment : BaseFragment() {
    private lateinit var binding : FragmentColorBinding

    override fun getLayoutId(): Int {
        return 0
    }

    override fun getLayoutId(inflater: LayoutInflater?, viewGroup: ViewGroup?): View {
        binding = FragmentColorBinding.inflate(layoutInflater)
        return binding.root
    }
}