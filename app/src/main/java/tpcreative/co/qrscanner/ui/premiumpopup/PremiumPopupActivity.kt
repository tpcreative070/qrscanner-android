package tpcreative.co.qrscanner.ui.premiumpopup

import android.graphics.Color
import android.os.Bundle
import tpcreative.co.qrscanner.common.activity.BaseActivity
import tpcreative.co.qrscanner.databinding.ActivityPremiumPopupBinding

class PremiumPopupActivity : BaseActivity() {
    lateinit var binding : ActivityPremiumPopupBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.TRANSPARENT
        binding = ActivityPremiumPopupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()

    }
}