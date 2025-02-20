package com.lyc.config;

import com.aliyun.teaopenapi.models.Config;
import com.aliyun.dysmsapi20170525.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName AliyunSMSConfig
 * @Description TODO 阿里云SMS配置类
 * @Author LuKey_C
 * @Date 2025/2/20 12:38
 * @Version 1.0
 */
@Configuration
@EnableConfigurationProperties(AliyunSMSProperties.class)
public class AliyunSMSConfig {

    @Autowired
    private AliyunSMSProperties properties;

    @Bean
    public Client smeClient(){
        Config config = new Config();
        config.setAccessKeyId(properties.getAccessKeyId());
        config.setAccessKeySecret(properties.getAccessKeySecret());
        config.setEndpoint(properties.getEndpoint());
        try {
            return new Client(config);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
