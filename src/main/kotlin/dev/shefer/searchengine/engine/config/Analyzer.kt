package dev.shefer.searchengine.engine.config

import dev.shefer.searchengine.engine.filter.TokenFilter
import dev.shefer.searchengine.engine.tokenizer.Tokenizer

class Analyzer(
    val tokenizer: () -> Tokenizer,
    val tokenFilters: List<TokenFilter>,
)

fun Analyzer.analyze(text: String): List<String> {
    val tokenizer1 = tokenizer()
    val result = ArrayList<String>()
    for (character in text) {
        tokenizer1
            .next(character)
            ?.let { token -> filterToken(token) }
            ?.let { token -> result.add(token) }
    }
    return result
}

fun Analyzer.filterToken(token: String?): String? {
    return tokenFilters.fold(token) { t, tf ->
        t?.let { it: String -> tf.filter(it) }
    }
}
