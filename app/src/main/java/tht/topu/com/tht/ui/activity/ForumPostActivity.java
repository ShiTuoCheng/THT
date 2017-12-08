package tht.topu.com.tht.ui.activity;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import tht.topu.com.tht.R;
import tht.topu.com.tht.ui.fragment.forumFragment.ForumContentFragment;
import tht.topu.com.tht.utils.API;
import tht.topu.com.tht.utils.Utilities;

public class ForumPostActivity extends AppCompatActivity {

    private RecyclerView imageRecyclerView;
    private TextView postButton;
    private TextView TagTextView;
//    private TextView CategoryTextView;

    private ImageView backButton;

    private EditText blogTitleEditText;
    private EditText blogContentEditText;

    private ListPopupWindow tagListPopupWindow;
//    private ListPopupWindow categoryPopupWindow;

    private LinearLayout postBlogLayout;

    private List<Bitmap> images = new ArrayList<>();
//    private List<String> tabTexts = new ArrayList<>();
    private List<String> tests = new ArrayList<>();
//    private List<String> tabCids = new ArrayList<>();

    private List<String> images64 = new ArrayList<>();
    private List<String> tagTexts = new ArrayList<>();
    private List<String> flids = new ArrayList<>();
    //private String imgBase64;

    private UploadImageAdapter uploadImageAdapter;
    public final static int CONSULT_DOC_PICTURE = 1000;
    private LocalBroadcastManager localBroadcastManager;
    private Intent broadcastIntent;

    public Handler uiHandler;
    public Handler alertHandler;

    private String mfid;

    private int uploadTimes = 0;

    public File file;

    public static final MediaType JSON = MediaType
            .parse("application/json; charset=utf-8");

    private static final String MID_KEY = "1x11";

    private String random32;
    private String time10;
    private String key64;

    private String currentFlid = "";
//    private String currentCid = "";

    private ProgressDialog progressDialog;

    private String flid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum_post);

        uiHandler = new Handler(getMainLooper());

        alertHandler = new Handler(){

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){

                    case 0:

                        Snackbar.make(postBlogLayout, "网络不可用", Snackbar.LENGTH_LONG).setAction("去设置", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                            }
                        }).show();

                        break;

                    case 1:
                        Toast toast = Toast.makeText(ForumPostActivity.this, "出现某些未知错误，请稍后再试", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.NO_GRAVITY, 0, Utilities.dip2px(ForumPostActivity.this, 200));
                        toast.show();

                        break;
                }
            }
        };
        // 判断网络是否可用
        if (!Utilities.isNetworkAvaliable(ForumPostActivity.this)){

            alertHandler.sendEmptyMessageDelayed(0,1000);
        }else {

//            getCategoryList();
            getTabTag();
        }
        images.add(null);
        initView();

        localBroadcastManager = LocalBroadcastManager.getInstance(this);

        Bundle bundle = getIntent().getBundleExtra("bundleFlid");
        flid = bundle.getString("flid");

        Log.d("flidData", flid);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ForumPostActivity.this.finish();
            }
        });
    }

    private void initView(){

        imageRecyclerView = (RecyclerView)findViewById(R.id.imageRecyclerView);
        TagTextView = (TextView)findViewById(R.id.forumPostTagTextView);
//        CategoryTextView = (TextView) findViewById(R.id.forumPostCategoryTextView);
        postButton = (TextView)findViewById(R.id.postButton);
        blogTitleEditText = (EditText)findViewById(R.id.blogTitleEditText);
        blogContentEditText = (EditText)findViewById(R.id.blogContentEditText);
        postBlogLayout = (LinearLayout)findViewById(R.id.postBlogLayout);
        backButton = (ImageView)findViewById(R.id.forumPostBack);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        uploadImageAdapter = new UploadImageAdapter(images);

        imageRecyclerView.setLayoutManager(layoutManager);
        imageRecyclerView.setAdapter(uploadImageAdapter);

        //标签列表初始化
        tagListPopupWindow = new ListPopupWindow(this);
        ArrayAdapter tagAdapter = new ArrayAdapter(this, R.layout.custom_spinner_text, tagTexts);
        tagListPopupWindow.setAdapter(tagAdapter);
        tagListPopupWindow.setWidth(Utilities.dip2px(this, 100));
        tagListPopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        tagListPopupWindow.setModal(true);
        //标签列表点击
        tagListPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                TagTextView.setText(tagTexts.get(i));
                currentFlid = flids.get(i);
                tagListPopupWindow.dismiss();
            }
        });

        //分类列表初始化
