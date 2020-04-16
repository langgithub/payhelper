package com.lang.payhelper.payhook;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import com.alibaba.fastjson.JSON;
import com.lang.payhelper.handler.Store;
import com.lang.payhelper.handler.ZfbApp;
import com.lang.payhelper.utils.DBManager;
import com.lang.payhelper.utils.LogToFile;
import com.lang.payhelper.utils.PayHelperUtils;
import com.lang.payhelper.utils.StringUtils;
import com.lang.sekiro.api.SekiroResponse;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;


public class AlipayHook {

	public static String BILLRECEIVED_ACTION = "com.tools.payhelper.billreceived";
	public static String QRCODERECEIVED_ACTION = "com.tools.payhelper.qrcodereceived";
	public static String SAVEALIPAYCOOKIE_ACTION = "com.tools.payhelper.savealipaycookie";
	public String qrCodeUrl;

	public void setQrCodeUrl(String qrCodeUrl) {
		if ("".equals(qrCodeUrl)){
			return;
		}
		this.qrCodeUrl = qrCodeUrl;
	}

	public String getQrCodeUrl() {
		return qrCodeUrl.split("\\?")[0]+"?t="+System.currentTimeMillis();
	}

    public void hook(final ClassLoader classLoader,final Context context) {
		XposedBridge.log("支付宝hook 开始: " + PayHelperUtils.getVerName(context));
		securityCheckHook(classLoader,context);
		preventUpgrade(context);
        try {
			/********************************支付宝个人订单回调****************************************/
            Class<?> insertTradeMessageInfo = XposedHelpers.findClass("com.alipay.android.phone.messageboxstatic.biz.dao.TradeDao", classLoader);
            XposedBridge.hookAllMethods(insertTradeMessageInfo, "insertMessageInfo", new XC_MethodHook() {
            	@Override
            	protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            		try {
            			XposedBridge.log("======支付宝个人账号订单start=========");
            			
            			//更新cookie
                		Intent cookieBroadCastIntent = new Intent();
                		String alipaycookie= PayHelperUtils.getCookieStr(classLoader);
                		cookieBroadCastIntent.putExtra("alipaycookie", alipaycookie);
                		cookieBroadCastIntent.setAction(SAVEALIPAYCOOKIE_ACTION);
                        context.sendBroadcast(cookieBroadCastIntent);

            			//获取全部字段
            			Object object = param.args[0];
            			String MessageInfo = (String) XposedHelpers.callMethod(object, "toString");
            			XposedBridge.log(MessageInfo);
						String content= StringUtils.getTextCenter(MessageInfo, "content='", "'");
						if (!content.contains("二维码收款")) {
							if (!content.contains("收到一笔转账")) {
								if (content.contains("花呗")) {
									String midText2 = StringUtils.getTextCenter(MessageInfo, "link='", "'");
									Uri uri = Uri.parse(midText2);
									JSONObject jsonObject = new JSONObject(JSON.toJSONString(object));
									String dt = jsonObject.getString("gmtCreate");
									String receiveAmount = new JSONObject(jsonObject.getString("content")).getString("content").replace("￥", "");
									String crowdNo = uri.getQueryParameter("tradeNO");
									XposedBridge.log("获取到的花呗订单号 》》》》" + crowdNo);
//									AlipayHook.getHBDetail(crowdNo, dt, receiveAmount, context, classLoader);
								}
								XposedBridge.log("======支付宝个人账号订单end=========");
							}
						}
						if(content.contains("二维码收款") || content.contains("收到一笔转账") || content.contains("付款成功")){
            				JSONObject jsonObject=new JSONObject(content);
							XposedBridge.log(jsonObject.toString());

							String money=jsonObject.getString("content").replace("￥", "");
                			String mark=jsonObject.getString("assistMsg2");
                			String tradeNo=StringUtils.getTextCenter(MessageInfo,"tradeNO=","&");
							String contents="[{"+StringUtils.getTextCenter(MessageInfo,"\"content\":[{","]")+"]";
							XposedBridge.log(contents);
							JSONArray jsonArray=new JSONArray(contents);
							String account=jsonArray.getJSONObject(1).getString("content");
							String time=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date().getTime());
							if (content.contains("二维码收款")){
								time=jsonArray.getJSONObject(3).getString("content");
							}
							String userId=StringUtils.getTextCenter(MessageInfo,"userId='","'");
							DBManager dbManager = new DBManager(context.getApplicationContext());

							String _mark = dbManager.getMark(money);
							XposedBridge.log("======获取自付宝备注"+_mark+"(money="+money+")=========");
							if(!"null".equals(_mark)){
								mark=_mark;
							}else {
								mark="";
							}

                			Intent broadCastIntent = new Intent();
                			broadCastIntent.putExtra("bill_no", tradeNo);
                            broadCastIntent.putExtra("bill_money", money);
                            broadCastIntent.putExtra("bill_mark", mark);
                            broadCastIntent.putExtra("bill_type", "alipay");
							broadCastIntent.putExtra("bill_time",time);
                            broadCastIntent.setAction(BILLRECEIVED_ACTION);
                            context.sendBroadcast(broadCastIntent);
            			}
                        XposedBridge.log("======支付宝个人账号订单end=========");
            		} catch (Exception e) {
            			XposedBridge.log(e.getMessage());
            		}
            	}
            });

