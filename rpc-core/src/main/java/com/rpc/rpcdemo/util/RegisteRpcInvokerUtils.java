package com.rpc.rpcdemo.util;

import com.rpc.rpcdemo.annotation.RpcProviderExport;
import com.rpc.rpcdemo.annotation.RpcInvokerService;
import com.rpc.rpcdemo.annotation.RpcPakageComponentScan;
import com.rpc.rpcdemo.annotation.RpcZkExport;
import com.rpc.rpcdemo.beandefinition.RegisteServiceImplDefination;
import com.rpc.rpcdemo.constant.DataConstant;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.reflections.Reflections;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Iterator;
import java.util.Set;

public class RegisteRpcInvokerUtils {

    private static final Log logger = LogFactory.getLog(RegisteRpcInvokerUtils.class);

    /*
       提供者注册服务
     */
    public static void registeBeanProcess(Class<?> startUpClass) throws Throwable {
        doRegiste(startUpClass);
    }

    // 获取指定的包路径，以便扫描路径下所有被RpcInvokerService注解的实现类
    private static String getPackageScan(Class<?> startUpClass) {
        // 获取启动类的注解：RpcPakageComponentScan
        RpcPakageComponentScan annotation = startUpClass.getAnnotation(RpcPakageComponentScan.class);

        String packageName;
        // 当直接为空或者注解没有设定值时，则代表扫描当前包以及以下的包路径
        if (null == annotation || "".equals(annotation.value())) {
            String classFullName = startUpClass.getName();
            packageName = classFullName.substring(0, classFullName.lastIndexOf(".") - 1);
        } else {
            packageName = annotation.value();
        }
        return packageName;
    }

    // 获取提供者的端口
    private static RpcProviderExport getRpckProviderExport(Class<?> startUpClass) {
        RpcProviderExport annotation = startUpClass.getAnnotation(RpcProviderExport.class);
        if (null == annotation) {
            throw new IllegalArgumentException("@Interface:RpcProviderExport must not null !!");
        }
        if (annotation.port() <= 0 || annotation.port() > 65535) {
            throw new IllegalArgumentException("Invalid port " + annotation.port());
        }
        return annotation;
    }

    // 获取注册中心IP与端口
    private static RpcZkExport getRpcZkExport(Class<?> startUpClass) {
        RpcZkExport annotation = startUpClass.getAnnotation(RpcZkExport.class);
        if (null == annotation) {
            throw new IllegalArgumentException("@Interface:RpcProviderExport must not null !!");
        }
        if (annotation.ip() == null || annotation.ip().length() == 0) {
            throw new IllegalArgumentException("ip == null!");
        }
        if (annotation.port() <= 0 || annotation.port() > 65535) {
            throw new IllegalArgumentException("Invalid port " + annotation.port());
        }
        return annotation;
    }

    // 扫描路径下贴有RpcInvokerService注解的实现类，表示此类需要远程调用,将此类暴露出到rpc-zk,相当于将
    // interfaceImpl.class + methods + prameterType + ip + port 注册到 rpc-zk 的自定义容器里面，
    // 并每隔配置时间（默认为5s）与之通信，并检查是否注册信息
    private static void doRegiste(Class<?> startUpClass) throws Throwable {
        // 获取提供者的端口
        RpcProviderExport rpcProviderExport = getRpckProviderExport(startUpClass);
        // 获取注册中心IP与端口
        RpcZkExport rpcZkExport = getRpcZkExport(startUpClass);
        // 获取扫描路径
        String packageScan = getPackageScan(startUpClass);
        // 调用Reflections类获取指定注解下的实现类
        Reflections reflections = new Reflections(packageScan);
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(RpcInvokerService.class);
        // 调用与注册中心的通信方法
        registerRpcProvider(rpcProviderExport, classes, rpcZkExport);
    }


    private static void registerRpcProvider(RpcProviderExport rpcProviderExport, Set<Class<?>> classes, RpcZkExport rpcZkExport) {
        // 此时服务器相当于客户端，rpc-zk 相当于服务端，创建服务端Socket,与 rpc-zk 对接
        Socket socket = doCreateSocket(rpcZkExport);
        // 发送消息与接收消息
        doGetAndSendMsg2Zk(socket, classes, rpcProviderExport.port(), rpcZkExport);
    }

    private static void doGetAndSendMsg2Zk(Socket socket, Set<Class<?>> classes, int localPort, RpcZkExport rpcZkExport) {
        // 向 rpc-zk 进行请求，并经请求参数放进流中，将对象写入到流里面，
        try {
            // 获取暴露服务机器的Ip
            InetAddress addr = InetAddress.getLocalHost();
            String localIP = addr.getHostAddress().toString();
            // 获取输出流
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            for (Iterator<Class<?>> iterator = classes.iterator(); iterator.hasNext(); ) {
                Class<?> o = iterator.next();
                // 此处不需要用到代理，直接生成 RegisteServiceImplDefination 对象，写入流里面
                RegisteServiceImplDefination defination = new RegisteServiceImplDefination();
                defination.setInterfaceImplClass(o.getClass());
                defination.setInterfaceClass(o.getInterfaces());
                defination.setIp(localIP);
                defination.setPort(localPort);
                defination.setMethods(o.getMethods());
                // 发送请求
                logger.info("\n rpc-provider sending the request：[]" + defination);
                outputStream.writeObject(defination.toString().getBytes(DataConstant.UTF8));

                // 读取 rpc-zk 返回的信息，证明已经注册成功
                ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                Object result = inputStream.readObject();
                if (result instanceof Throwable) {
                    throw (Throwable) result;
                }
                logger.info("\n rpc-provider is registered successfully,and the result：[] " + result);
                socket.shutdownOutput();
                socket.shutdownInput();
            }
            socket.close();
        } catch (Throwable e) {
            e.printStackTrace();
            doCreateSocket(rpcZkExport);
        }
    }

    private static Socket doCreateSocket(RpcZkExport rpcZkExport) {
        do {
            try {
                logger.info("rpc-provicder try to connet the rpc-zk ");
                Socket socket = new Socket(rpcZkExport.ip(), rpcZkExport.port());
                return socket;
            } catch (IOException e) {
                logger.info("rpc-provicder try to connet the rpc-zk faild");
            }
        } while (true);
    }
}
