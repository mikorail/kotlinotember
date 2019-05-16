package com.example.notembernew.ui;
import android.animation.ObjectAnimator;
import android.app.LoaderManager;
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
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.notembernew.R;
import com.example.notembernew.data.NotesDB;
import com.example.notembernew.data.NotesDBHelper;
import com.example.notembernew.NoteObj;
import com.example.notembernew.Constants;
import com.example.notembernew.receivers.BootReceiver;
import com.example.notembernew.data.NotesProvider;
import com.example.notembernew.ui.adapter.NotesAdapter;
import com.example.notembernew.widget.NotesWidget;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {
    public NotesAdapter mAdapter;
    protected NotesDBHelper dbHelper;
    boolean lightTheme, changed;
    ArrayList<NoteObj> noteObjArrayList;
    RecyclerView recyclerView;
    StaggeredGridLayoutManager layoutManager;
    TextView blankText;
    View fab;
    SharedPreferences pref;
    CardView adContainer;
    View addNoteView, addListView,addDrawView, shadow;
    View.OnClickListener fabClickListener, addNoteListener, addListListener,addDrawListener;
    boolean fabOpen = false;
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            changed = true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        pref = getSharedPreferences(Constants.PREFS, MODE_PRIVATE);

        //  Declare a new thread to do a preference check
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                //  Initialize SharedPreferences
                //  Create a new boolean and preference and set it to true
                boolean isFirstStart = pref.getBoolean(Constants.FIRST_START, true);
                //  If the activity has never started before...
                if (isFirstStart) {
                    //  Launch app intro
                    Intent i = new Intent(MainActivity.this, IntroActivity.class);
                    startActivity(i);

                    //  Make a new preferences editor
                    SharedPreferences.Editor e = pref.edit();
                    //  Edit preference to make it false because we don't want this to run again
                    e.putBoolean(Constants.FIRST_START, false);
                    //  Apply changes
                    e.apply();
                }
                new BootReceiver().onReceive(MainActivity.this, null);
            }
        });
        // Start the thread
        t.start();

        lightTheme = pref.getBoolean(Constants.LIGHT_THEME, false);
        if (lightTheme)
            setTheme(R.style.AppTheme_Light);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getLoaderManager().initLoader(0, null, this);

        fab = findViewById(R.id.fab);
        addNoteView = findViewById(R.id.note_fab);
        addDrawView =findViewById(R.id.draw_fab);
        addListView = findViewById(R.id.list_fab);
        shadow = findViewById(R.id.shadow);

        fabClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fabOpen) {
                    closeFabMenu();
                } else {
                    showFabMenu();
                }
            }
        };

        addDrawListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeFabMenu();
                Intent i = new Intent(MainActivity.this, DrawActivity.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this);
                    startActivity(i, options.toBundle());
                } else startActivity(i);

            }
        };
        addNoteListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeFabMenu();
                Intent i = new Intent(MainActivity.this, NoteActivity.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this);
                    startActivity(i, options.toBundle());
                } else startActivity(i);

            }
        };

        addListListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeFabMenu();
                Intent i = new Intent(MainActivity.this, ActivityChecklist.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this);
                    startActivity(i, options.toBundle());
                } else startActivity(i);

            }
        };

        fab.setOnClickListener(fabClickListener);
        shadow.setOnClickListener(fabClickListener);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        dbHelper = new NotesDBHelper(this);

        blankText = findViewById(R.id.blankTextView);

        noteObjArrayList = new ArrayList<>();

        recyclerView = findViewById(R.id.recyclerView);

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("note-list-changed"));

    }

    @Override
    protected void onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

    public float convertDpToPixel(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private void showFabMenu() {
        fab.setOnClickListener(null);
        fab.animate().rotationBy(45f).setDuration(300).start();
        fabOpen = true;
        addNoteView.setVisibility(View.VISIBLE);
        addNoteView.setAlpha(0f);
        addNoteView.animate().translationYBy(convertDpToPixel(-52)).alpha(1f).setDuration(300).start();
        addDrawView.setVisibility(View.VISIBLE);
        addDrawView.setAlpha(0f);
        addDrawView.animate().translationYBy(convertDpToPixel(-148)).alpha(1f).setDuration(300).start();
        addListView.setVisibility(View.VISIBLE);
        addListView.setAlpha(0f);
        addListView.animate().translationYBy(convertDpToPixel(-100)).alpha(1f).setDuration(300).start();
        shadow.setVisibility(View.VISIBLE);
        shadow.setAlpha(0f);
        shadow.animate().alpha(1f).setDuration(300).start();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                fab.setOnClickListener(fabClickListener);
                addNoteView.setOnClickListener(addNoteListener);
                addListView.setOnClickListener(addListListener);
                addDrawView.setOnClickListener(addDrawListener);
            }
        }, 300);
    }

    private void closeFabMenu() {
        fab.setOnClickListener(null);
        ObjectAnimator.ofFloat(fab, "rotation", 45f, 0f).start();
        fabOpen = false;
        addDrawView.animate().translationYBy(convertDpToPixel(148)).alpha(0f).setDuration(300).start();
        addNoteView.animate().translationYBy(convertDpToPixel(52)).alpha(0f).setDuration(300).start();
        addListView.animate().translationYBy(convertDpToPixel(100)).alpha(0f).setDuration(300).start();
        shadow.animate().alpha(0f).setDuration(300).start();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                addNoteView.setVisibility(View.GONE);
                addListView.setVisibility(View.GONE);
                addDrawView.setVisibility( View.GONE);
                shadow.setVisibility(View.GONE);
                addNoteView.setOnClickListener(null);
                addListView.setOnClickListener(null);
                addDrawView.setOnClickListener(null);
                fab.setOnClickListener(fabClickListener);
            }
        }, 300);
    }

    // setup the UI to match the currently selected section
    private void setupUI() {
        boolean archive = pref.getBoolean(Constants.ARCHIVE, false);
        boolean trash = pref.getBoolean(Constants.TRASH, false);
        int list_mode = pref.getInt(Constants.LIST_MODE, 0);
        if (trash) {
            getSupportActionBar().setTitle(R.string.trash);
            blankText.setText(R.string.blank_trash);
            fab.setVisibility(View.GONE);
        } else if (archive) {
            getSupportActionBar().setTitle(R.string.archive);
            blankText.setText(R.string.blank_archive);
            fab.setVisibility(View.GONE);
        } else if (list_mode == 1) {
            getSupportActionBar().setTitle(R.string.notes);
            blankText.setText(R.string.blank_notes);
            getLoaderManager().restartLoader(0, null, this);
            fab.setVisibility(View.VISIBLE);
        } else if (list_mode == 2) {
            getSupportActionBar().setTitle(R.string.checklists);
            blankText.setText(R.string.blank_checklists);
            getLoaderManager().restartLoader(0, null, this);
            fab.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        setupUI();

        if (changed) {
            getLoaderManager().restartLoader(0, null, this);
            updateWidgets();
            changed = false;
        }

        if (lightTheme != pref.getBoolean(Constants.LIGHT_THEME, false)) {
            if (lightTheme)
                setTheme(R.style.AppTheme_NoActionBar);
            else setTheme(R.style.AppTheme_Light_NoActionBar);
            lightTheme = !lightTheme;
            recreate();
        }

        super.onResume();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        if (lightTheme) {
            Drawable drawable = menu.getItem(0).getIcon();
            drawable.mutate();
            drawable.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_IN);
        }

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.one_column) {
            pref.edit().putInt(Constants.NUM_COLUMNS, 1).apply();
            getLoaderManager().restartLoader(0, null, this);
        } else if (id == R.id.two_column) {
            pref.edit().putInt(Constants.NUM_COLUMNS, 2).apply();
            getLoaderManager().restartLoader(0, null, this);
        } else if (id == R.id.newer) {
            pref.edit().putBoolean(Constants.OLDEST_FIRST, false).apply();
            getLoaderManager().restartLoader(0, null, this);
        } else if (id == R.id.older) {
            pref.edit().putBoolean(Constants.OLDEST_FIRST, true).apply();
            getLoaderManager().restartLoader(0, null, this);
        } else if (id == R.id.search) {
            startActivity(new Intent(this, SearchActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.beranda) {
            pref.edit()
                    .putBoolean(Constants.ARCHIVE, false)
                    .putBoolean(Constants.TRASH, false)
                    .putInt(Constants.LIST_MODE, 0).apply();
            getSupportActionBar().setTitle(R.string.app_name);
            blankText.setText(R.string.blank);
            getLoaderManager().restartLoader(0, null, this);
            fab.setVisibility(View.VISIBLE);
        } else if (id == R.id.nav_notes) {
            pref.edit()
                    .putBoolean(Constants.ARCHIVE, false)
                    .putBoolean(Constants.TRASH, false)
                    .putInt(Constants.LIST_MODE, 1).apply();
            getSupportActionBar().setTitle(R.string.notes);
            blankText.setText(R.string.blank_notes);
            getLoaderManager().restartLoader(0, null, this);
            fab.setVisibility(View.VISIBLE);
        } else if (id == R.id.nav_checklists) {
            pref.edit()
                    .putBoolean(Constants.ARCHIVE, false)
                    .putBoolean(Constants.TRASH, false)
                    .putInt(Constants.LIST_MODE, 2).apply();
            getSupportActionBar().setTitle(R.string.checklists);
            blankText.setText(R.string.blank_checklists);
            getLoaderManager().restartLoader(0, null, this);
            fab.setVisibility(View.VISIBLE);
        } else if (id == R.id.label) {
            pref.edit()
                    .putBoolean(Constants.ARCHIVE, true)
                    .putBoolean(Constants.TRASH, false)
                    .putInt(Constants.LIST_MODE, 0).apply();
            getSupportActionBar().setTitle(R.string.archive);
            blankText.setText(R.string.blank_archive);
            getLoaderManager().restartLoader(0, null, this);
            fab.setVisibility(View.GONE);
        } else if (id == R.id.sampah) {
            pref.edit()
                    .putBoolean(Constants.ARCHIVE, false)
                    .putBoolean(Constants.TRASH, true)
                    .putInt(Constants.LIST_MODE, 0).apply();
            getSupportActionBar().setTitle(R.string.trash);
            blankText.setText(R.string.blank_trash);
            getLoaderManager().restartLoader(0, null, this);
            fab.setVisibility(View.GONE);
        } else if (id == R.id.nav_about) {
            Intent i = new Intent(this, AboutActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_settings) {
            Intent i = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(i);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void updateWidgets() {
        Intent intent = new Intent();
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        AppWidgetManager man = AppWidgetManager.getInstance(this);
        int[] ids = man.getAppWidgetIds(
                new ComponentName(this, NotesWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);
    }

    @Override
    public android.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.
        Uri.Builder builder = NotesProvider.BASE_URI.buildUpon().appendPath(NotesDB.Note.TABLE_NAME);
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

        String sort;
        if (pref.getBoolean(Constants.OLDEST_FIRST, false))
            sort = " ASC";
        else
            sort = " DESC";

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
        return new CursorLoader(this, baseUri,
                projection, selection.toString(), null,
                NotesDB.Note.COLUMN_NAME_TIME + sort);
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor cursor) {
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
            layoutManager = new StaggeredGridLayoutManager(pref.getInt(Constants.NUM_COLUMNS, 1), StaggeredGridLayoutManager.VERTICAL);
            layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(mAdapter);
            if (recyclerViewState != null) {
                layoutManager.onRestoreInstanceState(recyclerViewState);
                recyclerView.smoothScrollToPosition(0);
            }

        }

        if (noteObjArrayList.size() == 0)
            blankText.setVisibility(View.VISIBLE);
        else
            blankText.setVisibility(View.GONE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
