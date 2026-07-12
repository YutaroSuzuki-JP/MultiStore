# MultiStore

[日本語のドキュメントはこちら (README_JA.md)](README_JA.md)

`MultiStore` is a Kotlin Multiplatform (KMP) Key-Value storage library that provides a unified, cross-platform API wrapping both standard and highly secure storage mechanisms for **Android, iOS, and Web (Kotlin Wasm)**.

With `MultiStore`, you can manage local configurations and sensitive data from your shared `commonMain` codebase, completely abstracting away the platform-specific storage APIs.

---

## Features

- 🔑 **Unified Common API**: Clean interface supporting primitive types (`String`, `Int`, `Long`, `Float`, `Boolean`) and CRUD operations.
- 🛡️ **Highly Configurable Secure Storage**: Easily customize security settings per platform during initialization.
  - **Android (Android KeyStore)**: 
    - Bypasses `SharedPreferences` completely for secure storage.
    - Encrypts keys/values and saves them to internal files.
    - Supports three modes: **RSA Hybrid Envelope Encryption** (asymmetric), **Direct AES-256 GCM** (symmetric), and standard **EncryptedSharedPreferences**.
    - Fully supports hardware-backed **StrongBox** configurations.
  - **iOS (Keychain Services)**: 
    - Direct Keychain integration via Swift/Obj-C interop.
    - Configurable Accessibility attributes (`AfterFirstUnlock`, `WhenUnlocked`, `Always`).
    - Supports App Access Groups (keychain sharing) and iCloud Keychain synchronization.
  - **Web (Kotlin Wasm)**: 
    - Wraps `window.localStorage` or `window.sessionStorage`.
    - Secure mode automatically obfuscates keys/values using safe Base64 Unicode encoding (`btoa` / `atob` wrapper) to prevent plain-text snooping in Chrome Developer Tools.

---

## Supported Platforms

- **Android** (API 24+)
- **iOS** (Arm64, Simulator)
- **Web** (Kotlin Wasm / `wasmJs`)

---

## Usage

### 1. Define the Common API

In your `commonMain` code, inject and use the `MultiStoreFactory` to create your stores:

```kotlin
// Inject or pass the factory in commonMain
class UserSettingsRepository(private val factory: MultiStoreFactory) {

    // Standard Store (wraps SharedPreferences, UserDefaults, or localStorage)
    private val standardStore = factory.create("settings_pref")
    
    // Secure Store (wraps KeyStore files, Keychain, or obfuscated localStorage)
    private val secureStore = factory.createSecure("user_secure_data")

    fun saveToken(token: String) {
        secureStore.putString("auth_token", token)
    }

    fun getToken(): String? {
        return secureStore.getString("auth_token")
    }
}
```

### 2. Instantiate Platform-Specific Factories

#### Android

Initialize the factory in your Activity or Application class using the `AndroidMultiStoreFactory`. You can customize the secure storage settings dynamically:

```kotlin
val factory = AndroidMultiStoreFactory(
    context = context,
    securityConfigProvider = { name ->
        // Customize security level here
        AndroidSecurityConfig.AesFile(
            alias = "my_app_secure_key_$name",
            directoryName = "secure_storage_$name",
            useStrongBox = true // Enable hardware StrongBox if available
        )
    }
)
```

#### iOS

Initialize the factory on the iOS side using the `IosMultiStoreFactory`:

```swift
let factory = IosMultiStoreFactory(
    securityConfigProvider: { name in
        return IosSecurityConfig(
            accessible: .afterFirstUnlock,
            accessGroup: "group.io.github.yutarosuzuki-jp.shared", // App Groups sharing
            synchronizable: true // iCloud Keychain sync
        )
    }
)
```

#### Web (Kotlin Wasm)

Initialize the factory in your Web entry point using `WebMultiStoreFactory`:

```kotlin
val factory = WebMultiStoreFactory(
    securityConfigProvider = { name ->
        WebSecurityConfig(
            useSessionStorage = false, // Use localStorage
            obfuscate = true           // Enable Base64 obfuscation
        )
    }
)
```

---

## License

This library is licensed under the MIT License. See [LICENSE](LICENSE) for details.
