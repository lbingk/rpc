package com.rpc.rpcdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

@ImportResource(value = {"classpath*:MATA-INF/spring/rpc.xml"})
@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) throws Throwable {
        SpringApplication.run(DemoApplication.class, args);
    }
}
