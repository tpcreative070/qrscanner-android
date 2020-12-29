package tpcreative.co.qrscanner.ui.create
import android.content.*
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.*
import com.basgeekball.awesomevalidation.AwesomeValidation
import com.basgeekball.awesomevalidation.ValidationStyle
import com.basgeekball.awesomevalidation.utility.RegexTemplate
import com.basgeekball.awesomevalidation.utility.custom.SimpleCustomValidation
import com.google.zxing.BarcodeFormat
import com.google.zxing.client.result.ParsedResultType
import kotlinx.android.synthetic.main.fragment_barcode.*
import org.apache.commons.validator.routines.checkdigit.EAN13CheckDigit
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide
import tpcreative.co.qrscanner.model.*
import tpcreative.co.qrscanner.viewmodel.GenerateViewModel

class BarcodeFragment : BaseActivitySlide(), GenerateSingleton.SingletonGenerateListener {
    var mAwesomeValidation: AwesomeValidation? = null
    var save: SaveModel? = null
    var dataAdapter: ArrayAdapter<FormatTypeModel>? = null
    lateinit var viewModel : GenerateViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_barcode)
        initUI()
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
                    create.productId = edtText.getText().toString().trim { it <= ' ' }
                    create.createType = ParsedResultType.PRODUCT
                    create.barcodeFormat = viewModel.mType?.name
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
            val mValue = Utils.checkSum(input).toString() + ""
            Utils.Log(TAG, mValue)
            if (viewModel.mType == BarcodeFormat.EAN_8) {
                if (input.length == 8) {
                    return@SimpleCustomValidation Utils.checkGTIN(input)
                } else {
                    return@SimpleCustomValidation false
                }
            }
            true
        }, R.string.warning_barcode_length_8)
    }

    fun FocusUI() {
        edtText.requestFocus()
    }

    fun onSetData() {
        edtText.setText(save?.text)
        if (save?.createType == ParsedResultType.PRODUCT.name) {
            if (save?.barcodeFormat == BarcodeFormat.EAN_13.name) {
                viewModel.mType = BarcodeFormat.EAN_13
                viewModel.mLength = 13
                spinner.setSelection(0)
            } else if (save?.barcodeFormat == BarcodeFormat.EAN_8.name) {
                viewModel.mType = BarcodeFormat.EAN_8
                viewModel.mLength = 8
                spinner.setSelection(1)
            }
        }
    }

    public override fun onStart() {
        super.onStart()
        Utils.Log(TAG, "onStart")
        mAwesomeValidation = AwesomeValidation(ValidationStyle.BASIC)
        addValidationForEditText()
        if (save != null) {
            onSetData()
        }
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

    fun onSetView() {
        dataAdapter?.notifyDataSetChanged()
    }

    fun onInitView() {
        addItemsOnSpinner()
        addListenerOnSpinnerItemSelection()
        viewModel.getBarcodeFormat()
    }

    // add items into spinner dynamically
    fun addItemsOnSpinner() {
        dataAdapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, viewModel.mBarcodeFormat)
        dataAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = dataAdapter
    }

    fun addListenerOnSpinnerItemSelection() {
        spinner.onItemSelectedListener = CustomOnItemSelectedListener()
    }

    inner class CustomOnItemSelectedListener : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
            val type = dataAdapter?.getItem(pos)
            if (type?.id === BarcodeFormat.EAN_13.name) {
                edtText.setHint(R.string.hint_13)
                viewModel.doSetMaxLength(true, edtText)
            } else {
                edtText.setHint(R.string.hint_8)
                viewModel.doSetMaxLength(false, edtText)
            }
            viewModel.mType = BarcodeFormat.valueOf(type?.id ?:"")
        }

        override fun onNothingSelected(arg0: AdapterView<*>?) {
            // TODO Auto-generated method stub
        }
    }

    companion object {
        private val TAG = BarcodeFragment::class.java.simpleName
    }
}