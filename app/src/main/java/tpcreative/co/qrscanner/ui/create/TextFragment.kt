package tpcreative.co.qrscanner.ui.create

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.widget.*
import butterknife.BindView
import com.basgeekball.awesomevalidation.AwesomeValidation
import com.basgeekball.awesomevalidation.ValidationStyle
import com.basgeekball.awesomevalidation.utility.RegexTemplate
import com.google.zxing.client.result.ParsedResultType
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.GenerateSingleton.SingletonGenerateListener
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide
import tpcreative.co.qrscanner.model.*

class TextFragment : BaseActivitySlide(), SingletonGenerateListener {
    var mAwesomeValidation: AwesomeValidation? = null

    @BindView(R.id.edtText)
    var editText: AppCompatEditText? = null
    private var save: SaveModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_text)
        val toolbar = findViewById<Toolbar?>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar.setDisplayHomeAsUpEnabled(true)
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
                    create.text = editText.getText().toString().trim { it <= ' ' }
                    create.createType = ParsedResultType.TEXT
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
        mAwesomeValidation.addValidation(this, R.id.edtText, RegexTemplate.NOT_EMPTY, R.string.err_text)
    }

    fun FocusUI() {
        editText.requestFocus()
    }

    fun onSetData() {
        editText.setText(save.text)
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

    companion object {
        private val TAG = TextFragment::class.java.simpleName
    }
}