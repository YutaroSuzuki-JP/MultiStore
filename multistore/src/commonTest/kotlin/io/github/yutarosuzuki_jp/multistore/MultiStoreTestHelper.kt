package io.github.yutarosuzuki_jp.multistore

import kotlin.test.*

fun runMultiStoreTests(store: MultiStore) {
    store.clear()

    // Initial state
    assertNull(store.getString("key_str"))
    assertNull(store.getInt("key_int"))
    assertNull(store.getLong("key_long"))
    assertNull(store.getFloat("key_float"))
    assertNull(store.getBoolean("key_bool"))
    assertFalse(store.hasKey("key_str"))

    // Put values
    store.putString("key_str", "value")
    store.putInt("key_int", 42)
    store.putLong("key_long", 1234567890L)
    store.putFloat("key_float", 3.14f)
    store.putBoolean("key_bool", true)

    // Verify values
    assertEquals("value", store.getString("key_str"))
    assertEquals(42, store.getInt("key_int"))
    assertEquals(1234567890L, store.getLong("key_long"))
    assertEquals(3.14f, store.getFloat("key_float"))
    assertEquals(true, store.getBoolean("key_bool"))
    assertTrue(store.hasKey("key_str"))

    // Update value
    store.putString("key_str", "new_value")
    assertEquals("new_value", store.getString("key_str"))

    // Put null to remove
    store.putString("key_str", null)
    assertNull(store.getString("key_str"))
    assertFalse(store.hasKey("key_str"))

    // Remove
    store.remove("key_int")
    assertNull(store.getInt("key_int"))

    // Clear
    store.clear()
    assertNull(store.getLong("key_long"))
    assertNull(store.getFloat("key_float"))
    assertNull(store.getBoolean("key_bool"))
}
