package tpcreative.co.qrscanner.ui.filecolor;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.presenter.BaseView;
import tpcreative.co.qrscanner.common.presenter.Presenter;
import tpcreative.co.qrscanner.model.EnumStatus;
import tpcreative.co.qrscanner.model.Theme;

public class ChangeFileColorPresenter extends Presenter<BaseView> {

    protected List<Theme>mList;
    protected Theme mTheme;
    private static final String TAG = ChangeFileColorPresenter.class.getSimpleName();

    public ChangeFileColorPresenter(){
        mList = new ArrayList<>();
    }

    public void getData(){
        BaseView view = view();
        mList = Theme.getInstance().getList();
        mTheme = Theme.getInstance().getThemeInfo();
        if (mTheme!=null){
            for(int i = 0;i <mList.size() ;i++){
                if (mTheme.getId()==mList.get(i).getId()){
                    mList.get(i).isCheck = true;
                }
                else{
                    mList.get(i).isCheck = false;
                }
            }
        }
        Utils.Log(TAG,"Value :" + new Gson().toJson(mList));
        view.onSuccessful("Successful", EnumStatus.SHOW_DATA);
    }

}
