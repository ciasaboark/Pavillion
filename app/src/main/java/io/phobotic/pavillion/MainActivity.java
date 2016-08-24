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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
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
import io.phobotic.pavillion.email.EmailSender;
import io.phobotic.pavillion.listener.PhotoTakenListener;
import io.phobotic.pavillion.photo.PictureHandler;
import io.phobotic.pavillion.prefs.Preferences;
import io.phobotic.pavillion.schedule.EmailScheduler;
import io.phobotic.pavillion.view.LocationCard;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final String FRAGMENT_DIALOG = "dialog";
    private static final String KEY_CHECKDIGITS = "checkdigits";
    private static final String KEY_ERROR_CARD_SHOWN = "error card shown";
    private final long FAB_TRANSITION_DURATION = 300;
    // Instance fields
    Account mAccount;
    private boolean locationCardVisible = false;
    private boolean errorCardShown = false;
    private FloatingActionButton fab;
    private LocationCard locationCard;
    private EditText input;
    private boolean fabVisible;
    private boolean enableCamera = false;
    private CardView errorCard;
    private TextView errorCardMessage;
    private float fabY;
    private float fabX;
    private CheckDigits checkDigits;
    private BroadcastReceiver emailListener;
    private Snackbar snackbar;

    private static final String KEY_CHECKDIGIT = "key checkdigits";

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
                if (locationCardVisible) {
                    hideDigitsCard();
                }

                if (errorCardShown) {
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
        locationCard = (LocationCard) findViewById(R.id.location_card);
        locationCard.clear();
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

        emailListener = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_main);
                if (snackbar != null && snackbar.isShown()) {
                    snackbar.dismiss();
                }
                switch (intent.getAction()) {
                    case EmailSender.EMAIL_SEND_SUCCESS:
                        snackbar = Snackbar.make(coordinatorLayout, "Email sent", Snackbar.LENGTH_LONG);
                        snackbar.show();
                        break;
                    case EmailSender.EMAIL_SEND_FAILED:
                        snackbar = Snackbar.make(coordinatorLayout, "Sending email failed", Snackbar.LENGTH_LONG);
                        snackbar.show();
                        break;
                    case EmailSender.EMAIL_SEND_START:
                        snackbar = Snackbar.make(coordinatorLayout, "Sending email...", Snackbar.LENGTH_LONG);
                        snackbar.show();
                        break;
                }
            }
        };

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

    private void initCamera() {
        requestCameraPermissions();
    }

    private void calculateCheckdigits() {
        Log.d(TAG, "performing checkdigt lookup");
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
            locationCard.clear();
            if (locationCardVisible) {
                hideDigitsCard();
            }
            showErrorCard("Unable to generate checkdigits for " + location);
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
        errorCardShown = false;
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
        locationCard.setLocation(checkDigits);
        input.setTextColor(getResources().getColor(R.color.location_verified));
        input.setText("");
        transitionFab();
    }

    private void transitionFab() {
        fab.setClickable(false);
        if (!fabVisible) {
            showFab();
        }

        Log.d(TAG, "original fab coordinates. x:" + fabX + " y:" + fabY);
        float offsetX = locationCard.getWidth() / 2;
        float offsetY = locationCard.getHeight() / 2;
        float offsetFabX = fab.getWidth() / 2;
        float offsetFabY = fab.getHeight() / 2;
        float startX = locationCard.getX();
        float startY = locationCard.getY();
        float cx = startX + offsetX - offsetFabX;
        float cy = startY + offsetY - offsetFabY;
        Log.d(TAG, "transitioning fab coordinates to x:" + cx + " y:" + cy);
        fabX = fab.getX();
        fabY = fab.getY();
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

    private void hideDigitsCard() {
        //declare the card hidden early to prevent multiple keypresses
        // from triggering duplicate animations
        locationCardVisible = false;
        fab.setClickable(false);

        if (Build.VERSION.SDK_INT >= 21) {
            final float cardY = locationCard.getY();
            float endY = cardY + 1000;
            ObjectAnimator anim = ObjectAnimator.ofFloat(locationCard, "y", endY);
            anim.setDuration(300);
            anim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    locationCard.setVisibility(View.INVISIBLE);
                    locationCard.setY(cardY);
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
            locationCard.setVisibility(View.INVISIBLE);
            locationCardVisible = false;
            showFab();
        }
    }

    private void circularReveal() {
        int cx = locationCard.getWidth() / 2;
        int cy = locationCard.getHeight() / 2;
        float radius = (float) Math.hypot(cx, cy);
        if (Build.VERSION.SDK_INT >= 21) {
            Animator anim = ViewAnimationUtils.createCircularReveal(locationCard, cx, cy, 0, radius);
            anim.start();
        }

        locationCard.setVisibility(View.VISIBLE);
        locationCardVisible = true;
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(KEY_CHECKDIGITS, checkDigits);
        outState.putBoolean(KEY_ERROR_CARD_SHOWN, errorCardShown);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        checkDigits = (CheckDigits) savedInstanceState.getSerializable(KEY_CHECKDIGITS);
        boolean showErrorCard = savedInstanceState.getBoolean(KEY_ERROR_CARD_SHOWN, false);
        if (showErrorCard) {
            showErrorCard("foobar");
        } else if (checkDigits != null) {
            showDigitsCard();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(emailListener);
    }

    public void onResume() {
        super.onResume();
        EmailScheduler scheduler = new EmailScheduler(this);
        scheduler.reschedule();

        final EditText input = (EditText) findViewById(R.id.location_input);
        input.requestFocus();
        showFab();
        IntentFilter intentFilter = new IntentFilter(EmailSender.EMAIL_SEND_SUCCESS);
        intentFilter.addAction(EmailSender.EMAIL_SEND_FAILED);
        intentFilter.addAction(EmailSender.EMAIL_SEND_START);

        LocalBroadcastManager.getInstance(this).registerReceiver(emailListener, intentFilter);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ErrorDialog.newInstance(getString(R.string.request_permission))
                        .show(getSupportFragmentManager(), FRAGMENT_DIALOG);
            } else {
                enableCameraSupport();
            }
        }
    }

    private void enableCameraSupport() {
        this.enableCamera = true;
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
            fabX = fab.getX();
            fabY = fab.getY();
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


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_settings) {
            final Context context  = this;
            final EditText input = new EditText(MainActivity.this);
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            input.setImeActionLabel(getString(android.R.string.ok), KeyEvent.KEYCODE_ENTER);
            final AlertDialog passwordDialog = new AlertDialog.Builder(this)
                    .setTitle("Settings")
                    .setMessage("Enter password")
                    .setView(input)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, int which) {
                            String pass = input.getText().toString();
                            Preferences preferences = Preferences.getInstance(context);
                            String curPass = preferences.getSettingsPassword();

                            if (pass.equals(curPass)) {
                                startActivity(new Intent(context, SettingsActivity.class));
                            } else {
                                final AlertDialog errorDialog = new AlertDialog.Builder(context)
                                        .setTitle("Error")
                                        .setMessage("Incorrect password")
                                        .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        }).show();
                            }

                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }).show();
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
