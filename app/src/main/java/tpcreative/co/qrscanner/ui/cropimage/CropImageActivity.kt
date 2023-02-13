package tpcreative.co.qrscanner.ui.cropimage
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.*
import androidx.lifecycle.lifecycleScope
import android.view.*
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.google.gson.Gson
import com.google.zxing.*
import com.google.zxing.client.result.*
import com.google.zxing.common.HybridBinarizer
import com.isseiaoki.simplecropview.callback.CropCallback
import com.isseiaoki.simplecropview.callback.LoadCallback
import com.isseiaoki.simplecropview.callback.MoveUpCallback
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.*
import kotlinx.android.synthetic.main.crop_activity_crop.*
import kotlinx.android.synthetic.main.crop_layout_done_cancel.*
import kotlinx.coroutines.launch
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide
import tpcreative.co.qrscanner.common.extension.*
import tpcreative.co.qrscanner.common.view.crop.Crop
import tpcreative.co.qrscanner.common.view.crop.FileUtil
import tpcreative.co.qrscanner.model.GeneralModel
import tpcreative.co.qrscanner.model.EnumFragmentType
import tpcreative.co.qrscanner.model.EnumImplement
import tpcreative.co.qrscanner.ui.scannerresult.ScannerResultActivity
import java.io.File
import java.util.*

class CropImageActivity : BaseActivitySlide(){

