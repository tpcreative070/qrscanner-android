package tpcreative.co.qrscanner.ui.save;
import androidx.fragment.app.Fragment;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.presenter.Presenter;
import tpcreative.co.qrscanner.helper.SQLiteHelper;
import tpcreative.co.qrscanner.model.SaveModel;
import tpcreative.co.qrscanner.model.TypeCategories;

public class SavePresenter extends Presenter<SaveView> {
    private final String TAG = SavePresenter.class.getSimpleName();
    protected List<TypeCategories> mListCategories;
    protected List<SaveModel> mList;
    protected List<Fragment> mFragment;
    private int i = 0;

    public SavePresenter(){
        mListCategories = new ArrayList<>();
        mList = new ArrayList<>();
        mFragment = new ArrayList<>();
    }

    public Map<String, SaveModel> getUniqueList(){
        SaveView view = view();
        final List<SaveModel> saver = SQLiteHelper.getListSave();
        if (saver==null){
            return new HashMap<>();
        }
        Utils.Log(TAG,new Gson().toJson(saver));
        Map<String, SaveModel> hashMap = new HashMap<>();
        for (SaveModel index : saver){
            hashMap.put(index.createType,index);
        }
        mListCategories.clear();
        i=0;
        for (Map.Entry<String, SaveModel>map : hashMap.entrySet()){
            mListCategories.add(new TypeCategories(i,map.getKey()));
            i+=1;
        }
        return hashMap;
    }

    public List<SaveModel> getListGroup(){
        getUniqueList();
        SaveView view = view();
        final List<SaveModel> list = SQLiteHelper.getListSave();
        List<SaveModel> mList = new ArrayList<>();
        for (TypeCategories index : mListCategories){
            for (SaveModel save : list){
                if (index.type.equals(save.createType)){
                    final SaveModel result = save;
                    result.typeCategories = index;
                    mList.add(result);
                }
            }
        }
        this.mList.clear();
        this.mList.addAll(mList);
        return mList;
    }

    public int getCheckedCount(){
        int count = 0;
        for (SaveModel index : mList){
            if (index.isChecked()){
                count+=1;
            }
        }
        return count;
    }

    public void deleteItem(){
        SaveView view = view();
        final List<SaveModel> list = mList;
        for (SaveModel index : list){
            if (index.isDeleted()){
                if (index.isChecked()){
                    SQLiteHelper.onDelete(index);
                }
            }
        }
        getListGroup();
        view.updateView();
    }

}
