package tpcreative.co.qrscanner.ui.changedesigntext

import android.content.Context
import android.content.Intent
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
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.toColor
import androidx.core.graphics.toColorInt
import androidx.lifecycle.lifecycleScope
import com.davidmiguel.dragtoclose.DragListener
import com.davidmiguel.dragtoclose.DragToClose
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
    private var mListColor : ArrayList<ColorPreferenceModel> = arrayListOf()
    private var mListFont : ArrayList<FontModel>  = arrayListOf()
    private var mListFontSize : ArrayList<FontModel> = arrayListOf()
    private lateinit var bitMap : Bitmap
    private lateinit var enumImage: EnumImage
    private var currentColor : Int = Color.parseColor("#000000")
    private var currentFont : Typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
    private var currentBackgroundColor : String = Constant.defaultColor.hexColor
    private var mapColor : HashMap<EnumImage,String> = hashMapOf()
    private var currentText : String = ""
    private var currentFontSize : Int = 90
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
            mListColor.clear()
            mListColor.add(ColorPreferenceModel(R.color.white.fromColorIntRes.hexColor,System.currentTimeMillis()))
            mapColor.forEach {
                mListColor.add(ColorPreferenceModel(it.value,System.currentTimeMillis()))
            }
            mListColor.addAll(it)

            ThemeUtil.getThemeList().forEach { mTheme ->
                mListColor.add(ColorPreferenceModel(mTheme.getPrimaryDarkColor().fromColorIntRes.hexColor,System.currentTimeMillis()))
            }
            val mResult = mListColor.distinctBy { Pair(it.hexColor, it.hexColor) }
            mListColor.clear()
            mListColor.addAll(mResult)
            addChipHexColor()
            Utils.Log(TAG,"Size list ${mListColor.size}")
        }
        binding.includeDragToClose.imgClose.addCircleRipple()
        binding.includeDragToClose.imgEdit.addCircleRipple()
    }

    private suspend fun onDrawBitmap(){
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
        mListColor.forEachIndexed { index, s ->
            val xmlView = inflater.inflate(R.layout.item_change_design_text_color, null, false)
            val img = xmlView.findViewById<CircleImageView>(R.id.chip)
            img.tag = index
            xmlView.tag = index
            img.setCircleBackgroundColor(s.hexColor.toColorInt())
            xmlView.addCircleRipple()
            xmlView.setOnClickListener {
                Utils.Log(TAG,"Get position ${it.tag}")
                Utils.Log(TAG,"Hex color ${mListColor.get(it.tag as Int).hexColor}")
                currentColor = mListColor.get(it.tag as Int).hexColor.toColorInt()
                lifecycleScope.launch(Dispatchers.IO){
                    onDrawBitmap()
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
        mListFont.forEachIndexed { index, s ->
            val xmlView = inflater.inflate(R.layout.item_change_design_text_font, null, false)
            val tvFont = xmlView.findViewById<AppCompatTextView>(R.id.tvFont)
            tvFont.tag = index
            xmlView.tag = index
            tvFont.text = s.fontName
            tvFont.typeface = s.enumFont.font.typeface
            xmlView.addCircleRipple()
            xmlView.setOnClickListener {
                Utils.Log(TAG,"Get position ${it.tag}")
                mListFont[it.tag as Int].enumFont.font.typeface?.let {
                    currentFont = it
                    lifecycleScope.launch(Dispatchers.IO){
                        onDrawBitmap()
                    }
                }
            }
            binding.llGroup.addView(xmlView)
        }
    }

    private fun addChipFontSize(){
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding.llGroup.removeAllViews()
        mListFontSize.forEachIndexed { index, s ->
            val xmlView = inflater.inflate(R.layout.item_change_design_text_font_size, null, false)
            val tvFontSize = xmlView.findViewById<AppCompatTextView>(R.id.tvFontSize)
            val imgIcon = xmlView.findViewById<AppCompatImageView>(R.id.imgIcon)
            tvFontSize.tag = index
            xmlView.tag = index
            tvFontSize.text = s.name
            imgIcon.visibility = View.VISIBLE
            when(s.enumFontSize){
                EnumFontSize.FREEDOM_INCREASE ->{
                    imgIcon.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_increase))
                }
                EnumFontSize.FREEDOM_DECREASE ->{
                    imgIcon.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_decrease))
                }
                else -> {
                    imgIcon.visibility = View.GONE
                }
            }
            tvFontSize.typeface = EnumFont.roboto_regular.font.typeface
            xmlView.addCircleRipple()
            xmlView.setOnClickListener {
                Utils.Log(TAG,"Get position ${it.tag}")
                mListFontSize[it.tag as Int].let { mFont->
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
                }
            }
            binding.llGroup.addView(xmlView)
        }
    }

    private fun addFont(){
        mListFont.clear()
        mListFont.add(FontModel(name = EnumFont.brandon_bold.name, enumFont = EnumFont.brandon_bold, fontName = "Brandon bold"))
        mListFont.add(FontModel(name = EnumFont.brandon_regular.name,enumFont = EnumFont.brandon_regular,fontName ="Brandon regular"))
        mListFont.add(FontModel(name = EnumFont.roboto_bold.name,enumFont = EnumFont.roboto_bold,fontName ="Roboto bold"))
        mListFont.add(FontModel(name = EnumFont.roboto_light.name,enumFont = EnumFont.roboto_light,fontName ="Roboto light"))
        mListFont.add(FontModel(name = EnumFont.roboto_medium.name,enumFont = EnumFont.roboto_medium,fontName ="Roboto medium"))
        mListFont.add(FontModel(name = EnumFont.roboto_regular.name,enumFont = EnumFont.roboto_regular,fontName ="Roboto regular"))
    }

    private fun addFontSize(){
        mListFontSize.clear()
        mListFontSize.add(FontModel(enumFontSize = EnumFontSize.SMALL,name = R.string.small.toText(), fontSize = 70))
        mListFontSize.add(FontModel(enumFontSize = EnumFontSize.MEDIUM,name = R.string.medium.toText(),fontSize = 90))
        mListFontSize.add(FontModel(enumFontSize = EnumFontSize.LARGE,name = R.string.large.toText(),fontSize = 110))
        mListFontSize.add(FontModel(enumFontSize = EnumFontSize.FREEDOM_INCREASE,name = R.string.freedom.toText(),fontSize = 90))
        mListFontSize.add(FontModel(enumFontSize = EnumFontSize.FREEDOM_DECREASE,name = R.string.freedom.toText(),fontSize = 90))
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

    companion object {
        var mRequestBitmap : ((bitmap : Bitmap) -> Unit?)? = null
    }
}