			/********************************商家服务订单回调****************************************/
            Class<?> insertServiceMessageInfo = XposedHelpers.findClass("com.alipay.android.phone.messageboxstatic.biz.dao.ServiceDao", classLoader);
            XposedBridge.hookAllMethods(insertServiceMessageInfo, "insertMessageInfo", new XC_MethodHook() {
            	@Override
            	protected void beforeHookedMethod(MethodHookParam param) {
            		try {
						XposedBridge.log("======支付宝商家服务订单start=========");
						//更新cookie
                		Intent cookieBroadCastIntent = new Intent();
                		String alipaycookie=PayHelperUtils.getCookieStr(classLoader);
                		cookieBroadCastIntent.putExtra("alipaycookie", alipaycookie);
                		cookieBroadCastIntent.setAction(SAVEALIPAYCOOKIE_ACTION);
                        context.sendBroadcast(cookieBroadCastIntent);
						
						Object object = param.args[0];
						String MessageInfo = (String) XposedHelpers.callMethod(object, "toString");
						String content=StringUtils.getTextCenter(MessageInfo, "extraInfo='", "'").replace("\\", "");
						String _money=StringUtils.getTextCenter(MessageInfo, "mainAmount\":\"", "\"");
						XposedBridge.log(content);
						if(content.contains("店员通")){
							String money=StringUtils.getTextCenter(content, "mainAmount\":\"", "\",\"mainTitle");
							String time=StringUtils.getTextCenter(content, "gmtCreate\":", ",gmtValid");
							String no=PayHelperUtils.getOrderId();
							Intent broadCastIntent = new Intent();
                			broadCastIntent.putExtra("bill_no", no);
                            broadCastIntent.putExtra("bill_money", money);
                            broadCastIntent.putExtra("bill_mark", "");
                            broadCastIntent.putExtra("bill_time", time);
                            broadCastIntent.setAction(BILLRECEIVED_ACTION);
                            context.sendBroadcast(broadCastIntent);
						}else if(content.contains("收钱到账") || content.contains("收款到账")){
							LogToFile.i("payhelper", "Hook到商家服务通知，开始调用getBill获取订单详细信息");
							String userId=PayHelperUtils.getAlipayUserId(classLoader);
							XposedBridge.log(userId+" "+alipaycookie);
							DBManager dbManager = new DBManager(context.getApplicationContext());
							String _mark = dbManager.getMark(_money);
							if("null".equals(_mark)){
								_mark="";
							}
							XposedBridge.log("======获取自付宝备注"+_mark+"(money="+_money+")=========");
							PayHelperUtils.getBill(context,alipaycookie,userId,_mark);
						}
						XposedBridge.log("======支付宝商家服务订单end=========");
					} catch (Exception e) {
						PayHelperUtils.sendmsg(context, e.getMessage());
					}
            	}
            });

