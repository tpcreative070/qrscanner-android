package tpcreative.co.qrscanner.ui.changedesign.fragment

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.BaseFragment
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.extension.calculateNoOfColumns
import tpcreative.co.qrscanner.common.extension.isPortrait
import tpcreative.co.qrscanner.common.extension.toJson
import tpcreative.co.qrscanner.common.view.GridSpacingItemDecoration
import tpcreative.co.qrscanner.databinding.FragmentLogoBinding
import tpcreative.co.qrscanner.model.EnumShape
import tpcreative.co.qrscanner.model.LogoModel

class LogoFragment : BaseFragment(), LogoFragmentAdapter.ItemSelectedListener {
    private lateinit var binding : FragmentLogoBinding
    private lateinit var mContext : Context
    private lateinit var adapter: LogoFragmentAdapter
    private lateinit var mList : MutableList<LogoModel>
    private lateinit var mInflater: LayoutInflater
    private var index : LogoModel? = null
    private var listener : ListenerLogoFragment? = null
    private var currentPosition  = 0
    private var shape : EnumShape = EnumShape.ORIGINAL
    private var uuId : String = ""

    override fun getLayoutId(): Int {
        return 0
    }

    override fun getLayoutId(inflater: LayoutInflater?, viewGroup: ViewGroup?): View {
        binding = FragmentLogoBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun work() {
        super.work()
        mContext = requireContext()
        mInflater = requireActivity().layoutInflater
        binding.rlOriginal.setOnClickListener {
            binding.rlOriginal.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_selected_highlight)
            binding.rlSquare.background = null
            binding.rlCircle.background =null
            shape = EnumShape.ORIGINAL
            listener?.logoSelectedIndex(currentPosition,shape,mList[currentPosition])
        }
        binding.rlSquare.setOnClickListener {
            binding.rlOriginal.background = null
            binding.rlSquare.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_selected_highlight)
            binding.rlCircle.background =null
            shape = EnumShape.SQUARE
            listener?.logoSelectedIndex(currentPosition,shape,mList[currentPosition])
        }
        binding.rlCircle.setOnClickListener {
            binding.rlOriginal.background = null
            binding.rlSquare.background = null
            binding.rlCircle.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_selected_highlight)
            shape = EnumShape.CIRCLE
            listener?.logoSelectedIndex(currentPosition,shape,mList[currentPosition])
        }
        when(shape){
            EnumShape.ORIGINAL ->{
                binding.rlOriginal.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_selected_highlight)
            }
            EnumShape.SQUARE ->{
                binding.rlSquare.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_selected_highlight)
            }
            else -> {
                binding.rlCircle.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_selected_highlight)
            }
        }
        load()
    }

    override fun onResume() {
        super.onResume()
        show()
    }

    fun setBinding(listenerView: ListenerLogoFragment){
        this.listener = listenerView
    }

    fun setSelectedIndex(index : LogoModel,mShare : EnumShape,uuId :String){
        this.index = index
        this.shape = mShare
        this.uuId = uuId
    }

    private fun initRecycleView(layoutInflater: LayoutInflater) {
        adapter = LogoFragmentAdapter(layoutInflater, mContext, this.uuId,this)
        var mNoOfColumns = Utils.calculateNoOfColumns(mContext,90F)
        if(!isPortrait()){
            mNoOfColumns = Utils.calculateNoOfColumns(mContext,160F)
        }
        val mLayoutManager: RecyclerView.LayoutManager = GridLayoutManager(mContext, mNoOfColumns)
        binding.recyclerView.addItemDecoration(GridSpacingItemDecoration(mNoOfColumns, 10, true))
        binding.recyclerView.layoutManager = mLayoutManager
        binding.recyclerView.itemAnimator = DefaultItemAnimator()
        binding.recyclerView.adapter = adapter
    }

    override fun onClickItem(position: Int) {
        this.currentPosition = position
        listener?.logoSelectedIndex(position,mList[position])
        this.index = mList[position]
        Utils.Log("TAG","index $position")
        reload()
    }

    private fun reload(){
        Utils.Log(TAG,"Index logo ${index?.toJson()}")
        mList = mList.mapIndexed { index, data ->
            if (data.enumIcon == this.index?.enumIcon) data.apply {
                isSelected = true
                currentPosition = index
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
        listener?.getData()?.let { mList.addAll(it) }
    }

    fun show(){
        Utils.Log("TAG","Show data ${mList.toJson()}")
        adapter.setDataSource(mList)
    }
    private fun load(){
        initRecycleView(mInflater)
        initializedData()
        reload()
    }

    interface ListenerLogoFragment {
        fun logoSelectedIndex(index : Int, selectedObject :LogoModel)
        fun logoSelectedIndex(index : Int,enumShape: EnumShape?, selectedObject :LogoModel)
        fun getData() : MutableList<LogoModel>
    }
}