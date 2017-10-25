package tht.topu.com.tht.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.Util;
import tht.topu.com.tht.R;
import tht.topu.com.tht.ui.base.BaseActivity;
import tht.topu.com.tht.utils.API;
import tht.topu.com.tht.utils.Utilities;

public class CardActivity extends BaseActivity {

    private LinearLayout card1; //第一张卡
    private LinearLayout card2; //第二张卡
    private LinearLayout card3; //第三张卡

    private TextView card1Price;
    private TextView card2Price;
    private TextView card3Price;
    public static final MediaType JSON = MediaType
            .parse("application/json; charset=utf-8");

    private LinearLayout loadingLayout;
    private RelativeLayout cardLayout;

    private AppCompatButton cardButton; //购买卡按钮

    private TextView cardAlertTextView;

    private boolean card1IsClicked = false;
    private boolean card2IsClicked = false;
    private boolean card3IsClicked = false;

    private String mid;
    private static final String MID_KEY = "1x11";

    private Handler uiHandler;

    private String random32;
    private String time10;
    private String key64;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(bindLayout());

        uiHandler = new Handler(Looper.getMainLooper());
        initView();

        //        判断网络是否可用
        if (!Utilities.isNetworkAvaliable(getApplicationContext())){

            Utilities.popUpAlert(this, "网络不可用");
        }
        getCardPrice();

        SharedPreferences sharedPreferences = getSharedPreferences("TokenData", MODE_PRIVATE);
        mid = sharedPreferences.getString(MID_KEY, "");

//        卡片1初始化点击
        card1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                cardAnimation(view);

                card1IsClicked = true;
                card2IsClicked = false;
                card3IsClicked = false;

                cardAlertTextView.setText("");

