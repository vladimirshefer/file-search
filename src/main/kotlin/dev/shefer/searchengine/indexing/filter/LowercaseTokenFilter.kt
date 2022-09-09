package dev.shefer.searchengine.indexing.filter

class LowercaseTokenFilter : TokenFilter {
    override fun filter(token: String): String {
        return token.lowercase()
    }
}
