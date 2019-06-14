package com.rpc.rpcdemo;

import com.rpc.rpcdemo.service.RpcDemoEmployeService;
import com.rpc.rpcdemo.service.RpcDemoUserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ImportResource;

@ImportResource(locations = {"classpath*:META-INF/rpc.xml"})
@SpringBootApplication
public class ConsumerApplication {


    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(ConsumerApplication.class, args);
        RpcDemoEmployeService rpcDemoEmployeService = (RpcDemoEmployeService)context.getBean("rpcDemoEmployeService");
        rpcDemoEmployeService.hello();
        rpcDemoEmployeService.hello();
        rpcDemoEmployeService.hello();
        rpcDemoEmployeService.hello();
        rpcDemoEmployeService.hello();
        rpcDemoEmployeService.hello();
        rpcDemoEmployeService.hello();
    }

}
