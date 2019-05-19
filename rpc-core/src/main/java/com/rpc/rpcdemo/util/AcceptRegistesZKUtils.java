package com.rpc.rpcdemo.util;

import com.rpc.rpcdemo.annotation.RpcZkExport;
import com.rpc.rpcdemo.constant.DataConstant;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class AcceptRegistesZKUtils {
    private static final Log logger = LogFactory.getLog(RegisteRpcInvokerUtils.class);

    /*
      提供者接收注册服务
    */
    public static void acceptRegisteBeanProcess(Class<?> startUpClass) throws IOException {
        doAcceptRegiste(startUpClass);
    }

    // 获取指定的包路径，以便扫描路径下 RpcZkExport
    private static void doAcceptRegiste(Class<?> startUpClass) throws IOException {
        // 获取启动类的注解：RpcZkExport
        RpcZkExport rpcZkExport = doGetRpcZkExport(startUpClass);
        // 创建服务端Socket
        ServerSocket serverSocket = new ServerSocket(rpcZkExport.port());
        Socket accept = serverSocket.accept();
        // 接收消息
        InputStream inputStream = accept.getInputStream();
        byte[] bytes = new byte[1024];
        int len;
        StringBuilder sb = new StringBuilder();
        // 客户端关闭输出流时，才能取得结尾的-1
        while ((len = inputStream.read(bytes)) != -1) {
            sb.append(new String(bytes, 0, len, DataConstant.UTF8));
            logger.info("accecpt : {}" + sb);
        }
    }

    private static RpcZkExport doGetRpcZkExport(Class<?> startUpClass) {
        // 获取启动类的注解：RpcZkExport
        RpcZkExport annotation = startUpClass.getAnnotation(RpcZkExport.class);
        if (null == annotation) {
            throw new IllegalArgumentException("@Interface:RpcZkExport must not null !!");
        }
        if (annotation.port() <= 0 || annotation.port() > 65535) {
            throw new IllegalArgumentException("Invalid port " + annotation.port());
        }
        return annotation;
    }

}
