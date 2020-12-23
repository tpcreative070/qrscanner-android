package tpcreative.co.qrscanner.ui.help

import android.os.Bundle
import androidx.appcompat.widget.*
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.ScannerSingleton
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide

class HelpActivity : BaseActivitySlide() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)
        val toolbar = findViewById<Toolbar?>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar.setDisplayHomeAsUpEnabled(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        ScannerSingleton.Companion.getInstance().setVisible()
    }

    override fun onResume() {
        super.onResume()
    }
}