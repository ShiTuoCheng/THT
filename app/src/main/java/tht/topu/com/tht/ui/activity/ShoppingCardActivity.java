package tht.topu.com.tht.ui.activity;

import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import okhttp3.MediaType;
import tht.topu.com.tht.R;

public class ShoppingCardActivity extends AppCompatActivity {

    private Handler uiHandler;
    private LinearLayout loadingLayout;
    private TextView password_title;
    private ImageView back;
    //json请求
    public static final MediaType JSON = MediaType
            .parse("application/json; charset=utf-8");

    private static final String MID_KEY = "1x11";

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_card);

        initView();

        SharedPreferences sharedPreferences = getSharedPreferences("TokenData", MODE_PRIVATE);
        String mid = sharedPreferences.getString(MID_KEY, "");

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ShoppingCardActivity.this.finish();
            }
        });

        webView.getSettings().setJavaScriptEnabled(true);

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

        webView.loadUrl("http://tht.65276588.cn/f/ShoppingCart.aspx?Mid="+mid+"&Stem_from=2");
    }

    private void initView(){

        webView = (WebView)findViewById(R.id.shoppingCardWebView);
        loadingLayout = (LinearLayout)findViewById(R.id.loadingLayout);
        back = (ImageView)findViewById(R.id.forumPostBack);
        password_title = (TextView)findViewById(R.id.password_title);
    }
}
