package tht.topu.com.tht.adapter;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.othershe.baseadapter.ViewHolder;
import com.othershe.baseadapter.base.CommonBaseAdapter;

import java.util.List;

import tht.topu.com.tht.R;
import tht.topu.com.tht.modle.Product;

/**
 * Created by shituocheng on 30/09/2017.
 */

public class RankDetailRecyclerViewAdapter extends CommonBaseAdapter<Product> {

    private Context context;

    public RankDetailRecyclerViewAdapter(Context context, List<Product> datas, boolean isOpenLoadMore) {
        super(context, datas, isOpenLoadMore);
        this.context = context;
    }

    @Override
    protected void convert(ViewHolder holder, Product data, int position) {

        holder.setText(R.id.rankDetailTextView, data.getProductTitle());
        holder.setText(R.id.rankDetailPriceTextView, data.getProductPrice());
        holder.setText(R.id.pointTextView, "获得积分："+data.getPoint());
        Glide.with(context).load(data.getImage()).into((ImageView)holder.getView(R.id.rankDetailImageView));
    }

    @Override
    protected int getItemLayoutId() {
        return R.layout.layout_rank_detail_item;
    }
}
