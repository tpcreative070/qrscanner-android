package tpcreative.co.qrscanner.ui.cropimage

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.Window
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.drawable.toIcon
import androidx.core.graphics.toPointF
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.google.zxing.*
import com.google.zxing.client.result.ResultParser
import com.google.zxing.common.HybridBinarizer
import com.isseiaoki.simplecropview.callback.CropCallback
import com.isseiaoki.simplecropview.callback.LoadCallback
import com.isseiaoki.simplecropview.callback.MoveUpCallback
import com.zxingcpp.BarcodeReader
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Constant
import tpcreative.co.qrscanner.common.Navigator
import tpcreative.co.qrscanner.common.ProgressDialog
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide
import tpcreative.co.qrscanner.common.extension.*
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.common.view.crop.Crop
import tpcreative.co.qrscanner.common.view.crop.FileUtil
import tpcreative.co.qrscanner.databinding.CropActivityCropBinding
import tpcreative.co.qrscanner.model.EnumFragmentType
import tpcreative.co.qrscanner.model.EnumImplement
import tpcreative.co.qrscanner.model.GeneralModel
import tpcreative.co.qrscanner.ui.scannerresult.ScannerResultActivity
import java.io.File
import java.util.*

const val key_type = "key_type"
class CropImageActivity : BaseActivitySlide(){

    private var isShareIntent = false
    private var isChangeDesign = false
    private var mCreate : GeneralModel? = null
    private var isAutoComplete : Boolean = true
    private var dialog : Dialog? = null
    private var actualImage: File? = null
    private var compressedImage: File? = null
    private var mFileDestination : File = File("")
    lateinit var binding : CropActivityCropBinding
    private val readerCpp = BarcodeReader()
    private var bitmapChangeDesign : Bitmap?  = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        dialog = ProgressDialog.progressDialog(this,R.string.loading.toText())
        setupViews()
        onHandlerIntent()
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

    private fun customCompressImage(images : File?) {
        showLoading()
        val imageFolder = File(cacheDir, Constant.images_folder)
        imageFolder.mkdirs()
        mFileDestination = File(imageFolder, "qrcode_processing.png")
        images?.let { imageFile ->
            if (imageFile.exists()){
                lifecycleScope.launch {
                    // Full custom
                    compressedImage = Compressor.compress(this@CropImageActivity, imageFile) {
                        resolution(600, 800)
                        quality(60)
                        format(Bitmap.CompressFormat.JPEG)
                        size(2_097_152) // 2
                        destination(mFileDestination)
                    }
                    setCompressedImage()
                }
            }else{
                showError(getString(R.string.error_occurred_importing))
            }
        } ?: showError(getString(R.string.error_occurred_importing))
    }

