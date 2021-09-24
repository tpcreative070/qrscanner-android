package tpcreative.co.qrscanner.ui.create
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.*
import com.basgeekball.awesomevalidation.AwesomeValidation
import com.basgeekball.awesomevalidation.ValidationStyle
import com.basgeekball.awesomevalidation.utility.RegexTemplate
import com.google.zxing.client.result.ParsedResultType
import kotlinx.android.synthetic.main.fragment_contact.*
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.GenerateSingleton.SingletonGenerateListener
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide
import tpcreative.co.qrscanner.model.*

class ContactFragment : BaseActivitySlide(), SingletonGenerateListener {
    private var mAwesomeValidation: AwesomeValidation? = null
    private var save: SaveModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_contact)
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
                    create.fullName = edtFullName?.text.toString().trim { it <= ' ' }
                    create.address = edtAddress?.text.toString()
                    create.phone = edtPhone?.text.toString()
                    create.email = edtEmail?.text.toString()
                    create.createType = ParsedResultType.ADDRESSBOOK
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
        mAwesomeValidation?.addValidation(this, R.id.edtFullName, RegexTemplate.NOT_EMPTY, R.string.err_fullName)
        mAwesomeValidation?.addValidation(this, R.id.edtAddress, RegexTemplate.NOT_EMPTY, R.string.err_address)
        mAwesomeValidation?.addValidation(this, R.id.edtPhone, Patterns.PHONE, R.string.err_phone)
        mAwesomeValidation?.addValidation(this, R.id.edtEmail, Patterns.EMAIL_ADDRESS, R.string.err_email)
    }

    fun FocusUI() {
        edtFullName.requestFocus()
    }

    fun onSetData() {
        edtFullName.setText(save?.fullName)
        edtAddress.setText(save?.address)
        edtPhone.setText(save?.phone)
        edtEmail.setText(save?.email)
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

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == Navigator.CREATE) {
            Utils.Log(TAG, "Finish...........")
            SaveSingleton.getInstance()?.reloadData()
            finish()
        }
    }

    companion object {
        private val TAG = ContactFragment::class.java.simpleName
    }
}