package io.github.yutarosuzuki_jp.multistore

import android.content.Context
import android.content.SharedPreferences

class AndroidMultiStore(
    context: Context,
    name: String
) : MultiStore {
    private val prefs: SharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)

    override fun getString(key: String): String? = prefs.getString(key, null)
    override fun putString(key: String, value: String?) {
        prefs.edit().putString(key, value).apply()
    }

    override fun getInt(key: String): Int? {
        return if (prefs.contains(key)) prefs.getInt(key, 0) else null
    }
    override fun putInt(key: String, value: Int?) {
        if (value == null) {
            prefs.edit().remove(key).apply()
        } else {
            prefs.edit().putInt(key, value).apply()
        }
    }

    override fun getLong(key: String): Long? {
        return if (prefs.contains(key)) prefs.getLong(key, 0L) else null
    }
    override fun putLong(key: String, value: Long?) {
        if (value == null) {
            prefs.edit().remove(key).apply()
        } else {
            prefs.edit().putLong(key, value).apply()
        }
    }

    override fun getFloat(key: String): Float? {
        return if (prefs.contains(key)) prefs.getFloat(key, 0.0f) else null
    }
    override fun putFloat(key: String, value: Float?) {
        if (value == null) {
            prefs.edit().remove(key).apply()
        } else {
            prefs.edit().putFloat(key, value).apply()
        }
    }

    override fun getBoolean(key: String): Boolean? {
        return if (prefs.contains(key)) prefs.getBoolean(key, false) else null
    }
    override fun putBoolean(key: String, value: Boolean?) {
        if (value == null) {
            prefs.edit().remove(key).apply()
        } else {
            prefs.edit().putBoolean(key, value).apply()
        }
    }

    override fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }

    override fun clear() {
        prefs.edit().clear().apply()
    }

    override fun hasKey(key: String): Boolean = prefs.contains(key)
}
