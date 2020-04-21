package tpcreative.co.qrscanner.ui.main;

import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.presenter.Presenter;
import tpcreative.co.qrscanner.ui.scanner.ScannerView;

public class MainPresenter extends Presenter<MainView> {
    public void doShowAds(){
        MainView view = view();
        if (Utils.isDebug() || Utils.isFreeRelease()){
            view.doShowAds(true);
        }else{
            view.doShowAds(false);
        }
    }
}
