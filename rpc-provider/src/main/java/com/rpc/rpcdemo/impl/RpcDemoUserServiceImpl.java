package com.rpc.rpcdemo.impl;

import com.rpc.rpcdemo.annotation.RpcInvokerService;
import com.rpc.rpcdemo.service.RpcDemoGoodsService;
import com.rpc.rpcdemo.service.RpcDemoUserService;


//@RpcInvokerService
public class RpcDemoUserServiceImpl implements RpcDemoUserService {
    @Override
    public void hello() {
        System.out.println("hello");
    }

    @Override
    public void hi() {
        System.out.println("hi");
    }
}
