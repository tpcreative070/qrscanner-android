@file:Suppress("deprecation")


package com.github.alexzhirkevich.customqrgenerator.encoder

import com.github.alexzhirkevich.customqrgenerator.QrData
import com.github.alexzhirkevich.customqrgenerator.QrOptions
import com.github.alexzhirkevich.customqrgenerator.style.*
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.encoder.ByteMatrix
import com.google.zxing.qrcode.encoder.Encoder
import com.google.zxing.qrcode.encoder.QRCode
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import java.nio.charset.Charset
import kotlin.math.roundToInt

private class ElementData (
    val x : (Int) -> (Int),
    val y : (Int) -> (Int),
    val size : Int,
    val modifier: QrShapeModifier
)

internal class QrEncoder(private val options: QrOptions)  {

    companion object {
        const val FRAME_SIZE = 7
        const val BALL_SIZE = 3
    }

    suspend fun encode(
        contents: QrData, charset: Charset?
    ): QrRenderResult = coroutineScope {

        val text = contents.encode()
        require(text.isNotEmpty()) { "Found empty contents" }
        val code = Encoder.encode(
            text, options.errorCorrectionLevel.lvl, charset?.let {
                mapOf(EncodeHintType.CHARACTER_SET to it)
            }
        )
        renderResult(code)
    }

    private suspend fun renderResult(code: QRCode): QrRenderResult = coroutineScope {
        val initialInput = (code.matrix ?: throw IllegalStateException())
            .toQrMatrix()

        val input = options.codeShape.apply(initialInput)

        val diff = (input.size - initialInput.size)/2
        val size = minOf(options.width, options.height)
        val padding = (size * options.padding.coerceIn(0f, 1f) /2f).roundToInt()
        val outputSize = (size - 2 * padding).coerceAtLeast(input.size)
        val multiple = outputSize / input.size
        val output = QrCodeMatrix(outputSize)


        val totalError =  ((outputSize.toFloat()/input.size - multiple)* input.size).roundToInt()
        val logoError =  ((outputSize.toFloat()/input.size - multiple)* input.size/2).roundToInt()

        input.applyLogoPadding(logoError/multiple.toFloat())

        val ballShape = options.shapes.ball.takeIf { it !is QrBallShape.AsDarkPixels }
            ?: QrBallShape.AsPixelShape(options.shapes.darkPixel)

        val frameShape = options.shapes.frame.takeIf { it !is QrFrameShape.AsDarkPixels }
            ?: QrFrameShape.AsPixelShape(options.shapes.darkPixel)

        var inputX = 0
        var outputX = 0
        while (inputX < input.size) {

            var inputY = 0
            var outputY = 0

            while (inputY < input.size) {

                ensureActive()

                    val elementData = elementDataOrNull(
                        inputX, inputY, diff, multiple, input.size,
                        ballShape, frameShape
                    )

                    if (elementData != null) {
                        for (i in 0 until multiple) {
                            for (j in 0 until multiple) {
                                kotlin.runCatching {
                                    output[inputX * multiple + i, inputY * multiple + j] =
                                        if (elementData.modifier.invoke(
                                                elementData.x(i),
                                                elementData.y(j),
                                                elementData.size,
                                                Neighbors.Empty
                                            )
                                        ) QrCodeMatrix.PixelType.DarkPixel
                                        else QrCodeMatrix.PixelType.Background
                                }
                            }
                        }
                    } else {
                        //pixels

                        if (input[inputX, inputY] != QrCodeMatrix.PixelType.Logo) {
                            val neighbors = input.neighborsReversed(inputX, inputY)

                            for (i in outputX until outputX + multiple) {
                                for (j in outputY until outputY + multiple) {
                                    output[i, j] = when {
                                        !options.codeShape.pixelInShape(inputX, inputY, input) ->
                                            QrCodeMatrix.PixelType.Background
                                        input[inputX, inputY] == QrCodeMatrix.PixelType.DarkPixel &&
                                                options.shapes.darkPixel.invoke(
                                                    i - outputX, j - outputY,
                                                    multiple, neighbors
                                                ) -> QrCodeMatrix.PixelType.DarkPixel
                                        options.shapes.lightPixel.invoke(
                                            i - outputX, j - outputY,
                                            multiple, neighbors
                                        ) -> QrCodeMatrix.PixelType.LightPixel
                                        else -> QrCodeMatrix.PixelType.Background
                                    }
                                }
                            }
                        }
                    }

                inputY++
                outputY += multiple
            }
            inputX++
            outputX += multiple
        }

        if (options.logo.padding.shouldApplyAccuratePadding){
            output.applyMinimalLogoPadding(totalError)
        }

        val frame = Rectangle(
            diff * multiple,
            diff * multiple,
            FRAME_SIZE * multiple
        )

        val ball = Rectangle(
            frame.x + (FRAME_SIZE - BALL_SIZE)/2 * multiple,
            frame.y + (FRAME_SIZE - BALL_SIZE)/2* multiple,
            BALL_SIZE * multiple
        )

        val (pX, pY) = if (options.width < options.height){
            padding to (options.height - outputSize)/2
        } else {
            (options.width - outputSize)/2 to padding
        }
        QrRenderResult(output, pX,pY, multiple,diff*multiple, frame, ball, totalError)
    }

