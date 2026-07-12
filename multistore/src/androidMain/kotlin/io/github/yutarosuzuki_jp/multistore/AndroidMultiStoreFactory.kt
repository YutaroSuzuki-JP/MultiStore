package io.github.yutarosuzuki_jp.multistore

import android.content.Context

class AndroidMultiStoreFactory(
    private val context: Context,
    private val securityConfigProvider: (String) -> AndroidSecurityConfig = { name ->
        AndroidSecurityConfig.RsaHybridFile(
            alias = "multistore_key_$name",
            directoryName = "multistore_secure_$name"
        )
    },
    private val isTest: Boolean = false
) : MultiStoreFactory {

    override fun create(name: String): MultiStore {
        return AndroidMultiStore(context, name)
    }

    override fun createSecure(name: String): MultiStore {
        return if (isTest) {
            AndroidMultiStore(context, "${name}_secure_test")
        } else {
            AndroidSecureMultiStore(context, securityConfigProvider(name))
        }
    }
}
