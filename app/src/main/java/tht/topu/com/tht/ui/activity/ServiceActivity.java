package tht.topu.com.tht.ui.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import okhttp3.MediaType;
import tht.topu.com.tht.R;

public class ServiceActivity extends AppCompatActivity {

    public static final MediaType JSON = MediaType
            .parse("application/json; charset=utf-8");

    private static final String TOKEN_KEY = "0x01";
    private static final String MID_KEY = "1x11";
    private WebView serviceWebView;
    private ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);

        initView();

        SharedPreferences sharedPreferences = getSharedPreferences("TokenData", Context.MODE_PRIVATE);
        String mid = sharedPreferences.getString(MID_KEY, "");

        serviceWebView.loadUrl("http://chat.taohuantang.com.cn?cid="+mid);

        Log.d("service", "http://chat.taohuantang.com.cn?cid="+mid);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ServiceActivity.this.finish();
            }
        });
    }

    private void initView() {

        serviceWebView = (WebView)findViewById(R.id.serviceWebView);
        serviceWebView.getSettings().setJavaScriptEnabled(true);
        serviceWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                return false;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request)
            {

                return false;
            }

        });
        back = (ImageView)findViewById(R.id.userFavoriteBack);
    }
}
