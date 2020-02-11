package com.dabai.qrtools;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.dabai.qrtools.utils.DragTouchListener;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import cn.simonlee.xcodescanner.core.CameraScanner;
import cn.simonlee.xcodescanner.core.GraphicDecoder;
import cn.simonlee.xcodescanner.core.NewCameraScanner;
import cn.simonlee.xcodescanner.core.OldCameraScanner;
import cn.simonlee.xcodescanner.core.ZBarDecoder;
import cn.simonlee.xcodescanner.view.AdjustTextureView;

public class ScanToolActivity extends AppCompatActivity implements CameraScanner.CameraListener, TextureView.SurfaceTextureListener, GraphicDecoder.DecodeListener, View.OnClickListener {

    private AdjustTextureView mTextureView;
    private View mScannerFrameView;
    private CameraScanner mCameraScanner;
    protected GraphicDecoder mGraphicDecoder;
    protected String TAG = "XCodeScanner";
    private ImageButton mButton_Flash;
    private int[] mCodeType;

    TextView sc_text;
    CardView sc_card;


    AlertDialog resdia;
    private String result_end;
    private Vibrator vibrator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_tool);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setElevation(0);


        //是否阻止截图
        if (Control.is_sc) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        }
        //dark
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mTextureView = findViewById(R.id.textureview);
        mTextureView.setSurfaceTextureListener(this);

        mScannerFrameView = findViewById(R.id.scannerframe);

        mButton_Flash = findViewById(R.id.btn_flash);
        mButton_Flash.setOnClickListener(this);



        resdia = new AlertDialog.Builder(this).setTitle("结果").setMessage("正在过滤数据")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ToResult(result_end);

                        String his = get_sharedString("QR_scan","");

                        finish();

                        for (String a : his.split("@@@")){
                            if (a.equals(result_end)){
                                return;
                            }
                        }

                        set_sharedString("QR_scan",his+result_end+"@@@");

                    }
                }).create();


        /*
         * 注意，SDK21的设备是可以使用NewCameraScanner的，但是可能存在对新API支持不够的情况，比如红米Note3（双网通Android5.0.2）
         * 开发者可自行配置使用规则，比如针对某设备型号过滤，或者针对某SDK版本过滤
         * */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mCameraScanner = new NewCameraScanner(this);
        } else {
            mCameraScanner = new OldCameraScanner(this);
        }

        int checkResult1 = getApplicationContext().checkCallingOrSelfPermission(Manifest.permission.CAMERA);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {


        if (requestCode == 1) {
            startActivity(new Intent(this, getClass()));
            finish();

        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onRestart() {
        if (mTextureView.isAvailable()) {
            //部分机型转到后台不会走onSurfaceTextureDestroyed()，因此isAvailable()一直为true，转到前台后不会再调用onSurfaceTextureAvailable()
            //因此需要手动开启相机
            mCameraScanner.setPreviewTexture(mTextureView.getSurfaceTexture());
            mCameraScanner.setPreviewSize(mTextureView.getWidth(), mTextureView.getHeight());
            mCameraScanner.openCamera(this.getApplicationContext());
        }
        super.onRestart();
    }

    @Override
    protected void onPause() {
        mCameraScanner.closeCamera();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mCameraScanner.setGraphicDecoder(null);
        if (mGraphicDecoder != null) {
            mGraphicDecoder.setDecodeListener(null);
            mGraphicDecoder.detach();
        }
        mCameraScanner.detach();
        super.onDestroy();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.scan, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.sc_other:

               checkPermission();

                return true;
            case android.R.id.home:
                // 处理返回逻辑
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    //检查权限
    public void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            //发现没有权限，调用requestPermissions方法向用户申请权限，requestPermissions接收三个参数，第一个是context，第二个是一个String数组，我们把要申请的权限
            //名放在数组中即可，第三个是请求码，只要是唯一值就行
        } else {
            openAlbum();//有权限就打开相册
        }
    }

    public void openAlbum() {
        //通过intent打开相册，使用startactivityForResult方法启动actvity，会返回到onActivityResult方法，所以我们还得复写onActivityResult方法
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, 133);
    }
    //弹出窗口向用户申请权限


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 133:
                if (resultCode == RESULT_OK) {
                    if (Build.VERSION.SDK_INT >= 19) {
                        handleImageOnkitKat(data);//高于4.4版本使用此方法处理图片
                    } else {
                        handleImageBeforeKitKat(data);//低于4.4版本使用此方法处理图片
                    }
                }
                break;
            default:
                break;
        }
    }

    @TargetApi(19)
    private void handleImageOnkitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this, uri)) {
            //如果是document类型的uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];//解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android,providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }

        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            imagePath = getImagePath(uri, null);
        }
        displayImage(imagePath);
    }

    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        displayImage(imagePath);
    }

    //获得图片路径
    public String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);   //内容提供器
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                try {
                    path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));   //获取路径
                } catch (Exception e) {

                }
            }
        }
        cursor.close();
        return path;
    }

    //展示图片
    private void displayImage(String picturePath) {
        if (picturePath != null) {

            Bitmap bitmap = getLoacalBitmap(picturePath); //从本地取图片(在cdcard中获取)  //

            //文件 转 bitmap
            Bitmap obmp = BitmapFactory.decodeFile(picturePath);
            int width1 = obmp.getWidth();
            int height1 = obmp.getHeight();
            int[] data1 = new int[width1 * height1];
            obmp.getPixels(data1, 0, width1, 0, 0, width1, height1);
            RGBLuminanceSource source = new RGBLuminanceSource(width1, height1, data1);
            BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));

            new QrCodeAsyncTask().execute(bitmap1);

        } else {
            Toast.makeText(this, "获取图片失败,请换一个图片选择器试试!", Toast.LENGTH_SHORT).show();
        }
    }


    class QrCodeAsyncTask extends AsyncTask<BinaryBitmap, Void, Result> {

        @Override
        protected Result doInBackground(BinaryBitmap... params) {
            QRCodeReader reader = new QRCodeReader();
            Result result = null;
            try {
                result = reader.decode(params[0]);
            } catch (NotFoundException e) {
                e.printStackTrace();
            } catch (ChecksumException e) {
                e.printStackTrace();
            } catch (FormatException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(Result result) {
            super.onPostExecute(result);
            if (result != null) {
                ToResult("" + result);
                finish();

            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog addddddd = new AlertDialog.Builder(ScanToolActivity.this)
                                .setTitle("提示")
                                .setMessage("没有扫描到图片中的QR码数据")
                                .setPositiveButton("确定", null)
                                .show();

                        Window window = addddddd.getWindow();//对话框窗口
                        window.setGravity(Gravity.BOTTOM);//设置对话框显示在屏幕中间
                        window.setWindowAnimations(R.style.dialog_style_bottom);//添加动画

                    }
                });
            }
        }
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
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mCameraScanner.setPreviewTexture(surface);
        mCameraScanner.setPreviewSize(width, height);
        mCameraScanner.openCamera(this);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        // TODO 当View大小发生变化时，要进行调整。
