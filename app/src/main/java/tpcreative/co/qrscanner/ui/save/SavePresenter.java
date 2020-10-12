package tpcreative.co.qrscanner.ui.save;
import androidx.fragment.app.Fragment;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.presenter.Presenter;
import tpcreative.co.qrscanner.helper.SQLiteHelper;
import tpcreative.co.qrscanner.model.HistoryModel;
import tpcreative.co.qrscanner.model.SaveModel;
import tpcreative.co.qrscanner.model.TypeCategories;

public class SavePresenter extends Presenter<SaveView> {
    private final String TAG = SavePresenter.class.getSimpleName();
    protected List<TypeCategories> mListCategories;
    protected List<SaveModel> mList;
    protected SaveModel mLatestValue;
    protected List<Fragment> mFragment;
    private int i = 0;

    public SavePresenter(){
        mListCategories = new ArrayList<>();
        mList = new ArrayList<>();
        mFragment = new ArrayList<>();
        mLatestValue = new SaveModel();
    }

    public List<SaveModel>getLatestList(List<SaveModel> mData){
        List<SaveModel> mList = new ArrayList<>();
        if (mData.size()>0){
            mLatestValue = mData.get(0);
            for (SaveModel index : mData){
                if (index.createType.equals(mLatestValue.createType)){
                    index.typeCategories = new TypeCategories(0,mLatestValue.createType);
                    mList.add(index);
                }
            }
        }
        Collections.sort(mList, new Comparator<SaveModel>() {
            public int compare(SaveModel o1, SaveModel o2) {
                return (Utils.getMilliseconds(o1.updatedDateTime)<Utils.getMilliseconds(o2.updatedDateTime)) ? 0 : 1;
            }
        });
        return mList;
    }

    public Map<String, SaveModel> getUniqueList(){
        final List<SaveModel> saver = SQLiteHelper.getSaveList();
        Utils.Log(TAG,"Save list "+ new Gson().toJson(saver));
        Utils.Log(TAG,new Gson().toJson(saver));
        Map<String, SaveModel> hashMap = new HashMap<>();
        for (SaveModel index : saver){
            hashMap.put(index.createType,index);
        }
        mListCategories.clear();
        i=1;
        for (Map.Entry<String, SaveModel>map : hashMap.entrySet()){
            mListCategories.add(new TypeCategories(i,map.getKey()));
            i+=1;
        }
        return hashMap;
    }

    public List<SaveModel> getListGroup(){
        getUniqueList();
        SaveView view = view();
        final List<SaveModel> list = SQLiteHelper.getSaveList();
        List<SaveModel> mList = new ArrayList<>();
        final List<SaveModel> mLatestType = getLatestList(list);
        Utils.Log(TAG,"Latest list "+ new Gson().toJson(mLatestType));
        Utils.Log(TAG,"Latest object " + new Gson().toJson(mLatestValue));
        for (TypeCategories index : mListCategories){
            for (SaveModel save : list){
                if (index.type.equals(save.createType) && !index.type.equals(mLatestValue.createType)){
                    final SaveModel result = save;
                    result.typeCategories = index;
                    mList.add(result);
                }
            }
        }
        /*Added latest list to ArrayList*/
        mLatestType.addAll(mList);
        this.mList.clear();
        this.mList.addAll(mLatestType);
        return mLatestType;
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
