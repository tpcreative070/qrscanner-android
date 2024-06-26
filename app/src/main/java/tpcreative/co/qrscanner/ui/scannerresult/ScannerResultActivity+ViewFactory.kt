package tpcreative.co.qrscanner.ui.scannerresult
import android.graphics.Bitmap
import android.text.InputType
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.tpcreative.flagkit.FlagKit
import co.tpcreative.supersafe.common.adapter.DividerItemDecoration
import co.tpcreative.supersafe.common.adapter.clearDecorations
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.getInputLayout
import com.afollestad.materialdialogs.input.input
import tpcreative.co.qrscanner.ui.scanner.cpp.BarcodeEncoder
import com.google.android.material.textfield.TextInputLayout
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import kotlinx.coroutines.*
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Constant
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.network.base.ViewModelFactory
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.common.view.ads.AdsView
import tpcreative.co.qrscanner.model.EnumAction
import tpcreative.co.qrscanner.model.EnumScreens
import tpcreative.co.qrscanner.model.Theme
import java.util.*


fun ScannerResultActivity.initUI(){
    TAG = this::class.java.name
    setSupportActionBar(binding.toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    binding.scrollView.smoothScrollTo(0, 0)
    initRecycleView()
    setupViewModel()
    getDataIntent()
    loadAds()
}

fun ScannerResultActivity.loadAds(){
    if (!Utils.isPremium()){
        viewAds = AdsView(this)
    }
    if(Utils.isHiddenAds(EnumScreens.SCANNER_RESULT_SMALL)){
        binding.rlAdsRoot.visibility = View.GONE
    }else{
        binding.rlAdsRoot.addView(viewAds?.getRootSmallAds())
    }
    if(Utils.isHiddenAds(EnumScreens.SCANNER_RESULT_LARGE)){
        binding.rlBannerLarger.visibility = View.GONE
    }else{
        binding.rlBannerLarger.addView(viewAds?.getRootLargeAds())
    }
    if (QRScannerApplication.getInstance().isResultSmallView() && QRScannerApplication.getInstance().isLiveAds() && QRScannerApplication.getInstance().isEnableResultSmallView() && !Utils.isHiddenAds(EnumScreens.SCANNER_RESULT_SMALL) && !Utils.isRequestShowLocalAds()) {
        QRScannerApplication.getInstance().requestResultSmallView(this)
    }
    if (QRScannerApplication.getInstance().isResultLargeView() && QRScannerApplication.getInstance().isLiveAds() && QRScannerApplication.getInstance().isEnableResultLargeView() && !Utils.isHiddenAds(EnumScreens.SCANNER_RESULT_LARGE) && !Utils.isRequestShowLocalAds()) {
        QRScannerApplication.getInstance().requestResultLargeView(this)
    }
    checkingShowAds()
}

fun ScannerResultActivity.initRecycleView() {
    adapter = ScannerResultActivityAdapter(layoutInflater, this, this)
    val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
    binding.recyclerView.layoutManager = mLayoutManager
    binding.recyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
    binding.recyclerView.clearDecorations()
    binding.recyclerView.adapter = adapter
}

fun ScannerResultActivity.getDataIntent() {
    viewModel.getIntent(this){
        setView()
    }
}

fun ScannerResultActivity.checkingShowAds(){
    viewModel.doShowAds{
        doShowAds(it)
    }
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
    val mMessage = getString(R.string.add_note)
    val builder: MaterialDialog = MaterialDialog(this)
            .title(text = mMessage)
            .negativeButton(R.string.cancel)
            .cancelable(true)
            .cancelOnTouchOutside(false)
            .negativeButton {
            }
            .positiveButton(R.string.save)
            .input(hintRes = R.string.enter_take_note, inputType = (InputType.TYPE_CLASS_TEXT),maxLength = 100, allowEmpty = false){ _, text->
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

suspend fun ScannerResultActivity.onGenerateReview(code: String?, mFormatCode: BarcodeFormat) =
    withContext(Dispatchers.IO) {
        try {
            val barcodeEncoder = BarcodeEncoder()
            val hints: MutableMap<EncodeHintType?, Any?> =
                EnumMap<EncodeHintType, Any?>(EncodeHintType::class.java)
            hints[EncodeHintType.CHARACTER_SET] = Charsets.UTF_8
            val theme: Theme? = Theme.getInstance()?.getThemeInfo()
            val mBitmap = if ((BarcodeFormat.QR_CODE !=  mFormatCode)) {
                hints[EncodeHintType.MARGIN] = 5
                var mWidth = Constant.QRCodeViewWidth + 100
                var mHeight = Constant.QRCodeViewHeight - 100
                if (mFormatCode== BarcodeFormat.AZTEC || mFormatCode == BarcodeFormat.DATA_MATRIX){
                    mWidth = Constant.QRCodeViewWidth
                    mHeight = Constant.QRCodeViewHeight
                    hints[EncodeHintType.MARGIN] = 2
                }
                barcodeEncoder.encodeBitmap(
                    this@onGenerateReview,
                    theme?.getPrimaryDarkColor() ?: 0,
                    code,
                    mFormatCode,
                    mWidth,
                    mHeight,
                    hints
                )
            } else {
                hints[EncodeHintType.MARGIN] = 2
                barcodeEncoder.encodeBitmap(
                    this@onGenerateReview,
                    theme?.getPrimaryDarkColor() ?: 0,
                    code,
                    BarcodeFormat.QR_CODE,
                    Constant.QRCodeViewHeight,
                    Constant.QRCodeViewHeight,
                    hints
                )
            }
            onDecode(mBitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

suspend fun ScannerResultActivity.onDecode(bitmap : Bitmap) =
    withContext(Dispatchers.Main) {
        val intArray = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(intArray, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        val source: LuminanceSource = RGBLuminanceSource(bitmap.width, bitmap.height, intArray)
        val mBitmap = BinaryBitmap(HybridBinarizer(source))
        Utils.Log(TAG,"width ${bitmap.width} height ${bitmap.height}")
        val reader: Reader = MultiFormatReader()
        try {
            var mResult : Result? = null
            try {
                mResult = reader.decode(mBitmap)
            }catch (e : Exception){
                e.printStackTrace()
                try {
                    mResult = reader.decode(mBitmap,addHint())
                }catch (e: Exception){
                    e.printStackTrace()
                }
            }
            if (mResult != null) {
                val mCountryCode = mResult.resultMetadata[ResultMetadataType.POSSIBLE_COUNTRY]
                mCountryCode?.let {
                    if (it == "US/CA"){
                        val drawable = FlagKit.getDrawable(this@onDecode, "us")
                        val l = Locale("", "us")

                        binding.imgFlag.setImageDrawable(drawable)
                        binding.tvFlag.text = String.format(getString(R.string.country_display1),l.displayCountry)

                        val drawable1 = FlagKit.getDrawable(this@onDecode, "ca")
                        val l1 = Locale("", "ca")

                        binding.imgFlag1.setImageDrawable(drawable1)
                        binding.tvFlag1.text = String.format(getString(R.string.country_display),l1.displayCountry)
                        binding.imgFlag1.visibility = View.VISIBLE
                        binding.tvFlag1.visibility = View.VISIBLE
                    }else{
                        val drawable = FlagKit.getDrawable(this@onDecode, it.toString().lowercase())
                        val l = Locale("", it.toString().lowercase())
                        binding.imgFlag.setImageDrawable(drawable)
                        binding.tvFlag.text = String.format(getString(R.string.country_display),l.displayCountry)
                    }
                    binding.imgFlag.visibility = View.VISIBLE
                    binding.tvFlag.visibility = View.VISIBLE
                }
                bitmap.recycle()
            }
        } catch (e: NotFoundException) {
            e.printStackTrace()
            Utils.Log(TAG, "Do not recognize qrcode type ${e.message}")
            bitmap.recycle()
        } catch (e: ChecksumException) {
            e.printStackTrace()
            Utils.Log(TAG, "Do not recognize qrcode type ChecksumException")
            bitmap.recycle()
        } catch (e: FormatException) {
            bitmap.recycle()
        }
    }

private fun addHint() : MutableMap<DecodeHintType, Any>{
    val tmpHintsMap: MutableMap<DecodeHintType, Any> = EnumMap(
        DecodeHintType::class.java
    )
    tmpHintsMap[DecodeHintType.TRY_HARDER] = java.lang.Boolean.TRUE
    tmpHintsMap[DecodeHintType.POSSIBLE_FORMATS] = EnumSet.allOf(BarcodeFormat::class.java)
    tmpHintsMap[DecodeHintType.PURE_BARCODE] = java.lang.Boolean.TRUE
    return tmpHintsMap
}


private fun ScannerResultActivity.setupViewModel() {
    viewModel = ViewModelProvider(
        this,
        ViewModelFactory()
    )[ScannerResultViewModel::class.java]
}