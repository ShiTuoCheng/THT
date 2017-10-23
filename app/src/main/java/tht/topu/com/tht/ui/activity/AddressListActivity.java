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

public class AddressListActivity extends AppCompatActivity {

    private WebView addressListWebView;
    private LinearLayout loadingLayout;
    private ImageView back;
    private static final String MID_KEY = "1x11";

    private String mid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_list);

        initView();

        SharedPreferences sharedPreferences = getSharedPreferences("TokenData", MODE_PRIVATE);
        mid = sharedPreferences.getString(MID_KEY, "");

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AddressListActivity.this.finish();
            }
        });

        addressListWebView.getSettings().setJavaScriptEnabled(true);

        addressListWebView.setWebViewClient(new WebViewClient(){

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                loadingLayout.setVisibility(View.GONE);
                addressListWebView.setVisibility(View.VISIBLE);
            }
        });

        addressListWebView.loadUrl("http://tht.65276588.cn/f/AddrssList.aspx?Mid="+mid+"&Stem_from=2");
    }

    private void initView(){

        addressListWebView = (WebView)findViewById(R.id.addressListWebView);
        loadingLayout = (LinearLayout)findViewById(R.id.loadingLayout);
        back = (ImageView)findViewById(R.id.forumPostBack);
    }
}
