package tht.topu.com.tht.ui.fragment.personalFragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
import tht.topu.com.tht.adapter.PersonalCenterAdapter;
import tht.topu.com.tht.ui.activity.CardActivity;
import tht.topu.com.tht.ui.activity.ChangePasswordActivity;
import tht.topu.com.tht.ui.activity.ChangeUserInfoActivity;
import tht.topu.com.tht.ui.activity.LoginActivity;
import tht.topu.com.tht.ui.activity.OrderListActivity;
import tht.topu.com.tht.ui.activity.PasswordActivity;
import tht.topu.com.tht.ui.activity.ServiceActivity;
import tht.topu.com.tht.ui.activity.SettingActivity;
import tht.topu.com.tht.ui.activity.ShoppingCardActivity;
import tht.topu.com.tht.ui.activity.UserFavoriteActivity;
import tht.topu.com.tht.ui.activity.UserForumBlogActivity;
import tht.topu.com.tht.ui.fragment.forumFragment.ForumContentFragment;
import tht.topu.com.tht.utils.API;
import tht.topu.com.tht.utils.CircleImageView;
import tht.topu.com.tht.utils.Utilities;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

/**
 * Created by asus on 2017/8/7.
 * 个人中心
 */
public class PersonalFragment extends Fragment {
    //当前页面对象
    private View view;
    //GridView对象
    private GridView personal_gridview;
    //上部分relative,背景对象
    private RelativeLayout personal_top_relative;
    //右侧部分
    private RelativeLayout personal_rigint_relative;
    //右侧圆形
    private  RelativeLayout personal_rigint_oval;
    //右侧图片
    private ImageView personal_rigint_Icon;
    //右侧文字vip
    private TextView personal_vip;
    //右侧vip数量
    private  TextView personal_vip_num;
    //日期标题
    private  TextView personal_vdate_title;
    //日期值
    private  TextView personal_vdate_val;
    //积分relative
    private LinearLayout personal_integral_relative;
    //积分文字标题
    private  TextView  personal_integral_title;
    //积分的值
    private  TextView personal_integral_val;
    //昵称
    private  TextView personal_nickname;

    private RelativeLayout personal_list_collection_relative;
    private RelativeLayout personal_list_renew_relative;
    private RelativeLayout personal_list_cart_relative;
    private RelativeLayout personal_list_post_relative;
    private RelativeLayout personal_list_service_relative;

    private ImageView personal_background_ImageView;

    private CircleImageView personal_head_img;

    private SwipeRefreshLayout swipeRefreshLayout;

    private SwitchCompat sunButton;
    private ImageView settingButton;
    private LocalReceiver localReceiver;

    private String random32;
    private String time10;
    private String key64;

    private Handler uiHandler;
    private LocalBroadcastManager localBroadcastManager;
    private IntentFilter intentFilter;

    public static final MediaType JSON = MediaType
            .parse("application/json; charset=utf-8");

    private static final String TOKEN_KEY = "0x01";
    private static final String MID_KEY = "1x11";

    //九宫格中的图片银色的
    private int[] imgs2={R.mipmap.personal_all_order,R.mipmap.personal_pending_payment,R.mipmap.personal_shipment_pending,R.mipmap.personal_shipped };
    //金色的
    private int[] imgs={R.mipmap.personal_all_order_golden,R.mipmap.personal_pending_payment_golden,R.mipmap.personal_shipment_pending_golden,R.mipmap.personal_shipped_golden };



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //当前页面对象
        view = inflater.inflate(R.layout.fragment_personal, container,false);

        //初始化控件
        init();

        intentFilter = new IntentFilter();
        intentFilter.addAction("com.changeUser.mybroadcast.MY_BROADCAST");
        localReceiver = new LocalReceiver();
        localBroadcastManager = LocalBroadcastManager.getInstance(getActivity().getApplicationContext());
        localBroadcastManager.registerReceiver(localReceiver, intentFilter);

