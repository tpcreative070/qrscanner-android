package tpcreative.co.qrscanner.ui.create
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import com.basgeekball.awesomevalidation.AwesomeValidation
import com.basgeekball.awesomevalidation.ValidationStyle
import com.basgeekball.awesomevalidation.utility.RegexTemplate
import com.google.zxing.client.result.ParsedResultType
import kotlinx.android.synthetic.main.fragment_text.*
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.GenerateSingleton.SingletonGenerateListener
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide
import tpcreative.co.qrscanner.common.extension.serializable
import tpcreative.co.qrscanner.model.*

class TextFragment : BaseActivitySlide(), SingletonGenerateListener, OnEditorActionListener {
    var mAwesomeValidation: AwesomeValidation? = null
    private var save: SaveModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_text)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val mData = intent.serializable(getString(R.string.key_data),SaveModel::class.java)
        if (mData != null) {
            save = mData
            onSetData()
        } else {
            Utils.Log(TAG, "Data is null")
        }
        edtText.setOnEditorActionListener(this)
    }

    override fun onEditorAction(p0: TextView?, p1: Int, p2: KeyEvent?): Boolean {
        if (p1 == EditorInfo.IME_ACTION_DONE || p2?.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
            onSave()
           return  true
        }
        return false
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

    private fun onSave(){
        hideSoftKeyBoard()
        if (mAwesomeValidation?.validate() == true) {
            val create = CreateModel(save)
            create.text = edtText.text.toString().trim { it <= ' ' }
            create.createType = ParsedResultType.TEXT
            Navigator.onMoveToReview(this, create)
        } else {
            Utils.Log(TAG, "error")
        }
    }

    private fun hideSoftKeyBoard() {
        val imm: InputMethodManager =
           getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        if (imm.isAcceptingText) { // verify if the soft keyboard is open
            imm.hideSoftInputFromWindow(this.currentFocus?.windowToken, 0)
        }
    }

    private fun addValidationForEditText() {
        mAwesomeValidation?.addValidation(this, R.id.edtText, RegexTemplate.NOT_EMPTY, R.string.err_text)
    }

    private fun FocusUI() {
        edtText.requestFocus()
    }

    fun onSetData() {
        edtText.setText(save?.text)
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

    companion object {
        private val TAG = TextFragment::class.java.simpleName
    }
}