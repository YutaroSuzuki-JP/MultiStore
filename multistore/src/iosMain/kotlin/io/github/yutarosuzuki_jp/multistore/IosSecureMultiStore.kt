package io.github.yutarosuzuki_jp.multistore

import kotlinx.cinterop.*
import platform.CoreFoundation.*
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.*

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class IosSecureMultiStore(
    private val serviceName: String,
    private val config: IosSecurityConfig
) : MultiStore {

    private fun String.toNSString(): NSString = NSString.create(string = this)

    private fun castObj(obj: Any): COpaquePointer {
        return interpretCPointer<CPointed>(obj.objcPtr())!!
    }

    private fun CFMutableDictionaryRef.applyConfig() {
        val accessibleValue = when (config.accessible) {
            IosSecurityConfig.Accessible.WhenUnlocked -> kSecAttrAccessibleWhenUnlocked
            IosSecurityConfig.Accessible.AfterFirstUnlock -> kSecAttrAccessibleAfterFirstUnlock
            IosSecurityConfig.Accessible.Always -> kSecAttrAccessibleAlways
        }
        CFDictionaryAddValue(this, kSecAttrAccessible, accessibleValue)
        
        config.accessGroup?.let {
            CFDictionaryAddValue(this, kSecAttrAccessGroup, castObj(it.toNSString()))
        }
        
        if (config.synchronizable) {
            CFDictionaryAddValue(this, kSecAttrSynchronizable, kCFBooleanTrue)
        }
    }

    override fun getString(key: String): String? {
        val nsService = serviceName.toNSString()
        val nsKey = key.toNSString()
        
        val query = CFDictionaryCreateMutable(kCFAllocatorDefault, 0, null, null) ?: return null
        try {
            CFDictionaryAddValue(query, kSecClass, kSecClassGenericPassword)
            CFDictionaryAddValue(query, kSecAttrService, castObj(nsService))
            CFDictionaryAddValue(query, kSecAttrAccount, castObj(nsKey))
            CFDictionaryAddValue(query, kSecReturnData, kCFBooleanTrue)
            CFDictionaryAddValue(query, kSecMatchLimit, kSecMatchLimitOne)

            return memScoped {
                val result = alloc<CFTypeRefVar>()
                val status = SecItemCopyMatching(query, result.ptr)
                println("MultiStore: SecItemCopyMatching (getString) status = $status")
                if (status == errSecSuccess) {
                    val ptr = result.value
                    if (ptr != null) {
                        val data = interpretObjCPointer<NSData>(ptr.rawValue)
                        NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString()
                    } else {
                        println("MultiStore: SecItemCopyMatching (getString) result pointer is null")
                        null
                    }
                } else {
                    null
                }
            }
        } finally {
            CFRelease(query)
        }
    }

    override fun putString(key: String, value: String?) {
        if (value == null) {
            remove(key)
            return
        }

        val nsService = serviceName.toNSString()
        val nsKey = key.toNSString()
        val nsData = value.toNSString().dataUsingEncoding(NSUTF8StringEncoding) ?: return

        val exists = hasKey(key)
        println("MultiStore: putString key = '$key', exists = $exists")

        if (exists) {
            val query = CFDictionaryCreateMutable(kCFAllocatorDefault, 0, null, null) ?: return
            val attributesToUpdate = CFDictionaryCreateMutable(kCFAllocatorDefault, 0, null, null) ?: return
            try {
                CFDictionaryAddValue(query, kSecClass, kSecClassGenericPassword)
                CFDictionaryAddValue(query, kSecAttrService, castObj(nsService))
                CFDictionaryAddValue(query, kSecAttrAccount, castObj(nsKey))

                CFDictionaryAddValue(attributesToUpdate, kSecValueData, castObj(nsData))

                val status = SecItemUpdate(query, attributesToUpdate)
                println("MultiStore: SecItemUpdate status = $status")
            } finally {
                CFRelease(query)
                CFRelease(attributesToUpdate)
            }
        } else {
            val query = CFDictionaryCreateMutable(kCFAllocatorDefault, 0, null, null) ?: return
            try {
                CFDictionaryAddValue(query, kSecClass, kSecClassGenericPassword)
                CFDictionaryAddValue(query, kSecAttrService, castObj(nsService))
                CFDictionaryAddValue(query, kSecAttrAccount, castObj(nsKey))
                CFDictionaryAddValue(query, kSecValueData, castObj(nsData))
                query.applyConfig()

                val status = SecItemAdd(query, null)
                println("MultiStore: SecItemAdd status = $status")
            } finally {
                CFRelease(query)
            }
        }
    }

    override fun getInt(key: String): Int? = getString(key)?.toIntOrNull()
    override fun putInt(key: String, value: Int?) = putString(key, value?.toString())

    override fun getLong(key: String): Long? = getString(key)?.toLongOrNull()
    override fun putLong(key: String, value: Long?) = putString(key, value?.toString())

    override fun getFloat(key: String): Float? = getString(key)?.toFloatOrNull()
    override fun putFloat(key: String, value: Float?) = putString(key, value?.toString())

    override fun getDouble(key: String): Double? = getString(key)?.toDoubleOrNull()
    override fun putDouble(key: String, value: Double?) = putString(key, value?.toString())

    override fun getBoolean(key: String): Boolean? = getString(key)?.toBooleanStrictOrNull()
    override fun putBoolean(key: String, value: Boolean?) = putString(key, value?.toString())

    override fun remove(key: String) {
        val nsService = serviceName.toNSString()
        val nsKey = key.toNSString()
        val query = CFDictionaryCreateMutable(kCFAllocatorDefault, 0, null, null) ?: return
        try {
            CFDictionaryAddValue(query, kSecClass, kSecClassGenericPassword)
            CFDictionaryAddValue(query, kSecAttrService, castObj(nsService))
            CFDictionaryAddValue(query, kSecAttrAccount, castObj(nsKey))

            val status = SecItemDelete(query)
            println("MultiStore: SecItemDelete (remove) status = $status")
        } finally {
            CFRelease(query)
        }
    }

    override fun clear() {
        val nsService = serviceName.toNSString()
        val query = CFDictionaryCreateMutable(kCFAllocatorDefault, 0, null, null) ?: return
        try {
            CFDictionaryAddValue(query, kSecClass, kSecClassGenericPassword)
            CFDictionaryAddValue(query, kSecAttrService, castObj(nsService))

            val status = SecItemDelete(query)
            println("MultiStore: SecItemDelete (clear) status = $status")
        } finally {
            CFRelease(query)
        }
    }

    override fun hasKey(key: String): Boolean {
        return getString(key) != null
    }
}
