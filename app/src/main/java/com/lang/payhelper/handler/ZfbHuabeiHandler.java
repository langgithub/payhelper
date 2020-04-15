package com.lang.payhelper.handler;

import android.content.Intent;
import android.util.Log;

import com.lang.payhelper.utils.PayHelperUtils;
import com.lang.sekiro.api.SekiroRequest;
import com.lang.sekiro.api.SekiroRequestHandler;
import com.lang.sekiro.api.SekiroResponse;

public class ZfbHuabeiHandler implements SekiroRequestHandler {

    @Override
    public void handleRequest(SekiroRequest sekiroRequest, SekiroResponse sekiroResponse) {
        try{
            String bz = sekiroRequest.getString("bz");
            String je = sekiroRequest.getString("je");
            Log.i("Xposed","ZfbHuabeiHandler request"+(sekiroRequest.getString("je")));
            ZfbApp zfbApp = ZfbApp.newInstance();
            // 绑定与xposed通讯
            Store.requestTaskMap.put(zfbApp, sekiroResponse);
            if (zfbApp.getContext()!=null && je!=null && !"".equals(bz) && !"".equals(je)){
                Log.i("Xposed","ZfbHuabeiHandler start");
                PayHelperUtils.sendmsg(zfbApp.getContext(),"获取花呗url handleRequest {'bz':'"+bz+"','je':'"+je+"'}");
                Intent intent=new Intent();
                intent.setAction("com.payhelper.alipay.makehuabei");
                intent.putExtra("mark", bz);
                intent.putExtra("money", je);
                zfbApp.getContext().sendBroadcast(intent);
            }else {
                sekiroResponse = Store.requestTaskMap.remove(zfbApp);
                if(sekiroResponse!=null){
                    sekiroResponse.success("je is null");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
