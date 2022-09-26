package dev.shefer.searchengine.engine.util

interface Progress {

    /**
     * Returns value between 0 and 1
     * where 0 is not started and 1 is fully complete.
     */
    fun report(): Double

    /**
     * Try to stop task execution as soon as
     * possible without awaiting completion.
     */
    fun cancel()

    /**
     * Blocking wait until progress if completed
     */
    fun join()

}
