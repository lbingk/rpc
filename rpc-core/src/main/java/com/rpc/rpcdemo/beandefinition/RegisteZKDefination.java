package com.rpc.rpcdemo.beandefinition;

import lombok.Data;

@Data
public class RegisteZKDefination {
    private String ip;
    private int port;
    private int timeout;
}
