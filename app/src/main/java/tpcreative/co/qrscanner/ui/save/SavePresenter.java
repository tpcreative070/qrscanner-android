package tpcreative.co.qrscanner.ui.save;

import android.support.v4.app.Fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tpcreative.co.qrscanner.common.presenter.Presenter;
import tpcreative.co.qrscanner.model.History;
import tpcreative.co.qrscanner.model.Save;
import tpcreative.co.qrscanner.model.TypeCategories;
import tpcreative.co.qrscanner.model.room.InstanceGenerator;
import tpcreative.co.qrscanner.ui.create.ContactFragment;
import tpcreative.co.qrscanner.ui.create.EmailFragment;
import tpcreative.co.qrscanner.ui.create.EventFragment;
import tpcreative.co.qrscanner.ui.create.LocationFragment;
import tpcreative.co.qrscanner.ui.create.MessageFragment;
import tpcreative.co.qrscanner.ui.create.TelephoneFragment;
import tpcreative.co.qrscanner.ui.create.TextFragment;
import tpcreative.co.qrscanner.ui.create.UrlFragment;
import tpcreative.co.qrscanner.ui.create.WifiFragment;
import tpcreative.co.qrscanner.ui.history.HistoryView;

public class SavePresenter extends Presenter<SaveView> {

    protected List<TypeCategories> mListCategories;
    protected List<Save> mList;
    protected List<Fragment> mFragment;
    private int i = 0;

    public SavePresenter(){
        mListCategories = new ArrayList<>();
        mList = new ArrayList<>();
        mFragment = new ArrayList<>();
    }

    public void setFragmentList(){
        mFragment.clear();
        mFragment.add(ContactFragment.newInstance(5));
        mFragment.add(EmailFragment.newInstance(6));
        mFragment.add(UrlFragment.newInstance(7));
        mFragment.add(WifiFragment.newInstance(8));
        mFragment.add(LocationFragment.newInstance(9));
        mFragment.add(TelephoneFragment.newInstance(10));
        mFragment.add(MessageFragment.newInstance(11));
        mFragment.add(EventFragment.newInstance(12));
        mFragment.add(TextFragment.newInstance(13));
    }


    public Map<String,Save> getUniqueList(){
        SaveView view = view();
        final List<Save> histories = InstanceGenerator.getInstance(view.getContext()).getListSave();
        if (histories==null){
            return new HashMap<>();
        }
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
