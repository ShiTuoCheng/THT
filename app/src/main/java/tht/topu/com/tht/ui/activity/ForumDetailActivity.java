package tht.topu.com.tht.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import tht.topu.com.tht.R;

public class ForumDetailActivity extends AppCompatActivity {

    private WebView forumDetailWebView;
    private LinearLayout loadingLayout;
    private ImageView back;
    private LocalBroadcastManager localBroadcastManager;
    private Intent broadcastIntent;

    private static final String MID_KEY = "1x11";

    private String flid;
    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum_detail);

        initView();

        Bundle bundle = getIntent().getBundleExtra("forumBundle");

        String fid = bundle.getString("fid");
        flid = bundle.getString("flid");

        localBroadcastManager = LocalBroadcastManager.getInstance(this);

        SharedPreferences sharedPreferences = getSharedPreferences("TokenData", MODE_PRIVATE);
        String mid = sharedPreferences.getString(MID_KEY, "");

        forumDetailWebView.getSettings().setJavaScriptEnabled(true);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ForumDetailActivity.this.finish();
            }
        });

        forumDetailWebView.addJavascriptInterface(this, "pageApp");
        forumDetailWebView.setWebViewClient(new WebViewClient(){

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                loadingLayout.setVisibility(View.GONE);
                forumDetailWebView.setVisibility(View.VISIBLE);
            }
        });

        forumDetailWebView.loadUrl("http://tht.65276588.cn/f/ForumDetail.aspx?Fid="+fid+"&Mid="+mid+"&Stem_from=2");
    }

    private void initView(){

        loadingLayout = (LinearLayout)findViewById(R.id.loadingLayout);
        forumDetailWebView = (WebView)findViewById(R.id.forumDetailWebView);
        back = (ImageView)findViewById(R.id.forumPostBack);
    }

    @JavascriptInterface
    public void finishPage() {
        forumDetailWebView.post(new Runnable() {
            @Override
            public void run() {
                //默认发送第一次广播
                broadcastIntent = new Intent("com.example.mybroadcast.MY_BROADCAST");
                broadcastIntent.putExtra("Flid", flid);
                localBroadcastManager.sendBroadcast(broadcastIntent);
                ForumDetailActivity.this.finish();
            }
        });
    }
}
