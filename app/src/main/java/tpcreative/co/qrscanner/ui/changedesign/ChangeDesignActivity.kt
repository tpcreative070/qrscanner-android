package tpcreative.co.qrscanner.ui.changedesign
import android.graphics.Bitmap
import android.graphics.Path
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.graphics.drawable.toBitmap
import com.github.alexzhirkevich.customqrgenerator.style.Neighbors
import com.github.alexzhirkevich.customqrgenerator.vector.style.*
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.ConstantKey
import tpcreative.co.qrscanner.common.ListenerView
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide
import tpcreative.co.qrscanner.common.extension.storeBitmap
import tpcreative.co.qrscanner.databinding.ActivityChangeDesignBinding
import tpcreative.co.qrscanner.model.ChangeDesignModel
import tpcreative.co.qrscanner.model.EnumView
import tpcreative.co.qrscanner.ui.changedesign.fragment.*


class ChangeDesignActivity : BaseActivitySlide() , ChangeDesignAdapter.ItemSelectedListener{
    lateinit var viewModel: ChangeDesignViewModel
    lateinit var binding : ActivityChangeDesignBinding
    var adapter: ChangeDesignAdapter? = null
    private lateinit var viewTemplate : TemplateFragment
    private lateinit var viewColor : ColorFragment
    private lateinit var viewDots : DotsFragment
    private lateinit var viewEyes : EyesFragment
    private lateinit var viewLogo : LogoFragment
    private lateinit var viewText : TextFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangeDesignBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        initUI()
        registerLayout()
    }

    private fun registerLayout(){
        viewTemplate = binding.includeLayoutTemplate.root
        viewTemplate.setBinding(binding.includeLayoutTemplate)
        viewTemplate.setListener(object :ListenerView {
            override fun onDone() {
                onVisit(EnumView.ALL_HIDDEN)
            }
            override fun onClose() {
                onVisit(EnumView.ALL_HIDDEN)
            }
        })

        viewColor = binding.includeLayoutColor.root
        viewColor.setBinding(binding.includeLayoutColor)
        viewColor.setListener(object : ListenerView {
            override fun onDone() {
                onVisit(EnumView.ALL_HIDDEN)
            }
            override fun onClose() {
                onVisit(EnumView.ALL_HIDDEN)
            }
        })

        viewDots = binding.includeLayoutDots.root
        viewDots.setBinding(binding.includeLayoutDots)
        viewDots.setListener(object :ListenerView {
            override fun onDone() {
                onVisit(EnumView.ALL_HIDDEN)
            }
            override fun onClose() {
                onVisit(EnumView.ALL_HIDDEN)
            }
        })

        viewEyes = binding.includeLayoutEyes.root
        viewEyes.setBinding(binding.includeLayoutEyes)
        viewEyes.setListener(object :ListenerView {
            override fun onDone() {
                onVisit(EnumView.ALL_HIDDEN)
            }
            override fun onClose() {
                onVisit(EnumView.ALL_HIDDEN)
            }
        })

        viewLogo = binding.includeLayoutLogo.root
        viewLogo.setBinding(binding.includeLayoutLogo,object  : LogoFragment.ListenerLogoFragment{
            override fun logoSelectedIndex(index: Int) {
                viewModel.logoSelectedIndex = index
                onHandleResponse()
            }

            override fun getData(): MutableList<ChangeDesignModel> {
                return viewModel.mLogoList
            }
        })
        viewLogo.setSelectedIndex(viewModel.logoSelectedIndex)
        viewLogo.load()
        viewLogo.setListener(object :ListenerView {
            override fun onDone() {
                onVisit(EnumView.ALL_HIDDEN)
                onHandleResponse()
            }
            override fun onClose() {
                onVisit(EnumView.ALL_HIDDEN)
            }
        })

        viewText = binding.includeLayoutText.root
        viewText.setBinding(binding.includeLayoutText)
        viewText.setListener(object :ListenerView {
            override fun onDone() {
                onVisit(EnumView.ALL_HIDDEN)
            }
            override fun onClose() {
                onVisit(EnumView.ALL_HIDDEN)
            }
        })
        onVisit(viewModel.enumView)
    }

    private fun onHandleResponse(){
        viewModel.onGenerateQR {
            binding.imgQRCode.setImageDrawable(it)
        }
    }

    private fun onVisit(view : EnumView){
        viewModel.enumView = view
        viewTemplate.visibility = View.INVISIBLE
        viewColor.visibility = View.INVISIBLE
        viewDots.visibility = View.INVISIBLE
        viewEyes.visibility = View.INVISIBLE
        viewLogo.visibility = View.INVISIBLE
        viewText.visibility = View.INVISIBLE
        binding.recyclerView.visibility = View.INVISIBLE
        when(view){
            EnumView.TEMPLATE ->{
                viewTemplate.visibility = View.VISIBLE
            }
            EnumView.COLOR ->{
                viewColor.visibility = View.VISIBLE
            }
            EnumView.DOTS ->{
                viewDots.visibility = View.VISIBLE
            }
            EnumView.EYES ->{
                viewEyes.visibility = View.VISIBLE
            }
            EnumView.LOGO ->{
                viewLogo.visibility = View.VISIBLE
                viewLogo.show()
            }
            EnumView.TEXT ->{
                viewText.visibility = View.VISIBLE
            }
            else -> {
                binding.recyclerView.visibility = View.VISIBLE
            }
        }
    }

    private fun share(){
        val mBitmap =  binding.imgQRCode.drawable.toBitmap(1024,1024,Bitmap.Config.ARGB_8888)
        val mUri = mBitmap.storeBitmap()
        if (mUri != null) {
            Utils.onShareImage(this,mUri)
        }
    }

    override fun onClickItem(position: Int) {
        val mType = adapter?.getItem(position)
        mType?.enumView?.let { onVisit(it) }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.menu_item_png_export -> {
                share()
                return true
            }
            R.id.menu_item_print -> {
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_change_design, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(ConstantKey.key_saved, viewModel.enumView.name)
        outState.putInt(ConstantKey.key_logo_is_selected,viewModel.logoSelectedIndex)
        Utils.Log(TAG,"State saved ${viewModel.enumView.name}")
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        viewModel.enumView = EnumView.valueOf(savedInstanceState.getString(ConstantKey.key_saved) ?:EnumView.ALL_HIDDEN.name)
        viewModel.logoSelectedIndex = savedInstanceState.getInt(ConstantKey.key_logo_is_selected)
        Utils.Log(TAG,"State restore ${viewModel.enumView.name}")
    }

    object Circle : QrVectorPixelShape {
        override fun createPath(size: Float, neighbors: Neighbors): Path = Path().apply {
            addCircle(size/2f, size/2f, size/2, Path.Direction.CW)
        }
    }
}