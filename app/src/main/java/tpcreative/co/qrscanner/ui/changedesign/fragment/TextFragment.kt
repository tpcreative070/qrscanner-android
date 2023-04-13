package tpcreative.co.qrscanner.ui.changedesign.fragment

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import tpcreative.co.qrscanner.common.BaseFragment
import tpcreative.co.qrscanner.common.ListenerView
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.databinding.FragmentLogoBinding
import tpcreative.co.qrscanner.databinding.FragmentTextBinding

class TextFragment : BaseFragment(){
    private lateinit var binding : FragmentTextBinding

    override fun getLayoutId(): Int {
        return 0
    }

    override fun getLayoutId(inflater: LayoutInflater?, viewGroup: ViewGroup?): View? {
        binding = FragmentTextBinding.inflate(layoutInflater)
        return binding.root
    }

}