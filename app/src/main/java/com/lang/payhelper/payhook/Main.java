package com.lang.payhelper.payhook;

import com.alibaba.fastjson.JSON;
import com.lang.payhelper.handler.Store;
import com.lang.payhelper.handler.TokenRedBackHandler;
import com.lang.payhelper.handler.ZfbApp;
import com.lang.payhelper.handler.ZfbHuabeiHandler;
import com.lang.payhelper.handler.ZfbQr1Handler;
import com.lang.payhelper.handler.ZfbQr2Handler;
import com.lang.payhelper.utils.AbSharedUtil;
import com.lang.payhelper.utils.JsonHelper;
import com.lang.payhelper.utils.PayHelperUtils;
import com.lang.payhelper.xp.hook.BaseHook;
import com.lang.sekiro.api.SekiroResponse;
import com.lang.sekiro.netty.client.SekiroClient;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
    public static boolean isruning = false;


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
//                            StartServerReceived startServerReceived=new StartServerReceived();
//                            IntentFilter server = new IntentFilter();
//                            server.addAction("com.payhelper.tcp.start");
//                            context.registerReceiver(startServerReceived, server);

                            StartAlipayReceived startAlipay=new StartAlipayReceived();
                            IntentFilter intentFilter = new IntentFilter();
                            intentFilter.addAction("com.payhelper.alipay.start");
                            intentFilter.addAction("com.payhelper.alipay.makehuabei");
                            intentFilter.addAction("com.payhelper.tcp.start");
                            intentFilter.addAction("com.payhelper.alipay.start2");
                            context.registerReceiver(startAlipay, intentFilter);

//                            StartAlipayQr startAlipayQr=new StartAlipayQr();
//                            IntentFilter intentFilter2 = new IntentFilter();
//                            intentFilter2.addAction("com.payhelper.alipay.start2");
//                            context.registerReceiver(startAlipayQr, intentFilter2);

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
//    class StartServerReceived extends BroadcastReceiver {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//
//            if(ALIPAY_PACKAGE_ISHOOK) {
//                final SekiroClient sekiroClient = SekiroClient.start(intent.getStringExtra("address"), 5600, UUID.randomUUID().toString(), "zfb_" + intent.getStringExtra("account"));
//                sekiroClient.registerHandler("zfbAppHandler", new ZfbQr1Handler());
//                sekiroClient.registerHandler("zfb2AppHandler", new ZfbQr2Handler());
//                sekiroClient.registerHandler("tokenRed", new TokenRedBackHandler());
//                PayHelperUtils.sendmsg(context, "服务器启动成功 接口访问地址:http://" + intent.getStringExtra("address") + ":5601/asyncInvoke?group=zfb_" + intent.getStringExtra("account") + "&action=zfbAppHandler");
//            }
//        }
//    }

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
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Main.getHuabei(context, intent);
                    }
                }).start();
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
//            intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent2.putExtra("mark", intent.getStringExtra("mark"));
//            intent2.putExtra("money", intent.getStringExtra("money"));
                context.startActivity(intent2);
            }

        }
    }

