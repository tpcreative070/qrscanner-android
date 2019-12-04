package tpcreative.co.qrscanner.ui.scanner;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.List;
import tpcreative.co.qrscanner.common.presenter.Presenter;


public class ScannerPresenter extends Presenter<ScannerView>{

    protected List<Fragment> mFragment;

    public ScannerPresenter(){
        mFragment = new ArrayList<>();
    }

}
