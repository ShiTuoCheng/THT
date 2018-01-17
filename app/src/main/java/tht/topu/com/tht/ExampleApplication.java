package tht.topu.com.tht;

import android.app.Application;
import android.support.multidex.MultiDexApplication;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by shituocheng on 13/12/2017.
 */

public class ExampleApplication extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        JPushInterface.init(this);
    }
}
