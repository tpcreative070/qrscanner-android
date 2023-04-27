package tpcreative.co.qrscanner.ui.changedesign
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.davidmiguel.dragtoclose.DragListener
import com.davidmiguel.dragtoclose.DragToClose
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.databinding.ActivityPopupColorBinding

class PopupColorActivity : AppCompatActivity() {
    lateinit var binding : ActivityPopupColorBinding
    private val TAG = this::class.java.simpleName
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.TRANSPARENT
        binding = ActivityPopupColorBinding.inflate(layoutInflater)
        setContentView(binding.root)
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

        binding.colorPicker.checkColor("#ffffff")
        binding.colorPicker.onColorChanged = { color ->
            //onColorChanged(color)
            Utils.Log(TAG,"Color changed $color")
        }

        binding.colorPicker.afterColorChanged = { color ->
            //afterColorChanged(color)
            Utils.Log(TAG,"after color changed $color")
        }
    }
}