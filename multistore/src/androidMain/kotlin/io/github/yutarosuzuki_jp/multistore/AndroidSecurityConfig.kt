package io.github.yutarosuzuki_jp.multistore

sealed class AndroidSecurityConfig {
    /**
     * Standard Android EncryptedSharedPreferences (AES-256 SIV for keys, AES-256 GCM for values).
     */
    data class EncryptedSharedPreferences(
        val name: String,
        val useStrongBox: Boolean = false
    ) : AndroidSecurityConfig()

    /**
     * Direct file-based storage using direct KeyStore RSA asymmetric keys (hybrid encryption).
     */
    data class RsaHybridFile(
        val alias: String,
        val directoryName: String,
        val useStrongBox: Boolean = false
    ) : AndroidSecurityConfig()

    /**
     * Direct file-based storage using direct KeyStore AES symmetric keys (AES-GCM).
     */
    data class AesFile(
        val alias: String,
        val directoryName: String,
        val useStrongBox: Boolean = false
    ) : AndroidSecurityConfig()
}
