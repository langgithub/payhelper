package com.lang.payhelper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.lang.payhelper.utils.AbSharedUtil;

/**
 * 

* @ClassName: SettingActivity

* @Description: TODO(这里用一句话描述这个类的作用)

* @author SuXiaoliang

* @date 2018年6月23日 下午1:26:51

*
 */
public class SettingActivity extends Activity implements OnClickListener{
	
	private EditText tv_notify_sms,tv_notify_zfb,address,et_wxid;
	private Button bt_save,bt_back;
	private RelativeLayout rl_back;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); 
		setContentView(R.layout.activity_setting);
		tv_notify_sms=(EditText) findViewById(R.id.notify_sms);
		tv_notify_zfb=(EditText) findViewById(R.id.notify_zfb);
		address=(EditText) findViewById(R.id.address);
		et_wxid=(EditText) findViewById(R.id.et_wxid);
		if(!TextUtils.isEmpty(AbSharedUtil.getString(getApplicationContext(), "notify_sms"))){
			tv_notify_sms.setText(AbSharedUtil.getString(getApplicationContext(), "notify_sms"));
		}
		if(!TextUtils.isEmpty(AbSharedUtil.getString(getApplicationContext(), "notify_zfb"))){
			tv_notify_zfb.setText(AbSharedUtil.getString(getApplicationContext(), "notify_zfb"));
		}
		if(!TextUtils.isEmpty(AbSharedUtil.getString(getApplicationContext(), "address"))){
			address.setText(AbSharedUtil.getString(getApplicationContext(), "address"));
		}
		if(!TextUtils.isEmpty(AbSharedUtil.getString(getApplicationContext(), "account"))){
			et_wxid.setText(AbSharedUtil.getString(getApplicationContext(), "account"));
		}
		
		bt_save=(Button) findViewById(R.id.save);
		bt_back=(Button) findViewById(R.id.back);
		rl_back=(RelativeLayout) findViewById(R.id.rl_back);
		bt_back.setOnClickListener(this);
		bt_save.setOnClickListener(this);
		rl_back.setOnClickListener(this);
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	@Override
	protected void onResume() {
		super.onResume();
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.save:
			String notify_sms=tv_notify_sms.getText().toString();
//			notify_sms="http://139.129.119.106:10000/rich/open/putMessage";
//			if(TextUtils.isEmpty(returnurl)){
//				Toast.makeText(getApplicationContext(), "同步跳转地址不能为空！", Toast.LENGTH_LONG).show();
//				return;
//			}else{
				AbSharedUtil.putString(getApplicationContext(), "notify_sms", notify_sms);
//			}
			Log.i("url",AbSharedUtil.getString(getApplicationContext(), "notify_sms"));
			String notify_zfb=tv_notify_zfb.getText().toString();
//			notify_zfb="http://139.129.119.106:10000/rich/open/paynotify";
//			if(TextUtils.isEmpty(notifyurl)){
//				Toast.makeText(getApplicationContext(), "异步通知地址不能为空！", Toast.LENGTH_LONG).show();
//				return;
//			}else{
				AbSharedUtil.putString(getApplicationContext(), "notify_zfb", notify_zfb);
//			}
			Log.i("url",AbSharedUtil.getString(getApplicationContext(), "notify_zfb"));
			String _address=address.getText().toString();
//			if(TextUtils.isEmpty(signkey)){
//				Toast.makeText(getApplicationContext(), "signkey不能为空！", Toast.LENGTH_LONG).show();
//				return;
//			}else{
				AbSharedUtil.putString(getApplicationContext(), "address", _address);
//			}
			String wxid=et_wxid.getText().toString();
			if(!TextUtils.isEmpty(wxid)){
				AbSharedUtil.putString(getApplicationContext(), "account", wxid);
			}
			Toast.makeText(getApplicationContext(), "保存成功", Toast.LENGTH_LONG).show();

			Intent broadCastIntent = new Intent();
			broadCastIntent.setAction("com.payhelper.tcp.start");
			broadCastIntent.putExtra("address",_address);
			sendBroadcast(broadCastIntent);

			finish();
			break;
		case R.id.back:
			finish();
			break;
		case R.id.rl_back:
			finish();
			break;
		default:
			break;
		}
	}
}
