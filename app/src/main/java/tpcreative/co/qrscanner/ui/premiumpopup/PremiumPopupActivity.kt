package tpcreative.co.qrscanner.ui.premiumpopup

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import tpcreative.co.qrscanner.common.activity.BaseActivity
import tpcreative.co.qrscanner.common.extension.px
import tpcreative.co.qrscanner.databinding.ActivityPremiumPopupBinding

class PremiumPopupActivity : BaseActivity() {
    lateinit var binding : ActivityPremiumPopupBinding
    lateinit var viewModel: PremiumPopupViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.TRANSPARENT
        binding = ActivityPremiumPopupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
    }

    fun redesignLayout(){
        if (viewModel.isBitMap()){
            val params = RelativeLayout.LayoutParams(
                400f.px,
                320f.px
            ).apply {
                topMargin = 20
                marginEnd = 20
                marginStart = 20
                bottomMargin = 20
                addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
            }
            binding.rlRoot.layoutParams = params
        }
    }
}