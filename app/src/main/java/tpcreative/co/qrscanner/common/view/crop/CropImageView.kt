package tpcreative.co.qrscanner.common.view.crop

import android.content.*
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.view.crop.CropImageView
import tpcreative.co.qrscanner.common.view.crop.HighlightView.ModifyMode
import java.util.*

class CropImageView : ImageViewTouchBase {
    var highlightViews: ArrayList<HighlightView?>? = ArrayList()
    var motionHighlightView: HighlightView? = null
    var context: Context? = null
    private var lastX = 0f
    private var lastY = 0f
    private var motionEdge = 0
    private var validPointerId = 0
    private var listenerState: ListenerState? = null

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {}

    fun setListenerState(listenerState: ListenerState?) {
        this.listenerState = listenerState
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (bitmapDisplayed.bitmap != null) {
            for (hv in highlightViews) {
                hv.matrix.set(unrotatedMatrix)
                hv.invalidate()
                if (hv.hasFocus()) {
                    centerBasedOnHighlightView(hv)
                }
            }
        }
    }

    override fun zoomTo(scale: Float, centerX: Float, centerY: Float) {
        super.zoomTo(scale, centerX, centerY)
        for (hv in highlightViews) {
            hv.matrix.set(unrotatedMatrix)
            hv.invalidate()
        }
        Utils.Log(TAG, "zoomTo")
    }

    override fun zoomIn() {
        super.zoomIn()
        for (hv in highlightViews) {
            hv.matrix.set(unrotatedMatrix)
            hv.invalidate()
        }
        Utils.Log(TAG, "zoomIn")
    }

    override fun zoomOut() {
        super.zoomOut()
        for (hv in highlightViews) {
            hv.matrix.set(unrotatedMatrix)
            hv.invalidate()
        }
        Utils.Log(TAG, "zoomOut")
    }

    override fun postTranslate(deltaX: Float, deltaY: Float) {
        super.postTranslate(deltaX, deltaY)
        for (hv in highlightViews) {
            hv.matrix.postTranslate(deltaX, deltaY)
            hv.invalidate()
        }
        Utils.Log(TAG, "postTranslate")
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val cropImageActivity = context as CropImageActivity?
        if (cropImageActivity.isSaving()) {
            return false
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> for (hv in highlightViews) {
                val edge = hv.getHit(event.x, event.y)
                if (edge != HighlightView.Companion.GROW_NONE) {
                    motionEdge = edge
                    motionHighlightView = hv
                    lastX = event.x
                    lastY = event.y
                    // Prevent multiple touches from interfering with crop area re-sizing
                    validPointerId = event.getPointerId(event.actionIndex)
                    motionHighlightView.setMode(ModifyMode.Grow)
                    break
                }
            }
            MotionEvent.ACTION_UP -> {
                if (motionHighlightView != null) {
                    centerBasedOnHighlightView(motionHighlightView)
                    motionHighlightView.setMode(ModifyMode.Grow)
                }
                motionHighlightView = null
                center()
                if (listenerState != null) {
                    if (!listenerState.isProgressingCropImage()) {
                        listenerState.onRequestCropImage()
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (motionHighlightView != null && event.getPointerId(event.actionIndex) == validPointerId) {
                    motionHighlightView.handleMotion(motionEdge, event.x
                            - lastX, event.y - lastY)
                    lastX = event.x
                    lastY = event.y
                }
                // If we're not zoomed then there's no point in even allowing the user to move the image around.
                // This call to center puts it back to the normalized location.
                if (scale == 1f) {
                    center()
                }
            }
        }
        return true
    }

    // Pan the displayed image to make sure the cropping rectangle is visible.
    private fun ensureVisible(hv: HighlightView?) {
        val r = hv.drawRect
        val panDeltaX1 = Math.max(0, left - r.left)
        val panDeltaX2 = Math.min(0, right - r.right)
        val panDeltaY1 = Math.max(0, top - r.top)
        val panDeltaY2 = Math.min(0, bottom - r.bottom)
        val panDeltaX = if (panDeltaX1 != 0) panDeltaX1 else panDeltaX2
        val panDeltaY = if (panDeltaY1 != 0) panDeltaY1 else panDeltaY2
        if (panDeltaX != 0 || panDeltaY != 0) {
            panBy(panDeltaX.toFloat(), panDeltaY.toFloat())
        }
    }

    // If the cropping rectangle's size changed significantly, change the
    // view's center and scale according to the cropping rectangle.
    private fun centerBasedOnHighlightView(hv: HighlightView?) {
        val drawRect = hv.drawRect
        val width = drawRect.width().toFloat()
        val height = drawRect.height().toFloat()
        val thisWidth = getWidth().toFloat()
        val thisHeight = getHeight().toFloat()
        val z1 = thisWidth / width * .6f
        val z2 = thisHeight / height * .6f
        var zoom = Math.min(z1, z2)
        zoom = zoom * this.scale
        zoom = Math.max(1f, zoom)
        if (Math.abs(zoom - scale) / zoom > .1) {
            val coordinates = floatArrayOf(hv.cropRect.centerX(), hv.cropRect.centerY())
            unrotatedMatrix.mapPoints(coordinates)
            zoomTo(zoom, coordinates[0], coordinates[1], 300f)
        }
        ensureVisible(hv)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (highlightView in highlightViews) {
            highlightView.draw(canvas)
        }
    }

    fun add(hv: HighlightView?) {
        highlightViews.add(hv)
        invalidate()
    }

    interface ListenerState {
        open fun isProgressingCropImage(): Boolean
        open fun onRequestCropImage()
    }

    companion object {
        private val TAG = CropImageView::class.java.simpleName
    }
}