//        mTextureView.setImageFrameMatrix();
//        mCameraScanner.setPreviewSize(width, height);
//        mCameraScanner.setFrameRect(mScannerFrameView.getLeft(), mScannerFrameView.getTop(), mScannerFrameView.getRight(), mScannerFrameView.getBottom());
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return true;
    }

    @Override// 每有一帧画面，都会回调一次此方法
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    @Override
    public void openCameraSuccess(int frameWidth, int frameHeight, int frameDegree) {
        mTextureView.setImageFrameMatrix(frameWidth, frameHeight, frameDegree);
        if (mGraphicDecoder == null) {
            // mGraphicDecoder = new DebugZBarDecoder(this, mCodeType);//使用带参构造方法可指定条码识别的格式
            mGraphicDecoder = new ZBarDecoder(this);
        }
        //该区域坐标为相对于父容器的左上角顶点。
        //TODO 应考虑TextureView与ScannerFrameView的Margin与padding的情况
        mCameraScanner.setFrameRect(mScannerFrameView.getLeft(), mScannerFrameView.getTop(), mScannerFrameView.getRight(), mScannerFrameView.getBottom());
        mCameraScanner.setGraphicDecoder(mGraphicDecoder);
    }

    @Override
    public void openCameraError() {
        Toast.makeText(this, "出错了", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void noCameraPermission() {

        /**
         * 申请权限
         */
        int checkResult1 = getApplicationContext().checkCallingOrSelfPermission(Manifest.permission.CAMERA);
        //if(!=允许),抛出异常
        if (checkResult1 != PackageManager.PERMISSION_GRANTED) {

            new AlertDialog.Builder(this).setTitle("权限申请")
                    .setMessage("如果没有相机权限，二维码扫描是不能工作的!")
                    .setCancelable(false)
                    .setNeutralButton("拒绝", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    })
                    .setPositiveButton("授权", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                requestPermissions(new String[]{Manifest.permission.CAMERA}, 1); // 动态申请读取权限
                            }
                        }
                    })
                    .show();

        }

    }

    @Override
    public void cameraDisconnected() {
        Toast.makeText(this, "断开连接", Toast.LENGTH_SHORT).show();

    }


    @Override
    public void cameraBrightnessChanged(int brightness) {

    }

    int mCount = 0;
    String mResult = null;

    void ToResult(String data) {

        if (data != null) {
            String result = data;
            Intent intent = new Intent(this, ScanResultActivity.class);
            intent.putExtra("result", result);
            startActivity(intent);
        }
    }


    @Override
    public void decodeComplete(String result, int type, int quality, int requestCode) {
        if (result == null) return;
        if (result.equals(mResult)) {
            if (!resdia.isShowing()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        vibrator.vibrate(100);
                    }
                }).start();

                resdia.show();

                Window window = resdia.getWindow();//对话框窗口
                window.setGravity(Gravity.BOTTOM);//设置对话框显示在屏幕中间
                window.setWindowAnimations(R.style.dialog_style_bottom);//添加动画

            }
            if (++mCount > 1) {//连续四次相同则显示结果（主要过滤脏数据，也可以根据条码类型自定义规则）
                if (quality < 10) {
                    result_end = result;
                    resdia.setMessage(result);
                } else if (quality < 100) {
                    result_end = result;
                    resdia.setMessage(result);
                } else {
                    result_end = result;
                    resdia.setMessage(result);
                }
            }
        } else {
            mCount = 1;
            mResult = result;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn_flash: {
                if (v.isSelected()) {
                    ((ImageButton) v).setImageDrawable(getDrawable(R.drawable.ic_flash_on_black_50dp));
                    v.setSelected(false);
                    mCameraScanner.closeFlash();
                } else {
                    ((ImageButton) v).setImageDrawable(getDrawable(R.drawable.ic_flash_off_black_50dp));
                    v.setSelected(true);
                    mCameraScanner.openFlash();
                }
                break;
            }
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


}
