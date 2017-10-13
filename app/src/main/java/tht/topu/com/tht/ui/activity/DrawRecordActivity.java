package tht.topu.com.tht.ui.activity;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.othershe.baseadapter.interfaces.OnLoadMoreListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.lemon.view.RefreshRecyclerView;
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
import tht.topu.com.tht.utils.API;
import tht.topu.com.tht.utils.DividerLine;
import tht.topu.com.tht.utils.Utilities;

public class DrawRecordActivity extends AppCompatActivity {

    private RecyclerView drawRecordRecyclerView;
    private ImageView backImageView;
    private List<Rank> ranks = new ArrayList<>();
    private List<Rank> moreRanks = new ArrayList<>();

    private RankRecyclerViewAdapter rankRecyclerViewAdapter;

    private String random32;
    private String time10;
    private String key64;

    int currentPage;
    int totalPage;

    private Handler alertHandler;
    private Handler uiHandler;

    private CoordinatorLayout drawLayout;

    public static final MediaType JSON = MediaType
            .parse("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw_record);

        initView();
        uiHandler = new Handler(Looper.getMainLooper());

        alertHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {

                    case 0:
                        Snackbar.make(drawLayout, "网络不可用", Snackbar.LENGTH_LONG).setAction("去设置", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                            }
                        }).show();

                        break;

                    case 1:
                        Toast toast = Toast.makeText(DrawRecordActivity.this.getApplicationContext(), "出现某些未知错误，请稍后再试", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.NO_GRAVITY, 0, Utilities.dip2px(DrawRecordActivity.this.getApplicationContext(), 200));
                        toast.show();

                        break;
                }
            }
        };

        rankRecyclerViewAdapter = new RankRecyclerViewAdapter(DrawRecordActivity.this, ranks, true, false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(DrawRecordActivity.this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        //列表分割线
        DividerLine dividerLine = new DividerLine();
        dividerLine.setColor(getResources().getColor(R.color.colorBorder));
        dividerLine.setSize(2);

        drawRecordRecyclerView.setLayoutManager(linearLayoutManager);
        drawRecordRecyclerView.setAdapter(rankRecyclerViewAdapter);

        rankRecyclerViewAdapter.notifyDataSetChanged();
        //初始化 开始加载更多的loading View
        rankRecyclerViewAdapter.setLoadingView(R.layout.progress_item);
        //加载完成，更新footer view提示
        rankRecyclerViewAdapter.setLoadEndView(R.layout.layout_load_end);

        drawRecordRecyclerView.addItemDecoration(dividerLine);

        // 加载更多
        rankRecyclerViewAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(boolean isReload) {

                currentPage++;
                initData(false);
            }
        });

        backImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DrawRecordActivity.this.finish();
            }
        });

        if (!Utilities.isNetworkAvaliable(DrawRecordActivity.this.getApplicationContext())){

            alertHandler.sendEmptyMessageDelayed(0,1000);
        }else {

            initData(true);
        }
    }

    private void initView(){

        drawLayout = (CoordinatorLayout)findViewById(R.id.drawRecordLayout);
        drawRecordRecyclerView = (RecyclerView) findViewById(R.id.drawRecordRecyclerview);
        backImageView = (ImageView)findViewById(R.id.drawRecordBack);
    }

    private void initData(final boolean isFirstLoad){

        random32 = Utilities.getStringRandom(32);
        time10 = Utilities.get10Time();
        key64 = Utilities.get64Key(random32);

        if (isFirstLoad){

            ranks.clear();
            currentPage = 1;
        }else {

            moreRanks.clear();
        }

        String json = "{\n" +
                "    \"validate_k\": \"1\",\n" +
                "    \"params\": [\n" +
                "        {\n" +
                "            \"type\": \"Prize_Record\",\n" +
                "            \"act\": \"Select_List\",\n" +
                "            \"para\": {\n" +
                "                \"params\": {\n" +
                "                    \"s_d1\": \"\",\n" +
                "                    \"s_d2\": \"\",\n" +
                "                    \"s_Is_Record\":\"\",\n"+
                "                    \"s_Keywords\": \"\",\n" +
                "                    \"s_Mid\": \"\",\n" +
                "                    \"s_Order\": \"Pdate desc\",\n" +
                "                    \"s_Pid\": \"\",\n" +
                "                    \"s_Prid\": \"\",\n" +
                "                    \"s_Total_parameter\": \"Prid,Mid,Member,Mobile,Pdate,Pid,Ptitle,Is_Record\"\n" +
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
                "                    \"p_Ps\": \"20\",\n" +
                "                    \"p_sk\": \"\",\n" +
                "                    \"p_Tp\": \"\"\n" +
                "                },\n" +
                "                \"sign_valid\": {\n" +
                "                    \"source\": \"Android\",\n" +
                "                    \"non_str\": \""+random32+"\",\n" +
                "                    \"stamp\": \""+time10+"\",\n" +
                "                    \"signature\": \""+Utilities.encode("s_d1="+"s_d2="+"s_Is_Record="+"s_Keywords="+"s_Mid="+"s_Order=Pdate desc"+"s_Pid="+"s_Prid="+"s_Total_parameter=Prid,Mid,Member,Mobile,Pdate,Pid,Ptitle,Is_Record"+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
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

                if (uiHandler != null){

                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {

                            alertHandler.sendEmptyMessageDelayed(1,1000);
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                try {

                    JSONObject jsonObject = new JSONObject(response.body().string());
                    JSONArray recordArr = jsonObject.getJSONArray("result").getJSONObject(0).getJSONArray("list");

                    totalPage = jsonObject.getJSONArray("result").getJSONObject(0).getJSONObject("page").getInt("Pc");
                    Rank.Builder rankBuilder = new Rank.Builder();

                    for (int i = 0; i < recordArr.length(); i++){

                        JSONObject eachRecordObj = recordArr.getJSONObject(i);

                        if (!eachRecordObj.getString("Ptitle").equals("")){

                            Rank rank = rankBuilder.userIcon(API.getAnothereHostName()+eachRecordObj.getJSONObject("Member").getString("Head_img")).userName(eachRecordObj.getJSONObject("Member").getString("Mname")).userPoint("积分："+eachRecordObj.getJSONObject("Member").getString("Integral")).build();

                            if (isFirstLoad){

                                ranks.add(rank);
                            }else {

                                moreRanks.add(rank);
                            }
                        }
                    }

                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!isFirstLoad){

                                rankRecyclerViewAdapter.setLoadMoreData(moreRanks);
                            }
                            rankRecyclerViewAdapter.notifyDataSetChanged();

                            if (ranks.size() == 0){

                                rankRecyclerViewAdapter.loadEnd();
                            }else if (currentPage >= totalPage){


                                rankRecyclerViewAdapter.loadEnd();
                            }
                            rankRecyclerViewAdapter.notifyDataSetChanged();
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (alertHandler != null){

            alertHandler = null;
        }

        if (uiHandler != null){

            uiHandler = null;
        }
    }
}
