package tpcreative.co.qrscanner.common.extension

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import tpcreative.co.qrscanner.BuildConfig
import tpcreative.co.qrscanner.common.Utils
import java.io.File


fun Context.isLandscape():Boolean{
    val currentOrientation = resources.configuration.orientation
    return (currentOrientation == Configuration.ORIENTATION_LANDSCAPE)
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

fun Context.openAppSystemSettings() {
    startActivity(Intent().apply {
        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        data = Uri.fromParts("package", packageName, null)
    })
}

fun Context.getUriForFile(file: File): Uri? {
    return FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file)
}

fun Context.getResource(name:String): Drawable? {
    val resID = this.resources.getIdentifier(name , "drawable", this.packageName)
    return ActivityCompat.getDrawable(this,resID)
}