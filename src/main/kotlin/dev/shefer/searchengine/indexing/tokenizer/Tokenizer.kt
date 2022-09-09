package dev.shefer.searchengine.indexing.tokenizer

interface Tokenizer {
    /**
     * Return token if delimiter met.
     * Return null if no token available.
     */
    fun next(char: Char): String?
}
