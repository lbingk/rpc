package com.rpc.rpcdemo.support;

import com.rpc.rpcdemo.beandefinition.InvokerMachineSocketDefination;

import java.lang.reflect.Proxy;


// 为消费者生成动态代理的对象
public class ProxyFactory {

    public static Object createProxy(Class<?> serviceClass, InvokerMachineSocketDefination invokerMachineSocketDefination, int consumerPort) {
        return Proxy.newProxyInstance(serviceClass.getClassLoader(), new Class<?>[]{serviceClass},
                new RpcServiceBean(serviceClass, invokerMachineSocketDefination, consumerPort) {});
    }
}
