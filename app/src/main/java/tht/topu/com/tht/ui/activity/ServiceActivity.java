package tht.topu.com.tht.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import java.net.URL;

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

        serviceWebView.loadUrl("http://chat.taohuantang.com.cn?cid="+mid+"&from=2");

        Log.d("service", "http://chat.taohuantang.com.cn?cid="+mid);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ServiceActivity.this.finish();
            }
        });
    }

    @JavascriptInterface
    public void closeServer() {
        ServiceActivity.this.finish();
    }

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    private void initView() {

        serviceWebView = (WebView)findViewById(R.id.serviceWebView);
        serviceWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        serviceWebView.getSettings().setJavaScriptEnabled(true);
        serviceWebView.getSettings().setSupportZoom(true);
        serviceWebView.getSettings().setBuiltInZoomControls(true);
        serviceWebView.getSettings().setUseWideViewPort(true);
        serviceWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        serviceWebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        serviceWebView.getSettings().setLoadWithOverviewMode(true);
        serviceWebView.getSettings().setAppCacheEnabled(true);
        serviceWebView.getSettings().setDomStorageEnabled(true);
        serviceWebView.setWebViewClient(new WebViewClient());
        serviceWebView.addJavascriptInterface(this, "server");
        back = (ImageView)findViewById(R.id.userFavoriteBack);
    }
}
