package com.supervision;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.supervision")
@MapperScan(basePackages = {"com.supervision.document.mapper", "com.supervision.project.mapper", "com.supervision.user.mapper", "com.supervision.library.mapper"})
public class SupervisionApplication {

    public static void main(String[] args) {
        SpringApplication.run(SupervisionApplication.class, args);
    }

}

