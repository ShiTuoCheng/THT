package tht.topu.com.tht.ui.activity;

import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import tht.topu.com.tht.R;
import tht.topu.com.tht.utils.Utilities;

public class SplashActivity extends AppCompatActivity {

    private ImageView imageView;
    private Handler uihandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        uihandler = new Handler(getMainLooper());

        initView();
        Glide.with(this).load(R.drawable.login).into(imageView);

        uihandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                SplashActivity.this.finish();
                Utilities.jumpToActivity(SplashActivity.this, MainActivity.class);
            }
        }, 1000);

    }

    private void initView(){

        imageView = (ImageView)findViewById(R.id.splashImageView);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        imageView = null;
    }
//
//    private void initData(){
//
//        random32 = Utilities.getStringRandom(32);
//        time10 = Utilities.get10Time();
//        key64 = Utilities.get64Key(random32);
//
//        String json = "{\n" +
//                "    \"validate_k\": \"1\",\n" +
//                "    \"params\": [\n" +
//                "        {\n" +
//                "            \"type\": \"Advertise\",\n" +
//                "            \"act\": \"Select_Detail\",\n" +
//                "            \"para\": {\n" +
//                "                \"params\": {\n" +
//                "                    \"d_Aid\": \"1\",\n" +
//                "                    \"s_Total_parameter\": \"Aid,Atitle,Url,Pic1\"\n" +
//                "                },\n" +
//                "                \"sign_valid\": {\n" +
//                "                    \"source\": \"Android\",\n" +
//                "                    \"non_str\": \""+random32+"\",\n" +
//                "                    \"stamp\": \""+time10+"\",\n" +
//                "                    \"signature\": \""+Utilities.encode("d_Aid=1"+"s_Total_parameter=Aid,Atitle,Url,Pic1"+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
//                "                }\n" +
//                "            }\n" +
//                "        }\n" +
//                "    ]\n" +
//                "}";
//
//        OkHttpClient okHttpClient = new OkHttpClient();
//
//        RequestBody requestBody = RequestBody.create(JSON, json);
//
//        Request request = new Request.Builder().url(API.getAPI()).post(requestBody).build();
//
//        okHttpClient.newCall(request).enqueue(new Callback() {
//
//            @Override
//            public void onFailure(Call call, IOException e) {
//
//                uiHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//
//                        Toast.makeText(SplashActivity.this, "获取资源失败,请重试", Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//
//                if (response.body() != null){
//
//                    try {
//
//                        JSONObject jsonObj = new JSONObject(response.body().string());
//                        final String imageUrl = jsonObj.getJSONArray("result").getJSONObject(0).getJSONObject("detail").getString("Pic1");
//                        final String url = jsonObj.getJSONArray("result").getJSONObject(0).getJSONObject("detail").getString("Url");
//
//                        uiHandler.post(new Runnable() {
//                            @Override
//                            public void run() {
//
//                                Glide.with(SplashActivity.this).load(API.getHostName()+imageUrl).into(imageView);
//
//                                imageView.setOnClickListener(new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View view) {
//
//                                        Intent intent = new Intent(SplashActivity.this, WebViewActivity.class);
//                                        intent.putExtra("url", url);
//
//                                        //定时器取消
//                                        countDownTimer.cancel();
//                                        startActivity(intent);
//                                    }
//                                });
//                            }
//                        });
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//
//                }
//            }
//        });
//    }
}
