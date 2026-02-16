package com.example.hyperskill_ai

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Main application class for the Hyperskill AI service.
 * 
 * This is the entry point for the Spring Boot application.
 */
@SpringBootApplication
class HyperskillAiApplication

/**
 * Main function to start the Spring Boot application.
 * 
 * @param args Command-line arguments passed to the application
 */
fun main(args: Array<String>) {
    runApplication<HyperskillAiApplication>(*args)
}
