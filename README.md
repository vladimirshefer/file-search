# Library for simple text search.

This library is supposed to consist of two parts: text index builder and search query executor.

#### Text index builder should:

- Be able to build a text index for a given folder in a file system.
- Show progress while building the index.
- Build the index using several threads in parallel.
- Be cancellable. It should be possible to interrupt indexing.
- (Optional) Be incremental. It would be nice if the builder would be able to listen to the file system changes and update the index accordingly.

#### Search query executor should:

- Find a position in files for a given string.

- Be able to process search requests in parallel.

Please also cover the library with a set of unit-tests. Your code should not use third-party indexing libraries.


## Examples

- Create SearchEngine instance. 
```kotlin
val searchEngine = SearchEngine(IndexSettings(
    sourceDir = "./src/main",
    dataDir = "./index_data",
    analyzer = Analyzer.TRIGRAM_CASEINSENSITIVE
))
```

- Build a text index for a given folder in a 
  file system using several threads in parallel. 
- Indexing is asynchronous, but you could wait for completion.
```kotlin
val indexProgress = searchEngine.rebuildIndex()
indexProgress.join() // wait for index to fully complete
```

- Show progress while building the index. It is possible to interrupt indexing.
```kotlin
val indexProgress = searchEngine.rebuildIndex()
// wait until 50% is ready
while (indexProgress.report() < 0.5) {
    println(indexProgress.report())
    Thread.sleep(50)
}
// then cancel indexing
indexProgress.cancel() // wait for index to fully complete
```

- Find a position in files for a given string. 
  You can run search requests in parallel.
```kotlin
searchEngine.search("Rec")
```

## Main concepts

### Inverted index
We build an inverted index for text files in the specified directory.

Inverted index is a map, where the key is the string token and the value is a set of locations of this token in files.



### Parallel indexing

We process each file in separate thread. The indexing is asynchronous. 
CompletableFuture-s are used with shared common thread pool.
