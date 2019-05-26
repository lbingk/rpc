package com.rpc.rpcdemo.util;


import com.rpc.rpcdemo.constant.DataConstant;

import java.io.*;

public class SerializeUtil {
    //序列化
    public static String serializeToString(Object obj) {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream objOut = null;
        String str = null;
        try {
            objOut = new ObjectOutputStream(byteOut);
            objOut.writeObject(obj);
            str = byteOut.toString(DataConstant.ISO_8859_1);//此处只能是ISO-8859-1,但是不会影响中文使用
        } catch (IOException e) {
            e.printStackTrace();
        }

        return str;
    }

    //反序列化
    public static Object deserializeToObject(String str) {
        ByteArrayInputStream byteIn = null;
        Object obj = null;
        try {
            byteIn = new ByteArrayInputStream(str.getBytes(DataConstant.ISO_8859_1));
            ObjectInputStream objIn = new ObjectInputStream(byteIn);
            obj = objIn.readObject();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return obj;
    }
}
