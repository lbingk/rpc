package com.rpc.rpcdemo.handler.namespaceHandler;

import com.rpc.rpcdemo.beandefinition.RegisteServiceDefination;
import com.rpc.rpcdemo.beandefinition.RegisteZKDefination;
import com.rpc.rpcdemo.xmlparser.RegisteServiceDefinationParser;
import com.rpc.rpcdemo.xmlparser.RegisteZKDefinationParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/*
   此类定义：解析RegisteDefinationParser的入口
 */
public class RegisteNamespaceHandlerSupport extends NamespaceHandlerSupport {
    @Override
    public void init() {
        registerBeanDefinitionParser("serviceCustom",
                new RegisteServiceDefinationParser(RegisteServiceDefination.class));
        registerBeanDefinitionParser("zkCustom",
                new RegisteZKDefinationParser(RegisteZKDefination.class));
    }
}
