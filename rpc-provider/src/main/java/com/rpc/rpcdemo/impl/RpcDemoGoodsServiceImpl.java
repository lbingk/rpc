package com.rpc.rpcdemo.impl;

import com.rpc.rpcdemo.annotation.RpcInvokerService;
import com.rpc.rpcdemo.service.RpcDemoGoodsService;


@RpcInvokerService
public class RpcDemoGoodsServiceImpl implements RpcDemoGoodsService {
    @Override
    public void hello() {
        System.out.println("hello");
    }

    @Override
    public void hi() {
        System.out.println("hi");
    }
}
