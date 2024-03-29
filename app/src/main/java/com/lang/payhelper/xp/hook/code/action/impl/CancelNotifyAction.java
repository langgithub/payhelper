package com.lang.payhelper.xp.hook.code.action.impl;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;

import com.lang.payhelper.common.utils.XLog;
import com.lang.payhelper.data.db.entity.SmsMsg;
import com.lang.payhelper.xp.hook.code.action.CallableAction;

import de.robv.android.xposed.XSharedPreferences;

public class CancelNotifyAction extends CallableAction {

    private static final int NOTIFICATION_NONE = -0xff;

    private int mNotificationId = NOTIFICATION_NONE;

    public CancelNotifyAction(Context appContext, Context phoneContext, SmsMsg smsMsg, XSharedPreferences xsp) {
        super(appContext, phoneContext, smsMsg, xsp);
    }

    public void setNotificationId(int notificationId) {
        mNotificationId = notificationId;
    }

    @Override
    public Bundle action() {
        cancelNotification();
        return null;
    }

    private void cancelNotification() {
        if (mNotificationId != NOTIFICATION_NONE) {
            NotificationManager manager = (NotificationManager) mPhoneContext.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager == null) {
                return;
            }
            manager.cancel(mNotificationId);
            XLog.d("Notification auto cancelled");
        }
    }
}
