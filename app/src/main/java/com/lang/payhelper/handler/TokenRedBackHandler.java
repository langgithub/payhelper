package com.lang.payhelper.handler;

import android.content.Intent;
import android.util.Log;

import com.lang.payhelper.utils.DBManager;
import com.lang.payhelper.utils.PayHelperUtils;
import com.lang.sekiro.api.SekiroRequest;
import com.lang.sekiro.api.SekiroRequestHandler;
import com.lang.sekiro.api.SekiroResponse;

import de.robv.android.xposed.XposedHelpers;

public class TokenRedBackHandler implements SekiroRequestHandler {

    @Override
    public void handleRequest(SekiroRequest sekiroRequest, SekiroResponse sekiroResponse) {
        try{
            String token = sekiroRequest.getString("token");
            Log.i("Xposed","口令红包 handleRequest request"+token);
            ZfbApp zfbApp = ZfbApp.newInstance();
            // 绑定与xposed通讯
            Store.requestTaskMap.put(zfbApp, sekiroResponse);
            if (zfbApp.getContext()!=null && token!=null){
                Log.i("Xposed","handleRequest start");
                PayHelperUtils.sendmsg(zfbApp.getContext(),"口令红包 handleRequest {'token':'"+token+"'}");
                zfbApp.setToken(token);
                Intent intent2=new Intent(zfbApp.getContext(), XposedHelpers.findClass("com.alipay.android.phone.discovery.envelope.HomeActivity", zfbApp.getContext().getClassLoader()));
//                intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                intent2.putExtra("token", token);
                zfbApp.getContext().startActivity(intent2);
            }else {
                sekiroResponse = Store.requestTaskMap.remove(zfbApp);
                if(sekiroResponse!=null){
                    sekiroResponse.success("token is null");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}