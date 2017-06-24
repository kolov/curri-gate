package com.akolov.currigate

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean


fun main(args: Array<String>) {
    SpringApplication.run(CurriGateApplication::class.java, "--debug")
}

@SpringBootApplication

open class CurriGateApplication() {

    @Bean
    @ConfigurationProperties("oauth2.google.client")
    open fun google(): AuthClient {
        return AuthClient()
    }
}