package com.wa.toolkit.xposed.core.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.LruCache
import com.wa.toolkit.xposed.core.components.FMessageWpp
import com.wa.toolkit.xposed.utils.Utils

class MessageHistory(context: Context) : SQLiteOpenHelper(context, "MessageHistory.db", null, 3) {
    
    private var dbWrite: SQLiteDatabase = writableDatabase

    private val messagesCache = LruCache<Long, ArrayList<MessageItem>>(MESSAGE_CACHE_SIZE)
    private val seenMessageCache = LruCache<String, MessageSeenItem>(SEEN_MESSAGE_CACHE_SIZE)
    private val seenMessagesListCache = LruCache<String, List<MessageSeenItem>>(SEEN_MESSAGES_LIST_CACHE_SIZE)

    enum class MessageType {
        MESSAGE_TYPE,
        VIEW_ONCE_TYPE
    }

    companion object {
        private const val MESSAGE_CACHE_SIZE = 100
        private const val SEEN_MESSAGE_CACHE_SIZE = 200
        private const val SEEN_MESSAGES_LIST_CACHE_SIZE = 50

        @Volatile
        private var mInstance: MessageHistory? = null

        @JvmStatic
        fun getInstance(): MessageHistory {
            return mInstance?.takeIf { it.readableDatabase.isOpen } ?: synchronized(this) {
                mInstance?.takeIf { it.readableDatabase.isOpen } ?: MessageHistory(Utils.getApplication()).also {
                    mInstance = it
                }
            }
        }
    }

    fun insertMessage(id: Long, message: String, timestamp: Long) {
        synchronized(this) {
            val contentValues = ContentValues().apply {
                put("row_id", id)
                put("text_data", message)
                put("editTimestamp", timestamp)
            }
            dbWrite.insert("MessageHistory", null, contentValues)

            // Invalidate cache for this message ID
            messagesCache.remove(id)
        }
    }

    fun getMessages(v: Long): ArrayList<MessageItem>? {
        // Check cache first
        messagesCache.get(v)?.let { return it }

        // If not in cache, query database
        val history = dbWrite.query(
            "MessageHistory",
            arrayOf("_id", "row_id", "text_data", "editTimestamp"),
            "row_id=?",
            arrayOf(v.toString()),
            null, null, null
        )
        
        if (!history.moveToFirst()) {
            history.close()
            return null
        }
        
        val messages = ArrayList<MessageItem>()
        do {
            val id = history.getLong(history.getColumnIndexOrThrow("row_id"))
            val timestamp = history.getLong(history.getColumnIndexOrThrow("editTimestamp"))
            val message = history.getString(history.getColumnIndexOrThrow("text_data"))
            messages.add(MessageItem(id, message, timestamp))
        } while (history.moveToNext())
        history.close()

        // Store in cache
        messagesCache.put(v, messages)
        return messages
    }

    fun insertHideSeenMessage(jid: String, message_id: String, type: MessageType, viewed: Boolean) {
        synchronized(this) {
            if (updateViewedMessage(jid, message_id, type, viewed)) {
                return
            }
            val content = ContentValues().apply {
                put("jid", jid)
                put("message_id", message_id)
                put("type", type.ordinal)
            }
            dbWrite.insert("hide_seen_messages", null, content)

            // Invalidate caches
            val cacheKey = createSeenMessageCacheKey(jid, message_id, type)
            seenMessageCache.remove(cacheKey)
            invalidateSeenMessagesListCache(jid, type)
        }
    }

    fun updateViewedMessage(jid: String, message_id: String, type: MessageType, viewed: Boolean): Boolean {
        val cursor = dbWrite.query(
            "hide_seen_messages",
            arrayOf("_id"),
            "jid=? AND message_id=? AND type =?",
            arrayOf(jid, message_id, type.ordinal.toString()),
            null, null, null
        )
        
        if (!cursor.moveToFirst()) {
            cursor.close()
            return false
        }
        
        val id = cursor.getString(cursor.getColumnIndexOrThrow("_id"))
        cursor.close()

        synchronized(this) {
            val content = ContentValues().apply {
                put("viewed", if (viewed) 1 else 0)
            }
            dbWrite.update("hide_seen_messages", content, "_id=?", arrayOf(id))

            // Update cache or invalidate
            val cacheKey = createSeenMessageCacheKey(jid, message_id, type)
            val cachedItem = seenMessageCache.get(cacheKey)
            if (cachedItem != null && cachedItem.viewed != viewed) {
                seenMessageCache.remove(cacheKey)
            }
            invalidateSeenMessagesListCache(jid, type)
        }
        return true
    }

