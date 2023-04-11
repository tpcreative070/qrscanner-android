package tpcreative.co.qrscanner.common.extension

import android.app.KeyguardManager
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView


val View.screenLocation get(): IntArray {
    val point = IntArray(2)
    getLocationOnScreen(point)
    return point
}

fun View.screenLocationSafe(callback: (Int, Int) -> Unit) {
    post {
        val (x, y) = screenLocation
        callback(x, y)
    }
}


fun View.addRipple() = with(TypedValue()) {
    context.theme.resolveAttribute(android.R.attr.selectableItemBackground, this, true)
    setBackgroundResource(resourceId)
}

fun View.addCircleRipple() = with(TypedValue()) {
    context.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, this, true)
    setBackgroundResource(resourceId)
}

fun View.rotateBitmap(context: Context, angle: Int?, color :Int): Bitmap? {
    val keyguardManager: KeyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    val orientation = context.resources.configuration.orientation
    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
        val matrix = Matrix()
        val mPaint = Paint()
        mPaint.color = color
        mPaint.style = Paint.Style.FILL
        matrix.postRotate(angle?.toFloat() ?:0F)
        val mBitmap = Bitmap.createBitmap(100,100, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(mBitmap);
        canvas.drawBitmap(mBitmap, matrix,mPaint)
        return mBitmap
    } else {
        val mBitmap = Bitmap.createBitmap(100,100, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(mBitmap);
        canvas.drawColor(color)
        return mBitmap
    }
}

fun View.margin(left: Float? = null, top: Float? = null, right: Float? = null, bottom: Float? = null) {
    layoutParams<ViewGroup.MarginLayoutParams> {
        left?.run { leftMargin = dpToPx(this) }
        top?.run { topMargin = dpToPx(this) }
        right?.run { rightMargin = dpToPx(this) }
        bottom?.run { bottomMargin = dpToPx(this) }
    }
}

inline fun <reified T : ViewGroup.LayoutParams> View.layoutParams(block: T.() -> Unit) {
    if (layoutParams is T) block(layoutParams as T)
}

fun View.dpToPx(dp: Float): Int = context.dpToPx(dp)
fun Context.dpToPx(dp: Float): Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics).toInt()

fun RecyclerView.autoFitColumns(columnWidth: Int) {
    val displayMetrics = this.context.resources.displayMetrics
    val noOfColumns = ((displayMetrics.widthPixels / displayMetrics.density) / columnWidth).toInt()
    this.layoutManager = GridLayoutManager(this.context, noOfColumns)
}

fun View.layout(@LayoutRes id : Int) :View{
    val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val v = inflater.inflate(id, null)
    return v
}

inline fun View.afterMeasured(crossinline block: () -> Unit) {
    if (measuredWidth > 0 && measuredHeight > 0) {
        block()
    } else {
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (measuredWidth > 0 && measuredHeight > 0) {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    block()
                }
            }
        })
    }
}

fun View.changeWith(width : Int){
    this.layoutParams = LinearLayout.LayoutParams(width, height)
}

fun View.changeHeight(height : Int){
    this.layoutParams = LinearLayout.LayoutParams(width, height)
}

fun View.change(width : Int,height : Int){
    this.layoutParams = LinearLayout.LayoutParams(width, height)
}