package com.github.alexzhirkevich.customqrgenerator.vector.dsl

import android.graphics.drawable.Drawable
import com.github.alexzhirkevich.customqrgenerator.style.*
import com.github.alexzhirkevich.customqrgenerator.vector.QrVectorOptions
import com.github.alexzhirkevich.customqrgenerator.vector.style.*

internal class InternalQrVectorLogoBuilderScope(
    val builder: QrVectorOptions.Builder,
) : QrVectorLogoBuilderScope {

    override var drawable: Drawable?
        get() = builder.logo.drawable
        set(value) = with(builder) {
            setLogo(logo.copy(drawable = value))
        }
    override var size: Float
        get() = builder.logo.size
        set(value) = with(builder) {
            setLogo(logo.copy(size = value))
        }

    override var padding: QrVectorLogoPadding
        get() = builder.logo.padding
        set(value) = with(builder) {
            setLogo(logo.copy(padding = value))
        }
    override var shape: QrVectorLogoShape
        get() = builder.logo.shape
        set(value) = with(builder) {
            setLogo(logo.copy(shape = value))
        }

    override var scale: BitmapScale
        get() = builder.logo.scale
        set(value) = with(builder) {
            setLogo(logo.copy(scale = value))
        }
    override var backgroundColor: QrVectorColor
        get() = builder.logo.backgroundColor
        set(value) = with(builder) {
            setLogo(logo.copy(backgroundColor = value))
        }
}