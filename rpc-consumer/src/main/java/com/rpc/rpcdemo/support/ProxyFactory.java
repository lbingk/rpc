package com.rpc.rpcdemo.support;

import com.rpc.rpcdemo.beandefinition.InvokerBeanDefinition;
import com.rpc.rpcdemo.beandefinition.InvokerMachineSocketDefination;
import com.rpc.rpcdemo.util.SerializeUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;


// 为消费者生成动态代理的对象
public class ProxyFactory implements InvocationHandler {
    // 被代理对象
    private Class<?> clz;
    // 经过一系列的算法计算出的远程服务地址
    private InvokerMachineSocketDefination invokerMachineSocketDefination;

    /*缓冲区大小*/
    private static int BLOCK = 4096;
    /*接受数据缓冲区*/
    private static ByteBuffer sendbuffer = ByteBuffer.allocate(BLOCK);
    /*发送数据缓冲区*/
    private static ByteBuffer receivebuffer = ByteBuffer.allocate(BLOCK);
    /*服务器端地址*/
    private static InetSocketAddress SERVER_ADDRESS = null;
    /*暴露调用的地址*/
    private static int port;
    /*暴露调用的IP*/
    private static String ip;


    public ProxyFactory(Class<?> serviceClass, InvokerMachineSocketDefination invokerMachineSocketDefination, int consumerPort) {
        this.clz = serviceClass;
        this.invokerMachineSocketDefination = invokerMachineSocketDefination;
        SERVER_ADDRESS = new InetSocketAddress(invokerMachineSocketDefination.getIp(), invokerMachineSocketDefination.getPort());
        port = consumerPort;
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 建立与提供者的通信
        // 打开socket通道
        SocketChannel socketChannel = SocketChannel.open();
        // 设置为非阻塞方式
        socketChannel.configureBlocking(false);
        // 打开选择器
        Selector selector = Selector.open();
        // 注册连接服务端socket动作
        socketChannel.register(selector, SelectionKey.OP_CONNECT);
        // 连接
        socketChannel.connect(SERVER_ADDRESS);
        // 分配缓冲区大小内存

        Set<SelectionKey> selectionKeys;
        Iterator<SelectionKey> iterator;
        SelectionKey selectionKey;
        SocketChannel client;
        String receiveText = "";
        int count = 0;

        while (true) {
            //选择一组键，其相应的通道已为 I/O 操作准备就绪。
            //此方法执行处于阻塞模式的选择操作。
            selector.select(1000);
            //返回此选择器的已选择键集。
            selectionKeys = selector.selectedKeys();
            iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                selectionKey = iterator.next();
                if (selectionKey.isConnectable()) {
                    System.out.println("client connect");
                    client = (SocketChannel) selectionKey.channel();
                    // 判断此通道上是否正在进行连接操作。
                    // 完成套接字通道的连接过程。
                    if (client.isConnectionPending()) {
                        client.finishConnect();
                    }
                    client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                } else if (selectionKey.isReadable()) {
                    client = (SocketChannel) selectionKey.channel();
                    //将缓冲区清空以备下次读取
                    receivebuffer.clear();
                    //读取服务器发送来的数据到缓冲区中
                    count = client.read(receivebuffer);
                    if (count > 0) {
                        receiveText = new String(receivebuffer.array(), 0, count);
                        System.out.println("接收服务端返回的执行结果 --:" + receiveText);
                        client.register(selector, SelectionKey.OP_WRITE);
                    }

                } else if (selectionKey.isWritable()) {
                    sendbuffer.clear();
                    client = (SocketChannel) selectionKey.channel();
                    // 定义调用参数
                    InvokerBeanDefinition invokerBeanDefinition = new InvokerBeanDefinition();
                    invokerBeanDefinition.setIp(ip);
                    invokerBeanDefinition.setPort(port);
                    invokerBeanDefinition.setServiceClass(clz);
                    invokerBeanDefinition.setMethodName(method);
                    invokerBeanDefinition.setParamaters(args);
                    // 序列化
                    String sendText = SerializeUtil.serializeToString(invokerBeanDefinition);
                    sendbuffer.put(sendText.getBytes());
                    //将缓冲区各标志复位,因为向里面put了数据标志被改变要想从中读取数据发向服务器,就要复位
                    sendbuffer.flip();
                    client.write(sendbuffer);
                    System.out.println("发送消息到服务端 --：" + sendText);
                    client.register(selector, SelectionKey.OP_READ);
                    socketChannel.close();
                    continue;
                }
            }
            selectionKeys.clear();
            return receiveText;
        }
    }
}
