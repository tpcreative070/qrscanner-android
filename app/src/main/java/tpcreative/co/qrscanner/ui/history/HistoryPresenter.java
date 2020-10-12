package tpcreative.co.qrscanner.ui.history;
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
import tpcreative.co.qrscanner.model.TypeCategories;

public class HistoryPresenter extends Presenter<HistoryView> {

    protected List<TypeCategories> mListCategories;
    protected List<HistoryModel> mList;
    protected HistoryModel mLatestValue;
    private int i = 0;
    private static final String TAG = HistoryPresenter.class.getSimpleName();

    public HistoryPresenter(){
        mListCategories = new ArrayList<>();
        mList = new ArrayList<>();
        mLatestValue = new HistoryModel();
    }

    public List<HistoryModel>getLatestList(List<HistoryModel> mData){
        List<HistoryModel> mList = new ArrayList<>();
        if (mData.size()>0){
            mLatestValue = mData.get(0);
            for (HistoryModel index : mData){
                if (index.createType.equals(mLatestValue.createType)){
                    index.typeCategories = new TypeCategories(0,mLatestValue.createType);
                    mList.add(index);
                }
            }
        }
        Collections.sort(mList, new Comparator<HistoryModel>() {
            public int compare(HistoryModel o1, HistoryModel o2) {
                return (Utils.getMilliseconds(o1.updatedDateTime)<Utils.getMilliseconds(o2.updatedDateTime)) ? 0 : 1;
            }
        });
        return mList;
    }

    public Map<String, HistoryModel> getUniqueList(){
        HistoryView view = view();
        final List<HistoryModel> histories = SQLiteHelper.getHistoryList();
        Utils.Log(TAG,"History list "+ new Gson().toJson(histories));
        if (histories==null){
            return new HashMap<>();
        }
        Utils.Log(TAG,new Gson().toJson(histories));
        Map<String, HistoryModel> hashMap = new HashMap<>();
        for (HistoryModel index : histories){
            hashMap.put(index.createType,index);
        }
        mListCategories.clear();
        i=1;
        for (Map.Entry<String, HistoryModel>map : hashMap.entrySet()){
            mListCategories.add(new TypeCategories(i,map.getKey()));
            i+=1;
        }
        return hashMap;
    }

    public List<HistoryModel> getListGroup(){
        getUniqueList();
        HistoryView view = view();
        final List<HistoryModel> list = SQLiteHelper.getHistoryList();
        List<HistoryModel> mList = new ArrayList<>();
        final List<HistoryModel> mLatestType = getLatestList(list);
        Utils.Log(TAG,"Latest list "+ new Gson().toJson(mLatestType));
        Utils.Log(TAG,"Latest object " + new Gson().toJson(mLatestValue));
        for (TypeCategories index : mListCategories){
            for (HistoryModel history : list){
                if (index.type.equals(history.createType) && !index.type.equals(mLatestValue.createType)){
                    final HistoryModel result = history;
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
        for (HistoryModel index : mList){
            if (index.isChecked()){
                count+=1;
            }
        }
        return count;
    }

    public void deleteItem(){
        HistoryView view = view();
        final List<HistoryModel> list = mList;
        for (HistoryModel index : list){
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
