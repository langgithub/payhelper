package com.lang.payhelper.xp.hook.code;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.lang.payhelper.BuildConfig;
import com.lang.payhelper.MainActivity;
import com.lang.payhelper.common.utils.XLog;
import com.lang.payhelper.common.utils.XSPUtils;
import com.lang.payhelper.data.db.entity.SmsMsg;
import com.lang.payhelper.xp.hook.code.action.impl.AutoInputAction;
import com.lang.payhelper.xp.hook.code.action.impl.CancelNotifyAction;
import com.lang.payhelper.xp.hook.code.action.impl.CopyToClipboardAction;
import com.lang.payhelper.xp.hook.code.action.impl.KillMeAction;
import com.lang.payhelper.xp.hook.code.action.impl.NotifyAction;
import com.lang.payhelper.xp.hook.code.action.impl.OperateSmsAction;
import com.lang.payhelper.xp.hook.code.action.impl.RecordSmsAction;
import com.lang.payhelper.xp.hook.code.action.impl.SmsParseAction;
import com.lang.payhelper.xp.hook.code.action.impl.ToastAction;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;

public class CodeWorker {

    private Context mPhoneContext;
    private Context mAppContext;
    private XSharedPreferences xsp;
    private Intent mSmsIntent;

    private Handler mUIHandler;

    private ScheduledExecutorService mScheduledExecutor;

    CodeWorker(Context appContext, Context phoneContext, Intent smsIntent) {
        mAppContext = appContext;
        mPhoneContext = phoneContext;
        xsp = new XSharedPreferences(BuildConfig.APPLICATION_ID);
        mSmsIntent = smsIntent;

        mUIHandler = new Handler(Looper.getMainLooper());

        mScheduledExecutor = Executors.newSingleThreadScheduledExecutor();
    }

    public ParseResult parse() {
        if (!XSPUtils.isEnabled(xsp)) {
            XLog.i("XposedSmsCode disabled, exiting");
            return null;
        }

        boolean verboseLog = XSPUtils.isVerboseLogMode(xsp);
        if (verboseLog) {
            XLog.setLogLevel(Log.VERBOSE);
        } else {
            XLog.setLogLevel(BuildConfig.LOG_LEVEL);
        }

        SmsParseAction smsParseAction = new SmsParseAction(mAppContext, mPhoneContext, null, xsp);
        smsParseAction.setSmsIntent(mSmsIntent);
        ScheduledFuture<Bundle> smsParseFuture = mScheduledExecutor.schedule((Callable<Bundle>) smsParseAction, 0, TimeUnit.MILLISECONDS);
        SmsMsg smsMsg;
        try {
            Bundle parseBundle = smsParseFuture.get();
            if (parseBundle == null) {
                return null;
            }


            boolean duplicated = parseBundle.getBoolean(SmsParseAction.SMS_DUPLICATED, false);
            if (duplicated) {
                return buildParseResult();
            }

            smsMsg = parseBundle.getParcelable(SmsParseAction.SMS_MSG);
        } catch (Exception e) {
            XLog.e("Error occurs when get SmsParseAction call value", e);
            return null;
        }

        //发送到回调
        Intent broadCastIntent = new Intent();
        Map<String, String> map=new HashMap<>();
        map.put("sender",smsMsg.getSender());
        map.put("code",smsMsg.getSmsCode());
        map.put("all",smsMsg.getBody());
        map.put("time",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(smsMsg.getDate()));

        broadCastIntent.putExtra("json", new JSONObject(map).toString());
        XLog.i(new JSONObject(map).toString());
        broadCastIntent.setAction("com.lang.sms");
        mAppContext.sendBroadcast(broadCastIntent);

//        // 复制到剪切板 Action
//        mUIHandler.post(new CopyToClipboardAction(mAppContext, mPhoneContext, smsMsg, xsp));
//
//        // 显示Toast Action
//        mUIHandler.post(new ToastAction(mAppContext, mPhoneContext, smsMsg, xsp));
//
//        // 自动输入 Action
//        AutoInputAction autoInputAction = new AutoInputAction(mAppContext, mPhoneContext, smsMsg, xsp);
//        mScheduledExecutor.schedule(autoInputAction, 0, TimeUnit.MILLISECONDS);
//
//        // 显示通知 Action
//        NotifyAction notifyAction = new NotifyAction(mAppContext, mPhoneContext, smsMsg, xsp);
//        ScheduledFuture<Bundle> notificationFuture = mScheduledExecutor.schedule(notifyAction, 0, TimeUnit.MILLISECONDS);
//
//        // 记录验证码短信 Action
//        RecordSmsAction recordSmsAction = new RecordSmsAction(mAppContext, mPhoneContext, smsMsg, xsp);
//        mScheduledExecutor.schedule(recordSmsAction, 0, TimeUnit.MILLISECONDS);
//
//        // 操作验证码短信（标记为已读 或者 删除） Action
//        OperateSmsAction operateSmsAction = new OperateSmsAction(mAppContext, mPhoneContext, smsMsg, xsp);
//        mScheduledExecutor.schedule(operateSmsAction, 3000, TimeUnit.MILLISECONDS);
//
//        // 自杀 Action
//        KillMeAction action = new KillMeAction(mAppContext, mPhoneContext, smsMsg, xsp);
//        mScheduledExecutor.schedule(action, 4000, TimeUnit.MILLISECONDS);
//
//        try {
//            // 清除通知
//            Bundle bundle = notificationFuture.get();
//            if (bundle != null && bundle.containsKey(NotifyAction.NOTIFY_RETENTION_TIME)) {
//                long delay = bundle.getLong(NotifyAction.NOTIFY_RETENTION_TIME, 0L);
//                int notificationId = bundle.getInt(NotifyAction.NOTIFY_ID, 0);
//                CancelNotifyAction cancelNotifyAction = new CancelNotifyAction(mAppContext, mPhoneContext, smsMsg, xsp);
//                cancelNotifyAction.setNotificationId(notificationId);
//
//                mScheduledExecutor.schedule(cancelNotifyAction, delay, TimeUnit.MILLISECONDS);
//            }
//        } catch (Exception e) {
//            XLog.e("Error in notification future get()", e);
//        }

        return buildParseResult();
    }

    private ParseResult buildParseResult() {
        ParseResult parseResult = new ParseResult();
        parseResult.setBlockSms(XSPUtils.blockSmsEnabled(xsp));
        return parseResult;
    }
}
