package tpcreative.co.qrscanner.common.extension

import android.content.Context
import android.graphics.Color
import android.view.Surface
import android.view.WindowManager
import androidx.core.graphics.toColorInt
import com.google.gson.Gson
import com.panshen.gridcolorpicker.LogUtil
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Constant
import tpcreative.co.qrscanner.common.ConstantKey
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.model.EnumImage
import vadiole.colorpicker.hexColor
import java.io.File
import java.util.regex.Matcher
import java.util.regex.Pattern


fun String.stringToMap() : Map<String,String>{
    val mMap = HashMap<String,String>()
    val lastName : String?
    val firstName : String?
    val middleName : String?
    val suffixName : String?
    if (this.isEmpty()){
        return mMap
    }

    val parts  = this.split(" ").toMutableList()
    val partsSuffix = this.split(", ")
    firstName = parts.firstOrNull()?.replace(",","")
    if (parts.size>0){
        parts.removeAt(0)
    }
    middleName = parts.firstOrNull()?.replace(",","")
    if (parts.size>0){
        parts.removeAt(0)
    }

    lastName = parts.firstOrNull()?.replace(",","")
    if (parts.size>0){
        parts.removeAt(0)
    }

    if (partsSuffix.size>1){
        suffixName  = partsSuffix.lastOrNull()?.replace(",","")?.replace(" ","")
    }else{
        suffixName  = ""
    }

    mMap[ConstantKey.MIDDLE_NAME] = middleName ?: ""
    mMap[ConstantKey.FIRST_NAME] = firstName ?: ""
    mMap[ConstantKey.LAST_NAME] = lastName ?: ""
    mMap[ConstantKey.SUFFIX_NAME] = suffixName ?: ""
    Utils.Log("stringToMap",Gson().toJson(mMap))
    return mMap
}

fun String.isSpecialCharacters() : Boolean{
    val p: Pattern = Pattern.compile("[^A-Za-z0-9]", Pattern.CASE_INSENSITIVE)
    val m: Matcher = p.matcher(this)
    return m.find()
}

fun String.createFolder(){
    File(this).mkdirs()
}

fun String.findImageName(enum: EnumImage) : File?{
    val imageFolder = QRScannerApplication.getInstance().getPathFolder()?.let { File(it) }
    var mFile = File(imageFolder, "$this shared_design_qr_code.png")
    if (enum == EnumImage.LOGO){
        mFile = File(imageFolder, "$this shared_design_logo_code.png")
    }
    if (enum == EnumImage.QR_TEMPLATE){
        mFile = File(imageFolder, "$this shared_design_template_code.png")
    }
    if (mFile.exists()){
        return mFile
    }
    return null
}

val String.changedDesignColor : String get() = Utils.getChangedDesignColor() ?: ""

val String.hexWithoutTransparent : String get() =  this.toColorInt().hexColor

val String.putChangedDesignColor: Unit
    get() = Utils.setChangedDesignColor(this)


fun String.toColorIntThrowDefaultColor() : Int{
    return try {
        if (this.isEmpty()){
            Utils.Log("TAG","Color result 1")
            return Constant.defaultColor
        }
        Utils.Log("TAG","Color result 2 $this")
        this.toColorInt()
    }
    catch (e: Exception){
        Utils.Log("TAG","Color result 3 $this")
        Constant.defaultColor
    }
}


fun String.toLogConsole(tag: String? = null){
    LogUtil.d(tag ?: "TAG_LOG",this)
}
