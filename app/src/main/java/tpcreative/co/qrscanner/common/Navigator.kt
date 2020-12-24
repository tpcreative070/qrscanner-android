package tpcreative.co.qrscanner.common
import android.app.Activity
import android.content.*
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.common.view.Bungee
import tpcreative.co.qrscanner.model.*
import tpcreative.co.qrscanner.ui.backup.BackupActivity
import tpcreative.co.qrscanner.ui.filecolor.ChangeFileColorActivity
import tpcreative.co.qrscanner.ui.help.HelpActivity
import tpcreative.co.qrscanner.ui.main.MainActivity
import tpcreative.co.qrscanner.ui.pro.ProVersionActivity
import tpcreative.co.qrscanner.ui.review.ReviewActivity
import tpcreative.co.qrscanner.ui.seeyousoon.SeeYouSoonActivity

object Navigator {
    const val CREATE = 1000
    const val SCANNER = 1001
    const val REQUEST_CODE_EMAIL = 1007
    const val REQUEST_CODE_EMAIL_ANOTHER_ACCOUNT = 1008
    fun onMoveToReview(context: Activity?, create: Create?) {
        val intent = Intent(context, ReviewActivity::class.java)
        val bundle = Bundle()
        bundle.putSerializable(QRScannerApplication.Companion.getInstance().getString(R.string.key_create_intent), create)
        intent.putExtras(bundle)
        context?.startActivityForResult(intent, CREATE)
    }

    fun onMoveToHelp(context: Context?) {
        val intent = Intent(context, HelpActivity::class.java)
        context?.startActivity(intent)
    }

    fun onMoveToChangeFileColor(context: Context?) {
        val intent = Intent(context, ChangeFileColorActivity::class.java)
        context?.startActivity(intent)
    }

    fun onMoveProVersion(context: Context?) {
        val intent = Intent(context, ProVersionActivity::class.java)
        context?.startActivity(intent)
    }

    fun <T> onGenerateView(context: Activity?, save: SaveModel?, clazz: Class<T?>?) {
        val intent = Intent(context, clazz)
        val bundle = Bundle()
        bundle.putSerializable(QRScannerApplication.Companion.getInstance().getString(R.string.key_data), save)
        intent.putExtras(bundle)
        context?.startActivity(intent)
    }

    fun <T> onResultView(context: Activity?, save: Create?, clazz: Class<T?>?) {
        val intent = Intent(context, clazz)
        val bundle = Bundle()
        bundle.putSerializable(QRScannerApplication.Companion.getInstance().getString(R.string.key_data), save)
        intent.putExtras(bundle)
        context?.startActivityForResult(intent, SCANNER)
    }

    fun onMoveMainTab(context: AppCompatActivity?) {
        val intent = Intent(context, MainActivity::class.java)
        context?.startActivity(intent)
        Bungee.fade(context)
        context?.finish()
    }

    fun onMoveSeeYouSoon(context: AppCompatActivity?) {
        val intent = Intent(context, SeeYouSoonActivity::class.java)
        context?.startActivity(intent)
        Bungee.fade(context)
        context?.finish()
    }

    fun onBackupData(context: Context?) {
        val intent = Intent(context, BackupActivity::class.java)
        context?.startActivity(intent)
    }
}