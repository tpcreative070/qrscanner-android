package tpcreative.co.qrscanner.common.view.crop
import android.content.*
import android.net.Uri
import android.provider.MediaStore
import tpcreative.co.qrscanner.common.extension.serializable
import tpcreative.co.qrscanner.ui.cropimage.CropImageActivity

/**
 * Builder for crop Intents and utils for handling result
 */
class Crop private constructor(source: Uri?, destination: Uri?) {
    internal interface Extra {
        companion object {
            val ASPECT_X: String = "aspect_x"
            val ASPECT_Y: String = "aspect_y"
            val MAX_X: String = "max_x"
            val MAX_Y: String = "max_y"
            val AS_PNG: String = "as_png"
            val ERROR: String = "error"
        }
    }

    private val cropIntent: Intent?

    /**
     * Set fixed aspect ratio for crop area
     *
     * @param x Aspect X
     * @param y Aspect Y
     */
    fun withAspect(x: Int, y: Int): Crop {
        cropIntent?.putExtra(Extra.ASPECT_X, x)
        cropIntent?.putExtra(Extra.ASPECT_Y, y)
        return this
    }

    /**
     * Crop area with fixed 1:1 aspect ratio
     */
    fun asSquare(): Crop {
        cropIntent?.putExtra(Extra.ASPECT_X, 1)
        cropIntent?.putExtra(Extra.ASPECT_Y, 1)
        return this
    }

    /**
     * Send the crop Intent with a custom request code
     *
     * @param context     Context
     * @param fragment    Fragment to receive result
     * @param requestCode requestCode for result
     */
    /**
     * Send the crop Intent with a custom request code
     *
     * @param context     Context
     * @param fragment    Fragment to receive result
     * @param requestCode requestCode for result
     */
    /**
     * Send the crop Intent from a support library Fragment
     *
     * @param context  Context
     * @param fragment Fragment to receive result
     */
    @JvmOverloads
    fun start(context: Context) : Intent? {
       return getIntent(context)
    }

    /**
     * Get Intent to start crop Activity
     *
     * @param context Context
     * @return Intent for CropImageActivity
     */
    private fun getIntent(context: Context): Intent? {
        cropIntent?.setClass(context, CropImageActivity::class.java)
        return cropIntent
    }

    companion object {
        const val REQUEST_CROP = 6709
        const val REQUEST_PICK = 9162
        const val RESULT_ERROR = 404
        val REQUEST_DATA: String = "DATA"

        /**
         * Create a crop Intent builder with source and destination image Uris
         *
         * @param source      Uri for image to crop
         * @param destination Uri for saving the cropped image
         */
        fun of(source: Uri?, destination: Uri?): Crop? {
            return Crop(source, destination)
        }

        fun getOutputString(result: Intent?): String? {
            return result?.getStringExtra(REQUEST_DATA)
        }

        /**
         * Retrieve error that caused crop to fail
         *
         * @param result Result Intent
         * @return Throwable handled in CropImageActivity
         */
        fun getError(result: Intent?): Throwable? {
            return result?.serializable(Extra.ERROR,Throwable::class.java)
        }

        @JvmOverloads
        fun getImagePicker(): Intent {
            return Intent(Intent.ACTION_GET_CONTENT).setType("image/*")
        }
    }

    init {
        cropIntent = Intent()
        cropIntent.data = source
        cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, destination)
    }
}