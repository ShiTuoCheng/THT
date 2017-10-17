package tht.topu.com.tht.ui.fragment.rankFragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.StringLoader;
import com.othershe.baseadapter.interfaces.OnLoadMoreListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.lemon.view.RefreshRecyclerView;
import jp.wasabeef.glide.transformations.CropCircleTransformation;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import tht.topu.com.tht.R;
import tht.topu.com.tht.adapter.RankRecyclerViewAdapter;
import tht.topu.com.tht.modle.Rank;
import tht.topu.com.tht.ui.activity.ChangePasswordActivity;
import tht.topu.com.tht.ui.activity.LoginActivity;
import tht.topu.com.tht.ui.base.BaseFragment;
import tht.topu.com.tht.utils.API;
import tht.topu.com.tht.utils.DividerLine;
import tht.topu.com.tht.utils.Utilities;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;


public class RankContentFragment extends Fragment {

    private static final String FIRST_ID = "0x04";
    private static final String SECOND_ID = "0x05";
    private ImageView userBackgroundImageView;
    private RecyclerView refreshRecyclerView;
    private RankRecyclerViewAdapter rankRecyclerViewAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView rankTextView;
    private TextView userNameTextView;
    private TextView rankNumTextView;
    private TextView userPointTextView;
    private ImageView userImageView;
    private ImageView cupImageView;

    private List<Rank> ranks = new ArrayList<>();
    private List<Rank> loadMoreRanks = new ArrayList<>();

    private int page;
    private String firstDay;
    private String secondDay;

    private int currentPage;
    private int totalPage;

    private String random32;
    private String time10;
    private String key64;

    private String mid;

    private Handler uiHandler;

    //json请求
    public static final MediaType JSON = MediaType
            .parse("application/json; charset=utf-8");
    private static final String MID_KEY = "1x11";

    public static RankContentFragment newInstance(String firstDay, String secondDay) {

        Bundle argus = new Bundle();
        argus.putString(FIRST_ID, firstDay);
        argus.putString(SECOND_ID, secondDay);

        RankContentFragment rankContentFragment = new RankContentFragment();
        rankContentFragment.setArguments(argus);
        return rankContentFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_rank_content, container, false);

        firstDay = getArguments().getString(FIRST_ID);
        secondDay = getArguments().getString(SECOND_ID);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("TokenData", Context.MODE_PRIVATE);
        mid = sharedPreferences.getString(MID_KEY, "");

        uiHandler = new Handler(Looper.getMainLooper());
        initView(v);

        if (!Utilities.isNetworkAvaliable(getActivity().getApplicationContext())){

            Utilities.popUpAlert(getActivity(), "网络不可用");
        }else {

            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(false);
                }
            });

            initData(true);
            //获取用户信息
            getUserInfo();
            getUserRank();
        }

        //下拉刷新
