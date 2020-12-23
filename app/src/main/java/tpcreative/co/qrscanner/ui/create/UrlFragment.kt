package tpcreative.co.qrscanner.ui.create

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.*
import androidx.appcompat.widget.*
import butterknife.BindView
import com.basgeekball.awesomevalidation.AwesomeValidation
import com.basgeekball.awesomevalidation.ValidationStyle
import com.google.zxing.client.result.ParsedResultType
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.GenerateSingleton.SingletonGenerateListener
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide
import tpcreative.co.qrscanner.model.*

class UrlFragment : BaseActivitySlide(), SingletonGenerateListener {
    var mAwesomeValidation: AwesomeValidation? = null

    @BindView(R.id.edtUrl)
    var edtUrl: AppCompatEditText? = null
    private var save: SaveModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_url)
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
                    Log.d(TAG, "Passed")
                    val create = Create(save)
                    create.url = edtUrl.getText().toString().trim { it <= ' ' }
                    create.createType = ParsedResultType.URI
                    Navigator.onMoveToReview(this, create)
                } else {
                    Log.d(TAG, "error")
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun addValidationForEditText() {
        mAwesomeValidation.addValidation(this, R.id.edtUrl, Patterns.WEB_URL, R.string.err_url)
    }

    fun FocusUI() {
        edtUrl.requestFocus()
    }

    fun clearAndFocusUI() {
        edtUrl.requestFocus()
        edtUrl.setText("")
    }

    fun onSetData() {
        edtUrl.setText(save.url)
    }

    public override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
        mAwesomeValidation = AwesomeValidation(ValidationStyle.BASIC)
        addValidationForEditText()
        if (save != null) {
            onSetData()
        }
        FocusUI()
    }

    public override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
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
        private val TAG = UrlFragment::class.java.simpleName
    }
}