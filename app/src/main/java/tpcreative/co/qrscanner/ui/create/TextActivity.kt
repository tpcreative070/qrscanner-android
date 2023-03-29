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
import com.google.zxing.BarcodeFormat
import com.google.zxing.client.result.ParsedResultType
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.GenerateSingleton.SingletonGenerateListener
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide
import tpcreative.co.qrscanner.common.network.base.ViewModelFactory
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.common.view.ads.AdsView
import tpcreative.co.qrscanner.databinding.ActivityTextBinding
import tpcreative.co.qrscanner.model.*
import tpcreative.co.qrscanner.ui.review.initUI
import tpcreative.co.qrscanner.viewmodel.GenerateViewModel

class TextActivity : BaseActivitySlide(), SingletonGenerateListener, OnEditorActionListener {
    lateinit var viewModel: GenerateViewModel
    var mAwesomeValidation: AwesomeValidation? = null
    private var save: GeneralModel? = null
    var viewAds : AdsView? = null
    lateinit var binding : ActivityTextBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTextBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (!Utils.isPremium()){
            viewAds = AdsView(this)
        }
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if(Utils.isHiddenAds(EnumScreens.CREATE_SMALL)){
            binding.rlAdsRoot.visibility = View.GONE
        }else{
            binding.rlAdsRoot.addView(viewAds?.getRootSmallAds())
        }
        if(Utils.isHiddenAds(EnumScreens.CREATE_SMALL)){
            binding.rlBannerLarger.visibility = View.GONE
        }else{
            binding.rlBannerLarger.addView(viewAds?.getRootLargeAds())
        }
        setupViewModel()
        getIntentData()
        if (QRScannerApplication.getInstance().isCreateSmallView() && QRScannerApplication.getInstance().isLiveAds() && QRScannerApplication.getInstance().isEnableCreateSmallView() && !Utils.isHiddenAds(EnumScreens.CREATE_SMALL) && !Utils.isRequestShowLocalAds()) {
            QRScannerApplication.getInstance().requestCreateSmallView(this)
        }
        if (QRScannerApplication.getInstance().isCreateLargeView() && QRScannerApplication.getInstance().isLiveAds() && QRScannerApplication.getInstance().isEnableCreateLargeView() && !Utils.isHiddenAds(EnumScreens.CREATE_LARGE) && !Utils.isRequestShowLocalAds()) {
            QRScannerApplication.getInstance().requestCreateLargeView(this)
        }
        checkingShowAds()
        binding.edtText.setOnEditorActionListener(this)
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
            val create = GeneralModel(save)
            create.textProductIdISNB = binding.edtText.text.toString().trim { it <= ' ' }
            create.createType = ParsedResultType.TEXT
            create.barcodeFormat = BarcodeFormat.QR_CODE.name
            Navigator.onMoveToReview(this, create)
        } else {
            Utils.Log(TAG, "error")
        }
    }

    private fun addValidationForEditText() {
        mAwesomeValidation?.addValidation(this, R.id.edtText, RegexTemplate.NOT_EMPTY, R.string.err_text)
    }

    private fun focusUI() {
        binding.edtText.requestFocus()
    }

    fun onSetData() {
        binding.edtText.setText(save?.textProductIdISNB)
        binding.edtText.setSelection(binding.edtText.text?.length ?: 0)
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
    }

    public override fun onDestroy() {
        super.onDestroy()
        GenerateSingleton.getInstance()?.setListener(null)
        Utils.Log(TAG, "onDestroy")
    }

    override fun onResume() {
        QRScannerApplication.getInstance().onResumeAds(EnumScreens.CREATE_SMALL)
        QRScannerApplication.getInstance().onResumeAds(EnumScreens.CREATE_LARGE)
        super.onResume()
        GenerateSingleton.getInstance()?.setListener(this)
        checkingShowAds()
        Utils.Log(TAG, "onResume")
    }

    override fun onCompletedGenerate() {
        SaveSingleton.getInstance()?.reloadData()
        Utils.Log(TAG, "Finish...........")
        //finish()
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
            QRScannerApplication.getInstance().loadCreateSmallView(viewAds?.getSmallAds())
            QRScannerApplication.getInstance().loadCreateLargeView(viewAds?.getLargeAds())
        }
    }

    companion object {
        private val TAG = TextActivity::class.java.simpleName
    }
}