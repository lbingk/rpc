package com.rpc.rpcdemo.beandefinition;

import lombok.Data;

import java.io.Serializable;

@Data
public class RegisteServiceDefination implements Serializable {
    private String ip;
    private int port;
    private int timeout;
}
