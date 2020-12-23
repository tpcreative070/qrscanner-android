package tpcreative.co.qrscanner.ui.create

android.content.*
import androidx.preference.PreferenceViewHolder
import androidx.appcompat.widget.SwitchCompat
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import tpcreative.co.qrscanner.model.PremiumModel
import androidx.core.content.PermissionChecker
import android.media.MediaScannerConnection
import android.provider.DocumentsContract
import tpcreative.co.qrscanner.common.PathUtil
import tpcreative.co.qrscanner.ui.help.HelpActivity
import tpcreative.co.qrscanner.ui.filecolor.ChangeFileColorActivity
import tpcreative.co.qrscanner.common.view.Bungee
import tpcreative.co.qrscanner.common.MemoryConstants
import tpcreative.co.qrscanner.common.TimeConstants
import android.graphics.Bitmap.CompressFormat
import android.graphics.PixelFormat
import androidx.annotation.IntDef
import androidx.fragment.app.FragmentActivity
import tpcreative.co.qrscanner.common.PermissionUtils.PermissionDeniedDialog
import tpcreative.co.qrscanner.common.PermissionUtils.RationaleDialog
import tpcreative.co.qrscanner.helper.TimeHelper
import androidx.appcompat.app.AppCompatDelegate

interface GenerateView {
    open fun getContext(): Context?
    open fun onSetView()
    open fun onInitView()
}