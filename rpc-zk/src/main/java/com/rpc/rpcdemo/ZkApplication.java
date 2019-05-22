package com.rpc.rpcdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ImportResource;

import java.io.IOException;

@SpringBootApplication
@ImportResource(locations = {"classpath*:META-INF/rpc.xml"})
public class ZkApplication {

    public static void main(String[] args) throws IOException {
        ApplicationContext context = SpringApplication.run(ZkApplication.class, args);
        AcceptRegisterZkHandler.acceptRegisteBeanProcess(context);

    }
}
