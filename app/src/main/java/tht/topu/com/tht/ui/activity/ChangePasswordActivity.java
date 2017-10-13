package tht.topu.com.tht.ui.activity;

import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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

public class ChangePasswordActivity extends BaseActivity {

    private TextView saveButton;
    private ImageView backImageView;
    private TextView password_phone;
    private EditText newPasswordEditText;

    private EditText qrcodeEditText;
    private Button getCodeButton;

    private static final String MID_KEY = "1x11";
    private static final String TOKEN_KEY = "0x01";
    private static final String PHONE_KEY = "2x11";

    private String random32;
    private String time10;
    private String key64;

    private Handler uiHandler;
    //json请求
    public static final MediaType JSON = MediaType
            .parse("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(bindLayout());

        uiHandler = new Handler(getMainLooper());

        //初始化视图
        initView();

        final SharedPreferences sharedPreferences = getSharedPreferences("TokenData", MODE_PRIVATE);

        final String mobile = sharedPreferences.getString(PHONE_KEY, "");

        //格式化手机号
        password_phone.setText(mobile.substring(0,3)+"****"+mobile.substring(7,mobile.length()));

        // 获取验证码
        getCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                qrCodeHandler(mobile);
            }
        });

        //保存点击事件
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String newPassword = newPasswordEditText.getText().toString();
                String qrCodeStr = qrcodeEditText.getText().toString();

                if (newPassword.equals("") || qrCodeStr.equals("")){

                    Utilities.popUpAlert(ChangePasswordActivity.this, "信息不能为空，请输入");
                    return;
                }

                isValid(sharedPreferences.getString(PHONE_KEY, ""), qrCodeStr, newPassword);
            }
        });

        //返回点击事件
        backImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ChangePasswordActivity.this.finish();
            }
        });
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_change_password;
    }

    @Override
    public void initView() {

        saveButton = (TextView)findViewById(R.id.password_keep);
        password_phone = (TextView)findViewById(R.id.password_phone);
        newPasswordEditText = (EditText)findViewById(R.id.password_new);
        backImageView = (ImageView)findViewById(R.id.forumPostBack);
        getCodeButton = (Button)findViewById(R.id.password_code_btn);
        qrcodeEditText = (EditText)findViewById(R.id.password_code);
    }

    private void qrCodeHandler(final String code){

        getVaild(code);

        CountDownTimer countDownTimer = new CountDownTimer(60000, 1000) {
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

                        Utilities.popUpAlert(ChangePasswordActivity.this, "出现某些未知错误，请稍后再试");
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

                                    qrcodeEditText.setText(validCode);
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
    private void isValid(final String phoneNum, String codeNum, final String password){

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

                        Utilities.popUpAlert(ChangePasswordActivity.this, "出现某些未知错误，请稍后再试");
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

                                    Utilities.popUpAlert(ChangePasswordActivity.this, "验证码不正确，请重试");
                                }else {

                                    submitChangePassword(phoneNum, password);

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
    private void submitChangePassword(String phoneNum, String password){

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

                        Utilities.popUpAlert(ChangePasswordActivity.this, "出现某些未知错误，请稍后再试");
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

                                Toast.makeText(ChangePasswordActivity.this, result, Toast.LENGTH_SHORT).show();
                                ChangePasswordActivity.this.finish();
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
