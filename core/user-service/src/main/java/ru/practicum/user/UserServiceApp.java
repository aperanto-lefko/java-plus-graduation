package ru.practicum.user;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@SpringBootApplication(scanBasePackages = "ru.practicum.user")
@EnableDiscoveryClient
@ConfigurationPropertiesScan
public class UserServiceApp {
    @Autowired
    Environment env;
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApp.class, args);
    }

}