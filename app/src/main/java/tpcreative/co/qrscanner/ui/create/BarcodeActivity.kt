package tpcreative.co.qrscanner.ui.create
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import com.basgeekball.awesomevalidation.AwesomeValidation
import com.basgeekball.awesomevalidation.ValidationStyle
import com.basgeekball.awesomevalidation.utility.RegexTemplate
import com.basgeekball.awesomevalidation.utility.custom.SimpleCustomValidation
import com.google.zxing.BarcodeFormat
import com.google.zxing.client.result.ParsedResultType
import com.google.zxing.oned.UPCAReader
import kotlinx.android.synthetic.main.activity_barcode.*
import org.apache.commons.validator.Validator
import org.apache.commons.validator.routines.ISBNValidator
import org.apache.commons.validator.routines.checkdigit.CheckDigit
import org.apache.commons.validator.routines.checkdigit.EAN13CheckDigit
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.GenerateSingleton
import tpcreative.co.qrscanner.common.Navigator
import tpcreative.co.qrscanner.common.SaveSingleton
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide
import tpcreative.co.qrscanner.common.extension.validBarcode
import tpcreative.co.qrscanner.model.FormatTypeModel
import tpcreative.co.qrscanner.model.GeneralModel
import tpcreative.co.qrscanner.viewmodel.GenerateViewModel

class BarcodeActivity : BaseActivitySlide(), GenerateSingleton.SingletonGenerateListener,OnEditorActionListener {
    var mAwesomeValidation: AwesomeValidation? = null
    var save: GeneralModel? = null
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
            val create = GeneralModel(save)
            create.textProductIdISNB = edtBarCode.text.toString().trim { it <= ' ' }
            create.barcodeFormat = viewModel.mType?.name
            if (create.barcodeFormat == BarcodeFormat.EAN_13.name){
                val validator = ISBNValidator()
                if (validator.isValid(edtBarCode.text.toString())){
                    create.createType = ParsedResultType.ISBN
                }else{
                    create.createType = ParsedResultType.PRODUCT
                }
            }
            else if (create.barcodeFormat == BarcodeFormat.EAN_8.name || create.barcodeFormat == BarcodeFormat.UPC_A.name || create.barcodeFormat == BarcodeFormat.UPC_E.name){
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

        mAwesomeValidation?.addValidation(this, R.id.edtBarCode, RegexTemplate.NOT_EMPTY, R.string.err_text)
        mAwesomeValidation?.addValidation(this, R.id.edtBarCode, SimpleCustomValidation { input -> // check if the age is >= 18
            if (viewModel.mType == BarcodeFormat.EAN_13) {
                if (input.length == 13) {
                    return@SimpleCustomValidation Utils.validBarcode(input,BarcodeFormat.EAN_13)
                } else {
                    return@SimpleCustomValidation false
                }
            }
            true
        }, R.string.warning_barcode_length_13)

        mAwesomeValidation?.addValidation(this, R.id.edtBarCode, SimpleCustomValidation { input -> // check if the age is >= 18
            if (viewModel.mType == BarcodeFormat.EAN_8) {
                if (input.length == 8) {
                    return@SimpleCustomValidation Utils.validBarcode(input,BarcodeFormat.EAN_8)
                } else {
                    return@SimpleCustomValidation false
                }
            }
            true
        }, R.string.warning_barcode_length_8)

        mAwesomeValidation?.addValidation(this, R.id.edtBarCode, SimpleCustomValidation { input -> // check if the age is >= 18
            if (viewModel.mType == BarcodeFormat.UPC_E) {
                if (input.length == 8) {
                    return@SimpleCustomValidation Utils.validBarcode(input,BarcodeFormat.UPC_E)
                } else {
                    return@SimpleCustomValidation false
                }
            }
            true
        }, R.string.warning_barcode_UPC_E_length_8)

        mAwesomeValidation?.addValidation(this, R.id.edtBarCode, SimpleCustomValidation { input -> // check if the age is >= 18
            if (viewModel.mType == BarcodeFormat.UPC_A) {
                if (input.length == 12) {
                    return@SimpleCustomValidation Utils.validBarcode(input,BarcodeFormat.UPC_A)
                } else {
                    return@SimpleCustomValidation false
                }
            }
            true
        }, R.string.warning_barcode_UPC_A_length_12)

        mAwesomeValidation?.addValidation(this, R.id.edtBarCode, SimpleCustomValidation { input -> // check if the age is >= 18
            if (viewModel.mType == BarcodeFormat.ITF) {
                return@SimpleCustomValidation Utils.validBarcode(input,BarcodeFormat.ITF)
            }
            true
        }, R.string.warning_barcode_ITF)

        mAwesomeValidation?.addValidation(this, R.id.edtBarCode, SimpleCustomValidation { input -> // check if the age is >= 18
            if (viewModel.mType == BarcodeFormat.CODABAR) {
                return@SimpleCustomValidation Utils.validBarcode(input,BarcodeFormat.CODABAR)
            }
            true
        }, R.string.warning_barcode_coda_bar)

        mAwesomeValidation?.addValidation(this, R.id.edtBarCode, SimpleCustomValidation { input -> // check if the age is >= 18
            if (viewModel.mType == BarcodeFormat.DATA_MATRIX) {
                return@SimpleCustomValidation Utils.validBarcode(input,BarcodeFormat.DATA_MATRIX)
            }
            true
        }, R.string.warning_barcode_data_matrix)

        mAwesomeValidation?.addValidation(this, R.id.edtBarCode, SimpleCustomValidation { input -> // check if the age is >= 18
            if (viewModel.mType == BarcodeFormat.PDF_417) {
                return@SimpleCustomValidation Utils.validBarcode(input,BarcodeFormat.PDF_417)
            }
            true
        }, R.string.warning_barcode_data_PDF_471)

        mAwesomeValidation?.addValidation(this, R.id.edtBarCode, SimpleCustomValidation { input -> // check if the age is >= 18
            if (viewModel.mType == BarcodeFormat.AZTEC) {
                return@SimpleCustomValidation Utils.validBarcode(input,BarcodeFormat.AZTEC)
            }
            true
        }, R.string.warning_barcode_data_aztec)

        mAwesomeValidation?.addValidation(this, R.id.edtBarCode, SimpleCustomValidation { input -> // check if the age is >= 18
            if (viewModel.mType == BarcodeFormat.CODE_128) {
                return@SimpleCustomValidation Utils.validBarcode(input,BarcodeFormat.CODE_128)
            }
            true
        }, R.string.warning_barcode_data_code_128)

        mAwesomeValidation?.addValidation(this, R.id.edtBarCode, SimpleCustomValidation { input -> // check if the age is >= 18
            if (viewModel.mType == BarcodeFormat.CODE_39) {
                return@SimpleCustomValidation Utils.validBarcode(input,BarcodeFormat.CODE_39)
            }
            true
        }, R.string.warning_barcode_data_code_39)

        mAwesomeValidation?.addValidation(this, R.id.edtBarCode, SimpleCustomValidation { input -> // check if the age is >= 18
            if (viewModel.mType == BarcodeFormat.CODE_93) {
                return@SimpleCustomValidation Utils.validBarcode(input,BarcodeFormat.CODE_93)
            }
            true
        }, R.string.warning_barcode_data_code_93)
    }

