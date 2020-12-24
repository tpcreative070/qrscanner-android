package tpcreative.co.qrscanner.model
import java.io.Serializable

class TypeCategories : Serializable {
    var id: Int?
    var mType: String?

    fun getId(): Int {
        return id ?: 0
    }

    fun setId(id: Int) {
        this.id = id
    }

    fun getType(): String? {
        return mType
    }

    fun setType(type: String?) {
        this.mType = type
    }

    constructor() {
        id = 0
        mType = ""
    }

    constructor(id: Int, type: String?) {
        this.id = id
        this.mType = type
    }
}