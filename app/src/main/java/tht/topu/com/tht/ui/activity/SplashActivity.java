package tht.topu.com.tht.ui.activity;

import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import tht.topu.com.tht.R;
import tht.topu.com.tht.utils.API;
import tht.topu.com.tht.utils.Utilities;

import static tht.topu.com.tht.ui.activity.SignActivity.JSON;

public class SplashActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView skipTextView;
    private String random32;
    private String time10;
    private String key64;

    private boolean isFinish = false;

    private Handler uiHandler;

    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        uiHandler = new Handler(Looper.getMainLooper());
        initView();

        initData();
    }

    private void initView(){

        imageView = (ImageView)findViewById(R.id.splashImageView);
        skipTextView = (TextView)findViewById(R.id.skipTextView);

        countDownTimer = new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long l) {

                skipTextView.setText("跳过"+ l/1000 +"s");
            }

            @Override
            public void onFinish() {

                if (!isFinish){

                    SplashActivity.this.finish();
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        };

        countDownTimer.start();

        skipTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                isFinish = true;
                SplashActivity.this.finish();
                Intent intent = new Intent(SplashActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null){

            countDownTimer.cancel();
        }
    }

    private void initData(){

        random32 = Utilities.getStringRandom(32);
        time10 = Utilities.get10Time();
        key64 = Utilities.get64Key(random32);

        String json = "{\n" +
                "    \"validate_k\": \"1\",\n" +
                "    \"params\": [\n" +
                "        {\n" +
                "            \"type\": \"Advertise\",\n" +
                "            \"act\": \"Select_Detail\",\n" +
                "            \"para\": {\n" +
                "                \"params\": {\n" +
                "                    \"d_Aid\": \"1\",\n" +
                "                    \"s_Total_parameter\": \"Aid,Atitle,Url,Pic1\"\n" +
                "                },\n" +
                "                \"sign_valid\": {\n" +
                "                    \"source\": \"Android\",\n" +
                "                    \"non_str\": \""+random32+"\",\n" +
                "                    \"stamp\": \""+time10+"\",\n" +
                "                    \"signature\": \""+Utilities.encode("d_Aid=1"+"s_Total_parameter=Aid,Atitle,Url,Pic1"+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
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

                        Toast.makeText(SplashActivity.this, "获取资源失败,请重试", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.body() != null){

                    try {

                        JSONObject jsonObj = new JSONObject(response.body().string());
                        final String imageUrl = jsonObj.getJSONArray("result").getJSONObject(0).getJSONObject("detail").getString("Pic1");

                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {

                                Glide.with(SplashActivity.this).load(API.getHostName()+imageUrl).into(imageView);
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
