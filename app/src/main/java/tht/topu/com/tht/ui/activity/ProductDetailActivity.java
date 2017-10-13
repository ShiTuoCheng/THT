package tht.topu.com.tht.ui.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import tht.topu.com.tht.R;

public class ProductDetailActivity extends AppCompatActivity {

    private WebView productDetailWebView;
    private LinearLayout loadingLayout;

    private String mid;
    private String mdid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        Bundle bundle = getIntent().getBundleExtra("bundleData");
        mid = bundle.getString("mid");
        mdid = bundle.getString("mdid");

        initView();
    }

    private void initView(){

        productDetailWebView = (WebView)findViewById(R.id.productDetailWebView);
        WebSettings webSettings = productDetailWebView.getSettings();
        loadingLayout = (LinearLayout)findViewById(R.id.loadingLayout);

        Log.d("mdid,mid", mid+" "+mdid);

        webSettings.setJavaScriptEnabled(true);

        productDetailWebView.setWebViewClient(new WebViewClient(){

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {


                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                loadingLayout.setVisibility(View.GONE);
                productDetailWebView.setVisibility(View.VISIBLE);
            }
        });
    }
}
