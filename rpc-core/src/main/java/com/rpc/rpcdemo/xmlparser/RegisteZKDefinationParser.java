package com.rpc.rpcdemo.xmlparser;


import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/*
   此类来解析：与注册中心的配置
 */
public class RegisteZKDefinationParser  implements BeanDefinitionParser {
    private final Class<?> beanClass;

    public RegisteZKDefinationParser(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        RootBeanDefinition beanDefinition = new RootBeanDefinition();
        beanDefinition.setBeanClass(beanClass);
        beanDefinition.setLazyInit(false);
        beanDefinition.getPropertyValues().add("ip", element.getAttribute("ip"));
        beanDefinition.getPropertyValues().add("port", element.getAttribute("port"));
        beanDefinition.getPropertyValues().add("timeout", element.getAttribute("timeout"));
        BeanDefinitionRegistry beanDefinitionRegistry = parserContext.getRegistry();
        //注册bean到BeanDefinitionRegistry中
        beanDefinitionRegistry.registerBeanDefinition(beanClass.getName(), beanDefinition);
        return beanDefinition;
    }
}
