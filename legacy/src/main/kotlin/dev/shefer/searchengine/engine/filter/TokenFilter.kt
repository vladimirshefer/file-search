package dev.shefer.searchengine.engine.filter

/**
 * Allows to change tokens before being indexed.
 * See LowercaseTokenFilter, which lowercases all tokens
 * to make them case insensitive during the search.
 */
interface TokenFilter {

    /**
     * Return either same token or token replacement or null.
     * Null means the token should be skipped and not indexed.
     */
    fun filter(token: String) : String?

}
