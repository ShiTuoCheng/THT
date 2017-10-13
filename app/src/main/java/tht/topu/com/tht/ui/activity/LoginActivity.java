package tht.topu.com.tht.ui.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.Request;

import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import tht.topu.com.tht.R;
import tht.topu.com.tht.ui.base.BaseActivity;
import tht.topu.com.tht.utils.API;
import tht.topu.com.tht.utils.MyScrollView;
import tht.topu.com.tht.utils.Softkeyboardlistener;
import tht.topu.com.tht.utils.Utilities;

public class LoginActivity extends BaseActivity {

    private ImageView backgroundImageView;
    private MyScrollView relativeLayout;
    private AppCompatButton loginButton;
    private AppCompatButton signInButton;
    private EditText userInput;
    private EditText passwordInput;

    private TextView forgotPasswordTextView;

    private Handler uiHandler;
    //json请求
    public static final MediaType JSON = MediaType
            .parse("application/json; charset=utf-8");

    private static final String TOKEN_KEY = "0x01";
    private static final String MID_KEY = "1x11";
    private static final String PHONE_KEY = "2x11";

    private static final String loginAPI = API.getAPI();

    private String random32;
    private String time10;
    private String key64;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences sharedPreferences = getSharedPreferences("TokenData", MODE_PRIVATE);

        if (!sharedPreferences.getString(TOKEN_KEY, "").equals("")){

            LoginActivity.this.finish();
            Utilities.jumpToActivity(LoginActivity.this, MainActivity.class);
        }

        super.onCreate(savedInstanceState);
        setContentView(bindLayout());

        initView();

        uiHandler = new Handler(Looper.getMainLooper());

//        判断网络是否可用
        if (!Utilities.isNetworkAvaliable(getApplicationContext())){

            Utilities.popUpAlert(this, "网络不可用");
        }

        //登录按钮逻辑
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    submitUser(userInput.getText().toString(), passwordInput.getText().toString());
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
//                Utilities.jumpToActivity(LoginActivity.this, MainActivity.class);
            }
        });

        //注册按钮点击逻辑
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Utilities.jumpToActivity(LoginActivity.this, SignActivity.class);
            }
        });

        //点击忘记密码逻辑
        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bundle bundle = new Bundle();

                bundle.putString("title", "忘记密码");
                Utilities.jumpToActivity(LoginActivity.this, PasswordActivity.class, bundle, "titleData");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (uiHandler != null){

            uiHandler = null;
        }
    }

    @Override
    public int bindLayout() {

        return R.layout.activity_login;
    }

    @Override
    public void initView(){

        relativeLayout = (MyScrollView)findViewById(R.id.scrollView);
        backgroundImageView = (ImageView)findViewById(R.id.loginBackgroundImageView);
        loginButton = (AppCompatButton)findViewById(R.id.loginButton);
        signInButton = (AppCompatButton)findViewById(R.id.signInButton);
        userInput = (EditText)findViewById(R.id.userInput);
        passwordInput = (EditText)findViewById(R.id.passwordInput);
        forgotPasswordTextView = (TextView)findViewById(R.id.forgotPassword);

        Glide.with(this).load(R.drawable.login).into(backgroundImageView);

        final int screenHeight = this.getWindowManager().getDefaultDisplay().getHeight();

        Softkeyboardlistener.setListener(LoginActivity.this, new Softkeyboardlistener.OnSoftKeyBoardChangeListener() {
            @Override
            public void keyBoardShow(int height) {

                backgroundImageView.setMinimumHeight(screenHeight);

                int screenHeight = relativeLayout.getHeight();
                relativeLayout.setMinimumHeight(screenHeight+height+200);
//                Toast.makeText(LoginActivity.this, "键盘显示 高度" + height, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void keyBoardHide(int height) {
//                Toast.makeText(LoginActivity.this, "键盘隐藏 高度" + height, Toast.LENGTH_SHORT).show();
            }
        });
    }

    //提交逻辑
    private void submitUser(String userName, String userPassword) throws NoSuchAlgorithmException {

        random32 = Utilities.getStringRandom(32);
        time10 = Utilities.get10Time();
        key64 = Utilities.get64Key(random32);
        if (!userName.equals("") && !userPassword.equals("")){

            OkHttpClient okHttpClient = new OkHttpClient();

            String json = "{\n" +
                    "    \"validate_k\": \"1\",\n" +
                    "    \"params\": [\n" +
                    "        {\n" +
                    "            \"type\": \"Members\",\n" +
                    "            \"act\": \"App_login\",\n" +
                    "            \"para\": {\n" +
                    "                \"params\": {\n" +
                    "                    \"Mobile\": \""+userName+"\",\n" +
                    "                    \"Passwd\": \""+userPassword+"\"\n" +
                    "                },\n" +
                    "                \"sign_valid\": {\n" +
                    "                    \"source\": \"Android\",\n" +
                    "                    \"non_str\": \""+random32+"\",\n" +
                    "                    \"stamp\": \""+time10+"\",\n" +
                    "                    \"signature\": \""+Utilities.encode("Mobile="+userName+"Passwd="+userPassword+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
                    "                }\n" +
                    "            }\n" +
                    "        }\n" +
                    "    ]\n" +
                    "}";

            RequestBody body = RequestBody.create(JSON, json);

            final Request request = new Request.Builder().url(loginAPI).post(body).build();

            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {

                            Utilities.popUpAlert(LoginActivity.this, "出现某些未知错误，请稍后再试");
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    if (response.body() != null){

                        try {
                            JSONObject jsonObject = new JSONObject(response.body().string());

                            Log.d("json", jsonObject.toString());
                            JSONArray result = jsonObject.getJSONArray("result");
                            String error = result.getJSONObject(0).getString("error");
                            String errorMsg = result.getJSONObject(0).getString("errmsg");

                            if (error.equals("SUCCESS") && errorMsg.equals("")){

                                JSONArray detailJson = result.getJSONObject(0).getJSONArray("detail");
                                String token = detailJson.getJSONObject(0).getString("Token");
                                String mid = detailJson.getJSONObject(0).getString("Mid");
                                String phone = detailJson.getJSONObject(0).getString("Mobile");

                                final int grade = detailJson.getJSONObject(0).getJSONObject("card").getInt("Grade");

                                //缓存token
                                SharedPreferences.Editor editor = getSharedPreferences("TokenData", MODE_PRIVATE).edit();
                                editor.putString(TOKEN_KEY, token);
                                editor.putString(MID_KEY, mid);
                                editor.putString(PHONE_KEY, phone);
                                editor.apply();

                                uiHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        
                                        Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                                        //跳转页面之前先收回软键盘
                                        InputMethodManager imm =  (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(),0);

                                        if (grade == 0){

                                            Toast.makeText(LoginActivity.this, "您还没有购买卡券，请购买卡券以继续", Toast.LENGTH_LONG).show();
                                            LoginActivity.this.finish();
                                            Utilities.jumpToActivity(LoginActivity.this, CardActivity.class);
                                        }else {

                                            LoginActivity.this.finish();
                                            Utilities.jumpToActivity(LoginActivity.this, MainActivity.class);
                                        }
                                    }
                                });
                            }else {

                                uiHandler.post(new Runnable() {
                                    @Override
                                    public void run() {

                                        Utilities.popUpAlert(LoginActivity.this, "登录失败！输入的用户名和密码有误。");
                                    }
                                });
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }else{

            Utilities.popUpAlert(LoginActivity.this, "用户名或密码不能为空");
        }
    }
}
