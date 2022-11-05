package tpcreative.co.qrscanner.ui.viewcode

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.FileProvider
import tpcreative.co.qrscanner.BuildConfig
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide
import java.io.File

class ViewCodeActivity : BaseActivitySlide() {
    var bitmap: Bitmap? = null
    var code: String? = null
    lateinit var viewModel : ViewCodeViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_code)
        initUI()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_view_code, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.menu_item_png_export -> {
                getImageUri()?.let { shareToSocial(it) }
                return true
            }
            R.id.menu_item_print -> {
                onPhotoPrint()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}