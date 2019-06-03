package com.rpc.rpcdemo.handler.namespacehandler;

import com.rpc.rpcdemo.beandefinition.RegisterServiceDefination;
import com.rpc.rpcdemo.beandefinition.RegisterZKDefination;
import com.rpc.rpcdemo.xmlparser.RegisterServiceDefinationParser;
import com.rpc.rpcdemo.xmlparser.RegisterZKDefinationParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/*
   此类定义了解析利用spring xml自定义扩展的类的入口
 */
public class RegisterNamespaceHandlerSupport extends NamespaceHandlerSupport {
    @Override
    public void init() {
        registerBeanDefinitionParser("zkCustom",
                new RegisterZKDefinationParser(RegisterZKDefination.class));
        registerBeanDefinitionParser("serviceCustom",
                new RegisterServiceDefinationParser(RegisterServiceDefination.class));
    }
}