    private var isShareIntent = false
    private var mCreate : GeneralModel? = null
    private var isAutoComplete : Boolean = true
    private var dialog : Dialog? = null
    private var dialogProcessing : Dialog? = null
    private var actualImage: File? = null
    private var compressedImage: File? = null
    private lateinit var mFileDestination : File
    public override fun onCreate(icicle: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(icicle)
        dialog = ProgressDialog.progressDialog(this,R.string.loading.toText())
        dialogProcessing = ProgressDialog.progressDialog(this,R.string.processing.toText())
        setupViews()
        onHandlerIntent()
        btn_done.isEnabled = false
        btn_done.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT
            ) {
                setResult(RESULT_CANCELED)
                finish()
            }
        } else {
            onBackPressedDispatcher.addCallback(
                this,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        setResult(RESULT_CANCELED)
                        finish()
                    }
                })
        }
    }

    private fun customCompressImage() {
        showLoading()
        val imageFolder = File(cacheDir, Constant.images_folder)
        imageFolder.mkdirs()
        mFileDestination = File(imageFolder, "qrcode_processing.png")
        actualImage?.let { imageFile ->
            lifecycleScope.launch {
                // Full custom
                compressedImage = Compressor.compress(this@CropImageActivity, imageFile) {
                    resolution(600, 600)
                    quality(60)
                    format(Bitmap.CompressFormat.JPEG)
                    size(2_097_152) // 2
                    destination(mFileDestination)
                }
                setCompressedImage()
            }
        } ?: showError(getString(R.string.no_items_found))
    }

    private fun showError(errorMessage: String) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }

    private fun setCompressedImage() {
        compressedImage?.let {
            dismissLoading()
            cropImageView?.load(it.toUri())
                ?.initialFrameRect(null)
                ?.useThumbnail(true)
                ?.execute(mLoadCallback)
            cropImageView?.moveUp()
                ?.execute(mMoveUpCallback)
            Utils.Log(TAG, "Compressed image save in " + it.path)
        }
    }

    private fun setupViews() {
        setContentView(R.layout.crop_activity_crop)
        btn_cancel.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
       btn_done.setOnClickListener {
           onCompleteCrop()
       }
    }

    /*Share File To QRScanner*/
    private fun onHandlerIntent() {
        var sourceUri : Uri? = null
        try {
            val action = intent?.action
            val type = intent?.type
            if (Intent.ACTION_SEND == action && type != null) {
                sourceUri = handleSendSingleItem()
                isShareIntent = true
            }else{
                sourceUri = intent.data
                isShareIntent = false
            }
        } catch (e: Exception) {
            Utils.onDropDownAlert(this, getString(R.string.error_occurred_importing))
            e.printStackTrace()
        }
        loadInput(sourceUri)
    }

    private fun handleSendSingleItem()  : Uri?{
        try {
            val imageUri = intent?.parcelable<Parcelable>(Intent.EXTRA_STREAM) as Uri?
            if (imageUri != null) {
                return imageUri
            } else {
                Utils.onDropDownAlert(this, getString(R.string.can_not_support_this_format))
            }
        } catch (e: Exception) {
            Utils.onDropDownAlert(this, getString(R.string.error_occurred_importing))
            e.printStackTrace()
        }
        return null
    }

    private fun onParseData(result: Result?){
        try {
            result?.let {mResult->
                Utils.Log(TAG, "Call back :" + result.text + "  type :" + result.barcodeFormat?.name)
                val parsedResult = ResultParser.parseResult(result)
                val create = Utils.onGeneralParse(mResult,GeneralModel::class)
                create.enumImplement = EnumImplement.VIEW
                create.fragmentType = EnumFragmentType.SCANNER
                create.barcodeFormat = BarcodeFormat.QR_CODE.name
                if (mResult.barcodeFormat != null) {
                    create.barcodeFormat = mResult.barcodeFormat.name
                }
                mCreate = create
                tvFormatType.text = Utils.onTranslateCreateType(parsedResult.type)
                /*Auto detect and navigation*/
                if (isAutoComplete && Utils.isAutoComplete()){
                    onCompleteCrop()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun onDoNavigation(){
        scanForResult.launch(Navigator.onResultView(this, mCreate, ScannerResultActivity::class.java))
    }

    private val scanForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            Utils.Log(TAG,"Okay")
        }
    }

    private fun loadInput(sourceUri : Uri?) {
        sourceUri?.let {
            actualImage = FileUtil().from(this,it)
            customCompressImage()
        }
    }

    private fun onCompleteCrop(){
        if (isShareIntent){
            onDoNavigation()
        }
        finish()
    }


    override fun onDestroy() {
        super.onDestroy()
        mFileDestination.deleteOnExit()
    }

    private fun setResultEncode(encode: Result?) {
        setResult(RESULT_OK, Intent().putExtra(Crop.REQUEST_DATA, Gson().toJson(encode)))
    }

    private fun onRenderCode(bitmap: Bitmap?) {
        try {
            Utils.Log(TAG,"onRenderCode")
            bitmap?.let {
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
                        btn_done.isEnabled = true
                        btn_done.setBackgroundColor(ContextCompat.getColor(this@CropImageActivity, R.color.colorPrimary))
                        setResultEncode(mResult)
                        onParseData(mResult)
                    } else {
                        btn_done.isEnabled = false
                        btn_done.setBackgroundColor(ContextCompat.getColor(this@CropImageActivity, R.color.colorAccent))
                        tvFormatType.text = ""
                    }
                } catch (e: NotFoundException) {
                    e.printStackTrace()
                    Utils.Log(TAG, "Do not recognize qrcode type ${e.message}")
                    btn_done.isEnabled = false
                    btn_done.setBackgroundColor(ContextCompat.getColor(this@CropImageActivity, R.color.colorAccent))
                } catch (e: ChecksumException) {
                    e.printStackTrace()
                    Utils.Log(TAG, "Do not recognize qrcode type ChecksumException")
                    btn_done.isEnabled = false
                    btn_done.setBackgroundColor(ContextCompat.getColor(this@CropImageActivity, R.color.colorAccent))
                }
            }
        } catch (e: FormatException) {
            e.printStackTrace()
            Utils.Log(TAG, "Do not recognize qrcode type FormatException")
            btn_done.isEnabled = false
            btn_done.setBackgroundColor(ContextCompat.getColor(this@CropImageActivity, R.color.colorAccent))
        }
        finally {
            dismissProgress()
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

    // Callbacks ///////////////////////////////////////////////////////////////////////////////////
    private val mLoadCallback: LoadCallback = object : LoadCallback {
        override fun onSuccess() {
            cropImageView?.crop(compressedImage?.toUri())?.execute(mCropCallback)
        }
        override fun onError(e: Throwable) {}
    }

    private val mCropCallback: CropCallback = object : CropCallback {
        override fun onSuccess(cropped: Bitmap) {
             Utils.Log(TAG,"Crop success ${cropped.width} ${cropped.height}")
            onRenderCode(cropped)
        }
        override fun onError(e: Throwable) {
            Utils.Log(TAG,"Crop error")
        }
    }

    private val mMoveUpCallback: MoveUpCallback
        get() = object : MoveUpCallback {
            override fun onSuccess(width: Int, height: Int,rectF: RectF) {
            }
            override fun onError(e: Throwable) {}
            override fun onDown() {
            }
            override fun onRelease() {
                showProgress()
                cropImageView?.crop(compressedImage?.toUri())?.execute(mCropCallback)
            }
        }
    private fun showLoading() {
        dialog?.show()
    }

    private fun dismissLoading() {
        dialog?.dismiss()
    }

    private fun showProgress() {
        dialogProcessing?.show()
    }

    private fun dismissProgress() {
        dialogProcessing?.dismiss()
    }
}