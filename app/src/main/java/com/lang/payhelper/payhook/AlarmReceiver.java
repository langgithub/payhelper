package com.lang.payhelper.payhook;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.lang.payhelper.utils.DBManager;
import com.lang.payhelper.utils.OrderBean;
import com.lang.payhelper.utils.PayHelperUtils;

import java.util.List;

/**
 * 

* @ClassName: AlarmReceiver

* @Description: TODO(这里用一句话描述这个类的作用)

* @author SuXiaoliang

* @date 2018年6月23日 下午1:25:47

*
 */

public class AlarmReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
//		PayHelperUtils.sendmsg(context, "轮询任务");
		try {
//			Intent broadCastIntent = new Intent();
//			broadCastIntent.setAction("com.payhelper.alipay.start2");
//			String time=System.currentTimeMillis()/10000L+"";
//			broadCastIntent.putExtra("mark", "test"+time);
//			broadCastIntent.putExtra("money", "0.01");
//			context.sendBroadcast(broadCastIntent);


			DBManager dbManager=new DBManager(context);
			List<OrderBean> orderBeans=dbManager.FindAllOrders();
			for (OrderBean orderBean : orderBeans) {
				PayHelperUtils.sendmsg(context, "重新保存发送订单"+orderBean.getNo());
				PayHelperUtils.notify(context, orderBean.getType(), orderBean.getNo(), orderBean.getMoney(), orderBean.getMark(), orderBean.getDt());
			}
//			long currentTimeMillis=System.currentTimeMillis()/1000;
//			PayHelperUtils.sendmsg(context, PayHelperUtils.getcurrentTimeMillis(context));
//			long currentTimeMillis2=Long.parseLong(PayHelperUtils.getcurrentTimeMillis(context));
//			long currentTimeMillis3=currentTimeMillis-currentTimeMillis2;
//			if(currentTimeMillis3>120 && currentTimeMillis2!=0){
//				PayHelperUtils.sendmsg(context, "轮询任务出现异常,重启中...");
////				PayHelperUtils.startAlipayMonitor(context);
//				PayHelperUtils.sendmsg(context, "轮询任务重启成功");
//			}
		} catch (Exception e) {
			e.printStackTrace();
			PayHelperUtils.sendmsg(context, "AlarmReceiver异常->>"+e.getMessage());
		}
	}

}
