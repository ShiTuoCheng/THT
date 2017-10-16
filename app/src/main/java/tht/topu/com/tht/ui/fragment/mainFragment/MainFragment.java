package tht.topu.com.tht.ui.fragment.mainFragment;


import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.listener.OnBannerListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
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
import tht.topu.com.tht.ui.activity.CardActivity;
import tht.topu.com.tht.ui.activity.WebViewActivity;
import tht.topu.com.tht.utils.API;
import tht.topu.com.tht.utils.GlideImageLoader;
import tht.topu.com.tht.utils.Utilities;

public class MainFragment extends Fragment {

    private List<String> bannerImages = new ArrayList<>();
    private List<String> bannerUrls = new ArrayList<>();
    private List<String> unselectedIcons = new ArrayList<>();
    private List<String> selectedIcons = new ArrayList<>();
    private List<String> tabTexts = new ArrayList<>();
    private List<String> tabCids = new ArrayList<>();

    private List<Drawable> unselectedDraw = new ArrayList<>();
    private List<Drawable> selectedDraw = new ArrayList<>();

    private Banner banner;
    private TabLayout tabLayout;
    private ViewPager mainViewPager;
    private SwipeRefreshLayout swipeRefreshLayout;

    private String random32;
    private String time10;
    private String key64;

    public static final MediaType JSON = MediaType
            .parse("application/json; charset=utf-8");

    private Handler uiHandler;

    private Handler alertHandler;

    private CoordinatorLayout coordinatorLayout;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        initView(v);

