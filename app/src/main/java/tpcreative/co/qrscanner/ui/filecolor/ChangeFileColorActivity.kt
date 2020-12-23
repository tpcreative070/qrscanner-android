package tpcreative.co.qrscanner.ui.filecolor

import android.app.Activity
import android.content.*
import android.graphics.Bitmap
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.*
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.journeyapps.barcodescanner.BarcodeEncoder
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.SettingsSingleton
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide
import tpcreative.co.qrscanner.common.controller.PrefsController
import tpcreative.co.qrscanner.common.presenter.BaseView
import tpcreative.co.qrscanner.common.view.GridSpacingItemDecoration
import tpcreative.co.qrscanner.model.*
import java.util.*

class ChangeFileColorActivity : BaseActivitySlide(), BaseView<Any?>, ChangeFileColorAdapter.ItemSelectedListener {
    @BindView(R.id.recyclerView)
    var recyclerView: RecyclerView? = null

    @BindView(R.id.imgResult)
    var imgResult: AppCompatImageView? = null
    private var bitmap: Bitmap? = null
    private var presenter: ChangeFileColorPresenter? = null
    private var adapter: ChangeFileColorAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chage_file_color)
        val toolbar = findViewById<Toolbar?>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar.setDisplayHomeAsUpEnabled(true)
        initRecycleView(layoutInflater)
        presenter = ChangeFileColorPresenter()
        presenter.bindView(this)
        presenter.getData()
    }

    fun initRecycleView(layoutInflater: LayoutInflater?) {
        adapter = ChangeFileColorAdapter(layoutInflater, applicationContext, this)
        val mLayoutManager: RecyclerView.LayoutManager = GridLayoutManager(applicationContext, 4)
        recyclerView.setLayoutManager(mLayoutManager)
        recyclerView.addItemDecoration(GridSpacingItemDecoration(4, 4, true))
        recyclerView.setItemAnimator(DefaultItemAnimator())
        recyclerView.setAdapter(adapter)
    }

    override fun onClickItem(position: Int) {
        presenter.mTheme = presenter.mList[position]
        PrefsController.putInt(getString(R.string.key_theme_object), position)
        presenter.getData()
        SettingsSingleton.Companion.getInstance().onUpdated()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onStartLoading(status: EnumStatus?) {}
    override fun onStopLoading(status: EnumStatus?) {}
    override fun onError(message: String?, status: EnumStatus?) {}
    override fun onError(message: String?) {}
    override fun onSuccessful(message: String?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?) {
        when (status) {
            EnumStatus.SHOW_DATA -> {
                adapter.setDataSource(presenter.mList)
                onGenerateReview("123")
            }
        }
    }

    override fun getActivity(): Activity? {
        return this
    }

    override fun getContext(): Context? {
        return applicationContext
    }

    override fun onSuccessful(message: String?, status: EnumStatus?, `object`: Any?) {}
    override fun onSuccessful(message: String?, status: EnumStatus?, list: MutableList<*>?) {}
    fun onGenerateReview(code: String?) {
        try {
            val barcodeEncoder = BarcodeEncoder()
            val hints: MutableMap<EncodeHintType?, Any?> = EnumMap(EncodeHintType::class.java)
            hints[EncodeHintType.MARGIN] = 2
            val theme: Theme = Theme.Companion.getInstance().getThemeInfo()
            bitmap = barcodeEncoder.encodeBitmap(this, theme.primaryDarkColor, code, BarcodeFormat.QR_CODE, 100, 100, hints)
            imgResult.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}