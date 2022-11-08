package tpcreative.co.qrscanner.ui.create
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.basgeekball.awesomevalidation.AwesomeValidation
import com.basgeekball.awesomevalidation.ValidationStyle
import com.basgeekball.awesomevalidation.utility.RegexTemplate
import com.google.zxing.client.result.ParsedResultType
import kotlinx.android.synthetic.main.activity_wifi.*
import kotlinx.android.synthetic.main.activity_wifi.llLargeAds
import kotlinx.android.synthetic.main.activity_wifi.llSmallAds
import kotlinx.android.synthetic.main.activity_wifi.toolbar
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.GenerateSingleton.SingletonGenerateListener
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide
import tpcreative.co.qrscanner.common.network.base.ViewModelFactory
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.model.*
import tpcreative.co.qrscanner.viewmodel.GenerateViewModel

class WifiActivity : BaseActivitySlide(), View.OnClickListener, SingletonGenerateListener,OnEditorActionListener {
    lateinit var viewModel: GenerateViewModel
    var mAwesomeValidation: AwesomeValidation? = null
    var typeEncrypt: String? = "WPA"
    private var save: SaveModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wifi)
        setSupportActionBar(toolbar)
        setupViewModel()
        getIntentData()
        if (QRScannerApplication.getInstance().isCreateSmallView() && QRScannerApplication.getInstance().isLiveAds() && QRScannerApplication.getInstance().isEnableCreateSmallView()) {
            QRScannerApplication.getInstance().requestCreateSmallView(this)
        }
        if (QRScannerApplication.getInstance().isCreateLargeView() && QRScannerApplication.getInstance().isLiveAds() && QRScannerApplication.getInstance().isEnableCreateLargeView()) {
            QRScannerApplication.getInstance().requestCreateLargeView(this)
        }
        checkingShowAds()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        radio0.setOnClickListener(this)
        radio1.setOnClickListener(this)
        radio2.setOnClickListener(this)
        edtSSID.setOnEditorActionListener(this)
        edtPassword.setOnEditorActionListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_select, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_select -> {
               onSave()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onEditorAction(p0: TextView?, p1: Int, p2: KeyEvent?): Boolean {
        if (p1 == EditorInfo.IME_ACTION_DONE) {
            onSave()
            return  true
        }
        return false
    }

    private fun onSave(){
        hideSoftKeyBoard()
        if (mAwesomeValidation?.validate() == true) {
            Utils.Log(TAG, "Passed")
            val create = CreateModel(save)
            create.ssId = edtSSID.text.toString().trim { it <= ' ' }
            create.password = edtPassword.text.toString()
            create.networkEncryption = typeEncrypt
            create.createType = ParsedResultType.WIFI
            Navigator.onMoveToReview(this, create)
        } else {
            Utils.Log(TAG, "error")
        }
    }

    private fun addValidationForEditText() {
        mAwesomeValidation?.addValidation(this, R.id.edtSSID, RegexTemplate.NOT_EMPTY, R.string.err_ssId)
        mAwesomeValidation?.addValidation(this, R.id.edtPassword, RegexTemplate.NOT_EMPTY, R.string.err_password)
    }

    private fun focusUI() {
        edtSSID.requestFocus()
    }

    fun onSetData() {
        edtSSID.setText(save?.ssId)
        edtPassword.setText(save?.password)
        if (save?.networkEncryption == "WPA") {
            radio0.isChecked = true
        } else if (save?.networkEncryption == "WEP") {
            radio1.isChecked = true
        } else {
            radio2.isChecked = true
        }
        edtSSID.setSelection(edtSSID.text?.length ?: 0)
    }

    public override fun onStart() {
        super.onStart()
        Utils.Log(TAG, "onStart")
        mAwesomeValidation = AwesomeValidation(ValidationStyle.BASIC)
        addValidationForEditText()
        focusUI()
    }

    public override fun onStop() {
        super.onStop()
        Utils.Log(TAG, "onStop")
    }

    public override fun onPause() {
        super.onPause()
        Utils.Log(TAG, "onPause")
    }

    public override fun onDestroy() {
        super.onDestroy()
        GenerateSingleton.getInstance()?.setListener(null)
        Utils.Log(TAG, "onDestroy")
    }

    public override fun onResume() {
        super.onResume()
        GenerateSingleton.getInstance()?.setListener(this)
        checkingShowAds()
        Utils.Log(TAG, "onResume")
    }

    override fun onCompletedGenerate() {
        SaveSingleton.getInstance()?.reloadData()
        Utils.Log(TAG, "Finish...........")
        finish()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.radio0 -> {
                typeEncrypt = "WPA"
                Utils.Log(TAG, "Selected here: radio 0")
            }
            R.id.radio1 -> {
                typeEncrypt = "WEP"
                Utils.Log(TAG, "Selected here: radio 1")
            }
            R.id.radio2 -> {
                run {
                    typeEncrypt = "None"
                    Utils.Log(TAG, "Selected here: radio 2")
                }
                run {}
            }
            else -> {
            }
        }
    }

    private fun getIntentData(){
        viewModel.getIntent(this).observe(this, Observer {
            if (it!=null){
                save = it
                onSetData()
            }
        })
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            this,
            ViewModelFactory()
        )[GenerateViewModel::class.java]
    }

    private fun checkingShowAds(){
        viewModel.doShowAds().observe(this, Observer {
            doShowAds(it)
        })
    }

    /*show ads*/
    private fun doShowAds(isShow: Boolean) {
        if (isShow) {
            QRScannerApplication.getInstance().loadCreateSmallView(llSmallAds)
            QRScannerApplication.getInstance().loadCreateLargeView(llLargeAds)
        }
    }

    companion object {
        private val TAG = WifiActivity::class.java.simpleName
    }
}