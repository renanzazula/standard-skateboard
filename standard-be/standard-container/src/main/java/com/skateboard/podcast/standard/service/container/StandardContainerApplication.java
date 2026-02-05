package com.skateboard.podcast.standard.service.container;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.skateboard.podcast")
public class StandardContainerApplication {

    public static void main(String[] args) {
        SpringApplication.run(StandardContainerApplication.class, args);
    }
}
