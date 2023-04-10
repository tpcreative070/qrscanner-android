package tpcreative.co.qrscanner.ui.changedesign.fragment

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import tpcreative.co.qrscanner.common.ListenerView
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.extension.calculateNoOfColumns
import tpcreative.co.qrscanner.common.extension.isLandscape
import tpcreative.co.qrscanner.common.extension.toJson
import tpcreative.co.qrscanner.common.view.GridSpacingItemDecoration
import tpcreative.co.qrscanner.databinding.FragmentLogoBinding
import tpcreative.co.qrscanner.model.ChangeDesignModel

class LogoFragment : ConstraintLayout, LogoFragmentAdapter.ItemSelectedListener {
    private lateinit var mListener : ListenerView
    private lateinit var binding : FragmentLogoBinding
    private lateinit var mContext : Context
    private lateinit var adapter: LogoFragmentAdapter
    private lateinit var mList : MutableList<ChangeDesignModel>
    private lateinit var mInflater: LayoutInflater
    private var logoIndexSelected : Int = -1
    private lateinit var listener : ListenerLogoFragment

    constructor(context: Context) : super(context) {
        mContext = context
        mInflater = LayoutInflater.from(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        mContext = context
        mInflater = LayoutInflater.from(context)
    }
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
    }

    fun setListener(mListenerView: ListenerView){
        this.mListener = mListenerView
    }

    fun setBinding(binding : FragmentLogoBinding,listenerLogoFragment: ListenerLogoFragment){
        this.listener = listenerLogoFragment
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

    fun setSelectedIndex(index : Int){
        this.logoIndexSelected = index
    }

    private fun initRecycleView(layoutInflater: LayoutInflater) {
        adapter = LogoFragmentAdapter(layoutInflater, mContext, this)
        var mNoOfColumns = Utils.calculateNoOfColumns(mContext,80F)
        if(context.isLandscape()){
            mNoOfColumns = Utils.calculateNoOfColumns(mContext,160F)
        }
        val mLayoutManager: RecyclerView.LayoutManager = GridLayoutManager(mContext, mNoOfColumns)
        binding.recyclerView.addItemDecoration(GridSpacingItemDecoration(mNoOfColumns, 10, true))
        binding.recyclerView.layoutManager = mLayoutManager
        binding.recyclerView.itemAnimator = DefaultItemAnimator()
        binding.recyclerView.adapter = adapter
    }

    override fun onClickItem(position: Int) {
        listener.logoSelectedIndex(position)
        this.logoIndexSelected = position
        Utils.Log("TAG","index $position")
        reload()
    }

    private fun reload(){
        mList = mList.mapIndexed { index, data ->
            if (index == logoIndexSelected) data.apply {
                isSelected = true
                Utils.Log("TAG","Selected at $index")
            }
            else data.apply {
                isSelected = false
            }
        }.toMutableList().apply {
            show()
        }
    }

    private fun initializedData(){
        mList = mutableListOf()
        mList.addAll(listener.getData())
    }

    fun show(){
        Utils.Log("TAG","Show data ${mList.toJson()}")
        adapter.setDataSource(mList)
    }

    fun load(){
        initRecycleView(mInflater)
        initializedData()
        reload()
    }

    interface ListenerLogoFragment {
        fun logoSelectedIndex(index : Int)
        fun getData() : MutableList<ChangeDesignModel>
    }
}