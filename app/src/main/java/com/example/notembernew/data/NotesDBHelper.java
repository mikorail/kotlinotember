package com.example.notembernew.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

import thedorkknightrises.checklistview.ChecklistData;
import com.example.notembernew.Constants;
import com.example.notembernew.NoteObj;

public class NotesDBHelper extends SQLiteOpenHelper{
    public static final int DATABASE_VERSION = 7;
    public static final String DATABASE_NAME = "Notes.db";
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + NotesDB.Note.TABLE_NAME + " (" +
                    NotesDB.Note._ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                    NotesDB.Note.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
                    NotesDB.Note.COLUMN_NAME_SUBTITLE + TEXT_TYPE + COMMA_SEP +
                    NotesDB.Note.COLUMN_NAME_CONTENT + TEXT_TYPE + COMMA_SEP +
                    NotesDB.Note.COLUMN_NAME_TIME + TEXT_TYPE + COMMA_SEP +
                    NotesDB.Note.COLUMN_NAME_CREATED_AT + TEXT_TYPE + " UNIQUE" + COMMA_SEP +
                    NotesDB.Note.COLUMN_NAME_ARCHIVED + " INTEGER" + COMMA_SEP +
                    NotesDB.Note.COLUMN_NAME_NOTIFIED + " INTEGER" + COMMA_SEP +
                    NotesDB.Note.COLUMN_NAME_COLOR + TEXT_TYPE + COMMA_SEP +
                    NotesDB.Note.COLUMN_NAME_ENCRYPTED + " INTEGER" + COMMA_SEP +
                    NotesDB.Note.COLUMN_NAME_PINNED + " INTEGER" + COMMA_SEP +
                    NotesDB.Note.COLUMN_NAME_TAG + " INTEGER" + COMMA_SEP +
                    NotesDB.Note.COLUMN_NAME_REMINDER + TEXT_TYPE + COMMA_SEP +
                    NotesDB.Note.COLUMN_NAME_CHECKLIST + " INTEGER" + COMMA_SEP +
                    NotesDB.Note.COLUMN_NAME_DELETED + " INTEGER" + " ) ";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + NotesDB.Note.TABLE_NAME;

    private static final String SQL_CREATE_ENTRIES_CHECKLIST =
            "CREATE TABLE " + NotesDB.Checklist.TABLE_NAME + " (" +
                    NotesDB.Checklist._ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                    NotesDB.Checklist.COLUMN_NAME_NOTE_ID + " INTEGER " + COMMA_SEP +
                    NotesDB.Checklist.COLUMN_NAME_ITEM + TEXT_TYPE + COMMA_SEP +
                    NotesDB.Checklist.COLUMN_NAME_CHECKED + " INTEGER ) ";
    private static final String SQL_DELETE_ENTRIES_CHECKLIST =
            "DROP TABLE IF EXISTS " + NotesDB.Checklist.TABLE_NAME;