                judgeCard();
            }
        });

        card2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                cardAnimation(view);

                card1IsClicked = false;
                card2IsClicked = true;
                card3IsClicked = false;

                cardAlertTextView.setText("");

                judgeCard();
            }
        });

        card3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                cardAnimation(view);

                card1IsClicked = false;
                card2IsClicked = false;
                card3IsClicked = true;

                cardAlertTextView.setText("");

                judgeCard();
            }
        });

        //点击按钮跳转
        cardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                float card1X = card1.getTranslationX();
                float card2X = card2.getTranslationX();
                float card3X = card3.getTranslationX();

                if (card1X == 200 || card2X == 200 || card3X == 200){

                    if (card1X == 200){
                        addCard("1", mid);
                    }else if (card2X == 200){
                        addCard("2", mid);
                    }else{
                        addCard("3", mid );
                    }
                }else {
                    cardAlertTextView.setText("请至少选择一种卡片");

                    TranslateAnimation animation = new TranslateAnimation(0, -10, 0, 0);
                    animation.setInterpolator(new OvershootInterpolator());
                    animation.setDuration(20);
                    animation.setRepeatCount(3);
                    animation.setRepeatMode(Animation.REVERSE);
                    cardAlertTextView.startAnimation(animation);
                }
            }
        });
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_card;
    }

    @Override
    public void initView() {

        card1 = (LinearLayout)findViewById(R.id.card1);
        card2 = (LinearLayout)findViewById(R.id.card2);
        card3 = (LinearLayout)findViewById(R.id.card3);

        card1Price = (TextView)findViewById(R.id.card1TextView);
        card2Price = (TextView)findViewById(R.id.card2TextView);
        card3Price = (TextView)findViewById(R.id.card3TextView);

        loadingLayout = (LinearLayout)findViewById(R.id.loadingLayout);
        cardLayout = (RelativeLayout)findViewById(R.id.cardLayout);

//        初始化卡为为未选中状态
        card1.setTranslationX(400);
        card2.setTranslationX(400);
        card3.setTranslationX(400);

        cardButton = (AppCompatButton)findViewById(R.id.cardButton);
        cardAlertTextView = (TextView)findViewById(R.id.cardAlertTextView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (uiHandler != null){

            uiHandler = null;
        }
    }

    //    卡片动画
    private void cardAnimation(View view){

        float viewX = view.getTranslationX();

        if (viewX == 400.0){

            view.animate()
                    .translationX(200)
                    .setDuration(300);


        }else if(viewX == 200.0){

            view.animate()
                    .translationX(400)
                    .setDuration(300);
        }
    }

//    判断卡券状态
    private void judgeCard(){
        if (card1IsClicked){

            card2.animate().translationX(400).setDuration(300);
            card3.animate().translationX(400).setDuration(300);

        }else if(card2IsClicked){

            card1.animate().translationX(400).setDuration(300);
            card3.animate().translationX(400).setDuration(300);

        }else if(card3IsClicked){

            card1.animate().translationX(400).setDuration(300);
            card2.animate().translationX(400).setDuration(300);

        }
    }

    //获取卡券价格
    private void getCardPrice(){

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
                "                    \"Iid\": \"5\"\n" +
                "                },\n" +
                "                \"sign_valid\": {\n" +
                "                    \"source\": \"Android\",\n" +
                "                    \"non_str\": \""+random32+"\",\n" +
                "                    \"stamp\": \""+time10+"\",\n" +
                "                    \"signature\": \""+Utilities.encode("Iid=5"+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
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

                        Utilities.popUpAlert(CardActivity.this, "出现某些未知错误，请稍后再试");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.body() != null){

                    try {
                        JSONObject resultJson = new JSONObject(response.body().string());
                        String priceStr = resultJson.getJSONArray("result").getJSONObject(0).getJSONObject("Iinfo").getString("Iinfo");

                        final String[] priceArr = priceStr.split("\\|");

                        if (uiHandler != null){

                            uiHandler.post(new Runnable() {
                                @Override
                                public void run() {

                                    loadingLayout.setVisibility(View.GONE);
                                    cardLayout.setVisibility(View.VISIBLE);
                                    card1Price.setText(priceArr[0]+" / 月");
                                    card2Price.setText(priceArr[1]+" / 月");
                                    card3Price.setText(priceArr[2]+" / 月");
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

    //添加卡券
    private void addCard(String card, String mid){

        random32 = Utilities.getStringRandom(32);
        time10 = Utilities.get10Time();
        key64 = Utilities.get64Key(random32);

        final String json = "{\n" +
                "    \"validate_k\": \"1\",\n" +
                "    \"params\": [\n" +
                "        {\n" +
                "            \"type\": \"Membership_card\",\n" +
                "            \"act\": \"Add\",\n" +
                "            \"para\": {\n" +
                "                \"params\": {\n" +
                "                    \"Grade\": \""+card+"\",\n" +
                "                    \"Mid\": \""+mid+"\"\n" +
                "                },\n" +
                "                \"sign_valid\": {\n" +
                "                    \"source\": \"Android\",\n" +
                "                    \"non_str\": \""+random32+"\",\n" +
                "                    \"stamp\": \""+time10+"\",\n" +
                "                    \"signature\": \""+Utilities.encode("Grade="+card+"Mid="+mid+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
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

                        String mcid = jsonObject.getJSONArray("result").getJSONObject(0).getString("Mcid");

                        Log.d("card", jsonObject.toString());

                        cardSerial(mcid);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void cardSerial(String mcid){

        random32 = Utilities.getStringRandom(32);
        time10 = Utilities.get10Time();
        key64 = Utilities.get64Key(random32);

        String json = "{\n" +
                "    \"validate_k\": \"1\",\n" +
                "    \"params\": [\n" +
                "        {\n" +
                "            \"type\": \"Membership_card\",\n" +
                "            \"act\": \"Serial_pay\",\n" +
                "            \"para\": {\n" +
                "                \"params\": {\n" +
                "                    \"d_Mcid\": \""+mcid+"\",\n" +
                "                    \"Serial_pay\": \"瞎几把编的\"\n" +
                "                },\n" +
                "                \"sign_valid\": {\n" +
                "                    \"source\": \"Android\",\n" +
                "                    \"non_str\": \""+random32+"\",\n" +
                "                    \"stamp\": \""+time10+"\",\n" +
                "                    \"signature\": \""+Utilities.encode("d_Mcid="+mcid+"Serial_pay=瞎几把编的"+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
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

                        Log.d("cardSerial", jsonObject.toString());

                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {

                                //清空activity调用盏
                                Intent intent = new Intent(CardActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
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
