package com.akolov.currigate

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication


fun main(args: Array<String>) {
    SpringApplication.run(CurriGateApplication::class.java, "--debug")
}

@SpringBootApplication
open class CurriGateApplication() {


}