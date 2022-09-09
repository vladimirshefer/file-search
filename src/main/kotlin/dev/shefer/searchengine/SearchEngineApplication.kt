package dev.shefer.searchengine

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SearchEngineApplication

fun main(args: Array<String>) {
	runApplication<SearchEngineApplication>(*args)
}
