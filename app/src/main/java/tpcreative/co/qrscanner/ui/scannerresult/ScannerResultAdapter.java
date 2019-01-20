package tpcreative.co.qrscanner.ui.scannerresult;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.OnClick;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.adapter.BaseAdapter;
import tpcreative.co.qrscanner.common.adapter.BaseHolder;
import tpcreative.co.qrscanner.model.ItemNavigation;

public class ScannerResultAdapter extends BaseAdapter<ItemNavigation, BaseHolder> {

    private Context context;
    private ItemSelectedListener itemSelectedListener;
    private String TAG = ScannerResultAdapter.class.getSimpleName();


    public ScannerResultAdapter(LayoutInflater inflater, Context context, ItemSelectedListener itemSelectedListener) {
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
        return new ItemHolder(inflater.inflate(R.layout.item_navigation, parent, false));
    }


    public interface ItemSelectedListener {
        void onClickItem(int position);
    }

    public class ItemHolder extends BaseHolder<ItemNavigation> {

        @BindView(R.id.imgAction)
        ImageView imgAction;
        @BindView(R.id.tvTitle)
        TextView tvTitle;
        private int mPosition;

        public ItemHolder(View itemView) {
            super(itemView);
        }


        @Override
        public void bind(ItemNavigation data, int position) {
            super.bind(data, position);
            this.mPosition  = position;
            tvTitle.setText(data.value);
            imgAction.setImageDrawable(context.getResources().getDrawable(data.res));
        }

        @OnClick(R.id.rlHome)
        public void onClicked(View view) {
            if (itemSelectedListener != null) {
                itemSelectedListener.onClickItem(mPosition);
            }
        }

    }

}
