package com.seguradora.consistente;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableStateMachine
@EnableTransactionManagement
public class ArquiteturaConsistenteApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArquiteturaConsistenteApplication.class, args);
    }
}