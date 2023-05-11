package tpcreative.co.qrscanner.ui.changedesign

import android.graphics.Color
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.davidmiguel.dragtoclose.DragListener
import com.davidmiguel.dragtoclose.DragToClose
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.databinding.ActivityChangeDesignTextBinding

class ChangeDesignTextActivity : AppCompatActivity() {
    private lateinit var binding : ActivityChangeDesignTextBinding
    val TAG = this::class.java
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.TRANSPARENT
        binding = ActivityChangeDesignTextBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imgClose.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.no_animation, R.anim.slide_down)
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
                    overridePendingTransition(R.anim.no_animation, R.anim.slide_down)
                }
            })

    }
}