package io.github.yutarosuzuki_jp.multistore

class WebMultiStoreFactory(
    private val securityConfigProvider: (String) -> WebSecurityConfig = { _ ->
        WebSecurityConfig()
    }
) : MultiStoreFactory {

    override fun create(name: String): MultiStore {
        return WebMultiStore(name)
    }

    override fun createSecure(name: String): MultiStore {
        return WebSecureMultiStore(name, securityConfigProvider(name))
    }
}
