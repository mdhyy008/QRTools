package com.dabai.qrtools;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dabai.qrtools.utils.AESUtils3;
import com.dabai.qrtools.utils.Base64;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.wildma.pictureselector.PictureSelector;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextQRActivity extends AppCompatActivity {

    ImageView img;
    TextInputLayout til;
    ConstraintLayout cons;

    boolean isOK = false;
    File file;
    private EditText edit;
    private int QRColor, QRBackColor = Color.WHITE;

    boolean is_rad = true;
    private String TAG = "dabaizzz";


    Button add_history;
    static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_qr);

        setTitle("二维码生成");

        context = getApplicationContext();
        img = findViewById(R.id.QR_create_imageView);
        til = findViewById(R.id.QR_create_input);
        cons = findViewById(R.id.cons);

        add_history = findViewById(R.id.add_history);

        Intent intent2 = getIntent();
        String downlink = intent2.getStringExtra("download");

        //dark
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);


        //是否阻止截图
        if (Control.is_sc) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        edit = til.getEditText();

        CardView imgcard = findViewById(R.id.QR_create_imgcard);

        //qr生成图的单击事件

        imgcard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                imageView = (ImageView) findViewById(R.id.image_view);

                //展示在dialog上面的大图
                dialog = new Dialog(TextQRActivity.this, android.R.style.Theme_NoTitleBar_Fullscreen);

                WindowManager.LayoutParams attributes = getWindow().getAttributes();
                attributes.width = WindowManager.LayoutParams.MATCH_PARENT;
                attributes.height = WindowManager.LayoutParams.MATCH_PARENT;
                dialog.getWindow().setAttributes(attributes);

                image = getImageView();
                dialog.setContentView(image);

                //大图的点击事件（点击让他消失）
                image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();


            }
        });

        imgcard.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                try {
                    hideInput();
                } catch (Exception e) {
                }

                final String[] items1 = {"添加标题"};
                final String[] items2 = {"添加图标", "添加标题", "二维码颜色", "二维码背景色", "恢复默认"};


                AlertDialog addddd = new AlertDialog.Builder(TextQRActivity.this)
                        .setTitle("美化二维码")
                        .setItems(edit.getText().toString().isEmpty() ? items1 : items2, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                switch (edit.getText().toString().isEmpty() ? items1[which] : items2[which]) {

                                    case "恢复默认":

                                        AlertDialog addddd = new AlertDialog.Builder(TextQRActivity.this)
                                                .setTitle("恢复")
                                                .setItems(new String[]{"默认样式", "默认颜色"}, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        switch (which) {
                                                            case 0:
                                                                TextView icrtext = findViewById(R.id.textView2);
                                                                icrtext.setVisibility(View.GONE);
                                                                CardView icrcard = findViewById(R.id.icrcard);
                                                                icrcard.setVisibility(View.GONE);
                                                                break;
                                                            case 1:
                                                                QRBackColor = Color.parseColor("#ffffff");
                                                                QRColor = Color.parseColor("#000000");

                                                                Bitmap bit = createQRCodeBitmap(edit.getText().toString(), 700, 700, "UTF-8", "H", "1", QRColor, QRBackColor);

                                                                if (bit == null){
                                                                    Toast.makeText(TextQRActivity.this, "生成失败:最多可容纳1850个大写字母或2710个数字或1108个字节，或500多个汉字!", Toast.LENGTH_SHORT).show();
                                                                    return;
                                                                }

                                                                img.setImageBitmap(bit);
                                                                try {
                                                                    hideInput();
                                                                } catch (Exception e) {
                                                                }
                                                                break;
                                                        }
                                                    }
                                                }).show();


                                        Window windowve = addddd.getWindow();//对话框窗口
                                        windowve.setGravity(Gravity.CENTER);//设置对话框显示在屏幕中间
                                        windowve.setWindowAnimations(R.style.dialog_style_top);//添加动画

                                        break;
                                    case "二维码颜色":

                                        ColorPickerDialogBuilder
                                                .with(TextQRActivity.this)
                                                .setTitle("选择颜色")
                                                .initialColor(Color.parseColor("#ffffff"))
                                                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                                                .density(12)
                                                .setOnColorSelectedListener(new OnColorSelectedListener() {
                                                    @Override
                                                    public void onColorSelected(int selectedColor) {
                                                    }
                                                })
                                                .setPositiveButton("选择", new ColorPickerClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                                        QRColor = selectedColor;
                                                        Bitmap bit = createQRCodeBitmap(edit.getText().toString(), 700, 700, "UTF-8", "H", "1", QRColor, QRBackColor);

                                                        if (bit == null){
                                                            Toast.makeText(TextQRActivity.this, "生成失败:最多可容纳1850个大写字母或2710个数字或1108个字节，或500多个汉字!", Toast.LENGTH_SHORT).show();
                                                            return;
                                                        }

                                                        img.setImageBitmap(bit);
                                                        is_rad = false;
                                                        try {
                                                            hideInput();
                                                        } catch (Exception e) {
                                                        }
                                                    }
                                                })
                                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                    }
                                                })
                                                .build()
                                                .show();

                                        break;
                                    case "二维码背景色":

                                        ColorPickerDialogBuilder
                                                .with(TextQRActivity.this)
                                                .setTitle("选择颜色")
                                                .initialColor(Color.parseColor("#ffffff"))
                                                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                                                .density(12)
                                                .setOnColorSelectedListener(new OnColorSelectedListener() {
                                                    @Override
                                                    public void onColorSelected(int selectedColor) {
                                                    }
                                                })
                                                .setPositiveButton("选择", new ColorPickerClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                                        QRBackColor = selectedColor;
                                                        Bitmap bit = createQRCodeBitmap(edit.getText().toString(), 700, 700, "UTF-8", "H", "1", QRColor, QRBackColor);

                                                        if (bit == null){
                                                            Toast.makeText(TextQRActivity.this, "生成失败:最多可容纳1850个大写字母或2710个数字或1108个字节，或500多个汉字!", Toast.LENGTH_SHORT).show();
                                                            return;
                                                        }

                                                        img.setImageBitmap(bit);
                                                        try {
                                                            hideInput();
                                                        } catch (Exception e) {
                                                        }
                                                    }
                                                })
                                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                    }
                                                })
                                                .build()
                                                .show();

                                        break;
                                    case "添加图标":
                                        /**
                                         * 图库选择  可裁剪
                                         *
                                         * */
                                        PictureSelector
                                                .create(TextQRActivity.this, PictureSelector.SELECT_REQUEST_CODE)
                                                .selectPicture(true, 200, 200, 1, 1);

                                        break;
                                    case "添加标题":

                                        final EditText inputServer = new EditText(TextQRActivity.this);
                                        inputServer.setMaxLines(3);

                                        AlertDialog.Builder builder = new AlertDialog.Builder(TextQRActivity.this);
                                        builder.setTitle("文本").setView(inputServer)
                                                .setNegativeButton("取消", null);
                                        builder.setPositiveButton("添加", new DialogInterface.OnClickListener() {

                                            public void onClick(DialogInterface dialog, int which) {
                                                if (!inputServer.getText().toString().isEmpty()) {
                                                    TextView icrtext = findViewById(R.id.textView2);
                                                    icrtext.setText(inputServer.getText().toString());
                                                    icrtext.setVisibility(View.VISIBLE);
                                                } else {
                                                    Snackbar.make(cons, "无文本，不生效", Snackbar.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                        builder.show();
                                        break;
                                }
                            }
                        }).show();


                Window windowve = addddd.getWindow();//对话框窗口
                windowve.setGravity(Gravity.CENTER);//设置对话框显示在屏幕中间
                windowve.setWindowAnimations(R.style.dialog_style_bottom);//添加动画

                return true;
            }
        });


        edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {

                String text = til.getEditText().getText().toString();
                if (!text.isEmpty()) {
                    QRColor = Color.BLACK;
                    Bitmap bit = createQRCodeBitmap(text, 700, 700, "UTF-8", "H", "1", QRColor, QRBackColor);

                    if (bit == null){
                        Toast.makeText(TextQRActivity.this, "生成失败:最多可容纳1850个大写字母或2710个数字或1108个字节，或500多个汉字!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    img.setImageBitmap(bit);
                    isOK = true;

                } else {
                    Snackbar.make(cons, "无内容，不生成", Snackbar.LENGTH_SHORT).show();
                }
            }
        });




        add_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /**
                 * 添加到历史记录
                 */

               String his = get_sharedString("QR_create","");

               String text = til.getEditText().getText().toString();

               for (String a : his.split("@@@")){
                   if (a.equals(text)){
                       add_history.setText("已存在!");
                       return;
                   }
               }

                Bitmap bit = createQRCodeBitmap(edit.getText().toString(), 700, 700, "UTF-8", "H", "1", QRColor, QRBackColor);

                if (bit == null){
                    add_history.setText("超出最大长度，不能添加!");
                    return;
                }

               set_sharedString("QR_create",his+text+"@@@");
               add_history.setText("添加成功!");

            }
        });


        if (downlink != null) {
            edit.setText(downlink);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

    }


    /**
     * 提交与获取
     *
     * @param key
     * @param value
     */
    public void set_sharedString(String key, String value) {
        SharedPreferences sp = this.getSharedPreferences("data", 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public String get_sharedString(String key, String moren) {
        SharedPreferences sp = this.getSharedPreferences("data", 0);
        return sp.getString(key, moren);
    }


    /**
     * 加载本地图片
     *
     * @param url
     * @return
     */
    public static Bitmap getLoacalBitmap(String url) {
        try {
            FileInputStream fis = new FileInputStream(url);
            return BitmapFactory.decodeStream(fis);  ///把流转化为Bitmap图片

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*结果回调*/
        if (requestCode == PictureSelector.SELECT_REQUEST_CODE) {
            if (data != null) {
                String picturePath = data.getStringExtra(PictureSelector.PICTURE_PATH);
                Bitmap bitmap = getLoacalBitmap(picturePath); //从本地取图片(在cdcard中获取)  //
                ImageView image1 = (ImageView) findViewById(R.id.icr);  //获得ImageView对象
                image1.setImageBitmap(bitmap); //设置Bitmap

                CardView icrcard = findViewById(R.id.icrcard);
                icrcard.setVisibility(View.VISIBLE);
            }
        }
    }


    private ImageView imageView;
    private Dialog dialog;
    private ImageView image;


    //动态的ImageView
    private ImageView getImageView() {
        ImageView imageView = new ImageView(this);

        //宽高
        imageView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        //imageView设置图片
        @SuppressLint("ResourceType")
        CardView imgcard = findViewById(R.id.QR_create_imgcard);
        Bitmap bitmap = getBitmapByView(imgcard);//iv是View
        Drawable drawable = new BitmapDrawable(bitmap);
        imageView.setImageDrawable(drawable);

        return imageView;
    }



    public void save_QR(View view) {


        try {
            if (isOK) {

                CardView imgcard = findViewById(R.id.QR_create_imgcard);
                Bitmap bitmap = getBitmapByView(imgcard);//iv是View
                int ran = new Random().nextInt(1000);
                savePhotoToSDCard(bitmap, "/sdcard/二维码助手", "QRCode_" + ran);
                file = new File("/sdcard/二维码助手/QRCode_" + ran + ".png");
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));

                Snackbar.make(cons, "保存" + file.getAbsolutePath() + "成功", Snackbar.LENGTH_SHORT).show();

            } else {
                Snackbar.make(cons, "现在不能保存", Snackbar.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
        }

        try {
            hideInput();
        } catch (Exception e) {
        }

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

    public void hideInput() throws Exception {
        ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
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
            return null;
        }
    }


    public void hide_input(View view) {
        try {
            hideInput();
        } catch (Exception e) {
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

    public void copy_text(View view) {

        if (!edit.getText().toString().isEmpty()) {

            ClipboardManager clipboardManager = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData mclipData = ClipData.newPlainText("Label", edit.getText().toString());
            clipboardManager.setPrimaryClip(mclipData);

            Snackbar.make(cons, "复制成功", Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(cons, "无文本，不复制", Snackbar.LENGTH_SHORT).show();

        }
    }



    private static final int BLACK = 0xff000000;
    private static final int WHITE = 0xFFFFFFFF;
    private static BarcodeFormat barcodeFormat = BarcodeFormat.CODE_128;

    public static Bitmap creatBarcode(String contents, int desiredWidth, int desiredHeight) {
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix result = null;
        try {
            result = writer.encode(contents, barcodeFormat, desiredWidth,
                    desiredHeight);
        } catch (WriterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        // All are 0, or black, by default
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }


    /**
     * 编码 转换utf8
     *
     * @param view
     */
    public void code_base64(View view) {

        String text = til.getEditText().getText().toString();

        Base64 base = new Base64();
        String resul = base.encode(text);

        if (!resul.isEmpty()) {

            QRColor = Color.BLACK;
            Bitmap bit = createQRCodeBitmap(resul, 700, 700, "UTF-8", "H", "1", QRColor, QRBackColor);

            if (bit == null){
                Toast.makeText(TextQRActivity.this, "生成失败:最多可容纳1850个大写字母或2710个数字或1108个字节，或500多个汉字!", Toast.LENGTH_SHORT).show();
                return;
            }

            img.setImageBitmap(bit);
            isOK = true;
            Snackbar.make(cons, "转换完成！", Snackbar.LENGTH_SHORT).show();

        } else {
            Snackbar.make(cons, "无内容，不生成", Snackbar.LENGTH_SHORT).show();
        }
        try {
            hideInput();
        } catch (Exception e) {
        }
    }


    public void qr_ipset(View view) {
        edit.setText("获取中...");
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    HtmlUtils hu = new HtmlUtils();
                    String html = hu.getHtml("https://www.mxnzp.com/api/ip/self");
                    JSONObject jo = new JSONObject(html);
                    JSONObject jo2 = new JSONObject(jo.getString("data"));
                    final String ip = jo2.getString("ip");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            edit.setText("" + ip);
                            Snackbar.make(cons, "获取完成！", Snackbar.LENGTH_SHORT).show();

                        }
                    });

                } catch (Exception e) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            edit.setText("网络异常...");
                        }
                    });
                }
            }
        }).start();
    }


    /**
     * 判断字符串中是否包含中文
     *
     * @param str 待校验字符串
     * @return 是否为中文
     * @warn 不能校验是否为中文标点符号
     */
    public static boolean isContainChinese(String str) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }


    String all_texttt;

    public void qr_pass(View view) {

        all_texttt = til.getEditText().getText().toString();

        if (!all_texttt.isEmpty()) {

            new MaterialDialog.Builder(this)
                    .title("加密文本")
                    .inputType(InputType.TYPE_CLASS_TEXT)
                    .input("请输入密钥", null, new MaterialDialog.InputCallback() {
                        @Override
                        public void onInput(MaterialDialog dialog, CharSequence input) {

                            if (!isContainChinese(input + "") && !(input + "").equals("")) {
                                String txt = null;
                                try {
                                    txt = AESUtils3.encrypt(all_texttt, "" + input);
                                } catch (Exception e) {
                                }

                                QRColor = Color.BLACK;
                                Bitmap bit = createQRCodeBitmap(txt, 700, 700, "UTF-8", "H", "1", QRColor, QRBackColor);

                                if (bit == null){
                                    Toast.makeText(TextQRActivity.this, "生成失败:最多可容纳1850个大写字母或2710个数字或1108个字节，或500多个汉字!", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                img.setImageBitmap(bit);
                                isOK = true;
                                Snackbar.make(cons, "加密完成！", Snackbar.LENGTH_SHORT).show();

                            } else {
                                Snackbar.make(cons, "无密钥或包含中文字符，不生成", Snackbar.LENGTH_SHORT).show();
                            }

                        }
                    })
                    .positiveText("确定")
                    .neutralText("取消")
                    .show();

        } else {
            Snackbar.make(cons, "无内容，不生成", Snackbar.LENGTH_SHORT).show();
        }

        try {
            hideInput();
        } catch (Exception e) {
        }

    }

    public void code_utf8(View view) {


        String text = til.getEditText().getText().toString();
        if (!text.isEmpty()) {
            QRColor = Color.BLACK;
            Bitmap bit = createQRCodeBitmap(text, 700, 700, "UTF-8", "H", "1", QRColor, QRBackColor);

            if (bit == null){
                Toast.makeText(TextQRActivity.this, "生成失败:最多可容纳1850个大写字母或2710个数字或1108个字节，或500多个汉字!", Toast.LENGTH_SHORT).show();
                return;
            }

            img.setImageBitmap(bit);
            isOK = true;
            Snackbar.make(cons, "转换完成！", Snackbar.LENGTH_SHORT).show();

        } else {
            Snackbar.make(cons, "无内容，不生成", Snackbar.LENGTH_SHORT).show();
        }
        try {
            hideInput();
        } catch (Exception e) {
        }
    }


    public void qr_barcode(View view) {
        String text = til.getEditText().getText().toString();


        if (!text.isEmpty()) {
           if (isContainChinese(text)){
               Snackbar.make(cons, "条形码转换不支持中文！", Snackbar.LENGTH_SHORT).show();

           }
           else {
               try {
                   QRColor = Color.BLACK;
                   Bitmap qrCodeBitmap = creatBarcode(text, 1000, 300);
                   img.setImageBitmap(qrCodeBitmap);
                   isOK = true;

                   Snackbar.make(cons, "转换完成！", Snackbar.LENGTH_SHORT).show();
               } catch (Exception e) {
                   Snackbar.make(cons, "转换失败！", Snackbar.LENGTH_SHORT).show();
               }

           }
        } else {
            Snackbar.make(cons, "无内容，不生成", Snackbar.LENGTH_SHORT).show();
        }

        try {
            hideInput();
        } catch (Exception e) {
        }

    }
}
