package tpcreative.co.qrscanner.common
import android.app.Activity
import android.content.*
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.model.*
import tpcreative.co.qrscanner.ui.backup.BackupActivity
import tpcreative.co.qrscanner.ui.filecolor.ChangeFileColorActivity
import tpcreative.co.qrscanner.ui.intro.IntroActivity
import tpcreative.co.qrscanner.ui.help.HelpActivity
import tpcreative.co.qrscanner.ui.main.MainActivity
import tpcreative.co.qrscanner.ui.pro.ProVersionActivity
import tpcreative.co.qrscanner.ui.review.ReviewActivity

object Navigator {
    const val CREATE = 1000
    const val SCANNER = 1001
    const val REQUEST_CODE_EMAIL = 1007
    const val REQUEST_CODE_EMAIL_ANOTHER_ACCOUNT = 1008
    fun onMoveToReview(context: Activity?, create: GeneralModel?) {
        val intent = Intent(context, ReviewActivity::class.java)
        val bundle = Bundle()
        bundle.putSerializable(QRScannerApplication.Companion.getInstance().getString(R.string.key_data), create)
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

    fun onFromAdsMoveToProVersion(context: Context?) {
        val intent = Intent(context, ProVersionActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK;
        context?.startActivity(intent)
    }

    fun <T> onGenerateView(context: Activity?, save: GeneralModel?, clazz: Class<T>) {
        val intent = Intent(context, clazz)
        val bundle = Bundle()
        bundle.putSerializable(QRScannerApplication.Companion.getInstance().getString(R.string.key_data), save)
        intent.putExtras(bundle)
        context?.startActivity(intent)
    }

    fun <T> onResultView(context: Activity?, save: GeneralModel?, clazz: Class<T>) : Intent {
        val intent = Intent(context, clazz)
        val bundle = Bundle()
        bundle.putSerializable(QRScannerApplication.Companion.getInstance().getString(R.string.key_data), save)
        intent.putExtras(bundle)
        Utils.Log(Constant.LOG_TAKE_TIME,"Start")
        return  intent
    }

    fun onMoveMainTab(context: AppCompatActivity?) {
        val intent = Intent(context, MainActivity::class.java)
        context?.startActivity(intent)
        context?.finish()
    }

    fun onBackupData(context: Context?) {
        val intent = Intent(context, BackupActivity::class.java)
        context?.startActivity(intent)
    }

    fun onIntro(context: Activity?){
        val intent = Intent(context,IntroActivity::class.java)
        context?.startActivity(intent)
        context?.finish()
    }

    fun <T> onIntent(context: Context?, clazz: Class<T>){
        val intent = Intent(context, clazz)
        context?.startActivity(intent)
    }

    fun <T> onPopupView(context: Activity?, mMap : HashMap<EnumImage,String>, image : EnumImage, clazz: Class<T>) : Intent {
        val intent = Intent(context, clazz)
        val bundle = Bundle()
        bundle.putSerializable(ConstantKey.KEY_CHANGE_DESIGN_COLOR_MAP, mMap)
        bundle.putString(ConstantKey.KEY_CHANGE_DESIGN_COLOR_TYPE,image.name)
        intent.putExtras(bundle)
        return  intent
    }

    fun <T> onChangeDesignText(context: Activity, clazz: Class<T>, enumImage: EnumImage,mMap : HashMap<EnumImage,String>,mapText : HashMap<EnumImage,TextModel>, changeDesign : ChangeDesignModel,dataCode : String,uuId :String) : Intent {
        val intent = Intent(context, clazz)
        val bundle = Bundle()
        bundle.putString(ConstantKey.KEY_POPUP_TEXT_TEXT_TYPE,enumImage.name)
        bundle.putSerializable(ConstantKey.KEY_CHANGE_DESIGN_COLOR_MAP, mMap)
        bundle.putSerializable(ConstantKey.KEY_CHANGE_DESIGN_TEXT_MAP_TEXT,mapText)
        bundle.putSerializable(ConstantKey.KEY_CHANGE_DESIGN_TEXT,changeDesign)
        bundle.putString(ConstantKey.KEY_DATA_CODE, dataCode)
        bundle.putString(ConstantKey.KEY_DATA_UUID,uuId)
        intent.putExtras(bundle)
        return  intent
    }

    fun <T> onPremiumPopupView(context: Activity?, mData : ChangeDesignModel, typeShape: EnumShape, clazz: Class<T>, dataCode : String, uuId :String, enumFontSize: EnumFontSize = EnumFontSize.NONE, fontModel: FontModel = FontModel(), enumView: EnumView = EnumView.LOGO, index :Int = 0) : Intent {
        val intent = Intent(context, clazz)
        val bundle = Bundle()
        bundle.putSerializable(ConstantKey.KEY_PREMIUM_POPUP, mData)
        bundle.putString(ConstantKey.KEY_PREMIUM_POPUP_TYPE_SHAPE,typeShape.name)
        bundle.putString(ConstantKey.KEY_DATA_CODE, dataCode)
        bundle.putString(ConstantKey.KEY_DATA_UUID,uuId)
        bundle.putString(ConstantKey.KEY_PREMIUM_POPUP_ENUM_FONT_SIZE,enumFontSize.name)
        bundle.putString(ConstantKey.KEY_CHANGE_DESIGN_CURRENT_VIEW,enumView.name)
        bundle.putInt(ConstantKey.KEY_CHANGE_DESIGN_INDEX,index)
        bundle.putSerializable(ConstantKey.KEY_CHANGE_DESIGN_TEXT_OBJECT,fontModel)
        intent.putExtras(bundle)
        return  intent
    }
}