package com.rpc.rpcdemo.xmlparser;

import com.rpc.rpcdemo.beandefinition.RegisteZKDefination;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class RegisteDefinationParserCommon implements BeanDefinitionParser {
    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {

        RootBeanDefinition def = new RootBeanDefinition();
        // 设置Bean Class
        def.setBeanClass(RegisteZKDefination.class);
        // 注册ID属性
        String id = element.getAttribute("id");
        BeanDefinitionHolder idHolder = new BeanDefinitionHolder(def, id);
        BeanDefinitionReaderUtils.registerBeanDefinition(idHolder,
                parserContext.getRegistry());

        // 注册属性
        String ip = element.getAttribute("ip");
        String port = element.getAttribute("port");
        String timeout = element.getAttribute("timeout");

        BeanDefinitionHolder ipHolder = new BeanDefinitionHolder(def, ip);
        BeanDefinitionReaderUtils.registerBeanDefinition(ipHolder,
                parserContext.getRegistry());
        BeanDefinitionHolder portHolder = new BeanDefinitionHolder(def, port);
        BeanDefinitionReaderUtils.registerBeanDefinition(portHolder,
                parserContext.getRegistry());
        BeanDefinitionHolder timeoutHolder = new BeanDefinitionHolder(def, timeout);
        BeanDefinitionReaderUtils.registerBeanDefinition(timeoutHolder,
                parserContext.getRegistry());

        def.getPropertyValues().addPropertyValue("ip", ip);
        def.getPropertyValues().addPropertyValue("port", port);
        def.getPropertyValues().addPropertyValue("timeout", timeout);
        return def;
    }
}
