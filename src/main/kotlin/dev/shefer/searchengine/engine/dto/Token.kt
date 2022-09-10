package dev.shefer.searchengine.engine.dto

data class Token(
    val token: String,
    val tokenLocation: TokenLocation
) {
    constructor(
        token: String,
        directoryPath: String,
        fileName: String,
        lineId: Int,
        tokenIndex: Int
    )
            : this(
        token,
        TokenLocation(
            LineLocation(
                FileLocation(directoryPath, fileName),
                lineId
            ),
            tokenIndex
        )
    )
}
