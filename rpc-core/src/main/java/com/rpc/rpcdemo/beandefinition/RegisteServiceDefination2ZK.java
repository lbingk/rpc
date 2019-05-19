package com.rpc.rpcdemo.beandefinition;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.lang.reflect.Method;

@Data
public class RegisteServiceDefination2ZK implements Serializable {
    private String ip;
    private int port;
    private Class<?> interfaceImplClass;
    private Class<?>[] interfaceClass;

}
