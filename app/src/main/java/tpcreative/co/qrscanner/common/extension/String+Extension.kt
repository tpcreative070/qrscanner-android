package tpcreative.co.qrscanner.common.extension

import com.google.gson.Gson
import tpcreative.co.qrscanner.common.ConstantKey
import tpcreative.co.qrscanner.common.Utils


fun String.stringToArray() : List<String>{
    val mStr = this.split(" ").toTypedArray()
    return mStr.toList()
}

fun String.stringToMap() : Map<String,String>{
    val mMap = HashMap<String,String>()
    val lastName : String?
    val firstName : String?
    val middleName : String?
    if (this.isEmpty()){
        return mMap
    }

    val parts  = this.split(" ").toMutableList()
    firstName = parts.firstOrNull()
    if (parts.size>0){
        parts.removeAt(0)
    }
    middleName = parts.firstOrNull()
    if (parts.size>0){
        parts.removeAt(0)
    }
    lastName = parts.joinToString(" ")

    mMap[ConstantKey.MIDDLE_NAME] = middleName ?: ""
    mMap[ConstantKey.FIRST_NAME] = firstName ?: ""
    mMap[ConstantKey.LAST_NAME] = lastName ?: ""
    Utils.Log("stringToMap",Gson().toJson(mMap))
    return mMap
}