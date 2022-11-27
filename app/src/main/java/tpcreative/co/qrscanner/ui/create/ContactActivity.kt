package tpcreative.co.qrscanner.ui.create
import android.os.Bundle
import android.util.Patterns
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import com.basgeekball.awesomevalidation.AwesomeValidation
import com.basgeekball.awesomevalidation.ValidationStyle
import com.basgeekball.awesomevalidation.utility.RegexTemplate
import com.google.zxing.client.result.ParsedResultType
import kotlinx.android.synthetic.main.activity_contact.*
import kotlinx.android.synthetic.main.activity_contact.edtEmail
import kotlinx.android.synthetic.main.activity_contact.edtPhone
import kotlinx.android.synthetic.main.activity_contact.toolbar
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.GenerateSingleton.SingletonGenerateListener
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide
import tpcreative.co.qrscanner.common.extension.serializable
import tpcreative.co.qrscanner.model.*

class ContactActivity : BaseActivitySlide(), SingletonGenerateListener,OnEditorActionListener {
    private var mAwesomeValidation: AwesomeValidation? = null
    private var save: SaveModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val mData = intent?.serializable(getString(R.string.key_data),SaveModel::class.java)
        if (mData != null) {
            save = mData
            onSetData()
        } else {
            Utils.Log(TAG, "Data is null")
        }
        edtFirstName.setOnEditorActionListener(this)
        edtAddress.setOnEditorActionListener(this)
        edtPhone.setOnEditorActionListener(this)
        edtEmail.setOnEditorActionListener(this)
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
            val create = CreateModel(save)
            create.fullName = edtFirstName?.text.toString().trim { it <= ' ' }
            create.address = edtAddress?.text.toString()
            create.phone = edtPhone?.text.toString()
            create.email = edtEmail?.text.toString()
            create.createType = ParsedResultType.ADDRESSBOOK
            Navigator.onMoveToReview(this, create)
        } else {
            Utils.Log(TAG, "error")
        }
    }

    private fun addValidationForEditText() {
        mAwesomeValidation?.addValidation(this, R.id.edtFirstName, RegexTemplate.NOT_EMPTY, R.string.err_fullName)
        mAwesomeValidation?.addValidation(this, R.id.edtAddress, RegexTemplate.NOT_EMPTY, R.string.err_address)
        mAwesomeValidation?.addValidation(this, R.id.edtPhone, Patterns.PHONE, R.string.err_phone)
        mAwesomeValidation?.addValidation(this, R.id.edtEmail, Patterns.EMAIL_ADDRESS, R.string.err_email)
    }

    private fun focusUI() {
        edtFirstName.requestFocus()
    }

    fun onSetData() {
        edtFirstName.setText(save?.fullName)
        edtAddress.setText(save?.address)
        edtPhone.setText(save?.phone)
        edtEmail.setText(save?.email)
        edtFirstName.setSelection(edtFirstName.text?.length ?: 0)
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

    companion object {
        private val TAG = ContactActivity::class.java.simpleName
    }
}