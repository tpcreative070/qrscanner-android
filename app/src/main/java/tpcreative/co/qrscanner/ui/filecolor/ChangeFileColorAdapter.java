package tpcreative.co.qrscanner.ui.filecolor;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.adapter.BaseAdapter;
import tpcreative.co.qrscanner.common.adapter.BaseHolder;
import tpcreative.co.qrscanner.model.Theme;

public class ChangeFileColorAdapter extends BaseAdapter<Theme, BaseHolder> {
    private Context context;
    private ItemSelectedListener itemSelectedListener;
    private String TAG = ChangeFileColorAdapter.class.getSimpleName();


    public ChangeFileColorAdapter(LayoutInflater inflater, Context context, ItemSelectedListener itemSelectedListener) {
        super(inflater);
        this.context = context;
        this.itemSelectedListener = itemSelectedListener;
    }

    @Override
    public int getItemCount() {
        return dataSource.size();
    }

    @Override
    public BaseHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemHolder(inflater.inflate(R.layout.theme_item, parent, false));
    }

    public interface ItemSelectedListener {
        void onClickItem(int position);
    }

    public class ItemHolder extends BaseHolder<Theme> {

        @BindView(R.id.imgTheme)
        CircleImageView imgTheme;
        @BindView(R.id.imgChecked)
        ImageView imgChecked;
        int mPosition;

        public ItemHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void bind(Theme data, int position) {
            super.bind(data, position);
            mPosition = position;
            //imgTheme.setBackgroundColor(data.getPrimaryColor());
            imgTheme.setImageResource(data.getPrimaryColor());
            if (data.isCheck){
                imgChecked.setVisibility(View.VISIBLE);
            }
            else {
                imgChecked.setVisibility(View.INVISIBLE);
            }
        }

        @OnClick(R.id.rlHome)
        public void onClicked(View view) {
            if (itemSelectedListener != null) {
                itemSelectedListener.onClickItem(mPosition);
            }
        }
    }

}
