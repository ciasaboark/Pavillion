package io.phobotic.pavillion;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import java.text.ParseException;

import io.phobotic.pavillion.checkdigit.CheckDigits;
import io.phobotic.pavillion.checkdigit.CheckDigitFactory;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateCheckdigits();
            }
        });

        final EditText input = (EditText) findViewById(R.id.location_input);
        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    calculateCheckdigits();
                    return true;
                } else {
                    Log.e(TAG, "action not recognized, will not act");
                }
                return false;
            }
        });
        input.setFilters(new InputFilter[] {new InputFilter.AllCaps()});
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // nothing to do
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextView location = (TextView) findViewById(R.id.location);
                location.setText(s);
                location.setTextColor(getResources().getColor(R.color.location_unverified));
//                calculateCheckdigits();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // nothing to do
            }
        });
//        input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (!hasFocus) {
//                    input.requestFocus();
//                }
//            }
//        });
    }

    public void onResume() {
        super.onResume();
        final EditText input = (EditText) findViewById(R.id.location_input);
        input.requestFocus();
    }

    private void calculateCheckdigits() {
        Log.d(TAG, "performing checkdigt lookup");
        TextView locationText = (TextView) findViewById(R.id.location);
        TextView mainCheckdigit = (TextView) findViewById(R.id.cd_main);
        TextView leftCheckdigit = (TextView) findViewById(R.id.cd_left);
        TextView middleCheckdigit = (TextView) findViewById(R.id.cd_middle);
        TextView rightCheckdigit = (TextView) findViewById(R.id.cd_right);
        EditText ed = (EditText) findViewById(R.id.location_input);
        String location = ed.getText().toString();

        try {
            CheckDigits checkDigits = CheckDigitFactory.fromString(location);
            mainCheckdigit.setText(checkDigits.getMainCheckdigit());
            leftCheckdigit.setText(checkDigits.getLeftCheckdigit());
            middleCheckdigit.setText(checkDigits.getMiddleCheckdigit());
            rightCheckdigit.setText(checkDigits.getRightCheckdigit());
            locationText.setTextColor(getResources().getColor(R.color.location_verified));
        } catch (ParseException e) {
            mainCheckdigit.setText("");
            leftCheckdigit.setText("");
            middleCheckdigit.setText("");
            rightCheckdigit.setText("");
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_checkdigit) {
            // Switch to main fragment
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
