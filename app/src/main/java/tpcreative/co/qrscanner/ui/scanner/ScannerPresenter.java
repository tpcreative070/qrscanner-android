package tpcreative.co.qrscanner.ui.scanner;
import android.support.v4.app.Fragment;
import java.util.ArrayList;
import java.util.List;
import tpcreative.co.qrscanner.common.presenter.Presenter;
import tpcreative.co.qrscanner.ui.scannerresult.ScannerResultFragment;


public class ScannerPresenter extends Presenter<ScannerView>{

    protected List<Fragment> mFragment;

    public ScannerPresenter(){
        mFragment = new ArrayList<>();
    }

}
