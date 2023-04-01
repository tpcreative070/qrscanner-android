package tpcreative.co.qrscanner.ui.changedesign

import android.graphics.Bitmap
import android.graphics.Path
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.github.alexzhirkevich.customqrgenerator.QrData
import com.github.alexzhirkevich.customqrgenerator.style.Neighbors
import com.github.alexzhirkevich.customqrgenerator.vector.QrCodeDrawable
import com.github.alexzhirkevich.customqrgenerator.vector.QrVectorOptions
import com.github.alexzhirkevich.customqrgenerator.vector.style.*
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide
import tpcreative.co.qrscanner.common.extension.toBitmap
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
        val mBitmap = drawable.toBitmap(200,200,Bitmap.Config.ARGB_8888)
        binding.imgQRCode.setImageDrawable(drawable)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.menu_item_png_export -> {
                return true
            }
            R.id.menu_item_print -> {
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_change_design, menu)
        return super.onCreateOptionsMenu(menu)
    }

    object Circle : QrVectorPixelShape {

        override fun createPath(size: Float, neighbors: Neighbors): Path = Path().apply {
            addCircle(size/2f, size/2f, size/2, Path.Direction.CW)
        }
    }
}