package tpcreative.co.qrscanner.common.view.crop
import android.annotation.SuppressLint
import android.content.*
import android.graphics.*
import android.os.Build
import android.util.TypedValue
import android.view.*
import androidx.core.content.ContextCompat
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.extension.avoidNAN
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import kotlin.math.abs
import kotlin.math.roundToInt

internal class HighlightView(  // View displaying image
        private val viewContext: View?) {
    internal enum class ModifyMode {
        None, Move, Grow
    }

    internal enum class HandleMode {
        Changing, Always, Never
    }

    var cropRect // Image space
            : RectF? = null
    var drawRect // Screen space
            : Rect? = null
    var matrix: Matrix? = null
    private var imageRect // Image space
            : RectF? = null
    private val outsidePaint: Paint = Paint()
    private val outlinePaint: Paint = Paint()
    private val handlePaint: Paint = Paint()
    private var showThirds = false
    private var showCircle = false
    private var highlightColor = 0
    private var modifyMode: ModifyMode? = ModifyMode.Grow
    private var handleMode: HandleMode? = HandleMode.Changing
    private var maintainAspectRatio = false
    private var initialAspectRatio = 0f
    private var handleRadius = 0f
    private var outlineWidth = 0f
    private var isFocused = false
    private fun initStyles(context: Context?) {
        val outValue = TypedValue()
        context?.theme?.resolveAttribute(R.attr.cropImageStyle, outValue, true)
        val attributes = context?.obtainStyledAttributes(outValue.resourceId, R.styleable.CropImageView)
        try {
            showThirds = attributes?.getBoolean(R.styleable.CropImageView_showThirds, false) == true
            showCircle = attributes?.getBoolean(R.styleable.CropImageView_showCircle, false) == true
            highlightColor = attributes?.getColor(R.styleable.CropImageView_highlightColor,
                    DEFAULT_HIGHLIGHT_COLOR) ?:0
            handleMode = HandleMode.values()[attributes?.getInt(R.styleable.CropImageView_showHandles, 0) ?:0]
        } finally {
            attributes?.recycle()
        }
    }

    fun setup(m: Matrix?, imageRect: Rect?, cropRect: RectF?, maintainAspectRatio: Boolean) {
        matrix = Matrix(m)
        this.cropRect = cropRect
        this.imageRect = RectF(imageRect)
        this.maintainAspectRatio = maintainAspectRatio
        initialAspectRatio = (this.cropRect?.width() ?: 0F) / (this.cropRect?.height() ?:0F)
        drawRect = computeLayout()
        outsidePaint.setARGB(125, 50, 50, 50)
        outlinePaint.style = Paint.Style.STROKE
        outlinePaint.isAntiAlias = true
        outlineWidth = dpToPx(OUTLINE_DP)
        handlePaint.color = ContextCompat.getColor(QRScannerApplication.getInstance(), R.color.colorAccent)
        handlePaint.style = Paint.Style.FILL
        handlePaint.isAntiAlias = true
        handleRadius = dpToPx(HANDLE_RADIUS_DP)
        modifyMode = ModifyMode.Grow
    }

    private fun dpToPx(dp: Float): Float {
        return dp * (viewContext?.resources?.displayMetrics?.density ?: 0F)
    }

    fun draw(canvas: Canvas?) {
        canvas?.save()
        val path = Path()
        outlinePaint.strokeWidth = outlineWidth
        if (!hasFocus()) {
            outlinePaint.color = Color.BLACK
            drawRect?.let { canvas?.drawRect(it, outlinePaint) }
        } else {
            val viewDrawingRect = Rect()
            viewContext?.getDrawingRect(viewDrawingRect)
            path.addRect(RectF(drawRect), Path.Direction.CW)
            outlinePaint.color = highlightColor
            if (isClipPathSupported(canvas)) {
                canvas?.clipPath(path, Region.Op.DIFFERENCE)
                canvas?.drawRect(viewDrawingRect, outsidePaint)
            } else {
                drawOutsideFallback(canvas)
            }
            canvas?.restore()
            canvas?.drawPath(path, outlinePaint)
            if (showThirds) {
                drawThirds(canvas)
            }
            if (showCircle) {
                drawCircle(canvas)
            }
            if (handleMode == HandleMode.Always ||
                    handleMode == HandleMode.Changing && modifyMode == ModifyMode.Grow) {
                drawHandles(canvas)
            }
        }
    }

    /*
     * Fall back to naive method for darkening outside crop area
     */
    private fun drawOutsideFallback(canvas: Canvas?) {
        canvas?.drawRect(0f, 0f, canvas.width.toFloat(), drawRect?.top?.toFloat() ?: 0F, outsidePaint)
        canvas?.drawRect(0f, drawRect?.bottom?.toFloat() ?: 0F, canvas.width.toFloat(), canvas.height.toFloat(), outsidePaint)
        canvas?.drawRect(0f, drawRect?.top?.toFloat() ?: 0F, drawRect?.left?.toFloat() ?:0F, drawRect?.bottom?.toFloat() ?: 0F, outsidePaint)
        canvas?.drawRect(drawRect?.right?.toFloat() ?: 0F, drawRect?.top?.toFloat() ?: 0F, canvas.width.toFloat(), drawRect?.bottom?.toFloat() ?: 0F, outsidePaint)
    }

    /*
     * Clip path is broken, unreliable or not supported on:
     * - JellyBean MR1
     * - ICS & ICS MR1 with hardware acceleration turned on
     */
    @SuppressLint("NewApi")
    private fun isClipPathSupported(canvas: Canvas?): Boolean {
        return if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN_MR1) {
            false
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH
                || Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            true
        } else {
            !(canvas?.isHardwareAccelerated ?: false)
        }
    }

    private fun drawHandles(canvas: Canvas?) {
        val xMiddle = (drawRect?.left ?:0) + ((drawRect?.right ?:0) - (drawRect?.left ?:0)) / 2
        val yMiddle = (drawRect?.top ?:0) + ((drawRect?.bottom ?:0) - (drawRect?.top ?:0)) / 2
        canvas?.drawCircle(drawRect?.left?.toFloat() ?: 0F, yMiddle.toFloat(), handleRadius, handlePaint)
        canvas?.drawCircle(xMiddle.toFloat(), drawRect?.top?.toFloat() ?:0F, handleRadius, handlePaint)
        canvas?.drawCircle(drawRect?.right?.toFloat() ?:0F, yMiddle.toFloat(), handleRadius, handlePaint)
        canvas?.drawCircle(xMiddle.toFloat(), drawRect?.bottom?.toFloat() ?:0F, handleRadius, handlePaint)
    }

    private fun drawThirds(canvas: Canvas?) {
        outlinePaint.strokeWidth = 1f
        val xThird = (((drawRect?.right ?:0) - (drawRect?.left ?:0)) / 3).toFloat()
        val yThird = (((drawRect?.bottom ?:0) - (drawRect?.top ?:0)) / 3).toFloat()
        canvas?.drawLine((drawRect?.left ?:0) + xThird, (drawRect?.top?.toFloat() ?:0F),
                (drawRect?.left ?:0) + xThird, (drawRect?.bottom?.toFloat() ?:0F), outlinePaint)
        canvas?.drawLine((drawRect?.left ?:0) + xThird * 2, (drawRect?.top?.toFloat() ?:0F),
                (drawRect?.left ?:0) + xThird * 2, (drawRect?.bottom?.toFloat() ?:0F), outlinePaint)
        canvas?.drawLine((drawRect?.left?.toFloat() ?:0F), (drawRect?.top ?:0) + yThird,
                (drawRect?.right?.toFloat() ?:0F), (drawRect?.top ?:0) + yThird, outlinePaint)
        canvas?.drawLine((drawRect?.left?.toFloat() ?:0F), (drawRect?.top ?:0) + yThird * 2,
                (drawRect?.right?.toFloat() ?:0F), (drawRect?.top ?:0) + yThird * 2, outlinePaint)
    }

    private fun drawCircle(canvas: Canvas?) {
        outlinePaint.strokeWidth = 1f
        canvas?.drawOval(RectF(drawRect), outlinePaint)
    }

    fun setMode(mode: ModifyMode?) {
        if (mode != modifyMode) {
            modifyMode = mode
            viewContext?.invalidate()
        }
    }

    // Determines which edges are hit by touching at (x, y)
    fun getHit(x: Float, y: Float): Int {
        val r = computeLayout()
        val hysteresis = 20f
        var retval = GROW_NONE

        // verticalCheck makes sure the position is between the top and
        // the bottom edge (with some tolerance). Similar for horizCheck.
        val verticalCheck = (y >= r.top - hysteresis
                && y < r.bottom + hysteresis)
        val horizCheck = (x >= (r.left) - hysteresis
                && x < (r.right) + hysteresis)

        // Check whether the position is near some edge(s)
        if (abs((r.left) - x) < hysteresis && verticalCheck) {
            retval = retval or GROW_LEFT_EDGE
        }
        if (abs((r.right) - x) < hysteresis && verticalCheck) {
            retval = retval or GROW_RIGHT_EDGE
        }
        if (abs((r.top) - y) < hysteresis && horizCheck) {
            retval = retval or GROW_TOP_EDGE
        }
        if (abs((r.bottom) - y) < hysteresis && horizCheck) {
            retval = retval or GROW_BOTTOM_EDGE
        }

        // Not near any edge but inside the rectangle: move
        if (retval == GROW_NONE && r.contains(x.toInt(), y.toInt())) {
            retval = MOVE
        }
        return retval
    }

    // Handles motion (dx, dy) in screen space.
    // The "edge" parameter specifies which edges the user is dragging.
    fun handleMotion(edge: Int, dx: Float, dy: Float) {
        var dx = dx
        var dy = dy
        val r = computeLayout()
        if (edge == MOVE) {
            // Convert to image space before sending to moveBy()
            moveBy(dx * ((cropRect?.width() ?:0F).div(r.width())),
                    dy * ((cropRect?.height() ?:0F).div(r.height())))
        } else {
            if (GROW_LEFT_EDGE or GROW_RIGHT_EDGE and edge == 0) {
                dx = 0f
            }
            if (GROW_TOP_EDGE or GROW_BOTTOM_EDGE and edge == 0) {
                dy = 0f
            }

            // Convert to image space before sending to growBy()
            val xDelta = dx * ((cropRect?.width() ?:0F) / r.width())
            val yDelta = dy * (cropRect?.height() ?:0F) / r.height()
            growBy((if (edge and GROW_LEFT_EDGE != 0) -1 else 1) * xDelta,
                    (if (edge and GROW_TOP_EDGE != 0) -1 else 1) * yDelta)
        }
    }

    // Grows the cropping rectangle by (dx, dy) in image space
    fun moveBy(dx: Float, dy: Float) {
        val invalRect = Rect(drawRect)
        cropRect?.offset(dx, dy)

        // Put the cropping rectangle inside image rectangle
        cropRect?.offset(
                0f.coerceAtLeast((imageRect?.left ?: 0F) - (cropRect?.left ?: 0F)),
                0f.coerceAtLeast((imageRect?.top ?: 0F) - (cropRect?.top ?: 0F)))
        cropRect?.offset(
                0f.coerceAtMost((imageRect?.right ?: 0F) - (cropRect?.right ?: 0F)),
                0f.coerceAtMost((imageRect?.bottom ?: 0F) - (cropRect?.bottom ?: 0F)))
        drawRect = computeLayout()
        drawRect?.let { invalRect.union(it) }
        invalRect.inset(-handleRadius.toInt(), -handleRadius.toInt())
        viewContext?.invalidate(invalRect)
    }

    // Grows the cropping rectangle by (dx, dy) in image space.
    fun growBy(dx: Float, dy: Float) {
        var dx = dx
        var dy = dy
        if (maintainAspectRatio) {
            if (dx != 0f) {
                dy = dx / initialAspectRatio
            } else if (dy != 0f) {
                dx = dy * initialAspectRatio
            }
        }

        // Don't let the cropping rectangle grow too fast.
        // Grow at most half of the difference between the image rectangle and
        // the cropping rectangle.
        val r = RectF(cropRect)
        if (dx > 0f && r.width() + 2 * dx > (imageRect?.width() ?:0F)) {
            dx = ((imageRect?.width() ?:0F) - r.width()) / 2f
            if (maintainAspectRatio) {
                dy = dx / initialAspectRatio
            }
        }
        if (dy > 0f && r.height() + 2 * dy > (imageRect?.height() ?:0F)) {
            dy = ((imageRect?.height() ?:0F) - r.height()) / 2f
            if (maintainAspectRatio) {
                dx = dy * initialAspectRatio
            }
        }
        r.inset(-dx, -dy)

        // Don't let the cropping rectangle shrink too fast
        val widthCap = 25f
        if (r.width() < widthCap) {
            r.inset(-(widthCap - r.width()) / 2f, 0f)
        }
        val heightCap = if (maintainAspectRatio) widthCap / initialAspectRatio else widthCap
        if (r.height() < heightCap) {
            r.inset(0f, -(heightCap - r.height()) / 2f)
        }

        // Put the cropping rectangle inside the image rectangle
        if (r.left < (imageRect?.left ?: 0F)) {
            r.offset((imageRect?.left ?: 0F) - r.left, 0f)
        } else if (r.right > (imageRect?.right ?: 0F)) {
            r.offset(-(r.right - (imageRect?.right ?:0F)), 0f)
        }
        if (r.top < (imageRect?.top ?:0F)) {
            r.offset(0f, (imageRect?.top ?:0F) - r.top)
        } else if (r.bottom > (imageRect?.bottom ?:0F)) {
            r.offset(0f, -(r.bottom - (imageRect?.bottom ?:0F)))
        }
        cropRect?.set(r)
        drawRect = computeLayout()
        viewContext?.invalidate()
    }

    // Returns the cropping rectangle in image space with specified scale
    fun getScaledCropRect(scale: Float): Rect {
        return Rect(((cropRect?.left ?:0F) * scale).toInt(), ((cropRect?.top ?:0F) * scale).toInt(),
                ((cropRect?.right ?:0F) * scale).toInt(), ((cropRect?.bottom ?:0F) * scale).toInt())
    }

    // Maps the cropping rectangle from image space to screen space
    private fun computeLayout(): Rect {
        val r = RectF((cropRect?.left ?:0F), (cropRect?.top ?:0F),
                (cropRect?.right ?:0F), (cropRect?.bottom ?:0F))
        matrix?.mapRect(r)
        return Rect(r.left.avoidNAN().roundToInt(), r.top.avoidNAN().roundToInt(),
                r.right.avoidNAN().roundToInt(), r.bottom.avoidNAN().roundToInt())
    }

    fun invalidate() {
        drawRect = computeLayout()
    }

    fun hasFocus(): Boolean {
        return isFocused
    }

    fun setFocus(isFocused: Boolean) {
        this.isFocused = isFocused
    }

    companion object {
        const val GROW_NONE = 1 shl 0
        const val GROW_LEFT_EDGE = 1 shl 1
        const val GROW_RIGHT_EDGE = 1 shl 2
        const val GROW_TOP_EDGE = 1 shl 3
        const val GROW_BOTTOM_EDGE = 1 shl 4
        const val MOVE = 1 shl 5
        private val DEFAULT_HIGHLIGHT_COLOR = ContextCompat.getColor(QRScannerApplication.Companion.getInstance(), R.color.colorPrimary)
        private const val HANDLE_RADIUS_DP = 15f
        private const val OUTLINE_DP = 2f
    }

    init {
        initStyles(viewContext?.context)
    }
}