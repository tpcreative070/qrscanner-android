package tpcreative.co.qrscanner.ui.changedesigntext

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.toColorInt
import androidx.lifecycle.lifecycleScope
import com.afollestad.materialdialogs.MaterialDialog
import com.davidmiguel.dragtoclose.DragListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.extension.*
import tpcreative.co.qrscanner.common.view.CircleImageView
import tpcreative.co.qrscanner.common.view.showcase.BubbleShowCase
import tpcreative.co.qrscanner.databinding.ActivityChangeDesignTextBinding
import tpcreative.co.qrscanner.model.*
import tpcreative.co.qrscanner.ui.changedesign.NewChangeDesignActivity
import tpcreative.co.qrscanner.ui.premiumpopup.PremiumPopupActivity
import vadiole.colorpicker.hexColor
import vadiole.colorpicker.textInputAsFlow


class ChangeDesignTextActivity : AppCompatActivity() {
    private lateinit var binding : ActivityChangeDesignTextBinding
    val TAG = this::class.java
    private var enumGroup : EnumGroup = EnumGroup.TEXT_COLOR
    private var mColorList : ArrayList<ColorPreferenceModel> = arrayListOf()
    private var mFontList : ArrayList<FontModel>  = arrayListOf()
    private var mFontSizeList : ArrayList<FontModel> = arrayListOf()
    private lateinit var bitMap : Bitmap
    private var currentColor :String =  R.color.black.stringHexNoTransparency
    private var currentFont : String = EnumFont.roboto_regular.name
    private var currentBackgroundColor : String = Constant.defaultColor.hexColor
    private var currentText : String = ""
    private var currentFontSize : Int = 90
    private var mapColorTag : HashMap<String,Int>  = hashMapOf()
    private var mapFontTag : HashMap<String,Int>  = hashMapOf()
    private var mapFontSizeTag : HashMap<String,Int>  = hashMapOf()
    private val maxFontSize = 180
    lateinit var viewModel : ChangeDesignTextViewModel
    private var isNavigation : Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.TRANSPARENT
        binding = ActivityChangeDesignTextBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
        currentBackgroundColor = viewModel.mapColor[EnumImage.QR_BACKGROUND] ?: Constant.defaultColor.hexColor
        Utils.Log(TAG,"While color ${Constant.defaultColor.hexColor}")
        Utils.Log(TAG,"Background color intent $currentBackgroundColor")
        if (savedInstanceState!=null){
            enumGroup = EnumGroup.valueOf(savedInstanceState.getString(ConstantKey.KEY_CHANGE_DESIGN_TEXT_ENUM_GROUP) ?: EnumGroup.FONT_SIZE.name)
            mColorList = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_TEXT_COLOR_LIST) ?: arrayListOf()
            mFontList = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_TEXT_FONT_LIST) ?: arrayListOf()
            mFontSizeList = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_TEXT_FONT_SIZE_LIST) ?: arrayListOf()
            viewModel.enumImage = EnumImage.valueOf(savedInstanceState.getString(ConstantKey.KEY_CHANGE_DESIGN_TEXT_ENUM_IMAGE) ?: EnumImage.QR_TEXT_BOTTOM.name)
            currentColor = savedInstanceState.getString(ConstantKey.KEY_CHANGE_DESIGN_TEXT_CURRENT_COLOR) ?: Constant.defaultColor.hexColor
            currentFont = savedInstanceState.getString(ConstantKey.KEY_CHANGE_DESIGN_TEXT_CURRENT_FONT) ?: EnumFont.roboto_regular.name
            currentBackgroundColor = savedInstanceState.getString(ConstantKey.KEY_CHANGE_DESIGN_TEXT_CURRENT_BACKGROUND_COLOR) ?: Constant.defaultColor.hexColor
            viewModel.mapColor = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_TEXT_MAP_COLOR) ?: hashMapOf()
            currentText = savedInstanceState.getString(ConstantKey.KEY_CHANGE_DESIGN_TEXT_CURRENT_TEXT) ?: ""
            currentFontSize = savedInstanceState.getInt(ConstantKey.KEY_CHANGE_DESIGN_TEXT_CURRENT_FONT_SIZE)
            mapColorTag = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_TEXT_MAP_COLOR_TAG) ?: hashMapOf()
            mapFontTag = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_TEXT_MAP_FONT_TAG) ?: hashMapOf()
            mapFontSizeTag = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_TEXT_MAP_FONT_SIZE_TAG) ?: hashMapOf()
            when(enumGroup){
                EnumGroup.TEXT_COLOR ->{
                    addChipHexColor()
                }
                EnumGroup.FONT ->{
                    addChipFont()
                }
                EnumGroup.FONT_SIZE->{
                    addChipFontSize()
                }
                else -> {}
            }
            binding.edtText.setText(currentText)
            binding.edtText.setSelection(currentText.length)
            mRequestBitmap = {
                onChangedRotation()
            }
            NewChangeDesignActivity.mRequestBitmap?.invoke()
            Utils.Log(TAG,"Font size ${mFontList.size}")
            Utils.Log(TAG,"currentText ${currentText}")
        }else{
            addFont()
            addFontSize()
            val mCurrentMap = viewModel.mapText[viewModel.enumImage]
            currentColor = mCurrentMap?.data?.currentColor ?: currentColor
            currentFont = mCurrentMap?.data?.currentFont ?: EnumFont.roboto_regular.name
            currentBackgroundColor = mCurrentMap?.data?.currentBackgroundColor ?: currentBackgroundColor
            currentText = mCurrentMap?.data?.currentText ?: ""
            currentFontSize = mCurrentMap?.data?.currentFontSize ?: 90
            binding.edtText.setText(currentText)
            binding.edtText.setSelection(currentText.length)
            mColorList.clear()
            mColorList.add(ColorPreferenceModel(R.color.white.fromColorIntRes.hexColor,System.currentTimeMillis()))
            viewModel.mapColor.forEach {
                mColorList.add(ColorPreferenceModel(it.value.hexWithoutTransparent,System.currentTimeMillis()))
            }
            Utils.getPopupColorPreferenceColor()?.let {
                mColorList.addAll(it)
            }
            ThemeUtil.getThemeList().forEach { mTheme ->
                mColorList.add(ColorPreferenceModel(mTheme.getPrimaryDarkColor().fromColorIntRes.hexColor,System.currentTimeMillis()))
            }
            val mResult = mColorList.distinctBy { Pair(it.hexColor.lowercase(), it.hexColor.lowercase()) }
            mColorList.clear()
            mColorList.addAll(mResult)
            addChipHexColor()
            Utils.Log(TAG,"List data ${mColorList.toJson()}")
        }
        binding.rlRootClose.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.no_animation,R.anim.slide_down)
        }

        binding.includeDragToClose.imgEdit.setOnClickListener {

        }

        binding.includeDragToClose.imgClose.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.no_animation, R.anim.slide_down)
        }

        mRequestBitmap = {
           onHandleBitmap()
        }
        NewChangeDesignActivity.mRequestBitmap?.invoke()

        binding.dragToClose.setDragListener(object : DragListener {
            override fun onStartDraggingView() {
                Utils.Log(TAG, "onStartDraggingView()")
            }

            override fun onDragging(dragOffset: Float) {
                Utils.Log(TAG, "onDragging(): $dragOffset")
            }

            override fun onViewCosed() {
                Utils.Log(TAG, "onViewCosed()")
            }
        })

        /*Press back button*/
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    finish()
                    overridePendingTransition(R.anim.no_animation, R.anim.slide_down)
                }
            })

        binding.edtText.textInputAsFlow()
            .map {
                val searchBarIsEmpty: Boolean = it.isNullOrBlank()
                return@map it
            }
            .debounce(200) // delay to prevent searching immediately on every character input
            .onEach {
                try {
                    currentText = it.toString()
                    onDrawBitmap()
                    Utils.Log(TAG,"Changed text ${it.toString()}")
                }catch (e : Exception){
                    e.printStackTrace()
                }
            }
            .launchIn(MainScope())
        binding.includeDragToClose.imgClose.addCircleRipple()
        binding.includeDragToClose.imgEdit.addCircleRipple()
        if (viewModel.enumImage == EnumImage.QR_TEXT_TOP){
            onShowGuide(binding.edtText,R.string.enter_a_text.toText(),EnumActivity.TEXT_ACTIVITY,R.drawable.ic_qr_text_top,BubbleShowCase.ArrowPosition.BOTTOM)
        }else{
            onShowGuide(binding.edtText,R.string.enter_a_text.toText(),EnumActivity.TEXT_ACTIVITY,R.drawable.ic_qr_text_bottom,BubbleShowCase.ArrowPosition.BOTTOM)
        }
    }

    private fun onSave(){
        if (viewModel.mapText[viewModel.enumImage] == null){
            val it = TextModel()
            viewModel.mapText[viewModel.enumImage] = TextModel(it.enumIcon,it.type,it.enumChangeDesignType,TextDataModel(currentColor,currentFont,currentBackgroundColor,currentText,currentFontSize))
            viewModel.mapText[EnumImage.QR_TEXT_TOP]?.data?.currentText?.isBlank().apply {
                if (this==true){
                    viewModel.mapText.remove(EnumImage.QR_TEXT_TOP)
                }
            }
            viewModel.mapText[EnumImage.QR_TEXT_BOTTOM]?.data?.currentText?.isBlank().apply {
                if (this==true){
                    viewModel.mapText.remove(EnumImage.QR_TEXT_BOTTOM)
                }
            }
            NewChangeDesignActivity.mResultText?.invoke(viewModel.mapText)
        }else{
            viewModel.mapText[viewModel.enumImage]?.let {
                viewModel.mapText[viewModel.enumImage] = TextModel(it.enumIcon,it.type,it.enumChangeDesignType,TextDataModel(currentColor,currentFont,currentBackgroundColor,currentText,currentFontSize))
                viewModel.mapText[EnumImage.QR_TEXT_TOP]?.data?.currentText?.isBlank().apply {
                    if (this==true){
                        viewModel.mapText.remove(EnumImage.QR_TEXT_TOP)
                    }
                }
                viewModel.mapText[EnumImage.QR_TEXT_BOTTOM]?.data?.currentText?.isBlank().apply {
                    if (this==true){
                        viewModel.mapText.remove(EnumImage.QR_TEXT_BOTTOM)
                    }
                }
                NewChangeDesignActivity.mResultText?.invoke(viewModel.mapText)
            }
        }
    }

    private fun onChangedRotation(){
        lifecycleScope.launch(Dispatchers.Main){
            onDrawBitmap()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        onSave()
    }

    private fun onDrawBitmap(){
        val mBitmap = Bitmap.createBitmap(bitMap)
        viewModel.mapText[viewModel.enumImage] = TextModel(EnumIcon.ic_qr_background,viewModel.enumImage,EnumChangeDesignType.NORMAL, TextDataModel(currentColor,currentFont,currentBackgroundColor,currentText, currentFontSize))
        mBitmap.onDrawOnBitmap(viewModel.mapText) { bm->
            lifecycleScope.launch(Dispatchers.Main){
                binding.imgReview.setImageBitmap(null)
                binding.imgReview.setImageBitmap(bm)
                NewChangeDesignActivity.mResultProgressing?.invoke(bm)
                Utils.Log(TAG,"Bitmap width: ${bm?.width} height: ${bm?.height}")
            }
        }
    }

    private fun addChipHexColor(){
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding.llGroup.removeAllViews()
        mColorList.forEachIndexed { index, s ->
            val xmlView = inflater.inflate(R.layout.item_change_design_text_color, null, false)
            val img = xmlView.findViewById<CircleImageView>(R.id.chip)
            val imgSelected = xmlView.findViewById<AppCompatImageView>(R.id.imgSelected)
            img.tag = index
            xmlView.tag = index
            imgSelected.visibility = if (mapColorTag[ConstantKey.KEY_COLOR] == index)  View.VISIBLE  else View.GONE
            img.setCircleBackgroundColor(s.hexColor.toColorInt())
            xmlView.addCircleRipple()
            xmlView.setOnClickListener {
                Utils.Log(TAG,"Get position ${it.tag}")
                Utils.Log(TAG,"Hex color ${mColorList.get(it.tag as Int).hexColor}")
                mapColorTag[ConstantKey.KEY_COLOR] = index
                val mObject = mColorList[it.tag as Int]
                mObject.isSelected = !mObject.isSelected
                currentColor = mObject.hexColor
                lifecycleScope.launch(Dispatchers.IO){
                    onDrawBitmap()
                }
                runOnUiThread {
                    addChipHexColor()
                }
            }
            binding.llGroup.addView(xmlView)
        }
    }

    private fun onHandleBitmap(){
        viewModel.onDrawable() {
            bitMap = it.toBitmap(1024,1024)
            bitMap.onDrawOnBitmap(viewModel.mapText){
                binding.imgReview.setImageBitmap(it)
            }
        }
        Utils.Log(TAG,"Callback bitmap here")
    }

    private fun addChipFont(){
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding.llGroup.removeAllViews()
        mFontList.forEachIndexed { index, s ->
            val xmlView = inflater.inflate(R.layout.item_change_design_text_font, null, false)
            val tvFont = xmlView.findViewById<AppCompatTextView>(R.id.tvFont)
            val include = xmlView.findViewById<ConstraintLayout>(R.id.includeFont)
            tvFont.tag = index
            xmlView.tag = index
            if (mapFontTag[ConstantKey.KEY_FONT] == index) {
                tvFont.background  = ContextCompat.getDrawable(this, R.drawable.bg_selected_highlight)
            }else{
                tvFont.background  = null
            }
            tvFont.text = s.fontName
            tvFont.typeface = s.enumFont.font.typeface
            if (s.enumChangeDesignType != EnumChangeDesignType.VIP || Utils.isPremium()){
                include.visibility = View.GONE
            }
            xmlView.addCircleRipple()
            xmlView.setOnClickListener { it ->
                val mObject = mFontList[it.tag as Int]
                if (mObject.enumChangeDesignType == EnumChangeDesignType.VIP && !Utils.isPremium()){
                    if (isNavigation){
                        return@setOnClickListener
                    }
                    isNavigation = true
                    val mIndex = HashMap<EnumImage,TextModel>()
                    viewModel.mapText.forEach {
                        mIndex.put(it.key,it.value)
                    }
                    mIndex.forEach {
                        mIndex[it.key] = TextModel(it.value.enumIcon,it.value.type,it.value.enumChangeDesignType, TextDataModel(it.value.data.currentColor,mObject.enumFont.name,it.value.data.currentBackgroundColor,it.value.data.currentText, it.value.data.currentFontSize))
                    }
                    val mEnumView = EnumView.TEXT_FONT
                    premiumPopupForResult.launch(Navigator.onPremiumPopupView(this,viewModel.viewModel.getChangeDataReviewToPremiumPopup(mIndex),viewModel.viewModel.shape,
                        PremiumPopupActivity::class.java,viewModel.viewModel.dataCode,viewModel.viewModel.uuId, fontModel = mObject, enumView = mEnumView,index = index))
                }else{
                    changeFontPosition(mObject,index)
                }
            }
            binding.llGroup.addView(xmlView)
        }
    }

    private fun changeFontPosition(mObject :FontModel, index : Int){
        mapFontTag[ConstantKey.KEY_FONT] = index
        mObject.isSelected = !mObject.isSelected
        currentFont = mObject.enumFont.name
        lifecycleScope.launch(Dispatchers.IO){
            onDrawBitmap()
        }
        runOnUiThread {
            addChipFont()
        }
    }

    private fun addChipFontSize(){
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding.llGroup.removeAllViews()
        mFontSizeList.forEachIndexed { index, s ->
            val xmlView = inflater.inflate(R.layout.item_change_design_text_font_size, null, false)
            val tvFontSize = xmlView.findViewById<AppCompatTextView>(R.id.tvFontSize)
            val include = xmlView.findViewById<ConstraintLayout>(R.id.includeFontSize)
            tvFontSize.tag = index
            xmlView.tag = index
            if (mapFontSizeTag[ConstantKey.KEY_FONT_SIZE] == index) {
                tvFontSize.background  = ContextCompat.getDrawable(this, R.drawable.bg_selected_highlight)
            }else{
                tvFontSize.background  = null
            }
            tvFontSize.text = s.name
            if (s.enumChangeDesignType != EnumChangeDesignType.VIP || Utils.isPremium()){
                include.visibility = View.GONE
            }
            when(s.enumFontSize){
                EnumFontSize.FREEDOM_INCREASE ->{
                    tvFontSize.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_increase_left, 0, 0, 0);
                }
                EnumFontSize.FREEDOM_DECREASE ->{
                    tvFontSize.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_decrease_left, 0, 0, 0);
                }
                else -> {
                    tvFontSize.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                }
            }
            tvFontSize.typeface = EnumFont.roboto_regular.font.typeface
            xmlView.addCircleRipple()
            xmlView.setOnClickListener {
                val mObject = mFontSizeList[it.tag as Int]
                if (mObject.enumChangeDesignType == EnumChangeDesignType.VIP && !Utils.isPremium()){
                    if (isNavigation){
                        return@setOnClickListener
                    }
                    isNavigation = true
                    val mIndex = HashMap<EnumImage,TextModel>()
                    viewModel.mapText.forEach {
                        mIndex.put(it.key,it.value)
                    }
                    mIndex.forEach {
                        mIndex[it.key] = TextModel(it.value.enumIcon,it.value.type,it.value.enumChangeDesignType, TextDataModel(it.value.data.currentColor,mObject.enumFont.name,it.value.data.currentBackgroundColor,it.value.data.currentText, it.value.data.currentFontSize))
                    }
                    val mEnumView = EnumView.TEXT_FONT_SIZE
                    premiumPopupForResult.launch(Navigator.onPremiumPopupView(this,viewModel.viewModel.getChangeDataReviewToPremiumPopup(mIndex),viewModel.viewModel.shape,
                        PremiumPopupActivity::class.java,viewModel.viewModel.dataCode,viewModel.viewModel.uuId,mObject.enumFontSize, fontModel = mObject, enumView = mEnumView,index = index))
                }else{
                    changeFontSizePosition(mObject,index)
                }
            }
            binding.llGroup.addView(xmlView)
        }
    }

    private fun changeFontSizePosition(mObject :FontModel, index : Int){
        mapFontSizeTag[ConstantKey.KEY_FONT_SIZE] = index
        mObject.isSelected = !mObject.isSelected
        mObject.let { mFont->
            when(mFont.enumFontSize) {
                EnumFontSize.FREEDOM_INCREASE ->{
                    if (currentFontSize >= maxFontSize){
                        return
                    }
                    val mCount = currentFontSize + 5
                    currentFontSize  = mCount
                    Utils.Log(TAG,"Increase clicked $currentFontSize")
                }
                EnumFontSize.FREEDOM_DECREASE ->{
                    if (currentFontSize <= 70){
                        return
                    }
                    val mCount = currentFontSize - 5
                    currentFontSize  = mCount
                    Utils.Log(TAG,"Decrease clicked $currentFontSize")
                }else ->{
                currentFontSize = mFont.fontSize
            }
            }
            lifecycleScope.launch(Dispatchers.IO){
                onDrawBitmap()
            }
            runOnUiThread {
                addChipFontSize()
            }
        }
    }

    private fun addFont(){
        mFontList.clear()
        mFontList.add(FontModel(name = EnumFont.brandon_bold.name, enumFont = EnumFont.brandon_bold, fontName = "Brandon bold",enumChangeDesignType = EnumChangeDesignType.VIP))
        mFontList.add(FontModel(name = EnumFont.brandon_regular.name,enumFont = EnumFont.brandon_regular,fontName ="Brandon regular",enumChangeDesignType = EnumChangeDesignType.VIP))
        mFontList.add(FontModel(name = EnumFont.acrotray_regular.name, enumFont = EnumFont.acrotray_regular, fontName = "Protract",enumChangeDesignType = EnumChangeDesignType.VIP))
        mFontList.add(FontModel(name = EnumFont.sprots_medium_regular.name,enumFont = EnumFont.sprots_medium_regular,fontName ="Sports medium",enumChangeDesignType = EnumChangeDesignType.VIP))
        mFontList.add(FontModel(name = EnumFont.thespeciment_regular.name, enumFont = EnumFont.thespeciment_regular, fontName = "Specimen",enumChangeDesignType = EnumChangeDesignType.VIP))
        mFontList.add(FontModel(name = EnumFont.nature_beauty_personal.name,enumFont = EnumFont.nature_beauty_personal,fontName ="Nature beauty",enumChangeDesignType = EnumChangeDesignType.VIP))
        mFontList.add(FontModel(name = EnumFont.wedding_day_personal_regular.name, enumFont = EnumFont.wedding_day_personal_regular, fontName = "Wedding day",enumChangeDesignType = EnumChangeDesignType.VIP))
        mFontList.add(FontModel(name = EnumFont.fugazone_regular.name, enumFont = EnumFont.fugazone_regular, fontName = "Fugaz one",enumChangeDesignType = EnumChangeDesignType.VIP))
        mFontList.add(FontModel(name = EnumFont.lobster_regular.name, enumFont = EnumFont.lobster_regular, fontName = "Lobster",enumChangeDesignType = EnumChangeDesignType.VIP))
        mFontList.add(FontModel(name = EnumFont.oleoscriptswashcaps_bold.name, enumFont = EnumFont.oleoscriptswashcaps_bold, fontName = "Oleo script bold",enumChangeDesignType = EnumChangeDesignType.VIP))
        mFontList.add(FontModel(name = EnumFont.oleoscriptswashcaps_regular.name, enumFont = EnumFont.oleoscriptswashcaps_regular, fontName = "Oleo script regular",enumChangeDesignType = EnumChangeDesignType.VIP))
        mFontList.add(FontModel(name = EnumFont.wendyone_regular.name, enumFont = EnumFont.wendyone_regular, fontName = "Wendy one",enumChangeDesignType = EnumChangeDesignType.VIP))
        mFontList.add(FontModel(name = EnumFont.roboto_bold.name,enumFont = EnumFont.roboto_bold,fontName ="Roboto bold"))
        mFontList.add(FontModel(name = EnumFont.roboto_light.name,enumFont = EnumFont.roboto_light,fontName ="Roboto light"))
        mFontList.add(FontModel(name = EnumFont.roboto_medium.name,enumFont = EnumFont.roboto_medium,fontName ="Roboto medium"))
        mFontList.add(FontModel(name = EnumFont.roboto_regular.name,enumFont = EnumFont.roboto_regular,fontName ="Roboto regular"))
    }

    private fun addFontSize(){
        mFontSizeList.clear()
        mFontSizeList.add(FontModel(enumFontSize = EnumFontSize.FREEDOM_DECREASE,name = R.string.freedom.toText(),fontSize = 90,enumChangeDesignType = EnumChangeDesignType.VIP))
        mFontSizeList.add(FontModel(enumFontSize = EnumFontSize.FREEDOM_INCREASE,name = R.string.freedom.toText(),fontSize = 90,enumChangeDesignType = EnumChangeDesignType.VIP))
        mFontSizeList.add(FontModel(enumFontSize = EnumFontSize.SMALL,name = R.string.small.toText(), fontSize = 70))
        mFontSizeList.add(FontModel(enumFontSize = EnumFontSize.MEDIUM,name = R.string.medium.toText(),fontSize = 90))
        mFontSizeList.add(FontModel(enumFontSize = EnumFontSize.LARGE,name = R.string.large.toText(),fontSize = 110))
    }

    fun onClick(v: View) {
        when (v.id) {
            R.id.btnColor -> {
                enumGroup = EnumGroup.TEXT_COLOR
                addChipHexColor()
            }
            R.id.btnFont -> {
                enumGroup = EnumGroup.FONT
                addChipFont()
            }
            R.id.btnFontSize ->{
                enumGroup = EnumGroup.FONT_SIZE
                addChipFontSize()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        isNavigation = false
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        enumGroup = EnumGroup.valueOf(savedInstanceState.getString(ConstantKey.KEY_CHANGE_DESIGN_TEXT_ENUM_GROUP) ?: EnumGroup.FONT_SIZE.name)
        mColorList = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_TEXT_COLOR_LIST) ?: arrayListOf()
        mFontList = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_TEXT_FONT_LIST) ?: arrayListOf()
        mFontSizeList = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_TEXT_FONT_SIZE_LIST) ?: arrayListOf()
        viewModel.enumImage = EnumImage.valueOf(savedInstanceState.getString(ConstantKey.KEY_CHANGE_DESIGN_TEXT_ENUM_IMAGE) ?: EnumImage.QR_TEXT_BOTTOM.name)
        currentColor = savedInstanceState.getString(ConstantKey.KEY_CHANGE_DESIGN_TEXT_CURRENT_COLOR) ?: Constant.defaultColor.hexColor
        currentFont = savedInstanceState.getString(ConstantKey.KEY_CHANGE_DESIGN_TEXT_CURRENT_FONT) ?: EnumFont.roboto_regular.name
        currentBackgroundColor = savedInstanceState.getString(ConstantKey.KEY_CHANGE_DESIGN_TEXT_CURRENT_BACKGROUND_COLOR) ?: Constant.defaultColor.hexColor
        viewModel.mapColor = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_TEXT_FONT_LIST) ?: hashMapOf()
        currentText = savedInstanceState.getString(ConstantKey.KEY_CHANGE_DESIGN_TEXT_CURRENT_TEXT) ?: ""
        currentFontSize = savedInstanceState.getInt(ConstantKey.KEY_CHANGE_DESIGN_TEXT_CURRENT_FONT_SIZE)
        mapColorTag = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_TEXT_MAP_COLOR_TAG) ?: hashMapOf()
        mapFontTag = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_TEXT_MAP_FONT_TAG) ?: hashMapOf()
        mapFontSizeTag = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_TEXT_MAP_FONT_SIZE_TAG) ?: hashMapOf()
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(ConstantKey.KEY_CHANGE_DESIGN_TEXT_ENUM_GROUP,enumGroup.name)
        outState.putSerializable(ConstantKey.KEY_CHANGE_DESIGN_TEXT_COLOR_LIST,mColorList)
        Utils.Log(TAG,"Font size ${mFontList.size}")
        outState.putSerializable(ConstantKey.KEY_CHANGE_DESIGN_TEXT_FONT_LIST,mFontList)
        outState.putSerializable(ConstantKey.KEY_CHANGE_DESIGN_TEXT_FONT_SIZE_LIST,mFontSizeList)
        outState.putString(ConstantKey.KEY_CHANGE_DESIGN_TEXT_ENUM_IMAGE,viewModel.enumImage.name)
        outState.putString(ConstantKey.KEY_CHANGE_DESIGN_TEXT_CURRENT_COLOR,currentColor)
        outState.putString(ConstantKey.KEY_CHANGE_DESIGN_TEXT_CURRENT_FONT,currentFont)
        outState.putString(ConstantKey.KEY_CHANGE_DESIGN_TEXT_CURRENT_BACKGROUND_COLOR,currentBackgroundColor)
        outState.putSerializable(ConstantKey.KEY_CHANGE_DESIGN_TEXT_MAP_COLOR,viewModel.mapColor)
        outState.putString(ConstantKey.KEY_CHANGE_DESIGN_TEXT_CURRENT_TEXT,currentText)
        outState.putInt(ConstantKey.KEY_CHANGE_DESIGN_TEXT_CURRENT_FONT_SIZE,currentFontSize)
        outState.putSerializable(ConstantKey.KEY_CHANGE_DESIGN_TEXT_MAP_COLOR_TAG,mapColorTag)
        outState.putSerializable(ConstantKey.KEY_CHANGE_DESIGN_TEXT_MAP_FONT_TAG,mapFontTag)
        outState.putSerializable(ConstantKey.KEY_CHANGE_DESIGN_TEXT_MAP_FONT_SIZE_TAG,mapFontSizeTag)
    }

    private val premiumPopupForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val index = result.data?.getIntExtra(ConstantKey.KEY_CHANGE_DESIGN_INDEX,0) ?: 0
            val enumView = EnumView.valueOf(result.data?.getStringExtra(ConstantKey.KEY_CHANGE_DESIGN_CURRENT_VIEW) ?: EnumView.LOGO.name)
            val mObject : FontModel = result.data?.serializable(ConstantKey.KEY_CHANGE_DESIGN_TEXT_OBJECT)  ?: FontModel()
            showChangedDesign(mObject,enumView,index)
        }
    }

    private fun showChangedDesign(mObject : FontModel,enumView: EnumView,index : Int){
        val dialog = MaterialDialog(this)
            .title(R.string.alert)
            .message(R.string.new_design_unlocked)
            .negativeButton(res = R.string.ok){
                when(enumView){
                    EnumView.TEXT_FONT ->{
                        changeFontPosition(mObject,index)
                    }
                    EnumView.TEXT_FONT_SIZE ->{
                        changeFontSizePosition(mObject,index)
                    }
                    else -> {}
                }
            }
        dialog.show()
    }


    companion object {
        var mRequestBitmap : (() -> Unit?)? = null
    }
}