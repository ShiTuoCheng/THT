package tht.topu.com.tht.ui.fragment.drawFragment;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;


import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import tht.topu.com.tht.R;
import tht.topu.com.tht.modle.Prize;
import tht.topu.com.tht.modle.Rank;
import tht.topu.com.tht.ui.activity.DrawRecordActivity;
import tht.topu.com.tht.ui.activity.DrawRulesActivity;
import tht.topu.com.tht.ui.base.BaseFragment;
import tht.topu.com.tht.utils.API;
import tht.topu.com.tht.utils.Utilities;

import static android.content.Context.MODE_PRIVATE;


public class DrawFragment extends Fragment {

    private TextSwitcher textSwitcher;
    private ImageSwitcher imageSwitcher;
    private ImageView drawImageView;
    private TextView drawTextView;
    private LinearLayout drawLayout;
    private ProgressDialog dialog;

    private List<String> textArr = new ArrayList<>();
    private List<Drawable> imagesArr = new ArrayList<>();
    private List<Prize> prizes = new ArrayList<>();
    private List<String> drawedMid = new ArrayList<>();
    private List<String> repeatMid = new ArrayList<>();
    private SoundPool soundPool;
    private int index = 0;
    private int mSoundId;

    private Timer timer = null;
    private TimerTask timerTask = null;

    private static SensorManager sensorManager = null;
    private SensorEventListener sensorEventListener;

    public static final MediaType JSON = MediaType
            .parse("application/json; charset=utf-8");

    private AppCompatButton drawButton;
    private TextView drawRecordTextView;
    private TextView drawRulesTextView;
    private boolean isShake = false;

    private String random32;
    private String time10;
    private String key64;

    private Handler uiHandler;
    private Handler alertHandler;

    private String midStr;

    private int amount = 0;
    private int drawNum = 0;

    private boolean isDrawed = false;
    private boolean canDrawed = false;


    private static final String TOKEN_KEY = "0x01";
    private static final String MID_KEY = "1x11";

    // 实例化方法
    public static DrawFragment newInstance(Context context){

        DrawFragment drawFragment = new DrawFragment();
        sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        return drawFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_draw, container, false);

        initView(view);


        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("TokenData", MODE_PRIVATE);

        midStr = sharedPreferences.getString(MID_KEY, "");

        soundPool = new SoundPool(2, AudioManager.STREAM_SYSTEM, 5);

        mSoundId = soundPool.load(getActivity(),R.raw.sound, 1);
        uiHandler = new Handler(Looper.getMainLooper());

        alertHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {

                    case 0:

                        Snackbar.make(drawLayout, "网络不可用", Snackbar.LENGTH_LONG).setAction("去设置", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                            }
                        }).show();

                        break;

                    case 1:
                        Toast toast = Toast.makeText(getContext().getApplicationContext(), "出现某些未知错误，请稍后再试", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.NO_GRAVITY, 0, Utilities.dip2px(getContext().getApplicationContext(), 200));
                        toast.show();

                        break;
                }
            }
        };


        if (!Utilities.isNetworkAvaliable(getActivity().getApplicationContext())){

            alertHandler.sendEmptyMessageDelayed(0,1000);
        }else {

            initData();
        }


        drawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isShake){

                    isShake = true;

                    dialog.show();
                    getDrawNum();
                }
            }
        });


        drawRecordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Utilities.jumpToActivity(getActivity(), DrawRecordActivity.class);
            }
        });

        drawRulesTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utilities.jumpToActivity(getActivity(), DrawRulesActivity.class);
            }
        });
        return view;
    }

