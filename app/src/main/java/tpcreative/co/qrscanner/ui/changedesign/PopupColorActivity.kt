package tpcreative.co.qrscanner.ui.changedesign

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import com.davidmiguel.dragtoclose.DragListener
import com.davidmiguel.dragtoclose.DragToClose
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Constant
import tpcreative.co.qrscanner.common.ConstantKey
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.extension.*
import tpcreative.co.qrscanner.common.view.CircleImageView
import tpcreative.co.qrscanner.databinding.ActivityPopupColorBinding
import tpcreative.co.qrscanner.model.ColorPreferenceModel
import tpcreative.co.qrscanner.model.EnumImage
import vadiole.colorpicker.ColorModel
import vadiole.colorpicker.ColorPickerView
import vadiole.colorpicker.OnSwitchColorModelListener
import vadiole.colorpicker.hexColor
import java.util.TreeSet

class PopupColorActivity : AppCompatActivity() {
    lateinit var binding : ActivityPopupColorBinding
    private val TAG = this::class.java.simpleName
    private lateinit var imageType : EnumImage
    private lateinit var mMapColor : HashMap<EnumImage,String>
    private lateinit var colorPicker : ColorPickerView
    private var isGrid : Boolean = true
    private var mListColor : ArrayList<ColorPreferenceModel> = arrayListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.TRANSPARENT
        binding = ActivityPopupColorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val bundle: Bundle? = intent?.extras
        mMapColor = bundle?.serializable(ConstantKey.KEY_CHANGE_DESIGN_COLOR_MAP) ?: HashMap<EnumImage,String>()
        imageType = EnumImage.valueOf(bundle?.serializable(ConstantKey.KEY_CHANGE_DESIGN_COLOR_TYPE) ?:"")
        Utils.Log(TAG,"Map result ${mMapColor.toJson()}")
        Utils.Log(TAG,"Map result image ${imageType.name}")
        binding.imgClose.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.no_animation,R.anim.slide_down)
        }

        val dragToClose = findViewById<DragToClose>(R.id.drag_to_close)
        dragToClose.setDragListener(object : DragListener {
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
                    overridePendingTransition(R.anim.no_animation,R.anim.slide_down)
                }
            })

        Utils.Log(TAG,"Checked color ${mMapColor[imageType]}")

        binding.colorPicker.checkColor(mMapColor[imageType] ?: "#ffffff")
        binding.colorPicker.onColorChanged = { color ->
            //onColorChanged(color)
            Utils.Log(TAG,"Color changed $color")
        }

        binding.colorPicker.afterColorChanged = { color ->
            //afterColorChanged(color)
            Utils.Log(TAG,"after color changed $color")
            val mHexColorWithoutTransparent = color.toColorInt().hexColor
            mMapColor[imageType] = mHexColorWithoutTransparent
            Utils.Log(TAG,"after color without no transparent $mHexColorWithoutTransparent")
            binding.imgReviewColor.setBackgroundColor(mHexColorWithoutTransparent.toColorInt())
            NewChangeDesignActivity.mResult?.invoke(mHexColorWithoutTransparent)
        }
        binding.imgClose.addCircleRipple()
        binding.imgEdit.addCircleRipple()
        binding.imgAdd.addCircleRipple()
        binding.imgDelete.addCircleRipple()

        binding.imgAdd.setImageResource(R.color.material_gray_700)
        binding.imgDelete.setImageResource(R.color.material_gray_700)
        binding.imgDelete.visibility = View.INVISIBLE
        binding.imgIconDelete.visibility = View.INVISIBLE
        Utils.getPopupColorPreferenceColor()?.let {
            mListColor.clear()
            mListColor.addAll(it)
            addChipHexColor()
        }
        if (savedInstanceState!=null){
            isGrid = savedInstanceState.getBoolean(ConstantKey.KEY_POPUP_COLOR_GRID)
            mMapColor[imageType] = savedInstanceState.getString(ConstantKey.KEY_POPUP_COLOR_SELECTED) ?: "#FFFFFF"
            mListColor = savedInstanceState.serializable(ConstantKey.KEY_POPUP_COLOR_COLOR_PREFERENCE) ?: arrayListOf()
            addChipHexColor()
        }
        addSliderColor()

        binding.imgAdd.setOnClickListener {
            Utils.Log(TAG,"Add")
            if (mListColor.size>=20){
                return@setOnClickListener
            }
            val mModel = ColorPreferenceModel((mMapColor[imageType] ?: Constant.defaultColor.hexColor),System.currentTimeMillis())
            mListColor.add(0,mModel)
            val mResult = mListColor.distinctBy { Pair(it.hexColor, it.hexColor) }
            mListColor.clear()
            mListColor.addAll(mResult)
            addChipHexColor()
        }

        binding.imgDelete.setOnClickListener {
            Utils.Log(TAG,"Delete")
            mListColor.remove(mListColor.lastOrNull())
            addChipHexColor()
        }

        if (isGrid){
            binding.colorPicker.visibility = View.VISIBLE
            colorPicker.visibility = View.INVISIBLE
        }else{
            binding.colorPicker.visibility = View.INVISIBLE
            colorPicker.visibility = View.VISIBLE
        }
        binding.imgReviewColor.setBackgroundColor(mMapColor[imageType]?.toColorInt() ?: R.color.colorAccent)
    }

    fun onClick(v: View) {
        when (v.id) {
            R.id.btnGrid -> {
                binding.colorPicker.checkColor(mMapColor[imageType] ?: Constant.defaultColor.hexColor)
                colorPicker.visibility = View.INVISIBLE
                binding.colorPicker.visibility = View.VISIBLE
                isGrid = true
            }
            R.id.btnSlider -> {
                colorPicker.setSwitchView((mMapColor[imageType] ?: Constant.defaultColor.hexColor).toColorInt())
                colorPicker.visibility = View.VISIBLE
                binding.colorPicker.visibility = View.INVISIBLE
                isGrid = false
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(ConstantKey.KEY_POPUP_COLOR_GRID,isGrid)
        outState.putString(ConstantKey.KEY_POPUP_COLOR_SELECTED,mMapColor[imageType])
        outState.putSerializable(ConstantKey.KEY_POPUP_COLOR_COLOR_PREFERENCE,mListColor)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        isGrid = savedInstanceState.getBoolean(ConstantKey.KEY_POPUP_COLOR_GRID)
        mMapColor[imageType] = savedInstanceState.getString(ConstantKey.KEY_POPUP_COLOR_SELECTED) ?: Constant.defaultColor.hexColor
        mListColor = savedInstanceState.serializable(ConstantKey.KEY_POPUP_COLOR_COLOR_PREFERENCE) ?: arrayListOf()
        super.onRestoreInstanceState(savedInstanceState)
    }

    private fun addChipHexColor(){
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding.rlGroup.removeAllViews()
        if (mListColor.isEmpty()){
            binding.imgDelete.visibility = View.INVISIBLE
            binding.imgIconDelete.visibility = View.INVISIBLE
        }else{
            binding.imgDelete.visibility = View.VISIBLE
            binding.imgIconDelete.visibility = View.VISIBLE
        }
        mListColor.forEachIndexed { index, s ->
            val xmlView = inflater.inflate(R.layout.item_chip, null, false)
            val img = xmlView.findViewById<CircleImageView>(R.id.chip)
            img.tag = index
            xmlView.tag = index
            img.setCircleBackgroundColor(s.hexColor.toColorInt())
            xmlView.addCircleRipple()
            xmlView.setOnClickListener {
                Utils.Log(TAG,"Get position ${it.tag}")
                mMapColor[imageType] = mListColor[it.tag as Int].hexColor
                if (isGrid) {
                    binding.colorPicker.checkColor(mMapColor[imageType] ?:  Constant.defaultColor.hexColor)
                    binding.imgReviewColor.setBackgroundColor(mMapColor[imageType]?.toColorInt()?:R.color.white)
                    NewChangeDesignActivity.mResult?.invoke(mMapColor[imageType] ?: Constant.defaultColor.hexColor)
                }else{
                    colorPicker.setSwitchView((mMapColor[imageType] ?: Constant.defaultColor.hexColor).toColorInt())
                }
            }
            binding.rlGroup.addView(xmlView)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.setPopupColorPreferenceColor(mListColor)
    }

    private fun addSliderColor(){
        colorPicker = ColorPickerView(
            this,
            R.string.ok,
            R.string.cancel,
            Color.parseColor(mMapColor[imageType]),
            ColorModel.valueOf(ColorModel.RGB.name),
            false,
            object  : OnSwitchColorModelListener {
                override fun onColorModelSwitched(colorModel: ColorModel) {
                }
            })
        colorPicker.onColorSelected = {
            Utils.Log(TAG,"Hex color $it")
            mMapColor[imageType] = it ?: "#FFFFFF"
            NewChangeDesignActivity.mResult?.invoke(it ?: Constant.defaultColor.hexColor)
        }
        colorPicker.onColorSelectedProgressing = {
            binding.imgReviewColor.setBackgroundColor(it?.toColorInt() ?: R.color.colorAccent)
        }
        binding.rlGroupColor.addView(colorPicker)
    }
}