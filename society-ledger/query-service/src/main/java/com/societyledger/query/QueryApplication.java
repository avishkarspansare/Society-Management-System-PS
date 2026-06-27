package com.societyledger.query;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class QueryApplication {
    public static void main(String[] args) {
        SpringApplication.run(QueryApplication.class, args);
    }
}
