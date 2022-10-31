package dev.shefer.searchengine

import dev.shefer.searchengine.optimize.FileSystemSubtree
import dev.shefer.searchengine.optimize.MediaOptimizationManager
import dev.shefer.searchengine.optimize.MediaOptimizer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import java.nio.file.Path
import kotlin.io.path.createDirectories

@SpringBootApplication
class RestApplication {
    @Bean
    fun mediaOptimizationManager(
        @Value("\${app.sourceMediaRootPath}")
        sourceMediaRootPath: String,
        @Value("\${app.optimizedMediaRootPath}")
        optimizedMediaRootPath: String,
        @Value("\${app.internalDataRootPath}")
        internalDataRootPath: String,
        mediaOptimizer: MediaOptimizer
    ): MediaOptimizationManager {
        val sourceMediaRoot = Path.of(sourceMediaRootPath)
            .also { it.createDirectories() }
        val optimizedMediaRoot = Path.of(optimizedMediaRootPath)
            .also { it.createDirectories() }
        val internalDataRoot = Path.of(internalDataRootPath)
            .also { it.createDirectories() }
        val thumbnailsMediaRoot = internalDataRoot.resolve("thumbnails")
            .also { it.createDirectories() }

        return MediaOptimizationManager(
            mediaOptimizer,
            sourceMediaRoot,
            optimizedMediaRoot,
            thumbnailsMediaRoot,
        )
    }

    @Qualifier("sourceSubtree")
    @Bean
    fun sourceMediaSubtree(
        @Value("\${app.sourceMediaRootPath}")
        sourceMediaRootPath: String,
    ) = FileSystemSubtree.of(sourceMediaRootPath)

}

fun main() {
    runApplication<RestApplication>()
}
