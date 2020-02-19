package com.lang.payhelper.handler;

import android.content.Intent;
import android.util.Log;


import com.lang.sekiro.api.SekiroRequest;
import com.lang.sekiro.api.SekiroRequestHandler;
import com.lang.sekiro.api.SekiroResponse;

import de.robv.android.xposed.XposedHelpers;

public class ZfbHandler implements SekiroRequestHandler {

    @Override
    public void handleRequest(SekiroRequest sekiroRequest, SekiroResponse sekiroResponse) {

        String bz = sekiroRequest.getString("bz");
        String je = sekiroRequest.getString("je");

        ZfbApp zfbApp = ZfbApp.newInstance();
        Log.i("Xposed","handleRequest request"+(zfbApp.getContext()!=null));

        // 绑定与xposed通讯
        Store.requestTaskMap.put(zfbApp, sekiroResponse);
        if (zfbApp.getContext()!=null){
            Log.i("Xposed","handleRequest start");
//            Intent intent2=new Intent(zfbApp.getContext(), XposedHelpers.findClass("com.alipay.mobile.payee.ui.PayeeQRActivity", zfbApp.getContext().getClassLoader()));
//            zfbApp.getContext().startActivity(intent2);
            Intent intent2=new Intent(zfbApp.getContext(), XposedHelpers.findClass("com.alipay.mobile.payee.ui.PayeeQRSetMoneyActivity", zfbApp.getContext().getClassLoader()));
            intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent2.putExtra("mark", bz);
            intent2.putExtra("money", je);
            zfbApp.getContext().startActivity(intent2);
        }else {
            sekiroResponse = Store.requestTaskMap.remove(zfbApp);
            if(sekiroResponse!=null){
                sekiroResponse.success("获取失败，getContext is null");
            }
        }
    }
}
