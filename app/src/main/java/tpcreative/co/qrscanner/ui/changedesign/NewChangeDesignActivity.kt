package tpcreative.co.qrscanner.ui.changedesign

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.drakeet.multitype.MultiTypeAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.ConstantKey
import tpcreative.co.qrscanner.common.Navigator
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide
import tpcreative.co.qrscanner.common.extension.*
import tpcreative.co.qrscanner.common.view.crop.Crop
import tpcreative.co.qrscanner.databinding.ActivityNewChangeDesignBinding
import tpcreative.co.qrscanner.model.*
import tpcreative.co.qrscanner.ui.changedesigntext.ChangeDesignTextActivity
import tpcreative.co.qrscanner.ui.premiumpopup.PremiumPopupActivity
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class NewChangeDesignActivity : BaseActivitySlide(){
    lateinit var viewModel: ChangeDesignViewModel
    lateinit var binding : ActivityNewChangeDesignBinding
    var items: java.util.ArrayList<java.io.Serializable> = ArrayList()
    var adapter = MultiTypeAdapter()
    private lateinit var selectedSetLogo: TreeSet<LogoModel>
    private lateinit var selectedSetColor: TreeSet<ColorModel>
    private lateinit var selectedSetPositionMarker: TreeSet<PositionMarkerModel>
    private lateinit var selectedSetBody: TreeSet<BodyModel>
    private lateinit var selectedSetText : TreeSet<TextModel>
    private var previousLogoPosition : Int = -1
    private var previousLogoCancelPosition : Int = -1
    private var previousPositionMarkerPosition : Int = -1
    private var previousPositionMarkerCancelPosition : Int = -1
    private var previousBodyPosition : Int = -1
    private var previousBodyCancelPosition : Int = -1
    private lateinit var selectedPreviousSetLogo: TreeSet<LogoModel>
    private lateinit var selectedPreviousSetPositionMarker: TreeSet<PositionMarkerModel>
    private lateinit var selectedPreviousSetBody: TreeSet<BodyModel>
    private var isNavigation : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewChangeDesignBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
        if (savedInstanceState != null) {
            viewModel.index = savedInstanceState.getInt(ConstantKey.KEY_CHANGE_DESIGN_INDEX)
            viewModel.indexLogo = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_LOGO) ?: viewModel.defaultLogo()
            viewModel.indexPositionMarker = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_POSITION_MARKER) ?: viewModel.defaultPositionMarker()
            viewModel.indexBody = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_BODY) ?: viewModel.defaultBody()
            viewModel.indexText = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_TEXT) ?: viewModel.defaultText()
            viewModel.changeDesignSave = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_SAVE) ?: viewModel.changeDesignSave
            viewModel.changeDesignReview = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_REVIEW) ?: viewModel.changeDesignReview
            val mUri = Uri.parse(savedInstanceState.getString(ConstantKey.KEY_CHANGE_DESIGN_URI))
            viewModel.shape =  EnumShape.valueOf(savedInstanceState.getString(ConstantKey.KEY_CHANGE_DESIGN_SHAPE) ?:EnumShape.ORIGINAL.name)
            viewModel.isOpenColorPicker = savedInstanceState.getBoolean(ConstantKey.KEY_CHANGE_DESIGN_COLOR_OPEN_PICKER)
            viewModel.enumType = EnumImage.valueOf(savedInstanceState.getString(ConstantKey.KEY_CHANGE_DESIGN_COLOR_INDEX) ?:EnumImage.NONE.name)
            viewModel.indexColor = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_COLOR) ?: viewModel.defaultColor()
            items = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_MULTIPLE_TYPE) ?: ArrayList()
            selectedPreviousSetLogo  = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_LOGO_PREVIOUS_SELECTED) ?: TreeSet()
            selectedPreviousSetPositionMarker  = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_POSITION_MARKER_PREVIOUS_SELECTED) ?: TreeSet()
            selectedPreviousSetBody = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_BODY_PREVIOUS_SELECTED) ?: TreeSet()
            previousLogoPosition = savedInstanceState.getInt(ConstantKey.KEY_CHANGE_DESIGN_LOGO_PREVIOUS_POSITION)
            previousLogoCancelPosition = savedInstanceState.getInt(ConstantKey.KEY_CHANGE_DESIGN_LOGO_PREVIOUS_CANCEL_POSITION)
            previousPositionMarkerPosition = savedInstanceState.getInt(ConstantKey.KEY_CHANGE_DESIGN_POSITION_MARKER_PREVIOUS_POSITION)
            previousPositionMarkerCancelPosition = savedInstanceState.getInt(ConstantKey.KEY_CHANGE_DESIGN_POSITION_MARKER_PREVIOUS_CANCEL_POSITION)
            previousBodyPosition = savedInstanceState.getInt(ConstantKey.KEY_CHANGE_DESIGN_BODY_PREVIOUS_POSITION)
            previousBodyCancelPosition = savedInstanceState.getInt(ConstantKey.KEY_CHANGE_DESIGN_BODY_PREVIOUS_CANCEL_POSITION)
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
        selectedSetText = TreeSet()
        selectedSetPositionMarker = TreeSet()
        selectedSetBody = TreeSet()
        selectedPreviousSetLogo = TreeSet()
        selectedPreviousSetPositionMarker = TreeSet()
        selectedPreviousSetBody = TreeSet()

        binding.recyclerView.layoutManager = layoutManager
        adapter.register(CategoryHolderInflater())

        /*Logo register*/
        adapter.register(LogoSquareViewBinder(selectedSetLogo,this,object  : LogoSquareViewBinder.ItemSelectedListener{
            override fun onClickItem(position: Int) {
                if (isNavigation){
                    return
                }
                isNavigation = true
                Utils.Log(TAG,"Clicked position $position")
                Utils.Log(TAG,"Logo ${selectedSetLogo.toJson()}")
                Utils.Log(TAG,"Data list ${items.toJson()}")
                val mData = selectedSetLogo.firstOrNull()
                if (mData?.enumChangeDesignType == EnumChangeDesignType.VIP && !Utils.isPremium()){
                    premiumPopupForResult.launch(Navigator.onPremiumPopupView(this@NewChangeDesignActivity,viewModel.getChangeDataReviewToPremiumPopup(mData),viewModel.shape,PremiumPopupActivity::class.java,))
                }else{
                    changeLogoItem(position)
                }
            }
        }))

        /*Color register*/
        adapter.register(ColorSquareViewBinder(selectedSetColor,this,object  : ColorSquareViewBinder.ItemSelectedListener{
            override fun onClickItem(position : Int) {
                if (isNavigation){
                    return
                }
                isNavigation = true
                Utils.Log(TAG,"Color ${selectedSetColor.toJson()}")
                Utils.Log(TAG,"Color current ${viewModel.indexColor.toJson()}")
                Utils.Log(TAG,"Clicked position $position")
                val mResultSelected = selectedSetColor.first()
                if (viewModel.isAllowNavigation(mResultSelected)){
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
            }
        }))

        /*Text register*/
        adapter.register(TextSquareViewBinder(selectedSetText,this,object :TextSquareViewBinder.ItemSelectedListener{
            override fun onClickItem(position: Int) {
                if (isNavigation){
                    return
                }
                isNavigation = true
                val mapColor = viewModel.indexColor.mapColor
                Utils.Log(TAG,"Index text ${viewModel.indexText.toJson()}")
                textForResult.launch(Navigator.onChangeDesignText(this@NewChangeDesignActivity,
                    ChangeDesignTextActivity::class.java,selectedSetText.firstOrNull()?.type ?: EnumImage.QR_TEXT_BOTTOM,mapColor,viewModel.indexText,viewModel.changeDesignReview))
                overridePendingTransition(R.anim.slide_up,  R.anim.no_animation);
            }
        }))

        mRequestBitmap = {
            ChangeDesignTextActivity.mRequestBitmap?.invoke()
        }

        /*Position marker register*/
        adapter.register(PositionMarkerSquareViewBinder(selectedSetPositionMarker,this,object :PositionMarkerSquareViewBinder.ItemSelectedListener{
            override fun onClickItem(position: Int) {
                if (isNavigation){
                    return
                }
                isNavigation = true
                val mData = selectedSetPositionMarker.firstOrNull()
                if (mData?.enumChangeDesignType == EnumChangeDesignType.VIP && !Utils.isPremium()){
                    premiumPopupForResult.launch(Navigator.onPremiumPopupView(this@NewChangeDesignActivity,viewModel.getChangeDataReviewToPremiumPopup(mData),viewModel.shape,PremiumPopupActivity::class.java,))
                }else{
                    changePositionMarkerItem(position)
                }
            }
        }))

        /*Body register*/
        adapter.register(BodySquareViewBinder(selectedSetBody,this,object :BodySquareViewBinder.ItemSelectedListener{
            override fun onClickItem(position: Int) {
                if (isNavigation){
                    return
                }
                isNavigation = true
                val mData = selectedSetBody.firstOrNull()
                if (mData?.enumChangeDesignType == EnumChangeDesignType.VIP && !Utils.isPremium()){
                    premiumPopupForResult.launch(Navigator.onPremiumPopupView(this@NewChangeDesignActivity,viewModel.getChangeDataReviewToPremiumPopup(mData),viewModel.shape,PremiumPopupActivity::class.java,))
                }else{
                    changeBodyItem(position)
                }
            }
        }))

        mResultTemplate  = {
            Utils.Log(TAG,"Call back from Template activity ${it.toJson()}")
            viewModel.callbackTemplate(it){
                onCallbackTemplate()
                onGenerateQRReview()
            }
        }

        mResultText = {
            handleText(it)
        }

        mResultProgressing = {
            binding.imgQRCode.setImageBitmap(it)
        }

        if (items.isEmpty()){
            loadData()
        }else{
            adapter.items = items
        }
        binding.recyclerView.adapter = adapter
        onGenerateQRReview()
    }

    private fun  onUpdateColorUI(selected : LogoModel?){
        items.forEachIndexed { index, serializable ->
            if (serializable is ColorModel){
                if (serializable.type == EnumImage.QR_BACKGROUND_ICON){
                    serializable.isSelected = !(selected?.isSupportedBGColor ?: false)
                    adapter.notifyItemChanged(index)
                }
                if (serializable.type == EnumImage.QR_FOREGROUND_ICON){
                    serializable.isSelected = !(selected?.isSupportedFGColor ?: false)
                    adapter.notifyItemChanged(index)
                }
            }
        }
    }

    private fun handleText(data : HashMap<EnumImage,TextModel>){
        viewModel.mapSetView.add(EnumView.TEXT)
        viewModel.indexText = data
        viewModel.selectedIndexOnReview()
        viewModel.selectedIndexOnSave()
        Utils.Log(TAG,"Handle text ${data.toJson()}")
        //onGenerateQRReview()
    }

    private fun loadData(){
        val spacialCategory1 = Category("1. ${getString(R.string.logo)}")
        items.add(spacialCategory1)
        items.addAll(viewModel.mLogoList)
        val spacialCategory2 = Category("2. ${getString(R.string.color)}")
        items.add(spacialCategory2)
        items.addAll(viewModel.mColorList)
        val spacialCategory3 = Category("3. ${getString(R.string.text)}")
        items.add(spacialCategory3)
        items.addAll(viewModel.mTextList)
        val spacialCategory4 = Category("4. ${getString(R.string.position_marker)}")
        items.add(spacialCategory4)
        items.addAll(viewModel.mPositionMarkerList)
        val spacialCategory5 = Category("5. ${getString(R.string.body)}")
        items.add(spacialCategory5)
        items.addAll(viewModel.mBodyList)
        items.forEachIndexed { index, it ->
            if (it is LogoModel){
                if (it.enumIcon == viewModel.indexLogo.enumIcon){
                    previousLogoPosition = index
                    it.isSelected = viewModel.indexLogo.isSelected
                    selectedSetLogo.add(it)
                    selectedPreviousSetLogo.add(it)
                    viewModel.mapSetView.add(EnumView.LOGO)
                    onUpdateColorUI(it)
                }
            }
            else if (it is PositionMarkerModel){
                if (it.enumPositionMarker == viewModel.indexPositionMarker.enumPositionMarker){
                    previousPositionMarkerPosition = index
                    it.isSelected = viewModel.indexPositionMarker.isSelected
                    selectedSetPositionMarker.add(it)
                    selectedPreviousSetPositionMarker.add(it)
                    viewModel.mapSetView.add(EnumView.POSITION_MARKER)
                }
            }
            else if (it is BodyModel){
                if (it.enumBody == viewModel.indexBody.enumBody){
                    previousBodyPosition = index
                    it.isSelected = viewModel.indexBody.isSelected
                    selectedSetBody.add(it)
                    selectedPreviousSetBody.add(it)
                    viewModel.mapSetView.add(EnumView.BODY)
                }
            }
        }
        adapter.items = items
    }

    private fun onCallbackTemplate(){
        selectedSetLogo.clear()
        selectedPreviousSetLogo.clear()
        selectedSetPositionMarker.clear()
        selectedPreviousSetPositionMarker.clear()
        selectedSetBody.clear()
        selectedPreviousSetBody.clear()
        items.forEachIndexed { index, it ->
            if (it is LogoModel){
                if (it.enumIcon == viewModel.indexLogo.enumIcon){
                    previousLogoPosition = index
                    selectedSetLogo.add(it)
                    selectedPreviousSetLogo.add(it)
                    viewModel.mapSetView.add(EnumView.LOGO)
                    it.isSelected = viewModel.indexLogo.enumIcon == it.enumIcon
                    onUpdateColorUI(it)
                }else{
                    it.isSelected = false
                }

                Utils.Log(TAG,"Selected logo result ${viewModel.indexLogo.enumIcon}: ${it.isSelected}")
            }
            else if (it is PositionMarkerModel){
                if (it.enumPositionMarker == viewModel.indexPositionMarker.enumPositionMarker){
                    previousPositionMarkerPosition = index
                    selectedSetPositionMarker.add(it)
                    selectedPreviousSetPositionMarker.add(it)
                    viewModel.mapSetView.add(EnumView.POSITION_MARKER)
                    it.isSelected = viewModel.indexPositionMarker.enumIcon == it.enumIcon
                }else{
                    it.isSelected = false
                }
            }
            else if (it is BodyModel){
                if (it.enumBody == viewModel.indexBody.enumBody){
                    previousBodyPosition = index
                    selectedSetBody.add(it)
                    selectedPreviousSetBody.add(it)
                    viewModel.mapSetView.add(EnumView.BODY)
                    it.isSelected = viewModel.indexBody.enumIcon == it.enumIcon
                }else{
                    it.isSelected = false
                }
            }
        }
        adapter.items = items
        adapter.notifyDataSetChanged()
        Utils.Log(TAG,"Selected logo ${selectedSetLogo.toJson()}")
    }

    private fun changeLogoItem(position : Int){
        val mResult = items[position] as LogoModel
        mResult.isSelected = !mResult.isSelected
        adapter.notifyItemChanged(position)
        if (previousLogoPosition>=0){
            val mPreviousResult = items[previousLogoPosition] as LogoModel
            Utils.Log(TAG,"Previous data ${mPreviousResult.toJson()}")
            mPreviousResult.isSelected = !mPreviousResult.isSelected
            adapter.notifyItemChanged(previousLogoPosition)
            previousLogoCancelPosition = previousLogoPosition
        }
        previousLogoPosition = position
        val index = selectedSetLogo.firstOrNull()
        onUpdateColorUI(index)
        viewModel.mapSetView.add(EnumView.LOGO)
        viewModel.indexLogo  = index ?: viewModel.indexLogo

        if (index?.typeIcon == EnumTypeIcon.BITMAP){
            onGetGallery()
        }else{
            selectedPreviousSetLogo.clear()
            selectedPreviousSetLogo.addAll(selectedSetLogo)
            viewModel.selectedIndexOnReview()
            viewModel.selectedIndexOnSave()
            onGenerateQRReview()
        }
        isNavigation = false
    }

    private fun changeLogoItemCancel(position : Int){
        val mResult = items[position] as LogoModel
        mResult.isSelected = !mResult.isSelected
        adapter.notifyItemChanged(position)
        if (previousLogoPosition>=0){
            val mPreviousResult = items[previousLogoPosition] as LogoModel
            Utils.Log(TAG,"Previous data ${mPreviousResult.toJson()}")
            mPreviousResult.isSelected = !mPreviousResult.isSelected
            adapter.notifyItemChanged(previousLogoPosition)
            previousLogoCancelPosition = previousLogoPosition
        }
        previousLogoPosition = position
        val index = selectedSetLogo.firstOrNull()
        onUpdateColorUI(index)
        viewModel.mapSetView.add(EnumView.LOGO)
        viewModel.indexLogo  = index ?: viewModel.indexLogo
        selectedPreviousSetLogo.clear()
        selectedPreviousSetLogo.addAll(selectedSetLogo)
        viewModel.selectedIndexOnReview()
        viewModel.selectedIndexOnSave()
        onGenerateQRReview()
        isNavigation = false
    }

    private fun changePositionMarkerItem(position : Int){
        val mResult = items[position] as PositionMarkerModel
        mResult.isSelected = !mResult.isSelected
        adapter.notifyItemChanged(position)
        if (previousPositionMarkerPosition>=0){
            val mPreviousResult = items[previousPositionMarkerPosition] as PositionMarkerModel
            Utils.Log(TAG,"Previous data ${mPreviousResult.toJson()}")
            mPreviousResult.isSelected = !mPreviousResult.isSelected
            adapter.notifyItemChanged(previousPositionMarkerPosition)
            previousPositionMarkerCancelPosition = previousPositionMarkerPosition
        }
        previousPositionMarkerPosition = position
        val index = selectedSetPositionMarker.firstOrNull()
        viewModel.mapSetView.add(EnumView.POSITION_MARKER)
        viewModel.indexPositionMarker  = index ?: viewModel.indexPositionMarker

        selectedPreviousSetPositionMarker.clear()
        selectedPreviousSetPositionMarker.addAll(selectedSetPositionMarker)
        viewModel.selectedIndexOnReview()
        viewModel.selectedIndexOnSave()
        onGenerateQRReview()
        isNavigation = false
    }

    private fun changeBodyItem(position : Int){
        val mResult = items[position] as BodyModel
        mResult.isSelected = !mResult.isSelected
        adapter.notifyItemChanged(position)
        if (previousBodyPosition>=0){
            val mPreviousResult = items[previousBodyPosition] as BodyModel
            Utils.Log(TAG,"Previous data ${mPreviousResult.toJson()}")
            mPreviousResult.isSelected = !mPreviousResult.isSelected
            adapter.notifyItemChanged(previousBodyPosition)
            previousBodyCancelPosition = previousBodyPosition
        }
        previousBodyPosition = position
        val index = selectedSetBody.firstOrNull()
        viewModel.mapSetView.add(EnumView.BODY)
        viewModel.indexBody  = index ?: viewModel.indexBody

        selectedPreviousSetBody.clear()
        selectedPreviousSetBody.addAll(selectedSetBody)
        viewModel.selectedIndexOnReview()
        viewModel.selectedIndexOnSave()
        onGenerateQRReview()
        isNavigation = false
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
        isNavigation = false
    }

    fun onGenerateQRReview(){
        viewModel.onGenerateQR {mData->
            val mFile = viewModel.create.uuId?.findImageName(EnumImage.QR_CODE)
            if (mFile!=null && viewModel.bitmap == null && !viewModel.isChangedReview()){
                binding.imgQRCode.setImageURI(mFile.toUri())
                Utils.Log(TAG,"No change review data")
            }else{
                onDraw(mData)
            }
        }
    }

    private fun onDraw(mDrawable : Drawable){
        val mBitmap = mDrawable.toBitmap(1024,1024)
        lifecycleScope.launch(Dispatchers.Main){
            mBitmap.onDrawOnBitmap(viewModel.indexText) {
                lifecycleScope.launch(Dispatchers.Main){
                    binding.imgQRCode.setImageBitmap(it)
                }
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
            changeLogoItemCancel(previousLogoCancelPosition)
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
            changeLogoItemCancel(previousLogoCancelPosition)
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

    private val textForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
        }
    }


    private val premiumPopupForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
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
        outState.putSerializable(ConstantKey.KEY_CHANGE_DESIGN_POSITION_MARKER,viewModel.indexPositionMarker)
        outState.putSerializable(ConstantKey.KEY_CHANGE_DESIGN_BODY,viewModel.indexBody)
        outState.putSerializable(ConstantKey.KEY_CHANGE_DESIGN_TEXT,viewModel.indexText)
        outState.putSerializable(ConstantKey.KEY_CHANGE_DESIGN_REVIEW,viewModel.changeDesignSave)
        outState.putSerializable(ConstantKey.KEY_CHANGE_DESIGN_REVIEW,viewModel.changeDesignReview)
        outState.putBoolean(ConstantKey.KEY_CHANGE_DESIGN_COLOR_OPEN_PICKER,viewModel.isOpenColorPicker)
        outState.putString(ConstantKey.KEY_CHANGE_DESIGN_COLOR_INDEX,viewModel.enumType.name)
        outState.putString(ConstantKey.KEY_CHANGE_DESIGN_URI,"${viewModel.uri}")
        outState.putString(ConstantKey.KEY_CHANGE_DESIGN_SHAPE,viewModel.shape.name)
        outState.putSerializable(ConstantKey.KEY_CHANGE_DESIGN_COLOR,viewModel.indexColor)
        outState.putSerializable(ConstantKey.KEY_CHANGE_DESIGN_MULTIPLE_TYPE,items)
        outState.putSerializable(ConstantKey.KEY_CHANGE_DESIGN_LOGO_PREVIOUS_SELECTED,selectedPreviousSetLogo)
        outState.putSerializable(ConstantKey.KEY_CHANGE_DESIGN_POSITION_MARKER_PREVIOUS_SELECTED,selectedPreviousSetPositionMarker)
        outState.putSerializable(ConstantKey.KEY_CHANGE_DESIGN_BODY_PREVIOUS_SELECTED,selectedPreviousSetBody)
        outState.putInt(ConstantKey.KEY_CHANGE_DESIGN_LOGO_PREVIOUS_POSITION,previousLogoPosition)
        outState.putInt(ConstantKey.KEY_CHANGE_DESIGN_LOGO_PREVIOUS_CANCEL_POSITION,previousLogoCancelPosition)
        outState.putInt(ConstantKey.KEY_CHANGE_DESIGN_POSITION_MARKER_PREVIOUS_POSITION,previousPositionMarkerPosition)
        outState.putInt(ConstantKey.KEY_CHANGE_DESIGN_POSITION_MARKER_PREVIOUS_CANCEL_POSITION,previousPositionMarkerCancelPosition)
        outState.putInt(ConstantKey.KEY_CHANGE_DESIGN_BODY_PREVIOUS_POSITION,previousBodyPosition)
        outState.putInt(ConstantKey.KEY_CHANGE_DESIGN_BODY_PREVIOUS_CANCEL_POSITION,previousBodyCancelPosition)
        outState.putSerializable(ConstantKey.KEY_CHANGE_DESIGN_COLOR_SELECTED_MAP,viewModel.mapSetView)
        Utils.Log(TAG,"State instance save ${viewModel.indexLogo.toJson()}")
        Utils.Log(TAG,"State instance save index ${viewModel.index}")
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        viewModel.index = savedInstanceState.getInt(ConstantKey.KEY_CHANGE_DESIGN_INDEX)
        viewModel.indexLogo = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_LOGO) ?: viewModel.defaultLogo()
        viewModel.indexPositionMarker = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_POSITION_MARKER) ?: viewModel.defaultPositionMarker()
        viewModel.indexBody = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_BODY) ?: viewModel.defaultBody()
        viewModel.indexText = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_TEXT) ?: viewModel.defaultText()
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
        selectedPreviousSetLogo  = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_LOGO_PREVIOUS_SELECTED) ?: TreeSet()
        selectedPreviousSetPositionMarker  = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_POSITION_MARKER_PREVIOUS_SELECTED) ?: TreeSet()
        selectedPreviousSetBody = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_BODY_PREVIOUS_SELECTED) ?: TreeSet()
        previousLogoPosition = savedInstanceState.getInt(ConstantKey.KEY_CHANGE_DESIGN_LOGO_PREVIOUS_POSITION)
        previousLogoCancelPosition = savedInstanceState.getInt(ConstantKey.KEY_CHANGE_DESIGN_LOGO_PREVIOUS_CANCEL_POSITION)
        previousPositionMarkerPosition = savedInstanceState.getInt(ConstantKey.KEY_CHANGE_DESIGN_POSITION_MARKER_PREVIOUS_POSITION)
        previousPositionMarkerCancelPosition = savedInstanceState.getInt(ConstantKey.KEY_CHANGE_DESIGN_POSITION_MARKER_PREVIOUS_CANCEL_POSITION)
        previousBodyPosition = savedInstanceState.getInt(ConstantKey.KEY_CHANGE_DESIGN_BODY_PREVIOUS_POSITION)
        previousBodyCancelPosition = savedInstanceState.getInt(ConstantKey.KEY_CHANGE_DESIGN_BODY_PREVIOUS_CANCEL_POSITION)
        viewModel.mapSetView  = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_COLOR_SELECTED_MAP) ?: TreeSet()
        Utils.Log(TAG,"State instance restore ${viewModel.indexLogo.toJson()}")
        Utils.Log(TAG,"State instance restore index ${viewModel.index}")
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        isNavigation = false
    }

    companion object {
        private const val SPAN_COUNT = 5
        var mResult : ((value : String) -> Unit?)? = null
        var mResultTemplate : ((value : TemplateModel) ->Unit?)? = null
        var mResultText : ((value : HashMap<EnumImage,TextModel>) -> Unit?)? = null
        var mRequestBitmap : (() -> Unit?)? = null
        var mResultProgressing : ((Bitmap?)->Unit?)? = null

    }
}