//        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//
//                // 获取用户信息
//                getUserInfo();
//
//                refreshData();
//            }
//        });

        swipeRefreshLayout.setEnabled(false);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        rankRecyclerViewAdapter = new RankRecyclerViewAdapter(getActivity().getApplicationContext(), ranks, true, true);

        DividerLine dividerLine = new DividerLine();
        dividerLine.setColor(getResources().getColor(R.color.colorBorder));
        dividerLine.setSize(2);

        rankRecyclerViewAdapter.notifyDataSetChanged();
        //初始化 开始加载更多的loading View
        rankRecyclerViewAdapter.setLoadingView(R.layout.progress_item);
        // 加载更多
        rankRecyclerViewAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(boolean isReload) {

                currentPage++;
                initData(false);

                Log.d("loadmore", "loadmore");
            }
        });

        refreshRecyclerView.setLayoutManager(linearLayoutManager);
        refreshRecyclerView.addItemDecoration(dividerLine);
        refreshRecyclerView.setAdapter(rankRecyclerViewAdapter);
        return v;
    }

    //初始化视图
    private void initView(View v){

        userBackgroundImageView = v.findViewById(R.id.rankBackgroundImageView);
        refreshRecyclerView = v.findViewById(R.id.rankRecyclerView);
        rankTextView = v.findViewById(R.id.rankTitle);
        swipeRefreshLayout = v.findViewById(R.id.swipeLayout);
        userNameTextView = v.findViewById(R.id.userName);
        userImageView = v.findViewById(R.id.userImageView);
        userPointTextView = v.findViewById(R.id.pointTextView);
        rankNumTextView = v.findViewById(R.id.rankNum);
        cupImageView = v.findViewById(R.id.cupImageView);
    }

    //设置为金色
    private void setGold(){

        Glide.with(getActivity()).load(R.mipmap.gold_background).into(userBackgroundImageView);
        userPointTextView.setTextColor(getResources().getColor(R.color.colorGold));
    }

    //设置为黑色
    private void setBlack(){

        Glide.with(getActivity()).load(R.mipmap.black_background).into(userBackgroundImageView);
        userPointTextView.setTextColor(getResources().getColor(R.color.colorblack));
    }

    //设置为银色
    private void setSilver(){

        Glide.with(getActivity()).load(R.mipmap.silver_background).into(userBackgroundImageView);
        userPointTextView.setTextColor(getResources().getColor(R.color.personal_vip_num_silvery));
    }


    //获得用户特定排名
    private void getUserRank(){

        random32 = Utilities.getStringRandom(32);
        time10 = Utilities.get10Time();
        key64 = Utilities.get64Key(random32);

        final String json = "{\n" +
                "    \"validate_k\": \"1\",\n" +
                "    \"params\": [\n" +
                "        {\n" +
                "            \"type\": \"Members\",\n" +
                "            \"act\": \"Select_List\",\n" +
                "            \"para\": {\n" +
                "                \"params\": {\n" +
                "                    \"s_Alive\": \"\",\n" +
                "                    \"s_Attention_state\": \"\",\n" +
                "                    \"s_d1\": \"\",\n" +
                "                    \"s_d2\": \"\",\n" +
                "                    \"s_d3\": \"\",\n" +
                "                    \"s_d4\": \"\",\n" +
                "                    \"s_d5\": \""+firstDay+"\",\n" +
                "                    \"s_d6\": \""+secondDay+"\",\n" +
                "                    \"s_Gag\": \"\",\n" +
                "                    \"s_Keywords\": \"\",\n" +
                "                    \"s_Members_LV\": \"\",\n" +
                "                    \"s_Mid\": \"\",\n" +
                "                    \"s_Mobile\": \"\",\n" +
                "                    \"s_Openid\": \"\",\n" +
                "                    \"s_Order\": \"Integral desc,Rdate\",\n" +
                "                    \"s_Ranking_Mid\": \""+mid+"\",\n"+
                "                    \"s_Referee_Mid\": \"\",\n"+
                "                    \"s_Referee2_Mid\": \"\",\n"+
                "                    \"s_Stem_from\": \"2\",\n" +
                "                    \"s_The_sun\": \"\",\n" +
                "                    \"s_Total_parameter\": \"Mid,OpenID,Nickname,Head_img,Mname,Sex,Mobile,Rdate,Birthday,Alive,Gag,The_sun,Members_LV,Orders_count,Integral,card,Passwd,Addr,Stem_from,Token,Token_expiry,Token_IP,Referee_Mid,Referee2_Mid,Referee,Referee2,Reward,Withdraw,Contribution_Award,Balance,Ranking\"\n" +
                "                },\n" +
                "                \"pages\": {\n" +
                "                    \"p_c\": \"\",\n" +
                "                    \"p_First\": \"\",\n" +
                "                    \"p_inputHeight\": \"\",\n" +
                "                    \"p_Last\": \"\",\n" +
                "                    \"p_method\": \"\",\n" +
                "                    \"p_Next\": \"\",\n" +
                "                    \"p_Page\": \"\",\n" +
                "                    \"p_pageName\": \"\",\n" +
                "                    \"p_PageStyle\": \"\",\n" +
                "                    \"p_Pname\": \"\",\n" +
                "                    \"p_Previous\": \"\",\n" +
                "                    \"p_Ps\": \"\",\n" +
                "                    \"p_sk\": \"\",\n" +
                "                    \"p_Tp\": \"\"\n" +
                "                },\n" +
                "                \"sign_valid\": {\n" +
                "                    \"source\": \"Android\",\n" +
                "                    \"non_str\": \""+random32+"\",\n" +
                "                    \"stamp\": \""+time10+"\",\n" +
                "                    \"signature\": \""+Utilities.encode("s_Alive="+"s_Attention_state="+"s_d1="+"s_d2="+"s_d3="+"s_d4="+"s_d5="+firstDay+"s_d6="+secondDay+"s_Gag="+"s_Keywords="+"s_Members_LV="+"s_Mid="+"s_Mobile="+"s_Openid="+"s_Order=Integral desc,Rdate"+"s_Ranking_Mid="+mid+"s_Referee_Mid="+"s_Referee2_Mid="+"s_Stem_from=2"+"s_The_sun="+"s_Total_parameter=Mid,OpenID,Nickname,Head_img,Mname,Sex,Mobile,Rdate,Birthday,Alive,Gag,The_sun,Members_LV,Orders_count,Integral,card,Passwd,Addr,Stem_from,Token,Token_expiry,Token_IP,Referee_Mid,Referee2_Mid,Referee,Referee2,Reward,Withdraw,Contribution_Award,Balance,Ranking"+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        OkHttpClient okHttpClient = new OkHttpClient();

        RequestBody body = RequestBody.create(JSON, json);

        final Request request = new Request.Builder().url(API.getAPI()).post(body).build();

        okHttpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {

                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        Utilities.popUpAlert(getActivity(), "出现某些未知错误，请稍后再试");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.body() != null){

                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());

                        final JSONArray listData = jsonObject.getJSONArray("result").getJSONObject(0).getJSONArray("list");
                        final JSONObject jsonObj = listData.getJSONObject(0);
                        final String rank = jsonObj.getString("Ranking");

                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {

                                //设置排行名次
                                rankNumTextView.setText(rank);

                                //头像旁边的奖杯判断
                                if (rank.equals("1")){

                                    cupImageView.setImageDrawable(getActivity().getResources().getDrawable(R.mipmap.no1));
                                }else if (rank.equals("2")){

                                    cupImageView.setImageDrawable(getActivity().getResources().getDrawable(R.mipmap.no2));
                                }else if (rank.equals("3")){

                                    cupImageView.setImageDrawable(getActivity().getResources().getDrawable(R.mipmap.no3));
                                }
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    //初始化数据
    private void initData(final boolean isFirstLoad){

        random32 = Utilities.getStringRandom(32);
        time10 = Utilities.get10Time();
        key64 = Utilities.get64Key(random32);

        if (isFirstLoad){

            ranks.clear();
            currentPage = 1;
        }else {

            loadMoreRanks.clear();
            Log.d("loadmore", "loadmore");
        }

        final String json = "{\n" +
                "    \"validate_k\": \"1\",\n" +
                "    \"params\": [\n" +
                "        {\n" +
                "            \"type\": \"Members\",\n" +
                "            \"act\": \"Select_List\",\n" +
                "            \"para\": {\n" +
                "                \"params\": {\n" +
                "                    \"s_Alive\": \"\",\n" +
                "                    \"s_Attention_state\": \"\",\n" +
                "                    \"s_d1\": \"\",\n" +
                "                    \"s_d2\": \"\",\n" +
                "                    \"s_d3\": \"\",\n" +
                "                    \"s_d4\": \"\",\n" +
                "                    \"s_d5\": \""+firstDay+"\",\n" +
                "                    \"s_d6\": \""+secondDay+"\",\n" +
                "                    \"s_Gag\": \"\",\n" +
                "                    \"s_Keywords\": \"\",\n" +
                "                    \"s_Members_LV\": \"\",\n" +
                "                    \"s_Mid\": \"\",\n" +
                "                    \"s_Mobile\": \"\",\n" +
                "                    \"s_Openid\": \"\",\n" +
                "                    \"s_Order\": \"Integral desc,Rdate\",\n" +
                "                    \"s_Ranking_Mid\": \"\",\n"+
                "                    \"s_Referee_Mid\": \"\",\n"+
                "                    \"s_Referee2_Mid\": \"\",\n"+
                "                    \"s_Stem_from\": \"2\",\n" +
                "                    \"s_The_sun\": \"\",\n" +
                "                    \"s_Total_parameter\": \"Mid,OpenID,Nickname,Head_img,Mname,Sex,Mobile,Rdate,Birthday,Alive,Gag,The_sun,Members_LV,Orders_count,Integral,card,Passwd,Addr,Stem_from,Token,Token_expiry,Token_IP,Referee_Mid,Referee2_Mid,Referee,Referee2,Reward,Withdraw,Contribution_Award,Balance,Ranking\"\n" +
                "                },\n" +
                "                \"pages\": {\n" +
                "                    \"p_c\": \"\",\n" +
                "                    \"p_First\": \"\",\n" +
                "                    \"p_inputHeight\": \"\",\n" +
                "                    \"p_Last\": \"\",\n" +
                "                    \"p_method\": \"\",\n" +
                "                    \"p_Next\": \"\",\n" +
                "                    \"p_Page\": \""+currentPage+"\",\n" +
                "                    \"p_pageName\": \"\",\n" +
                "                    \"p_PageStyle\": \"\",\n" +
                "                    \"p_Pname\": \"\",\n" +
                "                    \"p_Previous\": \"\",\n" +
                "                    \"p_Ps\": \"10\",\n" +
                "                    \"p_sk\": \"\",\n" +
                "                    \"p_Tp\": \"\"\n" +
                "                },\n" +
                "                \"sign_valid\": {\n" +
                "                    \"source\": \"Android\",\n" +
                "                    \"non_str\": \""+random32+"\",\n" +
                "                    \"stamp\": \""+time10+"\",\n" +
                "                    \"signature\": \""+Utilities.encode("s_Alive="+"s_Attention_state="+"s_d1="+"s_d2="+"s_d3="+"s_d4="+"s_d5="+firstDay+"s_d6="+secondDay+"s_Gag="+"s_Keywords="+"s_Members_LV="+"s_Mid="+"s_Mobile="+"s_Openid="+"s_Order=Integral desc,Rdate"+"s_Ranking_Mid="+"s_Referee_Mid="+"s_Referee2_Mid="+"s_Stem_from=2"+"s_The_sun="+"s_Total_parameter=Mid,OpenID,Nickname,Head_img,Mname,Sex,Mobile,Rdate,Birthday,Alive,Gag,The_sun,Members_LV,Orders_count,Integral,card,Passwd,Addr,Stem_from,Token,Token_expiry,Token_IP,Referee_Mid,Referee2_Mid,Referee,Referee2,Reward,Withdraw,Contribution_Award,Balance,Ranking"+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody body = RequestBody.create(JSON, json);

        Log.d("rank", json);

        final Request request = new Request.Builder().url(API.getAPI()).post(body).build();

        okHttpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {

                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        Utilities.popUpAlert(getActivity(), "出现某些未知错误，请稍后再试");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.body() != null){

                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());

                        if (isFirstLoad){

                            Log.d("loadMore", jsonObject.toString());
                        }

                        JSONArray listData = jsonObject.getJSONArray("result").getJSONObject(0).getJSONArray("list");
                        totalPage = jsonObject.getJSONArray("result").getJSONObject(0).getJSONObject("page").getInt("Pc");
                        Rank.Builder builder = new Rank.Builder();

                        for (int i=0; i < listData.length(); i++){

                            JSONObject eachObj = listData.getJSONObject(i);

                            if (isFirstLoad){

                                Rank rank = builder.userIcon(API.getAnothereHostName()+eachObj.getString("Head_img")).userName(eachObj.getString("Mname")).userPoint("积分："+eachObj.getString("Integral")).userRank(String.valueOf(i+1)).mid(eachObj.getString("Mid")).memberLv(eachObj.getInt("Members_LV")).grade(eachObj.getJSONObject("card").getInt("Grade")).build();
                                ranks.add(rank);
                            }else {

                                Rank rank = builder.userIcon(API.getAnothereHostName()+eachObj.getString("Head_img")).userName(eachObj.getString("Mname")).userPoint("积分："+eachObj.getString("Integral")).userRank(String.valueOf(rankRecyclerViewAdapter.getItemCount()+i)).mid(eachObj.getString("Mid")).grade(eachObj.getJSONObject("card").getInt("Grade")).memberLv(eachObj.getInt("Members_LV")).build();
                                loadMoreRanks.add(rank);
                            }
                        }

                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {

                                swipeRefreshLayout.setRefreshing(false);

                                if (!isFirstLoad){

                                    rankRecyclerViewAdapter.setLoadMoreData(loadMoreRanks);
                                    rankRecyclerViewAdapter.notifyDataSetChanged();
                                }
                                if (ranks.size() == 0){

                                    //加载完成，更新footer view提示
                                    rankRecyclerViewAdapter.setLoadEndView(R.layout.layout_load_end);

                                    rankRecyclerViewAdapter.loadEnd();
                                }else if (currentPage >= totalPage){

                                    //加载完成，更新footer view提示
                                    rankRecyclerViewAdapter.setLoadEndView(R.layout.layout_load_end);
                                    rankRecyclerViewAdapter.loadEnd();
                                }
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    //获取用户排名信息
    private void getUserInfo(){

        random32 = Utilities.getStringRandom(32);
        time10 = Utilities.get10Time();
        key64 = Utilities.get64Key(random32);

        final SharedPreferences sharedPreferences = getActivity().getSharedPreferences("TokenData", Context.MODE_PRIVATE);
        String mid = sharedPreferences.getString(MID_KEY, "");

        final String json = "{\n" +
                "    \"validate_k\": \"1\",\n" +
                "    \"params\": [\n" +
                "        {\n" +
                "            \"type\": \"Members\",\n" +
                "            \"act\": \"Select_Detail\",\n" +
                "            \"para\": {\n" +
                "                \"params\": {\n" +
                "                    \"d_Alive\": \"\",\n" +
                "                    \"d_IsUpdateToken\": \"0\",\n"+
                "                    \"d_Mid\": \""+mid+"\",\n" +
                "                    \"d_Token\": \"\",\n" +
                "                    \"s_Total_parameter\": \"Mid,OpenID,Nickname,Head_img,Mname,Sex,Mobile,Rdate,Birthday,Alive,Gag,The_sun,Members_LV,Orders_count,Integral,card,Passwd,Addr,Stem_from,Token,Token_expiry,Token_IP,Referee_Mid,Referee2_Mid,Referee,Referee2,Reward,Withdraw,Contribution_Award,Balance\"\n" +
                "                },\n" +
                "                \"sign_valid\": {\n" +
                "                    \"source\": \"Android\",\n" +
                "                    \"non_str\": \""+random32+"\",\n" +
                "                    \"stamp\": \""+time10+"\",\n" +
                "                    \"signature\": \""+Utilities.encode("d_Alive="+"d_IsUpdateToken=0"+"d_Mid="+mid+"d_Token="+"s_Total_parameter=Mid,OpenID,Nickname,Head_img,Mname,Sex,Mobile,Rdate,Birthday,Alive,Gag,The_sun,Members_LV,Orders_count,Integral,card,Passwd,Addr,Stem_from,Token,Token_expiry,Token_IP,Referee_Mid,Referee2_Mid,Referee,Referee2,Reward,Withdraw,Contribution_Award,Balance"+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    ]\n" +
                "}";
        OkHttpClient okHttpClient = new OkHttpClient();

        RequestBody requestBody = RequestBody.create(JSON, json);

        Request request = new Request.Builder().url(API.getAPI()).post(requestBody).build();

        okHttpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {

                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        Utilities.popUpAlert(getActivity(), "出现某些未知错误，请稍后再试");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.body() != null){

                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        //结果详情
                        JSONObject detailObj = jsonObject.getJSONArray("result").getJSONObject(0).getJSONObject("detail");

                        Log.d("rank", jsonObject.toString());
                        final String nickName = detailObj.getString("Nickname");
                        final String mName = detailObj.getString("Mname");
                        final int integral = detailObj.getInt("Integral");
                        final int memberLevel = detailObj.getInt("Members_LV");
                        final String headImg = detailObj.getString("Head_img");

                        final int grade = detailObj.getJSONObject("card").getInt("Grade");

                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {

                                swipeRefreshLayout.setRefreshing(false);

                                if (grade == 1){

                                    setSilver();
                                }else if (grade == 3){

                                    setBlack();
                                }else {

                                    setGold();
                                }

                                userPointTextView.setText("积分："+String.valueOf(integral));
                                userNameTextView.setText(mName);
                                Glide.with(getActivity()).load(API.getAnothereHostName()+headImg)
                                        .apply(bitmapTransform(new CropCircleTransformation()))
                                        .into((userImageView));

                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
