package tht.topu.com.tht.adapter;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.othershe.baseadapter.ViewHolder;
import com.othershe.baseadapter.base.CommonBaseAdapter;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.CropCircleTransformation;
import tht.topu.com.tht.R;
import tht.topu.com.tht.modle.Forum;
import tht.topu.com.tht.ui.activity.ForumDetailActivity;
import tht.topu.com.tht.utils.API;
import tht.topu.com.tht.utils.Utilities;

/**
 * Created by shituocheng on 2017/8/16.
 */

public class ForumRecyclerViewAdapter extends CommonBaseAdapter<Forum>{

    private Context context;
    private ImageView forumImg1;
    private ImageView forumImg2;
    private ImageView forumImg3;
    private String[] picArr;


    public ForumRecyclerViewAdapter(Context context, List<Forum> datas, boolean isOpenLoadMore) {
        super(context, datas, isOpenLoadMore);
        this.context = context;
    }

    @Override
    protected void convert(ViewHolder holder, final Forum data, int position) {

        holder.setIsRecyclable(false);

        holder.setText(R.id.tagTextView, data.getTagName());
        holder.setText(R.id.forumTitleTextView, data.getForumTitle());
        holder.setText(R.id.userNameTextView, data.getUserName());
        holder.setText(R.id.replyNumTextView, ""+data.getReplyNum());
        holder.setText(R.id.goodNumTextView, ""+data.getLikeNum());
        holder.setText(R.id.forumVipTextView, data.getVip());

        forumImg1 = holder.getView(R.id.forumImg1);
        forumImg2 = holder.getView(R.id.forumImg2);
        forumImg3 = holder.getView(R.id.forumImg3);

        if (data.getPic1() != null){

            picArr = data.getPic1().split(",");

            if (picArr.length == 0){
                forumImg1.setVisibility(View.GONE);
                forumImg2.setVisibility(View.GONE);
                forumImg3.setVisibility(View.GONE);

            }else if (picArr.length == 1) {
                Glide.with(context).load(API.getAnothereHostName()+picArr[0]).into(forumImg1);
                forumImg2.setVisibility(View.GONE);
                forumImg3.setVisibility(View.GONE);
                if (picArr[0].equals("")){
                    forumImg1.setVisibility(View.GONE);
                }
            }else if (picArr.length == 2) {

                Glide.with(context).load(API.getAnothereHostName()+picArr[0]).into(forumImg1);
                Glide.with(context).load(API.getAnothereHostName()+picArr[1]).into(forumImg2);
                forumImg3.setVisibility(View.GONE);
                if (picArr[0].equals("")){
                    forumImg1.setVisibility(View.GONE);
                }else if (picArr[1].equals("")){
                    forumImg2.setVisibility(View.GONE);
                }
            }else{

                Glide.with(context).load(API.getAnothereHostName()+picArr[0]).into(forumImg1);
                Glide.with(context).load(API.getAnothereHostName()+picArr[1]).into(forumImg2);
                Glide.with(context).load(API.getAnothereHostName()+picArr[2]).into(forumImg3);
                if (picArr[0].equals("")){
                    forumImg1.setVisibility(View.GONE);
                }else if (picArr[1].equals("")){
                    forumImg2.setVisibility(View.GONE);
                }else if (picArr[2].equals("")){
                    forumImg3.setVisibility(View.GONE);
                }
            }
        }else {

            forumImg1.setVisibility(View.GONE);
            forumImg2.setVisibility(View.GONE);
            forumImg3.setVisibility(View.GONE);
        }

        Glide.with(context).load(data.getAvatarIcon()).apply(RequestOptions.bitmapTransform(new CropCircleTransformation())).into((ImageView)holder.getView(R.id.userImageView));
        if (data.isFavorite() && !data.isTop()){

            ((ImageView)holder.getView(R.id.cornerImageView)).setImageDrawable(context.getResources().getDrawable(R.mipmap.jing));
        }else if (data.isTop() && !data.isFavorite()){

            ((ImageView)holder.getView(R.id.cornerImageView1)).setImageDrawable(context.getResources().getDrawable(R.mipmap.ding));
        }else if (data.isTop() && data.isFavorite()){

            ((ImageView)holder.getView(R.id.cornerImageView1)).setImageDrawable(context.getResources().getDrawable(R.mipmap.ding));
            ((ImageView)holder.getView(R.id.cornerImageView)).setImageDrawable(context.getResources().getDrawable(R.mipmap.jing));
        }

        holder.getView(R.id.forumTitleTextView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bundle bundle = new Bundle();

                bundle.putString("fid", data.getFid());
                bundle.putString("flid", data.getFlid());
                Utilities.jumpToActivity(context, ForumDetailActivity.class, bundle, "forumBundle");
            }
        });

        holder.getView(R.id.forumImgList).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                
                Bundle bundle = new Bundle();

                bundle.putString("fid", data.getFid());
                bundle.putString("flid", data.getFlid());
                Utilities.jumpToActivity(context, ForumDetailActivity.class, bundle, "forumBundle");
            }
        });
    }

    @Override
    protected int getItemLayoutId() {
        return R.layout.layout_forum_item;
    }
}
