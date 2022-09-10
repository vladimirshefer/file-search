package dev.shefer.searchengine.engine.analysis

import dev.shefer.searchengine.indexing.filter.TokenFilter
import dev.shefer.searchengine.indexing.tokenizer.Tokenizer

class Analyzer(
    val tokenizer: () -> Tokenizer,
    val tokenFilters: List<TokenFilter>,
)
