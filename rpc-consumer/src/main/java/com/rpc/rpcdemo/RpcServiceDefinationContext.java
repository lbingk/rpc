package com.rpc.rpcdemo;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rpc.rpcdemo.beandefinition.InvokerMachineSocketDefination;
import org.springframework.beans.factory.config.BeanDefinition;

import java.util.List;
import java.util.Map;

public class RpcServiceDefinationContext {
    // 远程调用的服务名List
    private static List<String> refList = Lists.newArrayList();
    // 远程调用的服务Map
    private static Map<String, BeanDefinition> rpcServiceDefinationMap = Maps.newHashMap();
    // 远程调用服务的所在的IP以及port
    private static Map<String, List<InvokerMachineSocketDefination>> rpcServiceMachineSocketDefinationMap = Maps.newHashMap();

    // 可调远程调用服务
    public Object getRpcServiceBean(String ref) {
        BeanDefinition beanDefinition = rpcServiceDefinationMap.get(ref);
        if (null == beanDefinition) {
            throw new RuntimeException("不存在创建的实例对象");
        }
        return beanDefinition;
    }

    public static void putRef(String ref, BeanDefinition beanDefinition) {
        rpcServiceDefinationMap.put(ref, beanDefinition);
    }

    public static Object addRef(String ref) {
        return refList.add(ref);
    }

    public static List<String> getRefList() {
        return refList;
    }

    // 单例模式
    private static volatile RpcServiceDefinationContext singleton;

    private RpcServiceDefinationContext() {
    }

    public static RpcServiceDefinationContext getInstance() {
        if (singleton == null) {
            synchronized (RpcServiceDefinationContext.class) {
                if (singleton == null) {
                    singleton = new RpcServiceDefinationContext();
                }
            }
        }
        return singleton;
    }

}
