package io.github.yutarosuzuki_jp.multistore

class WebMultiStore(
    private val name: String,
    private val useSession: Boolean = false
) : MultiStore {

    private val storage: Storage
        get() = if (useSession) window.sessionStorage else window.localStorage

    private fun getStorageKey(key: String): String = "multistore_${name}_$key"

    override fun getString(key: String): String? {
        return storage.getItem(getStorageKey(key))
    }

    override fun putString(key: String, value: String?) {
        val sKey = getStorageKey(key)
        if (value == null) {
            storage.removeItem(sKey)
        } else {
            storage.setItem(sKey, value)
        }
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
        val prefix = "multistore_${name}_"
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
