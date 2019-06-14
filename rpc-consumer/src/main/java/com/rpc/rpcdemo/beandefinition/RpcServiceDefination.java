package com.rpc.rpcdemo.beandefinition;

import com.google.common.collect.Lists;
import com.rpc.rpcdemo.support.ProxyFactory;
import com.rpc.rpcdemo.util.SerializeUtil;
import lombok.Data;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

// 此类定义xml配置的注册中心的信息
@Data
public class RpcServiceDefination implements ApplicationContextAware, FactoryBean {

    // 注册标志位
    private Boolean subscribeFlag = false;

    // 多路复用器
    private Selector selector = null;

    // 获取spring的上下文
    private ApplicationContext applicationContext;

    // 获取配置的注册中心信息
    private SubscribeZKDefination subscribeZKDefination;

    // 需要远程调用的接口全限定类名
    private String serviceClassName;

    // 配置的别名
    private String ref;

    // 配置的接口Class
    private Class<?> serviceClass;

    // 服务注册列表，key为serviceClassName
    List<InvokerMachineSocketDefination> invokerMachineSocketDefinationList = Lists.newArrayList();

    private static final Log logger = LogFactory.getLog(RpcServiceDefination.class);

    @Override
    public Object getObject() throws Exception {
        // 校验
        if (serviceClassName == null || serviceClassName.length() == 0) {
            throw new IllegalAccessException("配置参数有误，引用的服务名有误");
        }
        // 初始化
        initBeanParameters();
        // 向注册中心注册服务，获取对应的服务提供者列表
        subscribe();
        // 生成代理对象
        Object proxy = createProxy();
        return proxy;
    }

    private Object createProxy() {
        // TODO: 先假定获取地址列表的第一个地址参数，此处先省去负载均衡的手写实现
        return ProxyFactory.createProxy(serviceClass, invokerMachineSocketDefinationList.get(0), subscribeZKDefination.getConsumerPort());
    }

    private void subscribe() {
        try {
            doSubscribe();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void doSubscribe() throws IOException {
        // TODO: 此处省去监听注册中心的服务地址列表变化
        // 获取配置的注册中心信息
        subscribeZKDefination = applicationContext.getBean(SubscribeZKDefination.class);
        if (subscribeZKDefination == null) {
            throw new IllegalArgumentException("注册中心配置信息缺失");
        }

        selector = Selector.open();
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_CONNECT);
        socketChannel.connect(new InetSocketAddress(subscribeZKDefination.getZkIp(), subscribeZKDefination.getZkPort()));
        String receiveText = null;

        while (true) {
            //选择一组键，其相应的通道已为 I/O 操作准备就绪。
            //此方法执行处于阻塞模式的选择操作。
            selector.select();
            //返回此选择器的已选择键集。
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                if (selectionKey.isConnectable()) {
                    SocketChannel client = (SocketChannel) selectionKey.channel();
                    // 判断此通道上是否正在进行连接操作。
                    // 完成套接字通道的连接过程。
                    if (client.isConnectionPending()) {
                        client.finishConnect();
                    }
                    client.register(selector, SelectionKey.OP_WRITE);
                } else if (selectionKey.isReadable()) {
                    SocketChannel client = (SocketChannel) selectionKey.channel();
                    // 接收返回地址列表信息
                    ByteBuffer receivebuffer = ByteBuffer.allocate(4048 * 4);
                    // 将缓冲区清空以备下次读取
                    receivebuffer.clear();
                    // 读取服务器发送来的数据到缓冲区中
                    int count = client.read(receivebuffer);
                    if (count > 0) {
                        // 解析收到的消息
                        receiveText = new String(receivebuffer.array(), 0, count);
                        List<InvokerMachineSocketDefination> invokerMachineSocketDefinations =
                                (List<InvokerMachineSocketDefination>) SerializeUtil.deserializeToObject(receiveText);
                        invokerMachineSocketDefinationList.addAll(invokerMachineSocketDefinations);

                        client.register(selector, SelectionKey.OP_WRITE);
                    }
                    receivebuffer.clear();

                    // TODO:此处暂以：必须返回地址列表信息，否则异常
                    if (invokerMachineSocketDefinationList.isEmpty()) {
                        throw new RuntimeException("注册中心没该服务的地址列表");
                    }

                    System.out.println("接收到的注册中心提供的的地址列表信息: " + invokerMachineSocketDefinationList.get(0).toString());
                    socketChannel.close();
                    return;
                } else if (selectionKey.isWritable()) {
                    SocketChannel client = (SocketChannel) selectionKey.channel();
                    // 获取需要远程调用的服务
                    String s = SerializeUtil.serializeToString(serviceClassName);
                    // 定义缓冲区，用来接收注册中心的信息（服务提供者的地址列表）
                    ByteBuffer sendbuffer = ByteBuffer.allocate(4048 * 4);
                    sendbuffer.put(s.getBytes());
                    // 复位
                    sendbuffer.flip();
                    client.write(sendbuffer);
                    logger.info("尝试向注册中心: " + socketChannel.getRemoteAddress() + " 订阅服务：" + ref);
                    client.register(selector, SelectionKey.OP_READ);
                    // 清空
                    sendbuffer.clear();
                }
            }
            selectionKeys.clear();
        }
    }

    private void initBeanParameters() {
        try {
            serviceClass = Class.forName(serviceClassName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Class<?> getObjectType() {
        return null;
    }

    @Override
    public boolean isSingleton() {
        // 默认为单例
        return true;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
