package tht.topu.com.tht.ui.fragment.mainFragment;


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
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.othershe.baseadapter.interfaces.OnLoadMoreListener;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.listener.OnBannerListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.lemon.view.RefreshRecyclerView;
import cn.lemon.view.adapter.Action;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import tht.topu.com.tht.R;
import tht.topu.com.tht.adapter.ProductRecyclerViewAdapter;
import tht.topu.com.tht.modle.Product;
import tht.topu.com.tht.ui.activity.WebViewActivity;
import tht.topu.com.tht.ui.fragment.forumFragment.ForumContentFragment;
import tht.topu.com.tht.utils.API;
import tht.topu.com.tht.utils.GlideImageLoader;
import tht.topu.com.tht.utils.Utilities;

public class ProductFragment extends Fragment {

    private RecyclerView productRecyclerView;
    private ProductRecyclerViewAdapter productRecyclerViewAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    private List<String> bannerImages = new ArrayList<>();
    private List<String> bannerUrls = new ArrayList<>();


    private Banner banner;

    private static final String CID_ID = "0x31";
    private static final String RECOMMEND_ID = "1x31";
    private List<Product> products = new ArrayList<>();
    private List<Product> loadMoreProducts = new ArrayList<>();

    private View rootView;

    private Handler uiHandler;

    private String random32;
    private String time10;
    private String key64;

    private String cid;
    private String recommend;

    int currentPage;
    int totalPage;

    private LocalBroadcastManager localBroadcastManager;
    private IntentFilter intentFilter;
    private LocalReceiver localReceiver;

    public static final MediaType JSON = MediaType
            .parse("application/json; charset=utf-8");

//    public static ProductFragment newInstance(String cid, String recommend) {
//
//        Bundle argus = new Bundle();
//        argus.putString(CID_ID, cid);
//        argus.putString(RECOMMEND_ID, recommend);
//        ProductFragment productFragment = new ProductFragment();
//        productFragment.setArguments(argus);
//
//        return productFragment;
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

//            cid = getArguments().getString(CID_ID);
//            recommend = getArguments().getString(RECOMMEND_ID);
//            cid = getArguments().getString(CID_ID);
        intentFilter = new IntentFilter();
        intentFilter.addAction("com.product.mybroadcast.MY_BROADCAST");
        localReceiver = new LocalReceiver();
        localBroadcastManager = LocalBroadcastManager.getInstance(getActivity().getApplicationContext());
        localBroadcastManager.registerReceiver(localReceiver, intentFilter);
        rootView=inflater.inflate(R.layout.fragment_product, container, false);

        initView(rootView);// 控件初始化

        uiHandler = new Handler(Looper.getMainLooper());

        productRecyclerViewAdapter = new ProductRecyclerViewAdapter(getActivity().getApplicationContext(), null, true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity().getApplicationContext(), 2);

        productRecyclerViewAdapter.notifyDataSetChanged();
        //初始化 开始加载更多的loading View
        productRecyclerViewAdapter.setLoadingView(R.layout.progress_item);
        //加载完成，更新footer view提示
        productRecyclerViewAdapter.setLoadEndView(R.layout.layout_load_end);

        // 加载更多
        productRecyclerViewAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(boolean isReload) {

                currentPage++;
                initData(false, cid, recommend);
            }
        });

        productRecyclerView.setLayoutManager(gridLayoutManager);
        productRecyclerView.setNestedScrollingEnabled(false);
        productRecyclerView.setAdapter(productRecyclerViewAdapter);

//            initData(true, cid, recommend);

        bannerImages.clear();

        getBannerImages();

        return rootView;
    }

    private void initView(View v){

        productRecyclerView = v.findViewById(R.id.productRecyclerView);
        swipeRefreshLayout = v.findViewById(R.id.swipe_refresh_layout);
        banner = v.findViewById(R.id.mainBanner);

        swipeRefreshLayout.setEnabled(false);
    }

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

//                            alertHandler.sendEmptyMessageDelayed(1,1000);

