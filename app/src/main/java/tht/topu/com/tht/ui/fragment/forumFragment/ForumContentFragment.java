package tht.topu.com.tht.ui.fragment.forumFragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
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
import tht.topu.com.tht.utils.OnLoadMoreListener;
import tht.topu.com.tht.utils.Utilities;


public class ForumContentFragment extends Fragment {

    private RecyclerView forumRecyclerView;
    private TextView forumEmptyTextView;
    private ForumRecyclerViewAdapter forumRecyclerViewAdapter;
    private LocalBroadcastManager localBroadcastManager;
    private IntentFilter intentFilter;
    private LocalReceiver localReceiver;
    private SwipeRefreshLayout swipeRefreshLayout;

    private static final String CID_ID = "0x31x1";

    public static final MediaType JSON = MediaType
            .parse("application/json; charset=utf-8");

//    private String cid;
    private String flid;

    private String random32;
    private String time10;
    private String key64;

    private Handler uiHandler;

    int currentPage;
    int totalPage;

    private List<Forum> forumList = new ArrayList<>();
    private List<Forum> loadMoreForums = new ArrayList<>();

//    public static ForumContentFragment newInstance(String cid) {
//
//        Bundle argus = new Bundle();
//        argus.putString(CID_ID, cid);
//        ForumContentFragment forumContentFragment = new ForumContentFragment();
//        forumContentFragment.setArguments(argus);
//        return forumContentFragment;
//    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
//        cid = getArguments().getString(CID_ID);
        intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.mybroadcast.MY_BROADCAST");
        localReceiver = new LocalReceiver();
        localBroadcastManager = LocalBroadcastManager.getInstance(getActivity().getApplicationContext());
        localBroadcastManager.registerReceiver(localReceiver, intentFilter);
        View view = inflater.inflate(R.layout.fragment_forum_content, container, false);

        uiHandler = new Handler(Looper.getMainLooper());

