package com.nereusyi.namecheap.ddns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class NamecheapDDnsApplication {

    public static void main(String[] args) {
        SpringApplication.run(NamecheapDDnsApplication.class, args);
    }

}
