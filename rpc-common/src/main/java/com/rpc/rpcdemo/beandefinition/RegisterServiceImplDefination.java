package com.rpc.rpcdemo.beandefinition;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class RegisterServiceImplDefination implements Serializable {
    private String ip;
    private String port;
    private String interfaceImplClassName;
    private List<String> interfaceClassNameList;
    private List<String> methodNameList;

}
