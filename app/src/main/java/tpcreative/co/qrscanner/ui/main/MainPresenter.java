package tpcreative.co.qrscanner.ui.main;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.presenter.Presenter;

public class MainPresenter extends Presenter<MainView> {
    public void doShowAds(){
        MainView view = view();
        if (Utils.isDebug() || !Utils.isPremium()){
            view.doShowAds(true);
        }else{
            view.doShowAds(false);
        }
    }
}
