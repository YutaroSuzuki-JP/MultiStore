package io.github.yutarosuzuki_jp.multistore

import kotlin.test.Test

class IosMultiStoreTest {
    @Test
    fun testStandardStore() {
        val factory = IosMultiStoreFactory()
        val store = factory.create("test_standard_store")
        runMultiStoreTests(store)
    }

    @Test
    fun testSecureStore() {
        val factory = IosMultiStoreFactory()
        val store = factory.createSecure("test_secure_store")
        runMultiStoreTests(store)
    }
}
