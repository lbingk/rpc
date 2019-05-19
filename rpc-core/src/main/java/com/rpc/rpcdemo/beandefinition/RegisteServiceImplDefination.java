package com.rpc.rpcdemo.beandefinition;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.lang.reflect.Method;

@Data
@Accessors
public class RegisteServiceImplDefination implements Serializable {
    private String ip;
    private int port;
    private Class<?> interfaceImplClass;
    private Class<?>[] interfaceClass;
    private Method[] methods;

}