			/********************************hook获得二维码url 带金额和备注****************************************/
			XposedHelpers.findAndHookMethod("com.alipay.mobile.payee.ui.PayeeQRSetMoneyActivity", classLoader, "a",
					XposedHelpers.findClass("com.alipay.transferprod.rpc.result.ConsultSetAmountRes", classLoader), new XC_MethodHook() {
						@Override
						protected void afterHookedMethod(MethodHookParam param) throws Throwable {
							XposedBridge.log("=========支付宝生成完成start========");

							Field moneyField = XposedHelpers.findField(param.thisObject.getClass(), "g");
							String money = (String) moneyField.get(param.thisObject);

							Field markField = XposedHelpers.findField(param.thisObject.getClass(), "c");
							Object markObject = markField.get(param.thisObject);
							String mark = (String) XposedHelpers.callMethod(markObject, "getUbbStr");

							Object consultSetAmountRes = param.args[0];
							Field consultField = XposedHelpers.findField(consultSetAmountRes.getClass(), "qrCodeUrl");
							String payurl = (String) consultField.get(consultSetAmountRes);
							XposedBridge.log(money + "  " + mark + "  " + payurl);

							if (money != null) {
								XposedBridge.log("调用增加数据方法==>支付宝");
								Intent broadCastIntent = new Intent();
								broadCastIntent.putExtra("money", money);
								broadCastIntent.putExtra("mark", mark);
								broadCastIntent.putExtra("type", "alipay");
								broadCastIntent.putExtra("payurl", payurl);
								setQrCodeUrl(payurl);
								broadCastIntent.setAction(QRCODERECEIVED_ACTION);
								context.sendBroadcast(broadCastIntent);

								ZfbApp zfbApp = ZfbApp.newInstance();
								if ( zfbApp.getContext() != null) {
									SekiroResponse sekiroResponse = Store.requestTaskMap.remove(zfbApp);
									if(sekiroResponse!=null){
										XposedBridge.log("return  sekiroResponse>>>>");
										sekiroResponse.success(getQrCodeUrl());
									}
								}
							}

							XposedBridge.log("=========支付宝生成完成end========");
						}
					});

