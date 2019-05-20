package com.rpc.rpcdemo;

import com.rpc.rpcdemo.util.ApplicationContextHolder;
import com.rpc.rpcdemo.util.RegisteRpcInvokerUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ImportResource;


@ImportResource(locations = {"classpath*:META-INF/spring/rpc.xml"})
@SpringBootApplication
public class ProviderApplication {
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(ProviderApplication.class, args);
        ApplicationContextHolder.setApplicationContext(context);
        RegisteRpcInvokerUtils.registeBeanProcess();
    }
}
