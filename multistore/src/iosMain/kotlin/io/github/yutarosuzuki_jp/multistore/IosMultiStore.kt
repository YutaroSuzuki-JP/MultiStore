package io.github.yutarosuzuki_jp.multistore

import platform.Foundation.NSUserDefaults

class IosMultiStore(
    private val name: String
) : MultiStore {
    private val userDefaults = NSUserDefaults.standardUserDefaults

    private fun getStorageKey(key: String): String = "multistore_${name}_$key"

    override fun getString(key: String): String? = userDefaults.stringForKey(getStorageKey(key))
    
    override fun putString(key: String, value: String?) {
        val sKey = getStorageKey(key)
        if (value == null) {
            userDefaults.removeObjectForKey(sKey)
        } else {
            userDefaults.setObject(value, forKey = sKey)
        }
    }

    override fun getInt(key: String): Int? {
        val sKey = getStorageKey(key)
        return if (hasKey(key)) userDefaults.integerForKey(sKey).toInt() else null
    }
    
    override fun putInt(key: String, value: Int?) {
        val sKey = getStorageKey(key)
        if (value == null) {
            userDefaults.removeObjectForKey(sKey)
        } else {
            userDefaults.setInteger(value.toLong(), forKey = sKey)
        }
    }

    override fun getLong(key: String): Long? {
        val sKey = getStorageKey(key)
        return if (hasKey(key)) userDefaults.integerForKey(sKey) else null
    }
    
    override fun putLong(key: String, value: Long?) {
        val sKey = getStorageKey(key)
        if (value == null) {
            userDefaults.removeObjectForKey(sKey)
        } else {
            userDefaults.setInteger(value, forKey = sKey)
        }
    }

    override fun getFloat(key: String): Float? {
        val sKey = getStorageKey(key)
        return if (hasKey(key)) userDefaults.floatForKey(sKey) else null
    }
    
    override fun putFloat(key: String, value: Float?) {
        val sKey = getStorageKey(key)
        if (value == null) {
            userDefaults.removeObjectForKey(sKey)
        } else {
            userDefaults.setFloat(value, forKey = sKey)
        }
    }

    override fun getBoolean(key: String): Boolean? {
        val sKey = getStorageKey(key)
        return if (hasKey(key)) userDefaults.boolForKey(sKey) else null
    }
    
    override fun putBoolean(key: String, value: Boolean?) {
        val sKey = getStorageKey(key)
        if (value == null) {
            userDefaults.removeObjectForKey(sKey)
        } else {
            userDefaults.setBool(value, forKey = sKey)
        }
    }

    override fun remove(key: String) {
        userDefaults.removeObjectForKey(getStorageKey(key))
    }

    override fun clear() {
        val prefix = "multistore_${name}_"
        val dictionary = userDefaults.dictionaryRepresentation()
        dictionary.keys.forEach { key ->
            if (key is String && key.startsWith(prefix)) {
                userDefaults.removeObjectForKey(key)
            }
        }
    }

    override fun hasKey(key: String): Boolean {
        return userDefaults.objectForKey(getStorageKey(key)) != null
    }
}
