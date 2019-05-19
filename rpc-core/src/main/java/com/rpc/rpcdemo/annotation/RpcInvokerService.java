package com.rpc.rpcdemo.annotation;



import org.springframework.stereotype.Service;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Service
public @interface RpcInvokerService {
    public String value() default "";
}

