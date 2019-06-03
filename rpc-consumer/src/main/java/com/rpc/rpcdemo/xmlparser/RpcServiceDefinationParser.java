package com.rpc.rpcdemo.xmlparser;


import com.google.common.collect.Maps;
import com.rpc.rpcdemo.ApplicationContextHolder;
import com.rpc.rpcdemo.RpcServiceDefinationContext;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import java.util.HashMap;

/*
   此类来解析：与注册中心的配置
 */
public class RpcServiceDefinationParser implements BeanDefinitionParser {


    private final Class<?> beanClass;

    public RpcServiceDefinationParser(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        RootBeanDefinition beanDefinition = new RootBeanDefinition();
        beanDefinition.setBeanClass(beanClass);
        beanDefinition.setLazyInit(false);
        beanDefinition.getPropertyValues().add("serviceClassName", element.getAttribute("serviceClassName"));
        beanDefinition.getPropertyValues().add("ref", element.getAttribute("ref"));
        BeanDefinitionRegistry beanDefinitionRegistry = parserContext.getRegistry();
        beanDefinitionRegistry.registerBeanDefinition(element.getAttribute("ref"), beanDefinition);

        return beanDefinition;
    }
}
