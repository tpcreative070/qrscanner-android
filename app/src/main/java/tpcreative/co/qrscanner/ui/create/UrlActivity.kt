package tpcreative.co.qrscanner.ui.create
import android.os.Bundle
import android.util.Patterns
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import com.basgeekball.awesomevalidation.AwesomeValidation
import com.basgeekball.awesomevalidation.ValidationStyle
import com.google.zxing.BarcodeFormat
import com.google.zxing.client.result.ParsedResultType
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.GenerateSingleton.SingletonGenerateListener
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide
import tpcreative.co.qrscanner.common.extension.serializable
import tpcreative.co.qrscanner.databinding.ActivityUrlBinding
import tpcreative.co.qrscanner.model.*

class UrlActivity : BaseActivitySlide(), SingletonGenerateListener, OnEditorActionListener {
    var mAwesomeValidation: AwesomeValidation? = null
    private var save: GeneralModel? = null
    lateinit var binding : ActivityUrlBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUrlBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val mData = intent?.serializable(getString(R.string.key_data),GeneralModel::class.java)
        if (mData != null) {
            save = mData
            onSetData()
        } else {
            Utils.Log(TAG, "Data is null")
        }
        binding.edtUrl.setOnEditorActionListener(this)
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
            create.url = binding.edtUrl.text.toString().trim { it <= ' ' }
            create.createType = ParsedResultType.URI
            create.barcodeFormat = BarcodeFormat.QR_CODE.name
            Navigator.onMoveToReview(this, create)
        } else {
            Utils.Log(TAG, "error")
        }
    }

    private fun addValidationForEditText() {
        mAwesomeValidation?.addValidation(this, R.id.edtUrl, Patterns.WEB_URL, R.string.err_url)
    }

    private fun focusUI() {
        binding.edtUrl.requestFocus()
    }

    fun clearAndFocusUI() {
        binding.edtUrl.requestFocus()
        binding.edtUrl.setText("")
    }

    fun onSetData() {
        binding.edtUrl.setText(save?.url)
        binding.edtUrl.setSelection(binding.edtUrl.text?.length ?: 0)
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

    public override fun onDestroy() {
        super.onDestroy()
        GenerateSingleton.getInstance()?.setListener(null)
        Utils.Log(TAG, "onDestroy")
    }

    public override fun onResume() {
        super.onResume()
        GenerateSingleton.getInstance()?.setListener(this)
        Utils.Log(TAG, "onResume")
    }

    override fun onCompletedGenerate() {
        SaveSingleton.getInstance()?.reloadData()
        Utils.Log(TAG, "Finish...........")
        finish()
    }

    companion object {
        private val TAG = UrlActivity::class.java.simpleName
    }
}