package tht.topu.com.tht.ui.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
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
import tht.topu.com.tht.R;
import tht.topu.com.tht.ui.base.BaseActivity;
import tht.topu.com.tht.utils.API;
import tht.topu.com.tht.utils.Utilities;

public class SignActivity extends BaseActivity {

    private AppCompatButton getCodeButton;
    private AppCompatButton nextButton;
    private ImageView backButton;

    private EditText userInput;
    private EditText qrCodeInput;
    private EditText passwordInput;

    private CountDownTimer countDownTimer;

    public static final MediaType JSON = MediaType
            .parse("application/json; charset=utf-8");

    private static final String MID_KEY = "1x11";
    private static final String TOKEN_KEY = "0x01";

    private Handler uiHandler;

    private ProgressDialog dialog;

    private String random32;
    private String time10;
    private String key64;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(bindLayout());

        initView();


//        判断网络是否可用
        if (!Utilities.isNetworkAvaliable(getApplicationContext())){

            Utilities.popUpAlert(this, "网络不可用");
        }

        uiHandler = new Handler(Looper.getMainLooper());

        //获取验证码点击事件
        getCodeButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                String validCode = userInput.getText().toString();
                if (!validCode.equals("")){

                    if (Utilities.isMobile(validCode)){
                        //获取验证码
                        qrCodeHandler(validCode);

                    }else {

                        Utilities.popUpAlert(SignActivity.this, "请输入有效的手机号码");
                    }
                }else {

                    Utilities.popUpAlert(SignActivity.this, "手机号不能为空");
                }
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String phoneNum = userInput.getText().toString();
                String qrCode = qrCodeInput.getText().toString();
                String passWord = passwordInput.getText().toString();

