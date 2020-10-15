package tpcreative.co.qrscanner.ui.create;

/**
 * Created by Oclemy on 2017 for ProgrammingWizards TV Channel and http://www.camposha.info.
 - Our galaxycell class
 */

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.jaychang.srv.SimpleCell;
import com.jaychang.srv.SimpleViewHolder;
import butterknife.BindView;
import butterknife.ButterKnife;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.model.QRCodeType;


public class GenerateCell extends SimpleCell<QRCodeType,GenerateCell.ViewHolder> {
    private ItemSelectedListener listener;
    private static final String TAG = GenerateCell.class.getSimpleName();

    public GenerateCell(@NonNull QRCodeType item) {
        super(item);
    }

    protected void setListener(ItemSelectedListener listener){
        this.listener = listener;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.generate_item;
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
    protected void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i, @NonNull Context context, Object o) {
        final QRCodeType data = getItem();
        viewHolder.tvName.setText(data.name);
        viewHolder.imgIcon.setImageDrawable(context.getResources().getDrawable(data.res));
        viewHolder.imgDefault.setImageDrawable(context.getResources().getDrawable(R.drawable.baseline_add_box_white_48));
        viewHolder.imgDefault.setColorFilter(context.getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
        viewHolder.llRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener!=null){
                    listener.onClickItem(i,false);
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
        @BindView(R.id.tvName)
        AppCompatTextView tvName;
        @BindView(R.id.imgIcon)
        AppCompatImageView imgIcon;
        @BindView(R.id.imgDefault)
        AppCompatImageView imgDefault;
        @BindView(R.id.llRoot)
        LinearLayout llRoot;
        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public  interface ItemSelectedListener{
        void onClickItem(int position, boolean isChecked);
        void onClickShare(String value);
    }
}

