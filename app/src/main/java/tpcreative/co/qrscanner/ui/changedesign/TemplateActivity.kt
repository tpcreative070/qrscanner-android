package tpcreative.co.qrscanner.ui.changedesign

import android.app.Activity
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.afollestad.materialdialogs.MaterialDialog
import tpcreative.co.qrscanner.BuildConfig
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.ConstantKey
import tpcreative.co.qrscanner.common.Navigator
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.activity.BaseActivity
import tpcreative.co.qrscanner.databinding.ActivityTemplateBinding
import tpcreative.co.qrscanner.model.EnumChangeDesignType
import tpcreative.co.qrscanner.model.EnumView
import tpcreative.co.qrscanner.ui.premiumpopup.PremiumPopupActivity
import java.util.TreeSet

class TemplateActivity  : BaseActivity(), TemplateAdapter.ItemSelectedListener{
    lateinit var viewModel :TemplateViewModel
    lateinit var binding : ActivityTemplateBinding
    lateinit var adapter : TemplateAdapter
    lateinit var loadedList : TreeSet<String>
    private var isNavigation : Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTemplateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
    }

    override fun onClickItem(position: Int) {
        val mObject = viewModel.mTemplateList.get(position)
        if (mObject.enumChangeDesignType == EnumChangeDesignType.VIP && !Utils.isPremium()){
            if (isNavigation){
                return
            }
            isNavigation = true
            val mEnumView = EnumView.TEMPLATE
            premiumPopupForResult.launch(
                Navigator.onPremiumPopupView(this,viewModel.viewModel.getChangeDataReviewToPremiumPopup(mObject),viewModel.viewModel.shape,
                PremiumPopupActivity::class.java,viewModel.viewModel.dataCode,viewModel.viewModel.uuId, enumView = mEnumView, index = position))
        }else{
            NewChangeDesignActivity.mResultTemplate?.invoke(viewModel.mTemplateList[position])
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        isNavigation = false
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.setReloadTemplate(true)
        Utils.setCurrentChangeDesignCodeVersion(BuildConfig.VERSION_CODE)
    }

    private val premiumPopupForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val index = result.data?.getIntExtra(ConstantKey.KEY_CHANGE_DESIGN_INDEX,0) ?: 0
            val enumView = EnumView.valueOf(result.data?.getStringExtra(ConstantKey.KEY_CHANGE_DESIGN_CURRENT_VIEW) ?: EnumView.LOGO.name)
            showChangedDesign(enumView,index)
        }
    }

    private fun showChangedDesign(enumView: EnumView,position: Int){
        val dialog = MaterialDialog(this)
            .title(R.string.alert)
            .message(R.string.new_design_unlocked)
            .negativeButton(res = R.string.ok){
                when(enumView){
                    EnumView.TEMPLATE ->{
                        NewChangeDesignActivity.mResultTemplate?.invoke(viewModel.mTemplateList[position])
                        finish()
                    }
                    else -> {}
                }
            }
        dialog.show()
    }
}