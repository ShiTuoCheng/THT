package tht.topu.com.tht.ui.activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

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

/**
 * Created by asus on 2017/8/15.
 * 修改密码
 */
public class PasswordActivity extends BaseActivity{

    private Button qrCodeButton;
    private EditText phoneEditText;
    private EditText codeEditText;
    private EditText setPasswordEditText;
    private EditText confirmPasswordEditText;
    private TextView saveTextView;
    private ImageView backImageView;

    public static final MediaType JSON = MediaType
            .parse("application/json; charset=utf-8");

    private Handler uiHandler;

    private String random32;
    private String time10;
    private String key64;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //绑定
        setContentView(bindLayout());

        uiHandler = new Handler(getMainLooper());
        //初始化view
        initView();

        backImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PasswordActivity.this.finish();
            }
        });

        qrCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String phoneNum = phoneEditText.getText().toString();

                if (!phoneNum.equals("")){

                    if (Utilities.isMobile(phoneNum)){
                        //验证是否为注册过的手机号
                        isSigned(phoneNum);

                    }else {

                        Utilities.popUpAlert(PasswordActivity.this, "请输入有效的手机号码");
                    }
                }else {

                    Utilities.popUpAlert(PasswordActivity.this, "手机号不能为空");
                }

            }
        });

        saveTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String phoneNum = phoneEditText.getText().toString();
                String code = codeEditText.getText().toString();
                String password = setPasswordEditText.getText().toString();
                String confirm = confirmPasswordEditText.getText().toString();

                if (phoneNum.equals("")){

                    Utilities.popUpAlert(PasswordActivity.this, "手机号不能为空");
                }else if (code.equals("")){

                    Utilities.popUpAlert(PasswordActivity.this, "验证码不能为空");
                }else if (password.equals("")){

                    Utilities.popUpAlert(PasswordActivity.this, "密码不能为空");
                }else if (confirm.equals("")){

                    Utilities.popUpAlert(PasswordActivity.this, "请再输入一次密码");
                }else {

                    isValid(phoneNum, code);
                }
            }
        });
    }

    //绑定布局
    @Override
    public int bindLayout() {
        return R.layout.activity_password;
    }

    //初始化view
    @Override
    public void initView(){

        backImageView = (ImageView)findViewById(R.id.forumPostBack);
        qrCodeButton = (Button)findViewById(R.id.password_code_btn);
        phoneEditText = (EditText)findViewById(R.id.userName);
        codeEditText = (EditText)findViewById(R.id.password_code);
        setPasswordEditText = (EditText)findViewById(R.id.password_set);
        confirmPasswordEditText = (EditText)findViewById(R.id.password_confirm);
        saveTextView = (TextView)findViewById(R.id.password_keep);
    }

    //验证是否为注册过的手机号
    private void isSigned(final String phoneNum){

        random32 = Utilities.getStringRandom(32);
        time10 = Utilities.get10Time();
        key64 = Utilities.get64Key(random32);

        final String json = "{\n" +
                "    \"validate_k\": \"1\",\n" +
                "    \"params\": [\n" +
                "           {\n" +
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
                "                    \"s_Grade\": \"\",\n" +
                "                    \"s_Keywords\": \"\",\n" +
                "                    \"s_Members_LV\": \"\",\n" +
                "                    \"s_Mid\": \"\",\n" +
                "                    \"s_Mobile\": \""+phoneNum+"\",\n" +
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
                "                    \"signature\": \""+ Utilities.encode("s_Alive="+"s_Attention_state="+"s_d1="+"s_d2="+"s_d3="+"s_d4="+"s_d5="+"s_d6="+"s_Gag="+"s_Grade="+"s_Keywords="+"s_Members_LV="+"s_Mid="+"s_Mobile="+phoneNum+"s_Openid="+"s_Order="+"s_Ranking_Mid="+"s_Referee_Mid="+"s_Referee2_Mid="+"s_Stem_from=2"+"s_The_sun="+"s_Total_parameter=Mid,OpenID,Nickname,Head_img,Mname,Sex,Mobile,Rdate,Birthday,Alive,Gag,The_sun,Members_LV,Orders_count,Integral,card,Passwd,Addr,Stem_from,Token,Token_expiry,Token_IP,Referee_Mid,Referee2_Mid,Referee,Referee2,Reward,Withdraw,Contribution_Award,Balance,Ranking"+"non_str="+random32+"stamp="+time10+"keySecret="+key64) +"\"\n" +
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

                        Utilities.popUpAlert(PasswordActivity.this, "出现某些未知错误，请稍后再试");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.body() != null){

                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());

                        Log.d("result", jsonObject.toString());

                        JSONArray jsonArray  = jsonObject.getJSONArray("result").getJSONObject(0).getJSONArray("list");

                        if (jsonArray.length() == 0){

                            uiHandler.post(new Runnable() {
                                @Override
                                public void run() {

                                    Utilities.popUpAlert(PasswordActivity.this, "输入的手机号没有注册过，请重新输入有效手机号");
                                }
                            });
                        }else if(jsonArray.getJSONObject(0).getString("Stem_from").equals("1")){

                            uiHandler.post(new Runnable() {
                                @Override
                                public void run() {

                                    Utilities.popUpAlert(PasswordActivity.this, "输入的手机号没有注册过，请重新输入有效手机号");
                                }
                            });
                        } else{

                            uiHandler.post(new Runnable() {
                                @Override
                                public void run() {

                                    //调用获取验证码的方法
                                    qrCodeHandler(phoneNum);
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

    private void qrCodeHandler(final String phoneNum) {

        getCode(phoneNum);

        CountDownTimer countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long l) {

                qrCodeButton.setEnabled(false);
                qrCodeButton.setText(String.valueOf(l / 1000) + "秒后重发");
            }

            @Override
            public void onFinish() {

                qrCodeButton.setEnabled(true);
                qrCodeButton.setText("获取验证码");
            }
        };

        countDownTimer.start();
    }

    //如果为已注册的手机号则回调用本方法来获取验证码
    private void getCode(String num){

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
                "                    \"Mobile\": \""+num+"\"\n" +
                "                },\n" +
                "                \"sign_valid\": {\n" +
                "                    \"source\": \"Android\",\n" +
                "                    \"non_str\": \""+random32+"\",\n" +
                "                    \"stamp\": \""+time10+"\",\n" +
                "                    \"signature\": \""+Utilities.encode("Mobile="+num+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
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

                        Utilities.popUpAlert(PasswordActivity.this, "出现某些未知错误，请稍后再试");
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

                                    codeEditText.setText(validCode);
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

    //点击保存提交验证 分两步：
    //第一步验证手机号与验证码是否相符
    //若验证码有效则验证两次密码输入的是否一样，一样则提交更改密码
    private void isValid(final String phoneNum, String codeNum){

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
                "        }\n" +
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

                        Utilities.popUpAlert(PasswordActivity.this, "出现某些未知错误，请稍后再试");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.body() != null){

                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        final String mobile = jsonObject.getJSONArray("result").getJSONObject(0).getJSONObject("detail").getString("Mobile");

                        Log.d("firstStep", jsonObject.toString());

                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {

                                if (mobile.equals("")){

                                    Utilities.popUpAlert(PasswordActivity.this, "验证码不正确，请重试");
                                }else {

                                    submitChangePassword(phoneNum);

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

    //第二步验证两次密码是否一样，一样则提交修改密码
    private void submitChangePassword(String phoneNum){

        String password = setPasswordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        if (password.equals("") && confirmPassword.equals("")){

            Utilities.popUpAlert(PasswordActivity.this, "密码或者验证密码部分不能为空");

            return;
        }else if (!password.equals(confirmPassword)){

            Utilities.popUpAlert(PasswordActivity.this, "两次密码不一致，请重试");
            return;
        }

        random32 = Utilities.getStringRandom(32);
        time10 = Utilities.get10Time();
        key64 = Utilities.get64Key(random32);

        final String json = "{\n" +
                "    \"validate_k\": \"1\",\n" +
                "    \"params\": [\n" +
                "        {\n" +
                "            \"type\": \"Members\",\n" +
                "            \"act\": \"App_mobile_Passwd\",\n" +
                "            \"para\": {\n" +
                "                \"params\": {\n" +
                "                    \"Mobile\": \""+phoneNum+"\",\n" +
                "                    \"validate_pwd\": \""+password+"\"\n" +
                "                },\n" +
                "                \"sign_valid\": {\n" +
                "                    \"source\": \"Android\",\n" +
                "                    \"non_str\": \""+random32+"\",\n" +
                "                    \"stamp\": \""+time10+"\",\n" +
                "                    \"signature\": \""+Utilities.encode("Mobile="+phoneNum+"validate_pwd="+password+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
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

                        Utilities.popUpAlert(PasswordActivity.this, "出现某些未知错误，请稍后再试");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.body() != null){

                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        final String result = jsonObject.getJSONArray("result").getJSONObject(0).getString("result");

                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {

                                Toast.makeText(PasswordActivity.this, result, Toast.LENGTH_SHORT).show();
                                PasswordActivity.this.finish();
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
