package tpcreative.co.qrscanner.ui.help

import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.tpcreative.supersafe.common.adapter.DividerItemDecoration
import co.tpcreative.supersafe.common.adapter.clearDecorations
import com.afollestad.materialdialogs.MaterialDialog
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.network.base.ViewModelFactory
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.common.view.ads.AdsView
import tpcreative.co.qrscanner.model.EnumScreens

fun HelpActivity.initUI(){
    setupViewModel()
    setSupportActionBar(binding.toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    llSmallAds = AdsView(this)
    if(Utils.isHiddenAds(EnumScreens.HELP_FEEDBACK_SMALL)){
        binding.rlAdsRoot.visibility = View.GONE
    }else{
        binding.rlAdsRoot.addView(llSmallAds.getRootSmallAds())
    }
    if(Utils.isHiddenAds(EnumScreens.HELP_FEEDBACK_LARGE)){
        binding.rlBannerLarger.visibility = View.GONE
    }else{
        binding.rlBannerLarger.addView(llSmallAds.getRootLargeAds())
    }
    initRecycleView(layoutInflater)
    getData()
    if (QRScannerApplication.getInstance().isHelpFeedbackSmallView() && QRScannerApplication.getInstance().isLiveAds() && QRScannerApplication.getInstance().isEnableHelpFeedbackSmallView() && !Utils.isHiddenAds(EnumScreens.HELP_FEEDBACK_SMALL)) {
        QRScannerApplication.getInstance().requestHelpFeedbackSmallView(this)
    }
    if (QRScannerApplication.getInstance().isHelpFeedbackLargeView() && QRScannerApplication.getInstance().isLiveAds() && QRScannerApplication.getInstance().isEnableHelpFeedbackLargeView() && !Utils.isHiddenAds(EnumScreens.HELP_FEEDBACK_LARGE)) {
        QRScannerApplication.getInstance().requestHelpFeedbackLargeView(this)
    }
    checkingShowAds()
}

fun HelpActivity.checkingShowAds(){
    viewModel.doShowAds().observe(this, Observer {
        doShowAds(it)
    })
}

fun HelpActivity.getData(){
    viewModel.getData().observe(this, Observer {
        adapter?.setDataSource(it)
    })
}

fun HelpActivity.initRecycleView(layoutInflater: LayoutInflater) {
    adapter = HelpAdapter(layoutInflater, applicationContext, this)
    val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
    binding.recyclerView.layoutManager = mLayoutManager
    binding.recyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
    binding.recyclerView.clearDecorations()
    binding.recyclerView.adapter = adapter
}

fun HelpActivity.onAlertSendEmail() {
    val mMessage = getString(R.string.please_write_your_email_in_english)
    val builder: MaterialDialog = MaterialDialog(this)
        .title(text = mMessage)
        .message(res = R.string.attachment_photo)
        .negativeButton(R.string.cancel)
        .cancelable(true)
        .cancelOnTouchOutside(false)
        .positiveButton(R.string.ok){
            Utils.onSentEmail(this)
        }
    builder.show()
}

private fun HelpActivity.setupViewModel() {
    viewModel = ViewModelProvider(
        this,
        ViewModelFactory()
    ).get(HelpViewModel::class.java)
}