    public NotesDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
        db.execSQL(SQL_CREATE_ENTRIES_CHECKLIST);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 4:
                db.execSQL("ALTER TABLE " + NotesDB.Note.TABLE_NAME + " ADD COLUMN " + NotesDB.Note.COLUMN_NAME_CHECKLIST + " INTEGER DEFAULT 0;");
                db.execSQL("UPDATE " + NotesDB.Note.TABLE_NAME + " SET " + NotesDB.Note.COLUMN_NAME_CHECKLIST + " = 0");
                Log.d(getClass().getName(), "Database updated successfully to version 5 (added checklist column)");
            case 5:
                db.execSQL(SQL_CREATE_ENTRIES_CHECKLIST);
                Log.d(getClass().getName(), "Database updated successfully to version 6 (created checklist table)");
            case 6:
                db.execSQL("ALTER TABLE " + NotesDB.Note.TABLE_NAME + " ADD COLUMN " + NotesDB.Note.COLUMN_NAME_DELETED + " INTEGER DEFAULT 0;");
                db.execSQL("UPDATE " + NotesDB.Note.TABLE_NAME + " SET " + NotesDB.Note.COLUMN_NAME_DELETED + " = 0");
                Log.d(getClass().getName(), "Database updated successfully to version 7 (added deleted column)");
                break;
            default:
                db.execSQL(SQL_DELETE_ENTRIES);
                db.execSQL(SQL_DELETE_ENTRIES_CHECKLIST);
                onCreate(db);
        }
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public int addOrUpdateNote(int id, String title, String subtitle, String content, String time, String created_at, int archived, int notified, String color, int encrypted, int pinned, int tag, String reminder, int checklist, int deleted) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(NotesDB.Note.COLUMN_NAME_TITLE, title);
        values.put(NotesDB.Note.COLUMN_NAME_SUBTITLE, subtitle);
        values.put(NotesDB.Note.COLUMN_NAME_CONTENT, content);
        values.put(NotesDB.Note.COLUMN_NAME_TIME, time);
        values.put(NotesDB.Note.COLUMN_NAME_ARCHIVED, archived);
        values.put(NotesDB.Note.COLUMN_NAME_NOTIFIED, notified);
        values.put(NotesDB.Note.COLUMN_NAME_COLOR, color);
        values.put(NotesDB.Note.COLUMN_NAME_ENCRYPTED, encrypted);
        values.put(NotesDB.Note.COLUMN_NAME_PINNED, pinned);
        values.put(NotesDB.Note.COLUMN_NAME_TAG, tag);
        values.put(NotesDB.Note.COLUMN_NAME_REMINDER, reminder);
        values.put(NotesDB.Note.COLUMN_NAME_CHECKLIST, checklist);
        values.put(NotesDB.Note.COLUMN_NAME_DELETED, deleted);

        int i = db.update(NotesDB.Note.TABLE_NAME, values,
                NotesDB.Note.COLUMN_NAME_CREATED_AT + " = ? ",
                new String[]{created_at});
        if (i == 0) {
            values.put(NotesDB.Note.COLUMN_NAME_CREATED_AT, created_at);
            i = (int) db.insertWithOnConflict(NotesDB.Note.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            Log.d("DB", "Added");
        } else {
            Log.d("DB", "Updated");
            i = id;
        }
        db.close();
        return i;
    }

    public int updateFlag(int id, String field, int value) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(field, value);

        int i = db.update(NotesDB.Note.TABLE_NAME, values,
                NotesDB.Note._ID + " = ? ",
                new String[]{String.valueOf(id)});
        Log.d("Updated field", field + value + " for note id: " + i);
        db.close();
        return i;
    }

    public int updateFlag(int id, String field, String value) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(field, value);

        int i = db.update(NotesDB.Note.TABLE_NAME, values,
                NotesDB.Note._ID + " = ? ",
                new String[]{String.valueOf(id)});
        Log.d("Updated field", field + value + " for note id: " + i);
        db.close();
        return i;
    }

    public void deleteNote(String created_at) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(NotesDB.Note.TABLE_NAME,
                NotesDB.Note.COLUMN_NAME_CREATED_AT + " = ? ",
                new String[]{created_at});
        Log.d("DB", "Deleted");
        db.close();
    }

    public int deleteAllNotes() {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(NotesDB.Note.TABLE_NAME, null, null);
        db.delete(NotesDB.Checklist.TABLE_NAME, null, null);
        db.close();
        if (result == 1)
            Log.d("DB", "All notes deleted");
        return result;
    }

    public NoteObj getNote(int id) {
        ArrayList<NoteObj> mList = new ArrayList<NoteObj>();
        String[] projection = {
                NotesDB.Note._ID,
                NotesDB.Note.COLUMN_NAME_TITLE,
                NotesDB.Note.COLUMN_NAME_SUBTITLE,
                NotesDB.Note.COLUMN_NAME_CONTENT,
                NotesDB.Note.COLUMN_NAME_TIME,
                NotesDB.Note.COLUMN_NAME_CREATED_AT,
                NotesDB.Note.COLUMN_NAME_ARCHIVED,
                NotesDB.Note.COLUMN_NAME_NOTIFIED,
                NotesDB.Note.COLUMN_NAME_COLOR,
                NotesDB.Note.COLUMN_NAME_ENCRYPTED,
                NotesDB.Note.COLUMN_NAME_PINNED,
                NotesDB.Note.COLUMN_NAME_TAG,
                NotesDB.Note.COLUMN_NAME_REMINDER,
                NotesDB.Note.COLUMN_NAME_CHECKLIST,
                NotesDB.Note.COLUMN_NAME_DELETED
        };

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(NotesDB.Note.TABLE_NAME, projection, NotesDB.Note._ID + " = " + id, null, null, null, NotesDB.Note.COLUMN_NAME_TIME + " DESC");


        if (cursor.moveToFirst()) {
            NoteObj noteObj = new NoteObj(cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getInt(6),
                    cursor.getInt(7),
                    cursor.getString(8),
                    cursor.getInt(9),
                    cursor.getInt(10),
                    cursor.getInt(11),
                    cursor.getString(12),
                    cursor.getInt(13),
                    cursor.getInt(14));

            cursor.close();
            db.close();
            return noteObj;
        }

        cursor.close();
        db.close();
        return null;
    }

    public ArrayList<NoteObj> getAllNotes(int archive, int deleted) {
        ArrayList<NoteObj> mList = new ArrayList<NoteObj>();
        String[] projection = {
                NotesDB.Note._ID,
                NotesDB.Note.COLUMN_NAME_TITLE,
                NotesDB.Note.COLUMN_NAME_SUBTITLE,
                NotesDB.Note.COLUMN_NAME_CONTENT,
                NotesDB.Note.COLUMN_NAME_TIME,
                NotesDB.Note.COLUMN_NAME_CREATED_AT,
                NotesDB.Note.COLUMN_NAME_ARCHIVED,
                NotesDB.Note.COLUMN_NAME_NOTIFIED,
                NotesDB.Note.COLUMN_NAME_COLOR,
                NotesDB.Note.COLUMN_NAME_ENCRYPTED,
                NotesDB.Note.COLUMN_NAME_PINNED,
                NotesDB.Note.COLUMN_NAME_TAG,
                NotesDB.Note.COLUMN_NAME_REMINDER,
                NotesDB.Note.COLUMN_NAME_CHECKLIST,
                NotesDB.Note.COLUMN_NAME_DELETED
        };

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(NotesDB.Note.TABLE_NAME, projection, NotesDB.Note.COLUMN_NAME_ARCHIVED + " LIKE " + archive + " AND " + NotesDB.Note.COLUMN_NAME_DELETED + " LIKE " + deleted, null, null, null, NotesDB.Note.COLUMN_NAME_TIME + " DESC");

        if (cursor.moveToFirst()) {
            do {
                NoteObj noteObj = new NoteObj(cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getInt(6),
                        cursor.getInt(7),
                        cursor.getString(8),
                        cursor.getInt(9),
                        cursor.getInt(10),
                        cursor.getInt(11),
                        cursor.getString(12),
                        cursor.getInt(13),
                        cursor.getInt(14));
                mList.add(noteObj);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return mList;
    }

    public ArrayList<NoteObj> getNotificationsAndReminders() {
        ArrayList<NoteObj> mList = new ArrayList<NoteObj>();
        String[] projection = {
                NotesDB.Note._ID,
                NotesDB.Note.COLUMN_NAME_TITLE,
                NotesDB.Note.COLUMN_NAME_SUBTITLE,
                NotesDB.Note.COLUMN_NAME_CONTENT,
                NotesDB.Note.COLUMN_NAME_TIME,
                NotesDB.Note.COLUMN_NAME_CREATED_AT,
                NotesDB.Note.COLUMN_NAME_ARCHIVED,
                NotesDB.Note.COLUMN_NAME_NOTIFIED,
                NotesDB.Note.COLUMN_NAME_COLOR,
                NotesDB.Note.COLUMN_NAME_ENCRYPTED,
                NotesDB.Note.COLUMN_NAME_PINNED,
                NotesDB.Note.COLUMN_NAME_TAG,
                NotesDB.Note.COLUMN_NAME_REMINDER,
                NotesDB.Note.COLUMN_NAME_CHECKLIST,
                NotesDB.Note.COLUMN_NAME_DELETED
        };

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(NotesDB.Note.TABLE_NAME, projection, NotesDB.Note.COLUMN_NAME_NOTIFIED + " LIKE 1 OR " + NotesDB.Note.COLUMN_NAME_REMINDER + " NOT LIKE '" + Constants.REMINDER_NONE + "'",
                null, null, null, NotesDB.Note.COLUMN_NAME_TIME + " ASC");

        if (cursor.moveToFirst()) {
            do {
                NoteObj noteObj = new NoteObj(cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getInt(6),
                        cursor.getInt(7),
                        cursor.getString(8),
                        cursor.getInt(9),
                        cursor.getInt(10),
                        cursor.getInt(11),
                        cursor.getString(12),
                        cursor.getInt(13),
                        cursor.getInt(14));
                mList.add(noteObj);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return mList;
    }

    public void clearAllNotifications() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + NotesDB.Note.TABLE_NAME + " SET " + NotesDB.Note.COLUMN_NAME_NOTIFIED + " = 0 ");
        db.close();
    }

    public void clearAllReminders() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + NotesDB.Note.TABLE_NAME + " SET " + NotesDB.Note.COLUMN_NAME_REMINDER + " = '" + Constants.REMINDER_NONE + "'");
        db.close();
    }

    public int saveChecklist(int id, String title, String subtitle, String content,
                             ArrayList<ChecklistData> checklistData, String time, String created_at,
                             int archived, int notified, String color, int encrypted, int pinned,
                             int tag, String reminder, int deleted) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(NotesDB.Note.COLUMN_NAME_TITLE, title);
        values.put(NotesDB.Note.COLUMN_NAME_SUBTITLE, subtitle);
        values.put(NotesDB.Note.COLUMN_NAME_CONTENT, content);
        values.put(NotesDB.Note.COLUMN_NAME_TIME, time);
        values.put(NotesDB.Note.COLUMN_NAME_ARCHIVED, archived);
        values.put(NotesDB.Note.COLUMN_NAME_NOTIFIED, notified);
        values.put(NotesDB.Note.COLUMN_NAME_COLOR, color);
        values.put(NotesDB.Note.COLUMN_NAME_ENCRYPTED, encrypted);
        values.put(NotesDB.Note.COLUMN_NAME_PINNED, pinned);
        values.put(NotesDB.Note.COLUMN_NAME_TAG, tag);
        values.put(NotesDB.Note.COLUMN_NAME_REMINDER, reminder);
        values.put(NotesDB.Note.COLUMN_NAME_CHECKLIST, 1);
        values.put(NotesDB.Note.COLUMN_NAME_DELETED, deleted);

        int i = db.update(NotesDB.Note.TABLE_NAME, values,
                NotesDB.Note.COLUMN_NAME_CREATED_AT + " = ? ",
                new String[]{created_at});
        if (i == 0) {
            values.put(NotesDB.Note.COLUMN_NAME_CREATED_AT, created_at);
            i = (int) db.insertWithOnConflict(NotesDB.Note.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            Log.d("DB", "Added");
        } else {
            Log.d("DB", "Updated");
            i = id;
            db.execSQL("DELETE FROM " + NotesDB.Checklist.TABLE_NAME + " WHERE " + NotesDB.Checklist.COLUMN_NAME_NOTE_ID + " = " + i);
        }

        for (ChecklistData data : checklistData) {
            ContentValues checklistValues = new ContentValues();
            checklistValues.put(NotesDB.Checklist.COLUMN_NAME_NOTE_ID, i);
            checklistValues.put(NotesDB.Checklist.COLUMN_NAME_ITEM, data.getText());
            int checked = (data.isChecked()) ? 1 : 0;
            checklistValues.put(NotesDB.Checklist.COLUMN_NAME_CHECKED, checked);
            long cId = db.insertWithOnConflict(NotesDB.Checklist.TABLE_NAME, null, checklistValues, SQLiteDatabase.CONFLICT_REPLACE);
            Log.d("DB", "Checklist item " + cId + " added");
        }

        db.close();
        return i;
    }

    public ArrayList<ChecklistData> getChecklistData(int noteId) {
        ArrayList<ChecklistData> mList = new ArrayList<ChecklistData>();
        String[] projection = {
                NotesDB.Checklist.COLUMN_NAME_NOTE_ID,
                NotesDB.Checklist.COLUMN_NAME_ITEM,
                NotesDB.Checklist.COLUMN_NAME_CHECKED
        };

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(NotesDB.Checklist.TABLE_NAME, projection, NotesDB.Checklist.COLUMN_NAME_NOTE_ID + " = " + noteId,
                null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                boolean checked = (cursor.getInt(2) == 1);
                ChecklistData checklistData = new ChecklistData(checked, cursor.getString(1));
                mList.add(checklistData);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return mList;
    }

    public void deleteChecklistData(int noteId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + NotesDB.Checklist.TABLE_NAME + " WHERE " + NotesDB.Checklist.COLUMN_NAME_NOTE_ID + " = " + noteId);
        db.close();
    }
}
