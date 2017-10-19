package tht.topu.com.tht.ui.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import tht.topu.com.tht.utils.API;
import tht.topu.com.tht.utils.Utilities;

public class DrawRulesActivity extends AppCompatActivity {

    private ImageView drawRulesImageView;
    private WebView drawRulesWebView;
    private ImageView backImageView;

    private RelativeLayout drawLayout;

    private Handler alertHandler;
    private Handler uiHandler;

    private String random32;
    private String time10;
    private String key64;

    private LinearLayout loadingLayout;

    public static final MediaType JSON = MediaType
            .parse("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw_rules);

        initView();

        backImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DrawRulesActivity.this.finish();
            }
        });

        uiHandler = new Handler(Looper.getMainLooper());

        alertHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {

                    case 0:

                        Snackbar.make(drawLayout, "网络不可用", Snackbar.LENGTH_LONG).setAction("去设置", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                            }
                        }).show();

                        break;

                    case 1:
                        Toast toast = Toast.makeText(DrawRulesActivity.this.getApplicationContext(), "出现某些未知错误，请稍后再试", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.NO_GRAVITY, 0, Utilities.dip2px(DrawRulesActivity.this.getApplicationContext(), 200));
                        toast.show();

                        break;
                }
            }
        };

        if (!Utilities.isNetworkAvaliable(DrawRulesActivity.this.getApplicationContext())){

            alertHandler.sendEmptyMessageDelayed(0,1000);
        }else {

            drawRulesWebView.loadUrl("http://tht.65276588.cn/f/Activity_rules.aspx?Iid=6");
            drawRulesWebView.setWebViewClient(new WebViewClient(){

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    return super.shouldOverrideUrlLoading(view, url);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);

                    loadingLayout.setVisibility(View.GONE);
                    drawRulesWebView.setVisibility(View.VISIBLE);
                    drawRulesImageView.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private void initView(){
        drawRulesWebView = (WebView)findViewById(R.id.drawRulesWebView);
        backImageView = (ImageView)findViewById(R.id.drawRulesBack);
        drawLayout = (RelativeLayout)findViewById(R.id.drawLayout);
        loadingLayout = (LinearLayout)findViewById(R.id.loadingLayout);
        drawRulesImageView = (ImageView)findViewById(R.id.drawRulesImageView);

        drawRulesWebView.getSettings().setDefaultTextEncodingName("UTF-8") ;
    }

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
//                "            \"type\": \"Info\",\n" +
//                "            \"act\": \"Select_List\",\n" +
//                "            \"para\": {\n" +
//                "                \"params\": {\n" +
//                "                    \"s_Iid\": \"6\"\n" +
//                "                },\n" +
//                "                \"sign_valid\": {\n" +
//                "                    \"source\": \"Android\",\n" +
//                "                    \"non_str\": \""+random32+"\",\n" +
//                "                    \"stamp\": \""+time10+"\",\n" +
//                "                    \"signature\": \""+Utilities.encode("s_Iid=6"+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
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
//                if (uiHandler != null){
//
//                    uiHandler.post(new Runnable() {
//                        @Override
//                        public void run() {
//
//                            alertHandler.sendEmptyMessageDelayed(1,1000);
//                        }
//                    });
//                }
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//
//                try {
//
//                    JSONObject jsonObject = new JSONObject(response.body().string());
//                    JSONArray recordArr = jsonObject.getJSONArray("result").getJSONObject(0).getJSONArray("list");
//
//                    final String drawInfo = recordArr.getJSONObject(0).getString("Iinfo");
//
//                    uiHandler.post(new Runnable() {
//                        @Override
//                        public void run() {
//
//                            drawRulesWebView.loadDataWithBaseURL(null, drawInfo,"text/html","utf-8", null);
//                            loadingLayout.setVisibility(View.INVISIBLE);
//
//                            drawRulesImageView.setVisibility(View.VISIBLE);
//                            drawRulesWebView.setVisibility(View.VISIBLE);
//                        }
//                    });
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (alertHandler != null){

            alertHandler = null;
        }

        if (uiHandler != null){

            uiHandler = null;
        }
    }
}