    private fun showError(errorMessage: String) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }

    private fun setCompressedImage() {
        compressedImage?.let {
            dismissLoading()
            binding.cropImageView.load(it.toUri())
                ?.initialFrameRect(null)
                ?.useThumbnail(true)
                ?.execute(mLoadCallback)
            binding.cropImageView.moveUp()
                ?.execute(mMoveUpCallback)
            Utils.Log(TAG, "Compressed image save in " + it.path)
        }
    }

    private fun setupViews() {
        binding = CropActivityCropBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.doneCancelBar.btnCancel.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
       binding.doneCancelBar.btnDone.setOnClickListener {
           if (isChangeDesign){
               val mUri = bitmapChangeDesign?.storeBitmap()
               setResultChangeDesign(mUri)
               finish()
           }else{
               onCompleteCrop()
           }
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
                val bundle :Bundle? = intent.extras
                isChangeDesign = bundle?.getBoolean(key_type,false) ?: false
                Utils.Log(TAG,"bundle response $isChangeDesign")
            }
        } catch (e: Exception) {
            Utils.onDropDownAlert(this, getString(R.string.error_occurred_importing))
            e.printStackTrace()
        }
        if (isChangeDesign){
            binding.doneCancelBar.btnDone.isEnabled = true
            binding.doneCancelBar.btnDone.setBackgroundColor(ContextCompat.getColor(this@CropImageActivity, R.color.colorPrimary))
            binding.tvGuide.text = getString(R.string.drag_the_orange_markers_to_crop_picture)
        }else{
            binding.doneCancelBar.btnDone.isEnabled = false
            binding.doneCancelBar.btnDone.setBackgroundColor(ContextCompat.getColor(this@CropImageActivity, R.color.colorAccent))
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
                binding.tvFormatType.text = Utils.onTranslateCreateType(parsedResult.type)
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
        sourceUri?.let {uri->
            actualImage = FileUtil().from(this,uri)
            if (actualImage?.exists() == true){
                actualImage?.let {
                    customCompressImage(it)
                }
            }else{
                showError(getString(R.string.error_occurred_importing))
            }
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
        mFileDestination.delete()
        actualImage?.delete()
        compressedImage?.delete()
        QRScannerApplication.getInstance().setRequestClearCacheData(true)
    }

    private fun setResultEncode(encode: Result?) {
        setResult(RESULT_OK, Intent().putExtra(Crop.REQUEST_DATA, Gson().toJson(encode)))
    }

    private fun setResultChangeDesign(uri: Uri?) {
        setResult(RESULT_OK, Intent().putExtra(Crop.REQUEST_CHANGE_DESIGN_DATA,uri))
    }


    private fun onRenderCode(bm: Bitmap?) {
        var bitmap = bm
        Utils.Log(TAG,"Requesting....")
        try {
            Utils.Log(TAG,"onRenderCode")
            bitmap?.let {
                var intArray = IntArray(it.width * it.height)
                it.getPixels(intArray, 0, it.width, 0, 0, it.width, it.height)
                var source: LuminanceSource = RGBLuminanceSource(it.width, it.height, intArray)
                var mBitmap = BinaryBitmap(HybridBinarizer(source))
                Utils.Log(TAG,"width ${it.width} height ${it.height}")
                val reader: Reader = MultiFormatReader()
                try {
                    var mResult : Result? = null
                    try {
                        mResult = reader.decode(mBitmap)
                    }catch (e : Exception){
                        e.printStackTrace()
                        Utils.Log(TAG,"Reader again...1")
                        try {
                            mResult = reader.decode(mBitmap,addHint())
                        }catch (e: Exception){
                            e.printStackTrace()
                            Utils.Log(TAG,"Reader again...2")
                        }
                    }

                    try {
                        if (mResult==null){
                            bitmap  = createBlackAndWhite(it)
                            bitmap?.let { bitmapResult ->
                                intArray = IntArray(bitmapResult.width * bitmapResult.height)
                                bitmapResult.getPixels(intArray, 0, bitmapResult.width, 0, 0, bitmapResult.width, bitmapResult.height)
                                source = RGBLuminanceSource(bitmapResult.width, bitmapResult.height, intArray)
                                mBitmap = BinaryBitmap(HybridBinarizer(source))
                                Utils.Log(TAG,"Reader again...3")
                                mResult = reader.decode(mBitmap)
                            }
                        }
                    }catch (e : Exception){
                        e.printStackTrace()
                        Utils.Log(TAG,"Reader again...4")
                    }

                    try {
                        if (mResult==null){
                            bitmap = it.rotate(90F)
                            Utils.Log(TAG,"Reader again 5")
                            bitmap?.let { bitmapResult ->
                                intArray = IntArray(bitmapResult.width * bitmapResult.height)
                                bitmapResult.getPixels(intArray, 0, bitmapResult.width, 0, 0, bitmapResult.width, bitmapResult.height)
                                source = RGBLuminanceSource(bitmapResult.width, bitmapResult.height, intArray)
                                mBitmap = BinaryBitmap(HybridBinarizer(source))
                                mResult = reader.decode(mBitmap)
                                Utils.Log(TAG,"Reader again 6")
                            }
                        }
                    }catch (e: Exception){
                        e.printStackTrace()
                        Utils.Log(TAG,"Reader again 7")
                    }
                    if (mResult != null) {
                        lifecycleScope.launch(Dispatchers.Main){
                            binding.doneCancelBar.btnDone.isEnabled = true
                            binding.doneCancelBar.btnDone.setBackgroundColor(ContextCompat.getColor(this@CropImageActivity, R.color.colorPrimary))
                            setResultEncode(mResult)
                            onParseData(mResult)
                        }
                    } else {
                        lifecycleScope.launch(Dispatchers.Main){
                            binding.doneCancelBar.btnDone.isEnabled = false
                            binding.doneCancelBar.btnDone.setBackgroundColor(ContextCompat.getColor(this@CropImageActivity, R.color.colorAccent))
                            binding.tvFormatType.text = ""
                        }
                    }
                } catch (e: NotFoundException) {
                    e.printStackTrace()
                    Utils.Log(TAG, "Do not recognize qrcode type ${e.message}")
                    lifecycleScope.launch(Dispatchers.Main){
                        binding.doneCancelBar.btnDone.isEnabled = false
                        binding.doneCancelBar.btnDone.setBackgroundColor(ContextCompat.getColor(this@CropImageActivity, R.color.colorAccent))
                    }
                } catch (e: ChecksumException) {
                    e.printStackTrace()
                    Utils.Log(TAG, "Do not recognize qrcode type ChecksumException")
                    lifecycleScope.launch(Dispatchers.Main){
                        binding.doneCancelBar.btnDone.isEnabled = false
                        binding.doneCancelBar.btnDone.setBackgroundColor(ContextCompat.getColor(this@CropImageActivity, R.color.colorAccent))
                    }
                }
            }
        } catch (e: FormatException) {
            e.printStackTrace()
            Utils.Log(TAG, "Do not recognize qrcode type FormatException")
            lifecycleScope.launch(Dispatchers.Main){
                binding.doneCancelBar.btnDone.isEnabled = false
                binding.doneCancelBar.btnDone.setBackgroundColor(ContextCompat.getColor(this@CropImageActivity, R.color.colorAccent))
            }
        }
        finally {
            bitmap?.recycle()
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
            try {
                binding.cropImageView.crop(compressedImage?.toUri())?.execute(mCropCallback)
            }catch (e : Exception){
                e.printStackTrace()
            }
        }
        override fun onError(e: Throwable) {}
    }

    private val mCropCallback: CropCallback = object : CropCallback {
        override fun onSuccess(cropped: Bitmap) {
             Utils.Log(TAG,"Crop success ${cropped.width} ${cropped.height}")
            lifecycleScope.launch(Dispatchers.IO) {
                //onRenderCode(cropped)
                if (isChangeDesign){
                   bitmapChangeDesign = cropped
                }else{
                    handleScan(cropped)
                }
            }
        }
        override fun onError(e: Throwable) {
            Utils.Log(TAG,"Crop error")
        }
    }

    private fun handleScan(cropped: Bitmap){
        val resultText: String
        var resultPoints: List<PointF>? = null
        readerCpp.options = BarcodeReader.Options(
            formats = setOf(),
            tryHarder = true,
            tryRotate = true,
            tryInvert = false,
            tryDownscale = false
        )
        var mBitmap : Bitmap? = null
        try {
            mBitmap = createBlackAndWhite(cropped)
            mBitmap?.let { mBmFilter ->
                var mResultData = readerCpp.read(mBmFilter,Rect(0, 0, mBmFilter.width, mBmFilter.height))
                if (mResultData==null){
                    mBitmap = Bitmap.createScaledBitmap(cropped,800,800,true)
                    mBitmap?.let { mBmScale ->
                        mResultData = readerCpp.read(mBmScale,Rect(0, 0, mBmScale.width, mBmScale.height))
                        if (mResultData!=null){
                            Utils.Log(TAG,"Found data...")
                        }else{
                            Utils.Log(TAG,"Not found data...")
                        }
                    }
                }
                if (mResultData!=null){
                    resultText = try {
                        resultPoints = mResultData?.position?.let {
                            listOf(
                                it.topLeft,
                                it.topRight,
                                it.bottomRight,
                                it.bottomLeft
                            ).map { p ->
                                p.toPointF()
                            }
                        }
                        (mResultData?.let {
                            val mResultPoint = Array(1) { i ->
                                ResultPoint(
                                    (resultPoints?.get(0)?.x ?: 0).toFloat(),
                                    (resultPoints?.get(0)?.y ?: 0).toFloat()
                                )
                            }
                            val mResult = Result(
                                it.text,
                                it.bytes,
                                mResultPoint,
                                it.format.cppFormatToJavaFormat()
                            )

                            val parsedResult = ResultParser.parseResult(mResult)
                            if (parsedResult != null) {
                                lifecycleScope.launch(Dispatchers.Main){
                                    binding.doneCancelBar.btnDone.isEnabled = true
                                    binding.doneCancelBar.btnDone.setBackgroundColor(ContextCompat.getColor(this@CropImageActivity, R.color.colorPrimary))
                                    setResultEncode(mResult)
                                    onParseData(mResult)
                                }
                            }
                            "${it.format} (${it.contentType}): " +
                                    "${
                                        if (it.contentType != BarcodeReader.ContentType.BINARY) it.text else it.bytes!!.joinToString(
                                            separator = ""
                                        ) { v -> "%02x".format(v) }
                                    }"
                        }
                            ?: "")
                    } catch (e: Throwable) {
                        lifecycleScope.launch(Dispatchers.Main){
                            binding.doneCancelBar.btnDone.isEnabled = false
                            binding.doneCancelBar.btnDone.setBackgroundColor(ContextCompat.getColor(this@CropImageActivity, R.color.colorAccent))
                            binding.tvFormatType.text = ""
                        }
                        e.message ?: "Error"
                    }
                    Utils.Log(TAG,"Result text $resultText")
                }else{
                    lifecycleScope.launch(Dispatchers.Main){
                        binding.doneCancelBar.btnDone.isEnabled = false
                        binding.doneCancelBar.btnDone.setBackgroundColor(ContextCompat.getColor(this@CropImageActivity, R.color.colorAccent))
                        binding.tvFormatType.text = ""
                    }
                }
            }
        }catch (e : Exception){
            e.printStackTrace()
        }
        finally {
            mBitmap?.recycle()
            cropped.recycle()
        }
    }
    private fun createBlackAndWhite(src: Bitmap): Bitmap? {
        val width = src.width
        val height = src.height
        val bmOut = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val factor = 255f
        val redBri = 0.2126f
        val greenBri = 0.2126f
        val blueBri = 0.0722f
        val length = width * height
        val mInPixels = IntArray(length)
        val mOutPixels = IntArray(length)
        src.getPixels(mInPixels, 0, width, 0, 0, width, height)
        for ((point, pix) in mInPixels.withIndex()) {
            val mR = pix shr 16 and 0xFF
            val mG = pix shr 8 and 0xFF
            val mB = pix and 0xFF
            val lum = redBri * mR / factor + greenBri * mG / factor + blueBri * mB / factor
            if (lum > 0.4) {
                mOutPixels[point] = -0x1
            } else {
                mOutPixels[point] = -0x1000000
            }
        }
        bmOut.setPixels(mOutPixels, 0, width, 0, 0, width, height)
        return bmOut
    }

    private val mMoveUpCallback: MoveUpCallback
        get() = object : MoveUpCallback {
            override fun onSuccess(width: Int, height: Int,rectF: RectF) {
            }
            override fun onError(e: Throwable) {}
            override fun onDown() {
            }
            override fun onRelease() {
                try {
                    binding.cropImageView.crop(compressedImage?.toUri())?.execute(mCropCallback)
                }catch (e: Exception){
                    e.printStackTrace()
                }
            }
        }
    private fun showLoading() {
        dialog?.show()
    }

    private fun dismissLoading() {
        dialog?.dismiss()
    }
}