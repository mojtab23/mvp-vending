package com.javidconsulting.mvpvending

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MvpVendingApplication

fun main(args: Array<String>) {
    runApplication<MvpVendingApplication>(*args)
}
