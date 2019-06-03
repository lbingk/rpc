package com.rpc.rpcdemo.consumerserver;

import com.rpc.rpcdemo.ApplicationContextHolder;
import com.rpc.rpcdemo.beandefinition.SubscribeZKDefination;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;


public class ConsumerRpcInvokerHandler {
    // 日志
    private static final Log logger = LogFactory.getLog(ConsumerRpcInvokerHandler.class);

    // xml配置的注册中心的暴露信息
    private static SubscribeZKDefination subscribeZKDefination = null;
    // 获取自定义的容器装载类
    public static ApplicationContextHolder applicationContextHolder = ApplicationContextHolder.getInstance();
    // 获取本机IP地址
    public static String serviceIP = null;


    // 与zk注册中心的连接，订阅服务
    public static void subscribe(ApplicationContext ctx) {
        // 装载容器
        applicationContextHolder.setApplicationContext(ctx);
        doSubscribe();
    }

    private static void doSubscribe() {
        // 从容器中获取暴露的通信信息
        try {
            subscribeZKDefination = applicationContextHolder.getBean(SubscribeZKDefination.class);
        } catch (RuntimeException e) {
            logger.info("没有配置通信信息");
        }
       // 与注册中心建立通信
//       new ConsumerSubscribeServer(subscribeZKDefination);

    }
}
