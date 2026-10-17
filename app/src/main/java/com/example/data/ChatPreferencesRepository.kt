package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.ChatThread
import com.example.model.Message
import com.example.model.MessageSender
import com.example.model.Reaction
import com.example.model.ThemeOption
import com.example.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "thoshan_prefs")

class ChatPreferencesRepository(private val context: Context) {

    private val KEY_THEME = stringPreferencesKey("theme_pref")
    private val KEY_LANGUAGE = stringPreferencesKey("reply_language_pref")
    private val KEY_THREADS = stringPreferencesKey("chat_threads_json")

    val settingsFlow: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        val themeStr = prefs[KEY_THEME] ?: ThemeOption.DARK.name
        val lang = prefs[KEY_LANGUAGE] ?: "English"
        val theme = runCatching { ThemeOption.valueOf(themeStr) }.getOrDefault(ThemeOption.DARK)
        UserSettings(theme = theme, replyLanguage = lang)
    }

    suspend fun updateTheme(theme: ThemeOption) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME] = theme.name
        }
    }

    suspend fun updateLanguage(language: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LANGUAGE] = language
        }
    }

    val threadsFlow: Flow<List<ChatThread>> = context.dataStore.data.map { prefs ->
        val jsonStr = prefs[KEY_THREADS]
        if (jsonStr.isNullOrEmpty()) {
            emptyList()
        } else {
            deserializeThreads(jsonStr)
        }
    }

    suspend fun saveThreads(threads: List<ChatThread>) {
        // Enforce empty-history rule: Only persist threads with at least one message
        val nonEmpty = threads.filter { it.messages.isNotEmpty() }
        val json = serializeThreads(nonEmpty)
        context.dataStore.edit { prefs ->
            prefs[KEY_THREADS] = json
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_THREADS)
        }
    }

    private fun serializeThreads(threads: List<ChatThread>): String {
        val rootArray = JSONArray()
        for (thread in threads) {
            val threadObj = JSONObject().apply {
                put("id", thread.id)
                put("title", thread.title)
                put("createdAt", thread.createdAt)
                put("updatedAt", thread.updatedAt)

                val msgArray = JSONArray()
                for (msg in thread.messages) {
                    val msgObj = JSONObject().apply {
                        put("id", msg.id)
                        put("sender", msg.sender.name)
                        put("text", msg.text)
                        put("timestamp", msg.timestamp)
                        msg.reaction?.let { put("reaction", it.name) }
                        msg.attachmentUri?.let { put("attachmentUri", it) }
                        msg.attachmentName?.let { put("attachmentName", it) }
                        msg.attachmentMime?.let { put("attachmentMime", it) }

                        val thoughtsArray = JSONArray()
                        msg.thoughts.forEach { thoughtsArray.put(it) }
                        put("thoughts", thoughtsArray)
                    }
                    msgArray.put(msgObj)
                }
                put("messages", msgArray)
            }
            rootArray.put(threadObj)
        }
        return rootArray.toString()
    }

    private fun deserializeThreads(jsonStr: String): List<ChatThread> {
        return runCatching {
            val rootArray = JSONArray(jsonStr)
            val threads = mutableListOf<ChatThread>()
            for (i in 0 until rootArray.length()) {
                val threadObj = rootArray.getJSONObject(i)
                val id = threadObj.getString("id")
                val title = threadObj.optString("title", "Chat")
                val createdAt = threadObj.optLong("createdAt", System.currentTimeMillis())
                val updatedAt = threadObj.optLong("updatedAt", createdAt)

                val msgArray = threadObj.optJSONArray("messages") ?: JSONArray()
                val messages = mutableListOf<Message>()
                for (j in 0 until msgArray.length()) {
                    val msgObj = msgArray.getJSONObject(j)
                    val mId = msgObj.getString("id")
                    val sender = MessageSender.valueOf(msgObj.getString("sender"))
                    val text = msgObj.getString("text")
                    val timestamp = msgObj.optLong("timestamp", System.currentTimeMillis())
                    val reaction = msgObj.optString("reaction", null.toString()).let {
                        if (it != "null") runCatching { Reaction.valueOf(it) }.getOrNull() else null
                    }
                    val thoughtsArray = msgObj.optJSONArray("thoughts") ?: JSONArray()
                    val thoughts = mutableListOf<String>()
                    for (k in 0 until thoughtsArray.length()) {
                        thoughts.add(thoughtsArray.getString(k))
                    }
                    messages.add(
                        Message(
                            id = mId,
                            sender = sender,
                            text = text,
                            thoughts = thoughts,
                            timestamp = timestamp,
                            reaction = reaction,
                            attachmentUri = msgObj.optString("attachmentUri", null.toString()).takeIf { it != "null" },
                            attachmentName = msgObj.optString("attachmentName", null.toString()).takeIf { it != "null" },
                            attachmentMime = msgObj.optString("attachmentMime", null.toString()).takeIf { it != "null" }
                        )
                    )
                }
                threads.add(
                    ChatThread(
                        id = id,
                        title = title,
                        messages = messages,
                        createdAt = createdAt,
                        updatedAt = updatedAt
                    )
                )
            }
            threads
        }.getOrDefault(emptyList())
    }
}
