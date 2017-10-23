package tht.topu.com.tht.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.othershe.baseadapter.ViewHolder;
import com.othershe.baseadapter.base.CommonBaseAdapter;

import java.util.List;

import tht.topu.com.tht.R;
import tht.topu.com.tht.modle.Product;
import tht.topu.com.tht.ui.activity.ProductDetailActivity;
import tht.topu.com.tht.utils.Utilities;

/**
 * Created by shituocheng on 30/09/2017.
 */

public class RankDetailRecyclerViewAdapter extends CommonBaseAdapter<Product> {

    private Context context;
    private static final String MID_KEY = "1x11";

    public RankDetailRecyclerViewAdapter(Context context, List<Product> datas, boolean isOpenLoadMore) {
        super(context, datas, isOpenLoadMore);
        this.context = context;
    }

    @Override
    protected void convert(ViewHolder holder, final Product data, int position) {

        SharedPreferences sharedPreferences = context.getSharedPreferences("TokenData", Context.MODE_PRIVATE);
        final String mid = sharedPreferences.getString(MID_KEY, "");

        holder.setText(R.id.rankDetailTextView, data.getProductTitle());
        holder.setText(R.id.rankDetailPriceTextView, data.getProductPrice());
        holder.setText(R.id.pointTextView, "获得积分："+data.getPoint());

        Glide.with(context).load(data.getImage()).into((ImageView)holder.getView(R.id.rankDetailImageView));

        holder.getView(R.id.rankDetailImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                clickHandler(data, mid);
            }
        });

        holder.getView(R.id.rankDetailPriceTextView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                clickHandler(data, mid);
            }
        });

        holder.getView(R.id.rankDetailTextView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                clickHandler(data, mid);
            }
        });

    }

    @Override
    protected int getItemLayoutId() {
        return R.layout.layout_rank_detail_item;
    }

    private void clickHandler(Product data, String mid){

        Bundle bundle = new Bundle();
        bundle.putString("mdid", data.getMcid());
        bundle.putString("mid", mid);

        Utilities.jumpToActivity(context, ProductDetailActivity.class, bundle, "bundleData");

    }
}
