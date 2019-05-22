package com.rpc.rpcdemo.annotation;


import org.springframework.stereotype.Service;

import java.lang.annotation.*;

// 此注解表明该类是交于spring管理的需要远程调用的接口
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Service
public @interface RpcInvokerService {
    public String value() default "";
}

