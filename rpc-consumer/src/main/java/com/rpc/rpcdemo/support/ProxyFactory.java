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
public class ProxyFactory {

    public static Proxy<?> createProxy(Class<?> serviceClass, InvokerMachineSocketDefination invokerMachineSocketDefination, int consumerPort) {
        return new Proxy<>(serviceClass, invokerMachineSocketDefination, consumerPort);
    }
}
