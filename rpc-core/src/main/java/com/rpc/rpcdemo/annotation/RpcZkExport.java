package com.rpc.rpcdemo.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RpcZkExport {
    public String ip() default "";

    public int port() default 88888888;
}
