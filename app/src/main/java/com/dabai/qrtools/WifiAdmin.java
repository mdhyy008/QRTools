package com.dabai.qrtools;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.List;

/**
 * Created by cmos.
 */
public  class WifiAdmin {
    private static String TAG="WifiManger";
    public static final int TYPE_NO_PASSWD = 0x11;
    public static final int TYPE_WEP = 0x12;
    public static final int TYPE_WPA = 0x13;
    public WifiManager mWifiManager;
    private WifiInfo mWifiInfo;
    public WifiAdmin(Context context){
        mWifiManager=  (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        // mWifiInfo = mWifiManager.getConnectionInfo();
    }

    // 添加一个网络并连接
    public void addNetwork(WifiConfiguration wifi){
        int netId=mWifiManager.addNetwork(wifi);
        mWifiManager.enableNetwork(netId,true);

    }
    public void addNetwork(String ssid, String passwd, int type) {
        if (ssid == null || passwd == null || ssid.equals("")) {
            Log.e(TAG, "addNetwork() ## nullpointer error!");
            return;
        }

        if (type != TYPE_NO_PASSWD && type != TYPE_WEP && type != TYPE_WPA) {
            Log.e(TAG, "addNetwork() ## unknown type = " + type);
        }

        addNetwork(createWifiInfo(ssid, passwd, type));
    }

    public WifiConfiguration createWifiInfo(String SSID, String password, int type) {

        Log.e(TAG, "SSID = " + SSID + "## Password = " + password + "## Type = " + type);

        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";


        WifiConfiguration tempConfig = this.IsExsits(SSID);
        if (tempConfig != null) {
            //如果已存在该SSID的wifi
            mWifiManager.removeNetwork(tempConfig.networkId);
        }

        // 分为三种情况：1没有密码2用wep加密3用wpa加密
        if (type == TYPE_NO_PASSWD) {// 没有密码
            config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;

        } else if (type == TYPE_WEP) {  //  wep加密
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + password + "\"";
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (type == TYPE_WPA) {   // WPA加密
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.TKIP);
            // config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }

        return config;
    }

    //检测该SSID是否已存在
    private WifiConfiguration IsExsits(String SSID) {
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                return existingConfig;
            }
        }
        return null;
    }

    // 打开WIFI
    public void openWifi() {
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }
    }

    // 关闭WIFI
    public void closeWifi() {
        if (mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
        }
    }
}
