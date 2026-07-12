package io.github.yutarosuzuki_jp.multistore

data class IosSecurityConfig(
    val accessible: Accessible = Accessible.AfterFirstUnlock,
    val accessGroup: String? = null,
    val synchronizable: Boolean = false
) {
    enum class Accessible {
        WhenUnlocked,
        AfterFirstUnlock,
        Always
    }
}
