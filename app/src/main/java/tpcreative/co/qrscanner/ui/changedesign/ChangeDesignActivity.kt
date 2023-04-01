package tpcreative.co.qrscanner.ui.changedesign

import android.graphics.Path
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.github.alexzhirkevich.customqrgenerator.QrData
import com.github.alexzhirkevich.customqrgenerator.style.Neighbors
import com.github.alexzhirkevich.customqrgenerator.vector.QrCodeDrawable
import com.github.alexzhirkevich.customqrgenerator.vector.QrVectorOptions
import com.github.alexzhirkevich.customqrgenerator.vector.style.*
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide
import tpcreative.co.qrscanner.databinding.ActivityChangeDesignBinding
import tpcreative.co.qrscanner.ui.review.ReviewViewModel


class ChangeDesignActivity : BaseActivitySlide() {
    lateinit var viewModel: ReviewViewModel
    lateinit var binding : ActivityChangeDesignBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangeDesignBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        initUI()
    }

    fun onGenerateQR(){
        val options = QrVectorOptions.Builder()
            .setPadding(.3f)
            .setLogo(
                QrVectorLogo(
                    drawable = ContextCompat
                        .getDrawable(this, R.drawable.design_wifi),
                    size = .25f,
                    padding = QrVectorLogoPadding.Natural(.2f),
                    shape = QrVectorLogoShape
                        .Circle
                )
            )
            .setBackground(
                QrVectorBackground(
                    drawable = ContextCompat
                        .getDrawable(this, R.color.white),
                )
            )
            .setColors(
                QrVectorColors(
                    dark = QrVectorColor
                        .Solid(ContextCompat.getColor(this,R.color.colorAccent)),
                    ball = QrVectorColor.Solid(
                        ContextCompat.getColor(this, R.color.colorAccent)
                    )
                )
            )
            .setShapes(
                QrVectorShapes(
                    darkPixel = QrVectorPixelShape
                        .RoundCorners(.5f),
                    ball = QrVectorBallShape
                        .RoundCorners(.25f),
                    frame = QrVectorFrameShape
                        .RoundCorners(.25f),
                )
            )
            .build()
        val data = QrData.Text(viewModel.create.code?:"")
        val drawable : Drawable = QrCodeDrawable(data, options)
        binding.imgQRCode.setImageDrawable(drawable)
    }

    object Circle : QrVectorPixelShape {

        override fun createPath(size: Float, neighbors: Neighbors): Path = Path().apply {
            addCircle(size/2f, size/2f, size/2, Path.Direction.CW)
        }
    }
}