package com.lang.payhelper.handler;

import android.content.Intent;
import android.util.Log;


import com.lang.payhelper.utils.DBManager;
import com.lang.payhelper.utils.PayHelperUtils;
import com.lang.sekiro.api.SekiroRequest;
import com.lang.sekiro.api.SekiroRequestHandler;
import com.lang.sekiro.api.SekiroResponse;

import de.robv.android.xposed.XposedHelpers;

public class ZfbQr2Handler implements SekiroRequestHandler {

    @Override
    public void handleRequest(SekiroRequest sekiroRequest, SekiroResponse sekiroResponse) {

        try{
            String bz = sekiroRequest.getString("bz");
            String je = sekiroRequest.getString("je");
            Log.i("Xposed","handleRequest request"+(sekiroRequest.getString("je")));
//            String je = RSAMethod.privateDeData(sekiroRequest.getString("je"), RSAUtils.PRIVATE_KEY);
            ZfbApp zfbApp = ZfbApp.newInstance();
            // 绑定与xposed通讯
            Store.requestTaskMap.put(zfbApp, sekiroResponse);
            if (zfbApp.getContext()!=null&&je!=null){
                PayHelperUtils.sendmsg(zfbApp.getContext(),"支付宝二维码url1 handleRequest {'bz':'"+bz+"','je':'"+je+"'}");
                DBManager dbManager = new DBManager(zfbApp.getContext().getApplicationContext());
                if("null".equals(dbManager.getMark(je))){
                    dbManager.addMark(bz,je);
                    Log.i("Xposed","addMark");
                }else {
                    dbManager.updateMark(bz,je);
                    Log.i("Xposed","updateMark");
                }
                Intent intent2=new Intent(zfbApp.getContext(), XposedHelpers.findClass("com.alipay.mobile.payee.ui.PayeeQRActivity", zfbApp.getContext().getClassLoader()));
                zfbApp.getContext().startActivity(intent2);
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
