package com.rpc.rpcdemo;

import com.rpc.rpcdemo.beandefinition.RegisterServiceDefination2ZK;
import com.rpc.rpcdemo.beandefinition.RegisterZKDefination;
import com.rpc.rpcdemo.beandefinition.RpcZkContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.context.ApplicationContext;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class AcceptRegisterZkHandler {
    private static final Log logger = LogFactory.getLog(AcceptRegisterZkHandler.class);
    // 自定义上下文
    private static RpcZkContext rpcZkContext = RpcZkContext.getInstance();
    // xml配置的注册中心的暴露信息
    private static RegisterZKDefination registerZKDefination = null;
    // 获取自定义的容器装载类
    public static ApplicationContextHolder applicationContextHolder = ApplicationContextHolder.getInstance();

    // 从阻塞队列里获取注册消息，放进容器ConcurrentHashMap

    public static void getRegisteServiceIntoMap() {
        LinkedBlockingQueue<StringBuilder> linkedBlockingQueue = rpcZkContext.getLinkedBlockingQueue();
        ConcurrentHashMap<String, RegisterServiceDefination2ZK> registeServiceMap = rpcZkContext.getRegisterServiceMap();
        StringBuilder sb = linkedBlockingQueue.poll();
        // 死循环执行
        while (true) {
            logger.info("11");
            doRegisteServiceIntoMap(registeServiceMap, sb);
            logger.info("22");
        }
    }

    private static void doRegisteServiceIntoMap(ConcurrentHashMap<String, RegisterServiceDefination2ZK> registeServiceMap, StringBuilder sb) {
        // 判断是否为空
        if (null != sb) {
            // 当取出的消息不为空的时候，则代表需要注册到map里面
            // 1.采用 JSONObject 来解析取出的内容
            try {
                JSONObject jsonObject = new JSONObject(sb.toString());
                JSONObject serviceJson = jsonObject.getJSONObject("RegisterServiceDefination2ZK");
                String ip = serviceJson.getString("ip");
                int port = serviceJson.getInt("port");
                Object interfaceImplClass = serviceJson.get("interfaceImplClass");
                JSONArray interfaceClass = serviceJson.getJSONArray("interfaceClass");
                RegisterServiceDefination2ZK RegisterServiceDefination2ZK = new RegisterServiceDefination2ZK();
                RegisterServiceDefination2ZK.setIp(ip);
                RegisterServiceDefination2ZK.setPort(port);
                RegisterServiceDefination2ZK.setInterfaceImplClass(interfaceImplClass.getClass());

                for (int i = 0; i < interfaceClass.length(); i++) {
                    String interfaceClassString = interfaceClass.getString(i);
                    // 放进map,以接口名为key
                    registeServiceMap.put(interfaceClassString, RegisterServiceDefination2ZK);
                }
            } catch (JSONException e) {
                logger.info("解析注册消息出错!!!");
            }
        }
    }

    /*
      提供者接收注册服务
    */
    public static void acceptRegisteBeanProcess(ApplicationContext ctx) throws IOException {
        // 获取spring的容器
        applicationContextHolder.setApplicationContext(ctx);
        // 从容器获取注册中心的信息
        try {
            registerZKDefination = applicationContextHolder.getBean(RegisterZKDefination.class);
        } catch (RuntimeException e) {
            logger.info("没有配置注册中心信息");
            throw new IOException("没有配置注册中心信息");
        }
        // 启动注册中心的接收注册逻辑
        new Thread(new AcceptRegisterServer(registerZKDefination.getPort(), registerZKDefination.getTimeout(), rpcZkContext.getLinkedBlockingQueue())).start();
        // 启动处理接收注册消息的逻辑
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                getRegisteServiceIntoMap();
//            }
//        });
    }

}
