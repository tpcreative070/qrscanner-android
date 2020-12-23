package tpcreative.co.qrscanner.model

enum class EnumThemeMode(ord: Int) {
    LIGHT(0), DARK(1), DEFAULT(2);

    var positionOrdinal = 0

    companion object {
        fun byPosition(ord: Int): EnumThemeMode? {
            return values()[ord] // less safe
        }
    }

    init {
        positionOrdinal = ord
    }
}