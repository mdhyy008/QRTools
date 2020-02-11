package com.dabai.qrtools;


import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Random;
import java.util.regex.Matcher;

public class ClipActivity extends AppCompatActivity {


    ConstraintLayout cons;
    TextView tv;
    ImageView img;
    Switch sw1;

    String nolink;
    String link;
    private String text;
    private File file;


    @Override
    protected void onResume() {

        super.onResume();

        data_f5();

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clip);
        getSupportActionBar().setElevation(0);

        tv = findViewById(R.id.clip_tv);
        img = findViewById(R.id.clip_imageView);
        sw1 = findViewById(R.id.switch1);
        cons = findViewById(R.id.cons);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //是否阻止截图
        if (Control.is_sc) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        }


        //dark
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);



    }


    /**
     * 生成简单二维码
     *
     * @param content                字符串内容
     * @param width                  二维码宽度
     * @param height                 二维码高度
     * @param character_set          编码方式（一般使用UTF-8）
     * @param error_correction_level 容错率 L：7% M：15% Q：25% H：35%
     * @param margin                 空白边距（二维码与边框的空白区域）
     * @param color_black            黑色色块
     * @param color_white            白色色块
     * @return BitMap
     */
    public static Bitmap createQRCodeBitmap(String content, int width, int height,
                                            String character_set, String error_correction_level,
                                            String margin, int color_black, int color_white) {
        // 字符串内容判空
        if (TextUtils.isEmpty(content)) {
            return null;
        }
        // 宽和高>=0
        if (width < 0 || height < 0) {
            return null;
        }
        try {
            /** 1.设置二维码相关配置 */
            Hashtable<EncodeHintType, String> hints = new Hashtable<>();
            // 字符转码格式设置
            if (!TextUtils.isEmpty(character_set)) {
                hints.put(EncodeHintType.CHARACTER_SET, character_set);
            }
            // 容错率设置
            if (!TextUtils.isEmpty(error_correction_level)) {
                hints.put(EncodeHintType.ERROR_CORRECTION, error_correction_level);
            }
            // 空白边距设置
            if (!TextUtils.isEmpty(margin)) {
                hints.put(EncodeHintType.MARGIN, margin);
            }
            /** 2.将配置参数传入到QRCodeWriter的encode方法生成BitMatrix(位矩阵)对象 */
            BitMatrix bitMatrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);

            /** 3.创建像素数组,并根据BitMatrix(位矩阵)对象为数组元素赋颜色值 */
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    //bitMatrix.get(x,y)方法返回true是黑色色块，false是白色色块
                    if (bitMatrix.get(x, y)) {
                        pixels[y * width + x] = color_black;//黑色色块像素设置
                    } else {
                        pixels[y * width + x] = color_white;// 白色色块像素设置
                    }
                }
            }
            /** 4.创建Bitmap对象,根据像素数组设置Bitmap每个像素点的颜色值,并返回Bitmap对象 */
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }


    public void data_f5() {


        ClipboardManager clipboardManager = (ClipboardManager)getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            android.text.ClipboardManager cb = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            text = null;
            text = cb.getText().toString();

        } else {
            ClipboardManager cb = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            text = null;
            text = cb.getPrimaryClip().getItemAt(0).getText().toString();
        }

        nolink = text;
        tv.setText(text);


        Bitmap bit = createQRCodeBitmap(text, 700, 700, "UTF-8", "H", "1", Color.BLACK, Color.WHITE);
        img.setImageBitmap(bit);


        sw1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                    /**
                     * 提取文本中的链接
                     */
                    String data = tv.getText().toString();
                    Matcher matcher = Patterns.WEB_URL.matcher(data);
                    if (matcher.find()) {
                        link = matcher.group();
                    }
                    tv.setText(link);

                    Bitmap bit = createQRCodeBitmap(link, 700, 700, "UTF-8", "L", "1", Color.BLACK, Color.WHITE);
                    img.setImageBitmap(bit);
                } else {
                    tv.setText(nolink);

                    Bitmap bit = createQRCodeBitmap(nolink, 700, 700, "UTF-8", "L", "1", Color.BLACK, Color.WHITE);
                    img.setImageBitmap(bit);

                }
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case android.R.id.home:
                // 处理返回逻辑
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void save_QR(View view) {

        CardView imgcard = findViewById(R.id.clip_imgcard);
        Bitmap bitmap = getBitmapByView(imgcard);//iv是View
        int ran = new Random().nextInt(1000);
        savePhotoToSDCard(bitmap, "/sdcard/二维码助手", "Clip_" + ran);
        file = new File("/sdcard/二维码助手/Clip_" + ran + ".png");
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
        Snackbar.make(cons, "保存" + file.getAbsolutePath() + "成功", Snackbar.LENGTH_SHORT).show();
    }


    //根据view获取bitmap
    public static Bitmap getBitmapByView(View view) {
        int h = 0;
        Bitmap bitmap = null;
        bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
                Bitmap.Config.RGB_565);
        final Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    //检查sd
    public static boolean checkSDCardAvailable() {
        return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    }

    public static void savePhotoToSDCard(Bitmap photoBitmap, String path, String photoName) {
        if (checkSDCardAvailable()) {
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File photoFile = new File(path, photoName + ".png");
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(photoFile);
                if (photoBitmap != null) {
                    if (photoBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)) {
                        fileOutputStream.flush();
                    }
                }
            } catch (FileNotFoundException e) {
                photoFile.delete();
                e.printStackTrace();
            } catch (IOException e) {
                photoFile.delete();
                e.printStackTrace();
            } finally {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
