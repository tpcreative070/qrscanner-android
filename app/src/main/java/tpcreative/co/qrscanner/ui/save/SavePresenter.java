package tpcreative.co.qrscanner.ui.save;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tpcreative.co.qrscanner.common.presenter.Presenter;
import tpcreative.co.qrscanner.model.History;
import tpcreative.co.qrscanner.model.Save;
import tpcreative.co.qrscanner.model.TypeCategories;
import tpcreative.co.qrscanner.model.room.InstanceGenerator;
import tpcreative.co.qrscanner.ui.history.HistoryView;

public class SavePresenter extends Presenter<SaveView> {

    protected List<TypeCategories> mListCategories;
    protected List<Save> mList;
    private int i = 0;

    public SavePresenter(){
        mListCategories = new ArrayList<>();
        mList = new ArrayList<>();
    }

    public Map<String,Save> getUniqueList(){
        SaveView view = view();
        final List<Save> histories = InstanceGenerator.getInstance(view.getContext()).getListSave();
        Map<String,Save> hashMap = new HashMap<>();
        for (Save index : histories){
            hashMap.put(index.createType,index);
        }

        mListCategories.clear();
        i=0;
        for (Map.Entry<String,Save>map : hashMap.entrySet()){
            mListCategories.add(new TypeCategories(i,map.getKey()));
            i+=1;
        }
        return hashMap;
    }

    public List<Save> getListGroup(){
        getUniqueList();
        SaveView view = view();
        final List<Save> list = InstanceGenerator.getInstance(view.getContext()).getListSave();
        List<Save> mList = new ArrayList<>();
        for (TypeCategories index : mListCategories){
            for (Save save : list){
                if (index.type.equals(save.createType)){
                    final Save result = save;
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
        for (Save index : mList){
            if (index.isChecked()){
                count+=1;
            }
        }
        return count;
    }

    public void deleteItem(){
        SaveView view = view();
        final List<Save> list = mList;
        for (Save index : list){
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
