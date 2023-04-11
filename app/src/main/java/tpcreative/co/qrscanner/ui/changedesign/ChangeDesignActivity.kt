package tpcreative.co.qrscanner.ui.changedesign
import android.graphics.Bitmap
import android.graphics.Path
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import com.github.alexzhirkevich.customqrgenerator.style.Neighbors
import com.github.alexzhirkevich.customqrgenerator.vector.style.*
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.ConstantKey
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
    private val mFragments : MutableList<Fragment> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangeDesignBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
        registerLayout()
    }

    private fun loadFragment(homeFragment: Fragment) {
        val transaction = this.supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frameLayout,homeFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun registerLayout(){
        viewTemplate = TemplateFragment()
        viewColor = ColorFragment()
        viewDots = DotsFragment()
        viewEyes = EyesFragment()
        viewLogo = LogoFragment()
        viewText = TextFragment()

        mFragments.add(viewTemplate)
        mFragments.add(viewColor)
        mFragments.add(viewDots)
        mFragments.add(viewEyes)
        mFragments.add(viewLogo)
        mFragments.add(viewText)
        viewLogo.setSelectedIndex(viewModel.logoSelectedIndex)
        viewLogo.setBinding(object  : LogoFragment.ListenerLogoFragment{
            override fun logoSelectedIndex(index: Int) {
                viewModel.logoSelectedIndex = index
                onHandleResponse()
            }

            override fun getData(): MutableList<ChangeDesignModel> {
                return viewModel.mLogoList
            }
        })
//        viewLogo.load()
        onVisit(EnumView.ALL_HIDDEN)
    }

    private fun onHandleResponse(){
        viewModel.onGenerateQR {
            binding.imgQRCode.setImageDrawable(it)
        }
    }

    @Deprecated("Deprecated in Java", ReplaceWith("onBackPressedDispatcher.onBackPressed()"))
    override fun onBackPressed() {
        super.onBackPressed()
        onBackPressedDispatcher.onBackPressed() //with this line
    }

    fun onVisit(view : EnumView){
        when(view){
            EnumView.TEMPLATE ->{
                binding.doneCancelBar?.tvCancel?.text = getString(R.string.template)
                binding.doneCancelBar?.tvDone?.text = getString(R.string.done)
            }
            EnumView.COLOR ->{
                binding.doneCancelBar?.tvCancel?.text = getString(R.string.color)
                binding.doneCancelBar?.tvDone?.text = getString(R.string.done)
            }
            EnumView.DOTS ->{
                binding.doneCancelBar?.tvCancel?.text = getString(R.string.dots)
                binding.doneCancelBar?.tvDone?.text = getString(R.string.done)
            }
            EnumView.EYES ->{
                binding.doneCancelBar?.tvCancel?.text = getString(R.string.eyes)
                binding.doneCancelBar?.tvDone?.text = getString(R.string.done)
            }
            EnumView.LOGO ->{
                binding.doneCancelBar?.tvCancel?.text = getString(R.string.logo)
                binding.doneCancelBar?.tvDone?.text = getString(R.string.done)
//                val drawable = ContextCompat.getDrawable(this, R.drawable.ic_close)
//                binding.doneCancelBar?.tvCancel?.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
            }
            EnumView.TEXT ->{
                binding.doneCancelBar?.tvCancel?.text = getString(R.string.text)
                binding.doneCancelBar?.tvDone?.text = getString(R.string.done)
            }
            else -> {
                binding.recyclerView.visibility = View.VISIBLE
                binding.doneCancelBar?.tvCancel?.text = getString(R.string.cancel)
                binding.doneCancelBar?.tvDone?.text = getString(R.string.save)
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
        loadFragment(mFragments[position])
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
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