        uiHandler = new Handler(Looper.getMainLooper());

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

            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(true);
                }
            });

            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    initData();
                }
            });
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

        banner = view.findViewById(R.id.mainBanner);
        tabLayout = view.findViewById(R.id.mainTabLayout);
        mainViewPager = view.findViewById(R.id.mainViewPager);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        coordinatorLayout = view.findViewById(R.id.coordinatorLayout);
        //swipeRefreshLayout.setProgressViewEndTarget (true,300);

    }

    // 初始化数据
    private void initData(){

        bannerImages.clear();

        getBannerImages();

        getTabIcon();
    }

    //获取banner图
    private void getBannerImages(){

        random32 = Utilities.getStringRandom(32);
        time10 = Utilities.get10Time();
        key64 = Utilities.get64Key(random32);

        String json = "{\n" +
                "    \"validate_k\": \"1\",\n" +
                "    \"params\": [\n" +
                "        {\n" +
                "            \"type\": \"Article\",\n" +
                "            \"act\": \"Select_List\",\n" +
                "            \"para\": {\n" +
                "                \"params\": {\n" +
                "                    \"s_Aid\": \"\",\n" +
                "                    \"s_Alive\": \"\",\n" +
                "                    \"s_d1\": \"\",\n" +
                "                    \"s_d2\": \"\",\n" +
                "                    \"s_Keywords\": \"\",\n" +
                "                    \"s_Kind\": \"55\",\n" +
                "                    \"s_Order\": \"Layer\",\n" +
                "                    \"s_Recommend\": \"\",\n" +
                "                    \"s_Total_parameter\": \"Aid,Atitle,Url,Alive,Recommend,Kind,Layer,Ainfo,Atime,Pic1,Pic2,Summary,ieTitle,seoKeywords,seoDescription\"\n" +
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
                "                    \"signature\": \""+Utilities.encode("s_Aid="+"s_Alive="+"s_d1="+"s_d2="+"s_Keywords="+"s_Kind=55"+"s_Order=Layer"+"s_Recommend="+"s_Total_parameter=Aid,Atitle,Url,Alive,Recommend,Kind,Layer,Ainfo,Atime,Pic1,Pic2,Summary,ieTitle,seoKeywords,seoDescription"+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
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

                            if (swipeRefreshLayout.isRefreshing()){

                                swipeRefreshLayout.setRefreshing(false);
                            }
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.body() != null){

                    try {
                        JSONObject resultJson = new JSONObject(response.body().string());

                        JSONArray imgArr = resultJson.getJSONArray("result").getJSONObject(0).getJSONArray("list");

                        for (int i=0; i<imgArr.length(); i++){

                            JSONObject imgObj = imgArr.getJSONObject(i);
                            bannerImages.add(API.getHostName()+imgObj.getString("Pic1"));
                            bannerUrls.add(imgObj.getString("Url"));
                        }

                        if (uiHandler != null){

                            uiHandler.post(new Runnable() {
                                @Override
                                public void run() {

                                    banner.setImages(bannerImages);
                                    banner.setImageLoader(new GlideImageLoader());
                                    banner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR);

                                    banner.setOnBannerListener(new OnBannerListener() {
                                        @Override
                                        public void OnBannerClick(int position) {

                                            Intent intent = new Intent(getActivity(), WebViewActivity.class);
                                            intent.putExtra("url", bannerUrls.get(position));

                                            startActivity(intent);
                                        }
                                    });

                                    //开始轮播
                                    banner.start();
                                }
                            });
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

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
                "                    \"signature\": \""+Utilities.encode("s_Alive="+"s_Cid="+"s_Keywords="+"s_Order=Layer"+"s_Stem_from=2"+"s_Total_parameter=Cid,Ctitle,Pic1,Pic2,Layer,Alive,Stem_from"+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
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

                        if (swipeRefreshLayout.isRefreshing()){

                            swipeRefreshLayout.setRefreshing(false);
                        }
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

                        unselectedDraw.clear();
                        selectedDraw.clear();
                        tabCids.clear();

                        unselectedIcons.clear();
                        selectedIcons.clear();

                        tabTexts.add(0, "推荐");
                        unselectedDraw.add(0, getResources().getDrawable(R.drawable.recommand));
                        selectedDraw.add(0, getResources().getDrawable(R.drawable.selected_recommand));
                        tabCids.add("");

                        for (int i=0; i<imgArr.length(); i++){

                            JSONObject iconObj = imgArr.getJSONObject(i);
                            tabTexts.add(iconObj.getString("Ctitle"));
                            unselectedIcons.add(iconObj.getString("Pic2"));
                            selectedIcons.add(iconObj.getString("Pic1"));
                            tabCids.add(iconObj.getString("Cid"));
                        }

                        for (int j=0; j<selectedIcons.size(); j++){

                            selectedDraw.add(new BitmapDrawable(Utilities.returnBitmap(API.getHostName()+selectedIcons.get(j))));
                            unselectedDraw.add(new BitmapDrawable(Utilities.returnBitmap(API.getHostName()+unselectedIcons.get(j))));
                        }

                        if (uiHandler != null){

                            uiHandler.post(new Runnable() {
                                @Override
                                public void run() {

                                    swipeRefreshLayout.setRefreshing(false);
                                    Log.d("finish", "loadFinish");
                                    if (MainFragment.this.isAdded()){

                                        setTabIcon();
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

        tabLayout.removeAllTabs();

        for (int i = 0; i<tabTexts.size(); i++){

            if (i == 0){

                tabLayout.addTab(tabLayout.newTab().setIcon(selectedDraw.get(i)).setText(tabTexts.get(0)));
                // 加载推荐列表
                mainViewPagerAdapter.addFragment(ProductFragment.newInstance("", "1"));
            }else {

                tabLayout.addTab(tabLayout.newTab().setIcon(unselectedDraw.get(i)).setText(tabTexts.get(i)));
                // 加载列表
                mainViewPagerAdapter.addFragment(ProductFragment.newInstance(tabCids.get(i), ""));
            }
        }

        mainViewPager.setOffscreenPageLimit(tabTexts.size()-1);
        mainViewPager.setAdapter(mainViewPagerAdapter);
        mainViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mainViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout){

            @Override
            public void onPageScrollStateChanged(int state) {
                toggleRefreshing(state == ViewPager.SCROLL_STATE_IDLE);
            }

            private void toggleRefreshing(boolean enabled) {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setEnabled(enabled);
                }
            }
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mainViewPager.setCurrentItem(tab.getPosition());

                int index = tab.getPosition();

                tabLayout.setTabTextColors(getResources().getColor(R.color.colorWhite), getResources().getColor(R.color.colorGold));

                for (int i=0; i<tabTexts.size(); i++){

                    if (index == i) {

                        tabLayout.getTabAt(tab.getPosition()).setIcon(selectedDraw.get(i));
                    }
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

                int index = tab.getPosition();

                tabLayout.setTabTextColors(getResources().getColor(R.color.colorWhite), getResources().getColor(R.color.colorGold));

                for (int i=0; i<tabTexts.size(); i++){

                    if (index == i) {

                        tabLayout.getTabAt(tab.getPosition()).setIcon(unselectedDraw.get(i));
                    }
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }

        });

    }
}
