package com.rpc.rpcdemo.util;

import com.google.common.collect.Lists;
import com.rpc.rpcdemo.annotation.RpcInvokerService;
import com.rpc.rpcdemo.beandefinition.RegisteServiceDefination;
import com.rpc.rpcdemo.beandefinition.RegisteServiceDefination2ZK;
import com.rpc.rpcdemo.beandefinition.RegisteZKDefination;
import com.rpc.rpcdemo.beandefinition.RpcZkContext;
import com.rpc.rpcdemo.constant.DataConstant;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import java.util.Map;

@Component
public class RegisteRpcInvokerUtils {

    private static boolean registeFlag = !DataConstant.REGISTE_SUCCESS;

    private static final Log logger = LogFactory.getLog(RegisteRpcInvokerUtils.class);

    /*
       提供者注册服务
     */
    @PostConstruct
    public static void registeBeanProcess() {
        doRegister();
    }

    // 将RpcInvokerService注解的实现类，表示此类需要远程调用,将此类暴露出到rpc-zk
    private static void doRegister() {
        // 获取提供者的暴露服务的端口

        RegisteServiceDefination registeServiceDefination = null;
        try {
            registeServiceDefination = SpringUtils.getBean(RegisteServiceDefination.class);
        } catch (RuntimeException e) {
            throw new RuntimeException("没有配置对应的注册服务，无法找到暴露服务的信息");
        }
        // 获取注册中心IP与端口
        RegisteZKDefination registeZKDefination = null;
        try {
            registeZKDefination = SpringUtils.getBean(RegisteZKDefination.class);
        } catch (RuntimeException e) {
            throw new RuntimeException("无法找到注册中心信息");
        }

        // 从Spring容器中获取Bean，以保证bean与容器里的bean是一致的
        Map<String, Object> registeBeanMap = SpringUtils.getBeansWithAnnotation(RpcInvokerService.class);
        List<Object> registeBeanInstanceList = Lists.newArrayList();
        for (Map.Entry<String, Object> entry : registeBeanMap.entrySet()) {
            Object value = entry.getValue();
            registeBeanInstanceList.add(value);
        }
        // 调用与注册中心的通信方法
        registerRpcProvider(registeServiceDefination, registeZKDefination, registeBeanInstanceList);
    }

    private static void registerRpcProvider(RegisteServiceDefination registeServiceDefination,
                                            RegisteZKDefination registeZKDefination,
                                            List<Object> registeBeanInstanceList) {
        // 此时服务器相当于客户端，rpc-zk 相当于服务端，创建服务端Socket,与 rpc-zk 对接
        Socket socket = doCreateSocket(registeZKDefination);
        // 发送消息与接收消息
        doGetAndSendMsg2Zk(socket, registeServiceDefination, registeZKDefination, registeBeanInstanceList);
    }

    private static void doGetAndSendMsg2Zk(Socket socket, RegisteServiceDefination registeServiceDefination,
                                           RegisteZKDefination registeZKDefination, List<Object> registeBeanInstanceList) {
        // 向 rpc-zk 进行请求，并经请求参数放进流中，将对象写入到流里面，
        try {
            // 获取暴露服务机器的Ip
            InetAddress addr = InetAddress.getLocalHost();
            String localIP = addr.getHostAddress().toString();
            // 获取输出流
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());

            for (Object bean : registeBeanInstanceList) {
                // 定义注册信息
                RegisteServiceDefination2ZK defination = new RegisteServiceDefination2ZK();
                defination.setIp(localIP);
                defination.setPort(registeServiceDefination.getPort());
                defination.setInterfaceImplClass(bean.getClass());
                defination.setInterfaceClass(bean.getClass().getInterfaces());
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
                // 注意：socket 本身不关闭
            }
            registeFlag = DataConstant.REGISTE_SUCCESS;
        } catch (Throwable e) {
            logger.info(e.getMessage());
        } finally {
            doCreateSocket(registeZKDefination);
        }
    }

    private static Socket doCreateSocket(RegisteZKDefination registeZKDefination) {
        do {
            try {
                logger.info("rpc-provicder try to connet the rpc-zk ");
                Socket socket = new Socket(registeZKDefination.getIp(), registeZKDefination.getPort());
                // 设置超时时间
                socket.setSoTimeout(registeZKDefination.getTimeout());
                return socket;
            } catch (IOException e) {
                registeFlag = !DataConstant.REGISTE_SUCCESS;
                logger.info("rpc-provicder try to connet the rpc-zk faild");
            }
        } while (true);
    }
}
