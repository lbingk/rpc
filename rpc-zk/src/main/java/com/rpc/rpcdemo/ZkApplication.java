package com.rpc.rpcdemo;

import com.rpc.rpcdemo.util.AcceptRegistesZkUtils;
import com.rpc.rpcdemo.util.ApplicationContextHolder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ImportResource;

import java.io.IOException;

@SpringBootApplication
@ImportResource(locations = {"classpath*:META-INF/spring/rpc.xml"})
public class ZkApplication {

    public static void main(String[] args) throws IOException {
        ConfigurableApplicationContext context = SpringApplication.run(ZkApplication.class, args);
        ApplicationContextHolder.setApplicationContext(context);
        AcceptRegistesZkUtils.acceptRegisteBeanProcess();

    }
}
