package com.dabai.qrtools;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

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

public class TextOpen extends AppCompatActivity {

    TextView tv;
    ImageView img;
    private File file;
    ConstraintLayout cons;

    Switch sw1;

    String tenolink;
    String telink;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_open);
        //dark
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        Intent intent = getIntent();
        //val
        tv = findViewById(R.id.text_open_tv);
        img = findViewById(R.id.text_open_imageView);
        sw1 = findViewById(R.id.text_open_switch1);
        cons = findViewById(R.id.cons);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //是否阻止截图
        if (Control.is_sc) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        }


        try {
            if (intent.getClipData().getItemAt(0).getText() == null) {
                Toast.makeText(this, "没有获取到数据，无法生成二维码", Toast.LENGTH_SHORT).show();
                gohome();
                finish();
            }
        } catch (Exception e) {
            finish();
        }


        try {
            String link = "" + intent.getClipData().getItemAt(0).getText();
            tenolink = link;
            tv.setText(link);

            Bitmap bit = createQRCodeBitmap(link, 700, 700, "UTF-8", "H", "1", Color.BLACK, Color.WHITE);
            img.setImageBitmap(bit);
        } catch (Exception e) {
            Toast.makeText(this, "没有获取到数据，无法生成二维码", Toast.LENGTH_SHORT).show();
            gohome();
            finish();
        }


        //监听
        tv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                ClipboardManager clipboardManager = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                clipboardManager.setText(tv.getText().toString());
                Snackbar.make(cons, "复制成功", Snackbar.LENGTH_SHORT).show();

                return true;
            }
        });


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
                        telink = matcher.group();
                    }
                    tv.setText(telink);

                    Bitmap bit = createQRCodeBitmap(telink, 700, 700, "UTF-8", "L", "1", Color.BLACK, Color.WHITE);
                    img.setImageBitmap(bit);
                } else {
                    tv.setText(tenolink);

                    Bitmap bit = createQRCodeBitmap(tenolink, 700, 700, "UTF-8", "L", "1", Color.BLACK, Color.WHITE);
                    img.setImageBitmap(bit);

                }
            }
        });


    }

    private void gohome() {
        Intent intent2 = new Intent();
        // 为Intent设置Action、Category属性
        intent2.setAction(Intent.ACTION_MAIN);// "android.intent.action.MAIN"
        intent2.addCategory(Intent.CATEGORY_HOME); //"android.intent.category.HOME"
        startActivity(intent2);
    }


    //按钮事件
    public void share_link(View v) {
        sendText("" + tv.getText().toString());
    }

    public void save_qr(View view) {
        CardView imgcard = findViewById(R.id.text_open_imgcard);
        Bitmap bitmap = getBitmapByView(imgcard);//iv是View
        int ran = new Random().nextInt(1000);
        savePhotoToSDCard(bitmap, "/sdcard/二维码助手", "TextOpen_" + ran);
        file = new File("/sdcard/二维码助手/TextOpen_" + ran + ".png");
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));

        Snackbar.make(cons, "保存" + file.getAbsolutePath() + "成功", Snackbar.LENGTH_SHORT).show();
    }


    private void sendText(String p0) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "This is text to send.");
        // 指定发送内容的类型
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_TEXT, p0);
        startActivity(Intent.createChooser(sendIntent, "Share"));
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
}
