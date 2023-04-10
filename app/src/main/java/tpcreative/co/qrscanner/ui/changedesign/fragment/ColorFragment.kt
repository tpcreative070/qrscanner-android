package tpcreative.co.qrscanner.ui.changedesign.fragment

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import tpcreative.co.qrscanner.common.ListenerView
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.databinding.FragmentColorBinding


class ColorFragment : ConstraintLayout {
    private lateinit var mListener : ListenerView
    private lateinit var binding : FragmentColorBinding
    private lateinit var mContext : Context

    constructor(context: Context) : super(context) {
        this.mContext = context
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {

    }
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {

    }

    fun setListener(mListenerView: ListenerView){
        this.mListener = mListenerView
    }

    fun setBinding(binding : FragmentColorBinding){
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