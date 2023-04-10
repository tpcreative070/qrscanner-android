package tpcreative.co.qrscanner.ui.changedesign.fragment

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.ListenerView
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.databinding.FragmentColorBinding
import tpcreative.co.qrscanner.databinding.FragmentDotsBinding

class DotsFragment : ConstraintLayout {
    private lateinit var mListener : ListenerView
    private lateinit var binding : FragmentDotsBinding

    constructor(context: Context) : super(context) {
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {

    }
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {

    }

    fun setListener(mListenerView: ListenerView){
        this.mListener = mListenerView
    }

    fun setBinding(binding : FragmentDotsBinding){
        this.binding = binding
        binding.includeLayoutHeader.imgClose.setOnClickListener {
            Utils.Log("TAG","Close")
            mListener.onClose()
        }
        binding.includeLayoutHeader.imgDone.setOnClickListener {
            Utils.Log("TAG","Done")
            mListener.onDone()
        }
    }
}