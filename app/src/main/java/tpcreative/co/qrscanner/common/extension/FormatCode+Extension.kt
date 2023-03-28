package tpcreative.co.qrscanner.common.extension

import com.google.zxing.BarcodeFormat
import com.zxingcpp.BarcodeReader


fun BarcodeReader.Format.cppFormatToJavaFormat() : BarcodeFormat {
    if (this == BarcodeReader.Format.DATA_BAR){
        return BarcodeFormat.RSS_14
    }else if (this== BarcodeReader.Format.DATA_BAR_EXPANDED){
        return BarcodeFormat.RSS_EXPANDED
    }else{
        return BarcodeFormat.valueOf(this.name)
    }
}