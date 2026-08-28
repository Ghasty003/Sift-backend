package com.sift;

import org.springframework.boot.SpringApplication;

public class TestSiftApplication {

    public static void main(String[] args) {
        SpringApplication.from(SiftApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
