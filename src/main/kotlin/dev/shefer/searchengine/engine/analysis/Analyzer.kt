package dev.shefer.searchengine.engine.analysis

import dev.shefer.searchengine.engine.filter.TokenFilter
import dev.shefer.searchengine.engine.tokenizer.Tokenizer

class Analyzer(
    val tokenizer: () -> Tokenizer,
    val tokenFilters: List<TokenFilter>,
)
