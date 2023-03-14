package tpcreative.co.qrscanner.ui.create

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Patterns
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.basgeekball.awesomevalidation.AwesomeValidation
import com.basgeekball.awesomevalidation.ValidationStyle
import com.basgeekball.awesomevalidation.utility.RegexTemplate
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.client.result.ParsedResultType
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.GenerateSingleton.SingletonGenerateListener
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide
import tpcreative.co.qrscanner.common.extension.onParseContact
import tpcreative.co.qrscanner.common.extension.serializable
import tpcreative.co.qrscanner.common.extension.stringToMap
import tpcreative.co.qrscanner.databinding.ActivityContactBinding
import tpcreative.co.qrscanner.model.AddressModel
import tpcreative.co.qrscanner.model.ContactModel
import tpcreative.co.qrscanner.model.GeneralModel


class ContactActivity : BaseActivitySlide(), SingletonGenerateListener,OnEditorActionListener {
    private var mAwesomeValidation: AwesomeValidation? = null
    private var general: GeneralModel? = null
    private var phones : MutableMap<String,String>? = null
    private var addresses : MutableMap<String,AddressModel>? = null
    private var emails : MutableMap<String,String>? = null
    private var urls : MutableList<String>? = null
    lateinit var binding : ActivityContactBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        phones = mutableMapOf()
        addresses = mutableMapOf()
        emails = mutableMapOf()
        urls = mutableListOf()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val mData = intent?.serializable(getString(R.string.key_data),GeneralModel::class.java)
        if (mData != null) {
            general = mData
            onSetData()
        } else {
            Utils.Log(TAG, "Data is null")
        }
        binding.edtFirstName.setOnEditorActionListener(this)
        binding.edtLastName.setOnEditorActionListener(this)
        binding.edtCompany.setOnEditorActionListener(this)
        binding.edtJobTitle.setOnEditorActionListener(this)
        binding.edtAddress.setOnEditorActionListener(this)
        binding.edtZipcode.setOnEditorActionListener(this)
        binding.edtCity.setOnEditorActionListener(this)
        binding.edtRegion.setOnEditorActionListener(this)
        binding.edtCountry.setOnEditorActionListener(this)
        binding.edtPhoneNumber.setOnEditorActionListener(this)
        binding.edtEmail.setOnEditorActionListener(this)
        binding.edtWebite.setOnEditorActionListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_contact, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_select -> {
                onSave()
                return true
            }
            R.id.menu_open_contact -> {
                Dexter.withContext(this)
                    .withPermissions(
                        Manifest.permission.READ_CONTACTS)
                    .withListener(object : MultiplePermissionsListener {
                        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                            if (report?.areAllPermissionsGranted() == true) {
                                // Do something here
                                onPickUpContact()
                            } else {
                                Utils.Log(TAG, "Permission is denied")
                                finish()
                            }
                            // check for permanent denial of any permission
                            if (report?.isAnyPermissionPermanentlyDenied == true) {
                                /*Miss add permission in manifest*/
                                Utils.Log(TAG, "request permission is failed")
                            }
                        }
                        override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest?>?, token: PermissionToken?) {
                            /* ... */
                            token?.continuePermissionRequest()
                        }
                    })
                    .withErrorListener { Utils.Log(TAG, "error ask permission") }.onSameThread().check()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private val pickUpContactForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            Utils.Log(TAG, "REQUEST_PICK Up ${result.data.toString()}")
            val contactUri: Uri? = result.data?.data
            contactUri?.let {
                val mResult = contactUri.onParseContact(this)
                Utils.Log(TAG,"Contact ${Gson().toJson(mResult)}")
                general = GeneralModel()
                general?.contact = mResult
                onSetData()
            }
        }
    }

    private fun onPickUpContact(){
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        pickUpContactForResult.launch(intent)
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
            val create = GeneralModel(general)
            if (create.contact==null){
                create.contact = ContactModel()
            }
            create.contact?.givenName = binding.edtFirstName.text.toString()
            create.contact?.familyName = binding.edtLastName.text.toString()
            create.contact?.fullName =  binding.edtFirstName?.text.toString()+ " "+binding.edtLastName?.text.toString()
            create.contact?.company = binding.edtCompany.text.toString()
            create.contact?.jobTitle = binding.edtCompany.text.toString()

            if (create.contact?.addresses?.isNotEmpty() == true){
                create.contact?.addresses?.firstNotNullOfOrNull {
                    val mAddress = it.value
                    mAddress.street = binding.edtAddress?.text.toString()
                    mAddress.postalCode = binding.edtZipcode?.text.toString()
                    mAddress.city = binding.edtCity.text.toString()
                    mAddress.region = binding.edtRegion.text.toString()
                    mAddress.country = binding.edtCountry.text.toString()
                    create.contact?.addresses?.set(it.key,mAddress)
                }
            }else{
                val mAddress = AddressModel()
                mAddress.street = binding.edtAddress?.text.toString()
                mAddress.postalCode = binding.edtZipcode?.text.toString()
                mAddress.city = binding.edtCity.text.toString()
                mAddress.region = binding.edtRegion.text.toString()
                mAddress.country = binding.edtCountry.text.toString()
                create.contact?.addresses?.set(ConstantValue.HOME,mAddress)
            }

            if (create.contact?.phones?.isNotEmpty() == true){
                create.contact?.phones?.firstNotNullOfOrNull {
                    create.contact?.phones?.set(it.key,binding.edtPhoneNumber.text.toString())
                }
            }else{
                create.contact?.phones?.set(ConstantValue.HOME,binding.edtPhoneNumber.text.toString())
            }

            if (create.contact?.emails?.isNotEmpty()==true){
                create.contact?.emails?.firstNotNullOfOrNull {
                    create.contact?.emails?.set(it.key,binding.edtEmail.text.toString())
                }
            }else{
                create.contact?.emails?.set(ConstantValue.HOME,binding.edtEmail.text.toString())
            }

            if (create.contact?.urls?.isNotEmpty() == true){
                create.contact?.urls?.firstNotNullOfOrNull {
                    create.contact?.urls?.set(0,binding.edtWebite.text.toString())
                }
            }else{
                if (binding.edtWebite.text.toString().isNotEmpty()){
                    create.contact?.urls?.add(binding.edtWebite.text.toString())
                }
            }
            /*Force using VCard*/
            create.code = ""
            create.createType = ParsedResultType.ADDRESSBOOK
            create.barcodeFormat = BarcodeFormat.QR_CODE.name
            Navigator.onMoveToReview(this, create)
        } else {
            Utils.Log(TAG, "error")
        }
    }

    private fun addValidationForEditText() {
        mAwesomeValidation?.addValidation(this, R.id.edtFirstName, RegexTemplate.NOT_EMPTY, R.string.err_first_name)
    }

    private fun focusUI() {
        binding.edtFirstName.requestFocus()
    }

    fun onSetData() {
        val mMapName = general?.contact?.fullName?.stringToMap()
        val mSuffixes = general?.contact?.suffixesName
       if (general?.contact?.givenName.isNullOrEmpty()){
           binding.edtFirstName.setText(mMapName?.get(ConstantKey.FIRST_NAME))
           var mLastName = "${mMapName?.get(ConstantKey.MIDDLE_NAME)} ${mMapName?.get(ConstantKey.LAST_NAME)}"
           if (mSuffixes?.isNotEmpty()==true){
                mLastName = "${mMapName?.get(ConstantKey.MIDDLE_NAME)} ${mMapName?.get(ConstantKey.LAST_NAME)}, $mSuffixes"
           }
           binding.edtLastName.setText(mLastName)
       }else{
           binding.edtFirstName.setText(general?.contact?.givenName)
           var mLastName = "${general?.contact?.middleName} ${general?.contact?.familyName}"
           if (mSuffixes?.isNotEmpty()==true){
               mLastName = "${general?.contact?.middleName} ${general?.contact?.familyName}, $mSuffixes"
           }
           binding.edtLastName.setText(mLastName)
       }

        binding.edtCompany.setText(general?.contact?.company)
        binding.edtJobTitle.setText(general?.contact?.jobTitle)

        general?.contact?.addresses?.firstNotNullOfOrNull {
            if (it.value.street?.isNotEmpty()==true){
                binding.edtAddress.setText(it.value.street)
            }else{
                binding.edtAddress.setText(it.value.address)
            }
            binding.edtZipcode.setText(it.value.postalCode)
            binding.edtRegion.setText(it.value.region)
            binding.edtCity.setText(it.value.city)
            binding.edtCountry.setText(it.value.country)
        }

        general?.contact?.phones?.firstNotNullOfOrNull {
            binding.edtPhoneNumber.setText(it.value)
        }

        general?.contact?.emails?.firstNotNullOfOrNull {
            binding.edtEmail.setText(it.value)
        }

        general?.contact?.urls?.firstNotNullOfOrNull {
            Utils.Log(TAG,"Contact $it")
            binding.edtWebite.setText(it)
        }
        binding.edtFirstName.setSelection(binding.edtFirstName.text?.length ?: 0)
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
        //finish()
    }

    companion object {
        private val TAG = ContactActivity::class.java.simpleName
    }
}