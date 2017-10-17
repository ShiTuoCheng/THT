package tht.topu.com.tht.ui.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import tht.topu.com.tht.R;

public class WebViewActivity extends AppCompatActivity {

    private String url;
    private WebView webView;
    private LinearLayout loadingLayout;

    private TextView password_title;
    private ImageView forumPostBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        url = getIntent().getStringExtra("url");
        initView();

        webView.loadUrl(url);
        webView.setWebViewClient(new WebViewClient(){

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                loadingLayout.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
            }

        });

        webView.setWebChromeClient(new WebChromeClient(){

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);

                password_title.setText(title);
            }
        });

        forumPostBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                WebViewActivity.this.finish();
            }
        });
    }

    private void initView(){

        webView = (WebView)findViewById(R.id.webView);
        loadingLayout = (LinearLayout)findViewById(R.id.loadingLayout);
        password_title = (TextView)findViewById(R.id.password_title);
        forumPostBack = (ImageView)findViewById(R.id.forumPostBack);
    }
}
