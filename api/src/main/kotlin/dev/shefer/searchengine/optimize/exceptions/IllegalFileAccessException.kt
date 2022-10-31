package dev.shefer.searchengine.optimize.exceptions

class IllegalFileAccessException(
    message: String = "",
    cause: Throwable? = null
) : RuntimeException(message, cause)
