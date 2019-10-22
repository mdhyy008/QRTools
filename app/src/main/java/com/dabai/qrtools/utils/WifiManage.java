package com.dabai.qrtools.utils;

import android.os.Build;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WifiManage {

    private static String TAG = "dabaizzz";

    public static ArrayList<WifiInfo> Read() throws Exception {
        ArrayList<WifiInfo> wifiInfos = new ArrayList<WifiInfo>();

        Process process = null;
        DataOutputStream dataOutputStream = null;
        DataInputStream dataInputStream = null;
        StringBuffer wifiConf = new StringBuffer();
        try {
            process = Runtime.getRuntime().exec("su");
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            dataInputStream = new DataInputStream(process.getInputStream());


            if (Build.VERSION.SDK_INT >= 26) {
                dataOutputStream
                        .writeBytes("cat /data/misc/wifi/WifiConfigStore.xml\n");

            } else {
                dataOutputStream
                        .writeBytes("cat /data/misc/wifi/*.conf\n");

            }


            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            InputStreamReader inputStreamReader = new InputStreamReader(
                    dataInputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(
                    inputStreamReader);
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                wifiConf.append(line);
            }
            bufferedReader.close();
            inputStreamReader.close();
            process.waitFor();
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                if (dataOutputStream != null) {
                    dataOutputStream.close();
                }
                if (dataInputStream != null) {
                    dataInputStream.close();
                }
                process.destroy();
            } catch (Exception e) {
                throw e;
            }
        }


        if (Build.VERSION.SDK_INT >= 26) {


/**
 * 对Android O以上做出改动
 */


            String start = "<WifiConfiguration>";
            String end = "</WifiConfiguration>";

            Pattern network = Pattern.compile(start+"(.*?)"+end, Pattern.DOTALL);

            Matcher networkMatcher = network.matcher(wifiConf.toString());
            while (networkMatcher.find()) {
                String networkBlock = networkMatcher.group();

               // Log.d(TAG, "Read: "+networkBlock);

                Pattern ssid = Pattern.compile("<string name=\"SSID\">"+"(.*?)"+"</string>");
                Matcher ssidMatcher = ssid.matcher(networkBlock);

                if (ssidMatcher.find()) {
                    WifiInfo wifiInfo = new WifiInfo();
                    wifiInfo.ssid = ssidMatcher.group(1).replace("&quot;","");
                    Pattern psk = Pattern.compile("<string name=\"PreSharedKey\">"+"(.*?)"+"</string>");
                    Matcher pskMatcher = psk.matcher(networkBlock);
                    if (pskMatcher.find()) {
                        wifiInfo.password = pskMatcher.group(1).replace("&quot;","");
                    } else {
                        wifiInfo.password = "无密码";
                    }
                    wifiInfos.add(wifiInfo);
                }
            }


        } else {

            Pattern network = Pattern.compile("network=\\{([^\\}]+)\\}", Pattern.DOTALL);
            Matcher networkMatcher = network.matcher(wifiConf.toString());
            while (networkMatcher.find()) {
                String networkBlock = networkMatcher.group();

                Pattern ssid = Pattern.compile("ssid=\"([^\"]+)\"");
                Matcher ssidMatcher = ssid.matcher(networkBlock);

                if (ssidMatcher.find()) {
                    WifiInfo wifiInfo = new WifiInfo();
                    wifiInfo.ssid = ssidMatcher.group(1);
                    Pattern psk = Pattern.compile("psk=\"([^\"]+)\"");
                    Matcher pskMatcher = psk.matcher(networkBlock);
                    if (pskMatcher.find()) {
                        wifiInfo.password = pskMatcher.group(1);
                    } else {
                        wifiInfo.password = "无密码";
                    }
                    wifiInfos.add(wifiInfo);
                }
            }

        }


        Collections.reverse(wifiInfos);
        return wifiInfos;
    }

}