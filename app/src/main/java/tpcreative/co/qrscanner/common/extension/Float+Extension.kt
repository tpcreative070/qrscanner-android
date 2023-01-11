package tpcreative.co.qrscanner.common.extension

fun Float.avoidNAN() : Float{
    if (this.isNaN()){
        return 0F
    }
    return this
}