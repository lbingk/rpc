package com.rpc.rpcdemo;

import com.rpc.rpcdemo.beandefinition.RegisterZKDefination;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ImportResource;

import java.net.UnknownHostException;


@ImportResource(locations = {"classpath*:META-INF/rpc.xml"})
@SpringBootApplication
public class ProviderApplication {
    public static void main(String[] args) throws UnknownHostException {
        ApplicationContext context = SpringApplication.run(ProviderApplication.class, args);
        context.getBean(RegisterZKDefination.class);
        RegisterRpcInvokerHandler.register(context);
    }
}
