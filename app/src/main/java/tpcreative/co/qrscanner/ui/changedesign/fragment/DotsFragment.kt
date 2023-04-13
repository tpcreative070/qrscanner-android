package tpcreative.co.qrscanner.ui.changedesign.fragment

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.BaseFragment
import tpcreative.co.qrscanner.common.ListenerView
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.databinding.FragmentColorBinding
import tpcreative.co.qrscanner.databinding.FragmentDotsBinding

class DotsFragment : BaseFragment() {

    private lateinit var binding : FragmentDotsBinding
    override fun getLayoutId(): Int {
        return 0
    }

    override fun getLayoutId(inflater: LayoutInflater?, viewGroup: ViewGroup?): View? {
        binding = FragmentDotsBinding.inflate(layoutInflater)
        return binding.root
    }
}