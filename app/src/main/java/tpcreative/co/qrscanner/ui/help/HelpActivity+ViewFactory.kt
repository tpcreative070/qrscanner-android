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
import kotlinx.android.synthetic.main.activity_help.*
import kotlinx.android.synthetic.main.activity_help.rlAdsRoot
import kotlinx.android.synthetic.main.activity_help.rlBannerLarger
import kotlinx.android.synthetic.main.activity_help.toolbar
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.network.base.ViewModelFactory
import tpcreative.co.qrscanner.common.services.QRScannerApplication

fun HelpActivity.initUI(){
    setupViewModel()
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    if(Utils.isHiddenAds()){
        rlAdsRoot.visibility = View.GONE
        rlBannerLarger.visibility = View.GONE
    }
    initRecycleView(layoutInflater)
    getData()
    if (QRScannerApplication.getInstance().isHelpFeedbackSmallView() && QRScannerApplication.getInstance().isLiveAds() && QRScannerApplication.getInstance().isEnableHelpFeedbackSmallView() && !Utils.isHiddenAds()) {
        QRScannerApplication.getInstance().requestHelpFeedbackSmallView(this)
    }
    if (QRScannerApplication.getInstance().isHelpFeedbackLargeView() && QRScannerApplication.getInstance().isLiveAds() && QRScannerApplication.getInstance().isEnableHelpFeedbackLargeView() && !Utils.isHiddenAds()) {
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
    recyclerView.layoutManager = mLayoutManager
    recyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
    recyclerView.clearDecorations()
    recyclerView.adapter = adapter
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
