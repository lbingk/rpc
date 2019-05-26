package com.rpc.rpcdemo;

import com.google.common.collect.Lists;
import com.rpc.rpcdemo.annotation.RpcInvokerService;
import com.rpc.rpcdemo.beandefinition.RegisterServiceDefination;
import com.rpc.rpcdemo.beandefinition.RegisterServiceImplDefination;
import com.rpc.rpcdemo.beandefinition.RegisterZKDefination;
import com.rpc.rpcdemo.constant.DataConstant;
import com.rpc.rpcdemo.util.SerializeUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RegisterRpcInvokerHandler {
    // 日志
    private static final Log logger = LogFactory.getLog(RegisterRpcInvokerHandler.class);
    // 注册状态标志位（初始化为注册失败，以便启动时开始注册）
    private static volatile Integer registerFlag = DataConstant.REGISTE_FAIL;
    // xml配置的注册中心的暴露信息
    private static RegisterZKDefination registerZKDefination = null;
    // xml配置的提供者的暴露信息
    private static RegisterServiceDefination registerServiceDefination = null;
    // 获取自定义的容器装载类
    public static ApplicationContextHolder applicationContextHolder = ApplicationContextHolder.getInstance();
    // 获取本机IP地址
    public static String serviceIP = null;


    public static void register(ApplicationContext ctx) throws UnknownHostException {
        // 装载容器
        ApplicationContextHolder.setApplicationContext(ctx);
        doRegister();
        // 另起一线程来注册服务，不影响其他逻辑的运行
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    doRegister();
//                } catch (UnknownHostException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
    }

    private static void doRegister() throws UnknownHostException {
        // 从容器获取暴露的信息
        try {
            registerServiceDefination = applicationContextHolder.getBean(RegisterServiceDefination.class);
        } catch (RuntimeException e) {
            logger.info("没有配置服务暴露信息");
        }
        // 从容器获取注册中心的信息
        try {
            registerZKDefination = applicationContextHolder.getBean(RegisterZKDefination.class);
        } catch (RuntimeException e) {
            logger.info("没有配置注册中心信息");
        }
        List<Object> registerServeImplList = new ArrayList<>();
        Map<String, Object> beansWithAnnotation = applicationContextHolder.getBeansWithAnnotation(RpcInvokerService.class);
        for (Map.Entry<String, Object> entry : beansWithAnnotation.entrySet()) {
            registerServeImplList.add(entry.getValue());
        }
        // 获取本机IP地址
        serviceIP = InetAddress.getLocalHost().toString();
        // 注册服务
        while (true) {
            registerInfo(registerServiceDefination, registerZKDefination, registerServeImplList);
        }
    }

    private static void registerInfo(RegisterServiceDefination registerServiceDefination,
                                     RegisterZKDefination registerZKDefination,
                                     List<Object> registerServiceImplList) throws UnknownHostException {

        // 定义以客户端通道
        SocketChannel socketChannel = null;
        // 定义缓冲区，用来接收客户端的信息
        ByteBuffer allocate = ByteBuffer.allocate(4048);
        try {
            socketChannel = SocketChannel.open();
            // 定义以注册中心的IP以及端口号
            InetSocketAddress inetSocketAddress = new InetSocketAddress(registerZKDefination.getIp(), registerZKDefination.getPort());
            // 客户端连接注册中心
            if (registerFlag == DataConstant.REGISTE_FAIL) {
                logger.info("正在获取注册中心的连接信息....");
            }
            socketChannel.connect(inetSocketAddress);
            // 死循环
            while (true) {
                // 连接通道
                if (registerFlag == DataConstant.REGISTE_SUCCESS) {
                    // 在判定已经通道依然存在以及注册成功的情况下，证明不需要重新注册
                    continue;
                }
                // 需要重新注册
                registerFlag = DataConstant.REGISTE_ING;

                // 循环获取暴露的服务信息
                for (Object serviceIml : registerServiceImplList) {

                    RegisterServiceImplDefination registerService = new RegisterServiceImplDefination();
                    registerService.setIp(serviceIP);
                    registerService.setPort(String.valueOf(registerServiceDefination.getPort()));
                    registerService.setInterfaceImplClassName(serviceIml.getClass().toString());

                    List<String> interfaceNameList = Lists.newArrayList();
                    Class<?>[] interfaces = serviceIml.getClass().getInterfaces();
                    for (Class<?> aClass : interfaces) {
                        Method[] methods = interfaces.getClass().getMethods();
                        interfaceNameList.add(aClass.toString());
                    }
                    registerService.setInterfaceClassNameList(interfaceNameList);

                    List<String> methodNameList = Lists.newArrayList();
                    Method[] methods = registerService.getClass().getMethods();
                    for (Method method : methods) {
                        String methodName = method.toString();
                        methodNameList.add(methodName);
                    }
                    registerService.setMethodNameList(methodNameList);

                    String s = SerializeUtil.serializeToString(registerService);

                    allocate.put(s.getBytes());
                    // 复位
                    allocate.flip();
                    // 写出数据
                    socketChannel.write(allocate);
                    logger.info("注册服务：" + registerService.toString());
                    // 清空
                    allocate.clear();

                }
                // 全部成功发送注册消息
                registerFlag = DataConstant.REGISTE_SUCCESS;
                logger.info("注册服务成功");
                // 全部成功后关掉连接，不需要关闭通道 ！！！！
                socketChannel.close();
            }
        } catch (IOException e) {
            registerFlag = DataConstant.REGISTE_FAIL;
            logger.info("获取注册中心的连接信息失败，正重新建立连接...");
        }
    }
}
