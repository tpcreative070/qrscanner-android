package tpcreative.co.qrscanner.model

class ChangeDesignModel  : java.io.Serializable {
    var logo : LogoModel? = null
    constructor(logo : ChangeDesignModel){
        this.logo = logo.logo
    }
    constructor(){

    }
}