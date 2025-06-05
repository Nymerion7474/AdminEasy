package com.admineasy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AdminEasyApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdminEasyApplication.class, args);
    }
}