//        categoryPopupWindow = new ListPopupWindow(this);
//        ArrayAdapter categoryAdapter = new ArrayAdapter(this, R.layout.custom_spinner_text, tabTexts);
//        categoryPopupWindow.setAdapter(categoryAdapter);
//        categoryPopupWindow.setWidth(Utilities.dip2px(this, 100));
//        categoryPopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
//        categoryPopupWindow.setModal(true);
//        //标签列表点击
//        categoryPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//
//                CategoryTextView.setText(tabTexts.get(i));
//                currentCid = tabCids.get(i);
//                categoryPopupWindow.dismiss();
//            }
//        });
//
////        分类点击
//        CategoryTextView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                if (Build.VERSION.SDK_INT >=  Build.VERSION_CODES.KITKAT) {
//                    categoryPopupWindow.setDropDownGravity(Gravity.START);
//                }
//                categoryPopupWindow.setAnchorView(view);
//                categoryPopupWindow.show();
//            }
//        });

//        标签点击事件
        TagTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Build.VERSION.SDK_INT >=  Build.VERSION_CODES.KITKAT) {
                    tagListPopupWindow.setDropDownGravity(Gravity.START);
                }
                tagListPopupWindow.setAnchorView(view);
                tagListPopupWindow.show();
            }
        });

