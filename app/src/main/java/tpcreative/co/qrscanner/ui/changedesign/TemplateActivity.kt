package tpcreative.co.qrscanner.ui.changedesign

import android.os.Bundle
import tpcreative.co.qrscanner.common.activity.BaseActivity
import tpcreative.co.qrscanner.databinding.ActivityTemplateBinding

class TemplateActivity  : BaseActivity(){

    private lateinit var binding : ActivityTemplateBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTemplateBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}