package dev.shefer.searchengine.indexing.tokenizer

class TrigramTokenizer : Tokenizer {

    var char1: Char? = null
    var char2: Char? = null

    override fun next(char: Char): String? {
        if (char1 == null) {
            char1 = char
            return null
        }

        if (char2 == null) {
            char2 = char
            return null
        }

        val result = "" + char1 + char2 + char

        char1 = char2
        char2 = char

        return result
    }
}
