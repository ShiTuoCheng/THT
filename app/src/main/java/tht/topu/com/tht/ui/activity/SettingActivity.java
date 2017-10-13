package tht.topu.com.tht.ui.activity;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import tht.topu.com.tht.R;
import tht.topu.com.tht.utils.DataCleanManager;
import tht.topu.com.tht.utils.Utilities;

public class SettingActivity extends AppCompatActivity {

    private RelativeLayout changePasswordLayout;
    private RelativeLayout locationManageLayout;
    private RelativeLayout clearCacheLayout;
    private RelativeLayout exitLoginLayout;

    private ProgressDialog progressDialog;

    private TextView cacheTextView;

    private static final String TOKEN_KEY = "0x01";
    private static final String MID_KEY = "1x11";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        try {
            initView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() throws Exception {

        changePasswordLayout = (RelativeLayout)findViewById(R.id.changePasswordLayout);
        locationManageLayout = (RelativeLayout)findViewById(R.id.manageLocationLayout);
        clearCacheLayout = (RelativeLayout)findViewById(R.id.clearCacheLayout);
        exitLoginLayout = (RelativeLayout)findViewById(R.id.exitLoginLayout);
        cacheTextView = (TextView)findViewById(R.id.cacheTextView);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("正在清理...");
        progressDialog.setMessage("正在清理缓存");
        progressDialog.setCancelable(false);

        //获取缓存大小
        cacheTextView.setText(DataCleanManager.getTotalCacheSize(getApplicationContext()));

        //修改密码
        changePasswordLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Utilities.jumpToActivity(SettingActivity.this, ChangePasswordActivity.class);
            }
        });

        //地址管理
        locationManageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


            }
        });

        //清除缓存
        clearCacheLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                clearCache();
            }
        });

        //退出登录点击事件
        exitLoginLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);

                AlertDialog alertDialog = builder.setTitle("退出登录")
                                                 .setMessage("您确定要退出登录吗?")
                                                 .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                                     @Override
                                                     public void onClick(DialogInterface dialogInterface, int i) {

                                                         dialogInterface.dismiss();
                                                     }
                                                 })
                                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {

                                                        Toast.makeText(SettingActivity.this, "退出登录，请重新登录", Toast.LENGTH_SHORT).show();
                                                        SharedPreferences.Editor editor = SettingActivity.this.getSharedPreferences("TokenData", Context.MODE_PRIVATE).edit();
                                                        editor.putString(TOKEN_KEY, "");
                                                        editor.putString(MID_KEY, "");
                                                        editor.apply();

                                                        //清空activity调用盏
                                                        Intent intent = new Intent(SettingActivity.this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        startActivity(intent);
                                                    }
                                                }).create();

                alertDialog.show();
            }
        });
    }

    //清理缓存方法
    private void clearCache(){

        progressDialog.show();
        DataCleanManager.clearAllCache(getApplicationContext());
        //判断是否运行在主线程，通过looper判断
        if (Looper.myLooper() == Looper.getMainLooper()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Glide.get(SettingActivity.this.getApplicationContext()).clearDiskCache();
                }
            }).start();
        } else {
            Glide.get(SettingActivity.this.getApplicationContext()).clearDiskCache();
        }

        try{

            long cacheSize = DataCleanManager.getCacheSize(this);

            while (cacheSize < 1){

                break;
            }

            progressDialog.dismiss();
            cacheTextView.setText(DataCleanManager.getTotalCacheSize(getApplicationContext()));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
