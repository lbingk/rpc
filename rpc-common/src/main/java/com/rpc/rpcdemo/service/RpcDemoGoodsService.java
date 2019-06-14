package com.rpc.rpcdemo.service;


import java.rmi.Remote;

public interface RpcDemoGoodsService extends Remote {
    void hello();

    void hi();
}
