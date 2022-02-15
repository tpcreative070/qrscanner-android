package tpcreative.co.qrscanner.common.view.crop
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.opengl.GLES10
import android.os.*
import android.provider.MediaStore
import android.view.*
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import kotlinx.android.synthetic.main.crop_layout_done_cancel.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.view.crop.Crop.Extra
import tpcreative.co.qrscanner.common.view.crop.CropImageView.ListenerState
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.CountDownLatch

internal class CropImageActivity : MonitoredActivity(), ListenerState {
    private val handler: Handler = Handler(Looper.getMainLooper())
    private var aspectX = 0
    private var aspectY = 0

    // Output image
    private var maxX = 0
    private var maxY = 0
    private var exifRotation = 0
    private var sourceUri: Uri? = null
    private var isSaving = false
    private var sampleSize = 0
    private var rotateBitmap: RotateBitmap? = null
    private var imageView: CropImageView? = null
    private var cropView: HighlightView? = null
    private var isProgressing = false
    public override fun onCreate(icicle: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(icicle)
        setupViews()
        loadInput()
        if (rotateBitmap == null) {
            finish()
            return
        }
        startCrop()
        btn_done.isEnabled = false
        btn_done.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent))
    }

    private fun setupViews() {
        setContentView(R.layout.crop_activity_crop)
        imageView = findViewById<View?>(R.id.crop_image) as CropImageView
        imageView?.mContext = this
        imageView?.setRecycler(object : ImageViewTouchBase.Recycler {
            override fun recycle(b: Bitmap?) {
                b?.recycle()
                System.gc()
            }
        })
        btn_cancel.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
       btn_done.setOnClickListener { finish() }
    }

    private fun loadInput() {
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            aspectX = extras.getInt(Extra.ASPECT_X)
            aspectY = extras.getInt(Extra.ASPECT_Y)
            maxX = extras.getInt(Extra.MAX_X)
            maxY = extras.getInt(Extra.MAX_Y)
        }
        sourceUri = intent.data
        if (sourceUri != null) {
            exifRotation = CropUtil.getExifRotation(CropUtil.getFromMediaUri(this, contentResolver, sourceUri))
            var mInputStream: InputStream? = null
            try {
                sampleSize = calculateBitmapSampleSize(sourceUri)
                mInputStream = contentResolver.openInputStream(sourceUri!!)
                val option = BitmapFactory.Options()
                option.inSampleSize = sampleSize
                rotateBitmap = RotateBitmap(BitmapFactory.decodeStream(mInputStream, null, option), exifRotation)
            } catch (e: IOException) {
                Log.e("Error reading image: " + e.message, e)
                setResultException(e)
            } catch (e: OutOfMemoryError) {
                Log.e("OOM reading image: " + e.message, e)
                setResultException(e)
            } finally {
                CropUtil.closeSilently(mInputStream)
            }
        }
    }

    @Throws(IOException::class)
    private fun calculateBitmapSampleSize(bitmapUri: Uri?): Int {
        var mInputStream: InputStream? = null
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        try {
            mInputStream = bitmapUri?.let { contentResolver.openInputStream(it) }
            BitmapFactory.decodeStream(mInputStream, null, options) // Just get image size
        } finally {
            CropUtil.closeSilently(mInputStream)
        }
        val maxSize = getMaxImageSize()
        var sampleSize = 1
        while (options.outHeight / sampleSize > maxSize || options.outWidth / sampleSize > maxSize) {
            sampleSize = sampleSize shl 1
        }
        return sampleSize
    }

    private fun getMaxImageSize(): Int {
        val textureLimit = getMaxTextureSize()
        return if (textureLimit == 0) {
            SIZE_DEFAULT
        } else {
            textureLimit.coerceAtMost(SIZE_LIMIT)
        }
    }

    private fun getMaxTextureSize(): Int {
        // The OpenGL texture size is the maximum size that can be drawn in an ImageView
        val maxSize = IntArray(1)
        GLES10.glGetIntegerv(GLES10.GL_MAX_TEXTURE_SIZE, maxSize, 0)
        return maxSize[0]
    }

    private fun startCrop() {
        if (isFinishing) {
            return
        }
        imageView?.setListenerState(this)
        imageView?.setImageRotateBitmapResetBase(rotateBitmap, true)
        CropUtil.startBackgroundJob(this, null, resources.getString(R.string.crop__wait),
                {
                    val latch = CountDownLatch(1)
                    handler.post(Runnable {
                        if (imageView?.getScale() == 1f) {
                            imageView?.center()
                        }
                        latch.countDown()
                    })
                    try {
                        latch.await()
                    } catch (e: InterruptedException) {
                        throw RuntimeException(e)
                    }
                    Cropper().crop()
                }, handler
        )
    }

    private inner class Cropper {
        private fun makeDefault() {
            if (rotateBitmap == null) {
                return
            }
            val hv = HighlightView(imageView)
            val width = rotateBitmap?.getWidth() ?:0
            val height = rotateBitmap?.getHeight() ?:0
            val imageRect = Rect(0, 0, width, height)
            // Make the default size about 4/5 of the width or height
            var cropWidth = (width ?: 0).coerceAtMost(height ?: 0) * 4 / 5
            var cropHeight = cropWidth
            if (aspectX != 0 && aspectY != 0) {
                if (aspectX > aspectY) {
                    cropHeight = cropWidth * aspectY / aspectX
                } else {
                    cropWidth = cropHeight * aspectX / aspectY
                }
            }
            val x = (width - cropWidth) / 2
            val y = (height - cropHeight) / 2
            var cropRect = RectF(x.toFloat(), y.toFloat(), x + cropWidth.toFloat(), y + cropHeight.toFloat())
            if (width>height){
                 //Detect is barcode
                 cropRect = RectF(1f, 1f, width.toFloat() - 1, height.toFloat() - 1)
            }
            hv.setup(imageView?.getUnrotatedMatrix(), imageRect, cropRect, aspectX != 0 && aspectY != 0)
            imageView?.add(hv)
            Utils.Log(TAG,"with: $width, height: $height")
        }

        fun crop() {
            handler.post(Runnable {
                makeDefault()
                imageView?.invalidate()
                if (imageView?.highlightViews?.size == 1) {
                    cropView = imageView?.highlightViews?.get(0)
                    cropView?.setFocus(true)
                }
            })
        }
    }

    private fun decodeRegionCrop(rect: Rect?, outWidth: Int, outHeight: Int): Bitmap? {
        // Release memory now
//        clearImageView();
        var rect = rect
        var mInput: InputStream? = null
        var croppedImage: Bitmap? = null
        try {
            mInput = sourceUri?.let { contentResolver.openInputStream(it) }
            val decoder = mInput?.let { BitmapRegionDecoder.newInstance(it, false) }
            val width = decoder?.width?.toFloat()
            val height = decoder?.height?.toFloat()
            if (exifRotation != 0) {
                // Adjust crop area to account for image rotation
                val matrix = Matrix()
                matrix.setRotate(-exifRotation.toFloat())
                val adjusted = RectF()
                matrix.mapRect(adjusted, RectF(rect))
                // Adjust to account for origin at 0,0
                (if (adjusted.left < 0) width else 0.toFloat())?.let { (if (adjusted.top < 0) height else 0.toFloat())?.let { it1 ->
                    adjusted.offset(it,
                        it1
                    )
                } }
                rect = Rect(adjusted.left.toInt(), adjusted.top.toInt(), adjusted.right.toInt(), adjusted.bottom.toInt())
            }
            try {
                croppedImage = decoder?.decodeRegion(rect, BitmapFactory.Options())
                if (croppedImage != null && (rect?.width()!! > outWidth || rect.height() > outHeight)) {
                    val matrix = Matrix()
                    matrix.postScale(outWidth.toFloat() / rect.width(), outHeight.toFloat() / rect.height())
                    croppedImage = Bitmap.createBitmap(croppedImage, 0, 0, croppedImage.width, croppedImage.height, matrix, true)
                }
            } catch (e: IllegalArgumentException) {
                // Rethrow with some extra information
                throw IllegalArgumentException("Rectangle " + rect + " is outside of the image ("
                        + width + "," + height + "," + exifRotation + ")", e)
            }
        } catch (e: IOException) {
            Log.e("Error cropping image: " + e.message, e)
            setResultException(e)
        } catch (e: OutOfMemoryError) {
            Log.e("OOM cropping image: " + e.message, e)
            setResultException(e)
        } finally {
            CropUtil.closeSilently(mInput)
        }
        return croppedImage
    }

    private fun clearImageView() {
        imageView?.clear()
        if (rotateBitmap != null) {
            rotateBitmap?.recycle()
        }
        System.gc()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (rotateBitmap != null) {
            rotateBitmap?.recycle()
        }
    }

    override fun onSearchRequested(): Boolean {
        return false
    }

    fun isSaving(): Boolean {
        return isSaving
    }

    private fun setResultUri(uri: Uri?) {
        setResult(RESULT_OK, Intent().putExtra(MediaStore.EXTRA_OUTPUT, uri))
    }

    private fun setResultEncode(encode: Result?) {
        setResult(RESULT_OK, Intent().putExtra(Crop.REQUEST_DATA, Gson().toJson(encode)))
    }

    private fun setResultException(throwable: Throwable?) {
        setResult(Crop.RESULT_ERROR, Intent().putExtra(Extra.ERROR, throwable))
    }

    override fun isProgressingCropImage(): Boolean {
        return isProgressing
    }

    override fun onRequestCropImage() {
        Utils.Log(TAG, "onRequestCropImage")
        handleCropping()
    }

    private fun handleCropping() {
        if (cropView == null || isSaving) {
            return
        }
        isSaving = true
        isProgressing = true
        val croppedImage: Bitmap?
        val r = cropView?.getScaledCropRect(sampleSize.toFloat())
        val width = r?.width()?.toFloat() ?:0F
        val height = r?.height()?.toFloat() ?:0F
        var outWidth = width
        var outHeight = height
        if (maxX > 0 && maxY > 0 && (width > maxX || height > maxY)) {
            val ratio = width / height
            if (maxX.toFloat() / maxY.toFloat() > ratio) {
                outHeight = maxY.toFloat()
                outWidth = (maxY.toFloat() * ratio + .5f)
            } else {
                outWidth = maxX.toFloat()
                outHeight = (maxX.toFloat() / ratio + .5f)
            }
        }
        croppedImage = try {
            decodeRegionCrop(r, outWidth.toInt(), outHeight.toInt())
        } catch (e: IllegalArgumentException) {
            setResultException(e)
            finish()
            return
        }
        onRenderCode(croppedImage)
    }

    private fun onRenderCode(bitmap: Bitmap?) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                if (bitmap == null) {
                    isSaving = false
                    isProgressing = false
                    return@launch
                }
                val intArray = IntArray(bitmap.width * bitmap.height)
                bitmap.getPixels(intArray, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
                val source: LuminanceSource = RGBLuminanceSource(bitmap.width, bitmap.height, intArray)
                val mBitmap = BinaryBitmap(HybridBinarizer(source))
                val reader: Reader = MultiFormatReader()
                try {
                    val mResult = reader.decode(mBitmap)
                    if (mResult != null) {
                        isSaving = false
                        isProgressing = false
                        btn_done.isEnabled = true
                        btn_done.setBackgroundColor(ContextCompat.getColor(this@CropImageActivity, R.color.colorPrimary))
                        setResultEncode(mResult)
                    } else {
                        isSaving = false
                        isProgressing = false
                        btn_done.isEnabled = false
                        btn_done.setBackgroundColor(ContextCompat.getColor(this@CropImageActivity, R.color.colorAccent))
                    }
                } catch (e: NotFoundException) {
                    e.printStackTrace()
                    Utils.Log(TAG, "Do not recognize qrcode type")
                    isSaving = false
                    isProgressing = false
                    btn_done.isEnabled = false
                    btn_done.setBackgroundColor(ContextCompat.getColor(this@CropImageActivity, R.color.colorAccent))
                } catch (e: ChecksumException) {
                    e.printStackTrace()
                    Utils.Log(TAG, "Do not recognize qrcode type")
                    isSaving = false
                    isProgressing = false
                    btn_done.isEnabled = false
                    btn_done.setBackgroundColor(ContextCompat.getColor(this@CropImageActivity, R.color.colorAccent))
                }
            } catch (e: FormatException) {
                e.printStackTrace()
                Utils.Log(TAG, "Do not recognize qrcode type")
                isSaving = false
                isProgressing = false
                btn_done.isEnabled = false
                btn_done.setBackgroundColor(ContextCompat.getColor(this@CropImageActivity, R.color.colorAccent))
            }
        }
    }

    override fun onBackPressed() {
        setResult(RESULT_CANCELED)
        finish()
        super.onBackPressed()
    }

    companion object {
        private val TAG = CropImageActivity::class.java.simpleName
        private const val SIZE_DEFAULT = 2048
        private const val SIZE_LIMIT = 4096
    }
}