//      发帖点击事件
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String blogTitle = blogTitleEditText.getText().toString();
                String blogContent = blogContentEditText.getText().toString();

                postBlog(blogTitle, blogContent);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == CONSULT_DOC_PICTURE){
            if (resultCode == RESULT_OK && data != null){
                ClipData clipData = data.getClipData();

                if (clipData != null){

                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        Uri uri = clipData.getItemAt(i).getUri();
                        decodeUri(uri);

                        uploadImageAdapter.notifyDataSetChanged();
                    }
                }else {

                    decodeUri(data.getData());
                    uploadImageAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    //压缩图片
    public void decodeUri(Uri uri) {
        ParcelFileDescriptor parcelFD = null;
        try {
            parcelFD = this.getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor imageSource = parcelFD != null ? parcelFD.getFileDescriptor() : null;

            file = new File(String.valueOf(uri));

            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeFileDescriptor(imageSource, null, o);

            final int REQUIRED_SIZE = 1024;

            //剪裁图片
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp < REQUIRED_SIZE && height_tmp < REQUIRED_SIZE) {
                    break;
                }
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            //压缩图片
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            Bitmap bitmap = BitmapFactory.decodeFileDescriptor(imageSource, null, o2);

            if (images.size() > 9){
                Utilities.popUpAlert(ForumPostActivity.this, "最多只能上传9张图片");
            }else {

                images.add(bitmap);
                ByteArrayOutputStream bos=new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 20, bos);//参数100表示不压缩

                byte[] bytes=bos.toByteArray();
                images64.add(("data:image/jpeg;base64,"+ Base64.encodeToString(bytes, Base64.NO_WRAP)));
            }

        } catch (FileNotFoundException e) {
            // handle errors
        } finally {
            if (parcelFD != null)
                try {
                    parcelFD.close();
                } catch (IOException e) {
                    // ignored
                }
        }
    }

    //获取标签
    private void getTabTag(){

        random32 = Utilities.getStringRandom(32);
        time10 = Utilities.get10Time();
        key64 = Utilities.get64Key(random32);

        String json = "{\n" +
                "    \"validate_k\": \"1\",\n" +
                "    \"params\": [\n" +
                "        {\n" +
                "            \"type\": \"Forum_Label\",\n" +
                "            \"act\": \"Select_List\",\n" +
                "            \"para\": {\n" +
                "                \"params\": {\n" +
                "                    \"s_Alive\": \"\",\n" +
                "                    \"s_Flid\": \"\",\n" +
                "                    \"s_Keywords\": \"\",\n" +
                "                    \"s_Order\": \"Layer\",\n" +
                "                    \"s_Stem_from\": \"2\",\n" +
                "                    \"s_Total_parameter\": \"Flid,Ltitle,Layer,Alive,Stem_from\"\n" +
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
                "                    \"signature\": \""+Utilities.encode("s_Alive="+"s_Flid="+"s_Keywords="+"s_Order=Layer"+"s_Stem_from=2"+"s_Total_parameter=Flid,Ltitle,Layer,Alive,Stem_from"+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
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

                        alertHandler.sendEmptyMessageDelayed(1,1000);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.body() != null){

                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        JSONArray jsonArr = jsonObject.getJSONArray("result").getJSONObject(0).getJSONArray("list");

                        for (int i=0; i<jsonArr.length(); i++){

                            String tagText = jsonArr.getJSONObject(i).getString("Ltitle");
                            String flid = jsonArr.getJSONObject(i).getString("Flid");

                            flids.add(flid);
                            tagTexts.add(tagText);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    //获取分类名称和cid方法
//    private void getCategoryList(){
//
//        random32 = Utilities.getStringRandom(32);
//        time10 = Utilities.get10Time();
//        key64 = Utilities.get64Key(random32);
//
//        String json = "{\n" +
//                "    \"validate_k\": \"1\",\n" +
//                "    \"params\": [\n" +
//                "        {\n" +
//                "            \"type\": \"Classification\",\n" +
//                "            \"act\": \"Select_List\",\n" +
//                "            \"para\": {\n" +
//                "                \"params\": {\n" +
//                "                    \"s_Alive\": \"\",\n" +
//                "                    \"s_Cid\": \"\",\n" +
//                "                    \"s_Keywords\": \"\",\n" +
//                "                    \"s_Kind\": \"2\",\n" +
//                "                    \"s_Order\": \"\",\n" +
//                "                    \"s_Stem_from\":\"2\",\n" +
//                "                    \"s_Total_parameter\": \"Cid,Ctitle,Pic1,Pic2,Layer,Alive,Stem_from\"\n" +
//                "                },\n" +
//                "                \"pages\": {\n" +
//                "                    \"p_c\": \"\",\n" +
//                "                    \"p_First\": \"\",\n" +
//                "                    \"p_inputHeight\": \"\",\n" +
//                "                    \"p_Last\": \"\",\n" +
//                "                    \"p_method\": \"\",\n" +
//                "                    \"p_Next\": \"\",\n" +
//                "                    \"p_Page\": \"\",\n" +
//                "                    \"p_pageName\": \"\",\n" +
//                "                    \"p_PageStyle\": \"\",\n" +
//                "                    \"p_Pname\": \"\",\n" +
//                "                    \"p_Previous\": \"\",\n" +
//                "                    \"p_Ps\": \"\",\n" +
//                "                    \"p_sk\": \"\",\n" +
//                "                    \"p_Tp\": \"\"\n" +
//                "                },\n" +
//                "                \"sign_valid\": {\n" +
//                "                    \"source\": \"Android\",\n" +
//                "                    \"non_str\": \""+random32+"\",\n" +
//                "                    \"stamp\": \""+time10+"\",\n" +
//                "                    \"signature\": \""+Utilities.encode("s_Alive="+"s_Cid="+"s_Keywords="+"s_Kind=2"+"s_Order="+"s_Stem_from=2"+"s_Total_parameter=Cid,Ctitle,Pic1,Pic2,Layer,Alive,Stem_from"+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
//                "                }\n" +
//                "            }\n" +
//                "        }\n" +
//                "    ]\n" +
//                "}";
//
//        OkHttpClient okHttpClient = new OkHttpClient();
//
//        RequestBody requestBody = RequestBody.create(JSON, json);
//
//        Request request = new Request.Builder().url(API.getAPI()).post(requestBody).build();
//
//        okHttpClient.newCall(request).enqueue(new Callback() {
//
//            @Override
//            public void onFailure(Call call, IOException e) {
//
//
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//
//                if (response.body() != null){
//
//                    try {
//                        JSONObject resultJson = new JSONObject(response.body().string());
//                        JSONArray imgArr = resultJson.getJSONArray("result").getJSONObject(0).getJSONArray("list");
//
//                        tabTexts.clear();
//                        tabCids.clear();
//
//                        for (int i=0; i<imgArr.length(); i++){
//
//                            JSONObject iconObj = imgArr.getJSONObject(i);
//                            tabTexts.add(iconObj.getString("Ctitle"));
//                            tabCids.add(iconObj.getString("Cid"));
//                        }
//
//                    }catch (JSONException e){
//
//                        e.printStackTrace();
//                    }
//            }
//        }});
//    }

    //点击发送按钮发送一条帖子
    private void postBlog(String blogTitle, String blogContent){

        random32 = Utilities.getStringRandom(32);
        time10 = Utilities.get10Time();
        key64 = Utilities.get64Key(random32);

        if (blogTitle.equals("")){

            Utilities.popUpAlert(this, "帖子标题不能为空");

            return;
        }else if (blogContent.equals("")){

            Utilities.popUpAlert(this, "帖子内容不能为空");
            return;
//        }else if (currentCid.equals("")){
//
//            Utilities.popUpAlert(this, "必须选择一种分类");
//            return;
        }else if (currentFlid.equals("")){

            Utilities.popUpAlert(this, "必须选择一个标签");
            return;
        }

        SharedPreferences sharedPreferences = getSharedPreferences("TokenData", MODE_PRIVATE);
        String mid = sharedPreferences.getString(MID_KEY, "");
        final ProgressDialog progressDialog = ProgressDialog.show(ForumPostActivity.this, "正在发送", "正在发送帖子", false);

        progressDialog.setCancelable(false);
        final String json = "{\n" +
                "    \"validate_k\": \"1\",\n" +
                "    \"params\": [\n" +
                "        {\n" +
                "            \"type\": \"Forum\",\n" +
                "            \"act\": \"Add\",\n" +
                "            \"para\": {\n" +
                "                \"params\": {\n" +
                "                    \"Add_Essence\": \"false\",\n" +
                "                    \"Cid\": \"12\",\n" +
                "                    \"Finfo\": \""+blogContent+"\",\n" +
                "                    \"Flid\": \""+currentFlid+"\",\n" +
                "                    \"Ftitle\": \""+blogTitle+"\",\n" +
                "                    \"isTop\": \"false\",\n" +
                "                    \"Mid\": \""+mid+"\",\n" +
                "                    \"Stem_from\": \"2\"\n" +
                "                },\n" +
                "                \"sign_valid\": {\n" +
                "                    \"source\": \"Android\",\n" +
                "                    \"non_str\": \""+random32+"\",\n" +
                "                    \"stamp\": \""+time10+"\",\n" +
                "                    \"signature\": \""+Utilities.encode("Add_Essence=false"+"Cid=12"+"Finfo="+blogContent+"Flid="+currentFlid+"Ftitle="+blogTitle+"isTop=false"+"Mid="+mid+"Stem_from=2"+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
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

                        progressDialog.dismiss();
                        Toast.makeText(ForumPostActivity.this, "出了点小差错，发送失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {

                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            JSONObject jsonObj = new JSONObject(response.body().string());

                            mfid = jsonObj.getJSONArray("result").getJSONObject(0).getString("Fid");

                            //判断是否有上传图片 若有图片则调用添加图片方法
                            if (images64.size() == 0){

                                broadcastIntent = new Intent("com.example.mybroadcast.MY_BROADCAST");
                                broadcastIntent.putExtra("Flid", flid);
                                localBroadcastManager.sendBroadcast(broadcastIntent);
                                ForumPostActivity.this.finish();
                                progressDialog.dismiss();
                                Toast.makeText(ForumPostActivity.this, "发帖成功", Toast.LENGTH_SHORT).show();
                            }else {

                                broadcastIntent = new Intent("com.example.mybroadcast.MY_BROADCAST");
                                broadcastIntent.putExtra("Flid", flid);
                                localBroadcastManager.sendBroadcast(broadcastIntent);
                                uploadImage(mfid, images64.get(0));

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    //上传图片方法
    private void uploadImage(final String fid, String eachImage64){

        random32 = Utilities.getStringRandom(32);
        time10 = Utilities.get10Time();
        key64 = Utilities.get64Key(random32);

        String json = "{\n" +
                "    \"validate_k\": \"1\",\n" +
                "    \"params\": [\n" +
                "        {\n" +
                "            \"type\": \"Forum_Pic\",\n" +
                "            \"act\": \"Add\",\n" +
                "            \"para\": {\n" +
                "                \"params\": {\n" +
                "                    \"Fid\": \""+fid+"\",\n" +
                "                    \"Pic1\": \""+eachImage64+"\"\n" +
                "                },\n" +
                "                \"sign_valid\": {\n" +
                "                    \"source\": \"Android\",\n" +
                "                    \"non_str\": \""+random32+"\",\n" +
                "                    \"stamp\": \""+time10+"\",\n" +
                "                    \"signature\": \""+Utilities.encode("Fid="+fid+"Pic1="+eachImage64+"non_str="+random32+"stamp="+time10+"keySecret="+key64)+"\"\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        uploadTimes++;

        Log.d("test", String.valueOf(uploadTimes));

        OkHttpClient okHttpClient = new OkHttpClient();

        RequestBody requestBody = RequestBody.create(JSON, json);

        Request request = new Request.Builder().url(API.getAPI()).post(requestBody).build();

        okHttpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {

                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ForumPostActivity.this, "出了点小差错，发帖失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.body() != null){

                    try {

                        final JSONObject jsonObject = new JSONObject(response.body().string());
                        final String result = jsonObject.getJSONArray("result").getJSONObject(0).getString("error");
                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {

                                if (!result.equals("SUCCESS")){

                                    Toast.makeText(ForumPostActivity.this, "出了点小差错，发帖失败", Toast.LENGTH_SHORT).show();
                                }else {

                                    if (uploadTimes == images64.size()){

                                        Toast.makeText(ForumPostActivity.this, "发帖成功", Toast.LENGTH_SHORT).show();


                                        progressDialog.dismiss();
                                        ForumPostActivity.this.finish();

                                    }else {

                                        uploadImage(mfid, images64.get(uploadTimes));
                                    }
                                }

                                Log.d("success", jsonObject.toString());
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    //上传图片列表类
    private class UploadImageAdapter extends RecyclerView.Adapter{

        List<Bitmap> imageUrls = new ArrayList<>();

        private final int ADD_IMAGE_VIEW = 0;
        private final int SHOW_IMAGE_VIEW = 1;

        public UploadImageAdapter(List<Bitmap> imageUrls) {
            this.imageUrls = imageUrls;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            RecyclerView.ViewHolder viewHolder;

            if (viewType == SHOW_IMAGE_VIEW){

                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_upload_image, parent, false);

                viewHolder = new ImageViewHolder(v);
            }else{

                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_upload_add, parent, false);

                viewHolder = new UploadImageViewHolder(v);
            }
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

            if (holder instanceof ImageViewHolder){

                final Bitmap imageUrl = imageUrls.get(position);
                Log.d("position", String.valueOf(position)+String.valueOf(images.size())+String.valueOf(imageUrls.size()));

                ((ImageViewHolder)holder).imageView.setImageBitmap(imageUrl);
                ((ImageViewHolder)holder).deleteImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                            uploadImageAdapter.notifyItemRemoved(position);
                            uploadImageAdapter.notifyDataSetChanged();
                            images.remove(position);
                    }
                });
            }else{

                ((UploadImageViewHolder)holder).imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent intent = new Intent();

                        intent.setType("image/*");
                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent,"选择一张照片"), CONSULT_DOC_PICTURE);

                        //startActivityForResult(intent, CONSULT_DOC_PICTURE);

                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return imageUrls.size();
        }

        @Override
        public int getItemViewType(int position) {
            return images.get(position) != null ?  SHOW_IMAGE_VIEW : ADD_IMAGE_VIEW;
        }

        private class ImageViewHolder extends RecyclerView.ViewHolder{

            private ImageView imageView;
            private ImageView deleteImg;

            private ImageViewHolder(View itemView) {
                super(itemView);
                imageView = (ImageView)itemView.findViewById(R.id.uploadImageView);
                deleteImg = (ImageView)itemView.findViewById(R.id.deleteImg);

            }
        }

        private class UploadImageViewHolder extends RecyclerView.ViewHolder{

            private ImageView imageView;

            public UploadImageViewHolder(View itemView) {
                super(itemView);
                imageView = (ImageView)itemView.findViewById(R.id.addImageView);
            }
        }
    }
}
