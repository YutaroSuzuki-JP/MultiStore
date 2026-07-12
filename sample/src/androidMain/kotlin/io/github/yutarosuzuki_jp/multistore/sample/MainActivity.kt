package io.github.yutarosuzuki_jp.multistore.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import io.github.yutarosuzuki_jp.multistore.AndroidMultiStoreFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = AndroidMultiStoreFactory(applicationContext)
        setContent {
            App(factory)
        }
    }
}
