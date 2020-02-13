package com.lang.payhelper.xp.hook.code.action.impl;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.lang.payhelper.R;
import com.lang.payhelper.common.utils.XSPUtils;
import com.lang.payhelper.data.db.entity.SmsMsg;
import com.lang.payhelper.xp.hook.code.action.RunnableAction;

import de.robv.android.xposed.XSharedPreferences;

/**
 * 显示验证码Toast
 */
public class ToastAction extends RunnableAction {

    public ToastAction(Context appContext, Context phoneContext, SmsMsg smsMsg, XSharedPreferences xsp) {
        super(appContext, phoneContext, smsMsg, xsp);
    }

    @Override
    public Bundle action() {
        if (XSPUtils.shouldShowToast(xsp)) {
            showCodeToast();
        }
        return null;
    }

    private void showCodeToast() {
        String text = mAppContext.getString(R.string.current_sms_code, mSmsMsg.getSmsCode());
        Toast.makeText(mAppContext, text, Toast.LENGTH_LONG).show();
    }
}
