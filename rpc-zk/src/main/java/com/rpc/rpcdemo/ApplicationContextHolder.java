package com.rpc.rpcdemo;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import java.lang.annotation.Annotation;
import java.util.Map;

public class ApplicationContextHolder {

    private static ApplicationContext applicationContext;

    public static void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextHolder.applicationContext = applicationContext;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }


    public static Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) {
        return applicationContext.getBeansWithAnnotation(annotationType);
    }

    public static <T> T getBean(Class<T> requiredType) {
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
