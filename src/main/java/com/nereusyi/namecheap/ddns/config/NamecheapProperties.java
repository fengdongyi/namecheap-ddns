package com.nereusyi.namecheap.ddns.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "namecheap")
public class NamecheapProperties {

    private String host;
    private String domain;
    private String password;

}
