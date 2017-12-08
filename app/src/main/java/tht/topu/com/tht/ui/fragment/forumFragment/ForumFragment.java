package tht.topu.com.tht.ui.fragment.forumFragment;


import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.util.Util;

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
import tht.topu.com.tht.adapter.MainViewPagerAdapter;
import tht.topu.com.tht.ui.activity.ForumPostActivity;
import tht.topu.com.tht.ui.fragment.mainFragment.ProductFragment;
import tht.topu.com.tht.utils.API;
import tht.topu.com.tht.utils.Utilities;


public class ForumFragment extends Fragment {


    private Spinner spinner;
//    private ViewPager forumViewPager;
//    private TabLayout forumTabLayout;
    private LocalBroadcastManager localBroadcastManager;

    private List<String> unselectedIcons = new ArrayList<>();
    private List<String> selectedIcons = new ArrayList<>();
    private List<String> tabTexts = new ArrayList<>();
    private List<String> tabCids = new ArrayList<>();

    private List<Drawable> unselectedDraw = new ArrayList<>();
    private List<Drawable> selectedDraw = new ArrayList<>();

    private Intent broadcastIntent;

    private ImageView postButton;

    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout forumLayout;

    private String random32;
    private String time10;
    private String key64;

    private List<String> tagTexts = new ArrayList<>();
    private List<String> flids = new ArrayList<>();

    public static final MediaType JSON = MediaType
            .parse("application/json; charset=utf-8");

    private Handler uiHandler;

    private Handler alertHandler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        uiHandler = new Handler(Looper.getMainLooper());

