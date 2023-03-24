package tpcreative.co.qrscanner.ui.create
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.basgeekball.awesomevalidation.AwesomeValidation
import com.basgeekball.awesomevalidation.ValidationStyle
import com.basgeekball.awesomevalidation.utility.RegexTemplate
import com.google.zxing.BarcodeFormat
import com.google.zxing.client.result.ParsedResultType
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.GenerateSingleton.SingletonGenerateListener
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide
import tpcreative.co.qrscanner.common.network.base.ViewModelFactory
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.common.view.ads.AdsView
import tpcreative.co.qrscanner.databinding.ActivityWifiBinding
import tpcreative.co.qrscanner.model.*
import tpcreative.co.qrscanner.viewmodel.GenerateViewModel
import java.util.regex.Pattern

class WifiActivity : BaseActivitySlide(), View.OnClickListener, SingletonGenerateListener,OnEditorActionListener {
    lateinit var viewModel: GenerateViewModel
    var mAwesomeValidation: AwesomeValidation? = null
    var typeEncrypt: String? = ConstantValue.WPA
    private var save: GeneralModel? = null
    lateinit var llSmallAds : AdsView
    lateinit var binding : ActivityWifiBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWifiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        llSmallAds = AdsView(this)
        if(Utils.isHiddenAds(EnumScreens.CREATE_SMALL)){
            binding.rlAdsRoot.visibility = View.GONE
        }else{
            binding.rlAdsRoot.addView(llSmallAds.getSmallAds())
        }
        if(Utils.isHiddenAds(EnumScreens.CREATE_LARGE)){
            binding.rlBannerLarger.visibility = View.GONE
        }
        setupViewModel()
        getIntentData()
        if (QRScannerApplication.getInstance().isCreateSmallView() && QRScannerApplication.getInstance().isLiveAds() && QRScannerApplication.getInstance().isEnableCreateSmallView() && !Utils.isHiddenAds(EnumScreens.CREATE_SMALL)) {
            QRScannerApplication.getInstance().requestCreateSmallView(this)
        }
        if (QRScannerApplication.getInstance().isCreateLargeView() && QRScannerApplication.getInstance().isLiveAds() && QRScannerApplication.getInstance().isEnableCreateLargeView() && !Utils.isHiddenAds(EnumScreens.CREATE_LARGE)) {
            QRScannerApplication.getInstance().requestCreateLargeView(this)
        }
        checkingShowAds()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.radio0.setOnClickListener(this)
        binding.radio1.setOnClickListener(this)
        binding.radio2.setOnClickListener(this)
        binding.edtSSID.setOnEditorActionListener(this)
        binding.edtPassword.setOnEditorActionListener(this)
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
            val create = GeneralModel(save)
            create.ssId = binding.edtSSID.text.toString().trim { it <= ' ' }
            create.password = binding.edtPassword.text.toString()
            create.networkEncryption = typeEncrypt
            create.createType = ParsedResultType.WIFI
            create.barcodeFormat = BarcodeFormat.QR_CODE.name
            Navigator.onMoveToReview(this, create)
        } else {
            Utils.Log(TAG, "error")
        }
    }

    private fun addValidationForEditText() {
        mAwesomeValidation?.addValidation(this, R.id.edtSSID, RegexTemplate.NOT_EMPTY, R.string.err_ssId)
        mAwesomeValidation?.addValidation(this, R.id.edtPassword, RegexTemplate.NOT_EMPTY, R.string.err_password)
        mAwesomeValidation?.addValidation(this, R.id.edtPassword, Pattern.compile("\\p{ASCII}*$"), R.string.err_the_password_contains_unsupported_characters)
    }

    private fun focusUI() {
        binding.edtSSID.requestFocus()
    }

    fun onSetData() {
        binding.edtSSID.setText(save?.ssId)
        binding.edtPassword.setText(save?.password)
        if (save?.networkEncryption == ConstantValue.WPA) {
            binding.radio0.isChecked = true
        } else if (save?.networkEncryption == ConstantValue.WEP) {
            binding.radio1.isChecked = true
        } else {
            binding.radio2.isChecked = true
        }
        binding.edtSSID.setSelection(binding.edtSSID.text?.length ?: 0)
        hideSoftKeyBoard()
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

    override fun onPause() {
        QRScannerApplication.getInstance().onPauseAds(EnumScreens.CREATE_SMALL)
        QRScannerApplication.getInstance().onPauseAds(EnumScreens.CREATE_LARGE)
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
        QRScannerApplication.getInstance().onResumeAds(EnumScreens.CREATE_SMALL)
        QRScannerApplication.getInstance().onResumeAds(EnumScreens.CREATE_LARGE)
        GenerateSingleton.getInstance()?.setListener(this)
        checkingShowAds()
        Utils.Log(TAG, "onResume")
    }

    override fun onCompletedGenerate() {
        SaveSingleton.getInstance()?.reloadData()
        Utils.Log(TAG, "Finish...........")
        //finish()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.radio0 -> {
                typeEncrypt = ConstantValue.WPA
                Utils.Log(TAG, "Selected here: radio 0")
            }
            R.id.radio1 -> {
                typeEncrypt = ConstantValue.WEP
                Utils.Log(TAG, "Selected here: radio 1")
            }
            R.id.radio2 -> {
                run {
                    typeEncrypt = ConstantValue.NONE
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
            QRScannerApplication.getInstance().loadCreateSmallView(llSmallAds.getSmallAds())
            QRScannerApplication.getInstance().loadCreateLargeView(binding.llLargeAds)
        }
    }

    companion object {
        private val TAG = WifiActivity::class.java.simpleName
    }
}