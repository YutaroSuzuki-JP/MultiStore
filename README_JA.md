# MultiStore

[English README is available here (README.md)](README.md)

`MultiStore` は、Kotlin Multiplatform (KMP) 環境下で、**Android, iOS, Web (Kotlin Wasm)** のプラットフォーム固有の標準および高度にセキュアな Key-Value ストレージシステムをラップし、統一されたAPIを提供するライブラリです。

`MultiStore` を使用することで、共通コード（`commonMain`）からプラットフォーム別のストレージ実装を一切意識することなく、設定データやセンシティブな認証トークンなどの読み書き・削除を同じロジックで制御できます。

---

## 主な特徴

- 🔑 **統一された共通API**: プリミティブ型（`String`, `Int`, `Long`, `Float`, `Boolean`）に対応し、シンプルなCRUD操作を提供。
- 🛡️ **柔軟なセキュリティ設定**: プラットフォーム別の暗号化・セキュリティ要件を利用者側で自由にカスタマイズ可能。
  - **Android (Android KeyStore)**: 
    - セキュアストアでは `SharedPreferences` を一切使用しません。
    - キーと値の両方を暗号化したうえで、内部ストレージファイルへ直接保存します。
    - **RSAハイブリッドエンベロープ暗号** (非対称)、**直接AES-256 GCM** (対称)、および標準の **EncryptedSharedPreferences** の3つから暗号化形式を選択可能。
    - ハードウェア暗号化モジュールである **StrongBox** の使用ON/OFFをサポート。
  - **iOS (Keychain Services)**: 
    - Swift/Obj-Cとの相互運用性を考慮した Keychain 直接連携。
    - Keychainの復号クラスをカスタマイズ可能 (`AfterFirstUnlock`, `WhenUnlocked`, `Always`)。
    - 複数アプリ間でのKeychain共有のためのアプリグループ（Access Group）や、iCloud Keychain自動同期に対応。
  - **Web (Kotlin Wasm)**: 
    - ブラウザの `window.localStorage` または `window.sessionStorage` をラップ。
    - セキュアストアでは、開発者ツール上での平文露出を防ぐため、マルチバイト文字（日本語など）に対応した安全な Base64 難読化エンコードを実行して保存。

---

## サポート環境

- **Android** (API 24以上)
- **iOS** (Arm64, シミュレータ)
- **Web** (Kotlin Wasm / `wasmJs`)

---

## 基本的な使い方

### 1. 共通APIの定義 (`commonMain`)

共通コード内で `MultiStoreFactory` を注入または参照し、ストレージを構築します。

```kotlin
// commonMain でファクトリを利用する例
class UserSettingsRepository(private val factory: MultiStoreFactory) {

    // 通常のストレージ (SharedPreferences, UserDefaults, または localStorage)
    private val standardStore = factory.create("settings_pref")
    
    // セキュアストレージ (KeyStoreによる暗号化ファイル, Keychain, または 難読化localStorage)
    private val secureStore = factory.createSecure("user_secure_data")

    fun saveToken(token: String) {
        secureStore.putString("auth_token", token)
    }

    fun getToken(): String? {
        return secureStore.getString("auth_token")
    }
}
```

### 2. プラットフォームごとのファクトリ初期化

#### Android

Application や Activity の初期化タイミングで `AndroidMultiStoreFactory` を構築します。その際、利用者が自由にセキュリティ設定を構築できます。

```kotlin
val factory = AndroidMultiStoreFactory(
    context = context,
    securityConfigProvider = { name ->
        // 用途に合わせてセキュリティ設定をカスタマイズ
        AndroidSecurityConfig.AesFile(
            alias = "my_app_secure_key_$name",
            directoryName = "secure_storage_$name",
            useStrongBox = true // StrongBox を有効化
        )
    }
)
```

#### iOS

Swift側で `IosMultiStoreFactory` をインスタンス化します。

```swift
let factory = IosMultiStoreFactory(
    securityConfigProvider: { name in
        return IosSecurityConfig(
            accessible: .afterFirstUnlock,
            accessGroup: "group.io.github.yutarosuzuki-jp.shared", // グループ共有
            synchronizable: true // iCloud同期をON
        )
    }
)
```

#### Web (Kotlin Wasm)

Web側のエントリーポイントで `WebMultiStoreFactory` を初期化します。

```kotlin
val factory = WebMultiStoreFactory(
    securityConfigProvider = { name ->
        WebSecurityConfig(
            useSessionStorage = false, // localStorage を使用
            obfuscate = true           // Base64による難読化を有効化
        )
    }
)
```

---

## ライセンス

本ライブラリは MIT ライセンスの下で提供されています。詳細は [LICENSE](LICENSE) をご参照ください。
