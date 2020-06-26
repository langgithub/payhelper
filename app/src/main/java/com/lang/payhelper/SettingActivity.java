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
import com.lang.payhelper.utils.PayHelperUtils;

/**
 * 

* @ClassName: SettingActivity

* @Description: TODO(这里用一句话描述这个类的作用)

* @author SuXiaoliang

* @date 2018年6月23日 下午1:26:51

*
 */
public class SettingActivity extends Activity implements OnClickListener{
	
	private EditText tv_notify_sms,tv_notify_zfb,address,et_phone,bankCard,bankCard1,bankCard2,bankCard3,bankCard4;
	private Button bt_save,bt_back;
	private RelativeLayout rl_back;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); 
		setContentView(R.layout.activity_setting);
		tv_notify_sms=(EditText) findViewById(R.id.notify_sms);
		tv_notify_zfb=(EditText) findViewById(R.id.notify_zfb);
        bankCard=(EditText) findViewById(R.id.bankCard);
		bankCard1=(EditText) findViewById(R.id.bankCard1);
		bankCard2=(EditText) findViewById(R.id.bankCard2);
		bankCard3=(EditText) findViewById(R.id.bankCard3);
		bankCard4=(EditText) findViewById(R.id.bankCard4);
		address=(EditText) findViewById(R.id.address);
		et_phone=(EditText) findViewById(R.id.phone);
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
			et_phone.setText(AbSharedUtil.getString(getApplicationContext(), "account"));
		}
        if(!TextUtils.isEmpty(AbSharedUtil.getString(getApplicationContext(), "bankCard"))){
            bankCard.setText(AbSharedUtil.getString(getApplicationContext(), "bankCard"));
        }
		if(!TextUtils.isEmpty(AbSharedUtil.getString(getApplicationContext(), "bankCard1"))){
			bankCard1.setText(AbSharedUtil.getString(getApplicationContext(), "bankCard1"));
		}
		if(!TextUtils.isEmpty(AbSharedUtil.getString(getApplicationContext(), "bankCard2"))){
			bankCard2.setText(AbSharedUtil.getString(getApplicationContext(), "bankCard2"));
		}
		if(!TextUtils.isEmpty(AbSharedUtil.getString(getApplicationContext(), "bankCard3"))){
			bankCard3.setText(AbSharedUtil.getString(getApplicationContext(), "bankCard3"));
		}
		if(!TextUtils.isEmpty(AbSharedUtil.getString(getApplicationContext(), "bankCard4"))){
			bankCard4.setText(AbSharedUtil.getString(getApplicationContext(), "bankCard4"));
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
			PayHelperUtils.sendmsg(getApplicationContext(),"点击了保存");
			String notify_sms=tv_notify_sms.getText().toString();

			AbSharedUtil.putString(getApplicationContext(), "notify_sms", notify_sms);
			Log.i("url",AbSharedUtil.getString(getApplicationContext(), "notify_sms"));

			String notify_zfb=tv_notify_zfb.getText().toString();
			AbSharedUtil.putString(getApplicationContext(), "notify_zfb", notify_zfb);
			Log.i("url",AbSharedUtil.getString(getApplicationContext(), "notify_zfb"));

			String _address=address.getText().toString();
			AbSharedUtil.putString(getApplicationContext(), "address", _address);

			String account=et_phone.getText().toString();
			String bank=bankCard.getText().toString();
			String bank1=bankCard1.getText().toString();
			String bank2=bankCard2.getText().toString();
			String bank3=bankCard3.getText().toString();
			String bank4=bankCard4.getText().toString();
			if(!TextUtils.isEmpty(account)){
				AbSharedUtil.putString(getApplicationContext(), "account", account);
				AbSharedUtil.putString(getApplicationContext(), "bankCard", bank);
				AbSharedUtil.putString(getApplicationContext(), "bankCard1", bank1);
				AbSharedUtil.putString(getApplicationContext(), "bankCard2", bank2);
				AbSharedUtil.putString(getApplicationContext(), "bankCard3", bank3);
				AbSharedUtil.putString(getApplicationContext(), "bankCard4", bank4);
			}else {
				PayHelperUtils.sendmsg(getApplicationContext(),"支付宝账号为空");
				return;
			}

			Toast.makeText(getApplicationContext(), "保存成功", Toast.LENGTH_LONG).show();
			Intent broadCastIntent = new Intent();
			broadCastIntent.setAction("com.payhelper.tcp.start");
			broadCastIntent.putExtra("address",_address);
			broadCastIntent.putExtra("account",account);
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