    private fun QrCodeMatrix.applyLogoPadding(error: Float) {
        var logoSize = size /
                options.codeShape.shapeSizeIncrease.coerceAtLeast(1f) *
                options.logo.size.coerceIn(0f,1f) *
                (1 + options.logo.padding.value.coerceIn(0f,1f)) + 2

        if (options.logo.shape !is QrLogoShape.Default) {
            if (logoSize.roundToInt() % 2 == size % 2)
                logoSize--
        } else {
            if (logoSize.roundToInt() % 2 != size % 2)
                logoSize++
        }


        logoSize = logoSize.coerceIn(0f,size.toFloat())


        var logoPos = ((size - logoSize )/2f)

        if (options.logo.shape !is QrLogoShape.Default){
            logoPos -= error/2
        }

        options.logo.padding.apply(
            matrix = this,
            logoSize = logoSize.roundToInt(),
            logoPos = logoPos.roundToInt(),
            logoShape = options.logo.shape)
    }

    private fun QrCodeMatrix.applyMinimalLogoPadding(error: Int) {
        if (options.logo.padding.value >= Float.MIN_VALUE) {
            val logoSize = (size / options.codeShape.shapeSizeIncrease.coerceAtLeast(1f) *
                    options.logo.size.coerceIn(0f,1f) * (1 + options.logo.padding.value.coerceIn(0f,1f)))
                .roundToInt().coerceIn(0,size)

            val logoTopLeft = (size - logoSize - error) / 2

            for (i in 0 until logoSize) {
                for (j in 0 until logoSize) {
                    if (options.logo.shape.invoke(i, j, logoSize, Neighbors.Empty)) {
                        kotlin.runCatching {
                            this[logoTopLeft + i, logoTopLeft + j] = QrCodeMatrix.PixelType.Background
                        }
                    }
                }
            }
        }
    }

    private fun elementDataOrNull(
        inputX : Int, inputY : Int, diff :Int, multiple : Int, inputSize : Int,
        ballShape: QrBallShape, frameShape: QrFrameShape
    ) = when {

        //top left ball
        inputX - diff in 2 until 5 && inputY - diff in 2  until 5 ->
        ElementData(
            {(inputX -diff- 2) * multiple + it},
            {(inputY -diff- 2) * multiple + it},
            3 * multiple,
            ballShape,
        )

        // top left frame
        inputX- diff in 0 until 7 && inputY -diff in 0 until 7 ->
        ElementData(
            {(inputX - diff) * multiple + it},
            {(inputY - diff) * multiple + it },
            7 * multiple,
            frameShape
        )

        //top right ball
        inputSize - inputX-1 - diff in 2 until 5 && inputY - diff in 2 until 5->
        ElementData(
            {(inputSize - inputX - diff - 2) * multiple - it},
            {(inputY- 2 - diff) * multiple + it},
            3 * multiple,
            ballShape
        )

        //top right frame
        inputSize - inputX - 1 - diff in 0 until 7 && inputY - diff in 0 until 7 ->
        ElementData(
            {(inputSize - inputX - diff) * multiple - it},
            {(inputY - diff) * multiple + it},
            7 * multiple,
            frameShape
        )

        //bottom ball
        inputX - diff in 2 until 5 && inputSize- inputY-1 -diff in 2 until 5 ->
        ElementData(
            {(inputX-2 - diff) * multiple + it},
            {(inputSize - inputY-2 - diff) * multiple - it},
            3 * multiple,
            ballShape
        )
        //bottom frame
        inputX - diff in 0 until 7 && inputSize -inputY-1-diff in 0 until 7 ->
        ElementData(
            {(inputX - diff) * multiple + it},
            { (inputSize - inputY - diff) * multiple - it},
            7 * multiple,
            frameShape
        )
        else -> null
    }
}

fun ByteMatrix.toQrMatrix() : QrCodeMatrix {
    if (width != height)
        throw IllegalStateException("Non-square qr byte matrix")

    return QrCodeMatrix(width).apply {
        for (i in 0 until width){
            for (j in 0 until width){
                this[i,j] = if (this@toQrMatrix[i,j].toInt() == 1)
                    QrCodeMatrix.PixelType.DarkPixel
                else QrCodeMatrix.PixelType.LightPixel
            }
        }
    }
}

