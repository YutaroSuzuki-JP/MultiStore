package io.github.yutarosuzuki_jp.multistore

interface MultiStoreFactory {
    fun create(name: String): MultiStore
    fun createSecure(name: String): MultiStore
}
