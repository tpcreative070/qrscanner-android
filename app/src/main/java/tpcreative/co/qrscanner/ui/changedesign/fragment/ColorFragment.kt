package tpcreative.co.qrscanner.ui.changedesign.fragment

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import tpcreative.co.qrscanner.common.BaseFragment
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.view.GridSpacingItemDecoration
import tpcreative.co.qrscanner.databinding.FragmentColorBinding
import tpcreative.co.qrscanner.model.ColorModel
import android.content.DialogInterface
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.panshen.gridcolorpicker.builder.colorPickerDialog
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.extension.*
import tpcreative.co.qrscanner.model.EnumImage

class ColorFragment : BaseFragment(),ColorFragmentAdapter.ItemSelectedListener {
    private lateinit var binding : FragmentColorBinding
    private lateinit var mContext : Context
    private lateinit var adapter: ColorFragmentAdapter
    private lateinit var mList : MutableList<ColorModel>
    private lateinit var mInflater: LayoutInflater
    private var listener : ListenerColorFragment? = null
    private var uuId : String = ""
    private var dialog: AlertDialog? = null
    private var isOpenColorPicker : Boolean = false
    private var enumType : EnumImage = EnumImage.NONE
    private var mapColor : HashMap<EnumImage,String> = HashMap()

    override fun getLayoutId(): Int {
        return 0
    }

    override fun getLayoutId(inflater: LayoutInflater?, viewGroup: ViewGroup?): View {
        binding = FragmentColorBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        show()
    }

    override fun onPause() {
        super.onPause()
        onClearDialog()
    }

    override fun work() {
        super.work()
        mContext = requireContext()
        mInflater = requireActivity().layoutInflater
        load()
        Utils.Log(TAG,"Current color isOpenColorPicker  $isOpenColorPicker")
        if (isOpenColorPicker){
            val mData = mapColor[enumType]
            mData?.putChangedDesignColor
            showDialog()
        }
    }

    fun setBinding(listenerView: ListenerColorFragment){
        this.listener = listenerView
    }
    fun setSelectedIndex(mapColor : HashMap<EnumImage,String>,isOpenColorPicker : Boolean,enumImage: EnumImage){
        this.isOpenColorPicker = isOpenColorPicker
        this.enumType = enumImage
        this.mapColor = mapColor
    }

    private fun initRecycleView(layoutInflater: LayoutInflater) {
        adapter = ColorFragmentAdapter(layoutInflater, mContext, this.uuId,this)
        var mNoOfColumns = Utils.calculateNoOfColumns(mContext,100F)
        if(!isPortrait()){
            mNoOfColumns = Utils.calculateNoOfColumns(mContext,170F)
        }
        val mLayoutManager: RecyclerView.LayoutManager = GridLayoutManager(mContext, mNoOfColumns)
        binding.recyclerView.addItemDecoration(GridSpacingItemDecoration(mNoOfColumns, 10, true))
        binding.recyclerView.layoutManager = mLayoutManager
        binding.recyclerView.itemAnimator = DefaultItemAnimator()
        binding.recyclerView.adapter = adapter
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
    }

    override fun onClickItem(position: Int) {
        val mObject = mList[position]
        listener?.colorSelectedIndex(position,mObject)
        if (enumType!=mObject.type){
            enumType = mObject.type
            dialog?.dismiss()
            dialog = null
        }
        showDialog()
    }

    private fun onClearDialog(){
        dialog?.dismiss()
        dialog = null
    }

    private fun showDialog() {
        if (dialog == null) {
            dialog = colorPickerDialog {
                cancelable = false
                positiveButtonText = resources.getString(R.string.confirm)
                negativeButtonText = resources.getString(R.string.cancel)
                onPositiveButtonClickListener = DialogInterface.OnClickListener { _, _ ->
//                    Toast.makeText(requireContext(), "Click Positive Button", Toast.LENGTH_SHORT)
//                        .show()
                    listener?.onOpenColorPicker(false)
                    listener?.onAction(true)
                }
                onNegativeButtonClickListener = DialogInterface.OnClickListener { _, _ ->
//                    Toast.makeText(requireContext(), "Click Negative Button", Toast.LENGTH_SHORT)
//                        .show()
                    listener?.onOpenColorPicker(false)
                    listener?.onAction(false)
                }
                dismissListener = DialogInterface.OnDismissListener {
                    //Toast.makeText(requireContext(), "Dismiss", Toast.LENGTH_SHORT).show()
                }

                val mCurrentColor = mapColor[enumType]
                Utils.Log(TAG,"Current color  $enumType $mCurrentColor")
                colorPicker {
                    colorScheme =
                        arrayListOf(R.color.C_007c91,R.color.C_029FD6, R.color.C_0062FD, R.color.C_5023B1, R.color.C_962BB9, R.color.C_BA2C5E,R.color.C_EC407A, R.color.C_FB431B, R.color.C_FD6802, R.color.C_FFAB03, R.color.C_FFCB05, R.color.C_FFFD46, R.color.C_D8EB39, R.color.C_76BC40,R.color.C_818d00)
                    row = 10
                    checkedColor = mCurrentColor
                    selectorColorRes = R.color.colorAccent
                    showAlphaView = true
                    showAlphaViewLabel = true
                    alphaViewLabelText = resources.getString(R.string.opacity)
                    alphaViewLabelColorRes = R.color.colorAccent

                    onColorChanged = { color ->
                        onColorChanged(color)
                    }
                    afterColorChanged = { color ->
                        afterColorChanged(color)
                    }
                }
            }.show(requireContext())
            listener?.onOpenColorPicker(true)
        } else {
            listener?.onOpenColorPicker(true)
            dialog?.show()
        }
    }

    private fun onColorChanged(color: String) {
         Utils.Log(TAG, color)
         Utils.Log(TAG,  "onColorChanged() - ${color.uppercase()}")
    }

    private fun afterColorChanged(color: String) {
        color.putChangedDesignColor
        listener?.onColorChanged(color)
        Utils.Log(TAG, color)
        Utils.Log(TAG,
            "afterColorChanged() - ${color.uppercase()}")
        Utils.Log(TAG,"Current color after changed  $enumType $color")
    }

    interface ListenerColorFragment {
        fun colorSelectedIndex(index : Int, selectedObject : ColorModel)
        fun getData() : MutableList<ColorModel>
        fun onColorChanged(color : String)
        fun onOpenColorPicker(isOpen : Boolean)
        fun onAction(isPositive : Boolean)
    }
}