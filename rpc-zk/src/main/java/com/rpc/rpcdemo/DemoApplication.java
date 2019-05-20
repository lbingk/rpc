package com.rpc.rpcdemo;

import com.rpc.rpcdemo.util.AcceptRegistesZKUtils;
import com.rpc.rpcdemo.util.ApplicationContextHolder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ImportResource;

import java.io.IOException;

@SpringBootApplication
@ImportResource(locations = {"classpath*:META-INF/spring/rpc.xml"})
public class DemoApplication {

    public static void main(String[] args) throws IOException {
        ConfigurableApplicationContext context = SpringApplication.run(DemoApplication.class, args);
        ApplicationContextHolder.setApplicationContext(context);
        AcceptRegistesZKUtils.acceptRegisteBeanProcess();

    }
}
