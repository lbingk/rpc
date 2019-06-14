package com.rpc.rpcdemo.service;


import java.rmi.Remote;

public interface RpcDemoEmployeService extends Remote {
    void hello();

    void hi();
}
