package io.phobotic.pavillion;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import java.text.ParseException;

import io.phobotic.pavillion.checkdigit.CheckDigitFactory;
import io.phobotic.pavillion.checkdigit.CheckDigits;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private boolean cardShown = false;
    private FloatingActionButton fab;
    private CardView digitsCard;
    private EditText input;
    private TextView cardLocation;
    private TextView mainCheckdigit;
    private TextView leftCheckdigit;
    private TextView middleCheckdigit;
    private TextView rightCheckdigit;
    private boolean fabShown;

    private void init() {
        initFab();
        initCard();
        initInput();
    }

    private void initInput() {
        input = (EditText) findViewById(R.id.location_input);
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
        input.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // nothing to do
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                input.setTextColor(getResources().getColor(R.color.location_unverified));
                if (cardShown) {
                    circularHide();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // nothing to do
            }
        });
    }

    private void initCard() {
        digitsCard = (CardView) findViewById(R.id.card_digits);
        cardLocation = (TextView) findViewById(R.id.cd_location);
        mainCheckdigit = (TextView) findViewById(R.id.cd_main);
        leftCheckdigit = (TextView) findViewById(R.id.cd_left);
        middleCheckdigit = (TextView) findViewById(R.id.cd_middle);
        rightCheckdigit = (TextView) findViewById(R.id.cd_right);
    }

    private void initFab() {
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateCheckdigits();
            }
        });
        fab.setVisibility(View.INVISIBLE);
        fabShown = false;
    }

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

        init();
    }

    private void calculateCheckdigits() {
        Log.d(TAG, "performing checkdigt lookup");
        String location = input.getText().toString();

        try {
            CheckDigits checkDigits = CheckDigitFactory.fromString(location);
            cardLocation.setText(location);
            mainCheckdigit.setText(checkDigits.getMainCheckdigit());
            leftCheckdigit.setText(checkDigits.getLeftCheckdigit());
            middleCheckdigit.setText(checkDigits.getMiddleCheckdigit());
            rightCheckdigit.setText(checkDigits.getRightCheckdigit());
            input.setTextColor(getResources().getColor(R.color.location_verified));
            showCard();
        } catch (ParseException e) {
            mainCheckdigit.setText("");
            leftCheckdigit.setText("");
            middleCheckdigit.setText("");
            rightCheckdigit.setText("");
            if (cardShown) {
                hideCard();
            }
        }
    }

    private void showCard() {
        transitionFab();
    }

    private void transitionFab() {
        if (!fabShown) {
            showFab();
        }

        final float fabX = fab.getX();
        final float fabY = fab.getY();

        float offsetX = digitsCard.getWidth() / 2;
        float offsetY = digitsCard.getHeight() / 2;
        float offsetFabX = fab.getWidth() / 2;
        float offsetFabY = fab.getHeight() / 2;
        float startX = digitsCard.getX();
        float startY = digitsCard.getY();
        float cx = startX + offsetX - offsetFabX;
        float cy = startY + offsetY - offsetFabY;

        ObjectAnimator moveX = ObjectAnimator.ofFloat(fab, "x", cx );
        ObjectAnimator moveY = ObjectAnimator.ofFloat(fab, "y", cy );
        moveX.setInterpolator(new DecelerateInterpolator());
        moveY.setInterpolator(new AccelerateInterpolator());
        AnimatorSet as = new AnimatorSet();
        as.playTogether(moveX, moveY);
        as.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                circularReveal();
                fab.setVisibility(View.INVISIBLE);
                fabShown = false;
                fab.setX(fabX);
                fab.setY(fabY);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        as.start();
    }

    private void circularReveal() {
        int cx = digitsCard.getWidth() / 2;
        int cy = digitsCard.getHeight() / 2;
        float radius = (float) Math.hypot(cx, cy);
        if (Build.VERSION.SDK_INT >= 21) {
            Animator anim = ViewAnimationUtils.createCircularReveal(digitsCard, cx, cy, 0, radius);
            anim.start();
        }

        digitsCard.setVisibility(View.VISIBLE);
        cardShown = true;
    }

    private void hideCard() {
        circularHide();

    }

    private void circularHide() {
        //declare the card hidden early to prevent multiple keypresses
        // from triggering duplicate animations
        cardShown = false;
        final View card = findViewById(R.id.card_digits);
        int cx = card.getWidth() / 2;
        int cy = card.getHeight() / 2;
        float radius = (float) Math.hypot(cx, cy);
        if (Build.VERSION.SDK_INT >= 21) {
            Animator anim = ViewAnimationUtils.createCircularReveal(card, cx, cy, radius, 0);
            anim.start();
            anim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    //
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    card.setVisibility(View.INVISIBLE);
                    showFab();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    //
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                    //
                }
            });
        } else {
            card.setVisibility(View.INVISIBLE);
            cardShown = false;
            showFab();
        }
    }

    private void showFab() {
        if (!fabShown) {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(fab, "scaleX", 0, 1);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(fab, "scaleY", 0, 1);
            AnimatorSet animSetXY = new AnimatorSet();
            animSetXY.playTogether(scaleX, scaleY);
            animSetXY.setInterpolator(new AccelerateInterpolator());
            animSetXY.setDuration(500);
            fab.setVisibility(View.VISIBLE);
            animSetXY.start();
            fabShown = true;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        fab.setVisibility(View.INVISIBLE);
        fabShown = false;
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

    public void onResume() {
        super.onResume();
        final EditText input = (EditText) findViewById(R.id.location_input);
        input.requestFocus();
        showFab();
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
