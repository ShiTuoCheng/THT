package tht.topu.com.tht.utils;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * Created by shituocheng on 25/09/2017.
 */

public class ExampleApplication extends Application{
        @Override
        public void onCreate() {
            super.onCreate();
            LeakCanary.install(this);
        }
}
