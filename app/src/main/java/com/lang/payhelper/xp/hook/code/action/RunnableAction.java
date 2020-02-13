package com.lang.payhelper.xp.hook.code.action;

import android.content.Context;

import com.lang.payhelper.data.db.entity.SmsMsg;

import de.robv.android.xposed.XSharedPreferences;

/**
 * Runnable + Action + Callable
 */
public abstract class RunnableAction extends CallableAction implements Runnable {

    public RunnableAction(Context appContext, Context phoneContext, SmsMsg smsMsg, XSharedPreferences xsp) {
        super(appContext, phoneContext, smsMsg, xsp);
    }

    @Override
    public void run() {
        call();
    }
}
