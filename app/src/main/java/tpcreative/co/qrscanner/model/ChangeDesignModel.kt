package tpcreative.co.qrscanner.model

class ChangeDesignModel  : java.io.Serializable {
    var logo : LogoModel? = null
    var color : ColorModel? = null
    constructor(data : ChangeDesignModel){
        this.logo  =  data.logo
        this.color = data.color
    }
    constructor(){

    }
}