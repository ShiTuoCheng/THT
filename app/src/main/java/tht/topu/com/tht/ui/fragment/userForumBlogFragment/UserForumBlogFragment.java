package tht.topu.com.tht.ui.fragment.userForumBlogFragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import tht.topu.com.tht.R;
import tht.topu.com.tht.adapter.ForumRecyclerViewAdapter;
import tht.topu.com.tht.modle.Forum;
import tht.topu.com.tht.utils.API;
import tht.topu.com.tht.utils.DividerLine;
import tht.topu.com.tht.utils.Utilities;

public class UserForumBlogFragment extends Fragment {

    private boolean isMyBlog;
    private static final String isBlogKey = "1x11x1";
    private RecyclerView userForumBlogRecyclerView;
    private TextView forumEmptyTextView;
    private ForumRecyclerViewAdapter forumRecyclerViewAdapter;

    private List<Forum> forums = new ArrayList<>();
    private List<Forum> loadMoreForums = new ArrayList<>();

    private String mid;
    private String json;
    private int currentPage;
    private int totalPage;

    private static final String MID_KEY = "1x11";

    public static final MediaType JSON = MediaType
            .parse("application/json; charset=utf-8");
    private Handler uiHandler;

    private String random32;
    private String time10;
    private String key64;

    public static UserForumBlogFragment newInstance(boolean isMyBlog){

        Bundle argus = new Bundle();
        argus.putBoolean(isBlogKey, isMyBlog);
        UserForumBlogFragment userForumBlogFragment = new UserForumBlogFragment();

        userForumBlogFragment.setArguments(argus);
        return userForumBlogFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        isMyBlog = getArguments().getBoolean(isBlogKey);
        View view = inflater.inflate(R.layout.fragment_user_forum_blog, container, false);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("TokenData", Context.MODE_PRIVATE);
        mid = sharedPreferences.getString(MID_KEY, "");

        uiHandler = new Handler(Looper.getMainLooper());

        Log.d("is", String.valueOf(isMyBlog));
        initView(view);

        if (isMyBlog){

            initData(true);
        }
        return view;
    }

