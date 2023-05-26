package tpcreative.co.qrscanner.ui.premiumpopup

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import tpcreative.co.qrscanner.BuildConfig
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Constant
import tpcreative.co.qrscanner.common.ConstantKey
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.activity.BaseActivity
import tpcreative.co.qrscanner.common.extension.px
import tpcreative.co.qrscanner.common.extension.toText
import tpcreative.co.qrscanner.databinding.ActivityPremiumPopupBinding


class PremiumPopupActivity : BaseActivity() {
    lateinit var binding : ActivityPremiumPopupBinding
    lateinit var viewModel: PremiumPopupViewModel
    var rewardedAd: RewardedAd? = null
    var countRewarded : Int = Constant.countLimitRewarded
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.TRANSPARENT
        binding = ActivityPremiumPopupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (savedInstanceState!=null){
            countRewarded = savedInstanceState.getInt(ConstantKey.KEY_PREMIUM_POPUP_COUNT_REWARDED)
        }
        initUI()
    }

    fun redesignLayout(){
        if (viewModel.isBitMap() || viewModel.getText() !=null){
            val params = RelativeLayout.LayoutParams(
                400f.px,
                330f.px
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

    fun loadingRewardAds(){
        showWatchUI(false)
        val adRequest = AdRequest.Builder().build()
        val mId  : String = if (BuildConfig.DEBUG){
            R.string.reward_test.toText()
        }else{
            R.string.rewarded_change_design.toText()
        }
        RewardedAd.load(this,mId, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Utils.Log(TAG, adError.toString())
                rewardedAd = null
                showWatchUI(true)
            }

            override fun onAdLoaded(ad: RewardedAd) {
                Utils.Log(TAG, "Ad was loaded.")
                rewardedAd = ad
                showWatchUI(true)
            }
        })
    }

    fun showAds(){
        if (rewardedAd==null || !Utils.isShowReward()){
            return
        }
        rewardedAd?.let { ad ->
            ad.show(this) { rewardItem ->
                // Handle the reward.
                val rewardAmount = rewardItem.amount
                val rewardType = rewardItem.type
                countRewarded -= 1
                binding.tvWatchAds.text =
                    String.format(R.string.watch_item_ads.toText(),countRewarded)
                Utils.Log(TAG, "User earned the reward. $rewardAmount")
            }
        } ?: run {
            if (countRewarded>0){
                loadingRewardAds()
            }
            Utils.Log(TAG, "The rewarded ad wasn't ready yet.")
        }
        rewardedAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
            override fun onAdClicked() {
                // Called when a click is recorded for an ad.
                Utils.Log(TAG, "Ad was clicked.")
            }

            override fun onAdDismissedFullScreenContent() {
                // Called when ad is dismissed.
                // Set the ad reference to null so you don't show the ad a second time.
                Utils.Log(TAG, "Ad dismissed fullscreen content.")
                rewardedAd = null
                if (countRewarded>0){
                    loadingRewardAds()
                }else{
                   loadAdsFinished()
                }
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                // Called when ad fails to show.
                Utils.Log(TAG, "Ad failed to show fullscreen content.")
                rewardedAd = null
            }

            override fun onAdImpression() {
                // Called when an impression is recorded for an ad.
                Utils.Log(TAG, "Ad recorded an impression.")
            }

            override fun onAdShowedFullScreenContent() {
                // Called when ad is shown.
                Utils.Log(TAG, "Ad showed fullscreen content.")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (Utils.isPremium()){
            finish()
        }
    }

    fun showWatchUI(isShow : Boolean){
        if (isShow){
            binding.constraintAds.visibility = View.VISIBLE
            binding.progressLoadingReward.visibility = View.INVISIBLE
        }else{
            binding.constraintAds.visibility = View.INVISIBLE
            binding.progressLoadingReward.visibility = View.VISIBLE
        }
    }

    fun loadAdsFinished(){
        val output = Intent()
        output.putExtra(ConstantKey.KEY_CHANGE_DESIGN_CURRENT_VIEW, viewModel.enumView.name)
        output.putExtra(ConstantKey.KEY_CHANGE_DESIGN_INDEX, viewModel.index)
        output.putExtra(ConstantKey.KEY_CHANGE_DESIGN_TEXT_OBJECT,viewModel.font)
        setResult(RESULT_OK, output)
        finish()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(ConstantKey.KEY_PREMIUM_POPUP_COUNT_REWARDED,countRewarded)
        Utils.Log(TAG,"State saved")
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        countRewarded = savedInstanceState.getInt(ConstantKey.KEY_PREMIUM_POPUP_COUNT_REWARDED)
        Utils.Log(TAG,"State restore")
    }
}