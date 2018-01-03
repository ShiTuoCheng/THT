package tht.topu.com.tht.ui.fragment.mainFragment;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.othershe.baseadapter.Util;
import com.othershe.baseadapter.ViewHolder;
import com.othershe.baseadapter.base.CommonBaseAdapter;
import com.othershe.baseadapter.interfaces.OnItemChildClickListener;
import com.othershe.baseadapter.interfaces.OnItemClickListener;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.listener.OnBannerListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import tht.topu.com.tht.R;
import tht.topu.com.tht.adapter.MainViewPagerAdapter;
import tht.topu.com.tht.modle.Product;
import tht.topu.com.tht.ui.activity.CardActivity;
import tht.topu.com.tht.ui.activity.SearchActivity;
import tht.topu.com.tht.ui.activity.WebViewActivity;
import tht.topu.com.tht.utils.API;
import tht.topu.com.tht.utils.DividerLine;
import tht.topu.com.tht.utils.GlideImageLoader;
import tht.topu.com.tht.utils.Utilities;

public class MainFragment extends Fragment {

//    private List<String> bannerImages = new ArrayList<>();
//    private List<String> bannerUrls = new ArrayList<>();
    private List<String> tabTexts = new ArrayList<>();
    private List<String> tabCids = new ArrayList<>();

//    private Banner banner;
//    private TabLayout tabLayout;
//    private ViewPager mainViewPager;
//    private SwipeRefreshLayout swipeRefreshLayout;

    private RecyclerView mainCategoryRecyclerView;
    private MainCategoryAdapter mainCategoryAdapter;
    private AppBarLayout appBarLayout;

    private String random32;
    private String time10;
    private String key64;

    public static final MediaType JSON = MediaType
            .parse("application/json; charset=utf-8");

    private Handler uiHandler;
    private Handler alertHandler;

    private CoordinatorLayout coordinatorLayout;
    private ImageView searchImg;
    private ImageView signInImg;

    private LocalBroadcastManager localBroadcastManager;
    private Intent broadcastIntent;

    private int selectedPosition=0;

    private boolean canSignIn = false;

    String mid;
    int point;

    private static final String MID_KEY = "1x11";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        initView(v);

        localBroadcastManager = LocalBroadcastManager.getInstance(getActivity().getApplicationContext());

        uiHandler = new Handler(Looper.getMainLooper());

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("TokenData", Context.MODE_PRIVATE);
        mid = sharedPreferences.getString(MID_KEY, "");