        initView(view);
        return view;
    }

    private void initView(View view) {

        forumRecyclerView = view.findViewById(R.id.forumRecyclerView);
        forumEmptyTextView = view.findViewById(R.id.forumEmptyTextView);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);

        swipeRefreshLayout.setEnabled(false);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        //列表分割线
        DividerLine dividerLine = new DividerLine();
        dividerLine.setColor(getResources().getColor(R.color.colorBorder));
        dividerLine.setSize(2);

        forumRecyclerView.setHasFixedSize(true);
        forumRecyclerView.setLayoutManager(linearLayoutManager);
        forumRecyclerView.addItemDecoration(dividerLine);

        forumRecyclerViewAdapter = new ForumRecyclerViewAdapter(getActivity(), null, true);

        forumRecyclerViewAdapter.notifyDataSetChanged();


        //初始化 开始加载更多的loading View
        forumRecyclerViewAdapter.setLoadingView(R.layout.progress_item);
        //加载完成，更新footer view提示
        forumRecyclerViewAdapter.setLoadEndView(R.layout.layout_load_end);

        // 加载更多
        forumRecyclerViewAdapter.setOnLoadMoreListener(new com.othershe.baseadapter.interfaces.OnLoadMoreListener() {
            @Override
            public void onLoadMore(boolean isReload) {

                currentPage++;
                initData(false, flid, "12");

                Log.d("loadmore", "forumLoadMore");
            }
        });

        forumRecyclerView.setAdapter(forumRecyclerViewAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (uiHandler != null) {

            uiHandler = null;
        }
    }

    public void initData(final boolean isFirstLoad, String flid, String cid) {

        random32 = Utilities.getStringRandom(32);
        time10 = Utilities.get10Time();
        key64 = Utilities.get64Key(random32);

        if (isFirstLoad) {

            currentPage = 1;
            forumList.clear();
        } else {

            loadMoreForums.clear();
        }

        String json = "{\n" +
                "    \"validate_k\": \"1\",\n" +
                "    \"params\": [\n" +
                "        {\n" +
                "            \"type\": \"Forum\",\n" +
                "            \"act\": \"Select_List\",\n" +
                "            \"para\": {\n" +
                "                \"params\": {\n" +
                "                    \"s_Add_Essence\": \"\",\n" +
                "                    \"s_Cid\": \"" + cid + "\",\n" +
                "                    \"s_d1\": \"\",\n" +
                "                    \"s_d2\": \"\",\n" +
                "                    \"s_Fid\": \"\",\n" +
                "                    \"s_Flid\": \"" + flid + "\",\n" +
                "                    \"s_isDel\": \"2\",\n" +
                "                    \"s_isTop\": \"\",\n" +
                "                    \"s_Keywords\": \"\",\n" +
                "                    \"s_Mid\": \"\",\n" +
                "                    \"s_Order\": \"isTop desc,Add_Essence desc,Rdate desc\",\n" +
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
                "                    \"p_Page\": \"" + currentPage + "\",\n" +
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
                "                    \"non_str\": \"" + random32 + "\",\n" +
                "                    \"stamp\": \"" + time10 + "\",\n" +
                "                    \"signature\": \"" + Utilities.encode("s_Add_Essence=" + "s_Cid=" + cid + "s_d1=" + "s_d2=" + "s_Fid=" + "s_Flid=" + flid + "s_isDel=2" + "s_isTop=" + "s_Keywords=" + "s_Mid=" + "s_Order=isTop desc,Add_Essence desc,Rdate desc" + "s_Stem_from=2" + "s_Total_parameter=Fid,Cid,Ctitle,Flid,Ltitle,Ftitle,Mid,Member,isTop,Add_Essence,isDel,Rdate,Finfo,Fabulous_Num,Stem_from,Comment_Num,Final_date" + "non_str=" + random32 + "stamp=" + time10 + "keySecret=" + key64) + "\"\n" +
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

                        Toast.makeText(getActivity(), "加载数据失败，请重试", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    Log.d("forum", jsonObject.toString());
                    final JSONArray jsonArr = jsonObject.getJSONArray("result").getJSONObject(0).getJSONArray("list");

                    Forum.Builder builder = new Forum.Builder();
                    totalPage = jsonObject.getJSONArray("result").getJSONObject(0).getJSONObject("page").getInt("Pc");

                    for (int i = 0; i < jsonArr.length(); i++) {

                        JSONObject eachForumObj = jsonArr.getJSONObject(i);
                        Forum forum = builder.ForumTitle(eachForumObj.getString("Ftitle")).userName(eachForumObj.getJSONObject("Member").getString("Mname")).vip("VIP:" + eachForumObj.getJSONObject("Member").getString("Members_LV")).avatarIcon(API.getHostName() + eachForumObj.getJSONObject("Member").getString("Head_img")).likeNum(eachForumObj.getInt("Fabulous_Num")).tagName(eachForumObj.getString("Ltitle")).replyNum(eachForumObj.getInt("Comment_Num")).isDel(eachForumObj.getBoolean("isDel")).isFavorite(eachForumObj.getBoolean("Add_Essence")).isTop(eachForumObj.getBoolean("isTop")).fid(eachForumObj.getString("Fid")).flid(eachForumObj.getString("Flid")).build();

                        //判断是否为第一次加载
                        if (isFirstLoad) {

                            forumList.add(forum);
                        } else {

                            loadMoreForums.add(forum);
                        }
                    }

                    if (uiHandler != null) {

                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {

                                swipeRefreshLayout.setRefreshing(false);

                                Log.d("forumSize", String.valueOf(forumList.size()));

                                if (forumList.size() > 0) {

                                    forumEmptyTextView.setVisibility(View.GONE);
                                } else {

                                    forumEmptyTextView.setVisibility(View.VISIBLE);
                                }

                                if (!isFirstLoad) {

                                    forumRecyclerViewAdapter.setLoadMoreData(loadMoreForums);
                                } else {

                                    Log.d("forumSize", String.valueOf(forumList.size()));
                                    forumRecyclerViewAdapter.reset();
                                    forumRecyclerViewAdapter.setNewData(forumList);
                                }

//                                forumRecyclerViewAdapter.notifyDataSetChanged();
                                if (forumList.size() == 0) {

                                    forumRecyclerViewAdapter.loadEnd();
                                } else if (currentPage >= totalPage) {

                                    forumRecyclerViewAdapter.loadEnd();
                                }
                            }
                        });
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public class LocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            flid = intent.getStringExtra("Flid");

//            forumList.clear();
            initData(true, flid, "12");
            Log.d("接受广播", "接受广播");


            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(true);
                }
            });
            forumRecyclerViewAdapter.notifyDataSetChanged();
        }
    }
}