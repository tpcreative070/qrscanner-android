package tpcreative.co.qrscanner.model

import java.io.Serializable

class TypeCategories : Serializable {
    var id: Int
    var type: String?
    fun getId(): Int {
        return id
    }

    fun setId(id: Int) {
        this.id = id
    }

    fun getType(): String? {
        return type
    }

    fun setType(type: String?) {
        this.type = type
    }

    constructor() {
        id = 0
        type = ""
    }

    constructor(id: Int, type: String?) {
        this.id = id
        this.type = type
    }
}