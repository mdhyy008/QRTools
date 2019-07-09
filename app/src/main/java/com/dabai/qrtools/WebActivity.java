package com.dabai.qrtools;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.DownloadListener;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class WebActivity extends AppCompatActivity {

    WebView webview;
    ProgressBar pro;
    String last;
    ConstraintLayout cons;
    TextView webtip;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.web, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        //是否阻止截图
        if (Control.is_sc) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        }


        //dark
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        pro = findViewById(R.id.progressBar);
        webview = findViewById(R.id.webview);
        cons = findViewById(R.id.cons);
        webtip = findViewById(R.id.webtip);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        String link = null;
        try {
            Intent intent = getIntent();
            link = intent.getStringExtra("link");


            if (link == null) {
                intent = getIntent();
                link = "" + intent.getData();
                webview.loadUrl(link);
            }


            last = link;
            webview.loadUrl(link);

        } catch (Exception e) {
            Toast.makeText(this, "程序错误", Toast.LENGTH_SHORT).show();
            finish();
        }


        //声明WebSettings子类
        WebSettings webSettings = webview.getSettings();

        //如果访问的页面中要与Javascript交互，则webview必须设置支持Javascript
        webSettings.setJavaScriptEnabled(true);
        // 若加载的 html 里有JS 在执行动画等操作，会造成资源浪费（CPU、电量）
        // 在 onStop 和 onResume 里分别把 setJavaScriptEnabled() 给设置成 false 和 true 即可
// 特别注意：5.1以上默认禁止了https和http混用，以下方式是开启
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webview.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }


        //设置自适应屏幕，两者合用
        webSettings.setUseWideViewPort(true); //将图片调整到适合webview的大小
        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小

        //缩放操作
        webSettings.setSupportZoom(true); //支持缩放，默认为true。是下面那个的前提。
        webSettings.setBuiltInZoomControls(true); //设置内置的缩放控件。若为false，则该WebView不可缩放
        webSettings.setDisplayZoomControls(false); //隐藏原生的缩放控件

        //其他细节操作
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); //关闭webview中缓存
        webSettings.setAllowFileAccess(true); //设置可以访问文件
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); //支持通过JS打开新窗口
        webSettings.setLoadsImagesAutomatically(true); //支持自动加载图片
        webSettings.setDefaultTextEncodingName("utf-8");//设置编码格式


        webview.setDownloadListener(new DownloadListener() {

            private AlertDialog ad;

            @Override
            public void onDownloadStart(final String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setTitle("正在提取链接...");
                            }
                        });
                    }
                }).start();

                ad = new AlertDialog.Builder(WebActivity.this).setCancelable(false).setTitle("下载文件").setMessage("文件描述:" + contentDisposition + "\n类型:" + mimetype + "\n下载地址:" + url)
                        .setNeutralButton("回首页", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ad.dismiss();
                                webview.loadUrl(last);
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                setTitle("首页");
                                            }
                                        });
                                    }
                                }).start();
                            }
                        }).setNegativeButton("提取直链", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent resultIntent = new Intent(WebActivity.this, TextQRActivity.class);
                                resultIntent.putExtra("download", url);
                                startActivity(resultIntent);
                                finish();
                            }
                        }).setPositiveButton("下载", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new DabaiUtils().openLink(WebActivity.this, url);
                                Toast.makeText(WebActivity.this, "选择一个下载器", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }).show();

            }
        });


        webview.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);

                if (newProgress < 100) {
                    webtip.setText(newProgress + "%");

                } else {
                    webtip.setVisibility(View.GONE);
                }

            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                AlertDialog.Builder b = new AlertDialog.Builder(WebActivity.this);
                b.setTitle("Alert");
                b.setMessage(message);
                b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();
                    }
                });
                b.setCancelable(false);
                b.create().show();
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                return super.onJsConfirm(view, url, message, result);
            }
        });


        webview.setWebViewClient(new WebViewClient() {


            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                if (url.startsWith("http:") || url.startsWith("https:")) {
                    view.loadUrl(url);

                    return false;//返回false 意思是不拦截，让webview自己处理
                } else {
                    // Otherwise allow the OS to handle things like tel, mailto, etc.
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    finish();
                    return true;
                }

            }
        });


    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webview.canGoBack()) {
            webview.goBack();//返回上个页面
            return true;
        } else {
            Snackbar.make(cons, "退出?", Snackbar.LENGTH_SHORT).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            }).setActionTextColor(Color.WHITE).show();
            return false;
        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case android.R.id.home:
                // 处理返回逻辑
                finish();
                return true;
            case R.id.share_link:
                new DabaiUtils().sendText(getApplicationContext(), webview.getUrl());
                return true;
            case R.id.other_web:
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(webview.getUrl()));
                startActivity(intent);
                return true;
            case R.id.copy_link:
                ClipboardManager clipboardManager = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mclipData = ClipData.newPlainText("Label", webview.getUrl());
                clipboardManager.setPrimaryClip(mclipData);
                Snackbar.make(cons, "复制成功", Snackbar.LENGTH_SHORT).show();
                return true;
            case R.id.f5:

                webview.loadUrl(webview.getUrl());

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
