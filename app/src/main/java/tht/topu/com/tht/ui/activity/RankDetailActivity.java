package tht.topu.com.tht.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.othershe.baseadapter.interfaces.OnLoadMoreListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.CropCircleTransformation;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import tht.topu.com.tht.R;
import tht.topu.com.tht.adapter.RankDetailRecyclerViewAdapter;
import tht.topu.com.tht.adapter.RankRecyclerViewAdapter;
import tht.topu.com.tht.modle.Product;
import tht.topu.com.tht.ui.base.BaseActivity;
import tht.topu.com.tht.ui.fragment.rankFragment.RankContentFragment;
import tht.topu.com.tht.utils.API;
import tht.topu.com.tht.utils.DividerLine;
import tht.topu.com.tht.utils.Utilities;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class RankDetailActivity extends BaseActivity {

    private LinearLayout loadingLayout;
    private ImageView personal_background_ImageView;
    private ImageView personal_head_img;
    private ImageView forumBack;
    private LinearLayout closeSunLayout;
    private TextView personal_nickname;
    private TextView personal_vip_num;
    private TextView personal_integral_val;
    private RecyclerView rankDetailRecyclerView;
    private TextView blankTextView;
    //右侧文字vip
    private TextView personal_vip;
    //右侧部分
    private RelativeLayout personal_rigint_relative;
    //右侧圆形
    private  RelativeLayout personal_rigint_oval;
    //右侧图片
    private ImageView personal_rigint_Icon;

    private LinearLayout personal_integral_relative;
    private TextView personal_integral_title;

    public static final MediaType JSON = MediaType
            .parse("application/json; charset=utf-8");

    private String mid;
    private RankDetailRecyclerViewAdapter rankDetailRecyclerViewAdapter;

    private List<String> oids = new ArrayList<>();
    private List<Product> products = new ArrayList<>();

    private Handler uiHandler;

    private String random32;
    private String time10;
    private String key64;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(bindLayout());

        Intent intent = getIntent();
        mid = intent.getStringExtra("midData");

        uiHandler = new Handler(getMainLooper());
        initView();

        if (Utilities.isNetworkAvaliable(RankDetailActivity.this)){

            getUserInfo();
        }else {

            Toast.makeText(RankDetailActivity.this, "没有网络链接，请稍后再试", Toast.LENGTH_SHORT).show();
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(RankDetailActivity.this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        rankDetailRecyclerViewAdapter = new RankDetailRecyclerViewAdapter(RankDetailActivity.this, products, false);

        DividerLine dividerLine = new DividerLine();
        dividerLine.setColor(getResources().getColor(R.color.colorBorder));
        dividerLine.setSize(2);

        rankDetailRecyclerViewAdapter.setLoadEndView(R.layout.layout_load_end);

        rankDetailRecyclerView.setLayoutManager(linearLayoutManager);
        rankDetailRecyclerView.addItemDecoration(dividerLine);
        rankDetailRecyclerView.setAdapter(rankDetailRecyclerViewAdapter);

        forumBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                RankDetailActivity.this.finish();
            }
        });
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_rank_detail;
    }

    @Override
    public void initView() {

        personal_integral_title = (TextView)findViewById(R.id.personal_integral_title);
        personal_integral_relative = (LinearLayout)findViewById(R.id.personal_integral_relative);
        personal_background_ImageView = (ImageView)findViewById(R.id.personal_background_ImageView);
        rankDetailRecyclerView = (RecyclerView)findViewById(R.id.rankDetailRecyclerView);
        closeSunLayout = (LinearLayout)findViewById(R.id.closeSunLayout);
        personal_head_img = (ImageView)findViewById(R.id.personal_head_img);
        personal_nickname = (TextView)findViewById(R.id.personal_nickname);
        personal_vip_num = (TextView)findViewById(R.id.personal_vip_num);
        personal_integral_val = (TextView)findViewById(R.id.personal_integral_val);
        blankTextView = (TextView)findViewById(R.id.blankTextView);
        loadingLayout = (LinearLayout)findViewById(R.id.loadingLayout);
        //右半部分
        personal_rigint_relative=(RelativeLayout)findViewById(R.id.personal_right_relative);
        //右侧圆形
        personal_rigint_oval=(RelativeLayout)findViewById(R.id.personal_rigint_oval);
        //右侧图片
        personal_rigint_Icon=(ImageView)findViewById(R.id.personal_rigint_Icon);
        //右侧文字vip
        personal_vip=(TextView)findViewById(R.id.personal_vip);
        forumBack = (ImageView)findViewById(R.id.forumPostBack);
    }

    //获取用户信息
    private void getUserInfo(){

        random32 = Utilities.getStringRandom(32);
        time10 = Utilities.get10Time();
        key64 = Utilities.get64Key(random32);

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

                if (uiHandler!= null){

                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {

                            Utilities.popUpAlert(RankDetailActivity.this, "出现某些未知错误，请稍后再试");
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.body() != null){

                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        //结果详情
                        final JSONObject detailObj = jsonObject.getJSONArray("result").getJSONObject(0).getJSONObject("detail");
                        final boolean isSun = detailObj.getBoolean("The_sun");
                        final String mName = detailObj.getString("Mname");
                        final int integral = detailObj.getInt("Integral");
                        final int memberLevel = detailObj.getInt("Members_LV");
                        final String headImg = detailObj.getString("Head_img");
                        final int grade = detailObj.getJSONObject("card").getInt("Grade");

                        if (uiHandler != null){

                            uiHandler.post(new Runnable() {
                                @Override
                                public void run() {

                                    loadingLayout.setVisibility(View.GONE);
                                    if (grade == 1){

                                        setSilver();
                                    }else if (grade == 3){

                                        setBlack();
                                    }else {

                                        Glide.with(RankDetailActivity.this).load(R.drawable.personal_bj_golden).into(personal_background_ImageView);
                                    }

                                    Glide.with(RankDetailActivity.this).load(API.getAnothereHostName()+headImg).into(personal_head_img);
                                    personal_nickname.setText(mName);
                                    personal_vip_num.setText(""+memberLevel);
                                    personal_integral_val.setText(""+integral);
                                    if (isSun){

                                        closeSunLayout.setVisibility(View.GONE);
                                        rankDetailRecyclerView.setVisibility(View.VISIBLE);

                                        getOid();
                                    }else {

                                        closeSunLayout.setVisibility(View.VISIBLE);
                                    }
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

    //设置为银色背景
    private void setSilver(){

        Glide.with(RankDetailActivity.this).load(R.drawable.personal_bj_silvery).into(personal_background_ImageView);

        //右侧背景颜色
        personal_rigint_relative.setBackgroundResource(R.drawable.personal_right_border_silvery);
        //积分relative
        personal_integral_relative.setBackgroundResource(R.drawable.personal_integral_border_silvery);
        //积分标题
        personal_integral_title.setTextColor(ContextCompat.getColor(RankDetailActivity.this,R.color.personal_integral_title_silvery));
        //积分值
        personal_integral_val.setTextColor(ContextCompat.getColor(RankDetailActivity.this,R.color.personal_integral_val_silvery));
        //右侧圆形
        personal_rigint_oval.setBackgroundResource(R.drawable.personal_rigint_oval_silvery);
        //右侧图片
        personal_rigint_Icon.setImageResource(R.mipmap.personal_logo_silvery);
        //右侧vip
        personal_vip.setTextColor(ContextCompat.getColor(RankDetailActivity.this,R.color.personal_vip_silvery));
        //右侧vip数量
        personal_vip_num.setTextColor(ContextCompat.getColor(RankDetailActivity.this,R.color.personal_vip_num_silvery));
    }

    //设置为黑色背景
    private void setBlack(){

        Glide.with(RankDetailActivity.this).load(R.drawable.personal_bj_back).into(personal_background_ImageView);

        //右侧背景颜色
        personal_rigint_relative.setBackgroundResource(R.drawable.personal_right_border_balck);
        //积分relative
        personal_integral_relative.setBackgroundResource(R.drawable.personal_integral_border_balck);
        //积分标题
        personal_integral_title.setTextColor(ContextCompat.getColor(RankDetailActivity.this,R.color.personal_integral_title_black));
        //积分值
        personal_integral_val.setTextColor(ContextCompat.getColor(RankDetailActivity.this,R.color.personal_integral_val_black));
        //右侧圆形
        personal_rigint_oval.setBackgroundResource(R.drawable.personal_rigint_oval_balck);
        //右侧图片
        personal_rigint_Icon.setImageResource(R.mipmap.personal_logo_black);
        //右侧vip
        personal_vip.setTextColor(ContextCompat.getColor(RankDetailActivity.this,R.color.personal_vip_black));
        //右侧vip数量
        personal_vip_num.setTextColor(ContextCompat.getColor(RankDetailActivity.this,R.color.personal_vip_num_black));
    }

    //获取列表
    private void getOid(){

        random32 = Utilities.getStringRandom(32);
        time10 = Utilities.get10Time();
        key64 = Utilities.get64Key(random32);

        String json = "{\n" +
                "    \"validate_k\": \"1\",\n" +
                "    \"params\": [\n" +
                "        {\n" +
                "            \"type\": \"Orders\",\n" +
                "            \"act\": \"Select_List\",\n" +
                "            \"para\": {\n" +
                "                \"params\": {\n" +
                "                    \"s_d1\": \"\",\n" +
                "                    \"s_d2\": \"\",\n" +
                "                    \"s_d3\": \"\",\n" +
                "                    \"s_d4\": \"\",\n" +
                "                    \"s_d5\": \"\",\n" +
                "                    \"s_d6\": \"\",\n" +
                "                    \"s_Keywords\": \"\",\n" +
                "                    \"s_Maid\": \"\",\n" +
                "                    \"s_Mcid\": \"\",\n" +
                "                    \"s_Mid\": \""+mid+"\",\n" +
                "                    \"s_Oid\": \"\",\n" +
                "                    \"s_Order\": \"\",\n" +
                "                    \"s_Ostatus\": \"6\",\n" +
                "                    \"s_Stem_from\": \"2\",\n" +
                "                    \"s_Total_parameter\": \"Oid,Oserial,Mid,OpenID,Maid,Mname,addr,Zip_code,Mobile,Odate,Ddate,Pdate,Ostatus,Money,SF_single,Mcid,Ctitle,Discount_Price,Members_Prcie,Price_adjust,Serial_pay,Remarks,B_Remarks,M_Remarks,Stem_from,Members,Price_final,Total\"\n" +
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
                "                    \"signature\": \""+Utilities.encode("s_d1="+"s_d2="+"s_d3="+"s_d4="+"s_d5="+"s_d6="+"s_Keywords="+"s_Maid="+"s_Mcid="+"s_Mid="+mid+"s_Oid="+"s_Order="+"s_Ostatus=6"+"s_Stem_from=2"+"s_Total_parameter=Oid,Oserial,Mid,OpenID,Maid,Mname,addr,Zip_code,Mobile,Odate,Ddate,Pdate,Ostatus,Money,SF_single,Mcid,Ctitle,Discount_Price,Members_Prcie,Price_adjust,Serial_pay,Remarks,B_Remarks,M_Remarks,Stem_from,Members,Price_final,Total"+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
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

                            Toast.makeText(RankDetailActivity.this, "出了点差错，请稍后重试", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.body() != null){

                    try {

                        JSONObject jsonObj = new JSONObject(response.body().string());
                        JSONArray listArray = jsonObj.getJSONArray("result").getJSONObject(0).getJSONArray("list");

                        if (listArray.length() != 0){

                            for (int i=0; i<listArray.length(); i++){

                                String oid = listArray.getJSONObject(i).getString("Oid");
                                oids.add(oid);
                            }

                            getList();

                        }else {

                            uiHandler.post(new Runnable() {
                                @Override
                                public void run() {

                                    rankDetailRecyclerView.setVisibility(View.GONE);
                                    blankTextView.setVisibility(View.VISIBLE);
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

    private void getList(){

        random32 = Utilities.getStringRandom(32);
        time10 = Utilities.get10Time();
        key64 = Utilities.get64Key(random32);

        String result = "";

        if (oids != null && oids.size() > 0) {
            for (String item : oids) {
                result += item + ",";
            }
        }

        Log.d("rankdetailresult", result);

        String json = "{\n" +
                "    \"validate_k\": \"1\",\n" +
                "    \"params\": [\n" +
                "        {\n" +
                "            \"type\": \"Orders_Mdse\",\n" +
                "            \"act\": \"Select_List\",\n" +
                "            \"para\": {\n" +
                "                \"params\": {\n" +
                "                    \"s_Cid\": \"\",\n" +
                "                    \"s_d1\": \"\",\n" +
                "                    \"s_d2\": \"\",\n" +
                "                    \"s_Keywords\": \"\",\n" +
                "                    \"s_Kind\": \"\",\n" +
                "                    \"s_Mid\": \"\",\n" +
                "                    \"s_Oid\": \""+result+"\",\n" +
                "                    \"s_Omid\": \"\",\n" +
                "                    \"s_Order\": \"\",\n" +
                "                    \"s_Ostatus\": \"6\",\n" +
                "                    \"s_Total_parameter\": \"Omid,Oid,Order,Mid,Mdse,Kind,Bdate,Mtitle,Price,Amount,Cid,Ctitle\"\n" +
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
                "                    \"signature\": \""+Utilities.encode("s_Cid="+"s_d1="+"s_d2="+"s_Keywords="+"s_Kind="+"s_Mid="+"s_Oid="+result+"s_Omid="+"s_Order="+"s_Ostatus=6"+"s_Total_parameter="+"Omid,Oid,Order,Mid,Mdse,Kind,Bdate,Mtitle,Price,Amount,Cid,Ctitle"+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
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

                            Toast.makeText(RankDetailActivity.this, "除了些小差错，请稍后再试", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.body() != null){

                    try {
                        JSONObject jsonObj = new JSONObject(response.body().string());
                        Log.d("rankdetailjson", jsonObj.toString());

                        JSONArray jsonArr = jsonObj.getJSONArray("result").getJSONObject(0).getJSONArray("list");
                        Product.Builder builder = new Product.Builder();

                        if (jsonArr.length() != 0){

                            for (int i = 0; i < jsonArr.length(); i++){

                                JSONObject eachJson = jsonArr.getJSONObject(i);
                                Product product = builder.productPrice("¥"+eachJson.getString("Price")).productTitle(eachJson.getString("Ctitle")).image(eachJson.getJSONObject("Mdse").getString("Pic2")).build();

                                products.add(product);
                            }
                        }

                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {

                                rankDetailRecyclerViewAdapter.loadEnd();
                                rankDetailRecyclerViewAdapter.notifyDataSetChanged();
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
