package com.rpc.rpcdemo.util;

import com.rpc.rpcdemo.beandefinition.RegisteServiceDefination2ZK;
import com.rpc.rpcdemo.beandefinition.RegisteZKDefination;
import com.rpc.rpcdemo.beandefinition.RpcZkContext;
import com.rpc.rpcdemo.constant.DataConstant;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class AcceptRegistesZKUtils {
    private static final Log logger = LogFactory.getLog(RegisteRpcInvokerUtils.class);

    private static RpcZkContext rpcZkContext = RpcZkContext.getInstance();

    // 从阻塞队列里获取注册消息，放进容器ConcurrentHashMap
    @PostConstruct
    public static void getRegisteServiceIntoMap() {
        LinkedBlockingQueue<StringBuilder> linkedBlockingQueue = rpcZkContext.getLinkedBlockingQueue();
        ConcurrentHashMap<String, RegisteServiceDefination2ZK> registeServiceMap = rpcZkContext.getRegisteServiceMap();
        StringBuilder sb = linkedBlockingQueue.poll();
        // 死循环执行
        doRegisteServiceIntoMap(registeServiceMap, sb);
    }

    private static void doRegisteServiceIntoMap(ConcurrentHashMap<String, RegisteServiceDefination2ZK> registeServiceMap, StringBuilder sb) {
        do {
            // 判断是否为空
            if (null != sb) {
                // 当取出的消息不为空的时候，则代表需要注册到map里面
                // 1.采用 JSONObject 来解析取出的内容
                try {
                    JSONObject jsonObject = new JSONObject(sb.toString());
                    JSONObject serviceJson = jsonObject.getJSONObject("registeServiceDefination2ZK");
                    String ip = serviceJson.getString("ip");
                    int port = serviceJson.getInt("port");
                    Object interfaceImplClass = serviceJson.get("interfaceImplClass");
                    JSONArray interfaceClass = serviceJson.getJSONArray("interfaceClass");
                    RegisteServiceDefination2ZK registeServiceDefination2ZK = new RegisteServiceDefination2ZK();
                    registeServiceDefination2ZK.setIp(ip);
                    registeServiceDefination2ZK.setPort(port);
                    registeServiceDefination2ZK.setInterfaceImplClass(interfaceImplClass.getClass());

                    for (int i = 0; i < interfaceClass.length(); i++) {
                        String interfaceClassString = interfaceClass.getString(i);
                        // 放进map,以接口名为key
                        registeServiceMap.put(interfaceClassString, registeServiceDefination2ZK);
                    }
                } catch (JSONException e) {
                    logger.info("解析注册消息出错!!!");
                }
            }
        } while (true);
    }

    /*
      提供者接收注册服务
    */
    public static void acceptRegisteBeanProcess() throws IOException {
        doAcceptRegister();
    }

    private static void doAcceptRegister() throws IOException {
        // 获取注册中心IP与端口
        RegisteZKDefination registeZKDefination = null;
        try {
            registeZKDefination = ApplicationContextHolder.getBean(RegisteZKDefination.class);
        } catch (RuntimeException e) {
            throw new RuntimeException("无法找到注册中心信息");
        }

        doCreateSocket(registeZKDefination);
    }

    private static void doCreateSocket(RegisteZKDefination registeZKDefination) throws IOException {
        // 创建服务端Socket
        ServerSocket serverSocket = new ServerSocket(registeZKDefination.getPort());
        // 多线程启动，多个客户端连接时，需要并发处理
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(100);
        while (true) {
            Socket accept = serverSocket.accept();
            Runnable runnable = () -> {
                try {
                    accept.setSoTimeout(registeZKDefination.getTimeout());
                    // 接收消息
                    InputStream inputStream = accept.getInputStream();
                    byte[] bytes = new byte[1024];
                    int len;
                    StringBuilder sb = new StringBuilder();
                    // 客户端关闭输出流时，才能取得结尾的-1
                    while ((len = inputStream.read(bytes)) != -1) {
                        sb.append(new String(bytes, 0, len, DataConstant.UTF8));
                        // 封装注册服务的上下文记录注册服务
                        doRegisterIntoContext(sb);
                        logger.info("接收消息 : {}" + sb);
                    }
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };
            fixedThreadPool.submit(runnable);
        }
    }

    private static void doRegisterIntoContext(StringBuilder sb) {
        do {
            try {
                rpcZkContext.getLinkedBlockingQueue().put(sb);
                break;
            } catch (InterruptedException e) {
                // 当阻塞队列已满时则代表需要等待处理队列
                logger.info("正在处理注册消息 : {}" + sb);
            }
        } while (true);

    }
}
