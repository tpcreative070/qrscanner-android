package tpcreative.co.qrscanner.ui.history;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tpcreative.co.qrscanner.common.presenter.Presenter;
import tpcreative.co.qrscanner.model.History;
import tpcreative.co.qrscanner.model.TypeCategories;
import tpcreative.co.qrscanner.model.room.InstanceGenerator;

public class HistoryPresenter extends Presenter<HistoryView> {

    protected List<TypeCategories> mListCategories;
    protected List<History> mList;
    private int i = 0;

    public HistoryPresenter(){
        mListCategories = new ArrayList<>();
        mList = new ArrayList<>();
    }

    public Map<String,History> getUniqueList(){
        HistoryView view = view();
        final List<History> histories = InstanceGenerator.getInstance(view.getContext()).getList();
        if (histories==null){
            return new HashMap<>();
        }
        Map<String,History> hashMap = new HashMap<>();
        for (History index : histories){
            hashMap.put(index.createType,index);
        }

        mListCategories.clear();
        i=0;
        for (Map.Entry<String,History>map : hashMap.entrySet()){
            mListCategories.add(new TypeCategories(i,map.getKey()));
            i+=1;
        }
        return hashMap;
    }

    public List<History> getListGroup(){
        getUniqueList();
        HistoryView view = view();
        final List<History> list = InstanceGenerator.getInstance(view.getContext()).getList();
        List<History> mList = new ArrayList<>();
        for (TypeCategories index : mListCategories){
            for (History history : list){
                if (index.type.equals(history.createType)){
                    final History result = history;
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
        for (History index : mList){
            if (index.isChecked()){
                count+=1;
            }
        }
        return count;
    }

    public void deleteItem(){
        HistoryView view = view();
        final List<History> list = mList;
        for (History index : list){
            if (index.isDeleted()){
                if (index.isChecked()){
                    InstanceGenerator.getInstance(view.getContext()).onDelete(index);
                }
            }
        }
        getListGroup();
        view.updateView();
    }

}
