package com.rpc.rpcdemo.beandefinition;

import lombok.Data;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

@Data
public class RpcZkContext {
    // 使用链表有界队列，保证先进先出
    private LinkedBlockingQueue<StringBuilder> linkedBlockingQueue = new LinkedBlockingQueue();

    // 使用ConcurrentHashMap，保证安全与性能
    // key:service接口名为key
    private ConcurrentHashMap<String,RegisteServiceDefination2ZK> registeServiceMap = new ConcurrentHashMap<>(24);


    // 单例模式，保证唯一
    private RpcZkContext() {}
    private static class SingletonInstance {
        private static final RpcZkContext INSTANCE = new RpcZkContext();
    }
    public static RpcZkContext getInstance() {
        return SingletonInstance.INSTANCE;
    }
}
