package dev.shefer.searchengine.indexing.tokenizer

/**
 * The standard tokenizer divides text into terms on word boundaries.
 * It removes most punctuation symbols.
 * It is the best choice for most languages.
 */
class StandardTokenizer(
    delimitersString: String
) : Tokenizer {

    private val delimiters = delimitersString.toSet()

    private var buffer = StringBuilder()

    /**
     * Not thread safe
     */
    override fun next(char: Char): String? {
        if (char in delimiters) {
            if (buffer.isEmpty()) {
                return null
            }
            val result = buffer.toString()
            buffer = StringBuilder()
            return result
        }

        buffer.append(char)
        return null
    }

}
