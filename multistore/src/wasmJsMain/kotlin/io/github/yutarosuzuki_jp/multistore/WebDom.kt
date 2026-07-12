package io.github.yutarosuzuki_jp.multistore

import kotlin.js.ExperimentalWasmJsInterop

external val window: Window

external interface Window {
    val localStorage: Storage
    val sessionStorage: Storage
}

external interface Storage {
    val length: Int
    fun getItem(key: String): String?
    fun setItem(key: String, value: String)
    fun removeItem(key: String)
    fun key(index: Int): String?
}

// Helper functions for safe base64 with unicode support in Kotlin Wasm
@OptIn(ExperimentalWasmJsInterop::class)
fun base64Encode(str: String): String = js("window.btoa(unescape(encodeURIComponent(str)))")

@OptIn(ExperimentalWasmJsInterop::class)
fun base64Decode(str: String): String = js("decodeURIComponent(escape(window.atob(str)))")
