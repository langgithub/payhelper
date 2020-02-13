package com.lang.payhelper.xp.hook.code.action.impl;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;

import com.lang.payhelper.R;
import com.lang.payhelper.common.constant.NotificationConst;
import com.lang.payhelper.common.utils.XLog;
import com.lang.payhelper.common.utils.XSPUtils;
import com.lang.payhelper.data.db.entity.SmsMsg;
import com.lang.payhelper.xp.hook.code.CopyCodeReceiver;
import com.lang.payhelper.xp.hook.code.action.CallableAction;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import de.robv.android.xposed.XSharedPreferences;

/**
 * 显示验证码通知
 */
public class NotifyAction extends CallableAction {

    public static final String NOTIFY_RETENTION_TIME = "notify_retention_time";
    public static final String NOTIFY_ID = "notify_id";

    public NotifyAction(Context appContext, Context phoneContext, SmsMsg smsMsg, XSharedPreferences xsp) {
        super(appContext, phoneContext, smsMsg, xsp);
    }

    @Override
    public Bundle action() {
        if (XSPUtils.showCodeNotification(xsp)) {
            return showCodeNotification(mSmsMsg);
        }
        return null;
    }

    private Bundle showCodeNotification(SmsMsg smsMsg) {
        NotificationManager manager = (NotificationManager) mPhoneContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) {
            return null;
        }

        String company = smsMsg.getCompany();
        String smsCode = smsMsg.getSmsCode();
        String title = TextUtils.isEmpty(company) ? smsMsg.getSender() : company;
        String content = mAppContext.getString(R.string.code_notification_content, smsCode);

        int notificationId = smsMsg.hashCode();

        Intent copyCodeIntent = CopyCodeReceiver.createIntent(smsCode);
        PendingIntent contentIntent = PendingIntent.getBroadcast(mPhoneContext,
                0, copyCodeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(mAppContext, NotificationConst.CHANNEL_ID_SMSCODE_NOTIFICATION)
                .setSmallIcon(R.drawable.ic_app_icon)
                .setLargeIcon(BitmapFactory.decodeResource(mAppContext.getResources(), R.drawable.ic_app_icon))
                .setWhen(System.currentTimeMillis())
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(mAppContext, R.color.ic_launcher_background))
                .setGroup(NotificationConst.GROUP_KEY_SMSCODE_NOTIFICATION)
                .build();

        manager.notify(notificationId, notification);
        XLog.d("Show notification succeed");

        if (XSPUtils.autoCancelCodeNotification(xsp)) {
            long retentionTime = XSPUtils.getNotificationRetentionTime(xsp) * 1000;
            Bundle bundle = new Bundle();
            bundle.putLong(NOTIFY_RETENTION_TIME, retentionTime);
            bundle.putInt(NOTIFY_ID, notificationId);
            return bundle;
        }
        return null;
    }
}
