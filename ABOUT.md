## 二维码工具 - QRT

 v2.0 用到的权限

   
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" /> <!-- 联系人权限 -->
    <uses-permission android:name="android.permission.READ_CONTACTS" />
    <uses-permission android:name="android.permission.WRITE_CONTACTS" /> <!-- 储存权限 -->
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" /> <!-- 安装SHORTCUT -->
    <uses-permission android:name="android.permission.INSTALL_SHORTCUT" />
    <uses-permission android:name="android.permission.UNINSTALL_SHORTCUT" /> <!-- 添加快捷方式 -->
    <uses-permission android:name="com.android.launcher.permission.INSTALL_SHORTCUT" /> <!-- 移除快捷方式 -->
    <uses-permission android:name="com.android.launcher.permission.UNINSTALL_SHORTCUT" /> <!-- 查询快捷方式 -->
    <uses-permission android:name="com.android.launcher.permission.READ_SETTINGS" /> <!-- 网络权限 -->
    <uses-permission android:name="android.permission.INTERNET" /> <!-- 修改系统设置 -->
    <uses-permission
        android:name="android.permission.WRITE_SETTINGS"
        tools:ignore="ProtectedPermissions" /> <!-- wifi -->
    <uses-permission android:name="android.permission.CHANGE_NETWORK_STATE" />
    <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" /> <!-- 震动 和 安装App -->
    <uses-permission android:name="android.permission.VIBRATE" /> <!-- 前台服务必须 -->
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" /> <!-- 查看使用情况 -->
    <uses-permission
        android:name="android.permission.PACKAGE_USAGE_STATS"
        tools:ignore="ProtectedPermissions" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
   
   
   
   
 

 v2.0 用到的开源库
 
    implementation 'com.android.support:appcompat-v7:28.0.0'
    implementation 'com.android.support.constraint:constraint-layout:1.1.3'
    implementation 'com.android.support:cardview-v7:28.0.0'
    implementation 'com.google.zxing:core:3.3.0'
    implementation 'com.android.support:design:28.0.0'
    implementation 'com.android.support:customtabs:28.0.0'
    implementation 'com.github.QuadFlask:colorpicker:0.0.13'
    implementation 'com.github.wildma:PictureSelector:1.1.1'
    implementation 'com.github.qingmei2:rximagepicker:0.2.0'
    implementation 'com.github.qingmei2:rximagepicker_support:0.2.0'
    implementation 'com.android.support:mediarouter-v7:28.0.0' 
    implementation 'cn.simonlee.xcodescanner:zbar:1.1.7'
    implementation 'jp.wasabeef:blurry:2.1.1'
    implementation 'com.mjun:mtransition:0.1.3'
