package io.phobotic.pavillion;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
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
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.text.ParseException;

import io.phobotic.pavillion.checkdigit.CheckDigitFactory;
import io.phobotic.pavillion.checkdigit.CheckDigits;
import io.phobotic.pavillion.database.SearchInstance;
import io.phobotic.pavillion.database.SearchesDatabase;
import io.phobotic.pavillion.listener.PhotoTakenListener;
import io.phobotic.pavillion.photo.PictureHandler;
import io.phobotic.pavillion.service.EmailSenderService;
import io.phobotic.pavillion.service.SchedulerService;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // The authority for the sync adapter's content provider
    public static final String AUTHORITY = "com.example.android.datasync.provider";
    // An account type, in the form of a domain name
    public static final String ACCOUNT_TYPE = "example.com";
    // The account name
    public static final String ACCOUNT = "dummyaccount";
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final String FRAGMENT_DIALOG = "dialog";
    private static final String KEY_FAB_X = "fabX";
    private static final String KEY_FAB_Y = "fabY";
    private static final String KEY_FAB_VISIBLE = "fabVisible";
    private static final String KEY_CHECKDIGITS = "checkdigits";
    private final long FAB_TRANSITION_DURATION = 300;
    // Instance fields
    Account mAccount;
    private boolean digitsCardShown = false;
    private boolean errorCardShown = false;
    private FloatingActionButton fab;
    private CardView digitsCard;
    private EditText input;
    private TextView cardLocation;
    private TextView mainCheckdigit;
    private TextView leftCheckdigit;
    private TextView middleCheckdigit;
    private TextView rightCheckdigit;
    private boolean fabVisible;
    private boolean enableCamera = false;
    private CardView errorCard;
    private TextView errorCardMessage;
    private float fabY;
    private float fabX;
    private CheckDigits checkDigits;

    public static Account createSyncAccount(Context context) {
        // Create the account type and default account
        Account newAccount = new Account(
                ACCOUNT, ACCOUNT_TYPE);
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(
                        ACCOUNT_SERVICE);
        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call context.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
        } else {
            /*
             * The account exists or some other error occurred. Log this, report it,
             * or handle it internally.
             */
        }
        return newAccount;
    }

    private void init() {
        initFab();
        initCards();
        initInput();
        initCamera();
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
                    Log.e(TAG, "keyboard action not recognized, will not act");
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
                if (digitsCardShown) {
                    hideDigitsCard();
                } else if (errorCardShown) {
                    hideErrorCard();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // nothing to do
            }
        });
    }

    private void initCards() {
        initDigitsCard();
        initErrorCard();
    }

    private void initDigitsCard() {
        digitsCard = (CardView) findViewById(R.id.card_digits);
        cardLocation = (TextView) findViewById(R.id.cd_location);
        mainCheckdigit = (TextView) findViewById(R.id.cd_main);
        leftCheckdigit = (TextView) findViewById(R.id.cd_left);
        middleCheckdigit = (TextView) findViewById(R.id.cd_middle);
        rightCheckdigit = (TextView) findViewById(R.id.cd_right);
    }

    private void initErrorCard() {
        errorCard = (CardView) findViewById(R.id.error_card);
        errorCardMessage = (TextView) findViewById(R.id.error_card_text);
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
        fabX = fab.getX();
        fabY = fab.getY();
        fabVisible = false;
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

        Log.d(TAG, "starting scheduler service");
        Intent schedulerIntent = new Intent(this, SchedulerService.class);
        startService(schedulerIntent);

        init();

    }

    @Override
    protected void onStop() {
        super.onStop();
        fab.setVisibility(View.INVISIBLE);
        fabVisible = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putFloat(KEY_FAB_X, fabX);
        outState.putFloat(KEY_FAB_Y, fabY);
        outState.putBoolean(KEY_FAB_VISIBLE, fabVisible);
        outState.putSerializable(KEY_CHECKDIGITS, checkDigits);
    }

    private void initCamera() {
        requestCameraPermissions();
    }

    private void calculateCheckdigits() {
        Log.d(TAG, "performing checkdigt lookup");
        hideSoftKeyboard();
        String location = input.getText().toString();
        checkDigits = null;

        try {
            checkDigits = CheckDigitFactory.fromString(location);
            SearchInstance instance = new SearchInstance(System.currentTimeMillis(),
                    location, checkDigits.getMainCheckdigit(), checkDigits.getLeftCheckdigit(),
                    checkDigits.getMiddleCheckdigit(), checkDigits.getRightCheckdigit(), null);
            long requestId = recordRequest(instance);
            capturePicture(requestId);
            hideErrorCard();
            showDigitsCard();
        } catch (ParseException e) {
            mainCheckdigit.setText("");
            leftCheckdigit.setText("");
            middleCheckdigit.setText("");
            rightCheckdigit.setText("");
            if (digitsCardShown) {
                hideDigitsCard();
            }
            showErrorCard(e.getMessage());
        }
    }

    private void hideSoftKeyboard() {
        input.clearFocus();
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void hideErrorCard() {
        if (errorCardShown) {
            final float cx = errorCard.getX();
            float offscreenX = cx + errorCard.getWidth() + 100;

            ObjectAnimator animX = ObjectAnimator.ofFloat(errorCard, "x", offscreenX);
            animX.setInterpolator(new OvershootInterpolator());
            animX.setDuration(300);
            animX.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    fab.setClickable(false);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    errorCard.setVisibility(View.INVISIBLE);
                    errorCardShown = false;
                    errorCard.setX(cx);
                    fab.setClickable(true);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animX.start();
        }
    }

    private void showErrorCard(String message) {
        if (!errorCardShown) {
            errorCardMessage.setText(message);
            float cx = errorCard.getX();
            float offscreenX = cx - errorCard.getWidth() - 100;
            errorCard.setX(offscreenX);

            ObjectAnimator animX = ObjectAnimator.ofFloat(errorCard, "x", cx);
            animX.setInterpolator(new OvershootInterpolator());
            animX.setDuration(300);
            animX.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    fab.setClickable(false);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    errorCardShown = true;
                    fab.setClickable(true);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animX.start();

            errorCard.setVisibility(View.VISIBLE);

        }
    }

    private File capturePicture(long captureId) {
        PictureHandler ph = new PictureHandler();
        ph.setPhotoTakenListener(new PhotoTakenListener() {
            @Override
            public void onPhotoTaken(File pictureFile, long searchId) {
                SearchesDatabase.getInstance(getApplicationContext()).updateSearchFileName(searchId, pictureFile);
            }
        });
        File picFile = null;
        try {
            ph.takePicture(this, captureId);
        } catch (Exception e) {
            //todo
        }
        return picFile;
    }

    private long recordRequest(SearchInstance instance) {
        SearchesDatabase db = SearchesDatabase.getInstance(this);
        return db.insertSearch(instance);
    }

    private void showDigitsCard() {
        assert (checkDigits != null) : "expected checkdigits to be non-null";

        cardLocation.setText(checkDigits.getLocation());
        mainCheckdigit.setText(checkDigits.getMainCheckdigit());
        leftCheckdigit.setText(checkDigits.getLeftCheckdigit());
        middleCheckdigit.setText(checkDigits.getMiddleCheckdigit());
        rightCheckdigit.setText(checkDigits.getRightCheckdigit());
        input.setTextColor(getResources().getColor(R.color.location_verified));
        transitionFab();
    }

    private void sendEmail() {
        EmailSenderService sender = new EmailSenderService(this);
        sender.sendTestEmail(this);
    }

    private void transitionFab() {
        fab.setClickable(false);
        if (!fabVisible) {
            showFab();
        }

        Log.d(TAG, "original fab coordinates. x:" + fabX + " y:" + fabY);
        float offsetX = digitsCard.getWidth() / 2;
        float offsetY = digitsCard.getHeight() / 2;
        float offsetFabX = fab.getWidth() / 2;
        float offsetFabY = fab.getHeight() / 2;
        float startX = digitsCard.getX();
        float startY = digitsCard.getY();
        float cx = startX + offsetX - offsetFabX;
        float cy = startY + offsetY - offsetFabY;
        Log.d(TAG, "transitioning fab coordinates to x:" + cx + " y:" + cy);

        ObjectAnimator moveX = ObjectAnimator.ofFloat(fab, "x", cx);
        ObjectAnimator moveY = ObjectAnimator.ofFloat(fab, "y", cy);
        moveX.setInterpolator(new DecelerateInterpolator());
        moveY.setInterpolator(new AccelerateInterpolator());
        moveY.setDuration(FAB_TRANSITION_DURATION);
        moveX.setDuration(FAB_TRANSITION_DURATION);


        int colorFrom = getResources().getColor(R.color.colorPrimary);
        int colorTo = getResources().getColor(R.color.card_digits_background);
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(FAB_TRANSITION_DURATION); // milliseconds
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                fab.setBackgroundTintList(ColorStateList.valueOf((int) animator.getAnimatedValue()));
            }

        });

        AnimatorSet as = new AnimatorSet();
        as.playTogether(moveX, moveY, colorAnimation);
        as.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                circularReveal();
                fab.setVisibility(View.INVISIBLE);
                fabVisible = false;
                Log.d(TAG, "Resetting fab coordinates to x:" + fabX + " y:" + fabY);
                fab.setX(fabX);
                fab.setY(fabY);
                fab.setBackgroundTintList(ColorStateList.valueOf(
                        getResources().getColor(R.color.colorPrimary)));
                fab.setClickable(false);
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
        digitsCardShown = true;
    }

    private void hideDigitsCard() {
        //declare the card hidden early to prevent multiple keypresses
        // from triggering duplicate animations
        digitsCardShown = false;
        fab.setClickable(false);

        if (Build.VERSION.SDK_INT >= 21) {
            final float cardY = digitsCard.getY();
            float endY = cardY + 1000;  //todo get screen size
            ObjectAnimator anim = ObjectAnimator.ofFloat(digitsCard, "y", endY);
            anim.setDuration(300);
            anim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    digitsCard.setVisibility(View.INVISIBLE);
                    digitsCard.setY(cardY);
                    showFab();
                    fab.setClickable(true);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            anim.start();
        } else {
            digitsCard.setVisibility(View.INVISIBLE);
            digitsCardShown = false;
            showFab();
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
    protected void onPause() {
        super.onPause();

    }

    public void onResume() {
        super.onResume();
        final EditText input = (EditText) findViewById(R.id.location_input);
        input.requestFocus();
        showFab();
    }

    private void showFab() {
        if (!fabVisible) {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(fab, "scaleX", 0, 1);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(fab, "scaleY", 0, 1);
            AnimatorSet scaleXY = new AnimatorSet();
            scaleXY.playTogether(scaleX, scaleY);
            scaleXY.setInterpolator(new OvershootInterpolator());
            scaleXY.setDuration(FAB_TRANSITION_DURATION);
            fab.setVisibility(View.VISIBLE);
            scaleXY.start();
            fabVisible = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ErrorDialog.newInstance(getString(R.string.request_permission))
                        .show(getSupportFragmentManager(), FRAGMENT_DIALOG);
            }
        } else {
            enableCameraSupport();
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void enableCameraSupport() {
        this.enableCamera = true;
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

    private void requestCameraPermissions() {
        int permissions = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (permissions != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                new ConfirmationDialog().show(getSupportFragmentManager(), FRAGMENT_DIALOG);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            }
        } else {
            enableCameraSupport();
        }
        ;
    }

    /**
     * Shows an error message dialog.
     */
    public static class ErrorDialog extends DialogFragment {

        private static final String ARG_MESSAGE = "message";

        public static ErrorDialog newInstance(String message) {
            ErrorDialog dialog = new ErrorDialog();
            Bundle args = new Bundle();
            args.putString(ARG_MESSAGE, message);
            dialog.setArguments(args);
            return dialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity activity = getActivity();
            return new AlertDialog.Builder(activity)
                    .setMessage(getArguments().getString(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            activity.finish();
                        }
                    })
                    .create();
        }

    }

    /**
     * Shows OK/Cancel confirmation dialog about camera permission.
     */
    public static class ConfirmationDialog extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity parent = getActivity();
            return new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.request_permission)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(parent,
                                    new String[]{Manifest.permission.CAMERA},
                                    REQUEST_CAMERA_PERMISSION);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (parent != null) {
                                        parent.finish();
                                    }
                                }
                            })
                    .create();
        }
    }


}
