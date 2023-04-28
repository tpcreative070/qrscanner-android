package tpcreative.co.qrscanner.ui.changedesign

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.recyclerview.widget.GridLayoutManager
import com.drakeet.multitype.MultiTypeAdapter
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.ConstantKey
import tpcreative.co.qrscanner.common.Navigator
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide
import tpcreative.co.qrscanner.common.extension.*
import tpcreative.co.qrscanner.common.view.crop.Crop
import tpcreative.co.qrscanner.databinding.ActivityNewChangeDesignBinding
import tpcreative.co.qrscanner.model.*
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


class NewChangeDesignActivity : BaseActivitySlide(){
    lateinit var viewModel: ChangeDesignViewModel
    lateinit var binding : ActivityNewChangeDesignBinding
    var items: java.util.ArrayList<java.io.Serializable> = ArrayList()
    var adapter = MultiTypeAdapter()
    private lateinit var selectedSetLogo: TreeSet<LogoModel>
    private lateinit var selectedSetColor: TreeSet<ColorModel>
    private lateinit var selectedSet: TreeSet<Int>
    private var previousPosition : Int = -1
    private var previousCancelPosition : Int = -1
    private lateinit var selectedPreviousSetLogo: TreeSet<LogoModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewChangeDesignBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
        if (savedInstanceState != null) {
            viewModel.index = savedInstanceState.getInt(ConstantKey.KEY_CHANGE_DESIGN_INDEX)
            viewModel.indexLogo = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_LOGO) ?: viewModel.defaultLogo()
            viewModel.changeDesignSave = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_SAVE) ?: viewModel.changeDesignSave
            viewModel.changeDesignReview = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_REVIEW) ?: viewModel.changeDesignReview
            val mUri = Uri.parse(savedInstanceState.getString(ConstantKey.KEY_CHANGE_DESIGN_URI))
            viewModel.shape =  EnumShape.valueOf(savedInstanceState.getString(ConstantKey.KEY_CHANGE_DESIGN_SHAPE) ?:EnumShape.ORIGINAL.name)
            viewModel.isOpenColorPicker = savedInstanceState.getBoolean(ConstantKey.KEY_CHANGE_DESIGN_COLOR_OPEN_PICKER)
            viewModel.enumType = EnumImage.valueOf(savedInstanceState.getString(ConstantKey.KEY_CHANGE_DESIGN_COLOR_INDEX) ?:EnumImage.NONE.name)
            viewModel.indexColor = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_COLOR) ?: viewModel.defaultColor()
            items = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_MULTIPLE_TYPE) ?: ArrayList()
            selectedPreviousSetLogo  = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_LOGO_PREVIOUS_CANCEL_SET_LOGO) ?: TreeSet()
            previousPosition = savedInstanceState.getInt(ConstantKey.KEY_CHANGE_DESIGN_LOGO_PREVIOUS_POSITION)
            previousCancelPosition = savedInstanceState.getInt(ConstantKey.KEY_CHANGE_DESIGN_LOGO_PREVIOUS_CANCEL_POSITION)
            viewModel.mapSetView  = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_COLOR_SELECTED_MAP) ?: TreeSet()
            Utils.Log(TAG,"State instance saveInstanceState has value")
            if (mUri.isExist){
                viewModel.uri = mUri
                val bitmap = viewModel.uri?.let {
                    Utils.Log(TAG,"value uri ${it}")
                    contentResolver.openInputStream(it).use { data ->
                        BitmapFactory.decodeStream(data)
                    }
                }
                viewModel.onUpdateBitmap(bitmap)
            }
        }

        val layoutManager = GridLayoutManager(this, SPAN_COUNT)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (items[position] is Category) SPAN_COUNT else 1
            }
        }
        selectedSetLogo = TreeSet()
        selectedSetColor = TreeSet()
        selectedPreviousSetLogo = TreeSet()
        selectedSet = TreeSet()

        binding.recyclerView.layoutManager = layoutManager
        adapter.register(CategoryHolderInflater())
        adapter.register(LogoSquareViewBinder(selectedSetLogo,this,object  : LogoSquareViewBinder.ItemSelectedListener{
            override fun onClickItem(position: Int) {
                Utils.Log(TAG,"Logo ${selectedSetLogo.toJson()}")
                Utils.Log(TAG,"Data list ${items.toJson()}")
                changeLogoItem(position)
            }
        }))

        adapter.register(ColorSquareViewBinder(selectedSetColor,this,object  : ColorSquareViewBinder.ItemSelectedListener{
            override fun onClickItem() {
                Utils.Log(TAG,"Color ${selectedSetColor.toJson()}")
                val mResultSelected = selectedSetColor.first()
                viewModel.enumType = mResultSelected.type
                popupForResult.launch(  Navigator.onPopupView(this@NewChangeDesignActivity,viewModel.indexColor.mapColor,viewModel.enumType,PopupColorActivity::class.java))
                overridePendingTransition(R.anim.slide_up,  R.anim.no_animation);
                mResult = {
                    Utils.Log(TAG,"Result value color $it")
                    viewModel.indexColor.mapColor[viewModel.enumType] = it
                    viewModel.mapSetView.add(EnumView.COLOR)
                    viewModel.selectedIndexOnReview()
                    viewModel.selectedIndexOnSave()
                    onGenerateQRReview()
                }
            }
        }))

        adapter.register(SquareViewBinder(selectedSet))
        if (items.isEmpty()){
            loadData()
        }else{
            adapter.items = items
        }
        binding.recyclerView.adapter = adapter
        onGenerateQRReview()
    }

    private fun loadData(){
        val spacialCategory1 = Category("1. Logo")
        items.add(spacialCategory1)
        items.addAll(viewModel.mLogoList)
        val spacialCategory2 = Category("2. Color")
        items.add(spacialCategory2)
        items.addAll(viewModel.mColorList)
        items.forEachIndexed { index, it ->
            if (it is LogoModel){
                if (it.enumIcon == viewModel.indexLogo.enumIcon){
                    previousPosition = index
                    it.isSelected = viewModel.indexLogo.isSelected
                    selectedSetLogo.add(it)
                    selectedPreviousSetLogo.add(it)
                    viewModel.mapSetView.add(EnumView.LOGO)
                }
            }
        }
        adapter.items = items
    }

    private fun changeLogoItem(position : Int){
        val mResult = items[position] as LogoModel
        mResult.isSelected = !mResult.isSelected
        adapter.notifyItemChanged(position)
        if (previousPosition>=0){
            val mPreviousResult = items[previousPosition] as LogoModel
            Utils.Log(TAG,"Previous data ${mPreviousResult.toJson()}")
            mPreviousResult.isSelected = !mPreviousResult.isSelected
            adapter.notifyItemChanged(previousPosition)
            previousCancelPosition = previousPosition
        }
        previousPosition = position
        val index = selectedSetLogo.firstOrNull()
        viewModel.mapSetView.add(EnumView.LOGO)
        viewModel.mapSetView
        viewModel.indexLogo  = index ?: viewModel.indexLogo

        if (index?.enumChangeDesignType ==EnumChangeDesignType.VIP){
            onGetGallery()
        }else{
            selectedPreviousSetLogo.clear()
            selectedPreviousSetLogo.addAll(selectedSetLogo)
            viewModel.selectedIndexOnReview()
            viewModel.selectedIndexOnSave()
            onGenerateQRReview()
        }
        Utils.Log(TAG,"IndexLogo ${viewModel.indexLogo.toJson()}")
    }

    private fun changeLogoItemCancel(position : Int){
        val mResult = items[position] as LogoModel
        mResult.isSelected = !mResult.isSelected
        adapter.notifyItemChanged(position)
        if (previousPosition>=0){
            val mPreviousResult = items[previousPosition] as LogoModel
            Utils.Log(TAG,"Previous data ${mPreviousResult.toJson()}")
            mPreviousResult.isSelected = !mPreviousResult.isSelected
            adapter.notifyItemChanged(previousPosition)
            previousCancelPosition = previousPosition
        }
        previousPosition = position
        val index = selectedSetLogo.firstOrNull()
        viewModel.mapSetView.add(EnumView.LOGO)
        viewModel.indexLogo  = index ?: viewModel.indexLogo
        selectedPreviousSetLogo.clear()
        selectedPreviousSetLogo.addAll(selectedSetLogo)
        viewModel.selectedIndexOnReview()
        viewModel.selectedIndexOnSave()
        onGenerateQRReview()
        Utils.Log(TAG,"IndexLogo ${viewModel.indexLogo.toJson()}")
    }

    fun changeLogoShapeItem(){
        viewModel.mapSetView.forEach {
            when(it){
                EnumView.LOGO ->{
                    when(viewModel.shape){
                        EnumShape.ORIGINAL ->{
                            viewModel.shape = EnumShape.SQUARE
                        }
                        EnumShape.SQUARE ->{
                            viewModel.shape = EnumShape.CIRCLE
                        }
                        EnumShape.CIRCLE ->{
                            viewModel.shape = EnumShape.ORIGINAL
                        }
                    }
                    viewModel.selectedIndexOnReview()
                    viewModel.selectedIndexOnSave()
                    onGenerateQRReview()

                }
                else -> {  Utils.Log(TAG,"Enum view response $it")}
            }
        }
    }

    fun onGenerateQRReview(){
        viewModel.onGenerateQR {mData->
            val mFile = viewModel.create.uuId?.findImageName(EnumImage.QR_CODE)
            if (mFile!=null && viewModel.bitmap == null && !viewModel.isChangedReview()){
                binding.imgQRCode.setImageURI(mFile.toUri())
                Utils.Log(TAG,"No change review data")
            }else{
                binding.imgQRCode.setImageDrawable(mData)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_change_design, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private val pickGalleryForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            Utils.Log(TAG, "REQUEST_PICK")
            beginCrop(result.data?.data)
        }else{
            Utils.Log(TAG,"Cancel ${selectedPreviousSetLogo.toJson()}")
            selectedSetLogo.clear()
            selectedSetLogo.addAll(selectedPreviousSetLogo)
            changeLogoItemCancel(previousCancelPosition)
        }
    }

    private fun beginCrop(source: Uri?) {
        val destination = Uri.fromFile(File(this.cacheDir, "cropped"))
        cropForResult.launch(Crop.of(source, destination)?.asSquare()?.start(this,true))
    }

    private val cropForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            Utils.Log(TAG, "REQUEST_CROP")
            handleCrop(result.resultCode, result.data)
        }else{
            Utils.Log(TAG,"Cancel ${selectedPreviousSetLogo.toJson()}")
            selectedSetLogo.clear()
            selectedSetLogo.addAll(selectedPreviousSetLogo)
            changeLogoItemCancel(previousCancelPosition)
        }
    }

    private fun handleCrop(resultCode: Int, result: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val mData: Uri? = Crop.getOutputUri(result)
            Utils.Log(TAG,"Result cropped ${mData.toString()}")
            val bitmap = mData?.let {
                viewModel.uri = mData
                contentResolver.openInputStream(it).use { data ->
                    BitmapFactory.decodeStream(data)
                }
            }
            viewModel.onUpdateBitmap(bitmap)
            viewModel.selectedIndexOnReview()
            viewModel.selectedIndexOnSave()
            onGenerateQRReview()
        }
    }

    private val popupForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
        }
    }

    private fun onGetGallery() {
        pickGalleryForResult.launch(Crop.getImagePicker())
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(ConstantKey.KEY_CHANGE_DESIGN_INDEX,viewModel.index)
        outState.putSerializable(ConstantKey.KEY_CHANGE_DESIGN_LOGO,viewModel.indexLogo)
        outState.putSerializable(ConstantKey.KEY_CHANGE_DESIGN_REVIEW,viewModel.changeDesignSave)
        outState.putSerializable(ConstantKey.KEY_CHANGE_DESIGN_REVIEW,viewModel.changeDesignReview)
        outState.putBoolean(ConstantKey.KEY_CHANGE_DESIGN_COLOR_OPEN_PICKER,viewModel.isOpenColorPicker)
        outState.putString(ConstantKey.KEY_CHANGE_DESIGN_COLOR_INDEX,viewModel.enumType.name)
        outState.putString(ConstantKey.KEY_CHANGE_DESIGN_URI,"${viewModel.uri}")
        outState.putString(ConstantKey.KEY_CHANGE_DESIGN_SHAPE,viewModel.shape.name)
        outState.putSerializable(ConstantKey.KEY_CHANGE_DESIGN_COLOR,viewModel.indexColor)
        outState.putSerializable(ConstantKey.KEY_CHANGE_DESIGN_MULTIPLE_TYPE,items)
        outState.putSerializable(ConstantKey.KEY_CHANGE_DESIGN_LOGO_PREVIOUS_CANCEL_SET_LOGO,selectedPreviousSetLogo)
        outState.putInt(ConstantKey.KEY_CHANGE_DESIGN_LOGO_PREVIOUS_POSITION,previousPosition)
        outState.putInt(ConstantKey.KEY_CHANGE_DESIGN_LOGO_PREVIOUS_CANCEL_POSITION,previousCancelPosition)
        outState.putSerializable(ConstantKey.KEY_CHANGE_DESIGN_COLOR_SELECTED_MAP,viewModel.mapSetView)
        Utils.Log(TAG,"State instance save ${viewModel.indexLogo.toJson()}")
        Utils.Log(TAG,"State instance save index ${viewModel.index}")
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        viewModel.index = savedInstanceState.getInt(ConstantKey.KEY_CHANGE_DESIGN_INDEX)
        viewModel.indexLogo = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_LOGO) ?: viewModel.defaultLogo()
        viewModel.changeDesignSave = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_SAVE) ?: viewModel.changeDesignSave
        viewModel.changeDesignReview = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_REVIEW) ?: viewModel.changeDesignReview
        viewModel.isOpenColorPicker = savedInstanceState.getBoolean(ConstantKey.KEY_CHANGE_DESIGN_COLOR_OPEN_PICKER)
        viewModel.enumType = EnumImage.valueOf(savedInstanceState.getString(ConstantKey.KEY_CHANGE_DESIGN_COLOR_INDEX) ?:EnumImage.NONE.name)
        viewModel.indexColor = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_COLOR) ?: viewModel.defaultColor()
        val mUri = Uri.parse(savedInstanceState.getString(ConstantKey.KEY_CHANGE_DESIGN_URI))
        if (mUri.isExist){
            viewModel.uri = mUri
        }
        viewModel.shape =  EnumShape.valueOf(savedInstanceState.getString(ConstantKey.KEY_CHANGE_DESIGN_SHAPE) ?:EnumShape.ORIGINAL.name)
        items = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_MULTIPLE_TYPE) ?: ArrayList()
        selectedPreviousSetLogo  = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_LOGO_PREVIOUS_CANCEL_SET_LOGO) ?: TreeSet()
        previousPosition = savedInstanceState.getInt(ConstantKey.KEY_CHANGE_DESIGN_LOGO_PREVIOUS_POSITION)
        previousCancelPosition = savedInstanceState.getInt(ConstantKey.KEY_CHANGE_DESIGN_LOGO_PREVIOUS_CANCEL_POSITION)
        viewModel.mapSetView  = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_COLOR_SELECTED_MAP) ?: TreeSet()
        Utils.Log(TAG,"State instance restore ${viewModel.indexLogo.toJson()}")
        Utils.Log(TAG,"State instance restore index ${viewModel.index}")
        super.onRestoreInstanceState(savedInstanceState)
    }

    companion object {
        private const val SPAN_COUNT = 5
        var mResult : ((value : String) -> Unit?)? = null
    }
}