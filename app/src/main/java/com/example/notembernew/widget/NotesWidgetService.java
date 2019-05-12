package com.example.notembernew.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;

import com.example.notembernew.Constants;
import com.example.notembernew.NoteObj;
import com.example.notembernew.R;
import com.example.notembernew.data.NotesDB;
import com.example.notembernew.data.NotesDBHelper;
import com.example.notembernew.ui.MainActivity;
import com.example.notembernew.ui.ActivityChecklist;
import com.example.notembernew.ui.AddNotes;
public class NotesWidgetService extends RemoteViewsService {
    private ArrayList<NoteObj> notes;

    @Override
    public RemoteViewsService.RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListRemoteViewsFactory(this.getApplicationContext(), intent);
    }

    class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
        private final Context context;

        public ListRemoteViewsFactory(Context applicationContext, Intent intent) {
            context = applicationContext;
        }

        @Override
        public void onCreate() {
            NotesDBHelper NotesDBHelper = new NotesDBHelper(context);
            notes = NotesDBHelper.getAllNotes(0, 0);
        }

        @Override
        public void onDataSetChanged() {
            NotesDBHelper NotesDBHelper = new NotesDBHelper(context);
            notes = NotesDBHelper.getAllNotes(0, 0);
        }

        @Override
        public void onDestroy() {
        }

        @Override
        public int getCount() {
            if (notes != null)
                return notes.size();
            return 0;
        }

        @Override
        public RemoteViews getViewAt(int position) {
            NoteObj note = notes.get(position);
            RemoteViews remoteViews = new RemoteViews(this.context.getPackageName(), R.layout.note_layout_widget);
            if (note != null) {
                if ("".equals(note.getTitle()))
                    remoteViews.setViewVisibility(R.id.note_title, View.GONE);
                else remoteViews.setTextViewText(R.id.note_title, note.getTitle());
                if ("".equals(note.getSubtitle()))
                    remoteViews.setViewVisibility(R.id.note_subtitle, View.GONE);
                else remoteViews.setTextViewText(R.id.note_subtitle, note.getSubtitle());
                remoteViews.setTextViewText(R.id.note_content, note.getContent());
                remoteViews.setTextViewText(R.id.note_date, note.getTime());

                Bundle bundle = new Bundle();
                bundle.putInt(NotesDB.Note._ID, note.getId());
                bundle.putString(NotesDB.Note.COLUMN_NAME_TITLE, note.getTitle());
                bundle.putString(NotesDB.Note.COLUMN_NAME_SUBTITLE, note.getSubtitle());
                bundle.putString(NotesDB.Note.COLUMN_NAME_CONTENT, note.getContent());
                bundle.putString(NotesDB.Note.COLUMN_NAME_TIME, note.getTime());
                bundle.putString(NotesDB.Note.COLUMN_NAME_CREATED_AT, note.getCreated_at());
                bundle.putInt(NotesDB.Note.COLUMN_NAME_NOTIFIED, note.getNotified());
                bundle.putInt(NotesDB.Note.COLUMN_NAME_ARCHIVED, note.getArchived());
                bundle.putString(NotesDB.Note.COLUMN_NAME_COLOR, note.getColor());
                bundle.putInt(NotesDB.Note.COLUMN_NAME_ENCRYPTED, note.getEncrypted());
                bundle.putInt(NotesDB.Note.COLUMN_NAME_PINNED, note.getPinned());
                bundle.putInt(NotesDB.Note.COLUMN_NAME_TAG, note.getTag());
                bundle.putString(NotesDB.Note.COLUMN_NAME_REMINDER, note.getReminder());
                bundle.putInt(NotesDB.Note.COLUMN_NAME_CHECKLIST, note.getChecklist());
                bundle.putInt(NotesDB.Note.COLUMN_NAME_DELETED, note.getDeleted());
                Intent fillInIntent = new Intent();
                fillInIntent.putExtra(Constants.NOTE_DETAILS_BUNDLE, bundle);
                // Make it possible to distinguish the individual on-click
                // action of a given item
                remoteViews.setOnClickFillInIntent(R.id.note_card_widget, fillInIntent);
            }

            return remoteViews;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public long getItemId(int position) {
            NoteObj noteObj = notes.get(position);
            if (noteObj != null) {
                return noteObj.getId();
            }
            return 0;
        }

    }}

