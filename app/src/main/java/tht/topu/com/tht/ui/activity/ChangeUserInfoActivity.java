package tht.topu.com.tht.ui.activity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import jp.wasabeef.glide.transformations.CropCircleTransformation;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import tht.topu.com.tht.R;
import tht.topu.com.tht.utils.API;
import tht.topu.com.tht.utils.Utilities;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class ChangeUserInfoActivity extends AppCompatActivity {

    private ImageView userAvatar;
    private EditText userName;
    private EditText userPhoneNum;
    private EditText codeEdittext;
    private EditText addressEdittext;
    private RadioButton manRadioButton;
    private RadioButton femaleRadioButton;
    private EditText dateEdittext;
    private ImageView backButton;

    private Intent broadcastIntent;
    private LocalBroadcastManager localBroadcastManager;

    private AppCompatButton submitButton;
    private AppCompatButton getCodeButton;

    private static final String MID_KEY = "1x11";
    private static final String TOKEN_KEY = "0x01";

    private String random32;
    private String time10;
    private String key64;
    private String ogPhoneNum;

    private String image64 = "";
    private String headImg = "";
    private String sex = "";
    private String json = "";
    private String mid;

    private Calendar calendar; // 通过Calendar获取系统时间
    private int mYear;
    private int mMonth;
    private int mDay;

    private Handler uiHandler;
    //json请求
    public static final MediaType JSON = MediaType
            .parse("application/json; charset=utf-8");

    public final static int CONSULT_DOC_PICTURE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_user_info);

        uiHandler = new Handler(getMainLooper());
        initView();

        if (!Utilities.isNetworkAvaliable(ChangeUserInfoActivity.this)){

            Utilities.popUpAlert(ChangeUserInfoActivity.this, "网络不可用");

        }else {

            initData();
        }

        //获取验证码逻辑
        getCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String phoneNum = userPhoneNum.getText().toString();

                if (!phoneNum.equals("") && Utilities.isMobile(phoneNum)){

                    qrCodeHandler(phoneNum);
                }else {

                    Utilities.popUpAlert(ChangeUserInfoActivity.this, "手机号无效 请输入有效手机号码");
                }
            }
        });
    }

    private void initView(){

        calendar = Calendar.getInstance();
        userAvatar = (ImageView)findViewById(R.id.changeUserAvatar);
        userName = (EditText)findViewById(R.id.changeNameInput);
        userPhoneNum = (EditText)findViewById(R.id.changePhoneInput);
        codeEdittext = (EditText)findViewById(R.id.changeCodeInput);
        addressEdittext = (EditText)findViewById(R.id.changeAddressDayInput);
        manRadioButton = (RadioButton)findViewById(R.id.manRadioButton);
        femaleRadioButton = (RadioButton)findViewById(R.id.femaleRadioButton);
        dateEdittext = (EditText)findViewById(R.id.changeBirthDayInput);
        submitButton = (AppCompatButton)findViewById(R.id.changeSubmitButton);
        getCodeButton = (AppCompatButton)findViewById(R.id.getCodeButton);
        backButton = (ImageView)findViewById(R.id.changeBack);

        dateEdittext.setInputType(InputType.TYPE_NULL);


        //选择日期的逻辑
        dateEdittext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new DatePickerDialog(ChangeUserInfoActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int month, int day) {
                                mYear = year;
                                mMonth = month;
                                mDay = day;
                                // 更新EditText控件日期 小于10加0
                                dateEdittext.setText(mYear+"-"+(mMonth+1)+"-"+mDay);
                            }
                        }, calendar.get(Calendar.YEAR), calendar
                        .get(Calendar.MONTH), calendar
                        .get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        //提交逻辑
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                submitChangedData();
            }
        });

        //点击头像更改
        userAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(intent, CONSULT_DOC_PICTURE);
            }
        });

        //返回按钮点击逻辑
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ChangeUserInfoActivity.this.finish();
            }
        });
    }

    private void qrCodeHandler(final String phoneNum){

        if (ogPhoneNum.equals(userPhoneNum.getText().toString())){

            Utilities.popUpAlert(ChangeUserInfoActivity.this, "您没有更换手机号，无需获取验证码");
        }else {

            getCode(phoneNum);

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
    }

    //初始化数据
    private void initData(){

        random32 = Utilities.getStringRandom(32);
        time10 = Utilities.get10Time();
        key64 = Utilities.get64Key(random32);

        final SharedPreferences sharedPreferences = getSharedPreferences("TokenData", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString(TOKEN_KEY, "");
        mid = sharedPreferences.getString(MID_KEY, "");

        if (token.equals("") && mid.equals("")){

            Utilities.popUpAlert(ChangeUserInfoActivity.this, "登录过期，请重新登录");

            ChangeUserInfoActivity.this.finish();
            Utilities.jumpToActivity(ChangeUserInfoActivity.this, LoginActivity.class);
            return;
        }

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
                "                    \"d_Token\": \""+token+"\",\n" +
                "                    \"s_Total_parameter\": \"Mid,OpenID,Nickname,Head_img,Mname,Sex,Mobile,Rdate,Birthday,Alive,Gag,The_sun,Members_LV,Orders_count,Integral,card,Passwd,Addr,Stem_from,Token,Token_expiry,Token_IP,Referee_Mid,Referee2_Mid,Referee,Referee2,Reward,Withdraw,Contribution_Award,Balance\"\n" +
                "                },\n" +
                "                \"sign_valid\": {\n" +
                "                    \"source\": \"Android\",\n" +
                "                    \"non_str\": \""+random32+"\",\n" +
                "                    \"stamp\": \""+time10+"\",\n" +
                "                    \"signature\": \""+Utilities.encode("d_Alive="+"d_IsUpdateToken=0"+"d_Mid="+mid+"d_Token="+token+"s_Total_parameter=Mid,OpenID,Nickname,Head_img,Mname,Sex,Mobile,Rdate,Birthday,Alive,Gag,The_sun,Members_LV,Orders_count,Integral,card,Passwd,Addr,Stem_from,Token,Token_expiry,Token_IP,Referee_Mid,Referee2_Mid,Referee,Referee2,Reward,Withdraw,Contribution_Award,Balance"+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        OkHttpClient okHttpClient = new OkHttpClient();

        Log.d("data", "token:"+token+"mid:"+mid);

        RequestBody requestBody = RequestBody.create(JSON, json);

        Request request = new Request.Builder().url(API.getAPI()).post(requestBody).build();

        okHttpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {

                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        Utilities.popUpAlert(ChangeUserInfoActivity.this, "出现某些未知错误，请稍后再试");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.body() != null){

                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());

                        Log.d("personal", jsonObject.toString());
                        final String nickName = jsonObject.getJSONArray("result").getJSONObject(0).getJSONObject("detail").getString("Mname");
                        final String address = jsonObject.getJSONArray("result").getJSONObject(0).getJSONObject("detail").getString("Addr");
                        final String birthday = jsonObject.getJSONArray("result").getJSONObject(0).getJSONObject("detail").getString("Birthday");
                        final String mobile = jsonObject.getJSONArray("result").getJSONObject(0).getJSONObject("detail").getString("Mobile");
                        final String sex = jsonObject.getJSONArray("result").getJSONObject(0).getJSONObject("detail").getString("Sex");
                        headImg = jsonObject.getJSONArray("result").getJSONObject(0).getJSONObject("detail").getString("Head_img");

                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {

                                Date myDate = new Date(birthday);
                                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                                String date = formatter.format(myDate);

                                dateEdittext.setText(date);

                                userName.setText(nickName);
                                userPhoneNum.setText(mobile);
                                //加载头像
                                if (headImg.equals("")){

                                    userAvatar.setImageDrawable(getResources().getDrawable(R.mipmap.login_logo));
                                }else {

                                    Glide.with(ChangeUserInfoActivity.this).load(API.getAnothereHostName()+headImg)
                                            .apply(bitmapTransform(new CropCircleTransformation()))
                                            .into((userAvatar));
                                }
                                if (sex.equals("男")){

                                    manRadioButton.setChecked(true);
                                }else if (sex.equals("女")){

                                    femaleRadioButton.setChecked(true);
                                }

                                addressEdittext.setText(address);
                                ogPhoneNum = userPhoneNum.getText().toString();

                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    //提交更改
    private void submitChangedData(){

        final String phoneNum = userPhoneNum.getText().toString();
        String codeNum = codeEdittext.getText().toString();
        final String name = userName.getText().toString();
        final String date = dateEdittext.getText().toString();
        final String address = addressEdittext.getText().toString();

        if (manRadioButton.isChecked()){
            sex = "男";
        }else if (femaleRadioButton.isChecked()){
            sex = "女";
        }

        if (!phoneNum.equals(ogPhoneNum) && codeNum.equals("")){

            Utilities.popUpAlert(ChangeUserInfoActivity.this, "请输入验证码");
            return;
        }

        random32 = Utilities.getStringRandom(32);
        time10 = Utilities.get10Time();
        key64 = Utilities.get64Key(random32);

        if (!phoneNum.equals(ogPhoneNum)){

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
                    "                    \"s_Referee_Mid\": \"\",\n"+
                    "                    \"s_Referee2_Mid\": \"\",\n"+
                    "                    \"s_Stem_from\": \"2\",\n" +
                    "                    \"s_The_sun\": \"\",\n" +
                    "                    \"s_Total_parameter\": \"Mid,OpenID,Nickname,Head_img,Mname,Sex,Mobile,Rdate,Birthday,Alive,Gag,The_sun,Members_LV,Orders_count,Integral,card,Passwd,Addr,Stem_from,Token,Token_expiry,Token_IP,Referee_Mid,Referee2_Mid,Referee,Referee2,Reward,Withdraw,Contribution_Award,Balance\"\n" +
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
                    "                    \"signature\": \""+ Utilities.encode("s_Alive="+"s_Attention_state="+"s_d1="+"s_d2="+"s_d3="+"s_d4="+"s_d5="+"s_d6="+"s_Gag="+"s_Keywords="+"s_Members_LV="+"s_Mid="+"s_Mobile=$$$$$$$$$$Mobile_Valid:Select_Detail:detail:Mobile$$$$$$$$$$"+"s_Openid="+"s_Order="+"s_Referee_Mid="+"s_Referee2_Mid="+"s_Stem_from=2"+"s_The_sun="+"s_Total_parameter=Mid,OpenID,Nickname,Head_img,Mname,Sex,Mobile,Rdate,Birthday,Alive,Gag,The_sun,Members_LV,Orders_count,Integral,card,Passwd,Addr,Stem_from,Token,Token_expiry,Token_IP,Referee_Mid,Referee2_Mid,Referee,Referee2,Reward,Withdraw,Contribution_Award,Balance"+"non_str="+random32+"stamp="+time10+"keySecret="+key64) +"\"\n" +
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

                            Utilities.popUpAlert(ChangeUserInfoActivity.this, "出现某些未知错误，请稍后再试");
                        }
                    });
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {

                    if (response.body() != null){

                        try {
                            JSONObject jsonObject = new JSONObject(response.body().string());

                            String mobile = jsonObject.getJSONArray("result").getJSONObject(0).getJSONObject("detail").getString("Mobile");

                            Log.d("mobile", mobile);
                            if (!mobile.equals("")){

                                if (jsonObject.getJSONArray("result").getJSONObject(1).getJSONArray("list").isNull(0)){

                                    Log.d("开始执行修改方法", "提交修改信息");
                                    submitHandler(phoneNum, name, sex, date, address);

                                    //ChangeUserInfoActivity.this.finish();
                                }else {

                                    uiHandler.post(new Runnable() {
                                        @Override
                                        public void run() {

                                            Utilities.popUpAlert(ChangeUserInfoActivity.this, "该手机号已经被注册了！");
                                        }
                                    });
                                }

                            }else {

                                uiHandler.post(new Runnable() {
                                    @Override
                                    public void run() {

                                        Utilities.popUpAlert(ChangeUserInfoActivity.this, "验证码不正确，请重试");
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }else {

            submitHandler(phoneNum, name, sex, date, address);
            Log.d("personalInfo", phoneNum+name+sex+date+address);
        }
    }

    //获取验证码
    private void getCode(String phoneNum){

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

        RequestBody requestBody = RequestBody.create(JSON, json);

        Request request = new Request.Builder().url(API.getAPI()).post(requestBody).build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {


                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        Utilities.popUpAlert(ChangeUserInfoActivity.this, "出现某些未知错误，请稍后再试");
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

                                    codeEdittext.setText(validCode);
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

    private void submitHandler(String phoneHum, String name, String sex, String birth, String address){


        random32 = Utilities.getStringRandom(32);
        time10 = Utilities.get10Time();
        key64 = Utilities.get64Key(random32);


        if (image64.equals("")) {

            json = "{\n" +
                    "    \"validate_k\": \"1\",\n" +
                    "    \"params\": [\n" +
                    "        {\n" +
                    "            \"type\": \"Members\",\n" +
                    "            \"act\": \"Modify\",\n" +
                    "            \"para\": {\n" +
                    "                \"params\": {\n" +
                    "                    \"Addr\": \""+address+"\",\n"+
                    "                    \"Birthday\": \"" + birth + "\",\n" +
                    "                    \"d_Mid\": \"" + mid + "\",\n" +
                    "                    \"Head_img\": \"" + headImg + "\",\n" +
                    "                    \"Mname\": \"" + name + "\",\n" +
                    "                    \"Mobile\": \"" + phoneHum + "\",\n" +
                    "                    \"Sex\": \"" + sex + "\"\n" +
                    "                },\n" +
                    "                \"sign_valid\": {\n" +
                    "                    \"source\": \"Android\",\n" +
                    "                    \"non_str\": \"" + random32 + "\",\n" +
                    "                    \"stamp\": \"" + time10 + "\",\n" +
                    "                    \"signature\": \"" + Utilities.encode("Addr="+address + "Birthday=" + birth + "d_Mid=" + mid + "Head_img="+headImg+ "Mname=" + name + "Mobile=" + phoneHum + "Sex=" + sex + "non_str=" + random32 + "stamp=" + time10 + "keySecret=" + key64) + "\"\n" +
                    "                }\n" +
                    "            }\n" +
                    "        }\n" +
                    "    ]\n" +
                    "}";
        }else {

            json = "{\n" +
                    "    \"validate_k\": \"1\",\n" +
                    "    \"params\": [\n" +
                    "        {\n" +
                    "            \"type\": \"Members\",\n" +
                    "            \"act\": \"Modify\",\n" +
                    "            \"para\": {\n" +
                    "                \"params\": {\n" +
                    "                    \"Addr\": \""+address+"\",\n"+
                    "                    \"Birthday\": \"" + birth + "\",\n" +
                    "                    \"d_Mid\": \"" + mid + "\",\n" +
                    "                    \"Head_img\": \"" + image64 + "\",\n" +
                    "                    \"Mname\": \"" + name + "\",\n" +
                    "                    \"Mobile\": \"" + phoneHum + "\",\n" +
                    "                    \"Sex\": \"" + sex + "\"\n" +
                    "                },\n" +
                    "                \"sign_valid\": {\n" +
                    "                    \"source\": \"Android\",\n" +
                    "                    \"non_str\": \"" + random32 + "\",\n" +
                    "                    \"stamp\": \"" + time10 + "\",\n" +
                    "                    \"signature\": \"" + Utilities.encode("Addr="+address+"Birthday=" + birth + "d_Mid=" + mid + "Head_img="+image64+ "Mname=" + name + "Mobile=" + phoneHum + "Sex=" + sex + "non_str=" + random32 + "stamp=" + time10 + "keySecret=" + key64) + "\"\n" +
                    "                }\n" +
                    "            }\n" +
                    "        }\n" +
                    "    ]\n" +
                    "}";
        }

        OkHttpClient okHttpClient = new OkHttpClient();

        RequestBody requestBody = RequestBody.create(JSON, json);

        Request request = new Request.Builder().url(API.getAPI()).post(requestBody).build();

        okHttpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {

                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        Utilities.popUpAlert(ChangeUserInfoActivity.this, "出现某些未知错误，请稍后重试");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.body() != null){

                    try {

                        JSONObject jsonObject = new JSONObject(response.body().string());

                        Log.d("changePersonal", jsonObject.toString());

                        final String result = jsonObject.getJSONArray("result").getJSONObject(0).getString("error");

                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {

                                if (result.equals("SUCCESS")){

                                    Toast.makeText(ChangeUserInfoActivity.this, "更改成功", Toast.LENGTH_SHORT).show();

                                    broadcastIntent = new Intent("com.changeUser.mybroadcast.MY_BROADCAST");
                                    localBroadcastManager = LocalBroadcastManager.getInstance(ChangeUserInfoActivity.this);
                                    localBroadcastManager.sendBroadcast(broadcastIntent);
                                    ChangeUserInfoActivity.this.finish();
                                }else {
                                    Toast.makeText(ChangeUserInfoActivity.this, "出了点小差错，更改失败", Toast.LENGTH_SHORT).show();
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
    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == CONSULT_DOC_PICTURE){
            if (resultCode == RESULT_OK && data != null){

                decodeUri(data.getData());
            }
        }
    }

    //压缩图片
    public void decodeUri(Uri uri) {
        ParcelFileDescriptor parcelFD = null;
        try {
            parcelFD = this.getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor imageSource = parcelFD != null ? parcelFD.getFileDescriptor() : null;

            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeFileDescriptor(imageSource, null, o);

            final int REQUIRED_SIZE = 1024;

            //剪裁图片
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp < REQUIRED_SIZE && height_tmp < REQUIRED_SIZE) {
                    break;
                }
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            //压缩图片
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            Bitmap bitmap = BitmapFactory.decodeFileDescriptor(imageSource, null, o2);

            userAvatar.setImageBitmap(bitmap);

            ByteArrayOutputStream bos=new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, bos);//参数100表示不压缩

            byte[] bytes=bos.toByteArray();

            image64 = ("data:image/jpeg;base64,"+ Base64.encodeToString(bytes, Base64.NO_WRAP));

        } catch (FileNotFoundException e) {
            // handle errors
        } finally {
            if (parcelFD != null)
                try {
                    parcelFD.close();
                } catch (IOException e) {
                    // ignored
                }
        }
    }

}
