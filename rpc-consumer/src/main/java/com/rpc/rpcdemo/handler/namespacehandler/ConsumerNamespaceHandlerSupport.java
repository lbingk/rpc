package com.rpc.rpcdemo.handler.namespacehandler;

import com.rpc.rpcdemo.beandefinition.SubscribeZKDefination;
import com.rpc.rpcdemo.beandefinition.RpcServiceDefination;
import com.rpc.rpcdemo.xmlparser.RpcServiceDefinationParser;
import com.rpc.rpcdemo.xmlparser.SubscribeZKDefinationParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/*
   此类定义了解析利用spring xml自定义扩展的类的入口
 */
public class ConsumerNamespaceHandlerSupport extends NamespaceHandlerSupport {
    @Override
    public void init() {
        registerBeanDefinitionParser("socketCustom",
                new SubscribeZKDefinationParser(SubscribeZKDefination.class));
        registerBeanDefinitionParser("rpcService",
                new RpcServiceDefinationParser(RpcServiceDefination.class));
    }
}
