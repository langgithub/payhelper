package com.lang.payhelper.payhook;

import com.lang.payhelper.handler.ZfbApp;
import com.lang.payhelper.handler.ZfbHandler;
import com.lang.payhelper.utils.PayHelperUtils;
import com.lang.payhelper.xp.hook.BaseHook;
import com.lang.sekiro.netty.client.SekiroClient;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;

import java.util.UUID;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * 

* @ClassName: Main

* @Description: TODO(这里用一句话描述这个类的作用)

* @author SuXiaoliang

* @date 2018年6月23日 下午1:26:26

*
 */
public class Main extends BaseHook {
	public static String WECHAT_PACKAGE = "com.tencent.mm";
	public static String ALIPAY_PACKAGE = "com.eg.android.AlipayGphone";
	public static String QQ_PACKAGE = "com.tencent.mobileqq";
	public static String QQ_WALLET_PACKAGE = "com.qwallet";
	public static boolean WECHAT_PACKAGE_ISHOOK = false;
	public static boolean ALIPAY_PACKAGE_ISHOOK = false;
	public static boolean QQ_PACKAGE_ISHOOK = false;
	public static boolean QQ_WALLET_ISHOOK = false;


    @Override
    public void onLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        handleLoadPackage(lpparam);
    }

    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam)
			throws Throwable {

		if (lpparam.appInfo == null || (lpparam.appInfo.flags & (ApplicationInfo.FLAG_SYSTEM |
                ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0) {
            return;
        }
		final String packageName = lpparam.packageName;
        final String processName = lpparam.processName;
        if (WECHAT_PACKAGE.equals(packageName)) {
    		try {
                XposedHelpers.findAndHookMethod(ContextWrapper.class, "attachBaseContext", Context.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        Context context = (Context) param.args[0];
                        ClassLoader appClassLoader = context.getClassLoader();
                        if(WECHAT_PACKAGE.equals(processName) && !WECHAT_PACKAGE_ISHOOK){
                        	WECHAT_PACKAGE_ISHOOK=true;
                        	//获取版本信息
                        	PayHelperUtils.sendmsg(context, "微信Hook成功，当前微信版本:"+PayHelperUtils.getVerName(context));
                        	//自付宝hook
                        	new WechatHook().hook(appClassLoader,context);
                        	//主动打开收款嘛

                        }
                    }
                });
            } catch (Throwable e) {
                XposedBridge.log(e);
            }
        }else if(ALIPAY_PACKAGE.equals(packageName)){
			XposedBridge.log(">>>>>>>>>>>>>>>>>>>>>>>>zfb_hook,并启动服务器: " + lpparam.packageName+ALIPAY_PACKAGE_ISHOOK);
//            final SekiroClient sekiroClient = SekiroClient.start("sekiro.virjar.com", UUID.randomUUID().toString(), "zfb-lang");
			try {
                XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);

						Context context = (Context) param.args[0];
                        ClassLoader appClassLoader = context.getClassLoader();
                        if(ALIPAY_PACKAGE.equals(processName) && !ALIPAY_PACKAGE_ISHOOK){
                        	ALIPAY_PACKAGE_ISHOOK=true;
                        	//注册广播
                            StartServerReceived startServerReceived=new StartServerReceived();
                            IntentFilter server = new IntentFilter();
                            server.addAction("com.payhelper.tcp.start");
                            context.registerReceiver(startServerReceived, server);

                            StartAlipayReceived startAlipay=new StartAlipayReceived();
                            IntentFilter intentFilter = new IntentFilter();
                            intentFilter.addAction("com.payhelper.alipay.start");
                            context.registerReceiver(startAlipay, intentFilter);

                            StartAlipayQr startAlipayQr=new StartAlipayQr();
                            IntentFilter intentFilter2 = new IntentFilter();
                            intentFilter2.addAction("com.payhelper.alipay.start2");
                            context.registerReceiver(startAlipayQr, intentFilter2);

                            ZfbApp zfbApp= ZfbApp.newInstance();
                            XposedBridge.log(">>>>>>>>zfb_hook begin >>"+(zfbApp.getContext()==null));
                            if (zfbApp.getContext()==null){
                                zfbApp.setContext(context);
                            }

                        	XposedBridge.log("handleLoadPackage: " + packageName);
                        	PayHelperUtils.sendmsg(context, "支付宝Hook成功，当前支付宝版本:"+PayHelperUtils.getVerName(context));
                        	new AlipayHook().hook(appClassLoader,context);
                        }
                    }
                });
    		}catch (Throwable e) {
                XposedBridge.log(e);
            }
        }else if(QQ_PACKAGE.equals(packageName)){   
        }
	}

    //自定义启动服务器
    class StartServerReceived extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final SekiroClient sekiroClient = SekiroClient.start(intent.getStringExtra("address"),5600, UUID.randomUUID().toString(), "zfb_"+intent.getStringExtra("account"));
            sekiroClient.registerHandler("zfbAppHandler", new ZfbHandler());
            PayHelperUtils.sendmsg(context,"服务器启动成功 接口访问地址:http://"+intent.getStringExtra("address")+":5602/asyncInvoke?group=zfb_"+intent.getStringExtra("account")+"&action=zfbAppHandler");
        }
    }

    //自定义启动支付宝广播
    class StartAlipayReceived extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
    		Intent intent2=new Intent(context, XposedHelpers.findClass("com.alipay.mobile.payee.ui.PayeeQRSetMoneyActivity", context.getClassLoader()));
    		intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    		intent2.putExtra("mark", intent.getStringExtra("mark"));
    		intent2.putExtra("money", intent.getStringExtra("money"));
    		context.startActivity(intent2);
        }
    }

    //自定义启动支付宝广播
    class StartAlipayQr extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent intent2=new Intent(context, XposedHelpers.findClass("com.alipay.mobile.payee.ui.PayeeQRActivity", context.getClassLoader()));
//            intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent2.putExtra("mark", intent.getStringExtra("mark"));
//            intent2.putExtra("money", intent.getStringExtra("money"));
            context.startActivity(intent2);
        }
    }

}
