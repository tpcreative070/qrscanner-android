package tpcreative.co.qrscanner.ui.changedesign

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import com.davidmiguel.dragtoclose.DragListener
import com.davidmiguel.dragtoclose.DragToClose
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.extension.addCircleRipple
import tpcreative.co.qrscanner.common.extension.fromColorIntRes
import tpcreative.co.qrscanner.common.view.CircleImageView
import tpcreative.co.qrscanner.databinding.ActivityChangeDesignTextBinding
import tpcreative.co.qrscanner.model.ColorPreferenceModel
import tpcreative.co.qrscanner.model.ThemeUtil
import vadiole.colorpicker.hexColor
import vadiole.colorpicker.textInputAsFlow

class ChangeDesignTextActivity : AppCompatActivity() {
    private lateinit var binding : ActivityChangeDesignTextBinding
    val TAG = this::class.java
    private var isColor : Boolean = true
    private var mListColor : ArrayList<ColorPreferenceModel> = arrayListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.TRANSPARENT
        binding = ActivityChangeDesignTextBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        binding.edtText.textInputAsFlow()
            .map {
                val searchBarIsEmpty: Boolean = it.isNullOrBlank()
                return@map it
            }
            .debounce(200) // delay to prevent searching immediately on every character input
            .onEach {
                if (!it.isNullOrEmpty()){
                    try {
                    }catch (e : Exception){
                        e.printStackTrace()
                    }
                }
                Utils.Log(TAG,"${it?.trim()}")
            }
            .launchIn(MainScope())

        Utils.getPopupColorPreferenceColor()?.let { it ->
            mListColor.clear()
            mListColor.addAll(it)
            mListColor.add(ColorPreferenceModel(R.color.white.fromColorIntRes.hexColor,System.currentTimeMillis()))
            ThemeUtil.getThemeList().forEach { mTheme ->
                mListColor.add(ColorPreferenceModel(mTheme.getPrimaryDarkColor().fromColorIntRes.hexColor,System.currentTimeMillis()))
            }
            addChipHexColor()
            Utils.Log(TAG,"Size list ${mListColor.size}")
        }
        binding.includeDragToClose.imgClose.addCircleRipple()
        binding.includeDragToClose.imgEdit.addCircleRipple()
    }

    private fun addChipHexColor(){
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding.llGroup.removeAllViews()
        mListColor.forEachIndexed { index, s ->
            val xmlView = inflater.inflate(R.layout.item_change_design_text, null, false)
            val img = xmlView.findViewById<CircleImageView>(R.id.chip)
            img.tag = index
            xmlView.tag = index
            img.setCircleBackgroundColor(s.hexColor.toColorInt())
            xmlView.addCircleRipple()
            xmlView.setOnClickListener {
                Utils.Log(TAG,"Get position ${it.tag}")
                Utils.Log(TAG,"Hex color ${mListColor.get(it.tag as Int).hexColor}")
            }
            binding.llGroup.addView(xmlView)
        }
    }

    fun onClick(v: View) {
        when (v.id) {
            R.id.btnColor -> {
                isColor = true
            }
            R.id.btnFont -> {
                isColor = false
            }
        }
    }
}