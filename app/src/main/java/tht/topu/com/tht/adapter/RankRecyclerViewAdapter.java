package tht.topu.com.tht.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.othershe.baseadapter.ViewHolder;
import com.othershe.baseadapter.base.CommonBaseAdapter;

import java.util.List;

import cn.lemon.view.adapter.BaseViewHolder;
import cn.lemon.view.adapter.RecyclerAdapter;
import jp.wasabeef.glide.transformations.CropCircleTransformation;
import tht.topu.com.tht.R;
import tht.topu.com.tht.modle.Rank;
import tht.topu.com.tht.ui.activity.RankDetailActivity;
import tht.topu.com.tht.utils.API;
import tht.topu.com.tht.utils.Utilities;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

/**
 * Created by shituocheng on 2017/8/9.
 */

public class RankRecyclerViewAdapter extends CommonBaseAdapter<Rank>{

    private Context context;
    private ImageView userIconImageView;
    private TextView rankTextView;
    private TextView pointTextView;
    private RelativeLayout rankItem;
    private String mid;
    private static final String MID_KEY = "1x11";

    private boolean isRank;

    public RankRecyclerViewAdapter(Context context, List<Rank> datas, boolean isOpenLoadMore, boolean isRank) {
        super(context, datas, isOpenLoadMore);
        this.context = context;
        this.isRank = isRank;
    }

    @Override
    protected void convert(ViewHolder holder, final Rank data, int position) {

        //holder.setIsRecyclable(false);
        SharedPreferences sharedPreferences = context.getSharedPreferences("TokenData", Context.MODE_PRIVATE);
        mid = sharedPreferences.getString(MID_KEY, "");

        holder.setText(R.id.rankNum, data.getUserRank());
        userIconImageView = ((ImageView)holder.getView(R.id.rankAvatar));
        rankItem = holder.getView(R.id.rankItemLayout);
        rankTextView = ((TextView)holder.getView(R.id.rankUserTextView));
        pointTextView  = ((TextView)holder.getView(R.id.pointTextView));

        ((holder.getView(R.id.rankTop))).setVisibility(View.VISIBLE);
        ((holder.getView(R.id.rankNum))).setVisibility(View.VISIBLE);
        (holder.getView(R.id.rankHighLight)).setVisibility(View.VISIBLE);

        //如果用户没有图片，则显示默认图片
        if (!data.getUserIcon().equals("")){

            Glide.with(context).load(data.getUserIcon())
                    .apply(bitmapTransform(new CropCircleTransformation()))
                    .into((userIconImageView));
        }else {

            Glide.with(context).load(R.mipmap.login_logo)
                    .apply(bitmapTransform(new CropCircleTransformation()))
                    .into((userIconImageView));
//            userIconImageView.setImageDrawable(context.getResources().getDrawable(R.mipmap.login_logo));
        }

        if (data.getUserRank() != null){

            if (data.getUserRank().equals("1")){

                Glide.with(context.getApplicationContext()).load(R.mipmap.no1).into((ImageView)holder.getView(R.id.rankTop));
                (holder.getView(R.id.rankNum)).setVisibility(View.INVISIBLE);
            }else if (data.getUserRank().equals(("2"))){

                Glide.with(context.getApplicationContext()).load(R.mipmap.no2).into((ImageView)holder.getView(R.id.rankTop));
                (holder.getView(R.id.rankNum)).setVisibility(View.INVISIBLE);
            }else if (data.getUserRank().equals(("3"))){

                Glide.with(context.getApplicationContext()).load(R.mipmap.no3).into((ImageView)holder.getView(R.id.rankTop));
                (holder.getView(R.id.rankNum)).setVisibility(View.INVISIBLE);
            }else if (Integer.valueOf(data.getUserRank())  < 10 && Integer.valueOf(data.getUserRank()) > 2){
                Log.d("come", data.getUserRank());
                ((TextView)holder.getView(R.id.rankNum)).setText("0" + data.getUserRank());
                (holder.getView(R.id.rankTop)).setVisibility(View.INVISIBLE);
            }else {
                (holder.getView(R.id.rankTop)).setVisibility(View.INVISIBLE);
                ((TextView)holder.getView(R.id.rankNum)).setText(data.getUserRank());
            }
        }else {

            ViewGroup.MarginLayoutParams marginLayoutParams = new ViewGroup.MarginLayoutParams(userIconImageView.getLayoutParams());
            marginLayoutParams.setMargins(Utilities.dip2px(context, 18), Utilities.dip2px(context, 10), 0, 0);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(marginLayoutParams);

            userIconImageView.setLayoutParams(layoutParams);
        }


        rankTextView.setText(data.getUserName());
        pointTextView.setText(data.getUserPoint());

        if (data.getMid() != null && isRank){

            if (data.getMid().equals(mid)){

                holder.getView(R.id.rankHighLight).setVisibility(View.VISIBLE);
                if (data.getGrade() == 1){

                    holder.getView(R.id.rankHighLight).setBackgroundColor(context.getResources().getColor(R.color.silver_hignlight));
                }else if (data.getGrade() == 3){

                    holder.getView(R.id.rankHighLight).setBackgroundColor(context.getResources().getColor(R.color.black_hignlight));
                }else{

                    holder.getView(R.id.rankHighLight).setBackgroundColor(context.getResources().getColor(R.color.gold_hignlight));
                }
            }else {

                holder.getView(R.id.rankHighLight).setVisibility(View.GONE);
            }
        }

        if (isRank){

            rankTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    rankClickHandler(data);
                }
            });

            userIconImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    rankClickHandler(data);
                }
            });
        }
    }

    @Override
    protected int getItemLayoutId() {
        return R.layout.layout_rank_item;
    }

    private void rankClickHandler(Rank data){

        Intent intent = new Intent(context, RankDetailActivity.class);
        intent.putExtra("midData", data.getMid());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(intent);
    }
}
