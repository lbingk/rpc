package com.rpc.rpcdemo.acceptserver;

import com.google.common.collect.Lists;
import com.rpc.rpcdemo.ApplicationContextHolder;
import com.rpc.rpcdemo.beandefinition.InvokerMachineSocketDefination;
import com.rpc.rpcdemo.beandefinition.RegisterServiceImplDefination;
import com.rpc.rpcdemo.beandefinition.RegisterZKDefination;
import com.rpc.rpcdemo.beandefinition.RpcZkContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class AcceptHandler {
    private static final Log logger = LogFactory.getLog(AcceptHandler.class);
    // 自定义上下文
    private static RpcZkContext rpcZkContext = RpcZkContext.getInstance();
    // xml配置的注册中心的暴露信息
    private static RegisterZKDefination registerZKDefination = null;
    // 获取自定义的容器装载类
    public static ApplicationContextHolder applicationContextHolder = ApplicationContextHolder.getInstance();

    // 提供者接收注册服务
    public static void startup(ApplicationContext ctx) throws IOException {
        // 获取spring的容器
        applicationContextHolder.setApplicationContext(ctx);
        // 从容器获取注册中心的信息
        try {
            registerZKDefination = applicationContextHolder.getBean(RegisterZKDefination.class);
        } catch (RuntimeException e) {
            logger.info("没有配置注册中心信息");
            throw new IOException("没有配置注册中心信息");
        }

        // 启动注册中心的接收注册逻辑，启动处理与消费者的订阅（地址的推送）
        new Thread(new NIOServer(registerZKDefination.getPort(), registerZKDefination.getTimeout())).start();

        // 启动处理接收注册消息的逻辑，可以在此方法上加上持久化文件于磁盘
        new Thread(new Runnable() {
            @Override
            public void run() {
                getRegisteServiceIntoMap();
            }
        }).start();
    }


    // 死循环执行:从阻塞队列里获取注册消息，放进容器ConcurrentHashMap
    public static void getRegisteServiceIntoMap() {
        LinkedBlockingQueue<RegisterServiceImplDefination> linkedBlockingQueue = rpcZkContext.getLinkedBlockingQueue();
        ConcurrentHashMap<String, List<RegisterServiceImplDefination>> registeServiceMap = rpcZkContext.getRegisterServiceMap();
        ConcurrentHashMap<String, List<InvokerMachineSocketDefination>> registerServiceAddressMap = rpcZkContext.getRegisterServiceAddressMap();
        // 死循环执行
        while (true) {
            RegisterServiceImplDefination poll = linkedBlockingQueue.poll();
            doRegisteServiceIntoMap(registeServiceMap, registerServiceAddressMap, poll);
        }
    }

    // 从阻塞队列里获取注册消息，放进容器ConcurrentHashMap
    private static void doRegisteServiceIntoMap(ConcurrentHashMap<String, List<RegisterServiceImplDefination>> registeServiceMap,
                                                ConcurrentHashMap<String, List<InvokerMachineSocketDefination>> registerServiceAddressMap,
                                                RegisterServiceImplDefination poll) {
        // 判断是否为空
        if (null != poll) {
            // 当取出的消息不为空的时候，则代表需要注册到map里面
            // 1.采用 JSONObject 来解析取出的内容
            List<String> interfaceClassNameList = poll.getInterfaceClassNameList();
            for (String aClassName : interfaceClassNameList) {
                String interfaceClassString = aClassName;

                // 放进map,以接口名为key
                List<RegisterServiceImplDefination> registerServiceImplDefinations = registeServiceMap.get(interfaceClassString);

                InvokerMachineSocketDefination invokerMachineSocketDefination = new InvokerMachineSocketDefination();
                invokerMachineSocketDefination.setIp(poll.getIp());
                invokerMachineSocketDefination.setPort(Integer.valueOf(poll.getPort()));

                if (null != registerServiceImplDefinations) {
                    // 证明之前已经有其他地址进来了
                    registerServiceImplDefinations.add(poll);

                    // 放进map,以接口名为key,地址信息
                    registerServiceAddressMap.get(interfaceClassString).add(invokerMachineSocketDefination);

                } else {

                    registeServiceMap.put(interfaceClassString, Lists.newArrayList(poll));
                    registerServiceAddressMap.put(interfaceClassString, Lists.newArrayList(invokerMachineSocketDefination));
                }

                logger.info("注册中心已经处理注册信息：" + interfaceClassString);
            }
        }
    }

}
