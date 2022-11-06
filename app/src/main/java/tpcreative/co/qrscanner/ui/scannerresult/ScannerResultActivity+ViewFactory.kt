package tpcreative.co.qrscanner.ui.scannerresult
import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.text.InputType
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.tpcreative.supersafe.common.adapter.DividerItemDecoration
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.getInputLayout
import com.afollestad.materialdialogs.input.input
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_result.*
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Navigator
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.network.base.ViewModelFactory
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.ui.review.ReviewActivity
import tpcreative.co.qrscanner.ui.viewcode.ViewCodeActivity

fun ScannerResultActivity.initUI(){
    TAG = this::class.java.name
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    scrollView.smoothScrollTo(0, 0)
    mList.add(llEmail)
    mList.add(llSMS)
    mList.add(llContact)
    mList.add(llLocation)
    mList.add(llEvent)
    mList.add(llWifi)
    mList.add(llTelephone)
    mList.add(llText)
    mList.add(llURL)
    mList.add(llProduct)
    mList.add(llISBN)
    initRecycleView()
    setupViewModel()
    getDataIntent()
    if (QRScannerApplication.getInstance().isRequestLargeAds() && QRScannerApplication.getInstance().isLiveAds() && QRScannerApplication.getInstance().isEnableReviewAds()) {
        QRScannerApplication.getInstance().requestAdsLargeView(this)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        onBackInvokedDispatcher.registerOnBackInvokedCallback(
            OnBackInvokedDispatcher.PRIORITY_DEFAULT
        ) {
           showAds()
        }
    } else {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    showAds()
                }
            })
    }

    val viewForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            Utils.Log(TAG,"${result.resultCode}")
        }
    }

    btnTakeNote.setOnClickListener {
        enterTakeNote()
        Utils.Log(TAG,"action take note")
    }

    imgMarkFavorite.setOnClickListener {
        updatedFavorite()
        Utils.Log(TAG,"action mark favorite")
    }

    rlViewCode.setOnClickListener {
        viewForResult.launch(Navigator.onResultView(this,viewModel.result,ReviewActivity::class.java))
    }
    checkingShowAds()

}




fun ScannerResultActivity.showAds(){
    if (QRScannerApplication.getInstance().isRequestInterstitialAd()){
        // Back is pressed... Finishing the activity
        finish()
        Utils.Log(TAG,"333")
    }else{
        QRScannerApplication.getInstance().loadInterstitialAd(this)
        Utils.Log(TAG,"444")
    }
}
fun ScannerResultActivity.initRecycleView() {
    adapter = ScannerResultActivityAdapter(layoutInflater, this, this)
    val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
    if (recyclerView == null) {
        Utils.Log(TAG, "recyclerview is null")
    }
    recyclerView.layoutManager = mLayoutManager
    recyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
    recyclerView.adapter = adapter
}

fun ScannerResultActivity.getDataIntent() {
    viewModel.getIntent(this).observe(this, Observer {
        setView()
    })
}

fun ScannerResultActivity.checkingShowAds(){
    viewModel.doShowAds().observe(this, Observer {
        doShowAds(it)
    })
}

fun ScannerResultActivity.updatedFavorite(){
    viewModel.doUpdatedFavoriteItem().observe(this, Observer { mResult ->
        Utils.Log(TAG,"Status $mResult")
        checkFavorite()
        viewModel.reloadData()
    })
}

fun ScannerResultActivity.updatedTakeNote(){
    viewModel.doUpdatedTakeNoteItem().observe(this, Observer { mResult ->
        Utils.Log(TAG,"Status $mResult")
        viewModel.reloadData()
    })
}

fun ScannerResultActivity.delete(){
    viewModel.doDelete().observe(this) { mResult ->
        Utils.Log(TAG, "Status $mResult")
        viewModel.reloadData()
        finish()
    }
}

fun ScannerResultActivity.checkFavorite(){
    if (viewModel.isFavorite){
        imgMarkFavorite.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_favorite_24))
    }else{
        imgMarkFavorite.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_unfavorite_24))
    }
}

fun ScannerResultActivity.enterTakeNote() {
    val mMessage = "Alert"
    val builder: MaterialDialog = MaterialDialog(this)
            .title(text = mMessage)
            .negativeButton(R.string.cancel)
            .cancelable(true)
            .cancelOnTouchOutside(false)
            .negativeButton {
            }
            .positiveButton(R.string.update)
            .input(hintRes = R.string.enter_take_note, inputType = (InputType.TYPE_CLASS_TEXT),maxLength = 100, allowEmpty = false){ dialog, text->
                viewModel.takeNoted = text.toString()
                updatedTakeNote()
            }
    val input: TextInputLayout = builder.getInputLayout()
    input.editText?.setText(viewModel.takeNoted)
    input.editText?.setSelection(viewModel.takeNoted.length)
    if (Utils.getPositionTheme()==0){
        input.setBoxBackgroundColorResource(R.color.transparent)
    }else{
        input.setBoxBackgroundColorResource(R.color.grey_dark)
    }
    input.setPadding(0,50,0,20)
    builder.show()
}

fun ScannerResultActivity.shareToSocial(value : String) {
    val intent = Intent()
    intent.action = Intent.ACTION_SEND
    intent.type="text/plain"
    intent.putExtra(Intent.EXTRA_TEXT,value)
    startActivity(Intent.createChooser(intent, "Share"))
}

private fun ScannerResultActivity.setupViewModel() {
    viewModel = ViewModelProvider(
        this,
        ViewModelFactory()
    )[ScannerResultViewModel::class.java]
}