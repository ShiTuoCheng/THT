package tht.topu.com.tht.ui.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import tht.topu.com.tht.R;

public class ProductDetailActivity extends AppCompatActivity {

    private WebView productDetailWebView;
    private LinearLayout loadingLayout;
    private ImageView back;
    private TextView password_title;

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

        productDetailWebView.loadUrl("http://tht.65276588.cn/f/Mdse_detail.aspx?Mid="+mdid+"&Mids="+mid+"&Stem_from=2");
    }

    private void initView(){

        productDetailWebView = (WebView)findViewById(R.id.productDetailWebView);
        WebSettings webSettings = productDetailWebView.getSettings();
        loadingLayout = (LinearLayout)findViewById(R.id.loadingLayout);
        back = (ImageView)findViewById(R.id.forumPostBack);
        password_title = (TextView)findViewById(R.id.password_title);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ProductDetailActivity.this.finish();
            }
        });

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

        productDetailWebView.setWebChromeClient(new WebChromeClient(){

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);

                password_title.setText(title);
            }
        });
    }
}
