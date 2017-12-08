package tht.topu.com.tht.ui.activity;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import tht.topu.com.tht.adapter.ProductRecyclerViewAdapter;
import tht.topu.com.tht.modle.Product;
import tht.topu.com.tht.utils.API;
import tht.topu.com.tht.utils.Utilities;

public class SearchActivity extends AppCompatActivity {

    private EditText searchEditText;
    private TextView searchBtn;
    private RecyclerView searchRecyclerView;
    private RelativeLayout loadingLayout;

    private String random32;
    private String time10;
    private String key64;

    private Handler uiHandler;

    int currentPage;
    int totalPage;

    String searchString;

    private List<Product> products = new ArrayList<>();
    private List<Product> loadMoreProducts = new ArrayList<>();

    private ProductRecyclerViewAdapter productRecyclerViewAdapter;

    public static final MediaType JSON = MediaType
            .parse("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        uiHandler = new Handler(getMainLooper());
        initView();

        productRecyclerViewAdapter = new ProductRecyclerViewAdapter(this, null, true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);

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
                searchProduct(searchString, false);
            }
        });

        searchRecyclerView.setLayoutManager(gridLayoutManager);
        searchRecyclerView.setNestedScrollingEnabled(false);
        searchRecyclerView.setAdapter(productRecyclerViewAdapter);

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                products.clear();
                productRecyclerViewAdapter.notifyDataSetChanged();

                searchString = searchEditText.getText().toString();

                loadingLayout.setVisibility(View.VISIBLE);
                searchRecyclerView.setVisibility(View.GONE);

                searchProduct(searchString, true);
            }
        });
    }

    private void initView() {
        searchEditText = (EditText)findViewById(R.id.searchEditText);
        searchBtn = (TextView)findViewById(R.id.searchBtn);
        searchRecyclerView = (RecyclerView)findViewById(R.id.searchRecyclerView);
        loadingLayout = (RelativeLayout) findViewById(R.id.loadingLayout);
    }

    // 搜索宝贝
    private void searchProduct(String proName, final boolean isFirstLoad) {

        random32 = Utilities.getStringRandom(32);
        time10 = Utilities.get10Time();
        key64 = Utilities.get64Key(random32);

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
                "                    \"s_Cid\": \"\",\n" +
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
                "                    \"s_Keywords\": \""+proName+"\",\n" +
                "                    \"s_Mid\": \"\",\n" +
                "                    \"s_Order\": \"\",\n" +
                "                    \"s_Recommend\": \"\",\n" +
                "                    \"s_So_Stock_final\": \"\",\n" +
                "                    \"s_Special_offer\": \"\",\n" +
                "                    \"s_Stem_from\": \"2\",\n" +
                "                    \"s_Stock_final\": \"\",\n" +
                "                    \"s_Total_parameter\": \"Mid,Cid,Ctitle,Stitle,Pic1,Pic2,Description,Amount,Market_Price,Price,Special_offer,Sotitle,So_Price,So_Sdate,So_Bdate,So_Amount,Flash_sale,Fstitle,Fs_Price,Fs_Sdate,Fs_Bdate,Fs_Amount,Recommend,Hot,Layer,Alive,Stem_from,Sales_volume,Stock_final,Normal_margin,So_Stock_final,Fs_Stock_final\"\n" +
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
                "                    \"signature\": \""+Utilities.encode("s_Alive="+"s_Cid="+"s_d1="+"s_d2="+"s_d3="+"s_d4="+"s_d5="+"s_d6="+"s_d7="+"s_d8="+"s_Flash_sale="+"s_Fs_Stock_final="+"s_Hot="+"s_Keywords="+proName+"s_Mid="+"s_Order="+"s_Recommend="+"s_So_Stock_final="+"s_Special_offer="+"s_Stem_from=2"+"s_Stock_final="+"s_Total_parameter="+"Mid,Cid,Ctitle,Stitle,Pic1,Pic2,Description,Amount,Market_Price,Price,Special_offer,Sotitle,So_Price,So_Sdate,So_Bdate,So_Amount,Flash_sale,Fstitle,Fs_Price,Fs_Sdate,Fs_Bdate,Fs_Amount,Recommend,Hot,Layer,Alive,Stem_from,Sales_volume,Stock_final,Normal_margin,So_Stock_final,Fs_Stock_final"+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
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

                            loadingLayout.setVisibility(View.GONE);
                            searchRecyclerView.setVisibility(View.VISIBLE);

                            if (!isFirstLoad){

                                productRecyclerViewAdapter.setLoadMoreData(loadMoreProducts);
                            }else{
                                productRecyclerViewAdapter.reset();
                                productRecyclerViewAdapter.setNewData(products);
                            }
//                            productRecyclerViewAdapter.notifyDataSetChanged();

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
}
