package tpcreative.co.qrscanner.model

class ChangeDesignModel  : java.io.Serializable {
    var logo : LogoModel? = null
    var color : ColorModel? = null
    var positionMarker : PositionMarkerModel? = null
    var body : BodyModel? = null
    constructor(data : ChangeDesignModel){
        this.logo  =  data.logo
        this.color = data.color
        this.positionMarker = data.positionMarker
        this.body = data.body
    }
    constructor(){

    }
}