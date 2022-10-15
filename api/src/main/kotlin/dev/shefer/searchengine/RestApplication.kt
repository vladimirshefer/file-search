package dev.shefer.searchengine

import dev.shefer.searchengine.optimize.MediaOptimizationManager
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import java.nio.file.Path

@SpringBootApplication
class RestApplication {
    @Bean
    fun mediaOptimizationManager(
        @Value("\${app.sourceMediaRootPath}")
        sourceMediaRootPath: String,
        @Value("\${app.optimizedMediaRootPath}")
        optimizedMediaRootPath: String
    ): MediaOptimizationManager {
        return MediaOptimizationManager(
            Path.of(sourceMediaRootPath),
            Path.of(optimizedMediaRootPath)
        )
    }
}

fun main() {
    runApplication<RestApplication>()
}
