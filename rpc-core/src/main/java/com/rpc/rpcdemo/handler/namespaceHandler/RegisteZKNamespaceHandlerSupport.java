package com.rpc.rpcdemo.handler.namespaceHandler;

import com.rpc.rpcdemo.xmlparser.RegisteZKDefinationParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/*
   此类定义：解析RegisteZKDefinationParser的入口
 */
public class RegisteZKNamespaceHandlerSupport extends NamespaceHandlerSupport {
    @Override
    public void init() {
        registerBeanDefinitionParser("zkCustom", new RegisteZKDefinationParser());
    }
}
