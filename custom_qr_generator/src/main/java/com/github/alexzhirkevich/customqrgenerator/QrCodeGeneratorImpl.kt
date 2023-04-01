@file:Suppress("DEPRECATION")

package com.github.alexzhirkevich.customqrgenerator

import android.graphics.Bitmap
import androidx.core.graphics.alpha
import androidx.core.graphics.drawable.toBitmap
import com.github.alexzhirkevich.customqrgenerator.encoder.QrCodeMatrix
import com.github.alexzhirkevich.customqrgenerator.encoder.QrEncoder
import com.github.alexzhirkevich.customqrgenerator.encoder.QrRenderResult
import com.github.alexzhirkevich.customqrgenerator.style.*
import kotlinx.coroutines.*
import java.nio.charset.Charset
import kotlin.math.roundToInt

internal class QrCodeGeneratorImpl(
    private val threadPolicy: ThreadPolicy
) : QrCodeGenerator {

    override fun generateQrCode(data: QrData, options: QrOptions, charset: Charset?): Bitmap =
        runBlocking {
            kotlin.runCatching {
                createQrCodeInternal(data, options, charset)
            }.getOrElse {
                throw QrCodeCreationException(it)
            }
        }
    override suspend fun generateQrCodeSuspend(data: QrData, options: QrOptions, charset: Charset?): Bitmap =
        withContext(Dispatchers.Default) {
            kotlin.runCatching {
                createQrCodeInternal(data, options, charset)
            }.getOrElse {
                throw if (it is CancellationException)
                     it else QrCodeCreationException(cause = it)
            }
        }

    private suspend fun createQrCodeInternal(
        data: QrData, options: QrOptions, charset: Charset?
    ) : Bitmap {

        val encoder = QrEncoder(options.copy(
            errorCorrectionLevel = options.errorCorrectionLevel.fit(
                options.logo
            )))
        val result = encoder.encode(data, charset)

        val bmp = Bitmap.createBitmap(
            options.width, options.height,
            Bitmap.Config.ARGB_8888
        )

        return bmp.apply {
            drawCode(result, options)
        }
    }

    val colors = mutableMapOf<Pair<Int,Int>,Int>()

    private fun QrColor.getColor(
        i : Int, j : Int, width : Int, height : Int, pixelSize : Int
    ) = if (this is QrColorSeparatePixels) {
        val ri = i / pixelSize
        val rj = j / pixelSize
        colors[ri to rj] ?: invoke(ri, rj, width / pixelSize, height / pixelSize).also {
            colors[ri to rj] = it
        }
    } else invoke(i, j, width, height)


    private suspend fun Bitmap.drawCode(
        result: QrRenderResult,
        options: QrOptions,
        drawBg: Boolean = true,
        drawLogo : Boolean = true
    ) = coroutineScope {
        colors.clear()
        with(result) {

            val bgBitmap = options.background.drawable
                .takeIf { it !is EmptyDrawable && drawBg }
                ?.toBitmap(width, height, Bitmap.Config.ARGB_8888)

            val bgBitmapPixels = if (bgBitmap != null)
                IntArray(width * height) else null

            bgBitmap?.getPixels(bgBitmapPixels, 0,width,0,0,width, height)

            val offsetX = (paddingX * (1+ options.offset.x.coerceIn(-1f,1f))).roundToInt()
            val offsetY = (paddingY * (1+ options.offset.y.coerceIn(-1f,1f))).roundToInt()
            val array = IntArray(width*height)

            threadPolicy.invoke(width, height){ xrange, yrange ->

                if (drawBg){
                    for (x in xrange) {
                        for (y in yrange) {
                            val bitmapBgColor = options.background.color.invoke(
                                x, y,width, height
                            )
                            val bgColor =  bgBitmapPixels?.get(x + y * width)?.takeIf { it.alpha > 0 }
                                ?.let { QrUtil.mixColors(it, bitmapBgColor, it.alpha/255f * options.background.alpha) }
                                ?: bitmapBgColor

                            array[x + y * width] =bgColor
                        }
                    }
                }
                for (x in xrange) {
                    for (y in yrange) {
                        ensureActive()

                        val inCodeRange = x in paddingX until width - paddingX - error &&
                                y in paddingY until height - paddingY - error && options.shapes.highlighting
                            .invoke(
                                x - paddingX,
                                y - paddingY,
                                width -  2 * minOf(paddingX, paddingY),
                                Neighbors.Empty
                            )

                        if (inCodeRange){
                            val pixel = bitMatrix[x - paddingX, y - paddingY]

                            val realX = minOf(x - paddingX, width - x - error - paddingX)
                            val realY = minOf(y - paddingY, height  - y - error - paddingY)

                            val emptyCorner = width - x  < x && height - y < y

                            val bottom = height - y < y
                            val right = height - x < x

                            val idx  = x+error/2 - paddingX + offsetX +
                                    (y+error/2 - paddingY + offsetY) * width

                            val color = when {
                                pixel == QrCodeMatrix.PixelType.DarkPixel &&
                                        !emptyCorner && options.colors.ball !is QrColor.Unspecified &&
                                        ball.let {
                                            realX in it.x until it.x + it.size  &&
                                                    realY in it.y until it.y + it.size
                                        } -> options.colors.ball.invoke(
                                    i = (realX - ball.x).let {
                                         if (right && !options.colors.symmetry)
                                             ball.size - it else it
                                    },
                                    j= (realY-ball.y).let {
                                       if (bottom && !options.colors.symmetry)
                                           ball.size - it else it
                                    },
                                    width = ball.size,
                                    height = ball.size,
                                )

                                pixel == QrCodeMatrix.PixelType.DarkPixel &&
                                        !emptyCorner && options.colors.frame !is QrColor.Unspecified &&
                                        frame.let {
                                            realX in it.x until it.x + it.size &&
                                                    realY in it.y until it.y + it.size
                                        } -> options.colors.frame.invoke(
                                    i = (realX-frame.x).let{
                                        if (right && !options.colors.symmetry)
                                            frame.size - it else it
                                    },
                                    j = (realY-frame.y).let {
                                        if (bottom && !options.colors.symmetry)
                                            frame.size - it else it
                                    },
                                    width = frame.size,
                                    height = frame.size
                                )

                                pixel == QrCodeMatrix.PixelType.DarkPixel && options.colors.dark.invoke(
                                    x-paddingX, y-paddingY, width - 2* paddingX,height - 2* paddingY
                                ).alpha > 0 -> options.colors.dark.getColor(
                                    x-paddingX, y-paddingY,
                                    width - 2 * paddingX,
                                    height - 2* paddingY,
                                    pixelSize = pixelSize
                                )
                                pixel == QrCodeMatrix.PixelType.LightPixel && options.colors.light.invoke(
                                    x-paddingX, y-paddingY, width - 2 * paddingX,height - 2* paddingY
                                ).alpha > 0 -> options.colors.light.invoke(
                                    x-paddingX, y-paddingY, width - 2 * paddingX, height - 2* paddingY
                                )
                                else -> {
                                    val bgColor = array[idx]

                                    val codeBg = options.colors.highlighting.invoke(
                                        x-paddingX, y-paddingY, width - 2* paddingX,height - 2* paddingY
                                    )

                                    if (codeBg.alpha >0)
                                        QrUtil.mixColors(codeBg, bgColor, codeBg.alpha /255f)
                                    else bgColor
                                }
                            }
                            array[idx] = color
                        }
                    }
                }
            }

            if (drawLogo && options.logo.drawable != null) kotlin.run {
                val logoSize = ((width - minOf(paddingX,paddingY)*2) /
                        options.codeShape.shapeSizeIncrease *
                        options.logo.size)
                    .roundToInt()

                val bitmapLogo = options.logo.scale
                    .scale(options.logo.drawable, logoSize, logoSize)
                val logoPixels = IntArray(logoSize*logoSize)
                bitmapLogo.getPixels(logoPixels,0, logoSize, 0,0, logoSize, logoSize)

                val logoLeft = (width - logoSize) / 2 - paddingX + offsetX
                val logoTop = (height - logoSize) / 2 - paddingY + offsetY

                for (i in 0 until logoSize) {
                    for (j in 0 until logoSize) {

                        ensureActive()

                        if (!options.logo.shape.invoke(
                                i, j, logoSize,
                                Neighbors.Empty
                            )
                        ) {
                            continue
                        }

                        val bgColorPos = logoLeft + i + (logoTop + j) * width
                        val logoPixel = logoPixels[i + j * logoSize]
                        val logoBgColor = options.logo.backgroundColor.let {
                            if (it is QrColor.Unspecified) array[bgColorPos]
                            else it.invoke(i, j, logoSize, logoSize)
                        }
                        runCatching {

                            array[bgColorPos] = QrUtil.mixColors(
                                logoPixel, logoBgColor, logoPixel.alpha / 255f
                            )
                        }
                    }
                }
            }

            setPixels(array,0,width,0,0,width,height)
        }
    }
}

private fun QrErrorCorrectionLevel.fit(
    logo: QrLogo,
) : QrErrorCorrectionLevel  {
    val size = logo.size * (1 + logo.padding.value)
    val hasLogo = size > Float.MIN_VALUE && logo.drawable != EmptyDrawable ||
            logo.padding != QrLogoPadding.Empty
    return fit(hasLogo, size)
}