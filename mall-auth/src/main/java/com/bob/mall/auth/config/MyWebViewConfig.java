package com.bob.mall.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.OverridingMethodsMustInvokeSuper;

@Configuration
public class MyWebViewConfig implements WebMvcConfigurer {

    /**
     * 视图映射器
     * @param registry
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {

        registry.addViewController("/login.html").setViewName("/login");
        registry.addViewController("/reg.html").setViewName("/reg");
    }
}
