package tpcreative.co.qrscanner.ui.scannerresult
import android.text.InputType
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.tpcreative.supersafe.common.adapter.DividerItemDecoration
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import kotlinx.android.synthetic.main.fragment_review.*
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.network.base.ViewModelFactory
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.viewmodel.ScannerResultViewModel
import java.io.File

fun ScannerResultFragment.initUI(){
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
        QRScannerApplication.getInstance().getAdsLargeView(this)
    }
    btnTakeNote.setOnClickListener {
        enterTakeNote()
        Utils.Log(TAG,"action take note")
    }

    imgMarkFavorite.setOnClickListener {
        updatedFavorite()
        Utils.Log(TAG,"action mark favorite")
    }
    checkingShowAds()
}

fun ScannerResultFragment.initRecycleView() {
    adapter = ScannerResultAdapter(layoutInflater, this, this)
    val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
    if (recyclerView == null) {
        Utils.Log(TAG, "recyclerview is null")
    }
    recyclerView.layoutManager = mLayoutManager
    recyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
    recyclerView.adapter = adapter
}

fun ScannerResultFragment.getDataIntent() {
    viewModel.getIntent(this).observe(this, Observer {
        setView()
    })
}

fun ScannerResultFragment.checkingShowAds(){
    viewModel.doShowAds().observe(this, Observer {
        doShowAds(it)
    })
}

fun ScannerResultFragment.updatedFavorite(){
    viewModel.doUpdatedFavoriteItem().observe(this, Observer { mResult ->
        Utils.Log(TAG,"Status $mResult")
        checkFavorite()
        viewModel.reloadData()
    })
}

fun ScannerResultFragment.updatedTakeNote(){
    viewModel.doUpdatedTakeNoteItem().observe(this, Observer { mResult ->
        Utils.Log(TAG,"Status $mResult")
        viewModel.reloadData()
    })
}

fun ScannerResultFragment.checkFavorite(){
    if (viewModel.isFavorite){
        imgMarkFavorite.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_favorite_24))
    }else{
        imgMarkFavorite.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_unfavorite_24))
    }
}

fun ScannerResultFragment.enterTakeNote() {
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
    val input: EditText = builder.getInputField()
    if (Utils.getCurrentTheme()==0){
        input.setBackgroundColor(ContextCompat.getColor(this,R.color.white))
    }
    input.setPadding(0,50,0,20)
    builder.show()
}

private fun ScannerResultFragment.setupViewModel() {
    viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory()
    ).get(ScannerResultViewModel::class.java)
}