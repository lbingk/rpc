package com.rpc.rpcdemo.annotation;


import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RpcPakageComponentScan {
    public String value() default "";
}
