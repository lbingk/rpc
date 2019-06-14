package com.rpc.rpcdemo.impl;

import com.rpc.rpcdemo.annotation.RpcInvokerService;
import com.rpc.rpcdemo.service.RpcDemoEmployeService;


@RpcInvokerService
public class RpcDemoEmployeServiceImpl implements RpcDemoEmployeService {
    @Override
    public void hello() {

        System.out.println("hello");
    }

    @Override
    public void hi() {

        System.out.println("hi");
    }
}
