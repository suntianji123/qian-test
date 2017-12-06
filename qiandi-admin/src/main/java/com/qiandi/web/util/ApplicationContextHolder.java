package com.qiandi.web.util;

import javax.annotation.PostConstruct;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.stereotype.Component;


@Component

public class ApplicationContextHolder extends ApplicationObjectSupport {

    public static ApplicationContext applicationContext;

    @PostConstruct
    public void init() {
        ApplicationContextHolder.applicationContext = getApplicationContext();
    }
}