//    @Override
//    public void onPause() {
//        super.onPause();
//        if (sensorManager != null){
//
//            sensorManager.unregisterListener(sensorEventListener);
//        }
//    }

    //    初始化视图
    private void initView(View view){

        textSwitcher = view.findViewById(R.id.textSwitcher);
        imageSwitcher = view.findViewById(R.id.imageSwitcher);
        drawButton = view.findViewById(R.id.drawButton);
        drawRecordTextView = view.findViewById(R.id.recordTextView);
        drawRulesTextView = view.findViewById(R.id.drawRulesTextView);
        drawTextView = view.findViewById(R.id.drawTextView);
        drawLayout = view.findViewById(R.id.drawLayout);
        drawImageView = view.findViewById(R.id.drawImageView);

        dialog = new ProgressDialog(getActivity());

    }

    //    初始化数据
    private void initData(){
        getDrawRecord();
        getDrawInfo();
        getDrawImg();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser) {

            shakePhone();
        } else {

            if (sensorManager != null){

                sensorManager.unregisterListener(sensorEventListener);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (timer != null){

            timer.cancel();
        }
        if (alertHandler != null){

            alertHandler = null;
        }

        if (uiHandler != null){

            uiHandler = null;
        }

        if (sensorManager != null){

            sensorManager.unregisterListener(sensorEventListener);
        }
    }

    private void getDrawInfo(){

        random32 = Utilities.getStringRandom(32);
        time10 = Utilities.get10Time();
        key64 = Utilities.get64Key(random32);

        String json = "{\n" +
                "    \"validate_k\": \"1\",\n" +
                "    \"params\": [\n" +
                "        {\n" +
                "            \"type\": \"Info\",\n" +
                "            \"act\": \"Select_List\",\n" +
                "            \"para\": {\n" +
                "                \"params\": {\n" +
                "                    \"s_Iid\": \"5\"\n" +
                "                },\n" +
                "                \"sign_valid\": {\n" +
                "                    \"source\": \"Android\",\n" +
                "                    \"non_str\": \""+random32+"\",\n" +
                "                    \"stamp\": \""+time10+"\",\n" +
                "                    \"signature\": \""+Utilities.encode("s_Iid=5"+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
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

                if (uiHandler != null){

                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {

                            alertHandler.sendEmptyMessageDelayed(1,1000);
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                try {

                    JSONObject jsonObject = new JSONObject(response.body().string());
                    JSONArray recordArr = jsonObject.getJSONArray("result").getJSONObject(0).getJSONArray("list");

                    final String drawInfo = recordArr.getJSONObject(0).getString("Iinfo");

                    Log.d("img", drawInfo);

                    if (uiHandler !=null){

                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {

                                drawTextView.setText(drawInfo);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void getDrawImg(){

        random32 = Utilities.getStringRandom(32);
        time10 = Utilities.get10Time();
        key64 = Utilities.get64Key(random32);

        String json = "{\n" +
                "    \"validate_k\": \"1\",\n" +
                "    \"params\": [\n" +
                "        {\n" +
                "            \"type\": \"Advertise\",\n" +
                "            \"act\": \"Select_List\",\n" +
                "            \"para\": {\n" +
                "                \"params\": {\n" +
                "                    \"s_Aid\": \"4\",\n" +
                "                    \"s_Keywords\": \"\",\n" +
                "                    \"s_Order\": \"\",\n" +
                "                    \"s_Total_parameter\": \"Aid,Atitle,Url,Pic1\"\n" +
                "                },\n" +
                "                \"pages\": {\n" +
                "                    \"p_c\": \"\",\n" +
                "                    \"p_First\": \"\",\n" +
                "                    \"p_inputHeight\": \"\",\n" +
                "                    \"p_Last\": \"\",\n" +
                "                    \"p_method\": \"\",\n" +
                "                    \"p_Next\": \"\",\n" +
                "                    \"p_Page\": \"\",\n" +
                "                    \"p_pageName\": \"\",\n" +
                "                    \"p_PageStyle\": \"\",\n" +
                "                    \"p_Pname\": \"\",\n" +
                "                    \"p_Previous\": \"\",\n" +
                "                    \"p_Ps\": \"\",\n" +
                "                    \"p_sk\": \"\",\n" +
                "                    \"p_Tp\": \"\"\n" +
                "                },\n" +
                "                \"sign_valid\": {\n" +
                "                    \"source\": \"Android\",\n" +
                "                    \"non_str\": \""+random32+"\",\n" +
                "                    \"stamp\": \""+time10+"\",\n" +
                "                    \"signature\": \""+Utilities.encode("s_Aid=4"+"s_Keywords="+"s_Order="+"s_Total_parameter=Aid,Atitle,Url,Pic1"+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
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

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.body() != null){

                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());

                        final String imgUrl = jsonObject.getJSONArray("result").getJSONObject(0).getJSONArray("list").getJSONObject(0).getString("Pic1");
                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {

                                Glide.with(getActivity()).load(API.getHostName()+imgUrl).into(drawImageView);
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    //获取中奖纪录用来轮播
    private void getDrawRecord(){

        random32 = Utilities.getStringRandom(32);
        time10 = Utilities.get10Time();
        key64 = Utilities.get64Key(random32);

        String json = "{\n" +
                "    \"validate_k\": \"1\",\n" +
                "    \"params\": [\n" +
                "        {\n" +
                "            \"type\": \"Prize_Record\",\n" +
                "            \"act\": \"Select_List\",\n" +
                "            \"para\": {\n" +
                "                \"params\": {\n" +
                "                    \"s_d1\": \"\",\n" +
                "                    \"s_d2\": \"\",\n" +
                "                    \"s_Is_Record\":\"\",\n"+
                "                    \"s_Keywords\": \"\",\n" +
                "                    \"s_Mid\": \"\",\n" +
                "                    \"s_Order\": \"\",\n" +
                "                    \"s_Pid\": \"\",\n" +
                "                    \"s_Prid\": \"\",\n" +
                "                    \"s_Total_parameter\": \"Prid,Mid,Member,Mobile,Pdate,Pid,Ptitle,Is_Record\"\n" +
                "                },\n" +
                "                \"pages\": {\n" +
                "                    \"p_c\": \"\",\n" +
                "                    \"p_First\": \"\",\n" +
                "                    \"p_inputHeight\": \"\",\n" +
                "                    \"p_Last\": \"\",\n" +
                "                    \"p_method\": \"\",\n" +
                "                    \"p_Next\": \"\",\n" +
                "                    \"p_Page\": \"\",\n" +
                "                    \"p_pageName\": \"\",\n" +
                "                    \"p_PageStyle\": \"\",\n" +
                "                    \"p_Pname\": \"\",\n" +
                "                    \"p_Previous\": \"\",\n" +
                "                    \"p_Ps\": \"\",\n" +
                "                    \"p_sk\": \"\",\n" +
                "                    \"p_Tp\": \"\"\n" +
                "                },\n" +
                "                \"sign_valid\": {\n" +
                "                    \"source\": \"Android\",\n" +
                "                    \"non_str\": \""+random32+"\",\n" +
                "                    \"stamp\": \""+time10+"\",\n" +
                "                    \"signature\": \""+Utilities.encode("s_d1="+"s_d2="+"s_Is_Record="+"s_Keywords="+"s_Mid="+"s_Order="+"s_Pid="+"s_Prid="+"s_Total_parameter=Prid,Mid,Member,Mobile,Pdate,Pid,Ptitle,Is_Record"+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
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

                if (uiHandler != null){

                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {

                            alertHandler.sendEmptyMessageDelayed(1,1000);
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                try {

                    JSONObject jsonObject = new JSONObject(response.body().string());
                    JSONArray recordArr = jsonObject.getJSONArray("result").getJSONObject(0).getJSONArray("list");

                    for (int i = 0; i < recordArr.length(); i++){

                        JSONObject eachRecordObj = recordArr.getJSONObject(i);

                        if (!eachRecordObj.getString("Ptitle").equals("")){

                            textArr.add(eachRecordObj.getJSONObject("Member").getString("Mname")+"   抽中了   "+ eachRecordObj.getString("Ptitle"));
                            imagesArr.add(new BitmapDrawable(Utilities.returnBitmap(API.getAnothereHostName()+eachRecordObj.getJSONObject("Member").getString("Head_img"))));
                        }
                    }

                    if (uiHandler != null){

                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {

                                ViewSwitcher.ViewFactory viewFactory = new ViewSwitcher.ViewFactory() {
                                    @Override
                                    public View makeView() {

                                        TextView drawTextView = new TextView(getActivity().getApplicationContext());
                                        drawTextView.setTextColor(getResources().getColor(R.color.colorblack));
                                        drawTextView.setTextSize(12);
                                        drawTextView.setMaxLines(1);
                                        return drawTextView;
                                    }
                                };

                                ViewSwitcher.ViewFactory imageViewFactory = new ViewSwitcher.ViewFactory() {
                                    @Override
                                    public View makeView() {

                                        ImageView imageView = new ImageView(getActivity().getApplicationContext());
                                        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                                        return imageView;
                                    }
                                };

                                textSwitcher.setFactory(viewFactory);

                                imageSwitcher.setFactory(imageViewFactory);

                                timer = new Timer();

                                timerTask = new TimerTask() {
                                    @Override
                                    public void run() {

                                        if (getActivity() != null) {
                                            //切换到主线程
                                            getActivity().runOnUiThread(new Runnable() {
                                                public void run() {

                                                    if (textArr.size() != 0 && imagesArr.size() != 0 && (textSwitcher != null && imageSwitcher != null) ){

                                                        textSwitcher.setText(textArr.get(index));
                                                        imageSwitcher.setImageDrawable(imagesArr.get(index));
                                                        index++;
                                                        if (index == textArr.size()) {
                                                            index = 0;
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                    }
                                };
                                timer.scheduleAtFixedRate(timerTask, 0, 1000);
                            }
                        });
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    //摇一摇
    private void shakePhone(){

        if (sensorManager != null){

            final Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

            if (accelerometerSensor != null){

                sensorEventListener = new SensorEventListener() {
                    @Override
                    public void onSensorChanged(SensorEvent sensorEvent) {

                        int eventType = sensorEvent.sensor.getType();

                        if (eventType == Sensor.TYPE_ACCELEROMETER){

                            float value[] = sensorEvent.values;
                            float valueX = value[0];
                            float valueY = value[1];
                            float valueZ = value[2];

                            if ((Math.abs(valueX) > 17 || Math.abs(valueY) > 17 || Math
                                    .abs(valueZ) > 17) && !isShake){

                                isShake = true;

                                dialog.show();
                                getDrawNum();
                            }
                        }
                    }

                    @Override
                    public void onAccuracyChanged(Sensor sensor, int i) {


                    }
                };

                sensorManager.registerListener(sensorEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_UI);
            }
        }
    }

    //抽奖的逻辑
    private void drawLogic(){

        random32 = Utilities.getStringRandom(32);
        time10 = Utilities.get10Time();
        key64 = Utilities.get64Key(random32);

        String json = "{\n" +
                "    \"validate_k\": \"1\",\n" +
                "    \"params\": [\n" +
                "        {\n" +
                "            \"type\": \"Prize\",\n" +
                "            \"act\": \"Select_List\",\n" +
                "            \"para\": {\n" +
                "                \"params\": {\n" +
                "                    \"s_Alive\": \"\",\n" +
                "                    \"s_Keywords\": \"\",\n" +
                "                    \"s_Order\": \"\",\n" +
                "                    \"s_Pid\": \"\",\n" +
                "                    \"s_Pstate\": \"\",\n" +
                "                    \"s_Total_parameter\": \"Pid,Ptitle,Probability,Pstate,Alive,Amount,Stock_final\"\n" +
                "                },\n" +
                "                \"pages\": {\n" +
                "                    \"p_c\": \"\",\n" +
                "                    \"p_First\": \"\",\n" +
                "                    \"p_inputHeight\": \"\",\n" +
                "                    \"p_Last\": \"\",\n" +
                "                    \"p_method\": \"\",\n" +
                "                    \"p_Next\": \"\",\n" +
                "                    \"p_Page\": \"\",\n" +
                "                    \"p_pageName\": \"\",\n" +
                "                    \"p_PageStyle\": \"\",\n" +
                "                    \"p_Pname\": \"\",\n" +
                "                    \"p_Previous\": \"\",\n" +
                "                    \"p_Ps\": \"\",\n" +
                "                    \"p_sk\": \"\",\n" +
                "                    \"p_Tp\": \"\"\n" +
                "                },\n" +
                "                \"sign_valid\": {\n" +
                "                    \"source\": \"Android\",\n" +
                "                    \"non_str\": \""+random32+"\",\n" +
                "                    \"stamp\": \""+time10+"\",\n" +
                "                    \"signature\": \""+Utilities.encode("s_Alive="+"s_Keywords="+"s_Order="+"s_Pid="+"s_Pstate="+"s_Total_parameter=Pid,Ptitle,Probability,Pstate,Alive,Amount,Stock_final"+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
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

                if (uiHandler != null){

                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {

                            Toast.makeText(getActivity(), "出现某些问题，请重试", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.body() != null){

                    try {

                        JSONObject jsonObj = new JSONObject(response.body().string());

                        Log.d("draw", jsonObj.toString());
                        JSONArray prizeListData = jsonObj.getJSONArray("result").getJSONObject(0).getJSONArray("list");
                        Prize.Builder builder = new Prize.Builder();
                        prizes.clear();

                        for (int i=0; i < prizeListData.length(); i++){

                            //判断奖品余辆是否大于0
                            if (prizeListData.getJSONObject(i).getInt("Stock_final") > 0){

                                Prize prize = builder.name(prizeListData.getJSONObject(i).getString("Ptitle")).Probability(prizeListData.getJSONObject(i).getInt("Probability")).pid(prizeListData.getJSONObject(i).getString("Pid")).build();
                                prizes.add(prize);
                            }
                        }

                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {

                                //如果没有奖品直接提示失败,之后不再执行
                                if (prizes.size() == 0){

                                    submitDraw(midStr, String.valueOf(0), false);
                                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
                                    AlertDialog alertDialog = alertBuilder
                                            .setMessage("很遗憾，这次您没有抽中，请明天再试试吧")
                                            .setTitle("摇一摇")
                                            .setNeutralButton("好的", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {

                                                    isShake = false;

                                                    soundPool.stop(mSoundId);//播放声音
                                                    dialogInterface.dismiss();
                                                }
                                            }).create();

                                    alertDialog.setCancelable(false);
                                    alertDialog.show();

                                    soundPool.play(mSoundId, 1, 1, 1, 0, 1);//播放声音

                                    return;
                                }

                                drawHistory();
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    //查找中奖纪录方法
    private void drawHistory(){

        random32 = Utilities.getStringRandom(32);
        time10 = Utilities.get10Time();
        key64 = Utilities.get64Key(random32);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cale;
        String firstday, lastday;
        // 获取前月的第一天
        cale = Calendar.getInstance();
        cale.add(Calendar.MONTH, 0);
        cale.set(Calendar.DAY_OF_MONTH, 1);
        firstday = format.format(cale.getTime());
        // 获取前月的最后一天
        cale = Calendar.getInstance();
        cale.add(Calendar.MONTH, 1);
        cale.set(Calendar.DAY_OF_MONTH, 0);
        lastday = format.format(cale.getTime());
        Log.d("本月第一天和最后一天分别是 ： " , firstday + " and " + lastday);

        String json = "{\n" +
                "    \"validate_k\": \"1\",\n" +
                "    \"params\": [\n" +
                "        {\n" +
                "            \"type\": \"Prize_Record\",\n" +
                "            \"act\": \"Select_List\",\n" +
                "            \"para\": {\n" +
                "                \"params\": {\n" +
                "                    \"s_d1\": \""+firstday+"\",\n" +
                "                    \"s_d2\": \""+lastday+"\",\n" +
                "                    \"s_Is_Record\":\"\",\n"+
                "                    \"s_Keywords\": \"\",\n" +
                "                    \"s_Mid\": \"\",\n" +
                "                    \"s_Order\": \"\",\n" +
                "                    \"s_Pid\": \"\",\n" +
                "                    \"s_Prid\": \"\",\n" +
                "                    \"s_Total_parameter\": \"Prid,Mid,Member,Mobile,Pdate,Pid,Ptitle,Is_Record\"\n" +
                "                },\n" +
                "                \"pages\": {\n" +
                "                    \"p_c\": \"\",\n" +
                "                    \"p_First\": \"\",\n" +
                "                    \"p_inputHeight\": \"\",\n" +
                "                    \"p_Last\": \"\",\n" +
                "                    \"p_method\": \"\",\n" +
                "                    \"p_Next\": \"\",\n" +
                "                    \"p_Page\": \"\",\n" +
                "                    \"p_pageName\": \"\",\n" +
                "                    \"p_PageStyle\": \"\",\n" +
                "                    \"p_Pname\": \"\",\n" +
                "                    \"p_Previous\": \"\",\n" +
                "                    \"p_Ps\": \"\",\n" +
                "                    \"p_sk\": \"\",\n" +
                "                    \"p_Tp\": \"\"\n" +
                "                },\n" +
                "                \"sign_valid\": {\n" +
                "                    \"source\": \"Android\",\n" +
                "                    \"non_str\": \""+random32+"\",\n" +
                "                    \"stamp\": \""+time10+"\",\n" +
                "                    \"signature\": \""+Utilities.encode("s_d1="+firstday+"s_d2="+lastday+"s_Is_Record="+"s_Keywords="+"s_Mid="+"s_Order="+"s_Pid="+"s_Prid="+"s_Total_parameter=Prid,Mid,Member,Mobile,Pdate,Pid,Ptitle,Is_Record"+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
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

                if (uiHandler != null){

                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {

                            alertHandler.sendEmptyMessageDelayed(1,1000);
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                try {

                    JSONObject jsonObject = new JSONObject(response.body().string());
                    JSONArray recordArr = jsonObject.getJSONArray("result").getJSONObject(0).getJSONArray("list");

                    drawedMid.clear();
                    repeatMid.clear();

                    for (int i = 0; i < recordArr.length(); i++){

                        JSONObject eachRecordObj = recordArr.getJSONObject(i);

                        if (eachRecordObj.getBoolean("Is_Record")){

                            drawedMid.add(eachRecordObj.getString("Mid"));
                        }
                    }

                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {

                            int mid = Integer.valueOf(midStr);

                            for (String drawedmid: drawedMid) {

                                if (String.valueOf(mid).equals(drawedmid)){

                                    repeatMid.add(String.valueOf(mid));
                                }
                            }

                            int randomNum=(int)(Math.random()*100);

                            Log.d("drawRandom", String.valueOf(randomNum));

                            //如果用户随机到指定数内则开始抽奖
                            if (randomNum <= 100 - repeatMid.size() * amount){

                                Random random = new Random();
                                int drawNum = random.nextInt(prizes.size());

                                submitDraw(midStr, prizes.get(drawNum).getPid(), true);

                                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
                                AlertDialog alertDialog = alertBuilder
                                        .setMessage("恭喜，您抽中了"+prizes.get(drawNum).getName())
                                        .setTitle("摇一摇")
                                        .setNeutralButton("好的", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                                isShake = false;

                                                dialogInterface.dismiss();
                                            }
                                        }).create();

                                alertDialog.setCancelable(false);
                                alertDialog.show();

                            }else {

                                submitDraw(midStr, String.valueOf(0), false);
                                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
                                AlertDialog alertDialog = alertBuilder
                                        .setMessage("很遗憾，这次您没有抽中，请明天再试试吧")
                                        .setTitle("摇一摇")
                                        .setNeutralButton("好的", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                                isShake = false;

                                                dialogInterface.dismiss();
                                            }
                                        }).create();

                                alertDialog.setCancelable(false);
                                alertDialog.show();
                            }
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //提交中奖纪录方法
    private void submitDraw(String mid, String pid, boolean getPrize){

        random32 = Utilities.getStringRandom(32);
        time10 = Utilities.get10Time();
        key64 = Utilities.get64Key(random32);

        String json = "{\n" +
                "    \"validate_k\": \"1\",\n" +
                "    \"params\": [\n" +
                "        {\n" +
                "            \"type\": \"Prize_Record\",\n" +
                "            \"act\": \"Add\",\n" +
                "            \"para\": {\n" +
                "                \"params\": {\n" +
                "                    \"Is_Record\": \""+getPrize+"\",\n" +
                "                    \"Mid\": \""+mid+"\",\n" +
                "                    \"Mobile\": \"\",\n" +
                "                    \"Pid\": \""+pid+"\"\n" +
                "                },\n" +
                "                \"sign_valid\": {\n" +
                "                    \"source\": \"Android\",\n" +
                "                    \"non_str\": \""+random32+"\",\n" +
                "                    \"stamp\": \""+time10+"\",\n" +
                "                    \"signature\": \""+Utilities.encode("Is_Record="+getPrize+"Mid="+mid+"Mobile="+"Pid="+pid+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
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

                if (uiHandler != null){

                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {

                            alertHandler.sendEmptyMessageDelayed(1,1000);
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.body() != null){

                    Log.d("submitDrawResult", response.body().string());
                }
            }
        });
    }

    //验证用户是否今天已经抽过
     private void isDrawedAlready(){

         random32 = Utilities.getStringRandom(32);
         time10 = Utilities.get10Time();
         key64 = Utilities.get64Key(random32);

         Date date=new Date();
         DateFormat format=new SimpleDateFormat("yyyy-MM-dd");
         String time=format.format(date);

         String json = "{\n" +
                 "    \"validate_k\": \"1\",\n" +
                 "    \"params\": [\n" +
                 "        {\n" +
                 "            \"type\": \"Prize_Record\",\n" +
                 "            \"act\": \"Select_List\",\n" +
                 "            \"para\": {\n" +
                 "                \"params\": {\n" +
                 "                    \"s_d1\": \""+time+"\",\n" +
                 "                    \"s_d2\": \""+time+"\",\n" +
                 "                    \"s_Is_Record\":\"\",\n"+
                 "                    \"s_Keywords\": \"\",\n" +
                 "                    \"s_Mid\": \""+midStr+"\",\n" +
                 "                    \"s_Order\": \"\",\n" +
                 "                    \"s_Pid\": \"\",\n" +
                 "                    \"s_Prid\": \"\",\n" +
                 "                    \"s_Total_parameter\": \"Prid,Mid,Member,Mobile,Pdate,Pid,Ptitle,Is_Record\"\n" +
                 "                },\n" +
                 "                \"pages\": {\n" +
                 "                    \"p_c\": \"\",\n" +
                 "                    \"p_First\": \"\",\n" +
                 "                    \"p_inputHeight\": \"\",\n" +
                 "                    \"p_Last\": \"\",\n" +
                 "                    \"p_method\": \"\",\n" +
                 "                    \"p_Next\": \"\",\n" +
                 "                    \"p_Page\": \"\",\n" +
                 "                    \"p_pageName\": \"\",\n" +
                 "                    \"p_PageStyle\": \"\",\n" +
                 "                    \"p_Pname\": \"\",\n" +
                 "                    \"p_Previous\": \"\",\n" +
                 "                    \"p_Ps\": \"\",\n" +
                 "                    \"p_sk\": \"\",\n" +
                 "                    \"p_Tp\": \"\"\n" +
                 "                },\n" +
                 "                \"sign_valid\": {\n" +
                 "                    \"source\": \"Android\",\n" +
                 "                    \"non_str\": \""+random32+"\",\n" +
                 "                    \"stamp\": \""+time10+"\",\n" +
                 "                    \"signature\": \""+Utilities.encode("s_d1="+time+"s_d2="+time+"s_Is_Record="+"s_Keywords="+"s_Mid="+midStr+"s_Order="+"s_Pid="+"s_Prid="+"s_Total_parameter=Prid,Mid,Member,Mobile,Pdate,Pid,Ptitle,Is_Record"+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
                 "                }\n" +
                 "            }\n" +
                 "        }\n" +
                 "    ]\n" +
                 "}";

         OkHttpClient okHttpClient = new OkHttpClient();

         RequestBody requestBody = RequestBody.create(JSON, json);

         Request request = new Request.Builder().url(API.getAPI()).post(requestBody).build();

         Log.d("drawHistoryJson", json);

         okHttpClient.newCall(request).enqueue(new Callback() {
             @Override
             public void onFailure(Call call, IOException e) {

                 if (uiHandler != null){

                     uiHandler.post(new Runnable() {
                         @Override
                         public void run() {

                             alertHandler.sendEmptyMessageDelayed(1,1000);
                         }
                     });
                 }
             }

             @Override
             public void onResponse(Call call, Response response) throws IOException {

                 if (response.body() != null){

                     try {

                         JSONObject jsonObj = new JSONObject(response.body().string());

                         final JSONArray recordArr = jsonObj.getJSONArray("result").getJSONObject(0).getJSONArray("list");

                         uiHandler.post(new Runnable() {
                             @Override
                             public void run() {

                                 dialog.dismiss();
                                 if (!canDrawed){

                                     isDrawed = true;

                                     AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
                                     AlertDialog alertDialog = alertBuilder
                                             .setMessage("您这个月的可用抽奖次数已满，下个月再来吧")
                                             .setTitle("摇一摇")
                                             .setNeutralButton("好的", new DialogInterface.OnClickListener() {
                                                 @Override
                                                 public void onClick(DialogInterface dialogInterface, int i) {

                                                     isShake = false;

                                                     dialogInterface.dismiss();

                                                     soundPool.stop(mSoundId);
                                                 }
                                             }).create();

                                     alertDialog.setCancelable(false);
                                     alertDialog.show();

                                     soundPool.play(mSoundId, 1, 1, 1, 0, 1);//播放声音

                                 }else if (recordArr.length() > 0){

                                     isDrawed = true;

                                     AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
                                     AlertDialog alertDialog = alertBuilder
                                             .setMessage("您今天已经抽过了，明天再来吧")
                                             .setTitle("摇一摇")
                                             .setNeutralButton("好的", new DialogInterface.OnClickListener() {
                                                 @Override
                                                 public void onClick(DialogInterface dialogInterface, int i) {

                                                     isShake = false;

                                                     dialogInterface.dismiss();

                                                     soundPool.stop(mSoundId);
                                                 }
                                             }).create();

                                     alertDialog.setCancelable(false);
                                     alertDialog.show();

                                     soundPool.play(mSoundId, 1, 1, 1, 0, 1);//播放声音
                                 }else {

                                     isDrawed = false;
                                     drawLogic();
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

     private void getDrawNumMonth(){

         random32 = Utilities.getStringRandom(32);
         time10 = Utilities.get10Time();
         key64 = Utilities.get64Key(random32);

         SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
         Calendar cale;
         String firstday, lastday;
         // 获取前月的第一天
         cale = Calendar.getInstance();
         cale.add(Calendar.MONTH, 0);
         cale.set(Calendar.DAY_OF_MONTH, 1);
         firstday = format.format(cale.getTime());
         // 获取前月的最后一天
         cale = Calendar.getInstance();
         cale.add(Calendar.MONTH, 1);
         cale.set(Calendar.DAY_OF_MONTH, 0);
         lastday = format.format(cale.getTime());
         Log.d("本月第一天和最后一天分别是 ： " , firstday + " and " + lastday);

         String json = "{\n" +
                 "    \"validate_k\": \"1\",\n" +
                 "    \"params\": [\n" +
                 "        {\n" +
                 "            \"type\": \"Prize_Record\",\n" +
                 "            \"act\": \"Select_List\",\n" +
                 "            \"para\": {\n" +
                 "                \"params\": {\n" +
                 "                    \"s_d1\": \""+firstday+"\",\n" +
                 "                    \"s_d2\": \""+lastday+"\",\n" +
                 "                    \"s_Is_Record\":\"\",\n"+
                 "                    \"s_Keywords\": \"\",\n" +
                 "                    \"s_Mid\": \""+midStr+"\",\n" +
                 "                    \"s_Order\": \"\",\n" +
                 "                    \"s_Pid\": \"\",\n" +
                 "                    \"s_Prid\": \"\",\n" +
                 "                    \"s_Total_parameter\": \"Prid,Mid,Member,Mobile,Pdate,Pid,Ptitle,Is_Record\"\n" +
                 "                },\n" +
                 "                \"pages\": {\n" +
                 "                    \"p_c\": \"\",\n" +
                 "                    \"p_First\": \"\",\n" +
                 "                    \"p_inputHeight\": \"\",\n" +
                 "                    \"p_Last\": \"\",\n" +
                 "                    \"p_method\": \"\",\n" +
                 "                    \"p_Next\": \"\",\n" +
                 "                    \"p_Page\": \"\",\n" +
                 "                    \"p_pageName\": \"\",\n" +
                 "                    \"p_PageStyle\": \"\",\n" +
                 "                    \"p_Pname\": \"\",\n" +
                 "                    \"p_Previous\": \"\",\n" +
                 "                    \"p_Ps\": \"\",\n" +
                 "                    \"p_sk\": \"\",\n" +
                 "                    \"p_Tp\": \"\"\n" +
                 "                },\n" +
                 "                \"sign_valid\": {\n" +
                 "                    \"source\": \"Android\",\n" +
                 "                    \"non_str\": \""+random32+"\",\n" +
                 "                    \"stamp\": \""+time10+"\",\n" +
                 "                    \"signature\": \""+Utilities.encode("s_d1="+firstday+"s_d2="+lastday+"s_Is_Record="+"s_Keywords="+"s_Mid="+midStr+"s_Order="+"s_Pid="+"s_Prid="+"s_Total_parameter=Prid,Mid,Member,Mobile,Pdate,Pid,Ptitle,Is_Record"+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
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

             }

             @Override
             public void onResponse(Call call, Response response) throws IOException {

                 if (response.body()!= null){

                     try {
                         JSONObject jsonObj = new JSONObject(response.body().string());
                         JSONArray recordArr = jsonObj.getJSONArray("result").getJSONObject(0).getJSONArray("list");

                         if (recordArr.length() >= drawNum){

                             canDrawed = false;
                         }else {

                             canDrawed = true;
                         }

                         isDrawedAlready();
                     } catch (JSONException e) {
                         e.printStackTrace();
                     }

                 }
             }
         });
     }

     private void getAmound(){

         random32 = Utilities.getStringRandom(32);
         time10 = Utilities.get10Time();
         key64 = Utilities.get64Key(random32);

         String json = "{\n" +
                 "    \"validate_k\": \"1\",\n" +
                 "    \"params\": [\n" +
                 "        {\n" +
                 "            \"type\": \"Init\",\n" +
                 "            \"act\": \"getinfo\",\n" +
                 "            \"para\": {\n" +
                 "                \"params\": {\n" +
                 "                    \"Iid\": \"11\"\n" +
                 "                },\n" +
                 "                \"sign_valid\": {\n" +
                 "                    \"source\": \"Android\",\n" +
                 "                    \"non_str\": \""+random32+"\",\n" +
                 "                    \"stamp\": \""+time10+"\",\n" +
                 "                    \"signature\": \""+Utilities.encode("Iid=11"+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
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

             }

             @Override
             public void onResponse(Call call, Response response) throws IOException {

                 if (response.body()!= null){

                     try {
                         JSONObject jsonObject = new JSONObject(response.body().string());

                         amount = jsonObject.getJSONArray("result").getJSONObject(0).getJSONObject("Iinfo").getInt("Iinfo");

                         getDrawNumMonth();
                     } catch (JSONException e) {
                         e.printStackTrace();
                     }
                 }
             }
         });
     }

     private void getDrawNum(){

         random32 = Utilities.getStringRandom(32);
         time10 = Utilities.get10Time();
         key64 = Utilities.get64Key(random32);

         String json = "{\n" +
                 "    \"validate_k\": \"1\",\n" +
                 "    \"params\": [\n" +
                 "        {\n" +
                 "            \"type\": \"Init\",\n" +
                 "            \"act\": \"getinfo\",\n" +
                 "            \"para\": {\n" +
                 "                \"params\": {\n" +
                 "                    \"Iid\": \"7\"\n" +
                 "                },\n" +
                 "                \"sign_valid\": {\n" +
                 "                    \"source\": \"Android\",\n" +
                 "                    \"non_str\": \""+random32+"\",\n" +
                 "                    \"stamp\": \""+time10+"\",\n" +
                 "                    \"signature\": \""+Utilities.encode("Iid=7"+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
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

             }

             @Override
             public void onResponse(Call call, Response response) throws IOException {

                 if (response.body()!= null){

                     try {
                         JSONObject jsonObject = new JSONObject(response.body().string());

                         drawNum = jsonObject.getJSONArray("result").getJSONObject(0).getJSONObject("Iinfo").getInt("Iinfo");

                         getAmound();

                     } catch (JSONException e) {
                         e.printStackTrace();
                     }
                 }
             }
         });
     }

}
