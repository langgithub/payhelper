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
		try {
			DBManager dbManager=new DBManager(context);
			List<OrderBean> orderBeans=dbManager.FindAllOrders();
			for (OrderBean orderBean : orderBeans) {
				PayHelperUtils.sendmsg(context, "重新保存发送订单"+orderBean.getNo());
				PayHelperUtils.notify(context, orderBean.getType(), orderBean.getNo(), orderBean.getMoney(), orderBean.getMark(), orderBean.getDt());
			}
		} catch (Exception e) {
			e.printStackTrace();
			PayHelperUtils.sendmsg(context, "AlarmReceiver异常->>"+e.getMessage());
		}
	}

}
