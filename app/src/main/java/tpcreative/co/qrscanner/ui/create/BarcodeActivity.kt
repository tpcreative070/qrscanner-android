package tpcreative.co.qrscanner.ui.create
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import com.basgeekball.awesomevalidation.AwesomeValidation
import com.basgeekball.awesomevalidation.ValidationStyle
import com.basgeekball.awesomevalidation.utility.RegexTemplate
import com.basgeekball.awesomevalidation.utility.custom.SimpleCustomValidation
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.client.result.ParsedResultType
import kotlinx.android.synthetic.main.activity_barcode.*
import org.apache.commons.validator.routines.checkdigit.EAN13CheckDigit
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide
import tpcreative.co.qrscanner.model.*
import tpcreative.co.qrscanner.viewmodel.GenerateViewModel

class BarcodeActivity : BaseActivitySlide(), GenerateSingleton.SingletonGenerateListener,OnEditorActionListener {
    var mAwesomeValidation: AwesomeValidation? = null
    var save: SaveModel? = null
    var dataAdapter: ArrayAdapter<FormatTypeModel>? = null
    lateinit var viewModel : GenerateViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode)
        initUI()
        edtBarCode.setOnEditorActionListener(this)
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
            create.productId = edtBarCode.text.toString().trim { it <= ' ' }
            create.barcodeFormat = viewModel.mType?.name
            if (create.barcodeFormat == BarcodeFormat.EAN_8.name || create.barcodeFormat == BarcodeFormat.EAN_13.name || create.barcodeFormat == BarcodeFormat.UPC_A.name || create.barcodeFormat == BarcodeFormat.UPC_E.name){
                create.createType = ParsedResultType.PRODUCT
            }else{
                create.createType = ParsedResultType.TEXT
            }
            Navigator.onMoveToReview(this, create)
        } else {
            Utils.Log(TAG, "error")
        }
    }

    private fun addValidationForEditText() {
        mAwesomeValidation?.addValidation(this, R.id.edtText, RegexTemplate.NOT_EMPTY, R.string.err_text)
        mAwesomeValidation?.addValidation(this, R.id.edtText, SimpleCustomValidation { input -> // check if the age is >= 18
            if (viewModel.mType == BarcodeFormat.EAN_13) {
                if (input.length == 13) {
                    return@SimpleCustomValidation EAN13CheckDigit.EAN13_CHECK_DIGIT.isValid(input)
                } else {
                    return@SimpleCustomValidation false
                }
            }
            true
        }, R.string.warning_barcode_length_13)
        mAwesomeValidation?.addValidation(this, R.id.edtText, SimpleCustomValidation { input -> // check if the age is >= 18
            if (viewModel.mType == BarcodeFormat.EAN_8) {
                if (input.length == 8) {
                    return@SimpleCustomValidation Utils.checkGTIN(input)
                } else {
                    return@SimpleCustomValidation false
                }
            }
            true
        }, R.string.warning_barcode_length_8)

        mAwesomeValidation?.addValidation(this, R.id.edtText, SimpleCustomValidation { input -> // check if the age is >= 18
            if (viewModel.mType == BarcodeFormat.UPC_E) {
                if (input.length == 8) {
                    return@SimpleCustomValidation Utils.checkGTIN(input)
                } else {
                    return@SimpleCustomValidation false
                }
            }
            true
        }, R.string.warning_barcode_UPC_E_length_8)
        mAwesomeValidation?.addValidation(this, R.id.edtText, SimpleCustomValidation { input -> // check if the age is >= 18
            if (viewModel.mType == BarcodeFormat.UPC_A) {
                if (input.length == 12) {
                    return@SimpleCustomValidation Utils.checkGTIN(input)
                } else {
                    return@SimpleCustomValidation false
                }
            }
            true
        }, R.string.warning_barcode_UPC_A_length_12)
        mAwesomeValidation?.addValidation(this, R.id.edtText, SimpleCustomValidation { input -> // check if the age is >= 18
            if (viewModel.mType == BarcodeFormat.ITF) {
                return@SimpleCustomValidation Utils.checkITF(input)
            }
            true
        }, R.string.warning_barcode_ITF)

    }

    private fun focusUI() {
        edtBarCode.requestFocus()
    }

    fun onSetData() {
        edtBarCode.setText(save?.text)
        if (save?.createType == ParsedResultType.PRODUCT.name) {
            if (save?.barcodeFormat == BarcodeFormat.EAN_13.name) {
                viewModel.mType = BarcodeFormat.EAN_13
                viewModel.mLength = 13
                spinner.setSelection(0)
            } else if (save?.barcodeFormat == BarcodeFormat.EAN_8.name) {
                viewModel.mType = BarcodeFormat.EAN_8
                viewModel.mLength = 8
                spinner.setSelection(1)
            }else if (save?.barcodeFormat == BarcodeFormat.UPC_E.name){
                viewModel.mType = BarcodeFormat.UPC_E
                viewModel.mLength = 8
                spinner.setSelection(1)
            }else if (save?.barcodeFormat == BarcodeFormat.UPC_A.name){
                viewModel.mType = BarcodeFormat.UPC_A
                viewModel.mLength = 12
                spinner.setSelection(1)
            }else if (save?.barcodeFormat == BarcodeFormat.CODABAR.name){
                viewModel.mType = BarcodeFormat.CODABAR
                viewModel.mLength = 40
                spinner.setSelection(1)
            }else if (save?.barcodeFormat == BarcodeFormat.DATA_MATRIX.name){
                viewModel.mType = BarcodeFormat.DATA_MATRIX
                viewModel.mLength = 50
                spinner.setSelection(1)
            }
            else if (save?.barcodeFormat == BarcodeFormat.PDF_417.name){
                viewModel.mType = BarcodeFormat.PDF_417
                viewModel.mLength = 50
                spinner.setSelection(1)
            }
            else if (save?.barcodeFormat == BarcodeFormat.AZTEC.name){
                viewModel.mType = BarcodeFormat.AZTEC
                viewModel.mLength = 50
                spinner.setSelection(1)
            }
            else if (save?.barcodeFormat == BarcodeFormat.CODE_128.name){
                viewModel.mType = BarcodeFormat.CODE_128
                viewModel.mLength = 50
                spinner.setSelection(1)
            }
            else if (save?.barcodeFormat == BarcodeFormat.CODE_39.name){
                viewModel.mType = BarcodeFormat.CODE_39
                viewModel.mLength = 50
                spinner.setSelection(1)
            }
            else if (save?.barcodeFormat == BarcodeFormat.CODE_93.name){
                viewModel.mType = BarcodeFormat.CODE_93
                viewModel.mLength = 50
                spinner.setSelection(1)
            }
            else if (save?.barcodeFormat == BarcodeFormat.ITF.name){
                viewModel.mType = BarcodeFormat.ITF
                viewModel.mLength = 50
                spinner.setSelection(1)
            }
            Utils.Log(TAG,"Save ${Gson().toJson(save)}")
        }
        edtBarCode.setSelection(edtBarCode.text?.length ?: 0)
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

    fun onSetView() {
        dataAdapter?.notifyDataSetChanged()
    }

    fun onInitView() {
        addItemsOnSpinner()
        addListenerOnSpinnerItemSelection()
        getBarcodeFormat()
    }

    // add items into spinner dynamically
    private fun addItemsOnSpinner() {
        dataAdapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, viewModel.mBarcodeFormat)
        dataAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = dataAdapter
    }

    private fun addListenerOnSpinnerItemSelection() {
        spinner.onItemSelectedListener = CustomOnItemSelectedListener()
    }

    inner class CustomOnItemSelectedListener : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
            val type = dataAdapter?.getItem(pos)
            if (type?.id === BarcodeFormat.EAN_13.name) {
                edtBarCode.setHint(R.string.hint_13)
                viewModel.doSetMaxLength(BarcodeFormat.EAN_13, edtBarCode)
            }
            else if (type?.id === BarcodeFormat.EAN_8.name){
                edtBarCode.setHint(R.string.hint_8)
                viewModel.doSetMaxLength(BarcodeFormat.EAN_8, edtBarCode)
            }
            else if (type?.id === BarcodeFormat.UPC_E.name){
                edtBarCode.setHint(R.string.hint_8)
                viewModel.doSetMaxLength(BarcodeFormat.UPC_E, edtBarCode)
            }else if (type?.id === BarcodeFormat.UPC_A.name){
                edtBarCode.setHint(R.string.hint_12)
                viewModel.doSetMaxLength(BarcodeFormat.UPC_A, edtBarCode)
            }
            else if (type?.id === BarcodeFormat.CODABAR.name){
                edtBarCode.setHint(R.string.hint_digits)
                viewModel.doSetMaxLength(BarcodeFormat.CODABAR, edtBarCode)
            }
            else if (type?.id === BarcodeFormat.CODE_39.name){
                edtBarCode.setHint(R.string.hint_uppercase_characters)
                viewModel.doSetMaxLength(BarcodeFormat.CODE_39, edtBarCode)
            }
            else if (type?.id === BarcodeFormat.CODE_93.name){
                edtBarCode.setHint(R.string.hint_uppercase_characters)
                viewModel.doSetMaxLength(BarcodeFormat.CODE_93, edtBarCode)
            }
            else if (type?.id === BarcodeFormat.ITF.name){
                edtBarCode.setHint(R.string.hint_digits)
                viewModel.doSetMaxLength(BarcodeFormat.ITF, edtBarCode)
            }
            else {
                edtBarCode.setHint(R.string.hint_characters)
                viewModel.doSetMaxLength(BarcodeFormat.DATA_MATRIX, edtBarCode)
            }
            viewModel.mType = BarcodeFormat.valueOf(type?.id ?:"")
            if (viewModel.isText(save?.text)){
                edtBarCode.setText(save?.text)
                edtBarCode.requestFocus()
            }
        }

        override fun onNothingSelected(arg0: AdapterView<*>?) {
            // TODO Auto-generated method stub
        }
    }

    companion object {
        private val TAG = BarcodeActivity::class.java.simpleName
    }
}