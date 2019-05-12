package com.example.notembernew.ui;

import android.app.LoaderManager;
import android.app.SearchManager;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.TextView;

import com.example.notembernew.Constants;
import com.example.notembernew.NoteObj;
import com.example.notembernew.R;
import com.example.notembernew.data.NotesDB;
import com.example.notembernew.data.NotesProvider;
import com.example.notembernew.ui.adapter.NotesAdapter;
import com.example.notembernew.widget.NotesWidget;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, SearchView.OnQueryTextListener {
    boolean lightTheme, changed;
    ArrayList<NoteObj> noteObjArrayList;
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    TextView blankText;
    NotesAdapter mAdapter;
    String query = "";
    SharedPreferences pref;
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            changed = true;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        lightTheme = getSharedPreferences(Constants.PREFS, MODE_PRIVATE).getBoolean(Constants.LIGHT_THEME, false);
        if (lightTheme)
            setTheme(R.style.AppTheme_Light_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pref = getSharedPreferences(Constants.PREFS, MODE_PRIVATE);

        getLoaderManager().initLoader(0, null, this);

        recyclerView = this.findViewById(R.id.gridview);
        layoutManager = new LinearLayoutManager(this);
        noteObjArrayList = new ArrayList<>();

        recyclerView.setLayoutManager(layoutManager);
        blankText = findViewById(R.id.blankTextView);


        if (savedInstanceState == null)
            handleIntent(getIntent());
        else {
            noteObjArrayList = (ArrayList<NoteObj>) savedInstanceState.getSerializable("results");
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("note-list-changed"));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
            getLoaderManager().restartLoader(0, null, this);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putSerializable("results", noteObjArrayList);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (changed) {
            getLoaderManager().restartLoader(0, null, this);
            changed = false;
            updateWidgets();
        }
    }

    private void updateWidgets() {
        Intent intent = new Intent();
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        AppWidgetManager man = AppWidgetManager.getInstance(this);
        int[] ids = man.getAppWidgetIds(
                new ComponentName(this, NotesWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        androidx.appcompat.widget.SearchView searchView =
                (androidx.appcompat.widget.SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        if (pref.getBoolean(Constants.ARCHIVE, false)) {
            searchView.setQueryHint(getText(R.string.search_archive));
            blankText.setText(getText(R.string.search_no_results_archive));
        } else if (pref.getInt(Constants.LIST_MODE, 0) == 1) {
            searchView.setQueryHint(getText(R.string.search_notes));
        } else if (pref.getInt(Constants.LIST_MODE, 0) == 2) {
            searchView.setQueryHint(getText(R.string.search_checklists));
        }
        searchView.onActionViewExpanded();
        searchView.setIconified(false);
        searchView.setOnQueryTextListener(this);

        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        if (query.trim().equals("")) return new Loader<>(this);
        // This is called when a new Loader needs to be created.
        Uri.Builder builder = NotesProvider.BASE_URI.buildUpon().appendPath("join");
        Uri baseUri = builder.build();

        String[] projection = {
                NotesDB.Note.TABLE_NAME + "." + NotesDB.Note._ID,
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

        int mode = pref.getInt(Constants.LIST_MODE, 0);
        StringBuilder selection = new StringBuilder();
        switch (mode) {
            case 1: // Notes only
                selection.append(NotesDB.Note.COLUMN_NAME_CHECKLIST + " LIKE " + 0 + " AND ");
                break;
            case 2: // Checklists only
                selection.append(NotesDB.Note.COLUMN_NAME_CHECKLIST + " LIKE " + 1 + " AND ");
                break;
        }

        if (pref.getBoolean(Constants.TRASH, false)) {
            selection.append(NotesDB.Note.COLUMN_NAME_DELETED).append(" LIKE ").append(1);
        } else {
            selection.append(NotesDB.Note.COLUMN_NAME_DELETED).append(" LIKE ").append(0).append(" AND ");
            int archive = pref.getBoolean(Constants.ARCHIVE, false) ? 1 : 0;
            selection.append(NotesDB.Note.COLUMN_NAME_ARCHIVED).append(" LIKE ").append(archive);
        }
        selection.append(" AND ( ")
                .append(NotesDB.Note.COLUMN_NAME_TITLE).append(" LIKE '%").append(query)
                .append("%' OR ")
                .append(NotesDB.Note.COLUMN_NAME_SUBTITLE).append(" LIKE '%").append(query)
                .append("%' OR ")
                .append(NotesDB.Note.COLUMN_NAME_CONTENT).append(" LIKE '%").append(query)
                .append("%')");

        String sort;
        if (pref.getBoolean(Constants.OLDEST_FIRST, false))
            sort = " ASC";
        else
            sort = " DESC";

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(this, baseUri,
                projection, selection.toString(), null,
                NotesDB.Note.COLUMN_NAME_TIME + sort);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null) Log.d("onLoadFinished", "Cursor is null!");
        else {
            noteObjArrayList.clear();
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
                    noteObjArrayList.add(noteObj);
                } while (cursor.moveToNext());
            }

            Parcelable recyclerViewState = null;
            if (layoutManager != null && mAdapter != null) {
                // Save state
                recyclerViewState = layoutManager.onSaveInstanceState();
            }
            mAdapter = new NotesAdapter(this, this, cursor);
            recyclerView.setAdapter(mAdapter);
            if (recyclerViewState != null) {
                layoutManager.onRestoreInstanceState(recyclerViewState);
                recyclerView.smoothScrollToPosition(0);
            }

        }

        if (!query.trim().equals("")) {
            if (noteObjArrayList.size() == 0)
                blankText.setVisibility(View.VISIBLE);
            else
                blankText.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (!newText.equals("")) {
            query = newText;
            getLoaderManager().restartLoader(0, null, this);
            return true;
        }
        return false;
    }
}
