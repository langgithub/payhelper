package com.lang.payhelper.xp.hook.code.action;

import android.content.Context;
import android.os.Bundle;

import com.lang.payhelper.common.utils.XLog;
import com.lang.payhelper.data.db.entity.SmsMsg;

import java.util.concurrent.Callable;

import de.robv.android.xposed.XSharedPreferences;

/**
 * Action + Callable
 */
public abstract class CallableAction implements Action<Bundle>, Callable<Bundle> {

    protected Context mAppContext;
    protected Context mPhoneContext;
    protected SmsMsg mSmsMsg;
    protected XSharedPreferences xsp;

    public CallableAction(Context appContext, Context phoneContext, SmsMsg smsMsg, XSharedPreferences xsp) {
        mAppContext = appContext;
        mPhoneContext = phoneContext;
        mSmsMsg = smsMsg;
        this.xsp = xsp;
    }

    @Override
    public Bundle call() {
        try {
            return action();
        } catch (Throwable t) {
            XLog.e("Error in CallableAction#call()", t);
            return null;
        }
    }
}