    fun getHideSeenMessage(jid: String, message_id: String, type: MessageType): MessageSeenItem? {
        // Check cache first
        val cacheKey = createSeenMessageCacheKey(jid, message_id, type)
        seenMessageCache.get(cacheKey)?.let { return it }

        // If not in cache, query database
        val cursor = dbWrite.query(
            "hide_seen_messages",
            arrayOf("viewed"),
            "jid=? AND message_id=? AND type=?",
            arrayOf(jid, message_id, type.ordinal.toString()),
            null, null, null
        )
        
        if (!cursor.moveToFirst()) {
            cursor.close()
            return null
        }
        
        val viewed = cursor.getInt(cursor.getColumnIndexOrThrow("viewed")) == 1
        val message = MessageSeenItem(jid, message_id, viewed)
        cursor.close()

        // Store in cache
        seenMessageCache.put(cacheKey, message)
        return message
    }

    fun getHideSeenMessages(jid: String, type: MessageType, viewed: Boolean): List<MessageSeenItem>? {
        // Check cache first
        val cacheKey = createSeenMessagesListCacheKey(jid, type, viewed)
        seenMessagesListCache.get(cacheKey)?.let { return it }

        // If not in cache, query database
        val cursor = dbWrite.query(
            "hide_seen_messages",
            arrayOf("jid", "message_id", "viewed"),
            "jid=? AND type=? AND viewed=?",
            arrayOf(jid, type.ordinal.toString(), if (viewed) "1" else "0"),
            null, null, null
        )
        
        if (!cursor.moveToFirst()) {
            cursor.close()
            return null
        }
        
        val messages = ArrayList<MessageSeenItem>()
        do {
            val messageId = cursor.getString(cursor.getColumnIndexOrThrow("message_id"))
            val message = MessageSeenItem(jid, messageId, viewed)
            messages.add(message)

            // Also cache individual messages
            val msgCacheKey = createSeenMessageCacheKey(jid, messageId, type)
            seenMessageCache.put(msgCacheKey, message)
        } while (cursor.moveToNext())
        cursor.close()

        // Store in cache
        seenMessagesListCache.put(cacheKey, messages)
        return messages
    }

    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
        sqLiteDatabase.execSQL("create table MessageHistory(_id INTEGER PRIMARY KEY AUTOINCREMENT, row_id INTEGER NOT NULL, text_data TEXT NOT NULL, editTimestamp BIGINT DEFAULT 0 );")
        sqLiteDatabase.execSQL("create table hide_seen_messages(_id INTEGER PRIMARY KEY AUTOINCREMENT, jid TEXT NOT NULL, message_id TEXT NOT NULL,type INT NOT NULL, viewed INT DEFAULT 0);")
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            sqLiteDatabase.execSQL("create table hide_seen_messages(_id INTEGER PRIMARY KEY AUTOINCREMENT, jid TEXT NOT NULL, message_id TEXT NOT NULL,type INT NOT NULL, viewed INT DEFAULT 0);")
        }
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }

    private fun createSeenMessageCacheKey(jid: String, message_id: String, type: MessageType): String {
        return "${jid}_${message_id}_${type.ordinal}"
    }

    private fun createSeenMessagesListCacheKey(jid: String, type: MessageType, viewed: Boolean): String {
        return "${jid}_${type.ordinal}_${if (viewed) "1" else "0"}"
    }

    private fun invalidateSeenMessagesListCache(jid: String, type: MessageType) {
        seenMessagesListCache.remove(createSeenMessagesListCacheKey(jid, type, true))
        seenMessagesListCache.remove(createSeenMessagesListCacheKey(jid, type, false))
    }

    fun clearCaches() {
        messagesCache.evictAll()
        seenMessageCache.evictAll()
        seenMessagesListCache.evictAll()
    }

    data class MessageItem(val id: Long, val message: String, val timestamp: Long)

    class MessageSeenItem(val jid: String, val message: String, val viewed: Boolean) {
        private var fMessageWpp: FMessageWpp? = null

        fun getFMessage(): FMessageWpp? {
            if (fMessageWpp == null) {
                try {
                    val userJid = FMessageWpp.UserJid(jid)
                    if (userJid.isNull) return null
                    fMessageWpp = FMessageWpp.Key(message, userJid, false).fMessage
                } catch (ignored: Exception) {
                }
            }
            return fMessageWpp
        }
    }
}
