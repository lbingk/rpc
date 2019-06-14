package com.rpc.rpcdemo.service;


import java.rmi.Remote;

public interface RpcDemoUserService extends Remote {
    void hello();

    void hi();
}