        alertHandler = new Handler(){

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){

                    case 0:

                        Snackbar.make(forumLayout, "网络不可用", Snackbar.LENGTH_INDEFINITE).setAction("去设置", new View.OnClickListener() {
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

        View view =  inflater.inflate(R.layout.fragment_forum, container, false);

        initView(view);

        // 判断网络是否可用
        if (!Utilities.isNetworkAvaliable(getActivity().getApplicationContext())){

            alertHandler.sendEmptyMessageDelayed(0,1000);
        }else {

            initData();
        }

        return view;
    }

    private void initView(View view){

        spinner = view.findViewById(R.id.spinner);

        forumLayout = view.findViewById(R.id.forumLayout);

//        forumTabLayout = view.findViewById(R.id.forumTabLayout);
//        forumViewPager = view.findViewById(R.id.forumViewPager);

        postButton = (ImageView)view.findViewById(R.id.postForum);
        swipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.swipe_refresh_layout);

        localBroadcastManager = LocalBroadcastManager.getInstance(getActivity().getApplicationContext());

        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String flid = flids.get(spinner.getSelectedItemPosition());
                Bundle bundle = new Bundle();
                bundle.putString("flid", flid);
                Utilities.jumpToActivity(getActivity(), ForumPostActivity.class, bundle, "bundleFlid");
            }
        });

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });
        swipeRefreshLayout.setEnabled(false);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                if (flids.size() != 0){

                    broadcastIntent = new Intent("com.example.mybroadcast.MY_BROADCAST");
                    broadcastIntent.putExtra("Flid", flids.get(i));
                    localBroadcastManager.sendBroadcast(broadcastIntent);
                    Log.d("Flid", flids.get(i));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();

        transaction.add(R.id.forumContainer, new ForumContentFragment());

        transaction.commit();
    }

    @Override
    public void onStop() {
        super.onStop();
//        spinner.setSelection(0);
//        forumViewPager.setCurrentItem(0);
//        forumTabLayout.getTabAt(0).select();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void initData(){
//        getTabIcon();
        getTabTag();
    }

    //获取标签
    private void getTabTag(){

        random32 = Utilities.getStringRandom(32);
        time10 = Utilities.get10Time();
        key64 = Utilities.get64Key(random32);

        String json = "{\n" +
                "    \"validate_k\": \"1\",\n" +
                "    \"params\": [\n" +
                "        {\n" +
                "            \"type\": \"Forum_Label\",\n" +
                "            \"act\": \"Select_List\",\n" +
                "            \"para\": {\n" +
                "                \"params\": {\n" +
                "                    \"s_Alive\": \"\",\n" +
                "                    \"s_Flid\": \"\",\n" +
                "                    \"s_Keywords\": \"\",\n" +
                "                    \"s_Order\": \"Layer\",\n" +
                "                    \"s_Stem_from\": \"2\",\n" +
                "                    \"s_Total_parameter\": \"Flid,Ltitle,Layer,Alive,Stem_from\"\n" +
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
                "                    \"signature\": \""+Utilities.encode("s_Alive="+"s_Flid="+"s_Keywords="+"s_Order=Layer"+"s_Stem_from=2"+"s_Total_parameter=Flid,Ltitle,Layer,Alive,Stem_from"+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
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

                        alertHandler.sendEmptyMessageDelayed(1,1000);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.body() != null){

                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        JSONArray jsonArr = jsonObject.getJSONArray("result").getJSONObject(0).getJSONArray("list");

                        for (int i=0; i<jsonArr.length(); i++){

                            String tagText = jsonArr.getJSONObject(i).getString("Ltitle");
                            String flid = jsonArr.getJSONObject(i).getString("Flid");

                            flids.add(flid);
                            tagTexts.add(tagText);
                        }

                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {

                                ArrayAdapter sort_spinner_adapter = new ArrayAdapter<>(getActivity(),R.layout.custom_spinner_text, tagTexts);
                                spinner.setAdapter(sort_spinner_adapter);

                                swipeRefreshLayout.setRefreshing(false);
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    //获取tab栏图标
//    private void getTabIcon(){
//
//        random32 = Utilities.getStringRandom(32);
//        time10 = Utilities.get10Time();
//        key64 = Utilities.get64Key(random32);
//
//        String json = "{\n" +
//                "    \"validate_k\": \"1\",\n" +
//                "    \"params\": [\n" +
//                "        {\n" +
//                "            \"type\": \"Classification\",\n" +
//                "            \"act\": \"Select_List\",\n" +
//                "            \"para\": {\n" +
//                "                \"params\": {\n" +
//                "                    \"s_Alive\": \"\",\n" +
//                "                    \"s_Cid\": \"\",\n" +
//                "                    \"s_Keywords\": \"\",\n" +
//                "                    \"s_Kind\": \"2\",\n" +
//                "                    \"s_Order\": \"Layer\",\n" +
//                "                    \"s_Stem_from\":\"2\",\n" +
//                "                    \"s_Total_parameter\": \"Cid,Ctitle,Pic1,Pic2,Layer,Alive,Stem_from\"\n" +
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
//                "                    \"signature\": \""+Utilities.encode("s_Alive="+"s_Cid="+"s_Keywords="+"s_Kind=2"+"s_Order=Layer"+"s_Stem_from=2"+"s_Total_parameter=Cid,Ctitle,Pic1,Pic2,Layer,Alive,Stem_from"+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
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
//                uiHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//
//                        alertHandler.sendEmptyMessageDelayed(1,1000);
//
//                        if (swipeRefreshLayout.isRefreshing()){
//
//                            swipeRefreshLayout.setRefreshing(false);
//                        }
//                    }
//                });
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//
//                if (response.body() != null){
//
//                    try {
//                        JSONObject resultJson = new JSONObject(response.body().string());
//                        JSONArray imgArr = resultJson.getJSONArray("result").getJSONObject(0).getJSONArray("list");
//
//                        tabTexts.clear();
//
//                        unselectedDraw.clear();
//                        selectedDraw.clear();
//                        tabCids.clear();
//
//                        unselectedIcons.clear();
//                        selectedIcons.clear();
//
//                        unselectedDraw.add(0, getResources().getDrawable(R.drawable.recommand));
//                        selectedDraw.add(0, getResources().getDrawable(R.drawable.selected_recommand));
//
//                        for (int i=0; i<imgArr.length(); i++){
//
//                            JSONObject iconObj = imgArr.getJSONObject(i);
//                            tabTexts.add(iconObj.getString("Ctitle"));
//                            unselectedIcons.add(iconObj.getString("Pic2"));
//                            selectedIcons.add(iconObj.getString("Pic1"));
//                            tabCids.add(iconObj.getString("Cid"));
//                        }
//
//                        for (int j=0; j<selectedIcons.size(); j++){
//
//                            selectedDraw.add(new BitmapDrawable(Utilities.returnBitmap(API.getHostName()+selectedIcons.get(j))));
//                            unselectedDraw.add(new BitmapDrawable(Utilities.returnBitmap(API.getHostName()+unselectedIcons.get(j))));
//                        }
//
//                        if (uiHandler != null){
//
//                            uiHandler.post(new Runnable() {
//                                @Override
//                                public void run() {
//
//                                    swipeRefreshLayout.setRefreshing(false);
//                                    setTabIcon();
//                                }
//                            });
//                        }
//                    }catch (JSONException e){
//
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
//    }

    //设置tab的图标（主线程运行）
//    private void setTabIcon(){
//
//        mainViewPagerAdapter = new MainViewPagerAdapter(getFragmentManager());
//        mainViewPagerAdapter.notifyDataSetChanged();
//
//        forumTabLayout.removeAllTabs();
//
//        for (int i = 0; i<tabTexts.size(); i++){
//
//            forumTabLayout.addTab(forumTabLayout.newTab().setIcon(unselectedDraw.get(i)).setText(tabTexts.get(i)));
//            // 加载列表
//            mainViewPagerAdapter.addFragment(ForumContentFragment.newInstance(tabCids.get(i)));
//        }
//
//        forumViewPager.setAdapter(mainViewPagerAdapter);
//
//        forumViewPager.setOffscreenPageLimit(tabTexts.size()-1);
//        forumViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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
//        forumViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(forumTabLayout){
//
//            @Override
//            public void onPageScrollStateChanged(int state) {
//                toggleRefreshing(state == ViewPager.SCROLL_STATE_IDLE);
//            }
//
//            private void toggleRefreshing(boolean enabled) {
////                if (swipeRefreshLayout != null) {
////                    swipeRefreshLayout.setEnabled(enabled);
////                }
//            }
//        });
//
//        forumTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
//            @Override
//            public void onTabSelected(TabLayout.Tab tab) {
//                forumViewPager.setCurrentItem(tab.getPosition());
//
//                int index = tab.getPosition();
//
//                forumTabLayout.setTabTextColors(getResources().getColor(R.color.colorWhite), getResources().getColor(R.color.colorGold));
//
//                for (int i=0; i<tabTexts.size(); i++){
//
//                    if (index == i) {
//
//                        forumTabLayout.getTabAt(tab.getPosition()).setIcon(selectedDraw.get(i));
//                    }
//                }
//
//            }
//
//            @Override
//            public void onTabUnselected(TabLayout.Tab tab) {
//
//                int index = tab.getPosition();
//
//                forumTabLayout.setTabTextColors(getResources().getColor(R.color.colorWhite), getResources().getColor(R.color.colorGold));
//
//                for (int i=0; i<tabTexts.size(); i++){
//
//                    if (index == i) {
//
//                        forumTabLayout.getTabAt(tab.getPosition()).setIcon(unselectedDraw.get(i));
//                    }
//                }
//            }
//
//            @Override
//            public void onTabReselected(TabLayout.Tab tab) {
//
//            }
//
//        });
//
//        if (flids.size() > 0){
//            //默认发送第一次广播
//            broadcastIntent = new Intent("com.example.mybroadcast.MY_BROADCAST");
//            broadcastIntent.putExtra("Flid", flids.get(0));
//            localBroadcastManager.sendBroadcast(broadcastIntent);
//
//            if (mainViewPagerAdapter != null){
//
//                mainViewPagerAdapter.notifyDataSetChanged();
//            }
//        }
//    }

}
