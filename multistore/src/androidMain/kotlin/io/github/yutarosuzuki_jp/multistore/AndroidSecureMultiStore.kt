package io.github.yutarosuzuki_jp.multistore

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.io.File
import java.nio.ByteBuffer
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import java.security.spec.MGF1ParameterSpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource
import javax.crypto.spec.SecretKeySpec

class AndroidSecureMultiStore(
    context: Context,
    private val config: AndroidSecurityConfig
) : MultiStore {

    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    // SharedPreferences implementation (used if EncryptedSharedPreferences mode)
    private var sharedPrefs: SharedPreferences? = null

    // File-based implementation properties
    private var directory: File? = null
    private var keyAlias: String? = null

    init {
        when (config) {
            is AndroidSecurityConfig.EncryptedSharedPreferences -> {
                val masterKey = try {
                    if (config.useStrongBox && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                        MasterKey.Builder(context)
                            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                            .setRequestStrongBoxBacked(true)
                            .build()
                    } else {
                        MasterKey.Builder(context)
                            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                            .build()
                    }
                } catch (e: Exception) {
                    if (config.useStrongBox) {
                        MasterKey.Builder(context)
                            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                            .build()
                    } else {
                        throw e
                    }
                }
                sharedPrefs = EncryptedSharedPreferences.create(
                    context,
                    config.name,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            }
            is AndroidSecurityConfig.RsaHybridFile -> {
                directory = context.filesDir.resolve(config.directoryName).apply { mkdirs() }
                keyAlias = config.alias
                ensureRsaKeyPair(config.alias, config.useStrongBox)
            }
            is AndroidSecurityConfig.AesFile -> {
                directory = context.filesDir.resolve(config.directoryName).apply { mkdirs() }
                keyAlias = config.alias
                ensureAesKey(config.alias, config.useStrongBox)
            }
        }
    }

    private fun ensureRsaKeyPair(alias: String, useStrongBox: Boolean) {
        if (!keyStore.containsAlias(alias)) {
            val kpg = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_RSA,
                "AndroidKeyStore"
            )
            
            fun buildSpec(strongBox: Boolean): KeyGenParameterSpec {
                val builder = KeyGenParameterSpec.Builder(
                    alias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512, KeyProperties.DIGEST_SHA1)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
                if (strongBox && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    builder.setIsStrongBoxBacked(true)
                }
                return builder.build()
            }

            try {
                kpg.initialize(buildSpec(useStrongBox))
                kpg.generateKeyPair()
            } catch (e: Exception) {
                if (useStrongBox) {
                    try {
                        kpg.initialize(buildSpec(false))
                        kpg.generateKeyPair()
                    } catch (fallbackEx: Exception) {
                        throw fallbackEx
                    }
                } else {
                    throw e
                }
            }
        }
    }

    private fun ensureAesKey(alias: String, useStrongBox: Boolean) {
        if (!keyStore.containsAlias(alias)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                "AndroidKeyStore"
            )

            fun buildSpec(strongBox: Boolean): KeyGenParameterSpec {
                val builder = KeyGenParameterSpec.Builder(
                    alias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                if (strongBox && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    builder.setIsStrongBoxBacked(true)
                }
                return builder.build()
            }

            try {
                keyGenerator.init(buildSpec(useStrongBox))
                keyGenerator.generateKey()
            } catch (e: Exception) {
                if (useStrongBox) {
                    try {
                        keyGenerator.init(buildSpec(false))
                        keyGenerator.generateKey()
                    } catch (fallbackEx: Exception) {
                        throw fallbackEx
                    }
                } else {
                    throw e
                }
            }
        }
    }

    private fun getFileForKey(key: String): File {
        val hash = MessageDigest.getInstance("SHA-256")
            .digest(key.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
        return directory!!.resolve(hash)
    }

    private fun encryptFile(plainText: String): ByteArray {
        val alias = keyAlias ?: throw IllegalStateException("Key alias is missing")
        return when (config) {
            is AndroidSecurityConfig.RsaHybridFile -> {
                // RSA Hybrid
                val aesKeyGen = KeyGenerator.getInstance("AES")
                aesKeyGen.init(256)
                val aesKey = aesKeyGen.generateKey()

                val aesCipher = Cipher.getInstance("AES/GCM/NoPadding")
                val iv = ByteArray(12)
                SecureRandom().nextBytes(iv)
                val spec = GCMParameterSpec(128, iv)
                aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, spec)
                val encryptedData = aesCipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

                val privateKeyEntry = keyStore.getEntry(alias, null) as KeyStore.PrivateKeyEntry
                val publicKey = privateKeyEntry.certificate.publicKey
                val rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
                val oaepSpec = OAEPParameterSpec(
                    "SHA-256",
                    "MGF1",
                    MGF1ParameterSpec.SHA1,
                    PSource.PSpecified.DEFAULT
                )
                rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey, oaepSpec)
                val encryptedAesKey = rsaCipher.doFinal(aesKey.encoded)

                val buffer = ByteBuffer.allocate(4 + encryptedAesKey.size + 4 + iv.size + encryptedData.size)
                buffer.putInt(encryptedAesKey.size)
                buffer.put(encryptedAesKey)
                buffer.putInt(iv.size)
                buffer.put(iv)
                buffer.put(encryptedData)
                buffer.array()
            }
            is AndroidSecurityConfig.AesFile -> {
                // Direct AES
                val keyEntry = keyStore.getEntry(alias, null) as KeyStore.SecretKeyEntry
                val secretKey = keyEntry.secretKey
                val aesCipher = Cipher.getInstance("AES/GCM/NoPadding")
                val iv = ByteArray(12)
                SecureRandom().nextBytes(iv)
                val spec = GCMParameterSpec(128, iv)
                aesCipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)
                val encryptedData = aesCipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

                val buffer = ByteBuffer.allocate(4 + iv.size + encryptedData.size)
                buffer.putInt(iv.size)
                buffer.put(iv)
                buffer.put(encryptedData)
                buffer.array()
            }
            else -> throw IllegalStateException("File-based encryption not supported for config $config")
        }
    }

    private fun decryptFile(encryptedBytes: ByteArray): String {
        val alias = keyAlias ?: throw IllegalStateException("Key alias is missing")
        val buffer = ByteBuffer.wrap(encryptedBytes)
        return when (config) {
            is AndroidSecurityConfig.RsaHybridFile -> {
                val encryptedAesKeySize = buffer.int
                val encryptedAesKey = ByteArray(encryptedAesKeySize)
                buffer.get(encryptedAesKey)

                val ivSize = buffer.int
                val iv = ByteArray(ivSize)
                buffer.get(iv)

                val encryptedData = ByteArray(buffer.remaining())
                buffer.get(encryptedData)

                val privateKeyEntry = keyStore.getEntry(alias, null) as KeyStore.PrivateKeyEntry
                val privateKey = privateKeyEntry.privateKey
                val rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
                val oaepSpec = OAEPParameterSpec(
                    "SHA-256",
                    "MGF1",
                    MGF1ParameterSpec.SHA1,
                    PSource.PSpecified.DEFAULT
                )
                rsaCipher.init(Cipher.DECRYPT_MODE, privateKey, oaepSpec)
                val aesKeyBytes = rsaCipher.doFinal(encryptedAesKey)
                val aesKey: SecretKey = SecretKeySpec(aesKeyBytes, "AES")

                val aesCipher = Cipher.getInstance("AES/GCM/NoPadding")
                val spec = GCMParameterSpec(128, iv)
                aesCipher.init(Cipher.DECRYPT_MODE, aesKey, spec)
                val decryptedBytes = aesCipher.doFinal(encryptedData)
                String(decryptedBytes, Charsets.UTF_8)
            }
            is AndroidSecurityConfig.AesFile -> {
                val ivSize = buffer.int
                val iv = ByteArray(ivSize)
                buffer.get(iv)

                val encryptedData = ByteArray(buffer.remaining())
                buffer.get(encryptedData)

                val keyEntry = keyStore.getEntry(alias, null) as KeyStore.SecretKeyEntry
                val secretKey = keyEntry.secretKey
                val aesCipher = Cipher.getInstance("AES/GCM/NoPadding")
                val spec = GCMParameterSpec(128, iv)
                aesCipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
                val decryptedBytes = aesCipher.doFinal(encryptedData)
                String(decryptedBytes, Charsets.UTF_8)
            }
            else -> throw IllegalStateException("File-based decryption not supported for config $config")
        }
    }

    override fun getString(key: String): String? {
        val prefs = sharedPrefs
        if (prefs != null) {
            return prefs.getString(key, null)
        }
        val file = getFileForKey(key)
        if (!file.exists()) return null
        return try {
            decryptFile(file.readBytes())
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun putString(key: String, value: String?) {
        val prefs = sharedPrefs
        if (prefs != null) {
            prefs.edit().putString(key, value).apply()
            return
        }
        val file = getFileForKey(key)
        if (value == null) {
            remove(key)
            return
        }
        try {
            file.writeBytes(encryptFile(value))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getInt(key: String): Int? {
        val prefs = sharedPrefs
        if (prefs != null) {
            return if (prefs.contains(key)) prefs.getInt(key, 0) else null
        }
        return getString(key)?.toIntOrNull()
    }

    override fun putInt(key: String, value: Int?) {
        val prefs = sharedPrefs
        if (prefs != null) {
            if (value == null) prefs.edit().remove(key).apply()
            else prefs.edit().putInt(key, value).apply()
            return
        }
        putString(key, value?.toString())
    }

    override fun getLong(key: String): Long? {
        val prefs = sharedPrefs
        if (prefs != null) {
            return if (prefs.contains(key)) prefs.getLong(key, 0L) else null
        }
        return getString(key)?.toLongOrNull()
    }

    override fun putLong(key: String, value: Long?) {
        val prefs = sharedPrefs
        if (prefs != null) {
            if (value == null) prefs.edit().remove(key).apply()
            else prefs.edit().putLong(key, value).apply()
            return
        }
        putString(key, value?.toString())
    }

    override fun getFloat(key: String): Float? {
        val prefs = sharedPrefs
        if (prefs != null) {
            return if (prefs.contains(key)) prefs.getFloat(key, 0.0f) else null
        }
        return getString(key)?.toFloatOrNull()
    }

    override fun putFloat(key: String, value: Float?) {
        val prefs = sharedPrefs
        if (prefs != null) {
            if (value == null) prefs.edit().remove(key).apply()
            else prefs.edit().putFloat(key, value).apply()
            return
        }
        putString(key, value?.toString())
    }

    override fun getDouble(key: String): Double? {
        val prefs = sharedPrefs
        if (prefs != null) {
            return if (prefs.contains(key)) Double.fromBits(prefs.getLong(key, 0L)) else null
        }
        return getString(key)?.toDoubleOrNull()
    }

    override fun putDouble(key: String, value: Double?) {
        val prefs = sharedPrefs
        if (prefs != null) {
            if (value == null) prefs.edit().remove(key).apply()
            else prefs.edit().putLong(key, value.toBits()).apply()
            return
        }
        putString(key, value?.toString())
    }

    override fun getBoolean(key: String): Boolean? {
        val prefs = sharedPrefs
        if (prefs != null) {
            return if (prefs.contains(key)) prefs.getBoolean(key, false) else null
        }
        return getString(key)?.toBooleanStrictOrNull()
    }

    override fun putBoolean(key: String, value: Boolean?) {
        val prefs = sharedPrefs
        if (prefs != null) {
            if (value == null) prefs.edit().remove(key).apply()
            else prefs.edit().putBoolean(key, value).apply()
            return
        }
        putString(key, value?.toString())
    }

    override fun remove(key: String) {
        val prefs = sharedPrefs
        if (prefs != null) {
            prefs.edit().remove(key).apply()
            return
        }
        val file = getFileForKey(key)
        if (file.exists()) {
            file.delete()
        }
    }

    override fun clear() {
        val prefs = sharedPrefs
        if (prefs != null) {
            prefs.edit().clear().apply()
            return
        }
        directory?.listFiles()?.forEach { it.delete() }
    }

    override fun hasKey(key: String): Boolean {
        val prefs = sharedPrefs
        if (prefs != null) {
            return prefs.contains(key)
        }
        return directory?.resolve(
            MessageDigest.getInstance("SHA-256")
                .digest(key.toByteArray(Charsets.UTF_8))
                .joinToString("") { "%02x".format(it) }
        )?.exists() ?: false
    }
}
