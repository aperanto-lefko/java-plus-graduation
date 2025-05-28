package ru.practicum;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"ru.practicum", "ru.practicum.client"})
public class MainApplication {
    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }
    @PostConstruct
    public void logPort() {
        System.out.println("Actual port: " + System.getProperty("server.port"));
    }
}