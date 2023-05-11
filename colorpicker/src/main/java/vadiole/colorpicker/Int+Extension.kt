package vadiole.colorpicker

val Int.hexColor get() = String.format("#%06X", 0xFFFFFF and this)

//val Int.hexWithAlphaColor get() = String.format("#%08X", 0xFFFFFFFF and this)

val Int.hexWithAlphaColor get() = java.lang.String.format("#%08X", -0x1 and this)
