package com.example.notembernew.ui;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.notembernew.BuildConfig;
import com.example.notembernew.Constants;
import com.example.notembernew.R;


public class AboutActivity extends AppCompatActivity{
    SharedPreferences pref;
    String versionInfo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        pref = getSharedPreferences(Constants.PREFS, MODE_PRIVATE);
        if (pref.getBoolean(Constants.LIGHT_THEME, false))
            setTheme(R.style.AppTheme_Light_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        versionInfo = getString(R.string.package_name) + BuildConfig.APPLICATION_ID + "\n"
                + getString(R.string.version_name) + BuildConfig.VERSION_NAME + "\n"
                + getString(R.string.version_code) + BuildConfig.VERSION_CODE;

        TextView versionButton = ((TextView) findViewById(R.id.versionText));
        versionButton.setText(getString(R.string.version_name) + BuildConfig.VERSION_NAME);
        versionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(AboutActivity.this);
                alert.setTitle(getString(R.string.app_name))
                        .setMessage(versionInfo)
                        .show();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home)
            onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    public void onLinkClick(View v) {
        String text = ((TextView) v).getText().toString();
        String uri = "";
        if (text.equals(getString(R.string.github))) {
            uri = getString(R.string.link_github);
        } else if (text.equals(getString(R.string.rate_app))) {
            uri = getString(R.string.link_play_store_app);
        } else if (text.equals(getString(R.string.source))) {
            uri = getString(R.string.link_github_app);
        }
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(i);
    }

    public void showIntro(View v) {
        Intent i = new Intent(this, IntroActivity.class);
        startActivity(i);
    }


    public void shareApp(View view) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.putExtra(Intent.EXTRA_TEXT, getString(R.string.check_out) + " \"" + getString(R.string.app_name) + "\" " + getString(R.string.on_play_store) + ":\n" + getString(R.string.link_play_store_app));
        share.setType("text/plain");
        if (share.resolveActivity(getPackageManager()) != null) {
            startActivity(Intent.createChooser(share, getResources().getString(R.string.share_via)));
        } else {
            Toast.makeText(this, R.string.no_share_app_found, Toast.LENGTH_SHORT).show();
        }
    }

}
