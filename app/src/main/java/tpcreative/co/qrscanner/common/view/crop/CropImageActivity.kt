package tpcreative.co.qrscanner.common.view.crop

import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.opengl.GLES10
import android.os.*
import android.provider.MediaStore
import android.view.*
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import butterknife.BindView
import com.google.gson.Gson
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.view.crop.Crop.Extra
import tpcreative.co.qrscanner.common.view.crop.CropImageView.ListenerState
import tpcreative.co.qrscanner.common.view.crop.ImageViewTouchBase
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch

class CropImageActivity : MonitoredActivity(), ListenerState {
    private val handler: Handler? = Handler(Looper.getMainLooper())
    var compositeDisposable: CompositeDisposable? = CompositeDisposable()
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

    @BindView(R.id.btn_done)
    var layoutDone: FrameLayout? = null
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
        layoutDone.setEnabled(false)
        layoutDone.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent))
    }

    private fun setupViews() {
        setContentView(R.layout.crop_activity_crop)
        imageView = findViewById<View?>(R.id.crop_image) as CropImageView
        imageView.context = this
        imageView.setRecycler(ImageViewTouchBase.Recycler { b ->
            b.recycle()
            System.gc()
        })
        findViewById<View?>(R.id.btn_cancel).setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
        findViewById<View?>(R.id.btn_done).setOnClickListener { finish() }
    }

    private fun loadInput() {
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            aspectX = extras.getInt(Extra.Companion.ASPECT_X)
            aspectY = extras.getInt(Extra.Companion.ASPECT_Y)
            maxX = extras.getInt(Extra.Companion.MAX_X)
            maxY = extras.getInt(Extra.Companion.MAX_Y)
        }
        sourceUri = intent.data
        if (sourceUri != null) {
            exifRotation = CropUtil.getExifRotation(CropUtil.getFromMediaUri(this, contentResolver, sourceUri))
            var `is`: InputStream? = null
            try {
                sampleSize = calculateBitmapSampleSize(sourceUri)
                `is` = contentResolver.openInputStream(sourceUri)
                val option = BitmapFactory.Options()
                option.inSampleSize = sampleSize
                rotateBitmap = RotateBitmap(BitmapFactory.decodeStream(`is`, null, option), exifRotation)
            } catch (e: IOException) {
                Log.e("Error reading image: " + e.message, e)
                setResultException(e)
            } catch (e: OutOfMemoryError) {
                Log.e("OOM reading image: " + e.message, e)
                setResultException(e)
            } finally {
                CropUtil.closeSilently(`is`)
            }
        }
    }

    @Throws(IOException::class)
    private fun calculateBitmapSampleSize(bitmapUri: Uri?): Int {
        var `is`: InputStream? = null
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        try {
            `is` = contentResolver.openInputStream(bitmapUri)
            BitmapFactory.decodeStream(`is`, null, options) // Just get image size
        } finally {
            CropUtil.closeSilently(`is`)
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
            Math.min(textureLimit, SIZE_LIMIT)
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
        imageView.setListenerState(this)
        imageView.setImageRotateBitmapResetBase(rotateBitmap, true)
        CropUtil.startBackgroundJob(this, null, resources.getString(R.string.crop__wait),
                {
                    val latch = CountDownLatch(1)
                    handler.post(Runnable {
                        if (imageView.getScale() == 1f) {
                            imageView.center()
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
            val width = rotateBitmap.getWidth()
            val height = rotateBitmap.getHeight()
            val imageRect = Rect(0, 0, width, height)
            // Make the default size about 4/5 of the width or height
            var cropWidth = Math.min(width, height) * 4 / 5
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
            val cropRect = RectF(x, y, x + cropWidth, y + cropHeight)
            hv.setup(imageView.getUnrotatedMatrix(), imageRect, cropRect, aspectX != 0 && aspectY != 0)
            imageView.add(hv)
        }

        fun crop() {
            handler.post(Runnable {
                makeDefault()
                imageView.invalidate()
                if (imageView.highlightViews.size == 1) {
                    cropView = imageView.highlightViews[0]
                    cropView.setFocus(true)
                }
            })
        }
    }

    private fun decodeRegionCrop(rect: Rect?, outWidth: Int, outHeight: Int): Bitmap? {
        // Release memory now
//        clearImageView();
        var rect = rect
        var `is`: InputStream? = null
        var croppedImage: Bitmap? = null
        try {
            `is` = contentResolver.openInputStream(sourceUri)
            val decoder = BitmapRegionDecoder.newInstance(`is`, false)
            val width = decoder.width
            val height = decoder.height
            if (exifRotation != 0) {
                // Adjust crop area to account for image rotation
                val matrix = Matrix()
                matrix.setRotate(-exifRotation.toFloat())
                val adjusted = RectF()
                matrix.mapRect(adjusted, RectF(rect))

                // Adjust to account for origin at 0,0
                adjusted.offset(if (adjusted.left < 0) width else 0.toFloat(), if (adjusted.top < 0) height else 0.toFloat())
                rect = Rect(adjusted.left as Int, adjusted.top as Int, adjusted.right as Int, adjusted.bottom as Int)
            }
            try {
                croppedImage = decoder.decodeRegion(rect, BitmapFactory.Options())
                if (croppedImage != null && (rect.width() > outWidth || rect.height() > outHeight)) {
                    val matrix = Matrix()
                    matrix.postScale(outWidth as Float / rect.width(), outHeight as Float / rect.height())
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
            CropUtil.closeSilently(`is`)
        }
        return croppedImage
    }

    private fun clearImageView() {
        imageView.clear()
        if (rotateBitmap != null) {
            rotateBitmap.recycle()
        }
        System.gc()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (rotateBitmap != null) {
            rotateBitmap.recycle()
        }
        compositeDisposable.dispose()
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
        setResult(RESULT_OK, Intent().putExtra(Crop.Companion.REQUEST_DATA, Gson().toJson(encode)))
    }

    private fun setResultException(throwable: Throwable?) {
        setResult(Crop.Companion.RESULT_ERROR, Intent().putExtra(Extra.Companion.ERROR, throwable))
    }

    override fun isProgressingCropImage(): Boolean {
        return isProgressing
    }

    override fun onRequestCropImage() {
        Utils.Log(TAG, "onRequestCropImage")
        handleCropping()
    }

    fun handleCropping() {
        if (cropView == null || isSaving) {
            return
        }
        isSaving = true
        isProgressing = true
        val croppedImage: Bitmap?
        val r = cropView.getScaledCropRect(sampleSize.toFloat())
        val width = r.width()
        val height = r.height()
        var outWidth = width
        var outHeight = height
        if (maxX > 0 && maxY > 0 && (width > maxX || height > maxY)) {
            val ratio = width as Float / height as Float
            if (maxX as Float / maxY as Float > ratio) {
                outHeight = maxY
                outWidth = (maxY as Float * ratio + .5f) as Int
            } else {
                outWidth = maxX
                outHeight = (maxX as Float / ratio + .5f) as Int
            }
        }
        croppedImage = try {
            decodeRegionCrop(r, outWidth, outHeight)
        } catch (e: IllegalArgumentException) {
            setResultException(e)
            finish()
            return
        }
        onRenderCode(croppedImage)
    }

    fun onRenderCode(bitmap: Bitmap?) {
        compositeDisposable.add(Observable.fromCallable(Callable {
            try {
                if (bitmap == null) {
                    isSaving = false
                    isProgressing = false
                    return@Callable ""
                }
                val intArray = IntArray(bitmap.width * bitmap.height)
                bitmap.getPixels(intArray, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
                val source: LuminanceSource = RGBLuminanceSource(bitmap.width, bitmap.height, intArray)
                val mBitmap = BinaryBitmap(HybridBinarizer(source))
                val reader: Reader = MultiFormatReader()
                try {
                    val result = reader.decode(mBitmap)
                    Utils.Log(TAG, "This is type of qrcode")
                    return@Callable Gson().toJson(result)
                } catch (e: NotFoundException) {
                    e.printStackTrace()
                    Utils.Log(TAG, "Do not recognize qrcode type")
                    return@Callable ""
                } catch (e: ChecksumException) {
                    e.printStackTrace()
                    Utils.Log(TAG, "Do not recognize qrcode type")
                    return@Callable ""
                }
            } catch (e: FormatException) {
                e.printStackTrace()
                Utils.Log(TAG, "Do not recognize qrcode type")
                return@Callable ""
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe { response: String? ->
                    val mResult = Gson().fromJson(response, Result::class.java)
                    if (mResult != null) {
                        isSaving = false
                        isProgressing = false
                        layoutDone.setEnabled(true)
                        layoutDone.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
                        setResultEncode(mResult)
                    } else {
                        isSaving = false
                        isProgressing = false
                        layoutDone.setEnabled(false)
                        layoutDone.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent))
                    }
                })
        Utils.Log(TAG, "onRenderCode")
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