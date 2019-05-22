package com.rpc.rpcdemo.beandefinition;

import lombok.Data;

import java.io.Serializable;

@Data
public class RegisterServiceImpl implements Serializable {
    private String ip;
    private int port;
    private Class<?> interfaceImplClass;
    private Class<?>[] interfaceClass;

}
