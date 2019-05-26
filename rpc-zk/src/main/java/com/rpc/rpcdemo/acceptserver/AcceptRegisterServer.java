package com.rpc.rpcdemo.acceptserver;

import com.rpc.rpcdemo.beandefinition.RegisterServiceImplDefination;
import com.rpc.rpcdemo.constant.DataConstant;
import com.rpc.rpcdemo.util.SerializeUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

// 此类定义了处理服务器接收参数
public class AcceptRegisterServer implements Runnable {
    private static final Log logger = LogFactory.getLog(AcceptRegisterServer.class);

    private LinkedBlockingQueue<RegisterServiceImplDefination> linkedBlockingQueue = null;
    // 多路复用器
    private Selector selector;
    // 数据缓冲区
    private ByteBuffer readBuffer = ByteBuffer.allocate(4048);
    // 连接等待时间
    private int timeout;

    protected AcceptRegisterServer(Selector selector,int port, int timeout,
                                   LinkedBlockingQueue<RegisterServiceImplDefination> queue) {
        this.timeout = timeout;
        this.linkedBlockingQueue = queue;
        try {
            // 打开多路复用器
            this.selector = selector;
//            this.selector = Selector.open();
            // 打开服务器通道
            ServerSocketChannel socketChannel = ServerSocketChannel.open();
            // 服务器通道设置为false
            socketChannel.configureBlocking(false);
            // 绑定地址
            InetSocketAddress inetSocketAddress = new InetSocketAddress(port);
            socketChannel.bind(inetSocketAddress);
            // 将服务器通道注册到多路复用器上，并且监听阻塞事件
            socketChannel.register(this.selector, SelectionKey.OP_ACCEPT);
            logger.info("注册中心ZK已经启动，端口号为：" + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                // 多路复用器开始监听
                this.selector.select(timeout);
                // 返回多路复用器已经选择的结果集
                Set<SelectionKey> selectionKeys = this.selector.selectedKeys();
                // 遍历结果集
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey next = iterator.next();
                    // 获取后直接在容器中移除
                    iterator.remove();
                    // 如果是有效的话
                    if (next.isValid()) {
                        if (next.isAcceptable()) {
                            // 如果是阻塞状态
                            this.accept(next);
                        }
                        if (next.isReadable()) {
                            // 如果是可以读的话：此处接收服务端的服务注册
                            this.read(next);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void read(SelectionKey next) {
        // 清空缓冲区
        this.readBuffer.clear();
        // 获取客户端通道
        SocketChannel channel = (SocketChannel) next.channel();
        try {
            // 客户端是否有输入
            int read = channel.read(readBuffer);
            while (read != -1) {
                // read != -1 证明还没读到流的末端(连接没有关掉)
                // 有输入，则缓冲区复位
                StringBuilder sb = new StringBuilder();
                this.readBuffer.flip();
                // 解析
                RegisterServiceImplDefination registerServiceDefination = analysisReadBuffer();
                // 清除数据
                readBuffer.clear();
                // 将注册中心收到的消息放到zk的上下文里面
                logger.info("已经成功接收注册内容：" + registerServiceDefination);
                linkedBlockingQueue.put(registerServiceDefination);
                read = channel.read(readBuffer);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private RegisterServiceImplDefination analysisReadBuffer() {
        // 按照 UTF8 的编码格式解析
        Charset charset = Charset.forName(DataConstant.UTF8);
        CharsetDecoder decoder = charset.newDecoder();
        CharBuffer buffer = null;
        RegisterServiceImplDefination registerServiceImplDefination = new RegisterServiceImplDefination();
        try {
            buffer = decoder.decode(readBuffer);
            String toString = buffer.toString();

            registerServiceImplDefination = (RegisterServiceImplDefination) SerializeUtil.deserializeToObject(toString);

        } catch (CharacterCodingException e) {
            e.printStackTrace();
        } finally {
            return registerServiceImplDefination;

        }
    }

    private void accept(SelectionKey next) {
        // 获取服务通道
        ServerSocketChannel channel = (ServerSocketChannel) next.channel();
        try {
            // 调用服务器端的accept方法获取客户端的通道
            SocketChannel accept = channel.accept();
            // 设置为非阻塞
            accept.configureBlocking(false);
            // 注册,注意是客户通道
            accept.register(this.selector, SelectionKey.OP_READ);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
