package com.rpc.rpcdemo;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rpc.rpcdemo.annotation.RpcInvokerService;
import com.rpc.rpcdemo.beandefinition.*;
import com.rpc.rpcdemo.constant.DataConstant;
import com.rpc.rpcdemo.util.SerializeUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

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

    // TOOD:假设只有一个暴露的接口实现只有一个
    private static Map<Class<?>, Object> registerServiceImplMap = Maps.newHashMap();

    // 获取本机IP地址
    public static String serviceIP = null;

    /*多路复用器*/
    public static Selector selector;

    /*标识数字*/
    private static int flag = 0;

    /*缓冲区大小*/
    private static int BLOCK = 4096;

    /*接受数据缓冲区*/
    private static ByteBuffer sendbuffer = ByteBuffer.allocate(BLOCK);

    /*发送数据缓冲区*/
    private static ByteBuffer receivebuffer = ByteBuffer.allocate(BLOCK);


    /* TODO:全局变量储存解析结果，存在并发的不安全问题*/
    public static InvokerBeanDefinition analysisObj = null;


    public static void startup(ApplicationContext ctx) throws UnknownHostException {
        // 装载容器
        ApplicationContextHolder.setApplicationContext(ctx);
        registerServiceDefination = applicationContextHolder.getBean(RegisterServiceDefination.class);
        // 异步注册服务
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    doRegister();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        // 异步提供服务
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    doProvide();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static void doProvide() throws IOException {
        // 打开服务器套接字通道
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        // 服务器配置为非阻塞
        serverSocketChannel.configureBlocking(false);
        // 检索与此通道关联的服务器套接字
        ServerSocket serverSocket = serverSocketChannel.socket();
        // 进行服务的绑定
        serverSocket.bind(new InetSocketAddress(registerServiceDefination.getPort()));
        // 通过open()方法找到Selector
        selector = Selector.open();
        // 注册到selector，等待连接
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        // 监听
        while (true) {
            // 选择一组键，并且相应的通道已经打开
            try {
                selector.select();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 返回此选择器的已选择键集。
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                iterator.remove();
                try {
                    // 接受请求
                    ServerSocketChannel server = null;
                    SocketChannel client = null;
                    String receiveText;
                    String sendText;

                    int count = 0;
                    // 测试此键的通道是否已准备好接受新的套接字连接。
                    if (selectionKey.isAcceptable()) {
                        // 返回为之创建此键的通道。
                        server = (ServerSocketChannel) selectionKey.channel();
                        // 接受到此通道套接字的连接。
                        // 此方法返回的套接字通道（如果有）将处于阻塞模式。
                        client = server.accept();
                        // 配置为非阻塞
                        client.configureBlocking(false);
                        // 注册到selector，等待连接
                        client.register(selector, SelectionKey.OP_READ);
                    } else if (selectionKey.isReadable()) {
                        // 返回为之创建此键的通道。
                        client = (SocketChannel) selectionKey.channel();
                        //将缓冲区清空以备下次读取
                        receivebuffer.clear();
                        //读取服务器发送来的数据到缓冲区中
                        count = client.read(receivebuffer);
                        if (count > 0) {
                            System.out.println("接收到消费者： " + client.getRemoteAddress() + " 的消息");
                            // 解析收到的消息
                            receiveText = new String(receivebuffer.array(), 0, count);
                            analysisObj = (InvokerBeanDefinition) SerializeUtil.deserializeToObject(receiveText);
                            System.out.println("接收到消费者： " + client.getRemoteAddress() + " 的消息: " + analysisObj.toString() + " ......");

                            // 存储消息，注册到链表有界队列里面
                        } else {
                            System.out.println("注册中心接收订阅中： " + client.getRemoteAddress() + " 的注册消息: " + analysisObj.toString() + " ......");
                            // 设置监控状态
                            client.register(selector, SelectionKey.OP_WRITE);
                        }
                    } else if (selectionKey.isWritable()) {
                        //将缓冲区清空以备下次写入
                        sendbuffer.clear();
                        // 返回为之创建此键的通道。
                        client = (SocketChannel) selectionKey.channel();
                        // 反射调用消费者需要调用的方法，返回结果
                        String sendStr = doInvoker();
                        //向缓冲区中输入数据
                        sendbuffer.put(sendStr.getBytes());
                        //将缓冲区各标志复位,因为向里面put了数据标志被改变要想从中读取数据发向服务器,就要复位
                        sendbuffer.flip();
                        //输出到通道
                        client.write(sendbuffer);
                        System.out.println("注册中心向消费者提供的地址列表信息--：" + sendbuffer);
                        client.register(selector, SelectionKey.OP_READ);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static String doInvoker() {
        // 解析
        Class<?> serviceClass = analysisObj.getServiceClass();
        Method methodName = analysisObj.getMethodName();
        Object[] paramaters = analysisObj.getParamaters();
        // 执行结果
        Object invoke = null;
        String invokeStr = "";
        // 反射调用结果
        Object registerServiceImpl = registerServiceImplMap.get(serviceClass);
        // TODO: 假设一定存在提供者
        try {
            invoke = methodName.invoke(registerServiceImpl, paramaters);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        // 序列化数据
        if (invoke != null) {
            invokeStr = SerializeUtil.serializeToString(invoke);
        }
        return invokeStr;
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

            // 假设只有一个暴露的接口实现只有一个
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
            // 死循环
            socketChannel.connect(inetSocketAddress);
            while (true) {
                // 连接通道
                if (registerFlag == DataConstant.REGISTE_SUCCESS) {
                    // 在判定已经通道依然存在以及注册成功的情况下，
                    socketChannel.close();
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

                        // 存储服务到Map
                        registerServiceImplMap.put(aClass, serviceIml);
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
