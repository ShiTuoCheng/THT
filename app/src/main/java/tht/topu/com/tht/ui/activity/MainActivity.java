package tht.topu.com.tht.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import jp.wasabeef.glide.transformations.CropCircleTransformation;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import tht.topu.com.tht.R;
import tht.topu.com.tht.adapter.MainViewPagerAdapter;
import tht.topu.com.tht.ui.base.BaseActivity;
import tht.topu.com.tht.ui.fragment.BlankFragment;
import tht.topu.com.tht.ui.fragment.drawFragment.DrawFragment;
import tht.topu.com.tht.ui.fragment.forumFragment.ForumFragment;
import tht.topu.com.tht.ui.fragment.mainFragment.MainFragment;
import tht.topu.com.tht.ui.fragment.personalFragment.PersonalFragment;
import tht.topu.com.tht.ui.fragment.rankFragment.RankFragment;
import tht.topu.com.tht.utils.API;
import tht.topu.com.tht.utils.Utilities;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class MainActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private MenuItem prevMenuItem;

    private String random32;
    private String time10;
    private String key64;

    private static final String MID_KEY = "1x11";
    private static final String TOKEN_KEY = "0x01";
    private Handler uiHandler;

    public static final MediaType JSON = MediaType
            .parse("application/json; charset=utf-8");

    private boolean isCheckToken = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(bindLayout());

        uiHandler = new Handler(getMainLooper());
        initView();

    }

    //绑定布局
    public int bindLayout() {
        return R.layout.activity_main;
    }

    //初始化view
    public void initView() {

        final BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);

        viewPager = (ViewPager)findViewById(R.id.main_viewPager);

        viewPager.setOffscreenPageLimit(4);

        BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
                =   new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.product:

                        viewPager.setCurrentItem(0);
                        item.setIcon(R.mipmap.selected_product);
                        return true;
                    case R.id.forum:

                        viewPager.setCurrentItem(1);
                        item.setIcon(R.mipmap.selected_forum);
                        return true;
                    case R.id.draw:

                        viewPager.setCurrentItem(2);
                        item.setIcon(R.mipmap.selected_draw);
                        return true;
                    case R.id.rank:

                        viewPager.setCurrentItem(3);
                        item.setIcon(R.mipmap.selected_rank);
                        return true;
                    case R.id.user:

                        viewPager.setCurrentItem(4);
                        item.setIcon(R.mipmap.selected_user);
                        return true;
                }
                return false;
            }

        };
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        Utilities.disableShiftMode(navigation);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (prevMenuItem != null) {
                    prevMenuItem.setChecked(false);
                } else {
                    navigation.getMenu().getItem(0).setChecked(false);
                }
                navigation.getMenu().getItem(position).setChecked(true);
                prevMenuItem = navigation.getMenu().getItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        setupViewPager(viewPager);

        validToken(true);
    }

    private void setupViewPager(ViewPager viewPager) {
        MainViewPagerAdapter adapter = new MainViewPagerAdapter(getSupportFragmentManager());

        //引入其他页面
        adapter.addFragment(new MainFragment()); //宝贝
        adapter.addFragment(new ForumFragment()); //论坛
        adapter.addFragment(DrawFragment.newInstance(MainActivity.this.getApplicationContext())); //抽奖
        adapter.addFragment(new RankFragment()); //排行榜
        adapter.addFragment(new PersonalFragment()); //我的
        viewPager.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isCheckToken){

            validToken(false);
        }

        isCheckToken = false;
    }

    //验证token
    private void validToken(boolean isFirst){

        random32 = Utilities.getStringRandom(32);
        time10 = Utilities.get10Time();
        key64 = Utilities.get64Key(random32);

        isCheckToken = true;

        final SharedPreferences sharedPreferences = getSharedPreferences("TokenData", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString(TOKEN_KEY, "");
        String mid = sharedPreferences.getString(MID_KEY, "");

        if (token.equals("") && mid.equals("")){

            Toast.makeText(this, "登录过期，请重新登录", Toast.LENGTH_SHORT).show();

            MainActivity.this.finish();
            //跳转到登录页面
            Utilities.jumpToActivity(MainActivity.this, LoginActivity.class);
            return;
        }

        final String json;

        if (isFirst){
            json = "{\n" +
                    "    \"validate_k\": \"1\",\n" +
                    "    \"params\": [\n" +
                    "        {\n" +
                    "            \"type\": \"Members\",\n" +
                    "            \"act\": \"Select_Detail\",\n" +
                    "            \"para\": {\n" +
                    "                \"params\": {\n" +
                    "                    \"d_Alive\": \"\",\n" +
                    "                    \"d_IsUpdateToken\": \"1\",\n"+
                    "                    \"d_Mid\": \""+mid+"\",\n" +
                    "                    \"d_Token\": \""+token+"\",\n" +
                    "                    \"s_Total_parameter\": \"Mid,OpenID,Nickname,Head_img,Mname,Sex,Mobile,Rdate,Birthday,Alive,Gag,The_sun,Members_LV,Orders_count,Integral,card,Passwd,Addr,Stem_from,Token,Token_expiry,Token_IP,Referee_Mid,Referee2_Mid,Referee,Referee2,Reward,Withdraw,Contribution_Award,Balance\"\n" +
                    "                },\n" +
                    "                \"sign_valid\": {\n" +
                    "                    \"source\": \"Android\",\n" +
                    "                    \"non_str\": \""+random32+"\",\n" +
                    "                    \"stamp\": \""+time10+"\",\n" +
                    "                    \"signature\": \""+Utilities.encode("d_Alive="+"d_IsUpdateToken=1"+"d_Mid="+mid+"d_Token="+token+"s_Total_parameter=Mid,OpenID,Nickname,Head_img,Mname,Sex,Mobile,Rdate,Birthday,Alive,Gag,The_sun,Members_LV,Orders_count,Integral,card,Passwd,Addr,Stem_from,Token,Token_expiry,Token_IP,Referee_Mid,Referee2_Mid,Referee,Referee2,Reward,Withdraw,Contribution_Award,Balance"+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
                    "                }\n" +
                    "            }\n" +
                    "        }\n" +
                    "    ]\n" +
                    "}";
        }else{
            json = "{\n" +
                    "    \"validate_k\": \"1\",\n" +
                    "    \"params\": [\n" +
                    "        {\n" +
                    "            \"type\": \"Members\",\n" +
                    "            \"act\": \"Select_Detail\",\n" +
                    "            \"para\": {\n" +
                    "                \"params\": {\n" +
                    "                    \"d_Alive\": \"\",\n" +
                    "                    \"d_IsUpdateToken\": \"0\",\n"+
                    "                    \"d_Mid\": \""+mid+"\",\n" +
                    "                    \"d_Token\": \""+token+"\",\n" +
                    "                    \"s_Total_parameter\": \"Mid,OpenID,Nickname,Head_img,Mname,Sex,Mobile,Rdate,Birthday,Alive,Gag,The_sun,Members_LV,Orders_count,Integral,card,Passwd,Addr,Stem_from,Token,Token_expiry,Token_IP,Referee_Mid,Referee2_Mid,Referee,Referee2,Reward,Withdraw,Contribution_Award,Balance\"\n" +
                    "                },\n" +
                    "                \"sign_valid\": {\n" +
                    "                    \"source\": \"Android\",\n" +
                    "                    \"non_str\": \""+random32+"\",\n" +
                    "                    \"stamp\": \""+time10+"\",\n" +
                    "                    \"signature\": \""+Utilities.encode("d_Alive="+"d_IsUpdateToken=0"+"d_Mid="+mid+"d_Token="+token+"s_Total_parameter=Mid,OpenID,Nickname,Head_img,Mname,Sex,Mobile,Rdate,Birthday,Alive,Gag,The_sun,Members_LV,Orders_count,Integral,card,Passwd,Addr,Stem_from,Token,Token_expiry,Token_IP,Referee_Mid,Referee2_Mid,Referee,Referee2,Reward,Withdraw,Contribution_Award,Balance"+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
                    "                }\n" +
                    "            }\n" +
                    "        }\n" +
                    "    ]\n" +
                    "}";
        }

        OkHttpClient okHttpClient = new OkHttpClient();

        RequestBody requestBody = RequestBody.create(JSON, json);
        Request request = new Request.Builder().url(API.getAPI()).post(requestBody).build();

        okHttpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {

                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        Utilities.popUpAlert(MainActivity.this, "出现某些未知错误，请稍后再试");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.body() != null){

                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());

                        final String nickName = jsonObject.getJSONArray("result").getJSONObject(0).getJSONObject("detail").getString("Nickname");
                        final String newToken = jsonObject.getJSONArray("result").getJSONObject(0).getJSONObject("detail").getString("Token");
                        final int grade = jsonObject.getJSONArray("result").getJSONObject(0).getJSONObject("detail").getJSONObject("card").getInt("Grade");

                        Log.d("login", jsonObject.toString());

                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {

                                if (nickName.equals("")){

                                    Toast.makeText(MainActivity.this, "登录过期，请重新登录", Toast.LENGTH_LONG).show();
                                    SharedPreferences.Editor editor = MainActivity.this.getSharedPreferences("TokenData", Context.MODE_PRIVATE).edit();
                                    editor.putString(TOKEN_KEY, "");
                                    editor.putString(MID_KEY, "");
                                    editor.apply();
//
                                    MainActivity.this.finish();
                                    Utilities.jumpToActivity(MainActivity.this, LoginActivity.class);
                                    return;
                                }else {
                                    
                                    if (grade == 0){

                                        Toast.makeText(MainActivity.this, "会员已到期，请续费再来使用", Toast.LENGTH_SHORT).show();

                                        Intent intent = new Intent(MainActivity.this, CardActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);

                                        return;
                                    }

                                    SharedPreferences.Editor editor = MainActivity.this.getSharedPreferences("TokenData", Context.MODE_PRIVATE).edit();
                                    editor.putString(TOKEN_KEY, newToken);
                                    editor.apply();
                                }

                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
