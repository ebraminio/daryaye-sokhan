package ir.ham3da.darya;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

import ir.ham3da.darya.ganjoor.GanjoorDbBrowser;
import ir.ham3da.darya.ganjoor.GanjoorPoem;
import ir.ham3da.darya.ui.main.SectionsPagerAdapter;
import ir.ham3da.darya.utility.AppSettings;
import ir.ham3da.darya.utility.CustomProgress;
import ir.ham3da.darya.utility.LangSettingList;
import ir.ham3da.darya.utility.MyDialogs;
import ir.ham3da.darya.utility.SetLanguage;
import ir.ham3da.darya.utility.UtilFunctions;

public class ActivityMain extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public int booksCount = 0;
    MainActivityUtil mainActivityUtil1;
    UtilFunctions UtilFunctions1;
    MyDialogs MyDialogs1;
    String TAG = "ActivityMain";
    DrawerLayout drawer;
    int currentLocalIndex;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(SetLanguage.wrap(newBase));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppSettings.Init(this);
        LangSettingList langSetting = AppSettings.getLangSettingList(this);
        currentLocalIndex = langSetting.getId();

        mainActivityUtil1 = new MainActivityUtil(this);
        UtilFunctions1 = new UtilFunctions(this);
        MyDialogs1 = new MyDialogs(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        String DB_PATH = AppSettings.getDatabasePath(this);
        Log.e(TAG, "DB_PATH: "+DB_PATH);
        boolean exist_db = MainActivityUtil.checkExists(DB_PATH);

        if (exist_db) {
            loadPager();
        } else {
            mainActivityUtil1.extractGangoorDB(DB_PATH);
        }
    }

    public void LoadDBFirstTime(CustomProgress dlg1) {
        GanjoorDbBrowser GanjoorDbBrowser1 = new GanjoorDbBrowser(this);
        dlg1.dismiss();
        loadPager();
    }

    public void loadPager() {
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
    }

    @Override
    public void onBackPressed() {
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
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        GanjoorDbBrowser GanjoorDbBrowser1 = new GanjoorDbBrowser(this);
        Intent intent;
        int id = item.getItemId();
        switch (id) {
            case R.id.action_search:
                intent = new Intent(this, ActivitySearch.class);
                this.startActivity(intent);
                Bungee.card(this);
                break;
            case R.id.action_add_colection:
                ShowCollectionAct();
                break;

            case R.id.action_random_poem:

                try {

                    String randomSelectedCategories = AppSettings.getRandomSelectedCategories();

                    GanjoorPoem poem = GanjoorDbBrowser1.getPoemRandom(randomSelectedCategories);

                    if(poem != null) {

                        intent = new Intent(this, ActivityPoem.class);
                        intent.putExtra("poem_id", poem._ID);
                        startActivity(intent);
                        Bungee.spin(this);
                    }
                    else
                    {
                        Toast.makeText(this, R.string.nothing_found, Toast.LENGTH_SHORT).show();
                    }
                }
                catch (Exception ex)
                {
                    Log.e(TAG, "getPoemRandom: "+ex.getMessage() );
                }


                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean DOWNLOADEDING_NEW_POET = false;

    public void ShowCollectionAct() {

        if (UtilFunctions.isNetworkConnected(this)) {
            DOWNLOADEDING_NEW_POET = true;
            Intent intent = new Intent(this, ActivityCollection.class);
            this.startActivity(intent);
            Bungee.card(this);


        } else {
            MyDialogs1.ShowWarningMessage(getString(R.string.internet_failed));
        }
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {


        final int id = menuItem.getItemId();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent;
                switch (id) {
                    case R.id.collections:
                        ShowCollectionAct();
                        break;

                    case R.id.nav_last_read:

                        int getLastPoemIdVisited = AppSettings.getLastPoemIdVisited();
                        if (getLastPoemIdVisited > 0) {
                            intent = new Intent(ActivityMain.this, ActivityPoem.class);
                            intent.putExtra("poem_id", getLastPoemIdVisited);
                            startActivity(intent);
                            Bungee.card(ActivityMain.this);
                        } else {
                            Toast.makeText(ActivityMain.this, R.string.nothing_found, Toast.LENGTH_LONG).show();
                        }

                        break;

                    case R.id.nav_social_networks:
                        MyDialogs1.socialNetworks();
                        break;


                    case R.id.nav_help:
                        MyDialogs1.showHelp();
                        break;

                    case R.id.nav_setting:

                        intent = new Intent(ActivityMain.this, ActivitySettings.class);
                        startActivity(intent);
                        Bungee.card(ActivityMain.this);

                        break;
                    case R.id.nav_about:

                        MyDialogs1.showAbout();

                        break;
                    case R.id.nav_rating:
                        UtilFunctions1.gotoRateing();

                        break;
                    case R.id.nav_contact_us:

                        MyDialogs1.ShowContactUs();

                        break;
                    case R.id.nav_share:

                        UtilFunctions1.shareApp();

                        break;
                    case R.id.nav_policy:

                        MyDialogs1.showPolicy();
                        break;
                }
            }
        }, 300);


        // Handle navigation view item clicks here.

        if (drawer.isDrawerOpen(GravityCompat.START)) {

            drawer.closeDrawer(GravityCompat.START);
        }
        return true;

    }


    public void restartApp() {
        Intent intent = new Intent(getBaseContext(), ActivityMain.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    //### custom functions start

    /**
     * Set Count of Poets And Books in textview
     */
    public void setCountPoetsAndBooks() {
        try {
            GanjoorDbBrowser GanjoorDbBrowser1 = new GanjoorDbBrowser(this);
            int getPoetsCount = GanjoorDbBrowser1.getPoetsCount();
            int getBooksCount = booksCount;
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);
            View hview = navigationView.getHeaderView(0);
            TextView textView_all_count = (TextView) hview.findViewById(R.id.textView_all_count);
            String str_count_all_word = String.format(Locale.getDefault(), getString(R.string.nav_header_subtitle), getBooksCount, getPoetsCount);
            textView_all_count.setText(str_count_all_word);

        } catch (Exception ex) {
            Log.e(TAG, "setCountPoetsAndBooks: " + ex.getMessage());
        }


    }

    @Override
    protected void onResume() {
        super.onResume();

        LangSettingList langSetting = AppSettings.getLangSettingList(this);

        if (currentLocalIndex != langSetting.getId()) {
            currentLocalIndex = langSetting.getId();
            recreate();
        }

    }

    //### custom functions end

}