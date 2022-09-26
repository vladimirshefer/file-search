package dev.shefer.searchengine.engine.filter

/**
 * Replaces all uppercase symbols in token to lowercase.
 * Basically, used to make search case-insensitive.
 */
class LowercaseTokenFilter : TokenFilter {
    override fun filter(token: String): String {
        return token.lowercase()
    }
}
