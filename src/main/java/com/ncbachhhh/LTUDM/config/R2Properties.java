package com.ncbachhhh.LTUDM.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "r2")
public record R2Properties(
        String endpoint,
        String bucket,
        String accessKey,
        String secretKey,
        String region,
        String publicBaseUrl
) {
}
