package dev.shefer.searchengine.controller

import dev.shefer.searchengine.optimize.MediaOptimizationManager
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/processes")
class ProcessController(
    private val mediaOptimizationManager: MediaOptimizationManager
) {

    @GetMapping
    fun list(
        @RequestParam(required = false)
        ids: List<String>?
    ): Any {
        ids ?: return mediaOptimizationManager.currentProcesses.map { it }

        return mediaOptimizationManager.currentProcesses.flatMap { it -> it.children + it }
            .filter { it.id in ids }
    }

}
