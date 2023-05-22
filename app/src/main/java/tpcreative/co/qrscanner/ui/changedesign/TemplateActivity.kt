package tpcreative.co.qrscanner.ui.changedesign

import android.app.Activity
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import tpcreative.co.qrscanner.BuildConfig
import tpcreative.co.qrscanner.common.Navigator
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.activity.BaseActivity
import tpcreative.co.qrscanner.databinding.ActivityTemplateBinding
import tpcreative.co.qrscanner.model.EnumChangeDesignType
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
            premiumPopupForResult.launch(
                Navigator.onPremiumPopupView(this,viewModel.viewModel.getChangeDataReviewToPremiumPopup(mObject),viewModel.viewModel.shape,
                PremiumPopupActivity::class.java,viewModel.viewModel.dataCode,viewModel.viewModel.uuId))
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
        }
    }
}