package com.rpc.rpcdemo.xmlparser;

import com.rpc.rpcdemo.beandefinition.RegisteZKDefination;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/*
   此类来解析：与注册中心的配置
 */
public class RegisteZKDefinationParser implements BeanDefinitionParser {
    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {

        RootBeanDefinition def = new RootBeanDefinition();
        // 设置Bean Class
        def.setBeanClass(RegisteZKDefination.class);
        // 注册ID属性
        String id = element.getAttribute("ID");
        BeanDefinitionHolder idHolder = new BeanDefinitionHolder(def, id);
        BeanDefinitionReaderUtils.registerBeanDefinition(idHolder,
                parserContext.getRegistry());

        // 注册属性
        String ip = element.getAttribute("ip");
        String port = element.getAttribute("port");

        BeanDefinitionHolder ipHolder = new BeanDefinitionHolder(def, ip);
        BeanDefinitionReaderUtils.registerBeanDefinition(ipHolder,
                parserContext.getRegistry());
        BeanDefinitionHolder portHolder = new BeanDefinitionHolder(def, port);
        BeanDefinitionReaderUtils.registerBeanDefinition(portHolder,
                parserContext.getRegistry());

        def.getPropertyValues().addPropertyValue("url", ip);
        def.getPropertyValues().addPropertyValue("userName", port);
        return def;
    }
}
