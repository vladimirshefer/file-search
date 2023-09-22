package dev.shefer.searchengine.engine.tokenizer

/**
 * Stateful object, which should be created for each text.
 */
interface Tokenizer {
    /**
     * Return token if delimiter met.
     * Return null if no token available.
     */
    fun next(char: Char): String?
}
