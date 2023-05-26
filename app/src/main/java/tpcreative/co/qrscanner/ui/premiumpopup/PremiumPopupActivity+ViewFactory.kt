package tpcreative.co.qrscanner.ui.premiumpopup
import android.view.View
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Configuration
import tpcreative.co.qrscanner.common.Constant
import tpcreative.co.qrscanner.common.Navigator
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.controller.ServiceManager
import tpcreative.co.qrscanner.common.extension.addCircleRipple
import tpcreative.co.qrscanner.common.extension.onDrawOnBitmap
import tpcreative.co.qrscanner.common.network.base.ViewModelFactory
import tpcreative.co.qrscanner.ui.changedesign.ChangeDesignViewModel
import tpcreative.co.qrscanner.ui.changedesign.NewChangeDesignActivity
import tpcreative.co.qrscanner.ui.changedesign.VIP

fun PremiumPopupActivity.initUI(){
    binding.imgCircleCodeStatus.setImageResource(R.color.material_gray_200)
    binding.rlClose.setOnClickListener {
        finish()
    }
    binding.rlProVersion.setOnClickListener {
        Navigator.onMoveProVersion(this)
    }
    binding.rlClose.addCircleRipple()
    setupViewModel()
    viewModel.getIntent(this){
        binding.imgQRCode.setImageBitmap(it)
        val mIsShow = Utils.isShowReward()
        hiddenView(mIsShow)
    }
    binding.rlQRDisplay.setOnClickListener {
        showAds()
    }
}

private fun PremiumPopupActivity.hiddenView(isShowAds : Boolean){
    if (isShowAds){
        binding.tvWatchAds.text = String.format(getString(R.string.watch_item_ads),countRewarded)
        loadingRewardAds()
    }else{
        binding.rlNextAds.visibility = View.GONE
        binding.rlWatchAds.visibility = View.GONE
    }
    if (viewModel.isBitMap()){
        binding.imgQRCode.visibility = View.GONE
        redesignLayout()
    }
    else if (viewModel.getText() !=null){
        binding.imgQRCode.visibility = View.GONE
        binding.tvOwnLogo.text = viewModel.getText()
        redesignLayout()
    }
    else{
        binding.tvOwnLogo.visibility = View.GONE
    }
}

private fun PremiumPopupActivity.setupViewModel() {
    viewModel = ViewModelProvider(
        this,
        ViewModelFactory()
    ).get(PremiumPopupViewModel::class.java)
}