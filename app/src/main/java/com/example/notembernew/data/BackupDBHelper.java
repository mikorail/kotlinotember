package com.example.notembernew.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class BackupDBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 6;
    public static final String DATABASE_NAME = "Backup.db";
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

    public BackupDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
        db.execSQL(SQL_CREATE_ENTRIES_CHECKLIST);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 4:
                db.execSQL("ALTER TABLE " + NotesDB.Note.TABLE_NAME + " ADD COLUMN " + NotesDB.Note.COLUMN_NAME_CHECKLIST + " INTEGER DEFAULT 0");
                db.execSQL("UPDATE " + NotesDB.Note.TABLE_NAME + " SET " + NotesDB.Note.COLUMN_NAME_CHECKLIST + " = 0");
                Log.d(getClass().getName(), "Database updated successfully to version 5 (added checklist column)");
            case 5:
                db.execSQL(SQL_CREATE_ENTRIES_CHECKLIST);
                Log.d(getClass().getName(), "Database updated successfully to version 6 (created checklist table)");
            case 6:
                db.execSQL("ALTER TABLE " + NotesDB.Note.TABLE_NAME + " ADD COLUMN " + NotesDB.Note.COLUMN_NAME_DELETED + " INTEGER DEFAULT 0");
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

    public void merge(Context context) {
        SQLiteDatabase db = this.getReadableDatabase();
        NotesDBHelper notesDbHelper = new NotesDBHelper(context);

        int orig_id;
        int new_id;

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
                NotesDB.Note.COLUMN_NAME_CHECKLIST
        };

        Cursor cursor = db.query(NotesDB.Note.TABLE_NAME, projection, null, null, null, null, NotesDB.Note._ID);
        if (cursor.moveToFirst()) {
            do {
                new_id = notesDbHelper.addOrUpdateNote(cursor.getInt(0),
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
                        0);

                orig_id = cursor.getInt(0);

                String[] projection1 = {
                        NotesDB.Checklist.COLUMN_NAME_NOTE_ID,
                        NotesDB.Checklist.COLUMN_NAME_ITEM,
                        NotesDB.Checklist.COLUMN_NAME_CHECKED
                };

                SQLiteDatabase db1 = notesDbHelper.getWritableDatabase();

                Cursor checklistCursor = db.query(NotesDB.Checklist.TABLE_NAME, projection1,
                        NotesDB.Checklist.COLUMN_NAME_NOTE_ID + " = ? ", new String[]{String.valueOf(orig_id)},
                        null, null, NotesDB.Checklist.COLUMN_NAME_NOTE_ID);

                if (checklistCursor.moveToFirst()) {
                    do {
                        db1.delete(NotesDB.Checklist.TABLE_NAME,
                                NotesDB.Checklist.COLUMN_NAME_NOTE_ID + " = ? AND "
                                        + NotesDB.Checklist.COLUMN_NAME_ITEM + " = '" + checklistCursor.getString(1) + "'", new String[]{String.valueOf(new_id)});
                        ContentValues checklistValues = new ContentValues();
                        checklistValues.put(NotesDB.Checklist.COLUMN_NAME_NOTE_ID, new_id);
                        checklistValues.put(NotesDB.Checklist.COLUMN_NAME_ITEM, checklistCursor.getString(1));
                        checklistValues.put(NotesDB.Checklist.COLUMN_NAME_CHECKED, cursor.getInt(2));
                        db1.insertWithOnConflict(NotesDB.Checklist.TABLE_NAME, null, checklistValues, SQLiteDatabase.CONFLICT_REPLACE);
                    } while (checklistCursor.moveToNext());
                }
                checklistCursor.close();
                db1.close();

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
    }

}
