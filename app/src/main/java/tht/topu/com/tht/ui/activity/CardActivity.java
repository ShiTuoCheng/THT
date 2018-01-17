package tht.topu.com.tht.ui.activity;

import android.app.ProgressDialog;
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
import android.widget.Toast;

import com.jpay.JPay;

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

    private String[] priceArr;

    private ProgressDialog progressDialog;

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
                        addCard("1", mid, priceArr[0]);
                    }else if (card2X == 200){
                        addCard("2", mid, priceArr[1]);
                    }else{
                        addCard("3", mid, priceArr[2]);
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

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
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
                "                    \"Iid\": \"14\"\n" +
                "                },\n" +
                "                \"sign_valid\": {\n" +
                "                    \"source\": \"Android\",\n" +
                "                    \"non_str\": \""+random32+"\",\n" +
                "                    \"stamp\": \""+time10+"\",\n" +
                "                    \"signature\": \""+Utilities.encode("Iid=14"+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
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

                        priceArr = priceStr.split("\\|");

                        if (uiHandler != null){

                            uiHandler.post(new Runnable() {
                                @Override
                                public void run() {

                                    loadingLayout.setVisibility(View.GONE);
                                    cardLayout.setVisibility(View.VISIBLE);
                                    card1Price.setText("¥"+priceArr[0]+" / 1个月");
                                    card2Price.setText("¥"+priceArr[1]+" / 6个月");
                                    card3Price.setText("¥"+priceArr[2]+" / 12个月");
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
    private void addCard(final String card, String mid, final String cardPrice){

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

                        final String mcid = jsonObject.getJSONArray("result").getJSONObject(0).getString("Mcid");

                        Log.d("card", jsonObject.toString());

                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {

                                progressDialog.show();
                                getPrePay(cardPrice, card, mcid);
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    //获取订单预支付id
    private void getPrePay(final String price, final String grade, final String mcid){

        random32 = Utilities.getStringRandom(32);
        time10 = Utilities.get10Time();
        key64 = Utilities.get64Key(random32);

        String json = "{\n" +
                "    \"validate_k\": \"1\",\n" +
                "    \"params\": [\n" +
                "        {\n" +
                "            \"type\": \"Membership_card\",\n" +
                "            \"act\": \"UnifiedOrder\",\n" +
                "            \"para\": {\n" +
                "                \"params\": {\n" +
                "                    \"Grade\": \""+grade+"\",\n" +
                "                    \"Mcid\": \""+mcid+"\",\n" +
                "                    \"Money\": \""+price+"\"\n" +
                "                },\n" +
                "                \"sign_valid\": {\n" +
                "                    \"source\": \"Android\",\n" +
                "                    \"non_str\": \""+random32+"\",\n" +
                "                    \"stamp\": \""+time10+"\",\n" +
                "                    \"signature\": \""+Utilities.encode("Grade="+grade+"Mcid="+mcid+"Money="+price+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
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

                        Toast.makeText(CardActivity.this, "支付失败，请重试", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.body() != null){

                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        final String prePayId = jsonObject.getJSONArray("result").getJSONObject(0).getString("prepay_id");

                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {

                                if (!prePayId.equals("")){

                                    launchWXPay(prePayId);

                                }else {

                                    Toast.makeText(CardActivity.this, "支付出错 请重试", Toast.LENGTH_SHORT).show();
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

    private void launchWXPay(final String prepayId){

        JPay.getIntance(this).toWxPay(API.APPID, API.PartnerID, prepayId, Utilities.getStringRandom(32), Utilities.get10Time(), "Sign=WXPay", new JPay.JPayListener() {
            @Override
            public void onPaySuccess() {
                Toast.makeText(CardActivity.this, "支付成功", Toast.LENGTH_SHORT).show();
                progressDialog.cancel();

                Intent intent = new Intent(CardActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }

            @Override
            public void onPayError(int error_code, String message) {
                Toast.makeText(CardActivity.this, "支付失败>"+error_code+" "+ message, Toast.LENGTH_SHORT).show();
                progressDialog.cancel();
            }

            @Override
            public void onPayCancel() {
                Toast.makeText(CardActivity.this, "取消了支付", Toast.LENGTH_SHORT).show();
                progressDialog.cancel();
            }

            @Override
            public void onUUPay(String s, String s1, String s2) {

            }
        });
    }

//    private void cardSerial(String mcid){
//
//        random32 = Utilities.getStringRandom(32);
//        time10 = Utilities.get10Time();
//        key64 = Utilities.get64Key(random32);
//
//        String json = "{\n" +
//                "    \"validate_k\": \"1\",\n" +
//                "    \"params\": [\n" +
//                "        {\n" +
//                "            \"type\": \"Membership_card\",\n" +
//                "            \"act\": \"Serial_pay\",\n" +
//                "            \"para\": {\n" +
//                "                \"params\": {\n" +
//                "                    \"d_Mcid\": \""+mcid+"\",\n" +
//                "                    \"Serial_pay\": \""+time10+"\"\n" +
//                "                },\n" +
//                "                \"sign_valid\": {\n" +
//                "                    \"source\": \"Android\",\n" +
//                "                    \"non_str\": \""+random32+"\",\n" +
//                "                    \"stamp\": \""+time10+"\",\n" +
//                "                    \"signature\": \""+Utilities.encode("d_Mcid="+mcid+"Serial_pay="+time10+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
//                "                }\n" +
//                "            }\n" +
//                "        }\n" +
//                "    ]\n" +
//                "}";
//
//        OkHttpClient okHttpClient = new OkHttpClient();
//
//        RequestBody requestBody = RequestBody.create(JSON, json);
//        Request request = new Request.Builder().url(API.getAPI()).post(requestBody).build();
//
//        okHttpClient.newCall(request).enqueue(new Callback() {
//
//            @Override
//            public void onFailure(Call call, IOException e) {
//
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//
//                if (response.body() != null){
//
//                    try {
//                        JSONObject jsonObject = new JSONObject(response.body().string());
//
//                        Log.d("cardSerial", jsonObject.toString());
//
//                        uiHandler.post(new Runnable() {
//                            @Override
//                            public void run() {
//
//                                //清空activity调用盏
//                                Intent intent = new Intent(CardActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                                startActivity(intent);
//                            }
//                        });
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
//    }
}
