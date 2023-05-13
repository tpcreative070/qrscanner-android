package tpcreative.co.qrscanner.ui.changedesigntext

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.lifecycle.lifecycleScope
import com.davidmiguel.dragtoclose.DragListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Constant
import tpcreative.co.qrscanner.common.ConstantKey
import tpcreative.co.qrscanner.common.EnumFont
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.extension.*
import tpcreative.co.qrscanner.common.view.CircleImageView
import tpcreative.co.qrscanner.databinding.ActivityChangeDesignTextBinding
import tpcreative.co.qrscanner.model.*
import tpcreative.co.qrscanner.ui.changedesign.NewChangeDesignActivity
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
    private lateinit var enumImage: EnumImage
    private var currentColor :String =  Constant.defaultColor.hexColor
    private var currentFont : Typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
    private var currentBackgroundColor : String = Constant.defaultColor.hexColor
    private var mapColor : HashMap<EnumImage,String> = hashMapOf()
    private var currentText : String = ""
    private var currentFontSize : Int = 90
    private var mapColorTag : HashMap<String,Int>  = hashMapOf()
    private var mapFontTag : HashMap<String,Int>  = hashMapOf()
    private var mapFontSizeTag : HashMap<String,Int>  = hashMapOf()
    private val maxFontSize = 180
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.TRANSPARENT
        binding = ActivityChangeDesignTextBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val bundle: Bundle? = intent?.extras
        enumImage = EnumImage.valueOf(bundle?.getString(ConstantKey.KEY_POPUP_TEXT_TEXT_TYPE) ?: EnumImage.QR_TEXT_BOTTOM.name)
        mapColor = bundle?.serializable(ConstantKey.KEY_CHANGE_DESIGN_COLOR_MAP) ?: hashMapOf()
        currentBackgroundColor = mapColor[EnumImage.QR_BACKGROUND] ?: Constant.defaultColor.hexColor
        Utils.Log(TAG,"Background color intent $currentBackgroundColor")
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
           onHandleBitmap(it)
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
                    if (it.isNullOrEmpty()){
                        binding.imgReview.setImageBitmap(bitMap)
                        return@onEach
                    }
                    currentText = it.toString()
                    onDrawBitmap()
                }catch (e : Exception){
                    e.printStackTrace()
                }
            }
            .launchIn(MainScope())

        addFont()
        addFontSize()
        Utils.getPopupColorPreferenceColor()?.let { it ->
            mColorList.clear()
            mColorList.add(ColorPreferenceModel(R.color.white.fromColorIntRes.hexColor,System.currentTimeMillis()))
            mapColor.forEach {
                mColorList.add(ColorPreferenceModel(it.value,System.currentTimeMillis()))
            }
            mColorList.addAll(it)

            ThemeUtil.getThemeList().forEach { mTheme ->
                mColorList.add(ColorPreferenceModel(mTheme.getPrimaryDarkColor().fromColorIntRes.hexColor,System.currentTimeMillis()))
            }
            val mResult = mColorList.distinctBy { Pair(it.hexColor.lowercase(), it.hexColor.lowercase()) }
            mColorList.clear()
            mColorList.addAll(mResult)
            addChipHexColor()
            Utils.Log(TAG,"Size list ${mColorList.size}")
        }
        binding.includeDragToClose.imgClose.addCircleRipple()
        binding.includeDragToClose.imgEdit.addCircleRipple()
    }

    private suspend fun onDrawBitmap(){
        if (!isAllow()){
            return
        }
        val mBitmap = Bitmap.createBitmap(bitMap)
        mBitmap.onDrawOnBitmap(currentText,enumImage, currentFont,currentFontSize,currentColor,currentBackgroundColor) { bm->
            lifecycleScope.launch(Dispatchers.Main){
                binding.imgReview.setImageBitmap(null)
                binding.imgReview.setImageBitmap(bm)
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

    private fun onHandleBitmap(bm : Bitmap){
        this.bitMap = Bitmap.createBitmap(bm)
        lifecycleScope.launch(Dispatchers.Main){
            binding.imgReview.setImageBitmap(bitMap)
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
            if (s.enumChangeDesignType != EnumChangeDesignType.VIP){
                include.visibility = View.GONE
            }
            xmlView.addCircleRipple()
            xmlView.setOnClickListener {
                Utils.Log(TAG,"Get position ${it.tag}")
                mapFontTag[ConstantKey.KEY_FONT] = index
                val mObject = mFontList[it.tag as Int]
                mObject.isSelected = !mObject.isSelected
                mObject.enumFont.font.typeface?.let {
                    currentFont = it
                    lifecycleScope.launch(Dispatchers.IO){
                        onDrawBitmap()
                    }

                    runOnUiThread {
                        addChipFont()
                    }
                }
            }
            binding.llGroup.addView(xmlView)
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
            if (s.enumChangeDesignType != EnumChangeDesignType.VIP){
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
                Utils.Log(TAG,"Get position ${it.tag}")
                mapFontSizeTag[ConstantKey.KEY_FONT_SIZE] = index
                val mObject = mFontSizeList[it.tag as Int]
                mObject.isSelected = !mObject.isSelected
                mObject.let { mFont->
                    when(mFont.enumFontSize) {
                        EnumFontSize.FREEDOM_INCREASE ->{
                            if (currentFontSize >= maxFontSize){
                                return@setOnClickListener
                            }
                            val mCount = currentFontSize + 5
                            currentFontSize  = mCount
                            Utils.Log(TAG,"Increase clicked $currentFontSize")
                        }
                        EnumFontSize.FREEDOM_DECREASE ->{
                            if (currentFontSize <= 70){
                                return@setOnClickListener
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
            binding.llGroup.addView(xmlView)
        }
    }

    private fun addFont(){
        mFontList.clear()
        mFontList.add(FontModel(name = EnumFont.brandon_bold.name, enumFont = EnumFont.brandon_bold, fontName = "Brandon bold",enumChangeDesignType = EnumChangeDesignType.VIP))
        mFontList.add(FontModel(name = EnumFont.brandon_regular.name,enumFont = EnumFont.brandon_regular,fontName ="Brandon regular",enumChangeDesignType = EnumChangeDesignType.VIP))
        mFontList.add(FontModel(name = EnumFont.roboto_bold.name,enumFont = EnumFont.roboto_bold,fontName ="Roboto bold"))
        mFontList.add(FontModel(name = EnumFont.roboto_light.name,enumFont = EnumFont.roboto_light,fontName ="Roboto light"))
        mFontList.add(FontModel(name = EnumFont.roboto_medium.name,enumFont = EnumFont.roboto_medium,fontName ="Roboto medium"))
        mFontList.add(FontModel(name = EnumFont.roboto_regular.name,enumFont = EnumFont.roboto_regular,fontName ="Roboto regular"))
    }

    private fun addFontSize(){
        mFontSizeList.clear()
        mFontSizeList.add(FontModel(enumFontSize = EnumFontSize.SMALL,name = R.string.small.toText(), fontSize = 70))
        mFontSizeList.add(FontModel(enumFontSize = EnumFontSize.MEDIUM,name = R.string.medium.toText(),fontSize = 90))
        mFontSizeList.add(FontModel(enumFontSize = EnumFontSize.LARGE,name = R.string.large.toText(),fontSize = 110))
        mFontSizeList.add(FontModel(enumFontSize = EnumFontSize.FREEDOM_INCREASE,name = R.string.freedom.toText(),fontSize = 90,enumChangeDesignType = EnumChangeDesignType.VIP))
        mFontSizeList.add(FontModel(enumFontSize = EnumFontSize.FREEDOM_DECREASE,name = R.string.freedom.toText(),fontSize = 90,enumChangeDesignType = EnumChangeDesignType.VIP))
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

    private fun isAllow(): Boolean {
        if (currentText.isEmpty()){
            return false
        }
        return true
    }

    companion object {
        var mRequestBitmap : ((bitmap : Bitmap) -> Unit?)? = null
    }
}