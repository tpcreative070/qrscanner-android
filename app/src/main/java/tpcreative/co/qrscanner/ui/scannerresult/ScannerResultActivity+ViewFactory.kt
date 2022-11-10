package tpcreative.co.qrscanner.ui.scannerresult
import android.text.InputType
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.tpcreative.supersafe.common.adapter.DividerItemDecoration
import co.tpcreative.supersafe.common.adapter.clearDecorations
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.getInputLayout
import com.afollestad.materialdialogs.input.input
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_result.*
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.network.base.ViewModelFactory
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.model.EnumAction

fun ScannerResultActivity.initUI(){
    TAG = this::class.java.name
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    scrollView.smoothScrollTo(0, 0)
    initRecycleView()
    setupViewModel()
    getDataIntent()
    if (QRScannerApplication.getInstance().isResultSmallView() && QRScannerApplication.getInstance().isLiveAds() && QRScannerApplication.getInstance().isEnableResultSmallView()) {
        QRScannerApplication.getInstance().requestResultSmallView(this)
    }
    if (QRScannerApplication.getInstance().isResultLargeView() && QRScannerApplication.getInstance().isLiveAds() && QRScannerApplication.getInstance().isEnableResultLargeView()) {
        QRScannerApplication.getInstance().requestResultLargeView(this)
    }
    checkingShowAds()

}

fun ScannerResultActivity.initRecycleView() {
    adapter = ScannerResultActivityAdapter(layoutInflater, this, this)
    val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
    if (recyclerView == null) {
        Utils.Log(TAG, "recyclerview is null")
    }
    recyclerView.layoutManager = mLayoutManager
    recyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
    recyclerView.clearDecorations()
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
        onCheckFavorite()
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

fun ScannerResultActivity.onCheckFavorite(){
    viewModel.mListNavigation
    viewModel.mListNavigation.filter { it.enumAction == EnumAction.DO_ADVANCE }.forEach { it.isFavorite = viewModel.isFavorite }
    onReloadData()
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
            .positiveButton(R.string.save)
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

private fun ScannerResultActivity.setupViewModel() {
    viewModel = ViewModelProvider(
        this,
        ViewModelFactory()
    )[ScannerResultViewModel::class.java]
}