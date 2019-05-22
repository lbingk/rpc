package com.rpc.rpcdemo;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import java.lang.annotation.Annotation;
import java.util.Map;

// 此类获取spring容器类
public class ApplicationContextHolder {

    private static ApplicationContext applicationContext;

    public static void setApplicationContext(ApplicationContext ctx) throws BeansException {
        if (null == applicationContext) {
            ApplicationContextHolder.applicationContext = ctx;
        }
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }


    public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) {
        return applicationContext.getBeansWithAnnotation(annotationType);
    }

    public <T> T getBean(Class<T> requiredType) {
        return applicationContext.getBean(requiredType);
    }

    // 单例模式
    private static volatile ApplicationContextHolder singleton;

    private ApplicationContextHolder() {
    }

    public static ApplicationContextHolder getInstance() {
        if (singleton == null) {
            synchronized (ApplicationContextHolder.class) {
                if (singleton == null) {
                    singleton = new ApplicationContextHolder();
                }
            }
        }
        return singleton;
    }
}
