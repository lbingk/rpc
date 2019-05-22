package com.rpc.rpcdemo.beandefinition;

import lombok.Data;

import java.io.Serializable;

// 此类定义xml配置的提供者的信息
@Data
public class RegisterServiceDefination  {
    // 暴露服务的端口
    private int port;
    // 连接超时时间
    private int timeout;
}
