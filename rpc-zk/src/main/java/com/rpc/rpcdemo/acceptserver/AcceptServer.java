package com.rpc.rpcdemo.acceptserver;

import com.google.common.collect.Lists;
import com.rpc.rpcdemo.beandefinition.InvokerMachineSocketDefination;
import com.rpc.rpcdemo.beandefinition.RegisterServiceImplDefination;
import com.rpc.rpcdemo.constant.DataConstant;
import com.rpc.rpcdemo.util.SerializeUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

// 此类定义了处理服务器接收参数
public class AcceptServer implements Runnable {
    private static final Log logger = LogFactory.getLog(AcceptServer.class);

    private LinkedBlockingQueue<RegisterServiceImplDefination> linkedBlockingQueue = null;
    // 多路复用器
    private Selector selector;
    // 接收数据缓冲区
    private ByteBuffer receivebuffer = ByteBuffer.allocate(4048 * 4);
    // 发送数据缓冲区
    private ByteBuffer sendbuffer = ByteBuffer.allocate(4048 * 4);
    // 连接等待时间
    private int timeout;
    // 映射客户端channel
    private Map<String, SocketChannel> clientsMap = new HashMap<String, SocketChannel>();


    protected AcceptServer(int port, int timeout, LinkedBlockingQueue<RegisterServiceImplDefination> queue) {
        try {
            // 初始化相关参数
            init(port, timeout, queue);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                int select = selector.select(timeout);
                if (select == 0) {
                    continue;
                }
                //返回值为本次触发的事件数
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectionKeys.iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey next = keyIterator.next();
                    selectionKeys.remove(next);
                    handle(next);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void handle(SelectionKey key) {
        if (key.isAcceptable()) {
            this.accept(key);
        }
        if (key.isReadable()) {
            this.read(key);
        }
        if (key.isWritable()) {
            this.write(key);
        }
    }

    private void init(int port, int timeout, LinkedBlockingQueue<RegisterServiceImplDefination> queue) throws IOException {

        this.timeout = timeout;
        this.linkedBlockingQueue = queue;

        // 打开服务器套接字通道
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        // 服务器配置为非阻塞
        serverSocketChannel.configureBlocking(false);
        // 检索与此通道关联的服务器套接字
        ServerSocket serverSocket = serverSocketChannel.socket();
        // 进行服务的绑定
        serverSocket.bind(new InetSocketAddress(port));
        // 通过open()方法找到Selector
        selector = Selector.open();
        // 注册到selector，等待连接
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        logger.info("注册中心ZK已经启动，端口号为：" + port);

    }


    private void accept(SelectionKey next) {
        // 获取服务通道
        try {
            // 返回为之创建此键的通道。
            ServerSocketChannel channel = (ServerSocketChannel) next.channel();
            // 接受到此通道套接字的连接。
            // 此方法返回的套接字通道（如果有）将处于阻塞模式。
            SocketChannel accept = channel.accept();
            // 配置为非阻塞
            accept.configureBlocking(false);
            // 注册到selector，等待连接
            accept.register(selector, SelectionKey.OP_READ);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void read(SelectionKey next) {
        // 清空缓冲区
        this.receivebuffer.clear();
        // 获取客户端通道
        SocketChannel channel = (SocketChannel) next.channel();
        try {
            // 客户端是否有输入
            int read = channel.read(receivebuffer);
            while (read > 0) {
                // 解析
                Object analysisObj = analysisreceivebuffer();
                if (analysisObj instanceof RegisterServiceImplDefination) {

                    // 证明是服务端注册
                    logger.info("已经成功接收注册内容：" + analysisObj);

                    // 将注册中心收到的消息放到zk的上下文里面
                    RegisterServiceImplDefination implDefination = (RegisterServiceImplDefination) analysisObj;
                    linkedBlockingQueue.put(implDefination);

                } else {
                    // 证明是消费者订阅服务
                    logger.info("已经成功接收到消费端订阅服务：" + analysisObj);

                    // 发送地址列表信息
                    channel.register(selector, SelectionKey.OP_WRITE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void write(SelectionKey key) {
        // 将缓冲区清空以备下次写入
        sendbuffer.clear();
        // 返回为之创建此键的通道
        SocketChannel channel = (SocketChannel) key.channel();
        try {
            List<InvokerMachineSocketDefination> invokerMachineSocketDefinationList = Lists.newArrayList();
            InvokerMachineSocketDefination invokerMachineSocketDefination = new InvokerMachineSocketDefination();
            // TODO:假数据
            invokerMachineSocketDefination.setIp("127.0.0.1");
            invokerMachineSocketDefination.setPort(20886);
            invokerMachineSocketDefinationList.add(invokerMachineSocketDefination);

            String s = SerializeUtil.serializeToString(invokerMachineSocketDefinationList);

            // 向缓冲区中输入数据
            sendbuffer.put(s.getBytes());
            // 将缓冲区各标志复位,因为向里面put了数据标志被改变要想从中读取数据发向服务器,就要复位
            sendbuffer.flip();
            // 输出到通道
            channel.write(sendbuffer);
            channel.register(selector, SelectionKey.OP_READ);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private Object analysisreceivebuffer() {
        // 按照 UTF8 的编码格式解析
        Charset charset = Charset.forName(DataConstant.UTF8);
        CharsetDecoder decoder = charset.newDecoder();
        CharBuffer buffer = null;
        Object deserializeObj = null;

        try {
            buffer = decoder.decode(receivebuffer);
            String toString = buffer.toString();

            deserializeObj = SerializeUtil.deserializeToObject(toString);

        } catch (CharacterCodingException e) {
            e.printStackTrace();
        } finally {
            return deserializeObj;
        }
    }

}