//    //自定义启动支付宝广播
//    class StartAlipayQr extends BroadcastReceiver {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Intent intent2=new Intent(context, XposedHelpers.findClass("com.alipay.mobile.payee.ui.PayeeQRActivity", context.getClassLoader()));
////            intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////            intent2.putExtra("mark", intent.getStringExtra("mark"));
////            intent2.putExtra("money", intent.getStringExtra("money"));
//            context.startActivity(intent2);
//        }
//    }

    public static void getHuabei(Context context, Intent intent) {
        String url;
        Context context2 = context;
        Intent intent2 = intent;
        try {
            if (!isruning) {
                isruning = true;
                XposedBridge.log("38版本开始获取花呗");
                String money = intent2.getStringExtra("money");
                String mark = intent2.getStringExtra("mark");
                ClassLoader classLoader = context.getClassLoader();
                Object json = XposedHelpers.newInstance(XposedHelpers.findClass("com.alibaba.fastjson.JSONObject", classLoader), new Object[0]);
                Class H5RpcUtil = XposedHelpers.findClass("com.alipay.mobile.nebulabiz.rpc.H5RpcUtil", classLoader);
                String userId = PayHelperUtils.getAlipayserId(classLoader);
                String result1 = JsonHelper.toJson(XposedHelpers.callStaticMethod(H5RpcUtil, "rpcCall", new Object[]{"alipay.pcredit.huabei.promo.findUserCode", "[{\"businessType\":\"huabei\",\"request\":{\"codeType\":\"HB\"},\"requestFrom\":\"mobile\",\"source\":\"\"}]", "", true, json, null, false, null, 0, "", false, -1}));
                JSONObject resultJson = new JSONObject(result1);
                String str = result1;
                ClassLoader classLoader2 = classLoader;
                JSONObject response = new JSONObject(resultJson.getString("response"));
                StringBuilder sb = new StringBuilder();
                JSONObject jSONObject = resultJson;
                sb.append("response >>>>> ");
                sb.append(response);
                XposedBridge.log(sb.toString());
                String userCode = response.optString("result");
                JSONObject jSONObject2 = response;
                XposedHelpers.callMethod(json, "put", new Object[]{"appVersion", "1.0.34.201907232032-noResume"});
                XposedHelpers.callMethod(json, "put", new Object[]{"bizScenario", "appx"});
                XposedHelpers.callMethod(json, "put", new Object[]{"h5appid", "20000199"});
                XposedHelpers.callMethod(json, "put", new Object[]{"referrer", "https://render.alipay.com/p/h5/huabei/www/repayInvitation.html"});
                Object rpcResult = XposedHelpers.callStaticMethod(H5RpcUtil, "rpcCall", new Object[]{"com.alipay.pcreditweb.huabei.repayInvitation.createOrder", "[{\"bizScenario\":\"appx\",\"invitationAmount\":\"" + money + "\",\"invitationText\":\"\",\"userCode\":\"" + userCode + "\"}]", "", true, json, null, false, null, 0, "", false, -1});
                String result = JsonHelper.toJson(rpcResult);
                StringBuilder sb2 = new StringBuilder();
                sb2.append("花呗result》》》");
                sb2.append(result);
                XposedBridge.log(sb2.toString());
                JSONObject resultJson2 = new JSONObject(result);
                Object obj = rpcResult;
                JSONObject response2 = new JSONObject(resultJson2.getString("response"));
                Object obj2 = "";
                JSONObject jSONObject3 = resultJson2;
                if (!result.contains("invitationId")) {
                    String str2 = mark;
                    String url2 = response2.optString("resultDesc");
                    PayHelperUtils.sendmsg(context2, url2);
                    StringBuilder sb3 = new StringBuilder();
                    String str3 = result;
                    sb3.append("花呗订单数据异常 原因");
                    sb3.append(url2);
                    XposedBridge.log(sb3.toString());
                    url = "数据异常";
                } else {
                    JSONObject jsondata = response2.optJSONObject("data");
                    String orderid = jsondata.optString("invitationId");
                    StringBuilder sb4 = new StringBuilder();
                    JSONObject jSONObject4 = jsondata;
                    sb4.append("花呗单号》》》");
                    sb4.append(orderid);
                    XposedBridge.log(sb4.toString());
                    XposedBridge.log("花呗response》》》" + response2);
                    url = "https://render.alipay.com/p/h5/huabei/www/./helpRepay.html?availableAmount=" + money + "&inviteUserId=" + userId + "&invitationId=" + orderid;
                }
                Map<String,String> map=new HashMap<>();
                map.put("money", money);
                map.put("mark", mark);
                map.put("type", "huabei");
                map.put("payurl", url);
                String huaBeiResponse= com.alibaba.fastjson.JSONObject.toJSONString(map);
                ZfbApp zfbApp = ZfbApp.newInstance();
                if ( zfbApp.getContext() != null) {
                    SekiroResponse sekiroResponse = Store.requestTaskMap.remove(zfbApp);
                    if(sekiroResponse!=null){
                        XposedBridge.log("花呗url response>>>>"+huaBeiResponse);
                        sekiroResponse.success(huaBeiResponse);
                    }
                }

//                Intent broadCastIntent = new Intent();
//                broadCastIntent.putExtra("money", money);
//                broadCastIntent.putExtra("mark", mark);
//                broadCastIntent.putExtra("type", "huabei");
//                broadCastIntent.putExtra("payurl", url);
//                broadCastIntent.setAction(AlipayHook.QRCODERECEIVED_ACTION);
//                context2.sendBroadcast(broadCastIntent);
//                XposedBridge.log("------>花呗：" + url);
                isruning = false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
