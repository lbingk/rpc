package com.rpc.rpcdemo.beandefinition;

import lombok.Data;

import java.io.Serializable;

@Data
public class RegisterServiceDefination2ZK implements Serializable {
    private String ip;
    private int port;
    private Class<?> interfaceImplClass;
    private Class<?>[] interfaceClass;

}
