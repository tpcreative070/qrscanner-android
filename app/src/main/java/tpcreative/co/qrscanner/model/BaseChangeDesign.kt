package tpcreative.co.qrscanner.model

open class BaseChangeDesign() : java.io.Serializable {
    var type : EnumChangeDesignType = EnumChangeDesignType.NORMAL
}

enum class EnumChangeDesignType {
    VIP,NORMAL
}