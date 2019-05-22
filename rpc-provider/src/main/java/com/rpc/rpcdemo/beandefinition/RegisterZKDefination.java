package com.rpc.rpcdemo.beandefinition;

import lombok.Data;

// 此类定义xml配置的注册中心的信息
@Data
public class RegisterZKDefination {
    // 注册中心的地址
    private String ip;
    // 注册中心的端口
    private int port;
    // 连接超时时间
    private int timeout;
}
