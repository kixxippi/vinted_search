package dev.kixxippi.vinted_search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VintedSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(VintedSearchApplication.class, args);
    }
}