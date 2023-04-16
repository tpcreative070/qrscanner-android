package tpcreative.co.qrscanner.ui.changedesign

import tpcreative.co.qrscanner.model.LogoModel

fun ChangeDesignViewModel.getDataResult() : LogoModel? {
    if (logoSelectedIndex>=0){
        return mLogoList.get(logoSelectedIndex)
    }
    return null
}