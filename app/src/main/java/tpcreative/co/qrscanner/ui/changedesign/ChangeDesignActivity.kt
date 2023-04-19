package tpcreative.co.qrscanner.ui.changedesign
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Path
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import com.github.alexzhirkevich.customqrgenerator.style.Neighbors
import com.github.alexzhirkevich.customqrgenerator.vector.style.*
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.ConstantKey
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide
import tpcreative.co.qrscanner.common.extension.serializable
import tpcreative.co.qrscanner.common.extension.storeBitmap
import tpcreative.co.qrscanner.common.extension.toJson
import tpcreative.co.qrscanner.common.view.crop.Crop
import tpcreative.co.qrscanner.databinding.ActivityChangeDesignBinding
import tpcreative.co.qrscanner.model.EnumChangeDesignType
import tpcreative.co.qrscanner.model.EnumShape
import tpcreative.co.qrscanner.model.EnumView
import tpcreative.co.qrscanner.model.LogoModel
import tpcreative.co.qrscanner.ui.changedesign.fragment.*
import java.io.File


class ChangeDesignActivity : BaseActivitySlide() , ChangeDesignAdapter.ItemSelectedListener{
    lateinit var viewModel: ChangeDesignViewModel
    lateinit var binding : ActivityChangeDesignBinding
    var adapter: ChangeDesignAdapter? = null
    private lateinit var viewTemplate : TemplateFragment
    private lateinit var viewColor : ColorFragment
    private lateinit var viewDots : DotsFragment
    private lateinit var viewEyes : EyesFragment
    lateinit var viewLogo : LogoFragment
    private lateinit var viewText : TextFragment
    private val mFragments : MutableList<Fragment> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangeDesignBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
        if (savedInstanceState != null) {
            viewModel.index = savedInstanceState.getInt(ConstantKey.KEY_CHANGE_DESIGN_INDEX)
            viewModel.enumView = EnumView.valueOf(savedInstanceState.getString(ConstantKey.KEY_CHANGE_DESIGN_VIEW) ?:EnumView.ALL_HIDDEN.name)
            viewModel.indexLogo = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_LOGO) ?: viewModel.defaultObject()
            viewModel.changeDesignSave = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_SAVE) ?: viewModel.changeDesignSave
            viewModel.changeDesignReview = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_REVIEW) ?: viewModel.changeDesignReview
            viewModel.uri = Uri.parse(savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_URI))
            viewModel.shape =  EnumShape.valueOf(savedInstanceState.getString(ConstantKey.KEY_CHANGE_DESIGN_SHAPE) ?:EnumShape.ORIGINAL.name)
            Utils.Log(TAG,"State instance saveInstanceState has value")
            val bitmap = viewModel.uri?.let {
                contentResolver.openInputStream(it).use { data ->
                    BitmapFactory.decodeStream(data)
                }
            }
            viewModel.onUpdateBitmap(bitmap)
            registerLayout()
            onGenerateQRReview()
        }else{
            Utils.Log(TAG,"State instance saveInstanceState null")
            registerLayout()
        }
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
        viewLogo.setSelectedIndex(viewModel.indexLogo,viewModel.shape)
        Utils.Log(TAG,"State instance register ${viewModel.indexLogo.toJson()}")
        viewLogo.setBinding(object  : LogoFragment.ListenerLogoFragment{
            override fun logoSelectedIndex(index: Int,selectedObject : LogoModel) {
                Utils.Log(TAG,"Selected index ${selectedObject.toJson()}")
                if (selectedObject.enumChangeDesignType ==EnumChangeDesignType.VIP){
                    viewModel.indexLogo = selectedObject
                    viewModel.selectedIndexOnReview()
                    onGetGallery()
                }else{
                    viewModel.onCleanBitMap()
                    viewModel.indexLogo = selectedObject
                    viewModel.selectedIndexOnReview()
                    onGenerateQRReview()
                }
            }

            override fun logoSelectedIndex(
                index: Int,
                enumShape: EnumShape?,
                selectedObject: LogoModel
            ) {
                viewModel.indexLogo = selectedObject
                viewModel.shape = enumShape ?: EnumShape.ORIGINAL
                viewModel.selectedIndexOnReview()
                onGenerateQRReview()
            }

            override fun getData(): MutableList<LogoModel> {
                return viewModel.mLogoList
            }
        })
        Utils.Log(TAG,"State instance enumview ${viewModel.enumView.name}")
        Utils.Log(TAG,"State instance index ${viewModel.index}")
        onVisit(viewModel.enumView)
        if (viewModel.index>=0){
            loadFragment(mFragments[viewModel.index])
        }
    }

    fun onGenerateQRReview(){
        viewModel.onGenerateQR {
            binding.imgQRCode.setImageDrawable(null)
            binding.imgQRCode.setImageDrawable(it)
        }
    }

    fun onVisit(view : EnumView){
        viewModel.enumView = view
        when(view){
            EnumView.TEMPLATE ->{
                binding.doneCancelBar.tvCancel.text = getString(R.string.template)
                binding.doneCancelBar.btnSave.text = getString(R.string.done)
                binding.doneCancelBar.imgCancel.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_close))
                binding.doneCancelBar.btnSave.visibility = View.INVISIBLE
                binding.doneCancelBar.imgDone.visibility = View.VISIBLE
            }
            EnumView.COLOR ->{
                binding.doneCancelBar.tvCancel.text = getString(R.string.color)
                binding.doneCancelBar.btnSave.text = getString(R.string.done)
                binding.doneCancelBar.imgCancel.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_close))
                binding.doneCancelBar.btnSave.visibility = View.INVISIBLE
                binding.doneCancelBar.imgDone.visibility = View.VISIBLE
            }
            EnumView.DOTS ->{
                binding.doneCancelBar.tvCancel.text = getString(R.string.dots)
                binding.doneCancelBar.btnSave.text = getString(R.string.done)
                binding.doneCancelBar.imgCancel.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_close))
                binding.doneCancelBar.btnSave.visibility = View.INVISIBLE
                binding.doneCancelBar.imgDone.visibility = View.VISIBLE
            }
            EnumView.EYES ->{
                binding.doneCancelBar.tvCancel.text = getString(R.string.eyes)
                binding.doneCancelBar.btnSave.text = getString(R.string.done)
                binding.doneCancelBar.imgCancel.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_close))
                binding.doneCancelBar.btnSave.visibility = View.INVISIBLE
                binding.doneCancelBar.imgDone.visibility = View.VISIBLE
            }
            EnumView.LOGO ->{
                binding.doneCancelBar.tvCancel.text = getString(R.string.logo)
                binding.doneCancelBar.btnSave.text = getString(R.string.done)
                binding.doneCancelBar.imgCancel.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_close))
                binding.doneCancelBar.btnSave.visibility = View.INVISIBLE
                binding.doneCancelBar.imgDone.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.INVISIBLE
            }
            EnumView.TEXT ->{
                binding.doneCancelBar.tvCancel.text = getString(R.string.text)
                binding.doneCancelBar.btnSave.text = getString(R.string.done)
                binding.doneCancelBar.imgCancel.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_close))
                binding.doneCancelBar.btnSave.visibility = View.INVISIBLE
                binding.doneCancelBar.imgDone.visibility = View.VISIBLE
            }
            else -> {
                binding.recyclerView.visibility = View.VISIBLE
                binding.doneCancelBar.tvCancel.text = getString(R.string.change_design)
                binding.doneCancelBar.imgCancel.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_baseline_arrow_back_24))
                binding.doneCancelBar.btnSave.text = getString(R.string.save)
                binding.doneCancelBar.btnSave.visibility = View.VISIBLE
                binding.doneCancelBar.imgDone.visibility = View.INVISIBLE
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
        viewModel.index = position
        Utils.Log(TAG,"State instance click $position")
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
        outState.putInt(ConstantKey.KEY_CHANGE_DESIGN_INDEX,viewModel.index)
        outState.putString(ConstantKey.KEY_CHANGE_DESIGN_VIEW, viewModel.enumView.name)
        outState.putSerializable(ConstantKey.KEY_CHANGE_DESIGN_LOGO,viewModel.indexLogo)
        outState.putSerializable(ConstantKey.KEY_CHANGE_DESIGN_REVIEW,viewModel.changeDesignSave)
        outState.putSerializable(ConstantKey.KEY_CHANGE_DESIGN_REVIEW,viewModel.changeDesignReview)
        outState.putSerializable(ConstantKey.KEY_CHANGE_DESIGN_URI,viewModel.uri.toString())
        outState.putSerializable(ConstantKey.KEY_CHANGE_DESIGN_SHAPE,viewModel.shape.name)
        Utils.Log(TAG,"State instance save ${viewModel.indexLogo.toJson()}")
        Utils.Log(TAG,"State instance save index ${viewModel.index}")
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        viewModel.index = savedInstanceState.getInt(ConstantKey.KEY_CHANGE_DESIGN_INDEX)
        viewModel.enumView = EnumView.valueOf(savedInstanceState.getString(ConstantKey.KEY_CHANGE_DESIGN_VIEW) ?:EnumView.ALL_HIDDEN.name)
        viewModel.indexLogo = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_LOGO) ?: viewModel.defaultObject()
        viewModel.changeDesignSave = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_SAVE) ?: viewModel.changeDesignSave
        viewModel.changeDesignReview = savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_REVIEW) ?: viewModel.changeDesignReview
        viewModel.uri = Uri.parse(savedInstanceState.serializable(ConstantKey.KEY_CHANGE_DESIGN_URI))
        viewModel.shape =  EnumShape.valueOf(savedInstanceState.getString(ConstantKey.KEY_CHANGE_DESIGN_SHAPE) ?:EnumShape.ORIGINAL.name)
        Utils.Log(TAG,"State instance restore ${viewModel.indexLogo.toJson()}")
        Utils.Log(TAG,"State instance restore index ${viewModel.index}")
        super.onRestoreInstanceState(savedInstanceState)
    }

    private val pickGalleryForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            Utils.Log(TAG, "REQUEST_PICK")
            beginCrop(result.data?.data)
        }
    }

    private fun beginCrop(source: Uri?) {
        val destination = Uri.fromFile(File(this.cacheDir, "cropped"))
        cropForResult.launch(Crop.of(source, destination)?.asSquare()?.start(this,true))
    }

    private val cropForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            Utils.Log(TAG, "REQUEST_CROP")
            handleCrop(result.resultCode, result.data)
        }
    }

    private fun handleCrop(resultCode: Int, result: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val mData: Uri? = Crop.getOutputUri(result)
            Utils.Log(TAG,"Result cropped ${mData.toString()}")
            val bitmap = mData?.let {
                viewModel.uri = mData
                contentResolver.openInputStream(it).use { data ->
                    BitmapFactory.decodeStream(data)
                }
            }
            viewModel.onUpdateBitmap(bitmap)
            onGenerateQRReview()
        }
    }

    private fun onGetGallery() {
        pickGalleryForResult.launch(Crop.getImagePicker())
    }

    object Circle : QrVectorPixelShape {
        override fun createPath(size: Float, neighbors: Neighbors): Path = Path().apply {
            addCircle(size/2f, size/2f, size/2, Path.Direction.CW)
        }
    }
}