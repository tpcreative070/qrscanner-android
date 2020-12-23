package tpcreative.co.qrscanner.ui.create

import android.content.*
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.*
import butterknife.BindView
import butterknife.OnClick
import com.basgeekball.awesomevalidation.AwesomeValidation
import com.basgeekball.awesomevalidation.ValidationStyle
import com.basgeekball.awesomevalidation.utility.RegexTemplate
import com.basgeekball.awesomevalidation.utility.custom.SimpleCustomValidation
import com.google.zxing.BarcodeFormat
import com.google.zxing.client.result.ParsedResultType
import org.apache.commons.validator.routines.checkdigit.EAN13CheckDigit
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.GenerateSingleton.SingletonGenerateListener
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide
import tpcreative.co.qrscanner.model.*

class BarcodeFragment : BaseActivitySlide(), SingletonGenerateListener, GenerateView {
    var mAwesomeValidation: AwesomeValidation? = null

    @BindView(R.id.edtText)
    var editText: AppCompatEditText? = null

    @BindView(R.id.spinner)
    var spinner: AppCompatSpinner? = null
    private var save: SaveModel? = null
    private var presenter: GeneratePresenter? = null
    private var dataAdapter: ArrayAdapter<FormatTypeModel?>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_barcode)
        val toolbar = findViewById<Toolbar?>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar.setDisplayHomeAsUpEnabled(true)
        presenter = GeneratePresenter()
        presenter.bindView(this)
        presenter.doInitView()
        val bundle = intent.extras
        val mData = bundle.get(getString(R.string.key_data)) as SaveModel?
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

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item.getItemId()) {
            R.id.menu_item_select -> {
                if (mAwesomeValidation.validate()) {
                    Utils.Log(TAG, "Passed")
                    val create = Create(save)
                    create.productId = editText.getText().toString().trim { it <= ' ' }
                    create.createType = ParsedResultType.PRODUCT
                    create.barcodeFormat = presenter.mType.name
                    Navigator.onMoveToReview(this, create)
                } else {
                    Utils.Log(TAG, "error")
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @OnClick(R.id.btnRandom)
    fun onClickedRandom(view: View?) {
        val mValue = Utils.generateRandomDigits(presenter.mLength - 1).toString() + ""
        val mResult = Utils.generateEAN(mValue)
        editText.setText(mResult)
    }

    private fun addValidationForEditText() {
        mAwesomeValidation.addValidation(this, R.id.edtText, RegexTemplate.NOT_EMPTY, R.string.err_text)
        mAwesomeValidation.addValidation(this, R.id.edtText, SimpleCustomValidation { input -> // check if the age is >= 18
            if (presenter.mType == BarcodeFormat.EAN_13) {
                if (input.length == 13) {
                    return@SimpleCustomValidation if (EAN13CheckDigit.EAN13_CHECK_DIGIT.isValid(input)) {
                        true
                    } else false
                } else {
                    return@SimpleCustomValidation false
                }
            }
            true
        }, R.string.warning_barcode_length_13)
        mAwesomeValidation.addValidation(this, R.id.edtText, SimpleCustomValidation { input -> // check if the age is >= 18
            val mValue = Utils.checkSum(input).toString() + ""
            Utils.Log(TAG, mValue)
            if (presenter.mType == BarcodeFormat.EAN_8) {
                if (input.length == 8) {
                    return@SimpleCustomValidation if (Utils.checkGTIN(input)) {
                        true
                    } else false
                } else {
                    return@SimpleCustomValidation false
                }
            }
            true
        }, R.string.warning_barcode_length_8)
    }

    fun FocusUI() {
        editText.requestFocus()
    }

    fun onSetData() {
        editText.setText(save.text)
        if (save.createType == ParsedResultType.PRODUCT.name) {
            if (save.barcodeFormat == BarcodeFormat.EAN_13.name) {
                presenter.mType = BarcodeFormat.EAN_13
                presenter.mLength = 13
                spinner.setSelection(0)
            } else if (save.barcodeFormat == BarcodeFormat.EAN_8.name) {
                presenter.mType = BarcodeFormat.EAN_8
                presenter.mLength = 8
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
        GenerateSingleton.Companion.getInstance().setListener(null)
        Log.d(TAG, "onDestroy")
    }

    public override fun onResume() {
        super.onResume()
        GenerateSingleton.Companion.getInstance().setListener(this)
        Log.d(TAG, "onResume")
    }

    override fun onCompletedGenerate() {
        SaveSingleton.Companion.getInstance().reloadData()
        Utils.Log(TAG, "Finish...........")
        finish()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == Navigator.CREATE) {
            Utils.Log(TAG, "Finish...........")
            SaveSingleton.Companion.getInstance().reloadData()
            finish()
        }
    }

    override fun getContext(): Context? {
        return this
    }

    override fun onSetView() {
        dataAdapter.notifyDataSetChanged()
    }

    override fun onInitView() {
        addItemsOnSpinner()
        addListenerOnSpinnerItemSelection()
        presenter.getBarcodeFormat()
    }

    // add items into spinner dynamically
    fun addItemsOnSpinner() {
        dataAdapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, presenter.mBarcodeFormat)
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.setAdapter(dataAdapter)
    }

    fun addListenerOnSpinnerItemSelection() {
        spinner.setOnItemSelectedListener(CustomOnItemSelectedListener())
    }

    inner class CustomOnItemSelectedListener : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
            val type = dataAdapter.getItem(pos)
            if (type.id === BarcodeFormat.EAN_13.name) {
                editText.setHint(R.string.hint_13)
                presenter.doSetMaxLength(true, editText)
            } else {
                editText.setHint(R.string.hint_8)
                presenter.doSetMaxLength(false, editText)
            }
            presenter.mType = BarcodeFormat.valueOf(type.id)
        }

        override fun onNothingSelected(arg0: AdapterView<*>?) {
            // TODO Auto-generated method stub
        }
    }

    companion object {
        private val TAG = BarcodeFragment::class.java.simpleName
    }
}