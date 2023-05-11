package tpcreative.co.qrscanner.ui.changedesign
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.lifecycle.lifecycleScope
import com.davidmiguel.dragtoclose.DragListener
import com.davidmiguel.dragtoclose.DragToClose
import kotlinx.coroutines.launch
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.ConstantKey
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.extension.*
import tpcreative.co.qrscanner.databinding.ActivityPopupColorBinding
import tpcreative.co.qrscanner.model.EnumImage
import tpcreative.co.qrscanner.ui.review.initUI
import tpcreative.co.qrscanner.ui.review.showAds
import vadiole.colorpicker.ColorModel
import vadiole.colorpicker.ColorPickerView
import vadiole.colorpicker.OnSwitchColorModelListener
import vadiole.colorpicker.hexColor

class PopupColorActivity : AppCompatActivity() {
    lateinit var binding : ActivityPopupColorBinding
    private val TAG = this::class.java.simpleName
    private lateinit var imageType : EnumImage
    private lateinit var mMapColor : HashMap<EnumImage,String>
    private lateinit var colorPicker : ColorPickerView
    private var isGrid : Boolean = true
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
        if (savedInstanceState!=null){
            isGrid = savedInstanceState.getBoolean(ConstantKey.KEY_POPUP_COLOR_GRID)
            mMapColor[imageType] = savedInstanceState.getString(ConstantKey.KEY_POPUP_COLOR_SELECTED) ?: "#FFFFFF"
        }
        addSliderColor()
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
                colorPicker.visibility = View.INVISIBLE
                binding.colorPicker.visibility = View.VISIBLE
                isGrid = true
            }
            R.id.btnSlider -> {
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
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        isGrid = savedInstanceState.getBoolean(ConstantKey.KEY_POPUP_COLOR_GRID)
        mMapColor[imageType] = savedInstanceState.getString(ConstantKey.KEY_POPUP_COLOR_SELECTED) ?: "\"#FFFFFF\""
        super.onRestoreInstanceState(savedInstanceState)
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
            NewChangeDesignActivity.mResult?.invoke(it ?: "#FFFFFF")
        }
        colorPicker.onColorSelectedProgressing = {
            binding.imgReviewColor.setBackgroundColor(it?.toColorInt() ?: R.color.colorAccent)
        }
        binding.rlGroupColor.addView(colorPicker)
    }
}