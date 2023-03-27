package tpcreative.co.qrscanner.common.extension

import android.content.Context
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.fragment.app.Fragment
import tpcreative.co.qrscanner.common.Utils


fun Context.isLandscape():Boolean{
    val currentOrientation = resources.configuration.orientation
    return (currentOrientation == Configuration.ORIENTATION_LANDSCAPE)
}


fun Context.pxToDp(px : Float):Float{
    return px * resources.displayMetrics.density
}

fun Context.dpToPx_(dp: Float) : Float{
    return dp * resources.displayMetrics.density
}

fun Context.pxToSp(px: Float): Float {
    val scaledDensity = resources.displayMetrics.scaledDensity
    return px / scaledDensity
}

fun Context.isOnline(): Boolean {
    val connectivityManager =
        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val capabilities =
        connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    if (capabilities != null) {
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            Utils.Log("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
            return true
        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            Utils.Log("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
            return true
        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
            Utils.Log("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
            return true
        }
    }
    return false
}