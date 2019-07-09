package com.dabai.qrtools;

import java.net.*;
import java.io.*;



/**
 下载工具类 1
 大白2017

 缺点: 单线程,阻塞式
 **/


public class DownloadUtils
{

    DownloadUtils mDu;
    URL mUrl;
    boolean isConnection;
    private HttpURLConnection mHuc;

    private int mTimeOut;
    File mFile;

    private InputStream mIs;

    double mContentLength;
    double th;

    public DownloadUtils()
    {
        //构造this;
        this.mDu = this;
    }


    public DownloadUtils setDownload_NetPath(String url) throws MalformedURLException
    {
        //设置URL地址
        mUrl = new URL(url);
        return this.mDu;
    }

    public String getDownload_NetPath()
    {
        try
        {
            return mUrl.toString();
        }
        catch (Exception e)
        {
            return "异常:" + e;
        }
    }

    public DownloadUtils openConnection()
    {
        //打开连接(阻塞)
        try
        {
            mHuc = (HttpURLConnection)this.mUrl.openConnection();
            isConnection = true;

        }
        catch (IOException e)
        {
            isConnection = false;

        }
        return this.mDu;
    }
    public boolean getIsConnection()
    {
        return isConnection;
    }

    public void setConnectTimeout(int timeout)
    {
        //连接超时
        this.mTimeOut = timeout;
        mHuc.setConnectTimeout(this.mTimeOut);
    }

    public DownloadUtils setDownload_SDPath(String path)
    {
        //设置本地路径
        File file = new File(path);
        this.mFile = file;
        return this.mDu;
    }
    public File getDownload_SDFile()
    {
        return mFile;
    }
    public boolean ReadAndWrite() throws IOException
    {
        th = 0;
        //读取并写入(阻塞)   应该走单独线程
        mIs = mHuc.getInputStream();
        mContentLength = mHuc.getContentLength();

        byte data[] = new byte[1024];
        int len = 0;
        double lensize = 0;
        //System.out.println("指定文件名(在/sdcard/Download):");

        FileOutputStream fos = new FileOutputStream(mFile);

        th = 0;


        //System.out.println("正在读取并写入文件字节...");

        while ((len = mIs.read(data)) != -1)
        {
            fos.write(data, 0, len);
            lensize += len;
            th = Math.ceil((lensize / mContentLength) * 100);

        }


        if (mFile.exists() && mFile.length() == mContentLength)
        {
            return true;
        }
        else
        {
            return false;
        }


    }

    public double getFileLength(int i)
    {
        //获取目标文件长度
        switch (i)
        {
            case 1:
                return mContentLength;
            case 2:
                return mContentLength / 1024;
            case 3:
                return mContentLength / 1024 / 1024;
            case 4:
                return mContentLength / 1024 / 1024 / 1024;
        }
        return mContentLength;
    }
    public double getFileLength()
    {
        //获取目标文件长度
        return mContentLength;
    }
    public int getTh()
    {
        //获取已下载百分比
        return (int)th;
    }
}
