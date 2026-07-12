package io.github.yutarosuzuki_jp.multistore

import androidx.test.core.app.ApplicationProvider
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AndroidMultiStoreTest {

    @Test
    fun testStandardStore() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val factory = AndroidMultiStoreFactory(context, isTest = true)
        val store = factory.create("test_standard_store")
        runMultiStoreTests(store)
    }

    @Test
    fun testSecureStore() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val factory = AndroidMultiStoreFactory(context, isTest = true)
        val store = factory.createSecure("test_secure_store")
        runMultiStoreTests(store)
    }
}
