package tht.topu.com.tht.ui.activity;

import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v4.view.ViewPager;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import tht.topu.com.tht.R;
import tht.topu.com.tht.adapter.MainViewPagerAdapter;
import tht.topu.com.tht.ui.base.BaseActivity;
import tht.topu.com.tht.ui.fragment.BlankFragment;
import tht.topu.com.tht.utils.Utilities;

public class MainIntroActivity extends AppCompatActivity {

    private Runnable jumpRunnable;
    private ViewPager introViewPager;
    private LinearLayout linearLayout;
    private Handler handler;
    private static final String IS_IN = "0x22";
    private int current_circle = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences sharedPreferences = getSharedPreferences("splashData", MODE_PRIVATE);

        if (sharedPreferences.getBoolean(IS_IN, false)){
            Utilities.jumpToActivity(this, LoginActivity.class);
        }
        super.onCreate(savedInstanceState);

        setContentView(bindLayout());

        SharedPreferences.Editor editor = this.getSharedPreferences("splashData", MODE_PRIVATE).edit();
        editor.putBoolean(IS_IN, true);
        editor.apply();

        initView();
    }

    public int bindLayout() {
        return R.layout.activity_intro;
    }

    public void initView() {


        introViewPager = (ViewPager)findViewById(R.id.introViewPager);

        linearLayout = (LinearLayout)findViewById(R.id.dot_container);

        for(int i = 0;i<3;i++){
            ImageView iv = new ImageView(MainIntroActivity.this);
            //循环创建小圆点，判断第一个小圆点为白色的，其他的都是透明的
            if(i == 0){
                iv.setBackgroundResource(R.drawable.dot_focus);
            }else{
                iv.setBackgroundResource(R.drawable.dot_unfocus);
            }

            linearLayout.addView(iv);

            //设置小圆点的margin值
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(30, 30);
            lp.setMargins(5, 10, 5, 10);
            iv.setLayoutParams(lp);
        }

        MainViewPagerAdapter mainViewPagerAdapter = new MainViewPagerAdapter(getSupportFragmentManager());

        mainViewPagerAdapter.addFragment(new BlankFragment());
        mainViewPagerAdapter.addFragment(new BlankFragment());
        mainViewPagerAdapter.addFragment(new BlankFragment());

        introViewPager.setAdapter(mainViewPagerAdapter);

        introViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                View v = linearLayout.getChildAt(position);
                View cuview = linearLayout.getChildAt(current_circle);

                if(v != null && cuview != null){
                    ImageView pointView = (ImageView) v;
                    ImageView curpointView = (ImageView) cuview;

                    curpointView.setBackgroundResource(R.drawable.dot_unfocus);
                    pointView.setBackgroundResource(R.drawable.dot_focus);
                    current_circle = position;
                }

                if (position == 2){

                    handler = new Handler(getMainLooper()); //handler切到主线程
                    jumpRunnable = new Runnable() {
                        @Override
                        public void run() {

                            MainIntroActivity.this.finish();
                            Utilities.jumpToActivity(MainIntroActivity.this, LoginActivity.class);
                        }
                    };

                    handler.postDelayed(jumpRunnable, 1000);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //回收handler内存防止内存泄漏
        if (handler != null){

            handler.removeCallbacks(jumpRunnable);
            handler = null;
        }
    }
}
