package tpcreative.co.qrscanner.ui.create
import android.content.Intent
import android.os.Bundle
import android.view.*
import com.basgeekball.awesomevalidation.AwesomeValidation
import com.basgeekball.awesomevalidation.ValidationStyle
import com.basgeekball.awesomevalidation.utility.RegexTemplate
import com.google.zxing.client.result.ParsedResultType
import kotlinx.android.synthetic.main.fragment_wifi.*
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.GenerateSingleton.SingletonGenerateListener
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide
import tpcreative.co.qrscanner.model.*

class WifiFragment : BaseActivitySlide(), View.OnClickListener, SingletonGenerateListener {
    var mAwesomeValidation: AwesomeValidation? = null
    var typeEncrypt: String? = "WPA"
    private var save: SaveModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_wifi)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val bundle = intent.extras
        val mData = bundle?.get(getString(R.string.key_data)) as SaveModel?
        if (mData != null) {
            save = mData
            onSetData()
        } else {
            Utils.Log(TAG, "Data is null")
        }
        radio0.setOnClickListener(this)
        radio1.setOnClickListener(this)
        radio2.setOnClickListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_select, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_select -> {
                if (mAwesomeValidation?.validate() == true) {
                    Utils.Log(TAG, "Passed")
                    val create = Create(save)
                    create.ssId = edtSSID.text.toString().trim { it <= ' ' }
                    create.password = edtPassword.text.toString()
                    create.networkEncryption = typeEncrypt
                    create.createType = ParsedResultType.WIFI
                    Navigator.onMoveToReview(this, create)
                } else {
                    Utils.Log(TAG, "error")
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun addValidationForEditText() {
        mAwesomeValidation?.addValidation(this, R.id.edtSSID, RegexTemplate.NOT_EMPTY, R.string.err_ssId)
        mAwesomeValidation?.addValidation(this, R.id.edtPassword, RegexTemplate.NOT_EMPTY, R.string.err_password)
    }

    fun FocusUI() {
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
        FocusUI()
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

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == Navigator.CREATE) {
        }
    }

    companion object {
        private val TAG = WifiFragment::class.java.simpleName
    }
}