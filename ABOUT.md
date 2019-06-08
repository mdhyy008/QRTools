# 关于这个项目

### 项目介绍

这是个二维码扫描和生成于一身的工具。

### 用到的开源项目

    implementation 'com.android.support:appcompat-v7:28.0.0'
    implementation 'com.android.support.constraint:constraint-layout:1.1.3'
    implementation 'com.android.support:cardview-v7:28.0.0'
    implementation 'com.google.zxing:core:3.3.0'
    implementation 'com.android.support:design:28.0.0'
    implementation 'com.android.support:customtabs:28.0.0'

### 程序申请的权限


    <!-- 使用生物识别 -->
    <uses-permission android:name="android.permission.USE_BIOMETRIC"/>
    <uses-permission android:name="android.permission.USE_FINGERPRINT"/>
    
    <!-- 联系人权限 -->
    <uses-permission android:name="android.permission.READ_CONTACTS"/>
    <uses-permission android:name="android.permission.WRITE_CONTACTS"/>
    <!-- 储存权限 -->
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <!-- 安装SHORTCUT -->
    <uses-permission android:name="android.permission.INSTALL_SHORTCUT" />
    <uses-permission android:name="android.permission.UNINSTALL_SHORTCUT" />
    <!-- 网络权限 -->
    <uses-permission android:name="android.permission.INTERNET" />
    <!-- 修改系统设置 -->
    <uses-permission
        android:name="android.permission.WRITE_SETTINGS" tools:ignore="ProtectedPermissions" />
    <!-- wifi -->
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
    <!-- 震动 和 安装App -->
    <uses-permission android:name="android.permission.VIBRATE" />
    <uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />

    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.BODY_SENSORS" />
    <!-- 前台服务必须 -->
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />

