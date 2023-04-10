package tpcreative.co.qrscanner.ui.changedesign

import tpcreative.co.qrscanner.model.ChangeDesignModel

fun ChangeDesignViewModel.getDataResult() : ChangeDesignModel? {
    if (logoSelectedIndex>=0){
        return mLogoList.get(logoSelectedIndex)
    }
    return null
}