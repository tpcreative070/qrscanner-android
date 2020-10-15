package tpcreative.co.qrscanner.ui.history;

/**
 * Created by Oclemy on 2017 for ProgrammingWizards TV Channel and http://www.camposha.info.
 - Our galaxycell class
 */

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.google.zxing.client.result.ParsedResultType;
import com.jaychang.srv.SimpleCell;
import com.jaychang.srv.SimpleViewHolder;
import butterknife.BindView;
import butterknife.ButterKnife;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.entities.HistoryEntity;
import tpcreative.co.qrscanner.helper.TimeHelper;
import tpcreative.co.qrscanner.model.HistoryModel;

public class HistoryCell extends SimpleCell<HistoryModel,HistoryCell.ViewHolder> {
    private ItemSelectedListener listener;
    private static final String TAG = HistoryCell.class.getSimpleName();
    public HistoryCell(@NonNull HistoryModel item) {
        super(item);
    }
    protected void setListener(ItemSelectedListener listener){
        this.listener = listener;
    }
    @Override
    protected int getLayoutRes() {
        return R.layout.history_item;
    }

    /*
    - Return a ViewHolder instance
     */
    @NonNull
    @Override
    protected ViewHolder onCreateViewHolder(ViewGroup parent, View cellView) {
        return new ViewHolder(cellView);
    }

    /*
    - Bind data to widgets in our viewholder.
     */
    @Override
    protected void onBindViewHolder(@NonNull final ViewHolder viewHolder,final int i, @NonNull Context context, Object o) {
        final HistoryModel data = getItem();
        if (data.isDeleted()){
            viewHolder.ckDelete.setVisibility(View.VISIBLE);
            viewHolder.llCheckedBox.setVisibility(View.VISIBLE);
            viewHolder.imgShare.setVisibility(View.INVISIBLE);
        }
        else{
            viewHolder.ckDelete.setVisibility(View.INVISIBLE);
            viewHolder.llCheckedBox.setVisibility(View.INVISIBLE);
            viewHolder.imgShare.setVisibility(View.VISIBLE);
        }
        Log.d(TAG,"position :" + i +" checked :" + data.isChecked());
        viewHolder.ckDelete.setChecked(data.isChecked());
        viewHolder.llCheckedBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener!=null){
                    viewHolder.ckDelete.setChecked(!getItem().isChecked());
                    listener.onClickItem(i,!getItem().isChecked());
                }
            }
        });
        viewHolder.lItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener!=null){
                    if (listener.isDeleted()){
                        viewHolder.ckDelete.setChecked(!getItem().isChecked());
                        listener.onClickItem(i,!getItem().isChecked());
                    }
                    else{
                        listener.onClickItem(i);
                    }
                }
            }
        });

        viewHolder.lItem.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (listener!=null){
                    if (!listener.isDeleted()){
                        listener.onLongClickItem(i);
                    }
                }
                return false;
            }
        });
        viewHolder.tvTime.setText(Utils.getCurrentDateDisplay(data.updatedDateTime));
        if (data.createType.equals(ParsedResultType.EMAIL_ADDRESS.name())){
            viewHolder.tvContent.setText(data.email);
        }
        else if (data.createType.equals(ParsedResultType.SMS.name())){
            viewHolder.tvContent.setText(data.message);
        }
        else if (data.createType.equals(ParsedResultType.GEO.name())){
            viewHolder.tvContent.setText(data.lat + "," + data.lon+"("+data.query+")");
        }
        else if (data.createType.equals(ParsedResultType.CALENDAR.name())){
            viewHolder.tvContent.setText(data.title);
        }
        else if (data.createType.equals(ParsedResultType.ADDRESSBOOK.name())){
            viewHolder.tvContent.setText(data.fullName);
        }
        else if (data.createType.equals(ParsedResultType.TEL.name())){
            viewHolder.tvContent.setText(data.phone);
        }
        else if (data.createType.equals(ParsedResultType.WIFI.name())){
            viewHolder.tvContent.setText(data.ssId);
        }  else if (data.createType.equals(ParsedResultType.URI.name())){
            viewHolder.tvContent.setText(data.url);
        }
        else{
            viewHolder.tvContent.setText(data.text);
        }

        viewHolder.imgShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener!=null){
                    listener.onClickShare(i);
                }
            }
        });

    }
    /**
     - Our ViewHolder class.
     - Inner static class.
     * Define your view holder, which must extend SimpleViewHolder.
     * */
    static class ViewHolder extends SimpleViewHolder {
        @BindView(R.id.tvDate)
        AppCompatTextView tvTime;
        @BindView(R.id.tvContent)
        AppCompatTextView tvContent;
        @BindView(R.id.ckDelete)
        AppCompatCheckBox ckDelete;
        @BindView(R.id.imgShare)
        AppCompatImageView imgShare;
        @BindView(R.id.lItem)
        LinearLayout lItem;
        @BindView(R.id.llCheckedBox)
        LinearLayout llCheckedBox;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public  interface ItemSelectedListener{
        void onClickItem(int position, boolean isChecked);
        void onClickItem(int position);
        void onLongClickItem(int position);
        void onClickShare(int  position);
        boolean isDeleted();
    }
}

