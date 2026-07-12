package io.github.yutarosuzuki_jp.multistore

interface MultiStore {
    fun getString(key: String): String?
    fun putString(key: String, value: String?)
    
    fun getInt(key: String): Int?
    fun putInt(key: String, value: Int?)
    
    fun getLong(key: String): Long?
    fun putLong(key: String, value: Long?)
    
    fun getFloat(key: String): Float?
    fun putFloat(key: String, value: Float?)
    
    fun getBoolean(key: String): Boolean?
    fun putBoolean(key: String, value: Boolean?)
    
    fun remove(key: String)
    fun clear()
    fun hasKey(key: String): Boolean
}