                if (!phoneNum.equals("") && !qrCode.equals("") && !passWord.equals("")){

                    isCodeValid(phoneNum, qrCode, passWord);
                }else {

                    if (passwordInput.getText().toString().equals("")){

                        Utilities.popUpAlert(SignActivity.this, "登录密码不能为空!");
                    }else if (qrCodeInput.getText().toString().equals("")){

                        Utilities.popUpAlert(SignActivity.this, "验证码不能为空!");
                    }
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SignActivity.this.finish();
            }
        });
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_sign;
    }

    @Override
    public void initView() {

        getCodeButton = (AppCompatButton) findViewById(R.id.getCodeButton);
        nextButton = (AppCompatButton)findViewById(R.id.signInButton);
        backButton = (ImageView)findViewById(R.id.backButton);
        userInput = (EditText)findViewById(R.id.userInput);
        qrCodeInput = (EditText)findViewById(R.id.codeInput);
        passwordInput = (EditText)findViewById(R.id.passwordInput);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (uiHandler != null){

            uiHandler = null;
        }

        if (countDownTimer != null){

            countDownTimer.cancel();
        }
    }

    private void qrCodeHandler(final String code){

        getVaild(code);

        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long l) {

                getCodeButton.setEnabled(false);
                getCodeButton.setText(String.valueOf(l/1000)+"秒后重发");
            }

            @Override
            public void onFinish() {

                getCodeButton.setEnabled(true);
                getCodeButton.setText("获取验证码");
            }
        };

        countDownTimer.start();
    }

    //获取验证码
    private void getVaild(String phoneNum) {

        random32 = Utilities.getStringRandom(32);
        time10 = Utilities.get10Time();
        key64 = Utilities.get64Key(random32);

        String json = "{\n" +
                "    \"validate_k\": \"1\",\n" +
                "    \"params\": [\n" +
                "        {\n" +
                "            \"type\": \"Mobile_Valid\",\n" +
                "            \"act\": \"Add\",\n" +
                "            \"para\": {\n" +
                "                \"params\": {\n" +
                "                    \"Mobile\": \""+phoneNum+"\"\n" +
                "                },\n" +
                "                \"sign_valid\": {\n" +
                "                    \"source\": \"Android\",\n" +
                "                    \"non_str\": \""+random32+"\",\n" +
                "                    \"stamp\": \""+time10+"\",\n" +
                "                    \"signature\": \""+Utilities.encode("Mobile="+phoneNum+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        OkHttpClient okHttpClient = new OkHttpClient();

        Log.d("json", json);

        RequestBody requestBody = RequestBody.create(JSON, json);

        Request request = new Request.Builder().url(API.getAPI()).post(requestBody).build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {


                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        Utilities.popUpAlert(SignActivity.this, "出现某些未知错误，请稍后再试");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.body() != null){

                    try {
                        JSONObject resultJson = new JSONObject(response.body().string());

                        Log.d("json", resultJson.toString());
                        JSONArray result = resultJson.getJSONArray("result");
                        String error = result.getJSONObject(0).getString("error");

                        if (error.equals("SUCCESS")){

                            final String validCode = result.getJSONObject(0).getString("Valid_code");

                            uiHandler.post(new Runnable() {
                                @Override
                                public void run() {

                                    qrCodeInput.setText(validCode);
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


    //点击下一步的第一次验证
    private void isCodeValid(final String phoneNum, String codeNum, final String password) {

        random32 = Utilities.getStringRandom(32);
        time10 = Utilities.get10Time();
        key64 = Utilities.get64Key(random32);

        final String json = "{\n" +
                "    \"validate_k\": \"1\",\n" +
                "    \"params\": [\n" +
                "        {\n" +
                "            \"type\": \"Mobile_Valid\",\n" +
                "            \"act\": \"Select_Detail\",\n" +
                "            \"para\": {\n" +
                "                \"params\": {\n" +
                "                    \"d_Mobile\": \""+phoneNum+"\",\n" +
                "                    \"d_Valid_code\": \""+ codeNum+"\",\n" +
                "                    \"s_Total_parameter\": \"Mobile,Valid_code,Valid_date\"\n"+
                "                },\n" +
                "                \"sign_valid\": {\n" +
                "                    \"source\": \"Android\",\n" +
                "                    \"non_str\": \""+ random32 +"\",\n" +
                "                    \"stamp\": \""+ time10 +"\",\n" +
                "                    \"signature\": \""+Utilities.encode("d_Mobile="+phoneNum+"d_Valid_code="+codeNum+"s_Total_parameter=Mobile,Valid_code,Valid_date"+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
                "                }\n" +
                "            }\n" +
                "        },\n" +
                "{\n" +
                "            \"type\": \"Members\",\n" +
                "            \"act\": \"Select_List\",\n" +
                "            \"para\": {\n" +
                "                \"params\": {\n" +
                "                    \"s_Alive\": \"\",\n" +
                "                    \"s_Attention_state\": \"\",\n" +
                "                    \"s_d1\": \"\",\n" +
                "                    \"s_d2\": \"\",\n" +
                "                    \"s_d3\": \"\",\n" +
                "                    \"s_d4\": \"\",\n" +
                "                    \"s_d5\": \"\",\n" +
                "                    \"s_d6\": \"\",\n" +
                "                    \"s_Gag\": \"\",\n" +
                "                    \"s_Keywords\": \"\",\n" +
                "                    \"s_Members_LV\": \"\",\n" +
                "                    \"s_Mid\": \"\",\n" +
                "                    \"s_Mobile\": \"$$$$$$$$$$Mobile_Valid:Select_Detail:detail:Mobile$$$$$$$$$$\",\n" +
                "                    \"s_Openid\": \"\",\n" +
                "                    \"s_Order\": \"\",\n" +
                "                    \"s_Ranking_Mid\": \"\",\n"+
                "                    \"s_Referee_Mid\": \"\",\n"+
                "                    \"s_Referee2_Mid\": \"\",\n"+
                "                    \"s_Stem_from\": \"2\",\n" +
                "                    \"s_The_sun\": \"\",\n" +
                "                    \"s_Total_parameter\": \"Mid,OpenID,Nickname,Head_img,Mname,Sex,Mobile,Rdate,Birthday,Alive,Gag,The_sun,Members_LV,Orders_count,Integral,card,Passwd,Addr,Stem_from,Token,Token_expiry,Token_IP,Referee_Mid,Referee2_Mid,Referee,Referee2,Reward,Withdraw,Contribution_Award,Balance,Ranking\"\n" +
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
                "                    \"non_str\": \""+ random32 +"\",\n" +
                "                    \"stamp\": \""+ time10 +"\",\n" +
                "                    \"signature\": \""+ Utilities.encode("s_Alive="+"s_Attention_state="+"s_d1="+"s_d2="+"s_d3="+"s_d4="+"s_d5="+"s_d6="+"s_Gag="+"s_Keywords="+"s_Members_LV="+"s_Mid="+"s_Mobile=$$$$$$$$$$Mobile_Valid:Select_Detail:detail:Mobile$$$$$$$$$$"+"s_Openid="+"s_Order="+"s_Ranking_Mid="+"s_Referee_Mid="+"s_Referee2_Mid="+"s_Stem_from=2"+"s_The_sun="+"s_Total_parameter=Mid,OpenID,Nickname,Head_img,Mname,Sex,Mobile,Rdate,Birthday,Alive,Gag,The_sun,Members_LV,Orders_count,Integral,card,Passwd,Addr,Stem_from,Token,Token_expiry,Token_IP,Referee_Mid,Referee2_Mid,Referee,Referee2,Reward,Withdraw,Contribution_Award,Balance,Ranking"+"non_str="+random32+"stamp="+time10+"keySecret="+key64) +"\"\n" +
                "                }\n" +
                "            }\n" +
                "        }"+
                "    ]\n" +
                "}";

        OkHttpClient okHttpClient = new OkHttpClient();

        final RequestBody requestBody = RequestBody.create(JSON, json);
        Request request = new Request.Builder().url(API.getAPI()).post(requestBody).build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        Utilities.popUpAlert(SignActivity.this, "出现某些未知错误，请稍后再试");
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {

                if (response.body() != null){

                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());

                        String mobile = jsonObject.getJSONArray("result").getJSONObject(0).getJSONObject("detail").getString("Mobile");

                        if (!mobile.equals("")){

                            if (jsonObject.getJSONArray("result").getJSONObject(1).getJSONArray("list").isNull(0)){

                                submitUsr(phoneNum, password);

                                Utilities.jumpToActivity(SignActivity.this, CardActivity.class);
                            }else {

                                uiHandler.post(new Runnable() {
                                    @Override
                                    public void run() {

                                        Utilities.popUpAlert(SignActivity.this, "该手机号已经被注册了！");
                                    }
                                });
                            }

                        }else {

                            uiHandler.post(new Runnable() {
                                @Override
                                public void run() {

                                    Utilities.popUpAlert(SignActivity.this, "验证码不正确，请重试");
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

    //提交注册的用户名和密码
    private void submitUsr(String phoneNum, String password){

        random32 = Utilities.getStringRandom(32);
        time10 = Utilities.get10Time();
        key64 = Utilities.get64Key(random32);

        final String json = "{\n" +
                "    \"validate_k\": \"1\",\n" +
                "    \"params\": [\n" +
                "        {\n" +
                "            \"type\": \"Members\",\n" +
                "            \"act\": \"App_Add\",\n" +
                "            \"para\": {\n" +
                "                \"params\": {\n" +
                "                    \"Mobile\": \""+phoneNum+"\",\n" +
                "                    \"Passwd\": \""+ password+"\"\n" +
                "                },\n" +
                "                \"sign_valid\": {\n" +
                "                    \"source\": \"Android\",\n" +
                "                    \"non_str\": \""+ random32 +"\",\n" +
                "                    \"stamp\": \""+ time10 +"\",\n" +
                "                    \"signature\": \""+Utilities.encode("Mobile="+phoneNum+"Passwd="+password+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(JSON, json);

        final Request request = new Request.Builder().url(API.getAPI()).post(requestBody).build();


        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        Utilities.popUpAlert(SignActivity.this, "出现某些未知错误，请稍后再试");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.body() != null){

                    try {

                        JSONObject jsonObject = new JSONObject(response.body().string());
                        final String mid = jsonObject.getJSONArray("result").getJSONObject(0).getString("Mid");

                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {

                                 dialog = ProgressDialog.show(SignActivity.this, "正在提交",
                                        "正在提交用户信息...", false);
                            }
                        });

                        if (!mid.equals("")){

                            uiHandler.post(new Runnable() {
                                @Override
                                public void run() {

                                    SharedPreferences.Editor editor = getSharedPreferences("TokenData", MODE_PRIVATE).edit();
                                    editor.putString(MID_KEY, mid);
                                    editor.apply();

                                    fetchToken(mid);
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

    //提交用户信息之后默认登录
    private void fetchToken(final String mid){

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
                "                    \"d_IsUpdateToken\": \"1\",\n"+
                "                    \"d_Mid\": \""+mid+"\",\n" +
                "                    \"d_Token\": \"\",\n" +
                "                    \"s_Total_parameter\": \"Mid,OpenID,Nickname,Head_img,Mname,Sex,Mobile,Rdate,Birthday,Alive,Gag,The_sun,Members_LV,Orders_count,Integral,card,Passwd,Addr,Stem_from,Token,Token_expiry,Token_IP,Referee_Mid,Referee2_Mid,Referee,Referee2,Reward,Withdraw,Contribution_Award,Balance\"\n" +
                "                },\n" +
                "                \"sign_valid\": {\n" +
                "                    \"source\": \"Android\",\n" +
                "                    \"non_str\": \""+random32+"\",\n" +
                "                    \"stamp\": \""+time10+"\",\n" +
                "                    \"signature\": \""+Utilities.encode("d_Alive="+"d_IsUpdateToken=1"+"d_Mid="+mid+"d_Token="+"s_Total_parameter=Mid,OpenID,Nickname,Head_img,Mname,Sex,Mobile,Rdate,Birthday,Alive,Gag,The_sun,Members_LV,Orders_count,Integral,card,Passwd,Addr,Stem_from,Token,Token_expiry,Token_IP,Referee_Mid,Referee2_Mid,Referee,Referee2,Reward,Withdraw,Contribution_Award,Balance"+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
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

                        Utilities.popUpAlert(SignActivity.this, "出现某些未知错误，请稍后再试");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.body() != null){

                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());

                        final String newToken = jsonObject.getJSONArray("result").getJSONObject(0).getJSONObject("detail").getString("Token");

                        Log.d("signMid", mid);
                        Log.d("signActivity", jsonObject.toString());
                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {

                                final SharedPreferences.Editor editor = getSharedPreferences("TokenData", Context.MODE_PRIVATE).edit();

                                editor.putString(TOKEN_KEY, newToken);
                                editor.apply();

                                dialog.dismiss();
                                Utilities.jumpToActivity(SignActivity.this, MainActivity.class);
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
