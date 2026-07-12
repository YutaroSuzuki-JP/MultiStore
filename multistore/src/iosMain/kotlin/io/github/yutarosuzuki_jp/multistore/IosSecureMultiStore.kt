package io.github.yutarosuzuki_jp.multistore

import kotlinx.cinterop.*
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanFalse
import platform.CoreFoundation.kCFBooleanTrue
import platform.Foundation.NSData
import platform.Foundation.NSMutableDictionary
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Foundation.NSCopyingProtocol
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.SecItemUpdate
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlock
import platform.Security.kSecAttrAccessibleWhenUnlocked
import platform.Security.kSecAttrAccessibleAlways
import platform.Security.kSecAttrAccessGroup
import platform.Security.kSecAttrSynchronizable

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class IosSecureMultiStore(
    private val serviceName: String,
    private val config: IosSecurityConfig
) : MultiStore {

    private fun String.toNSString(): NSString = NSString.create(string = this)

    private fun castKey(key: CPointer<*>?): NSCopyingProtocol {
        return interpretObjCPointer(key!!.rawValue)
    }

    private fun NSMutableDictionary.applyConfig() {
        val accessibleValue = when (config.accessible) {
            IosSecurityConfig.Accessible.WhenUnlocked -> kSecAttrAccessibleWhenUnlocked
            IosSecurityConfig.Accessible.AfterFirstUnlock -> kSecAttrAccessibleAfterFirstUnlock
            IosSecurityConfig.Accessible.Always -> kSecAttrAccessibleAlways
        }
        setObject(accessibleValue, forKey = castKey(kSecAttrAccessible))
        
        config.accessGroup?.let {
            setObject(it.toNSString(), forKey = castKey(kSecAttrAccessGroup))
        }
        
        if (config.synchronizable) {
            setObject(kCFBooleanTrue, forKey = castKey(kSecAttrSynchronizable))
        } else {
            setObject(kCFBooleanFalse, forKey = castKey(kSecAttrSynchronizable))
        }
    }

    override fun getString(key: String): String? {
        val query = NSMutableDictionary().apply {
            setObject(kSecClassGenericPassword, forKey = castKey(kSecClass))
            setObject(serviceName.toNSString(), forKey = castKey(kSecAttrService))
            setObject(key.toNSString(), forKey = castKey(kSecAttrAccount))
            setObject(kCFBooleanTrue, forKey = castKey(kSecReturnData))
            setObject(kSecMatchLimitOne, forKey = castKey(kSecMatchLimit))
            applyConfig()
        }

        return memScoped {
            val result = alloc<CFTypeRefVar>()
            val status = SecItemCopyMatching(query as CFDictionaryRef, result.ptr)
            if (status == errSecSuccess) {
                val ptr = result.value
                if (ptr != null) {
                    val data = interpretObjCPointer<NSData>(ptr.rawValue)
                    NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString()
                } else {
                    null
                }
            } else {
                null
            }
        }
    }

    override fun putString(key: String, value: String?) {
        if (value == null) {
            remove(key)
            return
        }

        val data = value.toNSString().dataUsingEncoding(NSUTF8StringEncoding) ?: return

        if (hasKey(key)) {
            val query = NSMutableDictionary().apply {
                setObject(kSecClassGenericPassword, forKey = castKey(kSecClass))
                setObject(serviceName.toNSString(), forKey = castKey(kSecAttrService))
                setObject(key.toNSString(), forKey = castKey(kSecAttrAccount))
                applyConfig()
            }
            val attributesToUpdate = NSMutableDictionary().apply {
                setObject(data, forKey = castKey(kSecValueData))
            }
            SecItemUpdate(query as CFDictionaryRef, attributesToUpdate as CFDictionaryRef)
        } else {
            val query = NSMutableDictionary().apply {
                setObject(kSecClassGenericPassword, forKey = castKey(kSecClass))
                setObject(serviceName.toNSString(), forKey = castKey(kSecAttrService))
                setObject(key.toNSString(), forKey = castKey(kSecAttrAccount))
                setObject(data, forKey = castKey(kSecValueData))
                applyConfig()
            }
            SecItemAdd(query as CFDictionaryRef, null)
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
        val query = NSMutableDictionary().apply {
            setObject(kSecClassGenericPassword, forKey = castKey(kSecClass))
            setObject(serviceName.toNSString(), forKey = castKey(kSecAttrService))
            setObject(key.toNSString(), forKey = castKey(kSecAttrAccount))
            applyConfig()
        }
        SecItemDelete(query as CFDictionaryRef)
    }

    override fun clear() {
        val query = NSMutableDictionary().apply {
            setObject(kSecClassGenericPassword, forKey = castKey(kSecClass))
            setObject(serviceName.toNSString(), forKey = castKey(kSecAttrService))
            applyConfig()
        }
        SecItemDelete(query as CFDictionaryRef)
    }

    override fun hasKey(key: String): Boolean {
        val query = NSMutableDictionary().apply {
            setObject(kSecClassGenericPassword, forKey = castKey(kSecClass))
            setObject(serviceName.toNSString(), forKey = castKey(kSecAttrService))
            setObject(key.toNSString(), forKey = castKey(kSecAttrAccount))
            setObject(kSecMatchLimitOne, forKey = castKey(kSecMatchLimit))
            applyConfig()
        }
        val status = SecItemCopyMatching(query as CFDictionaryRef, null)
        return status == errSecSuccess
    }
}