			//hook设置金额和备注的onCreate方法，自动填写数据并点击
			XposedHelpers.findAndHookMethod("com.alipay.mobile.payee.ui.PayeeQRSetMoneyActivity", classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					XposedBridge.log("========支付宝设置金额start=========");
					Intent cookieBroadCastIntent = new Intent();
					String alipaycookie=PayHelperUtils.getCookieStr(classLoader);
					cookieBroadCastIntent.putExtra("alipaycookie", alipaycookie);
					cookieBroadCastIntent.setAction(SAVEALIPAYCOOKIE_ACTION);
					context.sendBroadcast(cookieBroadCastIntent);

					Field jinErField = XposedHelpers.findField(param.thisObject.getClass(), "b");
					final Object jinErView = jinErField.get(param.thisObject);
					Field beiZhuField = XposedHelpers.findField(param.thisObject.getClass(), "c");
					final Object beiZhuView = beiZhuField.get(param.thisObject);
					Intent intent = ((Activity) param.thisObject).getIntent();
					String mark=intent.getStringExtra("mark");
					String money=intent.getStringExtra("money");
					//设置支付宝金额和备注
					XposedHelpers.callMethod(jinErView, "setText", money);
					XposedHelpers.callMethod(beiZhuView, "setText", mark);
					//点击确认
					Field quRenField = XposedHelpers.findField(param.thisObject.getClass(), "e");
					final Button quRenButton = (Button) quRenField.get(param.thisObject);
					quRenButton.performClick();
					XposedBridge.log("=========支付宝设置金额end========");
				}
			});

			/********************************收款二维码抓取 不带金额备注的****************************************/
			final Object[] obj = {null};
			XposedHelpers.findAndHookMethod("com.alipay.transferprod.rpc.req.CreateSessionReq", classLoader, "toString", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					String result = (String) param.getResult();
					XposedBridge.log("======获取二维码url=========" + result);
					Pattern compile = Pattern.compile("qrCodeUrl='(.*?)', prin");
					Matcher matcher = compile.matcher(result);
					if (matcher.find()) {
						setQrCodeUrl(matcher.group(1));
						Intent broadCastIntent = new Intent();
						broadCastIntent.putExtra("money", "没有设置");
						broadCastIntent.putExtra("mark", "没有设置");
						broadCastIntent.putExtra("type", "alipay");
						broadCastIntent.putExtra("payurl", getQrCodeUrl());
						broadCastIntent.setAction(QRCODERECEIVED_ACTION);

						if (obj[0]!=null){
							ZfbApp zfbApp = ZfbApp.newInstance();
							if ( zfbApp.getContext() != null) {
								SekiroResponse sekiroResponse = Store.requestTaskMap.remove(zfbApp);
								if(sekiroResponse!=null){
									XposedBridge.log("return  sekiroResponse>>>>");
									sekiroResponse.success(getQrCodeUrl());
								}
							}
							XposedBridge.log("close PayeeQRActivity");
							Method onBackPressed = XposedHelpers.findMethodBestMatch(XposedHelpers.findClass("com.alipay.mobile.payee.ui.PayeeQRActivity", classLoader), "onBackPressed");
							onBackPressed.invoke(obj[0]);
							Method finish = XposedHelpers.findMethodBestMatch(XposedHelpers.findClass("com.alipay.mobile.payee.ui.PayeeQRActivity", classLoader), "finish");
							finish.invoke(obj[0]);
						}
					}else {
						ZfbApp zfbApp = ZfbApp.newInstance();
						if ( zfbApp.getContext() != null) {
							SekiroResponse sekiroResponse = Store.requestTaskMap.remove(zfbApp);
							if(sekiroResponse!=null){
								XposedBridge.log("return  sekiroResponse>>>>");
								sekiroResponse.success("url 为空");
							}
						}
					}
				}
			});
			//用于退出指定窗口
			XposedHelpers.findAndHookMethod("com.alipay.mobile.payee.ui.PayeeQRActivity", classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					obj[0] =param.thisObject;
				}
			});

			/********************************hook获取loginid****************************************/
			XposedHelpers.findAndHookMethod("com.alipay.mobile.quinox.LauncherActivity", classLoader, "onResume",
            		 new XC_MethodHook() {
            	@Override
            	protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            		PayHelperUtils.isFirst=true;
            		String loginid=PayHelperUtils.getAlipayLoginId(classLoader);
            		PayHelperUtils.sendLoginId(loginid, "alipay", context);
            	}
            });

			/********************************拦截“人气大爆发，一会再试试”****************************************/
			XposedHelpers.findAndHookMethod("com.alipay.mobile.antui.basic.AUDialog", classLoader, "show",
            		new XC_MethodHook() {
            	@Override
            	protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            		Context mContext=(Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
            		if (mContext.getClass().getSimpleName().equals("PayeeQRSetMoneyActivity")){
            			XposedHelpers.setObjectField(param.thisObject, "mContext", null);
            		}
            	}
            	
            });

			/********************************拦截设置cookie****************************************/
            XposedHelpers.findAndHookMethod("com.alipay.mobile.common.transport.http.GwCookieCacheHelper", classLoader, "setCookies",String.class,Map.class,
            		new XC_MethodHook() {
            	
            	@Override
            	protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            		Map<String, String> cookie=(Map<String, String>)param.args[1];
            		String ck=(String) XposedHelpers.callStaticMethod(XposedHelpers.findClass("com.alipay.mobile.common.transport.http.GwCookieCacheHelper", classLoader), "toCookieString",cookie);
            		if(ck.contains("ALIPAYJSESSIONID")){
            			//更新cookie
            			Intent cookieBroadCastIntent = new Intent();
            			cookieBroadCastIntent.putExtra("alipaycookie", ck);
            			cookieBroadCastIntent.setAction(SAVEALIPAYCOOKIE_ACTION);
            			context.sendBroadcast(cookieBroadCastIntent);
            		}
            	}
            	
            });
        } catch (Error | Exception e) {
        	PayHelperUtils.sendmsg(context, e.getMessage());
        }
    }

	/**
	 * 反反hook
	 * @param classLoader classloader
	 * @param context context
	 */
	private void securityCheckHook(ClassLoader classLoader,final Context context) {

		try {
			int version = PayHelperUtils.getVersionCode(context);
			XposedBridge.log("支付宝绕过VXP校验: " + PayHelperUtils.getVerName(context));
			XposedBridge.log("支付宝 versioncode----->" + version);
			if (version == 240) {
				XposedHelpers.findAndHookMethod("com.alipay.mobile.quinox.utils.MonitorLogger", classLoader, "putBizExternParams", String.class, String.class, new XC_MethodHook() {
					/* access modifiers changed from: protected */
					public void beforeHookedMethod(MethodHookParam methodHookParam) {
						if (methodHookParam.args[0].equals("isSandbox")) {
							methodHookParam.args[1] = "0";
						}
					}
				});
				XposedHelpers.findAndHookMethod("com.alipay.apmobilesecuritysdk.scanattack.common.ScanAttack", classLoader, "vir1", Context.class, new XC_MethodHook() {
					/* access modifiers changed from: protected */
					@SuppressLint("SdCardPath")
					public void afterHookedMethod(MethodHookParam methodHookParam) {
						StringBuilder stringBuilder = new StringBuilder();
						stringBuilder.append("result = ");
						stringBuilder.append(methodHookParam.getResult());
						methodHookParam.setResult(((String) methodHookParam.getResult()).replace("/data/user/0/io.va.exposed/virtual/data/user/0/com.eg.android.AlipayGphone", "/data/user/0/com.eg.android.AlipayGphone").replace("^^^", ""));
					}
				});
				XposedHelpers.findAndHookMethod("com.alipay.apmobilesecuritysdk.scanattack.common.ScanAttack", classLoader, "xp1", Context.class, new XC_MethodReplacement() {
					/* access modifiers changed from: protected */
					public Object replaceHookedMethod(MethodHookParam methodHookParam) {
						return false;
					}
				});
				XposedHelpers.findAndHookMethod("com.alipay.apmobilesecuritysdk.scanattack.common.ScanAttack", classLoader, "xp2", Context.class, new XC_MethodReplacement() {
					/* access modifiers changed from: protected */
					public Object replaceHookedMethod(MethodHookParam methodHookParam) {
						return false;
					}
				});
				XposedHelpers.findAndHookMethod("com.alipay.apmobilesecuritysdk.scanattack.common.ScanAttack", classLoader, "xp3", Context.class, new XC_MethodReplacement() {
					/* access modifiers changed from: protected */
					public Object replaceHookedMethod(MethodHookParam methodHookParam) {
						return "[]";
					}
				});
				XposedHelpers.findAndHookMethod("com.alipay.apmobilesecuritysdk.scanattack.common.ScanAttack", classLoader, "xp4", Context.class, new XC_MethodReplacement() {
					/* access modifiers changed from: protected */
					public Object replaceHookedMethod(MethodHookParam methodHookParam) {
						return "[]";
					}
				});
				XposedHelpers.findAndHookMethod("com.alipay.apmobilesecuritysdk.scanattack.common.ScanAttack", classLoader, "methodToNative", new XC_MethodReplacement() {
					/* access modifiers changed from: protected */
					public Object replaceHookedMethod(MethodHookParam methodHookParam) {
						return "[]";
					}
				});
				XposedHelpers.findAndHookMethod("com.alipay.euler.andfix.AndFix", classLoader, "a", new XC_MethodReplacement() {
					/* access modifiers changed from: protected */
					public Object replaceHookedMethod(MethodHookParam methodHookParam) {
						return false;
					}
				});
			}

			XposedBridge.log("支付宝绕过Xp校验");
            Class<?> securityCheckClazz = XposedHelpers.findClass("com.alipay.mobile.base.security.CI", classLoader);
            XposedHelpers.findAndHookMethod(securityCheckClazz, "a", String.class, String.class, String.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Object object = param.getResult();
                    XposedHelpers.setBooleanField(object, "a", false);
                    param.setResult(object);
                    super.afterHookedMethod(param);
                }
            });
            XposedHelpers.findAndHookMethod(securityCheckClazz, "a", Class.class, String.class, String.class, new XC_MethodReplacement() {
                @Override
                protected Object replaceHookedMethod(MethodHookParam param) {
                    return (byte) 1;
                }
            });
            XposedHelpers.findAndHookMethod(securityCheckClazz, "a", ClassLoader.class, String.class, new XC_MethodReplacement() {
                @Override
                protected Object replaceHookedMethod(MethodHookParam param) {
                    return (byte) 1;
                }
            });
            XposedHelpers.findAndHookMethod(securityCheckClazz, "a", new XC_MethodReplacement() {
                @Override
                protected Object replaceHookedMethod(MethodHookParam param) {
                    return false;
                }
            });
			// 用于检查是否绕过Xposed
			XposedHelpers.findAndHookMethod(Base64.class, "decode", String.class, Integer.TYPE, new XC_MethodHook() {
				public void beforeHookedMethod(MethodHookParam methodHookParam) {
					if (methodHookParam.args[0].toString().equals("ZGUucm9idi5hbmRyb2lkLnhwb3NlZC5YcG9zZWRIZWxwZXJz")) {
						PayHelperUtils.sendmsg(context, "看到我,请截图给技术哦!有精美彩蛋!alipay fuck xposed");
						methodHookParam.args[0] = "ZGUucm9idi5hbmRyb2lkLnhwb3NlZC5YcG9zZWRIZWxwMTIz";
					}
				}
			});
        } catch (Error | Exception e) {
            e.printStackTrace();
        }
    }

	/**
	 * 阻止弹出升级窗口
	 * @param context context
	 */
	private static void preventUpgrade(Context context) {
		XposedHelpers.findAndHookMethod("com.alipay.android.launcher.AlipayUpgradeHelper", context.getClassLoader(), "isUpgrade", new XC_MethodReplacement() {
			/* access modifiers changed from: protected */
			public Object replaceHookedMethod(MethodHookParam methodHookParam) {
				return false;
			}
		});
	}
}