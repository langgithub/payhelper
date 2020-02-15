package com.lang.payhelper.handler;

import android.content.Context;
import android.util.Log;

public class ZfbApp {

    private static ZfbApp zfbApp=null;

    private Context context=null;

    public void setContext(Context context) {
        Log.i("Xposed","setContext");
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    private ZfbApp(){}

    public static ZfbApp newInstance(){
        if (zfbApp==null){
            synchronized (ZfbApp.class){
                while (zfbApp==null) {
                    zfbApp = new ZfbApp();
                    Log.i("Xposed","new ZfbApp()");
                }
            }
        }
        return zfbApp;
    }
}
