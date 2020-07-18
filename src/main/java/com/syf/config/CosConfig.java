package com.syf.config;

import com.syf.utils.CosUtils;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @Author syf
 * @Date 2020/7/16 17:08
 */
@Configuration
@Data
public class CosConfig {
    @Value("${cos.secretId}")
    private  String secretId ;

    @Value("${cos.secretKey}")
    private  String secretKey;

    @Value("${cos.localhost}")
    private  String localhost ;

    @Value("${cos.bucketName}")
    private  String bucketName;

    @Value("${cos.region}")
    private  String region;

    @PostConstruct
    public void init(){
        CosUtils.initConfigInfo(this);
    }
}
