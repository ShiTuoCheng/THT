package tht.topu.com.tht;

import android.app.Application;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by shituocheng on 13/12/2017.
 */

public class ExampleApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        JPushInterface.setDebugMode(true);
        JPushInterface.init(this);
    }
}
