package com.rpc.rpcdemo.beandefinition;

import lombok.Data;

// 此类定义xml配置的注册中心的信息
@Data
public class SubscribeZKDefination {
    // 注册中心的地址
    private String zkIp;
    // 注册中心的端口
    private int zkPort;
    // 连接超时时间
    private int zkTimeout;
    // 暴露的端口
    private int consumerPort;
}
