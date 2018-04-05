package andy_eyad_ucsc.splitthetab.presenter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import andy_eyad_ucsc.splitthetab.R;
import andy_eyad_ucsc.splitthetab.model.AppUser;
import andy_eyad_ucsc.splitthetab.model.DatabaseHandler;
import andy_eyad_ucsc.splitthetab.model.PaymentHandler;
import andy_eyad_ucsc.splitthetab.presenter.login_and_signup.LoginActivity;

public class NavigationDrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private NavigationView navigationView;
    private int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                updateUserDisplayedInfo();

                if((counter % 2) == 0)
                    if (AppUser.getInstance().userIsLoggedIn()) {
                        setupDialog();
                        counter = 0;
                    }

                counter++;
            }
        };

        drawer.addDrawerListener(toggle);

        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            Fragment fragment = new SplitFragment();
            instantiateFragment(fragment);
        }

        this.setTitle("Split the Tab");
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
        getMenuInflater().inflate(R.menu.navigation_drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return item.getItemId() == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_split_view) {
            Fragment fragment = new SplitFragment();
            instantiateFragment(fragment);

        } else if (id == R.id.nav_profile) {
            Fragment fragment = new ProfileFragment();
            instantiateFragment(fragment);

        } else if (id == R.id.nav_login) {
            Intent intent = new Intent(NavigationDrawerActivity.this, LoginActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (AppUser.getInstance().userIsLoggedIn()) {
            // Instantiate database listener
            DatabaseHandler.getInstance();
        }
    }

    private void instantiateFragment(Fragment fragment) {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.navigation_drawer_detail, fragment)
                .commit();
    }

    private void updateUserDisplayedInfo() {
        View headerView = navigationView.getHeaderView(0);

        TextView userEmailTextView = headerView.findViewById(R.id.user_email_menu);
        TextView usernameTextView = headerView.findViewById(R.id.username_menu);

        if (AppUser.getInstance().userIsLoggedIn()) {
            String[] parts = AppUser.getInstance().getEmail().split("@");
            usernameTextView.setText(String.format("%s%s", parts[0].substring(0, 1).toUpperCase(), parts[0].substring(1).toLowerCase()));
            userEmailTextView.setText(AppUser.getInstance().getEmail());
        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void setupDialog() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                PaymentHandler.showOwingDialog(NavigationDrawerActivity.this, getBaseContext());
            }
        }, 2000);
    }
}
