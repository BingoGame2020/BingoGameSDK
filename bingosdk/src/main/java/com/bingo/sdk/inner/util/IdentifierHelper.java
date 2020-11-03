package com.bingo.sdk.inner.util;

import android.content.Context;
import android.util.Log;

import com.bun.miitmdid.core.ErrorCode;
import com.bun.miitmdid.core.MdidSdkHelper;
import com.bun.miitmdid.interfaces.IIdentifierListener;
import com.bun.miitmdid.interfaces.IdSupplier;

public class IdentifierHelper implements IIdentifierListener {

    private int CallFromReflect(Context context) {
        return MdidSdkHelper.InitSdk(context, true, this);
    }

    public IdentifierHelper(AppIdsUpdater listener) {
        this.listener = listener;
    }


    public void getOAID(Context cxt) {

        long timeb = System.currentTimeMillis();
        // 方法调用
        int nres = CallFromReflect(cxt);
        LogUtil.i("获取oaid res: " + nres);

        long timee = System.currentTimeMillis();
        long offset = timee - timeb;
        if (nres == ErrorCode.INIT_ERROR_DEVICE_NOSUPPORT) {//不支持的设备

        } else if (nres == ErrorCode.INIT_ERROR_LOAD_CONFIGFILE) {//加载配置文件出错

        } else if (nres == ErrorCode.INIT_ERROR_MANUFACTURER_NOSUPPORT) {//不支持的设备厂商

        } else if (nres == ErrorCode.INIT_ERROR_RESULT_DELAY) {//获取接口是异步的，结果会在回调中返回，回调执行的回调可能在工作线程

        } else if (nres == ErrorCode.INIT_HELPER_CALL_ERROR) {//反射调用出错

        }
        Log.d(getClass().getSimpleName(), "return value: " + String.valueOf(nres));

    }

    @Override
    public void OnSupport(boolean isSupport, IdSupplier idSupplier) {
        if (idSupplier == null) {
            return;
        }
        String oaid = idSupplier.getOAID();
//        String vaid = idSupplier.getVAID();
//        String aaid = idSupplier.getAAID();
//        StringBuilder builder = new StringBuilder();
//        builder.append("support: ").append(isSupport ? "true" : "false").append("\n");
//        builder.append("OAID: ").append(oaid).append("\n");
//        builder.append("VAID: ").append(vaid).append("\n");
//        builder.append("AAID: ").append(aaid).append("\n");
//        String idstext = builder.toString();
        if (listener != null) {
            listener.onIdValid(oaid);
        }
    }

    private AppIdsUpdater listener;

    public interface AppIdsUpdater {
        void onIdValid(String id);
    }

}
