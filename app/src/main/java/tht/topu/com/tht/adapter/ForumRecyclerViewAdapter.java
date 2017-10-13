package tht.topu.com.tht.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.othershe.baseadapter.ViewHolder;
import com.othershe.baseadapter.base.CommonBaseAdapter;

import java.util.List;

import jp.wasabeef.glide.transformations.CropCircleTransformation;
import tht.topu.com.tht.R;
import tht.topu.com.tht.modle.Forum;

/**
 * Created by shituocheng on 2017/8/16.
 */

public class ForumRecyclerViewAdapter extends CommonBaseAdapter<Forum>{

    private Context context;

    public ForumRecyclerViewAdapter(Context context, List<Forum> datas, boolean isOpenLoadMore) {
        super(context, datas, isOpenLoadMore);
        this.context = context;
    }

    @Override
    protected void convert(ViewHolder holder, Forum data, int position) {

        holder.setText(R.id.tagTextView, data.getTagName());
        holder.setText(R.id.forumTitleTextView, data.getForumTitle());
        holder.setText(R.id.userNameTextView, data.getUserName());
        holder.setText(R.id.replyNumTextView, ""+data.getReplyNum());
        holder.setText(R.id.goodNumTextView, ""+data.getLikeNum());
        holder.setText(R.id.forumVipTextView, data.getVip());
        Glide.with(context).load(data.getAvatarIcon()).apply(RequestOptions.bitmapTransform(new CropCircleTransformation())).into((ImageView)holder.getView(R.id.userImageView));
        if (data.isFavorite()){

            ((ImageView)holder.getView(R.id.cornerImageView)).setImageDrawable(context.getResources().getDrawable(R.mipmap.jing));
        }else if (data.isTop()){

            ((ImageView)holder.getView(R.id.cornerImageView)).setImageDrawable(context.getResources().getDrawable(R.mipmap.ding));
        }

        if (data.isDel()){

            (holder.getView(R.id.isDelLayout)).setVisibility(View.VISIBLE);
        }else{

            (holder.getView(R.id.isDelLayout)).setVisibility(View.GONE);
        }
    }

    @Override
    protected int getItemLayoutId() {
        return R.layout.layout_forum_item;
    }
}
