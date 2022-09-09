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
