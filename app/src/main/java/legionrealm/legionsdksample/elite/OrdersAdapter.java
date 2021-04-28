package legionrealm.legionsdksample.elite;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.ViewHolder> {

    private Context mContext;
    private List<OrderBean> mData = new ArrayList<>();
    private boolean hasNewVersion = false;
    private OnItemClickListener mOnItemClickListener;

    public OrdersAdapter(Context context, List<OrderBean> OrderBeans) {
        this.mContext = context;
        mData = OrderBeans;
    }

    /**
     * 设置 点击事件
     * @param listener listener
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    /**
     * @param OrderBeans
     */
    public void setOrderBeans(List<OrderBean> OrderBeans) {
        mData = OrderBeans;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_info, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        final Product info = mData.get(position).getProduct();
        holder.tvIndex.setText((position+1)+"");
        holder.tvName.setText(info.getName());
        Product.Price price = info.getPrice().get(0);
        holder.tvPrice.setText(price.getAmount() + " " + price.getCurrency());
        holder.itemView.setOnClickListener(v -> mOnItemClickListener.onItemClick(position));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView tvIndex;
        public TextView tvName;
        public TextView tvPrice;

        public ViewHolder(View view) {
            super(view);
            tvIndex = view.findViewById(R.id.tv_index);
            tvName = view.findViewById(R.id.tv_name);
            tvPrice = view.findViewById(R.id.tv_price);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}