        uiHandler = new Handler(Looper.getMainLooper());

        if (!Utilities.isNetworkAvaliable(getActivity())){

            Utilities.popUpAlert(getActivity(), "网络不可用");

        }else {

            getUserDataInfo();
        }

        /*默认金色*/

        //背景图片
        PersonalCenterAdapter adapter= new PersonalCenterAdapter(view.getContext(),imgs,3);
        personal_gridview.setAdapter(adapter);
        return view;
    }

    //初始化控件
    private void init(){
        //GridView对象
        personal_gridview=(GridView) view.findViewById(R.id.personal_gridview);
        //上半部分relative
        personal_top_relative=(RelativeLayout) view.findViewById(R.id.personal_top_relative);
        //右半部分
        personal_rigint_relative=(RelativeLayout) view.findViewById(R.id.personal_rigint_relative);
        //右侧圆形
        personal_rigint_oval=(RelativeLayout) view.findViewById(R.id.personal_rigint_oval);
        //右侧图片
        personal_rigint_Icon=(ImageView) view.findViewById(R.id.personal_rigint_Icon);
        //右侧文字vip
        personal_vip=(TextView) view.findViewById(R.id.personal_vip);
        //右侧vip数量
        personal_vip_num=(TextView) view.findViewById(R.id.personal_vip_num);
        //日期标题
        personal_vdate_title=(TextView) view.findViewById(R.id.personal_vdate_title);
        //日期值
        personal_vdate_val=(TextView) view.findViewById(R.id.personal_vdate_val);
        //积分relative
        personal_integral_relative=(LinearLayout) view.findViewById(R.id.personal_integral_relative);
        //积分文字标题
        personal_integral_title=(TextView) view.findViewById(R.id.personal_integral_title);
        //积分的值
        personal_integral_val=(TextView) view.findViewById(R.id.personal_integral_val);
        //昵称
        personal_nickname=(TextView) view.findViewById(R.id.personal_nickname);
        personal_head_img = (CircleImageView)view.findViewById(R.id.personal_head_img);
        sunButton = (SwitchCompat)view.findViewById(R.id.personal_list_share_switch);
        settingButton = (ImageView)view.findViewById(R.id.setting);

        personal_list_collection_relative = (RelativeLayout)view.findViewById(R.id.personal_list_collection_relative);
        personal_list_renew_relative = (RelativeLayout)view.findViewById(R.id.personal_list_renew_relative);
        personal_list_post_relative = (RelativeLayout)view.findViewById(R.id.personal_list_post_relative);
        personal_list_cart_relative = view.findViewById(R.id.personal_list_cart_relative);
        personal_list_service_relative = view.findViewById(R.id.personal_list_service_relative);
        swipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.swipe_refresh_layout);

        personal_background_ImageView = (ImageView)view.findViewById(R.id.personal_background_ImageView);

        personal_head_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Utilities.jumpToActivity(getActivity(), ChangeUserInfoActivity.class);
            }
        });

        personal_list_renew_relative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Utilities.jumpToActivity(getActivity(), CardActivity.class);
            }
        });

        personal_list_post_relative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Utilities.jumpToActivity(getActivity(), UserForumBlogActivity.class);
            }
        });

        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Utilities.jumpToActivity(getActivity(), SettingActivity.class);
            }
        });

        personal_gridview.setOverScrollMode(View.OVER_SCROLL_NEVER);

        personal_list_collection_relative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Utilities.jumpToActivity(getActivity(), UserFavoriteActivity.class);
            }
        });

        //跳转到修改密码页面
        personal_nickname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Utilities.jumpToActivity(getActivity(), ChangePasswordActivity.class);
            }
        });

        personal_list_cart_relative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Utilities.jumpToActivity(getActivity(), ShoppingCardActivity.class);
            }
        });

        personal_list_service_relative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Utilities.jumpToActivity(getActivity(), ServiceActivity.class);
            }
        });

        Glide.with(getActivity()).load(R.drawable.personal_bj_golden).into(personal_background_ImageView);

        sunButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("TokenData", Context.MODE_PRIVATE);
                String mid = sharedPreferences.getString(MID_KEY, "");

                changeSun(mid);
            }
        });

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getUserDataInfo();
            }
        });

        final Bundle bundle = new Bundle();

        personal_gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {


                switch (i){
                    case 0:

                        bundle.putInt("ostatus", 0);
                        Utilities.jumpToActivity(getActivity(), OrderListActivity.class, bundle, "bundleOstatus");
                        break;
                    case 1:

                        bundle.putInt("ostatus", 2);
                        Utilities.jumpToActivity(getActivity(), OrderListActivity.class, bundle, "bundleOstatus");
                        break;
                    case 2:

                        bundle.putInt("ostatus", 4);
                        Utilities.jumpToActivity(getActivity(), OrderListActivity.class, bundle, "bundleOstatus");
                        break;
                    case 3:

                        bundle.putInt("ostatus", 5);
                        Utilities.jumpToActivity(getActivity(), OrderListActivity.class, bundle, "bundleOstatus");
                        break;
                }
            }
        });
    }

    /* 黑色设置样式*/
    private void changeBlack(){

        if (getActivity() != null){

            //背景图片
            Glide.with(getActivity()).load(R.drawable.personal_bj_back).into(personal_background_ImageView);
        }
        //右侧背景颜色
        personal_rigint_relative.setBackgroundResource(R.drawable.personal_right_border_balck);
        //右侧圆形
        personal_rigint_oval.setBackgroundResource(R.drawable.personal_rigint_oval_balck);
        //右侧图片
        personal_rigint_Icon.setImageResource(R.mipmap.personal_logo_black);
        //右侧vip
        personal_vip.setTextColor(ContextCompat.getColor(view.getContext(),R.color.personal_vip_black));
        //右侧vip数量
        personal_vip_num.setTextColor(ContextCompat.getColor(view.getContext(),R.color.personal_vip_num_black));
        //日期标题
        personal_vdate_title.setTextColor(ContextCompat.getColor(view.getContext(),R.color.personal_vdate_title_black));
        //日期值
        personal_vdate_val.setTextColor(ContextCompat.getColor(view.getContext(),R.color.personal_vdate_val_black));
        //积分relative
        personal_integral_relative.setBackgroundResource(R.drawable.personal_integral_border_balck);
        //积分标题
        personal_integral_title.setTextColor(ContextCompat.getColor(view.getContext(),R.color.personal_integral_title_black));
        //积分值
        personal_integral_val.setTextColor(ContextCompat.getColor(view.getContext(),R.color.personal_integral_val_black));
        //昵称
        personal_nickname.setTextColor(ContextCompat.getColor(view.getContext(),R.color.personal_nickname_black));

        //禁止拉伸
        //personal_gridview.setStretchMode(GridView.NO_STRETCH);
        PersonalCenterAdapter adapter= new PersonalCenterAdapter(view.getContext(),imgs,3);
        personal_gridview.setAdapter(adapter);
    }

    /* 银色设置样式*/
    private void changeSilver(){

        if (getActivity() != null){

            //背景图片
            Glide.with(getActivity()).load(R.drawable.personal_bj_silvery).into(personal_background_ImageView);
        }
        //右侧背景颜色
        personal_rigint_relative.setBackgroundResource(R.drawable.personal_right_border_silvery);
        //右侧圆形
        personal_rigint_oval.setBackgroundResource(R.drawable.personal_rigint_oval_silvery);
        //右侧图片
        personal_rigint_Icon.setImageResource(R.mipmap.personal_logo_silvery);
        //右侧vip
        personal_vip.setTextColor(ContextCompat.getColor(view.getContext(),R.color.personal_vip_silvery));
        //右侧vip数量
        personal_vip_num.setTextColor(ContextCompat.getColor(view.getContext(),R.color.personal_vip_num_silvery));
        //日期标题
        personal_vdate_title.setTextColor(ContextCompat.getColor(view.getContext(),R.color.personal_vdate_title_silvery));
        //日期值
        personal_vdate_val.setTextColor(ContextCompat.getColor(view.getContext(),R.color.personal_vdate_val_silvery));
        //积分relative
        personal_integral_relative.setBackgroundResource(R.drawable.personal_integral_border_silvery);
        //积分标题
        personal_integral_title.setTextColor(ContextCompat.getColor(view.getContext(),R.color.personal_integral_title_silvery));
        //积分值
        personal_integral_val.setTextColor(ContextCompat.getColor(view.getContext(),R.color.personal_integral_val_silvery));
        //昵称
        personal_nickname.setTextColor(ContextCompat.getColor(view.getContext(),R.color.personal_nickname_silvery));

        //禁止拉伸
        //personal_gridview.setStretchMode(GridView.NO_STRETCH);
        PersonalCenterAdapter adapter= new PersonalCenterAdapter(view.getContext(),imgs2,3);
        personal_gridview.setAdapter(adapter);
    }

    private void getUserDataInfo(){

        random32 = Utilities.getStringRandom(32);
        time10 = Utilities.get10Time();
        key64 = Utilities.get64Key(random32);

        final SharedPreferences sharedPreferences = getActivity().getSharedPreferences("TokenData", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString(TOKEN_KEY, "");
        String mid = sharedPreferences.getString(MID_KEY, "");

//        if (token.equals("") && mid.equals("")){
//
//            Utilities.popUpAlert(getActivity(), "登录过期，请重新登录");
//
//            getActivity().finish();
//            Utilities.jumpToActivity(getActivity(), LoginActivity.class);
//            return;
//        }

        final String json = "{\n" +
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
                "                    \"d_Token\": \"\",\n" +
                "                    \"s_Total_parameter\": \"Mid,OpenID,Nickname,Head_img,Mname,Sex,Mobile,Rdate,Birthday,Alive,Gag,The_sun,Members_LV,Orders_count,Integral,card,Passwd,Addr,Stem_from,Token,Token_expiry,Token_IP,Referee_Mid,Referee2_Mid,Referee,Referee2,Reward,Withdraw,Contribution_Award,Balance\"\n" +
                "                },\n" +
                "                \"sign_valid\": {\n" +
                "                    \"source\": \"Android\",\n" +
                "                    \"non_str\": \""+random32+"\",\n" +
                "                    \"stamp\": \""+time10+"\",\n" +
                "                    \"signature\": \""+Utilities.encode("d_Alive="+"d_IsUpdateToken=0"+"d_Mid="+mid+"d_Token="+"s_Total_parameter=Mid,OpenID,Nickname,Head_img,Mname,Sex,Mobile,Rdate,Birthday,Alive,Gag,The_sun,Members_LV,Orders_count,Integral,card,Passwd,Addr,Stem_from,Token,Token_expiry,Token_IP,Referee_Mid,Referee2_Mid,Referee,Referee2,Reward,Withdraw,Contribution_Award,Balance"+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        OkHttpClient okHttpClient = new OkHttpClient();

        RequestBody requestBody = RequestBody.create(JSON, json);

        Request request = new Request.Builder().url(API.getAPI()).post(requestBody).build();

        okHttpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {

                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        Utilities.popUpAlert(getActivity(), "出现某些未知错误，请稍后再试");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.body() != null){

                    try {

                        JSONObject jsonObject = new JSONObject(response.body().string());

                        Log.d("personal", jsonObject.toString());

                        final String nickName = jsonObject.getJSONArray("result").getJSONObject(0).getJSONObject("detail").getString("Nickname");
                        final String mName = jsonObject.getJSONArray("result").getJSONObject(0).getJSONObject("detail").getString("Mname");
                        final boolean isSun = jsonObject.getJSONArray("result").getJSONObject(0).getJSONObject("detail").getBoolean("The_sun");
                        final int integral = jsonObject.getJSONArray("result").getJSONObject(0).getJSONObject("detail").getInt("Integral");
                        final int memberLevel = jsonObject.getJSONArray("result").getJSONObject(0).getJSONObject("detail").getInt("Members_LV");
                        final String headImg = jsonObject.getJSONArray("result").getJSONObject(0).getJSONObject("detail").getString("Head_img");
                        final String deadLine = jsonObject.getJSONArray("result").getJSONObject(0).getJSONObject("detail").getJSONObject("card").getString("Vdate");
                        final int grade = jsonObject.getJSONArray("result").getJSONObject(0).getJSONObject("detail").getJSONObject("card").getInt("Grade");

                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {

                                Log.d("personal", nickName);

                                swipeRefreshLayout.setRefreshing(false);
//                                if (nickName.equals("")){
//
//                                    Toast.makeText(getActivity(), "登录过期，请重新登录", Toast.LENGTH_LONG).show();
//                                    SharedPreferences.Editor editor = getActivity().getSharedPreferences("TokenData", Context.MODE_PRIVATE).edit();
//                                    editor.putString(TOKEN_KEY, "");
//                                    editor.putString(MID_KEY, "");
//                                    editor.apply();
//
//                                    getActivity().finish();
//                                    Utilities.jumpToActivity(getActivity(), LoginActivity.class);
//                                    return;
//                                }

                                if (grade == 1){

                                    changeSilver();
                                }else if (grade == 3) {

                                    changeBlack();
                                }
                                sunButton.setChecked(isSun);
                                if (headImg.equals("")){

                                    personal_head_img.setImageDrawable(getResources().getDrawable(R.mipmap.login_logo));
                                }else {

                                    Glide.with(getActivity()).load(API.getAnothereHostName()+headImg)
                                            .apply(bitmapTransform(new CropCircleTransformation()))
                                            .into((personal_head_img));
                                }

                                personal_nickname.setText(mName);
                                personal_integral_val.setText(""+integral);
                                personal_vip_num.setText(""+memberLevel);
                                java.text.SimpleDateFormat format1 = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                try {
                                    Date myDate = format1.parse(String.valueOf(deadLine));
                                    java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyy-MM-dd");
                                    String date = formatter.format(myDate);

                                    personal_vdate_val.setText(date);
                                } catch (ParseException e) {
                                    e.printStackTrace();
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

    //修改晒的状态
    private void changeSun(String mid){

        random32 = Utilities.getStringRandom(32);
        time10 = Utilities.get10Time();
        key64 = Utilities.get64Key(random32);

        String json = "{\n" +
                "    \"validate_k\": \"1\",\n" +
                "    \"params\": [\n" +
                "        {\n" +
                "            \"type\": \"Members\",\n" +
                "            \"act\": \"The_sun\",\n" +
                "            \"para\": {\n" +
                "                \"params\": {\n" +
                "                    \"d_Mid\": \""+mid+"\"\n" +
                "                },\n" +
                "                \"sign_valid\": {\n" +
                "                    \"source\": \"Android\",\n" +
                "                    \"non_str\": \""+random32+"\",\n" +
                "                    \"stamp\": \""+time10+"\",\n" +
                "                    \"signature\": \""+Utilities.encode("d_Mid="+mid+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        OkHttpClient okHttpClient = new OkHttpClient();

        RequestBody requestBody = RequestBody.create(JSON, json);

        Request request = new Request.Builder().url(API.getAPI()).post(requestBody).build();

        okHttpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {

                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        Utilities.popUpAlert(getActivity(), "出现某些未知错误，请稍后再试");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                Log.d("changeSun", String.valueOf(response.body().string()));
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        localBroadcastManager.unregisterReceiver(localReceiver);
    }

    public class LocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            swipeRefreshLayout.setRefreshing(true);
            getUserDataInfo();
        }
    }
}