//                            if (swipeRefreshLayout.isRefreshing()){
//
//                                swipeRefreshLayout.setRefreshing(false);
//                            }
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

    private void initData(final boolean isFirstLoad, String Cid, String recommend){

        random32 = Utilities.getStringRandom(32);
        time10 = Utilities.get10Time();
        key64 = Utilities.get64Key(random32);

//        localBroadcastManager.unregisterReceiver(localReceiver);

        if (isFirstLoad){

            products.clear();
            currentPage = 1;
        }else {

            loadMoreProducts.clear();
            Log.d("loadmore", "loadmore");
        }

        final String json = "{\n" +
                "    \"validate_k\": \"1\",\n" +
                "    \"params\": [\n" +
                "        {\n" +
                "            \"type\": \"Mdse\",\n" +
                "            \"act\": \"Select_List\",\n" +
                "            \"para\": {\n" +
                "                \"params\": {\n" +
                "                    \"s_Alive\": \"\",\n" +
                "                    \"s_Cid\": \""+Cid+"\",\n" +
                "                    \"s_d1\": \"\",\n" +
                "                    \"s_d2\": \"\",\n" +
                "                    \"s_d3\": \"\",\n" +
                "                    \"s_d4\": \"\",\n" +
                "                    \"s_d5\": \"\",\n" +
                "                    \"s_d6\": \"\",\n" +
                "                    \"s_d7\": \"\",\n" +
                "                    \"s_d8\": \"\",\n" +
                "                    \"s_Flash_sale\": \"\",\n" +
                "                    \"s_Fs_Stock_final\": \"\",\n" +
                "                    \"s_Hot\": \"\",\n" +
                "                    \"s_Keywords\": \"\",\n" +
                "                    \"s_Mid\": \"\",\n" +
                "                    \"s_Order\": \"\",\n" +
                "                    \"s_Recommend\": \""+recommend+"\",\n" +
                "                    \"s_So_Stock_final\": \"\",\n" +
                "                    \"s_Special_offer\": \"\",\n" +
                "                    \"s_Stem_from\": \"2\",\n" +
                "                    \"s_Stock_final\": \"\",\n" +
                "                    \"s_Total_parameter\": \"Mid,Cid,Ctitle,Stitle,Pic1,Pic2,Description,Amount,Market_Price,Price,Special_offer,Sotitle,So_Price,So_Sdate,So_Bdate,So_Amount,Flash_sale,Fstitle,Fs_Price,Fs_Sdate,Fs_Bdate,Fs_Amount,Recommend,Hot,Layer,Alive,Stem_from,Sales_volume,Stock_final,So_Stock_final,Fs_Stock_final\"\n" +
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
                "                    \"p_Ps\": \"4\",\n" +
                "                    \"p_sk\": \"\",\n" +
                "                    \"p_Tp\": \"\"\n" +
                "                },\n" +
                "                \"sign_valid\": {\n" +
                "                    \"source\": \"Android\",\n" +
                "                    \"non_str\": \""+random32+"\",\n" +
                "                    \"stamp\": \""+time10+"\",\n" +
                "                    \"signature\": \""+Utilities.encode("s_Alive="+"s_Cid="+Cid+"s_d1="+"s_d2="+"s_d3="+"s_d4="+"s_d5="+"s_d6="+"s_d7="+"s_d8="+"s_Flash_sale="+"s_Fs_Stock_final="+"s_Hot="+"s_Keywords="+"s_Mid="+"s_Order="+"s_Recommend="+recommend+"s_So_Stock_final="+"s_Special_offer="+"s_Stem_from=2"+"s_Stock_final="+"s_Total_parameter="+"Mid,Cid,Ctitle,Stitle,Pic1,Pic2,Description,Amount,Market_Price,Price,Special_offer,Sotitle,So_Price,So_Sdate,So_Bdate,So_Amount,Flash_sale,Fstitle,Fs_Price,Fs_Sdate,Fs_Bdate,Fs_Amount,Recommend,Hot,Layer,Alive,Stem_from,Sales_volume,Stock_final,So_Stock_final,Fs_Stock_final"+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
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

                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    final JSONArray productArr = jsonObject.getJSONArray("result").getJSONObject(0).getJSONArray("list");

                    totalPage = jsonObject.getJSONArray("result").getJSONObject(0).getJSONObject("page").getInt("Pc");
                    Product.Builder builder = new Product.Builder();

                    Log.d("result", jsonObject.toString());

                    for (int i=0; i<productArr.length(); i++){

                        JSONObject eachProPrice = productArr.getJSONObject(i);

                        Product product = builder
                                .ogProductPrice(eachProPrice.getString("Market_Price"))
                                .productPrice(eachProPrice.getString("Price"))
                                .productTitle(eachProPrice.getString("Stitle"))
                                .isHot(eachProPrice.getBoolean("Hot"))
                                .isRecommend(eachProPrice.getBoolean("Recommend"))
                                .image(API.getHostName()+eachProPrice.getString("Pic2"))
                                .Mcid(eachProPrice.getString("Mid"))
                                .build();

                        if (isFirstLoad){

                            products.add(product);
                        }else {

                            loadMoreProducts.add(product);
                        }
                    }

                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {

                            swipeRefreshLayout.setRefreshing(false);

                            if (!isFirstLoad){

                                productRecyclerViewAdapter.setLoadMoreData(loadMoreProducts);
                            }else{
                                productRecyclerViewAdapter.reset();
                                productRecyclerViewAdapter.setNewData(products);
                            }

                            if (products.size() == 0){

                                productRecyclerViewAdapter.loadEnd();
                            }else if (currentPage >= totalPage){


                                productRecyclerViewAdapter.loadEnd();
                            }
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public class LocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            recommend = intent.getStringExtra("Recommend");
            cid = intent.getStringExtra("Cid");
//            forumList.clear();
            initData(true, cid, recommend);
            Log.d("接受广播——product", "接受广播");

            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(true);
                }
            });
            productRecyclerViewAdapter.notifyDataSetChanged();
        }
    }
}
