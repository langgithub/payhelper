package com.lang.payhelper.payhook;

import com.lang.payhelper.handler.TokenRedBackHandler;
import com.lang.payhelper.handler.ZfbApp;
import com.lang.payhelper.handler.ZfbHuabeiHandler;
import com.lang.payhelper.handler.ZfbQr1Handler;
import com.lang.payhelper.handler.ZfbQr2Handler;
import com.lang.payhelper.utils.PayHelperUtils;
import com.lang.payhelper.xp.hook.BaseHook;
import com.lang.sekiro.netty.client.SekiroClient;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import java.util.UUID;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * hook 主入口
 */
public class Main extends BaseHook {
	public static String WECHAT_PACKAGE = "com.tencent.mm";
	public static String ALIPAY_PACKAGE = "com.eg.android.AlipayGphone";
	public static String QQ_PACKAGE = "com.tencent.mobileqq";
	public static boolean ALIPAY_PACKAGE_ISHOOK = false;

    @Override
    public void onLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        handleLoadPackage(lpparam);
    }

    /**
     * Xposed hook
     * @param lpparam
     */
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {

		if (lpparam.appInfo == null || (lpparam.appInfo.flags & (ApplicationInfo.FLAG_SYSTEM |
                ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0) {
            return;
        }
		final String packageName = lpparam.packageName;
        final String processName = lpparam.processName;
        if (WECHAT_PACKAGE.equals(packageName)) {

        }else if(ALIPAY_PACKAGE.equals(packageName)){
			try {
                XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);

						Context context = (Context) param.args[0];
                        ClassLoader appClassLoader = context.getClassLoader();
                        if(ALIPAY_PACKAGE.equals(processName) && !ALIPAY_PACKAGE_ISHOOK){
                        	ALIPAY_PACKAGE_ISHOOK=true;

                            StartAlipayReceived startAlipay=new StartAlipayReceived();
                            IntentFilter intentFilter = new IntentFilter();
                            intentFilter.addAction("com.payhelper.alipay.start");
                            intentFilter.addAction("com.payhelper.alipay.makehuabei");
                            intentFilter.addAction("com.payhelper.tcp.start");
                            intentFilter.addAction("com.payhelper.alipay.start2");
                            context.registerReceiver(startAlipay, intentFilter);

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

    //自定义启动支付宝广播
    class StartAlipayReceived extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("com.payhelper.alipay.start")) {
                Intent intent2=new Intent(context, XposedHelpers.findClass("com.alipay.mobile.payee.ui.PayeeQRSetMoneyActivity", context.getClassLoader()));
                intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent2.putExtra("mark", intent.getStringExtra("mark"));
                intent2.putExtra("money", intent.getStringExtra("money"));
                context.startActivity(intent2);
            }else if(intent.getAction().equals("com.payhelper.alipay.makehuabei")){

            }else if(intent.getAction().equals("com.payhelper.tcp.start")){
                if(ALIPAY_PACKAGE_ISHOOK) {
                    final SekiroClient sekiroClient = SekiroClient.start(intent.getStringExtra("address"), 5600, UUID.randomUUID().toString(), "zfb_" + intent.getStringExtra("account"));
                    sekiroClient.registerHandler("zfbAppHandler", new ZfbQr1Handler());
                    sekiroClient.registerHandler("zfb2AppHandler", new ZfbQr2Handler());
                    sekiroClient.registerHandler("tokenRed", new TokenRedBackHandler());
                    sekiroClient.registerHandler("zfbHuabei",new ZfbHuabeiHandler());
                    PayHelperUtils.sendmsg(context, "服务器启动成功 接口访问地址:http://" + intent.getStringExtra("address") + ":5601/asyncInvoke?group=zfb_" + intent.getStringExtra("account") + "&action=zfbAppHandler");
                }
            }else if(intent.getAction().equals("com.payhelper.alipay.start2")){
                Intent intent2=new Intent(context, XposedHelpers.findClass("com.alipay.mobile.payee.ui.PayeeQRActivity", context.getClassLoader()));
                context.startActivity(intent2);
            }

        }
    }
}
