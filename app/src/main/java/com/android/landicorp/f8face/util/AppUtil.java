package com.android.landicorp.f8face.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

/**
 * Created by admin on 2019/6/21.
 */

public class AppUtil {
    /**
     * 判断目前网络类型
     * @param context
     * @return
     */
    public static boolean getNetWorkType(Context context, int type){
        NetworkInfo info = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info != null && info.isConnected()){
            return type==info.getType();
        }
        return false;
    }
    /**
     * 获取网络信号强度
     */
    public static int getNetWorkSingleUnit (Context mContext){
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(mContext.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (!TextUtils.isEmpty(wifiInfo.getSSID())){
            return wifiInfo.getRssi();
        }
        return 0;
    }


}
