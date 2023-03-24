package tpcreative.co.qrscanner.ui.scanner.cpp

import android.content.Context
import android.graphics.Bitmap
import androidx.core.content.ContextCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix

class BarcodeEncoder {
	fun createBitmap(matrix: BitMatrix): Bitmap {
		val width = matrix.width
		val height = matrix.height
		val pixels = IntArray(width * height)
		for (y in 0 until height) {
			val offset = y * width
			for (x in 0 until width) {
				pixels[offset + x] = if (matrix[x, y]) BLACK else WHITE
			}
		}
		val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
		return bitmap
	}

	fun createBitmap(context: Context?, res: Int, matrix: BitMatrix): Bitmap {
		val width = matrix.width
		val height = matrix.height
		val pixels = IntArray(width * height)
		for (y in 0 until height) {
			val offset = y * width
			for (x in 0 until width) {
				pixels[offset + x] = if (matrix[x, y]) ContextCompat.getColor(context!!, res) else WHITE
			}
		}
		val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
		return bitmap
	}

	@Throws(WriterException::class)
	fun encode(contents: String?, format: BarcodeFormat?, width: Int, height: Int): BitMatrix {
		return try {
			MultiFormatWriter().encode(contents, format, width, height)
		} catch (e: WriterException) {
			throw e
		} catch (e: Exception) {
			// ZXing sometimes throws an IllegalArgumentException
			throw WriterException(e)
		}
	}

	@Throws(WriterException::class)
	fun encode(
		contents: String?,
		format: BarcodeFormat?,
		width: Int,
		height: Int,
		hints: Map<EncodeHintType?, *>?
	): BitMatrix {
		return try {
			MultiFormatWriter().encode(contents, format, width, height, hints)
		} catch (e: WriterException) {
			throw e
		} catch (e: Exception) {
			throw WriterException(e)
		}
	}

	@Throws(WriterException::class)
	fun encodeBitmap(contents: String?, format: BarcodeFormat?, width: Int, height: Int): Bitmap {
		return createBitmap(encode(contents, format, width, height))
	}

	@Throws(WriterException::class)
	fun encodeBitmap(
		contents: String?,
		format: BarcodeFormat?,
		width: Int,
		height: Int,
		hints: Map<EncodeHintType?, *>?
	): Bitmap {
		return createBitmap(encode(contents, format, width, height, hints))
	}

	@Throws(WriterException::class)
	fun encodeBitmap(
		context: Context?,
		res: Int,
		contents: String?,
		format: BarcodeFormat?,
		width: Int,
		height: Int,
		hints: Map<EncodeHintType?, *>?
	): Bitmap {
		return createBitmap(context, res, encode(contents, format, width, height, hints))
	}

	companion object {
		private const val WHITE = -0x1
		private const val BLACK = -0x1000000
	}
}