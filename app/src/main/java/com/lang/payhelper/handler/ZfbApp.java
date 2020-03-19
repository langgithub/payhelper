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

    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    private Object pointer;

    private Object pointer2;

    public void setPointer2(Object pointer2) {
        this.pointer2 = pointer2;
    }

    public Object getPointer2() {
        return pointer2;
    }

    public void setPointer(Object pointer) {
        this.pointer = pointer;
    }

    public Object getPointer() {
        return pointer;
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
