package tht.topu.com.tht.ui.activity;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;

import tht.topu.com.tht.R;

public class OrderListActivity extends AppCompatActivity {

    private WebView orderListWebView;
    private LinearLayout loadingLayout;

    private ImageView back;

    private int Ostatus = 0;
    private static final String MID_KEY = "1x11";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list);

        SharedPreferences sharedPreferences = getSharedPreferences("TokenData", MODE_PRIVATE);
        String mid = sharedPreferences.getString(MID_KEY, "");

        initView();

        Bundle bundle = getIntent().getBundleExtra("bundleOstatus");
        Ostatus = bundle.getInt("ostatus");

        orderListWebView.getSettings().setJavaScriptEnabled(true);
        orderListWebView.setWebViewClient(new WebViewClient(){

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                loadingLayout.setVisibility(View.GONE);
                orderListWebView.setVisibility(View.VISIBLE);
            }
        });

        orderListWebView.loadUrl("http://tht.65276588.cn/f/MyOrder.aspx?Ostatus="+Ostatus+"&Mid="+mid+"&Stem_from=2");

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                OrderListActivity.this.finish();
            }
        });
    }

    private void initView(){

        loadingLayout = (LinearLayout)findViewById(R.id.loadingLayout);
        orderListWebView = (WebView)findViewById(R.id.orderListWebView);
        back = (ImageView)findViewById(R.id.forumPostBack);
    }
}
