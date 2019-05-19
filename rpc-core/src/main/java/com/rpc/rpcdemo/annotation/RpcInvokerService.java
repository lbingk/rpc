package com.rpc.rpcdemo.annotation;



import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RpcInvokerService {
    public String value() default "";
}

