package com.rpc.rpcdemo;

import com.rpc.rpcdemo.beandefinition.RpcZkContext;
import com.rpc.rpcdemo.constant.DataConstant;
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
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

// 此类定义了处理服务器接收参数
public class AcceptRegisterServer implements Runnable {
    private static final Log logger = LogFactory.getLog(AcceptRegisterServer.class);

    private static final Charset charset = Charset.forName(DataConstant.UTF8);

    private LinkedBlockingQueue<StringBuilder> linkedBlockingQueue = null;
    // 多路复用器
    private Selector selector;
    // 数据缓冲区
    private ByteBuffer readBuffer = ByteBuffer.allocate(1024);

    protected AcceptRegisterServer(int port, LinkedBlockingQueue<StringBuilder> queue) {
        this.linkedBlockingQueue = queue;
        try {
            // 打开多路复用器
            this.selector = Selector.open();
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
                this.selector.select();
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
                            // 如果是可以读的话
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
            if (read == -1) {
                // 证明没有输入,关掉通道
                channel.close();
                next.cancel();
            }
            // 有输入，则缓冲区复位
            this.readBuffer.flip();
            // 根据输入数据的大小来初始化数组的大小
            byte[] bytes = new byte[this.readBuffer.remaining()];
            // 将数组写进缓冲区里面
            ByteBuffer byteBuffer = this.readBuffer.get(bytes);

            CharsetDecoder decoder = charset.newDecoder();
            CharBuffer charBuffer = decoder.decode(byteBuffer);
            StringBuilder append = new StringBuilder().append(charBuffer.toString());
            // 将注册中心收到的消息放到zk的上下文里面
            logger.info("已经成功接收注册内容：{}" + append);
            linkedBlockingQueue.put(append);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void accept(SelectionKey next) {
        // 获取服务通道
        ServerSocketChannel channel = (ServerSocketChannel) next.channel();
        try {
            // 调用服务器端的accept方法获取客户端的通道
            SocketChannel accept = channel.accept();
            // 设置为非阻塞
            channel.configureBlocking(false);
            // 注册
            channel.register(this.selector, SelectionKey.OP_READ);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
