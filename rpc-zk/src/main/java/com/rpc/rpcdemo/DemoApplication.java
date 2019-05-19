package com.rpc.rpcdemo;

import com.rpc.rpcdemo.util.AcceptRegistesZKUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication

public class DemoApplication {

    public static void main(String[] args) throws IOException {
        SpringApplication.run(DemoApplication.class, args);

        AcceptRegistesZKUtils.acceptRegisteBeanProcess(DemoApplication.class);
    }

}
