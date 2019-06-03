package com.rpc.rpcdemo.beandefinition;

import lombok.Data;

import java.io.Serializable;
import java.lang.reflect.Method;

@Data
public class InvokerBeanDefinition implements Serializable {
   private Class<?> serviceClass;
   private Method methodName;
   private Object[] paramaters;
   private String ip;
   private int port;
}
