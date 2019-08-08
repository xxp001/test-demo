package com.supervision.user.config;

import com.github.pagehelper.PageHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * @ClassName pageConfig
 * @Description TODO
 * @Author fangyong
 * @Date 2019/2/2 12:37
 **/
@Configuration
public class pageConfig {
    @Bean
    public PageHelper pageHelper(){
        PageHelper pageHelper = new PageHelper();
        Properties properties = new Properties();
        properties.setProperty("offsetAsPageNum","true");
        properties.setProperty("rowBoundsWithCount","true");
        properties.setProperty("reasonable","true");
        properties.setProperty("dialect","oracle");    //配置oracle数据库的方言
        pageHelper.setProperties(properties);
        return pageHelper;
    }
}