    private fun focusUI() {
        edtBarCode.requestFocus()
    }

    fun onSetData() {
        edtBarCode.setText(save?.textProductIdISNB)
        if (save?.barcodeFormat == BarcodeFormat.EAN_8.name) {
            viewModel.mType = BarcodeFormat.EAN_8
            viewModel.mLength = 8
            spinner.setSelection(0)
        }
        else if (save?.barcodeFormat == BarcodeFormat.EAN_13.name) {
            viewModel.mType = BarcodeFormat.EAN_13
            viewModel.mLength = 13
            spinner.setSelection(1)
        }
        else if (save?.barcodeFormat == BarcodeFormat.UPC_A.name){
            viewModel.mType = BarcodeFormat.UPC_A
            viewModel.mLength = 12
            spinner.setSelection(2)
        }
        else if (save?.barcodeFormat == BarcodeFormat.UPC_E.name){
            viewModel.mType = BarcodeFormat.UPC_E
            viewModel.mLength = 8
            spinner.setSelection(3)
        }else if (save?.barcodeFormat == BarcodeFormat.CODABAR.name){
            viewModel.mType = BarcodeFormat.CODABAR
            viewModel.mLength = 40
            spinner.setSelection(4)
        }else if (save?.barcodeFormat == BarcodeFormat.DATA_MATRIX.name){
            viewModel.mType = BarcodeFormat.DATA_MATRIX
            viewModel.mLength = 50
            spinner.setSelection(5)
        }
        else if (save?.barcodeFormat == BarcodeFormat.PDF_417.name){
            viewModel.mType = BarcodeFormat.PDF_417
            viewModel.mLength = 50
            spinner.setSelection(6)
        }
        else if (save?.barcodeFormat == BarcodeFormat.AZTEC.name){
            viewModel.mType = BarcodeFormat.AZTEC
            viewModel.mLength = 50
            spinner.setSelection(7)
        }
        else if (save?.barcodeFormat == BarcodeFormat.CODE_128.name){
            viewModel.mType = BarcodeFormat.CODE_128
            viewModel.mLength = 50
            spinner.setSelection(8)
        }
        else if (save?.barcodeFormat == BarcodeFormat.CODE_39.name){
            viewModel.mType = BarcodeFormat.CODE_39
            viewModel.mLength = 50
            spinner.setSelection(9)
        }
        else if (save?.barcodeFormat == BarcodeFormat.CODE_93.name){
            viewModel.mType = BarcodeFormat.CODE_93
            viewModel.mLength = 50
            spinner.setSelection(10)
        }
        else if (save?.barcodeFormat == BarcodeFormat.ITF.name){
            viewModel.mType = BarcodeFormat.ITF
            viewModel.mLength = 50
            spinner.setSelection(11)
        }
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

    fun onSetView() {
        dataAdapter?.notifyDataSetChanged()
        onSetData()
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
            viewModel.mType = BarcodeFormat.valueOf(type?.id ?:BarcodeFormat.QR_CODE.name)
            if (viewModel.isText(save?.textProductIdISNB)){
                edtBarCode.setText(save?.textProductIdISNB)
                val mLength = save?.textProductIdISNB?.length?:0
                if (viewModel.mLength >= mLength){
                    edtBarCode.setSelection(mLength)
                }else{
                    edtBarCode.setSelection(save?.textProductIdISNB?.substring(0,viewModel.mLength)?.length ?:0)
                }
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