        alertHandler = new Handler(){

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){

                    case 0:

                        Snackbar.make(coordinatorLayout, "网络不可用", Snackbar.LENGTH_LONG).setAction("去设置", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                            }
                        }).show();

                        break;

                    case 1:
                        Toast toast = Toast.makeText(getContext().getApplicationContext(), "出现某些未知错误，请稍后再试", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.NO_GRAVITY, 0, Utilities.dip2px(getContext().getApplicationContext(), 200));
                        toast.show();

                        break;
                }
            }
        };

        // 判断网络是否可用
        if (!Utilities.isNetworkAvaliable(getActivity().getApplicationContext())){

            alertHandler.sendEmptyMessageDelayed(0,1000);
        }else {

            initData();
//
//            swipeRefreshLayout.post(new Runnable() {
//                @Override
//                public void run() {
//                    swipeRefreshLayout.setRefreshing(true);
//                }
//            });
//
//            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//                @Override
//                public void onRefresh() {
//                    initData();
//                }
//            });
        }

        return v;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (alertHandler != null){

            alertHandler = null;
        }

        if (uiHandler != null){

            uiHandler = null;
        }
    }

    //初始化视图
    private void initView(View view){

//        banner = view.findViewById(R.id.mainBanner);
//        tabLayout = view.findViewById(R.id.mainTabLayout);
//        mainViewPager = view.findViewById(R.id.mainViewPager);
//        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        coordinatorLayout = view.findViewById(R.id.coordinatorLayout);
        //swipeRefreshLayout.setProgressViewEndTarget (true,300);
        mainCategoryRecyclerView = view.findViewById(R.id.mainCategoryLayout);
        appBarLayout = (AppBarLayout) view.findViewById(R.id.appbar);
        signInImg = (ImageView)view.findViewById(R.id.signInImg);
        searchImg = view.findViewById(R.id.searchImg);

        signInImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (canSignIn){

                    getPoint();
                }else{

                    isSignToday(true);
                }
            }
        });

        searchImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Utilities.jumpToActivity(getActivity(), SearchActivity.class);
            }
        });

        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();

        transaction.add(R.id.frameLayout_container, new ProductFragment());

        transaction.commit();

    }

    // 初始化数据
    private void initData(){

        getTabIcon();

        isSignToday(false);
    }

    // 查询是否签到
    private void isSignToday(final boolean canShow) {
        random32 = Utilities.getStringRandom(32);
        time10 = Utilities.get10Time();
        key64 = Utilities.get64Key(random32);

        String json = "{\n" +
                "    \"validate_k\": \"1\",\n" +
                "    \"params\": [\n" +
                "{\n" +
                "    \"type\": \"Members_Integral\",\n" +
                "    \"act\": \"Select_List\",\n" +
                "    \"para\": {\n" +
                "        \"params\": {\n" +
                "            \"s_d1\": \"\",\n" +
                "            \"s_d2\": \"\",\n" +
                "            \"s_Keywords\": \"\",\n" +
                "            \"s_Kind\": \"\",\n" +
                "            \"s_Mid\": \""+mid+"\",\n" +
                "            \"s_Miid\": \"\",\n" +
                "            \"s_Oid\": \"\",\n" +
                "            \"s_Order\": \"\",\n" +
                "            \"s_Total_parameter\": \"Miid,Mid,Oid,Idate,Integral,Kind,Sign_count\"\n" +
                "        },\n" +
                "        \"pages\": {\n" +
                "            \"s_Order\": \"\",\n" +
                "            \"s_Miid\": \"\",\n" +
                "            \"s_Mid\": \"\",\n" +
                "            \"s_d1\": \"\",\n" +
                "            \"s_d2\": \"\",\n" +
                "            \"s_Kind\": \"\",\n" +
                "            \"s_Oid\": \"\",\n" +
                "            \"p_c\": \"\",\n" +
                "            \"p_First\": \"\",\n" +
                "            \"p_inputHeight\": \"\",\n" +
                "            \"p_Last\": \"\",\n" +
                "            \"p_method\": \"\",\n" +
                "            \"p_Next\": \"\",\n" +
                "            \"p_Page\": \"\",\n" +
                "            \"p_pageName\": \"\",\n" +
                "            \"p_PageStyle\": \"\",\n" +
                "            \"p_Pname\": \"\",\n" +
                "            \"p_Previous\": \"\",\n" +
                "            \"p_Ps\": \"\",\n" +
                "            \"p_sk\": \"\",\n" +
                "            \"p_Tp\": \"\"\n" +
                "        },\n" +
                "        \"sign_valid\": {\n" +
                "            \"source\": \"Android\",\n" +
                "            \"non_str\": \""+random32+"\",\n" +
                "            \"stamp\": \""+time10+"\",\n" +
                "            \"signature\": \""+Utilities.encode("s_d1="+"s_d2="+"s_Keywords="+"s_Kind="+"s_Mid="+mid+"s_Miid="+"s_Oid="+"s_Order="+"s_Total_parameter=Miid,Mid,Oid,Idate,Integral,Kind,Sign_count"+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
                "        }\n" +
                "    }\n" +
                "}"+
                "    ]\n" +
                "}";

        OkHttpClient okHttpClient = new OkHttpClient();

        RequestBody requestBody = RequestBody.create(JSON, json);

        Request request = new Request.Builder().url(API.getAPI()).post(requestBody).build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.body() != null){

                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());

                        final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");

                        JSONArray list = jsonObject.getJSONArray("result").getJSONObject(0).getJSONArray("list");

                        String date = list.getJSONObject(list.length()-1).getString("Idate").split(" ")[0];

                        final int signCount = Integer.parseInt(list.getJSONObject(list.length()-1).getString("Sign_count"));

                        Log.d("idate", date);

                        final Date myDate = new Date(date);

                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {

                                if((fmt.format(myDate).toString()).equals(fmt.format(new Date()).toString())){//格式化为相同格式

                                    Glide.with(getActivity()).load(R.mipmap.sign_in).into(signInImg);
                                    canSignIn = false;

                                    if (canShow){

                                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogCustom);

                                        if (signCount != 0){


                                            final AlertDialog alertDialog = builder.setTitle("您今天已经签过到了").setMessage("您已经连续签到"+signCount+"天")
                                                    .setCancelable(true)
                                                    .setNegativeButton("知道了", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            dialogInterface.dismiss();
                                                        }
                                                    })
                                                    .create();

                                            alertDialog.setCancelable(false);

                                            alertDialog.show();
                                        }else{

                                            final AlertDialog alertDialog = builder.setTitle("您今天已经签过到了")
                                                    .setCancelable(true)
                                                    .setNegativeButton("知道了", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            dialogInterface.dismiss();
                                                        }
                                                    })
                                                    .create();

                                            alertDialog.setCancelable(false);

                                            alertDialog.show();
                                        }
                                    }

                                } else {


                                    canSignIn = true;
                                    Glide.with(getActivity()).load(R.mipmap.uncheck_sign_in).into(signInImg);

                                }
                            }
                        });


                        Log.d("signIn", jsonObject.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    // 点击签到
    private void signIn(){

        random32 = Utilities.getStringRandom(32);
        time10 = Utilities.get10Time();
        key64 = Utilities.get64Key(random32);

        String json = "{\n" +
                "    \"validate_k\": \"1\",\n" +
                "    \"params\": [\n" +
                "   {\n" +
                "    \"type\": \"Members_Integral\",\n" +
                "    \"act\": \"Add\",\n" +
                "    \"para\": {\n" +
                "        \"params\": {\n" +
                "            \"Integral\": \""+point+"\",\n" +
                "            \"Kind\": \"1\",\n" +
                "            \"Mid\": \""+mid+"\",\n" +
                "            \"Oid\": \"-1\"\n" +
                "        },\n" +
                "        \"sign_valid\": {\n" +
                "            \"source\": \"Android\",\n" +
                "            \"non_str\": \""+random32+"\",\n" +
                "            \"stamp\": \""+time10+"\",\n" +
                "            \"signature\": \""+Utilities.encode("Integral="+point+"Kind=1"+"Mid="+mid+"Oid=-1"+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
                "        }\n" +
                "    }\n" +
                "    }"+
                "    ]\n" +
                "}";

        OkHttpClient okHttpClient = new OkHttpClient();

        RequestBody requestBody = RequestBody.create(JSON, json);

        Request request = new Request.Builder().url(API.getAPI()).post(requestBody).build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.body() != null){

                    try {

                        final JSONObject jsonObject = new JSONObject(response.body().string());

                        final String error = jsonObject.getJSONArray("result").getJSONObject(0).getString("error");

                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {

                                if (error.equals("SUCCESS")){

                                    Utilities.popUpAlert(getActivity(), "签到成功");
                                    Glide.with(getActivity()).load(R.mipmap.sign_in).into(signInImg);

                                    canSignIn = false;
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

    // 获取积分
    private void getPoint(){

        random32 = Utilities.getStringRandom(32);
        time10 = Utilities.get10Time();
        key64 = Utilities.get64Key(random32);

        String json = "{\n" +
                "    \"validate_k\": \"1\",\n" +
                "    \"params\": [\n" +
                "        {\n" +
                "            \"type\": \"Init\",\n" +
                "            \"act\": \"getinfo\",\n" +
                "            \"para\": {\n" +
                "                \"params\": {\n" +
                "                    \"Iid\": \"18\"\n" +
                "                },\n" +
                "                \"sign_valid\": {\n" +
                "                    \"source\": \"Android\",\n" +
                "                    \"non_str\": \""+random32+"\",\n" +
                "                    \"stamp\": \""+time10+"\",\n" +
                "                    \"signature\": \""+Utilities.encode("Iid=18"+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
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

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.body() != null){

                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());

                        point = Integer.parseInt(jsonObject.getJSONArray("result").getJSONObject(0).getJSONObject("Iinfo").getString("Iinfo"));
                        signIn();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    //获取banner图
//    private void getBannerImages(){
//
//        random32 = Utilities.getStringRandom(32);
//        time10 = Utilities.get10Time();
//        key64 = Utilities.get64Key(random32);
//
//        String json = "{\n" +
//                "    \"validate_k\": \"1\",\n" +
//                "    \"params\": [\n" +
//                "        {\n" +
//                "            \"type\": \"Article\",\n" +
//                "            \"act\": \"Select_List\",\n" +
//                "            \"para\": {\n" +
//                "                \"params\": {\n" +
//                "                    \"s_Aid\": \"\",\n" +
//                "                    \"s_Alive\": \"\",\n" +
//                "                    \"s_d1\": \"\",\n" +
//                "                    \"s_d2\": \"\",\n" +
//                "                    \"s_Keywords\": \"\",\n" +
//                "                    \"s_Kind\": \"55\",\n" +
//                "                    \"s_Order\": \"Layer\",\n" +
//                "                    \"s_Recommend\": \"\",\n" +
//                "                    \"s_Total_parameter\": \"Aid,Atitle,Url,Alive,Recommend,Kind,Layer,Ainfo,Atime,Pic1,Pic2,Summary,ieTitle,seoKeywords,seoDescription\"\n" +
//                "                },\n" +
//                "                \"pages\": {\n" +
//                "                    \"p_c\": \"\",\n" +
//                "                    \"p_First\": \"\",\n" +
//                "                    \"p_inputHeight\": \"\",\n" +
//                "                    \"p_Last\": \"\",\n" +
//                "                    \"p_method\": \"\",\n" +
//                "                    \"p_Next\": \"\",\n" +
//                "                    \"p_Page\": \"\",\n" +
//                "                    \"p_pageName\": \"\",\n" +
//                "                    \"p_PageStyle\": \"\",\n" +
//                "                    \"p_Pname\": \"\",\n" +
//                "                    \"p_Previous\": \"\",\n" +
//                "                    \"p_Ps\": \"\",\n" +
//                "                    \"p_sk\": \"\",\n" +
//                "                    \"p_Tp\": \"\"\n" +
//                "                },\n" +
//                "                \"sign_valid\": {\n" +
//                "                    \"source\": \"Android\",\n" +
//                "                    \"non_str\": \""+random32+"\",\n" +
//                "                    \"stamp\": \""+time10+"\",\n" +
//                "                    \"signature\": \""+Utilities.encode("s_Aid="+"s_Alive="+"s_d1="+"s_d2="+"s_Keywords="+"s_Kind=55"+"s_Order=Layer"+"s_Recommend="+"s_Total_parameter=Aid,Atitle,Url,Alive,Recommend,Kind,Layer,Ainfo,Atime,Pic1,Pic2,Summary,ieTitle,seoKeywords,seoDescription"+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
//                "                }\n" +
//                "            }\n" +
//                "        }\n" +
//                "    ]\n" +
//                "}";
//
//        OkHttpClient okHttpClient = new OkHttpClient();
//
//        RequestBody requestBody = RequestBody.create(JSON, json);
//
//        Request request = new Request.Builder().url(API.getAPI()).post(requestBody).build();
//
//        okHttpClient.newCall(request).enqueue(new Callback() {
//
//            @Override
//            public void onFailure(Call call, IOException e) {
//
//                if (uiHandler != null){
//
//                    uiHandler.post(new Runnable() {
//                        @Override
//                        public void run() {
//
//                            alertHandler.sendEmptyMessageDelayed(1,1000);
//
////                            if (swipeRefreshLayout.isRefreshing()){
////
////                                swipeRefreshLayout.setRefreshing(false);
////                            }
//                        }
//                    });
//                }
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//
//                if (response.body() != null){
//
//                    try {
//                        JSONObject resultJson = new JSONObject(response.body().string());
//
//                        JSONArray imgArr = resultJson.getJSONArray("result").getJSONObject(0).getJSONArray("list");
//
//                        for (int i=0; i<imgArr.length(); i++){
//
//                            JSONObject imgObj = imgArr.getJSONObject(i);
//                            bannerImages.add(API.getHostName()+imgObj.getString("Pic1"));
//                            bannerUrls.add(imgObj.getString("Url"));
//                        }
//
//                        if (uiHandler != null){
//
//                            uiHandler.post(new Runnable() {
//                                @Override
//                                public void run() {
//
//                                    banner.setImages(bannerImages);
//                                    banner.setImageLoader(new GlideImageLoader());
//                                    banner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR);
//
//                                    banner.setOnBannerListener(new OnBannerListener() {
//                                        @Override
//                                        public void OnBannerClick(int position) {
//
//                                            Intent intent = new Intent(getActivity(), WebViewActivity.class);
//                                            intent.putExtra("url", bannerUrls.get(position));
//
//                                            startActivity(intent);
//                                        }
//                                    });
//
//                                    //开始轮播
//                                    banner.start();
//                                }
//                            });
//                        }
//
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
//    }



    //获取tab栏图标
    private void getTabIcon(){



        random32 = Utilities.getStringRandom(32);
        time10 = Utilities.get10Time();
        key64 = Utilities.get64Key(random32);

        String json = "{\n" +
                "    \"validate_k\": \"1\",\n" +
                "    \"params\": [\n" +
                "        {\n" +
                "            \"type\": \"Classification\",\n" +
                "            \"act\": \"Select_List\",\n" +
                "            \"para\": {\n" +
                "                \"params\": {\n" +
                "                    \"s_Alive\": \"\",\n" +
                "                    \"s_Cid\": \"\",\n" +
                "                    \"s_Keywords\": \"\",\n" +
                "                    \"s_Kind\": \"1\",\n" +
                "                    \"s_Order\": \"Layer\",\n" +
                "                    \"s_Stem_from\":\"2\",\n" +
                "                    \"s_Total_parameter\": \"Cid,Ctitle,Pic1,Pic2,Layer,Alive,Stem_from\"\n" +
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
                "                    \"signature\": \""+Utilities.encode("s_Alive="+"s_Cid="+"s_Keywords="+"s_Kind=1"+"s_Order=Layer"+"s_Stem_from=2"+"s_Total_parameter=Cid,Ctitle,Pic1,Pic2,Layer,Alive,Stem_from"+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        OkHttpClient okHttpClient = new OkHttpClient();

        RequestBody requestBody = RequestBody.create(JSON, json);

        Log.d("tabJson", json);

        Request request = new Request.Builder().url(API.getAPI()).post(requestBody).build();

        okHttpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {

                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        alertHandler.sendEmptyMessageDelayed(1,1000);

//                        if (swipeRefreshLayout.isRefreshing()){
//
//                            swipeRefreshLayout.setRefreshing(false);
//                        }
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.body() != null){

                    try {
                        JSONObject resultJson = new JSONObject(response.body().string());
                        JSONArray imgArr = resultJson.getJSONArray("result").getJSONObject(0).getJSONArray("list");

                        tabTexts.clear();
                        tabCids.clear();

                        tabTexts.add(0, "推荐");
                        tabCids.add("");

                        for (int i=0; i<imgArr.length(); i++){

                            JSONObject iconObj = imgArr.getJSONObject(i);
                            tabTexts.add(iconObj.getString("Ctitle"));
                            tabCids.add(iconObj.getString("Cid"));
                        }

                        if (uiHandler != null){

                            uiHandler.post(new Runnable() {
                                @Override
                                public void run() {

//                                    if (swipeRefreshLayout.isRefreshing()){
//
//                                        swipeRefreshLayout.setRefreshing(false);
//                                    }
                                    if (MainFragment.this.isAdded()){

//                                        setTabIcon();

                                        mainCategoryAdapter = new MainCategoryAdapter(getActivity(), tabTexts, false);

                                        final GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 4);
                                        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                                        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                                        mainCategoryRecyclerView.setLayoutManager(gridLayoutManager);
                                        mainCategoryRecyclerView.setAdapter(mainCategoryAdapter);

                                        if (mainCategoryAdapter != null){

                                            mainCategoryAdapter.notifyDataSetChanged();
                                        }

                                        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                                            @Override
                                            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                                                if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {

                                                    uiHandler.postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            mainCategoryRecyclerView.setLayoutManager(linearLayoutManager);
                                                        }
                                                    }, 100);
                                                    // Collapsed
                                                } else if (verticalOffset == 0) {

                                                    uiHandler.postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            mainCategoryRecyclerView.setLayoutManager(gridLayoutManager);
                                                        }
                                                    }, 100);
                                                    // Expanded
                                                } else {
                                                    // Somewhere in between
                                                    linearLayoutManager.scrollToPositionWithOffset(selectedPosition, 0);
                                                }
                                            }
                                        });

                                        broadcastIntent = new Intent("com.product.mybroadcast.MY_BROADCAST");
                                        broadcastIntent.putExtra("Cid", "");
                                        broadcastIntent.putExtra("Recommend", "1");
                                        localBroadcastManager.sendBroadcast(broadcastIntent);
                                        if (mainCategoryAdapter != null){

                                            mainCategoryAdapter.notifyDataSetChanged();
                                        }
                                    }
                                }
                            });
                        }
                    }catch (JSONException e){

                        e.printStackTrace();
                    }
                }
            }
        });
    }

    //设置tab的图标（主线程运行）
    private void setTabIcon(){

        MainViewPagerAdapter mainViewPagerAdapter = new MainViewPagerAdapter(getChildFragmentManager());
        mainViewPagerAdapter.notifyDataSetChanged();

//        tabLayout.removeAllTabs();
//
//        for (int i = 0; i<tabTexts.size(); i++){
//
//            if (i == 0){
//
//                tabLayout.addTab(tabLayout.newTab().setText(tabTexts.get(0)));
//                // 加载推荐列表
//                mainViewPagerAdapter.addFragment(ProductFragment.newInstance("", "1"));
//            }else {
//
//                tabLayout.addTab(tabLayout.newTab().setText(tabTexts.get(i)));
//                // 加载列表
//                mainViewPagerAdapter.addFragment(ProductFragment.newInstance(tabCids.get(i), ""));
//            }
//        }
//
//        mainViewPager.setOffscreenPageLimit(tabTexts.size()-1);
//        mainViewPager.setAdapter(mainViewPagerAdapter);
//        mainViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//            @Override
//            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//
//            }
//
//            @Override
//            public void onPageSelected(int position) {
//
//            }
//
//            @Override
//            public void onPageScrollStateChanged(int state) {
//
//            }
//        });
//
//        mainViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout){
//
//            @Override
//            public void onPageScrollStateChanged(int state) {
//                toggleRefreshing(state == ViewPager.SCROLL_STATE_IDLE);
//            }
//
//            private void toggleRefreshing(boolean enabled) {
//                if (swipeRefreshLayout != null) {
//                    swipeRefreshLayout.setEnabled(enabled);
//                }
//            }
//        });
//
//        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
//            @Override
//            public void onTabSelected(TabLayout.Tab tab) {
//                mainViewPager.setCurrentItem(tab.getPosition());
//
//                int index = tab.getPosition();
//
//                tabLayout.setTabTextColors(getResources().getColor(R.color.colorWhite), getResources().getColor(R.color.colorGold));
//
//
//            }
//
//            @Override
//            public void onTabUnselected(TabLayout.Tab tab) {
//
//                int index = tab.getPosition();
//
//                tabLayout.setTabTextColors(getResources().getColor(R.color.colorWhite), getResources().getColor(R.color.colorGold));
//
//            }
//
//            @Override
//            public void onTabReselected(TabLayout.Tab tab) {
//
//            }
//
//        });

    }

    public class MainCategoryAdapter extends CommonBaseAdapter<String> {

        private TextView text;
        private CardView selectedView;
        private List<String> data = new ArrayList<>();

        public MainCategoryAdapter(Context context, List<String> datas, boolean isOpenLoadMore) {
            super(context, datas, isOpenLoadMore);
            this.data = datas;
        }

        @Override
        protected void convert(ViewHolder holder, String data, int position) {

            text = holder.getView(R.id.mainCategoryTextView);
            selectedView = holder.getView(R.id.selectedCardView);
            text.setText(data);

//        Log.d("comein", String.valueOf(position));
            if (position == 0){
                holder.setTextColor(R.id.mainCategoryTextView,Color.parseColor("#c92e2e"));
                selectedView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected int getItemLayoutId() {
            return R.layout.layout_main_category;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            super.onBindViewHolder(holder, position);
            if(selectedPosition==position) {
                text.setTextColor(Color.parseColor("#c92e2e"));
                selectedView.setVisibility(View.VISIBLE);
            }else {
                text.setTextColor(Color.parseColor("#707070"));
                selectedView.setVisibility(View.INVISIBLE);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    selectedPosition=position;
                    notifyDataSetChanged();

                    if (position == 0){
                        broadcastIntent = new Intent("com.product.mybroadcast.MY_BROADCAST");
                        broadcastIntent.putExtra("Cid", "");
                        broadcastIntent.putExtra("Recommend", "1");
                        localBroadcastManager.sendBroadcast(broadcastIntent);
                        if (mainCategoryAdapter != null){

                            mainCategoryAdapter.notifyDataSetChanged();
                        }
                    }else{

                        broadcastIntent = new Intent("com.product.mybroadcast.MY_BROADCAST");
                        broadcastIntent.putExtra("Cid", tabCids.get(position));
                        broadcastIntent.putExtra("Recommend", "");
                        localBroadcastManager.sendBroadcast(broadcastIntent);
                        if (mainCategoryAdapter != null){

                            mainCategoryAdapter.notifyDataSetChanged();
                        }
                    }
                }
            });
        }

    }
}
