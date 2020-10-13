package tpcreative.co.qrscanner.ui.main;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.presenter.Presenter;

public class MainPresenter extends Presenter<MainView> {
    public boolean isPremium;
    public void doShowAds(){
        MainView view = view();
        if (!Utils.isPremium() && Utils.isLiveAds()){
            view.doShowAds(true);
        }else{
            view.doShowAds(false);
        }
    }
}