    private void initView(View view){

        userForumBlogRecyclerView = (RecyclerView)view.findViewById(R.id.userForumBlogRecyclerView);
        forumEmptyTextView = (TextView)view.findViewById(R.id.userBlogEmptyTextView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        //列表分割线
        DividerLine dividerLine = new DividerLine();
        dividerLine.setColor(getResources().getColor(R.color.colorBorder));
        dividerLine.setSize(2);

        userForumBlogRecyclerView.setHasFixedSize(true);
        userForumBlogRecyclerView.setLayoutManager(linearLayoutManager);
        userForumBlogRecyclerView.addItemDecoration(dividerLine);

        forumRecyclerViewAdapter = new ForumRecyclerViewAdapter(getActivity().getApplicationContext(), forums, true);

        forumRecyclerViewAdapter.notifyDataSetChanged();

        //初始化 开始加载更多的loading View
        forumRecyclerViewAdapter.setLoadingView(R.layout.progress_item);
        //加载完成，更新footer view提示
        forumRecyclerViewAdapter.setLoadEndView(R.layout.layout_load_end);

        forumRecyclerViewAdapter.setOnLoadMoreListener(new com.othershe.baseadapter.interfaces.OnLoadMoreListener() {
            @Override
            public void onLoadMore(boolean isReload) {

                if (isMyBlog){

                    currentPage++;
                    initData(false);
                }
            }
        });

        userForumBlogRecyclerView.setAdapter(forumRecyclerViewAdapter);
    }

    private void initData(final boolean isFirstLoad){

        random32 = Utilities.getStringRandom(32);
        time10 = Utilities.get10Time();
        key64 = Utilities.get64Key(random32);

        if (isFirstLoad){

            forums.clear();
            currentPage = 1;
        }else {

            loadMoreForums.clear();
        }

        if (isMyBlog){

            json = "{\n" +
                    "    \"validate_k\": \"1\",\n" +
                    "    \"params\": [\n" +
                    "        {\n" +
                    "            \"type\": \"Forum\",\n" +
                    "            \"act\": \"Select_List\",\n" +
                    "            \"para\": {\n" +
                    "                \"params\": {\n" +
                    "                    \"s_Add_Essence\": \"\",\n" +
                    "                    \"s_Cid\": \"\",\n" +
                    "                    \"s_d1\": \"\",\n" +
                    "                    \"s_d2\": \"\",\n" +
                    "                    \"s_Fid\": \"\",\n" +
                    "                    \"s_Flid\": \"\",\n" +
                    "                    \"s_isDel\": \"\",\n" +
                    "                    \"s_isTop\": \"\",\n" +
                    "                    \"s_Keywords\": \"\",\n" +
                    "                    \"s_Mid\": \""+mid+"\",\n" +
                    "                    \"s_Order\": \"Rdate desc\",\n" +
                    "                    \"s_Stem_from\": \"2\",\n" +
                    "                    \"s_Total_parameter\": \"Fid,Cid,Ctitle,Flid,Ltitle,Ftitle,Mid,Member,isTop,Add_Essence,isDel,Rdate,Finfo,Fabulous_Num,Stem_from,Comment_Num,Final_date\"\n" +
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
                    "                    \"p_Ps\": \"15\",\n" +
                    "                    \"p_sk\": \"\",\n" +
                    "                    \"p_Tp\": \"\"\n" +
                    "                },\n" +
                    "                \"sign_valid\": {\n" +
                    "                    \"source\": \"Android\",\n" +
                    "                    \"non_str\": \""+random32+"\",\n" +
                    "                    \"stamp\": \""+time10+"\",\n" +
                    "                    \"signature\": \""+Utilities.encode("s_Add_Essence="+"s_Cid="+"s_d1="+"s_d2="+"s_Fid="+"s_Flid="+"s_isDel="+"s_isTop="+"s_Keywords="+"s_Mid="+mid+"s_Order=Rdate desc"+"s_Stem_from=2"+"s_Total_parameter=Fid,Cid,Ctitle,Flid,Ltitle,Ftitle,Mid,Member,isTop,Add_Essence,isDel,Rdate,Finfo,Fabulous_Num,Stem_from,Comment_Num,Final_date"+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
                    "                }\n" +
                    "            }\n" +
                    "        }\n" +
                    "    ]\n" +
                    "}";
        }


        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(JSON, json);
        Request request = new Request.Builder().url(API.getAPI()).post(requestBody).build();
        okHttpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {

                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        Toast.makeText(getActivity(), "除了些小差错，获取数据失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.body() != null){

                    try {
                        JSONObject jsonObj = new JSONObject(response.body().string());
                        JSONArray jsonArr = jsonObj.getJSONArray("result").getJSONObject(0).getJSONArray("list");

                        totalPage = jsonObj.getJSONArray("result").getJSONObject(0).getJSONObject("page").getInt("Pc");

                        Forum.Builder builder = new Forum.Builder();

                        for (int i=0; i<jsonArr.length(); i++){

                            JSONObject eachForumObj = jsonArr.getJSONObject(i);

                            Forum forum = builder.ForumTitle(eachForumObj.getString("Ftitle")).userName(eachForumObj.getJSONObject("Member").getString("Mname")).avatarIcon(API.getHostName()+eachForumObj.getJSONObject("Member").getString("Head_img")).likeNum(eachForumObj.getInt("Fabulous_Num")).tagName(eachForumObj.getString("Ltitle")).replyNum(eachForumObj.getInt("Comment_Num")).isDel(eachForumObj.getBoolean("isDel")).vip("VIP:"+eachForumObj.getJSONObject("Member").getString("Members_LV")).isTop(eachForumObj.getBoolean("isTop")).isFavorite(eachForumObj.getBoolean("Add_Essence")).fid(eachForumObj.getString("Fid")).flid(eachForumObj.getString("Flid")).build();

                            //判断是否为第一次加载
                            if (isFirstLoad){

                                forums.add(forum);
                            }else {

                                loadMoreForums.add(forum);
                            }
                        }

                        //切换至主线程
                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {

                                if (forums.size() == 0){

                                    forumEmptyTextView.setVisibility(View.VISIBLE);
                                }else {

                                    forumEmptyTextView.setVisibility(View.GONE);
                                }

                                if (!isFirstLoad){

                                    forumRecyclerViewAdapter.setLoadMoreData(loadMoreForums);
                                }
                                forumRecyclerViewAdapter.notifyDataSetChanged();
                                if (forums.size() == 0){

                                    forumRecyclerViewAdapter.loadEnd();
                                }else if (currentPage >= totalPage){

                                    forumRecyclerViewAdapter.loadEnd();
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
}
