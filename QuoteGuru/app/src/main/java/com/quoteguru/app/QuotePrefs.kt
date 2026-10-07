package com.quoteguru.app

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.quoteDataStore by preferencesDataStore(name = "quoteguru")

data class QuoteSettings(
    val quote: String = "Dream big and dare to fail.",
    val author: String = "Norman Vaughan",
    val dark: Boolean = true,
    val shape: Int = 0
)

object QuotePrefs {
    private val quoteKey = stringPreferencesKey("quote")
    private val authorKey = stringPreferencesKey("author")
    private val darkKey = booleanPreferencesKey("dark")
    private val shapeKey = intPreferencesKey("shape")

    fun flow(context: Context): Flow<QuoteSettings> = context.quoteDataStore.data.map { p ->
        QuoteSettings(
            quote = p[quoteKey] ?: "Dream big and dare to fail.",
            author = p[authorKey] ?: "Norman Vaughan",
            dark = p[darkKey] ?: true,
            shape = p[shapeKey] ?: 0
        )
    }

    suspend fun save(context: Context, settings: QuoteSettings) {
        context.quoteDataStore.edit { p ->
            p[quoteKey] = settings.quote
            p[authorKey] = settings.author
            p[darkKey] = settings.dark
            p[shapeKey] = settings.shape
        }
    }
}
