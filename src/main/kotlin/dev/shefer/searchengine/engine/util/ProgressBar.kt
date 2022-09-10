package dev.shefer.searchengine.engine.util

class ProgressBar(
    var message: String,
    initialValue: Int,
    var maxValue: Int,
    private val cancel: () -> Unit = {}
) {

    var value = initialValue


    fun show(){
        println("$value of $maxValue: $message");
    }

}
