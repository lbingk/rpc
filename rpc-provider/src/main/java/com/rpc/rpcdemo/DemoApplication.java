package com.rpc.rpcdemo;

import com.rpc.rpcdemo.annotation.RpcPakageComponentScan;
import com.rpc.rpcdemo.annotation.RpcProviderExport;
import com.rpc.rpcdemo.annotation.RpcZkExport;
import com.rpc.rpcdemo.util.RegisteRpcInvokerUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@RpcProviderExport(port = 20880)
@RpcZkExport(ip = "127.0.0.1", port = 2181)
@RpcPakageComponentScan

@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) throws Throwable {
        SpringApplication.run(DemoApplication.class, args);

        // TODO:此方法待改成spring启动时自动隐式调用
        // 调用调用 rpc-core 的注册服务方法
        RegisteRpcInvokerUtils.registeBeanProcess(DemoApplication.class);
        ApplicationContext applicationContext;
}
}
