package io.github.yutarosuzuki_jp.multistore

class WebSecureMultiStore(
    private val name: String,
    private val config: WebSecurityConfig
) : MultiStore {

    private val storage: Storage
        get() = if (config.useSessionStorage) window.sessionStorage else window.localStorage

    private fun getStorageKey(key: String): String {
        val hashedKey = if (config.obfuscate) base64Encode(key) else key
        return "multistore_secure_${name}_$hashedKey"
    }

    override fun getString(key: String): String? {
        val raw = storage.getItem(getStorageKey(key)) ?: return null
        return if (config.obfuscate) {
            try {
                base64Decode(raw)
            } catch (e: Exception) {
                null
            }
        } else {
            raw
        }
    }

    override fun putString(key: String, value: String?) {
        val sKey = getStorageKey(key)
        if (value == null) {
            storage.removeItem(sKey)
            return
        }
        val encoded = if (config.obfuscate) base64Encode(value) else value
        storage.setItem(sKey, encoded)
    }

    override fun getInt(key: String): Int? = getString(key)?.toIntOrNull()
    override fun putInt(key: String, value: Int?) = putString(key, value?.toString())

    override fun getLong(key: String): Long? = getString(key)?.toLongOrNull()
    override fun putLong(key: String, value: Long?) = putString(key, value?.toString())

    override fun getFloat(key: String): Float? = getString(key)?.toFloatOrNull()
    override fun putFloat(key: String, value: Float?) = putString(key, value?.toString())

    override fun getBoolean(key: String): Boolean? = getString(key)?.toBooleanStrictOrNull()
    override fun putBoolean(key: String, value: Boolean?) = putString(key, value?.toString())

    override fun remove(key: String) {
        storage.removeItem(getStorageKey(key))
    }

    override fun clear() {
        val prefix = "multistore_secure_${name}_"
        val keysToRemove = mutableListOf<String>()
        for (i in 0 until storage.length) {
            val key = storage.key(i)
            if (key != null && key.startsWith(prefix)) {
                keysToRemove.add(key)
            }
        }
        keysToRemove.forEach { storage.removeItem(it) }
    }

    override fun hasKey(key: String): Boolean {
        return storage.getItem(getStorageKey(key)) != null
    }
}
