package dev.shefer.searchengine.engine.filter

class LowercaseTokenFilter : TokenFilter {
    override fun filter(token: String): String {
        return token.lowercase()
    }
}
