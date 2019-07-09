package com.dabai.qrtools;

import android.graphics.Bitmap;

import com.qingmei2.rximagepicker.entity.observeras.AsBitmap;
import com.qingmei2.rximagepicker.entity.observeras.AsFile;
import com.qingmei2.rximagepicker.entity.sources.Camera;
import com.qingmei2.rximagepicker.entity.sources.Gallery;

import java.io.File;

import io.reactivex.Observable;

public interface MyImagePicker {

    @Gallery    //打开相册选择图片
    @AsFile
        //返回值为File类型
    Observable<File> openGallery();


    @Camera    //打开相机拍照
    @AsBitmap
        //返回值为Bitmap类型
    Observable<Bitmap> openCamera();
}