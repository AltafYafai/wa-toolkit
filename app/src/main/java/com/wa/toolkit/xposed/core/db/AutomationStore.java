package com.wa.toolkit.xposed.core.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class AutomationStore extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "automation.db";
    private static final int DATABASE_VERSION = 1;

    // Tables
    private static final String TABLE_SCHEDULED_MESSAGES = "scheduled_messages";
    private static final String TABLE_AUTO_REPLIES = "auto_replies";

    // Common columns
    private static final String COL_ID = "id";
    private static final String COL_CONTENT = "content";
    private static final String COL_ENABLED = "enabled";

    // Scheduled Messages columns
    private static final String COL_RECIPIENT_JID = "recipient_jid";
    private static final String COL_SCHEDULE_TIME = "schedule_time";
    private static final String COL_STATUS = "status"; // 0: Pending, 1: Sent, 2: Failed

    // Auto Reply columns
    private static final String COL_KEYWORD = "keyword";
    private static final String COL_MATCH_TYPE = "match_type"; // 0: Contains, 1: Exact, 2: StartsWith

    private static AutomationStore instance;

    public static synchronized AutomationStore getInstance(Context context) {
        if (instance == null) {
            instance = new AutomationStore(context.getApplicationContext());
        }
        return instance;
    }

    private AutomationStore(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_SCHEDULED_MESSAGES + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RECIPIENT_JID + " TEXT, " +
                COL_CONTENT + " TEXT, " +
                COL_SCHEDULE_TIME + " INTEGER, " +
                COL_STATUS + " INTEGER DEFAULT 0, " +
                COL_ENABLED + " INTEGER DEFAULT 1)");

        db.execSQL("CREATE TABLE " + TABLE_AUTO_REPLIES + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_KEYWORD + " TEXT, " +
                COL_CONTENT + " TEXT, " +
                COL_MATCH_TYPE + " INTEGER, " +
                COL_ENABLED + " INTEGER DEFAULT 1)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle upgrades
    }

    // --- Scheduled Messages ---

    public long addScheduledMessage(String jid, String content, long time) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_RECIPIENT_JID, jid);
        values.put(COL_CONTENT, content);
        values.put(COL_SCHEDULE_TIME, time);
        return db.insert(TABLE_SCHEDULED_MESSAGES, null, values);
    }

    public List<ScheduledMessage> getPendingMessages(long beforeTime) {
        List<ScheduledMessage> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_SCHEDULED_MESSAGES, null,
                COL_STATUS + " = 0 AND " + COL_ENABLED + " = 1 AND " + COL_SCHEDULE_TIME + " <= ?",
                new String[]{String.valueOf(beforeTime)}, null, null, COL_SCHEDULE_TIME + " ASC");

        if (cursor.moveToFirst()) {
            do {
                list.add(new ScheduledMessage(
                        cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_RECIPIENT_JID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTENT)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(COL_SCHEDULE_TIME))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public void updateMessageStatus(long id, int status) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_STATUS, status);
        db.update(TABLE_SCHEDULED_MESSAGES, values, COL_ID + " = ?", new String[]{String.valueOf(id)});
    }

    // --- Auto Replies ---

    public long addAutoReply(String keyword, String content, int matchType) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_KEYWORD, keyword);
        values.put(COL_CONTENT, content);
        values.put(COL_MATCH_TYPE, matchType);
        return db.insert(TABLE_AUTO_REPLIES, null, values);
    }

    public List<AutoReply> getEnabledAutoReplies() {
        List<AutoReply> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_AUTO_REPLIES, null, COL_ENABLED + " = 1", null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                list.add(new AutoReply(
                        cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_KEYWORD)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTENT)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_MATCH_TYPE))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public static class ScheduledMessage {
        public long id;
        public String jid;
        public String content;
        public long time;

        public ScheduledMessage(long id, String jid, String content, long time) {
            this.id = id;
            this.jid = jid;
            this.content = content;
            this.time = time;
        }
    }

    public static class AutoReply {
        public long id;
        public String keyword;
        public String content;
        public int matchType;

        public AutoReply(long id, String keyword, String content, int matchType) {
            this.id = id;
            this.keyword = keyword;
            this.content = content;
            this.matchType = matchType;
        }
    }
}
