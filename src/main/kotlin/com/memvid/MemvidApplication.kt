package com.memvid

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MemvidApplication

fun main(args: Array<String>) {
    runApplication<MemvidApplication>(*args)
} 