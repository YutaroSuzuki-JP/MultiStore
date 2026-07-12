package io.github.yutarosuzuki_jp.multistore.sample

import androidx.compose.ui.window.ComposeUIViewController
import io.github.yutarosuzuki_jp.multistore.IosMultiStoreFactory
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController {
    val factory = IosMultiStoreFactory()
    App(factory)
}
