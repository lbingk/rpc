package com.rpc.rpcdemo.beandefinition;

import lombok.Data;

// 此类定义xml配置的注册中心的信息
@Data
public class RpcServiceDefination {
    // 需要远程调用的接口全限定类名
    private String serviceClassName;
    // 配置的别名
    private String ref;
}
