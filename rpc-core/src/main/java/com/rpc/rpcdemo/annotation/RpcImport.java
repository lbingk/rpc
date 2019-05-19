package com.rpc.rpcdemo.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RpcImport {
    public String ip() default "";

    public String port() default "";
}
