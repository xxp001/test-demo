package com.supervision.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/*
 * @Project:SupervisionSystem
 * @Description:server config
 * @Author:TjSanshao
 * @Create:2019-02-17 20:39
 *
 **/
@Data
@Component
@ConfigurationProperties(prefix = "custom-server-config")
public class ServerConfig {

    private String fileLocationPath;

}
