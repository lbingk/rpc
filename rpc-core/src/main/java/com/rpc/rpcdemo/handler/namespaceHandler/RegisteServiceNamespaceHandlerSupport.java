package com.rpc.rpcdemo.handler.namespaceHandler;

import com.rpc.rpcdemo.xmlparser.RegisteServiceDefinationParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/*
   此类定义：解析RegisteServiceDefinationParser的入口
 */
public class RegisteServiceNamespaceHandlerSupport extends NamespaceHandlerSupport {
    @Override
    public void init() {
        registerBeanDefinitionParser("serviceCustom", new RegisteServiceDefinationParser());
    }
}
