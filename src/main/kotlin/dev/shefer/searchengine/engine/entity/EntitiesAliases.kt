package dev.shefer.searchengine.engine.entity

typealias LineIndex = MutableMap<Int, MutableList<Int>>
typealias FileIndex = MutableMap<String, LineIndex>
typealias DirectoryIndex = MutableMap<String, FileIndex>
typealias TokenIndex = MutableMap<String, DirectoryIndex>

