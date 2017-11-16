package tht.topu.com.tht.ui.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.othershe.baseadapter.ViewHolder;
import com.othershe.baseadapter.base.CommonBaseAdapter;
import com.othershe.baseadapter.interfaces.OnLoadMoreListener;

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
import tht.topu.com.tht.modle.Product;
import tht.topu.com.tht.ui.base.BaseActivity;
import tht.topu.com.tht.utils.API;
import tht.topu.com.tht.utils.DividerLine;
import tht.topu.com.tht.utils.Utilities;

public class UserFavoriteActivity extends BaseActivity {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView userFavoriteRecyclerView;
    private UserFavoriteAdapter userFavoriteAdapter;
    private ImageView userFavoriteBack;

    public static final MediaType JSON = MediaType
            .parse("application/json; charset=utf-8");

    private static final String MID_KEY = "1x11";

    private List<Product> products = new ArrayList<>();
    private List<Product> loadMoreProducts = new ArrayList<>();

    private Handler uiHandler;

    private String random32;
    private String time10;
    private String key64;

    private int currentPage;
    private int totalPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(bindLayout());

        initView();

        uiHandler = new Handler(Looper.getMainLooper());

        userFavoriteBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                UserFavoriteActivity.this.finish();
            }
        });

        if (Utilities.isNetworkAvaliable(UserFavoriteActivity.this)){

            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(false);
                }
            });

            initData(true);

            //下拉刷新
            swipeRefreshLayout.setEnabled(false);

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(UserFavoriteActivity.this);
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

            userFavoriteAdapter = new UserFavoriteAdapter(UserFavoriteActivity.this, products, true);

            DividerLine dividerLine = new DividerLine();
            dividerLine.setColor(getResources().getColor(R.color.colorBorder));
            dividerLine.setSize(2);

            userFavoriteAdapter.notifyDataSetChanged();
            //初始化 开始加载更多的loading View
            userFavoriteAdapter.setLoadingView(R.layout.progress_item);
            //加载完成，更新footer view提示
            userFavoriteAdapter.setLoadEndView(R.layout.layout_load_end);

            // 加载更多
            userFavoriteAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
                @Override
                public void onLoadMore(boolean isReload) {

                    currentPage++;
                    initData(false);
                }
            });
            userFavoriteRecyclerView.setLayoutManager(linearLayoutManager);
            userFavoriteRecyclerView.addItemDecoration(dividerLine);
            userFavoriteRecyclerView.setAdapter(userFavoriteAdapter);
        }else {

            Toast.makeText(UserFavoriteActivity.this, "没有网络链接，请稍后再试", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_user_favorite;
    }

    @Override
    public void initView() {

        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh_layout);
        userFavoriteRecyclerView = (RecyclerView)findViewById(R.id.userFavoriteRecyclerView);
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh_layout);
        userFavoriteBack = (ImageView)findViewById(R.id.userFavoriteBack);
    }

    private void initData(final boolean isFirstLoad){

        random32 = Utilities.getStringRandom(32);
        time10 = Utilities.get10Time();
        key64 = Utilities.get64Key(random32);

        SharedPreferences sharedPreferences = getSharedPreferences("TokenData", MODE_PRIVATE);
        String mid = sharedPreferences.getString(MID_KEY, "");

        if (isFirstLoad){

            currentPage = 1;
            products.clear();
        }else {

            loadMoreProducts.clear();

        }
        String json = "{\n" +
                "    \"validate_k\": \"1\",\n" +
                "    \"params\": [\n" +
                "        {\n" +
                "            \"type\": \"Members_Collection\",\n" +
                "            \"act\": \"Select_List\",\n" +
                "            \"para\": {\n" +
                "                \"params\": {\n" +
                "                    \"s_d1\": \"\",\n" +
                "                    \"s_d2\": \"\",\n" +
                "                    \"s_Keywords\": \"\",\n" +
                "                    \"s_Mcid\": \"\",\n" +
                "                    \"s_Mdid\": \"\",\n" +
                "                    \"s_Mid\": \""+mid+"\",\n" +
                "                    \"s_Order\": \"Cdate desc\",\n" +
                "                    \"s_Total_parameter\": \"Mcid,Mid,Members,Mdid,Mdse,Cdate\"\n" +
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
                "                    \"signature\": \""+Utilities.encode("s_d1="+"s_d2="+"s_Keywords="+"s_Mcid="+"s_Mdid="+"s_Mid="+mid+"s_Order=Cdate desc"+"s_Total_parameter="+"Mcid,Mid,Members,Mdid,Mdse,Cdate"+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
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

                Toast.makeText(UserFavoriteActivity.this, "出现某些错误，请重试", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                try {

                    if (response.body() != null){

                        JSONObject jsonObject = new JSONObject(response.body().string());

                        JSONArray jsonArray = jsonObject.getJSONArray("result").getJSONObject(0).getJSONArray("list");
                        totalPage = jsonObject.getJSONArray("result").getJSONObject(0).getJSONObject("page").getInt("Pc");
                        Product.Builder builder = new Product.Builder();

                        for (int i = 0; i < jsonArray.length(); i++){

                            JSONObject eachJson = jsonArray.getJSONObject(i).getJSONObject("Mdse");
                            Product product = builder.productTitle(eachJson.getString("Stitle")).productPrice("¥"+eachJson.getString("Price")).image(API.getHostName()+eachJson.getString("Pic2")).Mcid(jsonArray.getJSONObject(i).getString("Mcid")).build();

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

                                    userFavoriteAdapter.setLoadMoreData(loadMoreProducts);
                                }
                                userFavoriteAdapter.notifyDataSetChanged();

                                if (products.size() == 0){

                                    userFavoriteAdapter.loadEnd();
                                }else if (currentPage >= totalPage){

                                    userFavoriteAdapter.loadEnd();
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

    private class UserFavoriteAdapter extends CommonBaseAdapter<Product> {

        private UserFavoriteAdapter(Context context, List<Product> datas, boolean isOpenLoadMore) {
            super(context, datas, isOpenLoadMore);
        }

        @Override
        protected void convert(ViewHolder holder, final Product data, final int position) {

            SharedPreferences sharedPreferences = getSharedPreferences("TokenData", MODE_PRIVATE);
            final String mid = sharedPreferences.getString(MID_KEY, "");

            ((TextView)holder.getView(R.id.userFavoriteTextView)).setText(data.getProductTitle());
            ((TextView)holder.getView(R.id.userFavoritePriceTextView)).setText(data.getProductPrice());
            Glide.with(UserFavoriteActivity.this).load(data.getImage()).into((ImageView)holder.getView(R.id.userFavoriteItemImageView));

            ((ImageView)holder.getView(R.id.userFavoriteItemImageView)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Bundle bundle = new Bundle();
                    bundle.putString("mdid", data.getMcid());
                    bundle.putString("mid", mid);

                    Utilities.jumpToActivity(mContext, ProductDetailActivity.class, bundle, "bundleData");
                }
            });
            (holder.getView(R.id.deleteFavoriteItem)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    deleteItem(position);
                }
            });
        }

        @Override
        protected int getItemLayoutId() {
            return R.layout.layout_user_favorite_item;
        }

        private void deleteItem(final int position){

            random32 = Utilities.getStringRandom(32);
            time10 = Utilities.get10Time();
            key64 = Utilities.get64Key(random32);

            String json = "{\n" +
                    "    \"validate_k\": \"1\",\n" +
                    "    \"params\": [\n" +
                    "        {\n" +
                    "            \"type\": \"Members_Collection\",\n" +
                    "            \"act\": \"Del\",\n" +
                    "            \"para\": {\n" +
                    "                \"params\": {\n" +
                    "                    \"Mcid\": \""+products.get(position).getMcid()+"\"\n" +
                    "                },\n" +
                    "                \"sign_valid\": {\n" +
                    "                    \"source\": \"Android\",\n" +
                    "                    \"non_str\": \""+random32+"\",\n" +
                    "                    \"stamp\": \""+time10+"\",\n" +
                    "                    \"signature\": \""+Utilities.encode("Mcid="+products.get(position).getMcid()+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
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

                    Toast.makeText(UserFavoriteActivity.this, "删除失败，请重试", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    if (response.body() != null){

                        try {
                            JSONObject jsonObj = new JSONObject(response.body().string());

                            Log.d("delete", jsonObj.toString());

                            uiHandler.post(new Runnable() {
                                @Override
                                public void run() {

                                    userFavoriteAdapter.notifyItemRemoved(position);
                                    userFavoriteAdapter.notifyDataSetChanged();
                                    products.remove(position);

                                    Toast.makeText(UserFavoriteActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
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
}
