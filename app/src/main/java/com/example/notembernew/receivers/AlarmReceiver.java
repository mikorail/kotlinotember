package com.example.notembernew.receivers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Bundle;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import android.text.TextUtils;
import android.util.Log;

import com.example.notembernew.Constants;
import com.example.notembernew.NoteObj;
import com.example.notembernew.R;
import com.example.notembernew.data.NotesDB;
import com.example.notembernew.data.NotesDBHelper;
import com.example.notembernew.ui.ActivityChecklist;
import com.example.notembernew.ui.NoteActivity;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by Samriddha Basu on 02-07-2017.
 */

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        Bundle bundle = intent.getBundleExtra(Constants.NOTE_DETAILS_BUNDLE);
        if (bundle != null) {
            int id = bundle.getInt(NotesDB.Note._ID);

            NotesDBHelper notesDbHelper = new NotesDBHelper(context);
            notesDbHelper.updateFlag(id, NotesDB.Note.COLUMN_NAME_REMINDER, Constants.REMINDER_NONE);

            NoteObj note = notesDbHelper.getNote(id);

            if (note != null && note.getDeleted() == 0) {

                String title = note.getTitle();
                String subtitle = note.getSubtitle();
                String content = note.getContent();
                String time = note.getTime();
                String info;

                if (subtitle != null && !subtitle.equals("")) info = subtitle;
                else info = time;
                NotificationCompat.Builder notif =
                        new NotificationCompat.Builder(context)
                                .setSmallIcon(R.drawable.launch_icon)
                                .setContentText(content)
                                .setSubText(info)
                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                .setCategory(context.getString(R.string.reminders))
                                .setGroup(NotesDB.Note.COLUMN_NAME_REMINDER)
                                .setColor(Color.argb(255, 32, 128, 200));

                if (!TextUtils.isEmpty(title)) {
                    notif.setContentTitle(title);
                } else {
                    notif.setContentTitle(context.getString(R.string.note));
                }

                notif.setStyle(new NotificationCompat.BigTextStyle().bigText(content).setSummaryText(time));
                // Sets an ID for the notification
                Log.d("NOTIFICATION ID", String.valueOf(id));

                Class c;
                if (note.getChecklist() == 0)
                    c = NoteActivity.class;
                else
                    c = ActivityChecklist.class;
                Intent resultIntent = new Intent(context, c);

                bundle.putInt(NotesDB.Note._ID, id);
                bundle.putString(NotesDB.Note.COLUMN_NAME_TITLE, title);
                bundle.putString(NotesDB.Note.COLUMN_NAME_SUBTITLE, subtitle);
                bundle.putString(NotesDB.Note.COLUMN_NAME_CONTENT, content);
                bundle.putString(NotesDB.Note.COLUMN_NAME_TIME, time);
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
                resultIntent.putExtra(Constants.NOTE_DETAILS_BUNDLE, bundle);
                resultIntent.setAction("REMINDER_NOTE_" + id);

                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                stackBuilder.addParentStack(c);
                // Adds the Intent to the top of the stack
                stackBuilder.addNextIntent(resultIntent);
                // Gets a PendingIntent containing the entire back stack
                PendingIntent resultPendingIntent =
                        stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                notif.setContentIntent(resultPendingIntent);

                notif.setAutoCancel(true);
                notif.setDeleteIntent(PendingIntent.getBroadcast(context, id, new Intent(context, BootReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT));
                notif.setChannelId(Constants.CHANNEL_ID_REMINDER);

                SharedPreferences pref = context.getSharedPreferences(Constants.PREFS, MODE_PRIVATE);
                if (pref.getBoolean(Constants.REMINDER_SOUND, true))
                    notif.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                if (pref.getBoolean(Constants.REMINDER_VIBRATE, true))
                    notif.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000, 1000});
                else notif.setVibrate(new long[]{0, 0});
                if (pref.getBoolean(Constants.REMINDER_LED, true))
                    notif.setLights(Color.argb(255, 32, 128, 200), 1000, 1000);

                // Builds the notification and issues it.
                mNotifyMgr.notify(id, notif.build());
            }
        }
    }
}
