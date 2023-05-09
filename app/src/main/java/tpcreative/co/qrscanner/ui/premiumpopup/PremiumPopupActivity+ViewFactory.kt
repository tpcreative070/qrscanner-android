package tpcreative.co.qrscanner.ui.premiumpopup
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.extension.addCircleRipple

fun PremiumPopupActivity.initUI(){
    binding.imgCircleCodeStatus.setImageResource(R.color.material_gray_200)
    binding.rlClose.setOnClickListener {
        finish()
    }
    binding.rlClose.addCircleRipple()
}