package io.github.yutarosuzuki_jp.multistore

class IosMultiStoreFactory(
    private val securityConfigProvider: (String) -> IosSecurityConfig = { _ ->
        IosSecurityConfig()
    }
) : MultiStoreFactory {

    override fun create(name: String): MultiStore {
        return IosMultiStore(name)
    }

    override fun createSecure(name: String): MultiStore {
        return IosSecureMultiStore(name, securityConfigProvider(name))
    }
}
