/*
* Copyright 2022 Axel Waggershauser
* Copyright 2022 Markus Fisch
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package tpcreative.co.qrscanner.ui.scanner.cpp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.media.Image
import android.util.AttributeSet
import android.view.View
import android.view.WindowManager
import androidx.camera.core.ImageProxy
import androidx.core.content.ContextCompat
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.ui.scanner.Size
import kotlin.math.max
import kotlin.math.min

class PreviewOverlay : View {
	private val TAG = this::class.java.simpleName
	// Size of this container, non-null after layout is performed
	private var containerSize: Size? = null

	// Framing rectangle relative to this view
	private var framingRect: Rect? = null

	// Framing rectangle relative to this default of view
	private var defaultFramingRect: Rect? = null

	// Size of the framing rectangle. If null, defaults to using a margin percentage.
	private var framingRectSize: Size? = null

	// Fraction of the width / heigth to use as a margin. This fraction is used on each size, so
	// must be smaller than 0.5;
	private val marginFraction = 0.2

	private var windowManager: WindowManager? = null

	protected var mBorderPaint: Paint? = null
	protected var mBorderLineLength = 0
	private val mDefaultBorderColor = ContextCompat.getColor(context, R.color.zxing_colorBlueLight)
	private val mDefaultBorderStrokeWidth =
		resources.getInteger(R.integer.zxing_viewfinder_border_width)
	private val mDefaultBorderLineLength =
		resources.getInteger(R.integer.zxing_viewfinder_border_length)


	constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
	}
	constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0) {
		val styledAttributes =
			getContext().obtainStyledAttributes(attrs, R.styleable.PreviewOverlay)
		windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
		val framingRectWidth = styledAttributes.getDimension(
			R.styleable.PreviewOverlay_preview_overlay_rect_width,
			-1f
		).toInt()
		val framingRectHeight = styledAttributes.getDimension(
			R.styleable.PreviewOverlay_preview_overlay_rect_height,
			-1f
		).toInt()

		if (framingRectWidth > 0 && framingRectHeight > 0) {
			framingRectSize = Size(framingRectWidth, framingRectHeight)
		}
		Utils.Log(TAG,"constructor width $framingRectWidth  height $framingRectHeight")
		//border paint

		//border paint
		mBorderPaint = Paint()
		mBorderPaint?.color = mDefaultBorderColor
		mBorderPaint?.style = Paint.Style.STROKE
		mBorderPaint?.strokeWidth = mDefaultBorderStrokeWidth.toFloat()
		mBorderPaint?.isAntiAlias = true
		mBorderLineLength = mDefaultBorderLineLength
		styledAttributes.recycle()
	}


	private val paintPath = Paint(Paint.ANTI_ALIAS_FLAG).apply {
		style = Paint.Style.FILL
		isAntiAlias = true
		isDither = true
		color = ContextCompat.getColor(context,R.color.colorAccentOverLay)
		strokeWidth = 2 * context.resources.displayMetrics.density
	}
	private val paintRect = Paint().apply {
		style = Paint.Style.STROKE
		color = 0x80ffffff.toInt()
		strokeWidth = 3 * context.resources.displayMetrics.density
	}
	private val path = Path()
	private var cropRect = Rect()
	private var rotation = 0
	private var s: Float = 0f
	private var o: Float = 0f

	fun update(viewFinder: View, image: ImageProxy, points: List<PointF>?) {
		cropRect = image.cropRect
		rotation = image.imageInfo.rotationDegrees
		Utils.Log(TAG,"Call here width ${viewFinder.width} height ${viewFinder.height} image ${image.height}")
		s = min(viewFinder.width, viewFinder.height).toFloat() / image.height
		o = (max(viewFinder.width, viewFinder.height) - (image.width * s).toInt()).toFloat() / 2
		path.apply {
			rewind()
			if (!points.isNullOrEmpty()) {
				moveTo(points.last().x, points.last().y)
				for (p in points)
					lineTo(p.x, p.y)
			}
		}
		invalidate()
	}

	private fun calculateFrames() {
		if (containerSize == null) {
			framingRect = null
			throw IllegalStateException("containerSize or previewSize is not set yet")
		}
		val container = Rect(0, 0, width, height)
		framingRect = calculateFramingRect(container)
		defaultFramingRect = calculateDefaultFramingRect(container)
		Utils.Log(TAG,"framingRect $framingRect")
		Utils.Log(TAG,"defaultFramingRect $defaultFramingRect")
	}

	fun setFrameRect(rect: Rect){
		this.framingRect = rect
	}

	fun getFrameRect() : Rect? {
		return framingRect
	}

	fun getDefaultFrameRect() : Rect? {
		return  defaultFramingRect
	}

	/**
	 * Calculate framing rectangle, relative to the preview frame.
	 *
	 * Note that the SurfaceView may be larger than the container.
	 *
	 * Override this for more control over the framing rect calculations.
	 *
	 * @param container this container, with left = top = 0
	 * @param surface   the SurfaceView, relative to this container
	 * @return the framing rect, relative to this container
	 */

	private fun calculateFramingRect(container: Rect?): Rect {
		// intersection is the part of the container that is used for the preview
		val intersection = Rect(container)
		if (framingRectSize != null) {
			// Specific size is specified. Make sure it's not larger than the container or surface.
			val horizontalMargin = Math.max(0, (intersection.width() - framingRectSize!!.width) / 2)
			val verticalMargin = Math.max(0, (intersection.height() - framingRectSize!!.height) / 2)
			intersection.inset(horizontalMargin, verticalMargin)
			return intersection
		}
		// margin as 10% (default) of the smaller of width, height
		val margin =
			Math.min(intersection.width() * marginFraction, intersection.height() * marginFraction)
				.toInt()
		intersection.inset(margin, margin)
		if (intersection.height() > intersection.width()) {
			// We don't want a frame that is taller than wide.
			intersection.inset(0, (intersection.height() - intersection.width()) / 2)
		}
		return intersection
	}

	/**
	 * Calculate framing rectangle, relative to the preview frame.
	 *
	 * Note that the SurfaceView may be larger than the container.
	 *
	 * Override this for more control over the framing rect calculations.
	 *
	 * @param container this container, with left = top = 0
	 * @param surface   the SurfaceView, relative to this container
	 * @return the framing rect, relative to this container
	 */

	private fun calculateDefaultFramingRect(container: Rect?): Rect {
		// intersection is the part of the container that is used for the preview
		val intersection = Rect(container)
		// margin as 10% (default) of the smaller of width, height
		val margin =
			Math.min(intersection.width() * marginFraction, intersection.height() * marginFraction)
				.toInt()
		intersection.inset(margin, margin)
		if (intersection.height() > intersection.width()) {
			// We don't want a frame that is taller than wide.
			intersection.inset(0, (intersection.height() - intersection.width()) / 2)
		}
		return intersection
	}

	@SuppressLint("DrawAllocation")
	override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
		super.onLayout(changed, left, top, right, bottom)
		val with = right - left
		val height = bottom - top
		containerSize = Size(with,height)
		calculateFrames()
		Utils.Log(TAG," Layout width $with  height $height")
	}

	override fun onDraw(canvas: Canvas) {
		canvas.apply {
			drawViewFinderBorder(canvas)
			// draw the cropRect, which is relative to the original image orientation
			save()
			if (rotation == 90) {
				translate(width.toFloat(), 0f)
				rotate(rotation.toFloat())
			}
			translate(o, 0F)
			scale(s, s)
			//drawRect(cropRect, paintRect)
			restore()

			// draw the path, which is relative to the (centered) rotated cropRect
			when (rotation) {
				0, 180 -> translate(o + cropRect.left * s, cropRect.top * s)
				90 -> translate(cropRect.top * s, o + cropRect.left * s)
			}
			scale(s,s)
			drawPath(path, paintPath)
			Utils.Log(TAG, "Call here")
		}
	}

	private fun drawViewFinderBorder(canvas: Canvas) {
		val framingRect = framingRect
		// Top-left corner
		val path = Path()
		path.moveTo(framingRect!!.left.toFloat(), (framingRect.top + mBorderLineLength).toFloat())
		path.lineTo(framingRect.left.toFloat(), framingRect.top.toFloat())
		path.lineTo((framingRect.left + mBorderLineLength).toFloat(), framingRect.top.toFloat())
		canvas.drawPath(path, mBorderPaint!!)

		// Top-right corner
		path.moveTo(framingRect.right.toFloat(), (framingRect.top + mBorderLineLength).toFloat())
		path.lineTo(framingRect.right.toFloat(), framingRect.top.toFloat())
		path.lineTo((framingRect.right - mBorderLineLength).toFloat(), framingRect.top.toFloat())
		canvas.drawPath(path, mBorderPaint!!)

		// Bottom-right corner
		path.moveTo(framingRect.right.toFloat(), (framingRect.bottom - mBorderLineLength).toFloat())
		path.lineTo(framingRect.right.toFloat(), framingRect.bottom.toFloat())
		path.lineTo((framingRect.right - mBorderLineLength).toFloat(), framingRect.bottom.toFloat())
		canvas.drawPath(path, mBorderPaint!!)

		// Bottom-left corner
		path.moveTo(framingRect.left.toFloat(), (framingRect.bottom - mBorderLineLength).toFloat())
		path.lineTo(framingRect.left.toFloat(), framingRect.bottom.toFloat())
		path.lineTo((framingRect.left + mBorderLineLength).toFloat(), framingRect.bottom.toFloat())
		canvas.drawPath(path, mBorderPaint!!)
	}

}
