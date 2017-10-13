package tht.topu.com.tht.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
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
 * Created by shituocheng on 2017/8/8.
 */

public class ProductRecyclerViewAdapter extends CommonBaseAdapter<Product> {

    private Context context;
    private static final String MID_KEY = "1x11";

    public ProductRecyclerViewAdapter(Context context, List<Product> datas, boolean isOpenLoadMore) {
        super(context, datas, isOpenLoadMore);
        this.context = context;
    }

    @Override
    protected void convert(ViewHolder holder, final Product data, int position) {

        SharedPreferences sharedPreferences = context.getSharedPreferences("TokenData", Context.MODE_PRIVATE);
        final String mid = sharedPreferences.getString(MID_KEY, "");

        Glide.with(context).load(data.getImage()).into((ImageView)holder.getView(R.id.productImageView));
        holder.setText(R.id.productTextView, data.getProductTitle());
        holder.setText(R.id.priceTextView, data.getProductPrice());
        holder.setText(R.id.ogPriceTextView, data.getOgProductPrice());

        //holder.setIsRecyclable(false);

        (holder.getView(R.id.productRecommend)).setVisibility(View.VISIBLE);
        (holder.getView(R.id.productHot)).setVisibility(View.VISIBLE);

        ((TextView)holder.getView(R.id.ogPriceTextView)).setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);

        if (!data.isRecommend()){

            (holder.getView(R.id.productRecommend)).setVisibility(View.INVISIBLE);
        }

        if (!data.isHot()){

            (holder.getView(R.id.productHot)).setVisibility(View.INVISIBLE);
        }

        (holder.getView(R.id.productImageView)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bundle bundle = new Bundle();
                bundle.putString("mdid", data.getMcid());
                bundle.putString("mid", mid);

                Utilities.jumpToActivity(context, ProductDetailActivity.class, bundle, "bundleData");
            }
        });
    }

    @Override
    protected int getItemLayoutId() {
        return R.layout.layout_product;
    }
}
