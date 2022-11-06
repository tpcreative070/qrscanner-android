package tpcreative.co.qrscanner.ui.create
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import com.basgeekball.awesomevalidation.AwesomeValidation
import com.basgeekball.awesomevalidation.ValidationStyle
import com.basgeekball.awesomevalidation.utility.RegexTemplate
import com.google.zxing.client.result.ParsedResultType
import kotlinx.android.synthetic.main.activity_text.*
import kotlinx.android.synthetic.main.activity_wifi.*
import kotlinx.android.synthetic.main.activity_wifi.toolbar
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.GenerateSingleton.SingletonGenerateListener
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide
import tpcreative.co.qrscanner.common.extension.serializable
import tpcreative.co.qrscanner.model.*

class WifiActivity : BaseActivitySlide(), View.OnClickListener, SingletonGenerateListener,OnEditorActionListener {
    var mAwesomeValidation: AwesomeValidation? = null
    var typeEncrypt: String? = "WPA"
    private var save: SaveModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wifi)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val mData = intent?.serializable(getString(R.string.key_data),SaveModel::class.java)
        if (mData != null) {
            save = mData
            onSetData()
        } else {
            Utils.Log(TAG, "Data is null")
        }
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
        if (p1 == EditorInfo.IME_ACTION_DONE || p2?.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
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

    companion object {
        private val TAG = WifiActivity::class.java.simpleName
    }
}