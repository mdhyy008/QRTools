package com.dabai.qrtools;


import android.util.Log;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;


/*
 网络Html工具类
 大白2017
 */
public class HtmlUtils
{


    //获取标题 所需组件
    private static URL url;
    private static String dataz;
    private static char[] data;
    private static BufferedReader br;
    //返回结果
    List<String> res;
    //记录
    List<String> links;



    //获取批量中间的string   所需组件
    List<String> subtextres;
    StringBuffer subtext;



    //构造方法
    public HtmlUtils(){
    }


    //获取网站
    public String getHtml(String link) throws Exception{

        subtext = new StringBuffer();

        URL suburl = new URL(link);

        HttpURLConnection huc = (HttpURLConnection) suburl.openConnection();
        System.out.println("打开连接..."+huc.getResponseMessage());
        InputStream is = huc.getInputStream();
        int len = 0;

        byte data[]=new byte[1024];
        System.out.println("开始读取");
        while((len = is.read(data)) != -1){

            subtext.append(new String(data,0,len));
            //System.out.println("正在获取数据..."+len);
        }

        System.out.println("读取完毕");

        String tmp = subtext.toString();

        return tmp;
    }




    //获取网站
    public String getHtml(String link,String charset) throws Exception{

        subtext = new StringBuffer();

        URL suburl = new URL(link);

        HttpURLConnection huc = (HttpURLConnection) suburl.openConnection();
        System.out.println("打开连接..."+huc.getResponseMessage());
        InputStream is = huc.getInputStream();
        int len = 0;

        byte data[]=new byte[1024];
        System.out.println("开始读取");
        while((len = is.read(data)) != -1){

            subtext.append(new String(data,0,len,charset));
            //System.out.println("正在获取数据..."+len);
        }
        System.out.println("读取完毕");
        String tmp = subtext.toString();

        return tmp;
    }



    //获取网站指定 前文本和后文本 批量中间的string

    public List<String> getHtmlSubText(String link,String start,String end) throws Exception{
        subtextres = new ArrayList<>();
        subtext = new StringBuffer();

        URL suburl = new URL(link);

        br = new BufferedReader(new InputStreamReader(suburl.openStream()));
        data = new char[1024];

        while (br.read(data) != -1)
        {

            dataz = String.valueOf(data);
            subtext.append(dataz);

        }
        br.close();

        //System.out.println(subtext);

        Pattern p = Pattern.compile(start+"(.*?)"+end);
        Matcher m = p.matcher(subtext.toString());
        while(m.find()) {
            String subt = m.group(1);//m.group(1)不包括这两个字符

            subtextres.add(subt);
            //Log.d("dabai", "getHtmlSubText: "+subt);
        }



        return subtextres;
    }




    //获取网站title    返回 List
    public List<String> getHtmlTitle(String ...a) throws Exception
    {
        links = new ArrayList<>();
        res = new ArrayList<>();

        for (String linkz : a)
        {
            links.add(linkz);

            url = new URL(linkz);
            br = new BufferedReader(new InputStreamReader(url.openStream()));
            data = new char[1024];

            while (br.read(data) != -1)
            {

                dataz = String.valueOf(data);
                //System.out.println(dataz);

                if (dataz.contains("</title>"))
                {
                    res.add(dataz.substring(dataz.indexOf("<title>") + 7, dataz.indexOf("</title>")));

                    break;
                }
            }
            br.close();
        }
        return res;
    }
}
