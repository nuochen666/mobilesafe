package com.nuochen.b.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Administrator on 2015/8/15.
 * @return string 返回的字符串
 * @throws IOException
 */
public class StreamTools {
    public static String readStreamString(InputStream is)throws IOException{
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        byte[] buffer=new byte[1024];
        int len=0;
        while((len=is.read(buffer))!=-1){
            baos.write(buffer,0,len);
        }
        is.close();
        String results=baos.toString();
        baos.close();
        return results;
    }

}
