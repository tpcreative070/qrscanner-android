package tpcreative.co.qrscanner.common.extension

import android.content.Context
import android.view.Surface
import android.view.WindowManager
import com.google.gson.Gson
import tpcreative.co.qrscanner.common.ConstantKey
import tpcreative.co.qrscanner.common.Utils


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