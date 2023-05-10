package tpcreative.co.qrscanner.ui.changedesign
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
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

class PopupColorActivity : AppCompatActivity() {
    lateinit var binding : ActivityPopupColorBinding
    private val TAG = this::class.java.simpleName
    private lateinit var imageType : EnumImage
    private lateinit var mMapColor : HashMap<EnumImage,String>
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
            Utils.Log(TAG,"after color without no transparent $mHexColorWithoutTransparent")
            NewChangeDesignActivity.mResult?.invoke(mHexColorWithoutTransparent)
        }
        binding.imgClose.addCircleRipple()
        binding.imgEdit.addCircleRipple()
    }
}