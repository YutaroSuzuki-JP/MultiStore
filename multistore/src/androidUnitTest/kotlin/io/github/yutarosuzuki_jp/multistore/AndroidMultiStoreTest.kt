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

    private fun isAndroidKeyStoreAvailable(): Boolean {
        return try {
            java.security.KeyStore.getInstance("AndroidKeyStore")
            true
        } catch (e: Exception) {
            false
        }
    }

    @Test
    fun testActualSecureStoreEncryptedSharedPreferences() {
        if (!isAndroidKeyStoreAvailable()) return
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val factory = AndroidMultiStoreFactory(
            context = context,
            securityConfigProvider = { name ->
                AndroidSecurityConfig.EncryptedSharedPreferences(
                    name = "${name}_encrypted_prefs",
                    useStrongBox = true // Triggers StrongBox fallback check
                )
            },
            isTest = false
        )
        val store = factory.createSecure("test_secure_store_eps")
        runMultiStoreTests(store)
    }

    @Test
    fun testActualSecureStoreRsaHybridFile() {
        if (!isAndroidKeyStoreAvailable()) return
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val factory = AndroidMultiStoreFactory(
            context = context,
            securityConfigProvider = { name ->
                AndroidSecurityConfig.RsaHybridFile(
                    alias = "test_rsa_key_$name",
                    directoryName = "test_rsa_dir_$name",
                    useStrongBox = true // Triggers StrongBox fallback check
                )
            },
            isTest = false
        )
        val store = factory.createSecure("test_secure_store_rsa")
        runMultiStoreTests(store)
    }

    @Test
    fun testActualSecureStoreAesFile() {
        if (!isAndroidKeyStoreAvailable()) return
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val factory = AndroidMultiStoreFactory(
            context = context,
            securityConfigProvider = { name ->
                AndroidSecurityConfig.AesFile(
                    alias = "test_aes_key_$name",
                    directoryName = "test_aes_dir_$name",
                    useStrongBox = true // Triggers StrongBox fallback check
                )
            },
            isTest = false
        )
        val store = factory.createSecure("test_secure_store_aes")
        runMultiStoreTests(store)
    }
}
