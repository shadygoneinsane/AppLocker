package vkd.locker.ui;

import android.app.Activity;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;

import vkd.applocker.R;
import vkd.locker.lock.AppLockService;
import vkd.locker.lock.LockService;
import vkd.locker.util.PrefUtils;
import vkd.locker.util.Util;
import vkd.util.DialogSequencer;

public class MainActivity extends AppCompatActivity {

    static final String EXTRA_UNLOCKED = "vkd.locker.unlocked";

    private DialogSequencer mSequencer;
    private Fragment mCurrentFragment;

    /**
     * Used to store the last screen title.
     */
    private CharSequence mTitle;

    private ActionBar mActionBar;
    private BroadcastReceiver mReceiver;
    private IntentFilter mFilter;
    private Switch switchAB;

    private class ServiceStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("MainACtivity", "Received broadcast (action=" + intent.getAction());
            updateLayout();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handleIntent();

        mReceiver = new ServiceStateReceiver();
        mFilter = new IntentFilter();
        mFilter.addCategory(AppLockService.CATEGORY_STATE_EVENTS);
        mFilter.addAction(AppLockService.BROADCAST_SERVICE_STARTED);
        mFilter.addAction(AppLockService.BROADCAST_SERVICE_STOPPED);

        mTitle = getTitle();

        mActionBar = getSupportActionBar();
        //mCurrentFragment = new AppsFragment();
        mCurrentFragment = new OnBoardingFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.container, mCurrentFragment)
                .commit();

        mSequencer = new DialogSequencer();
        //showDialogs();
        showLockerIfNotUnlocked(false);

        if (Build.VERSION.SDK_INT > 22 && !Settings.canDrawOverlays(this)) {
            Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            myIntent.setData(Uri.parse("package: " + getPackageName()));
            startActivityForResult(myIntent, 101);
        }
    }

    public void nextFragment() {
        if (mCurrentFragment != null)
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(mCurrentFragment)
                    .commit();

        mCurrentFragment = new AppsFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, mCurrentFragment)
                .addToBackStack(null)
                .commit();
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Main", "onResume");
        showLockerIfNotUnlocked(true);
        registerReceiver(mReceiver, mFilter);
        updateLayout();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LockService.hide(this);
        unregisterReceiver(mReceiver);
        mSequencer.stop();
    }

    @Override
    protected void onDestroy() {
        Log.v("Main", "onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d("", "onNewIntent");
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent();
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        mTitle = title;
        getSupportActionBar().setTitle(title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.global, menu);
        MenuItem item = menu.findItem(R.id.switchId);
        MenuItemCompat.setActionView(item, R.layout.switch_layout);
        RelativeLayout switchParent = (RelativeLayout) MenuItemCompat.getActionView(item);
        switchAB = switchParent.findViewById(R.id.switchAB);
        if (switchAB != null)
            switchAB.setOnCheckedChangeListener(mainServiceToggler);
        return true;
    }

    private CompoundButton.OnCheckedChangeListener mainServiceToggler = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                Toast.makeText(getApplication(), "ON", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplication(), "OFF", Toast.LENGTH_SHORT).show();
            }
            toggleService();
        }
    };

    /**
     * Provide a way back to {@link MainActivity} without having to provide a
     * password again. It finishes the calling {@link Activity}
     *
     * @param context
     */
    public static void showWithoutPassword(Context context) {
        Intent i = new Intent(context, MainActivity.class);
        i.putExtra(EXTRA_UNLOCKED, true);
        if (!(context instanceof Activity)) {
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(i);
    }

    public void setActionBarTitle(int resId) {
        mActionBar.setTitle(resId);
    }

    private void showLockerIfNotUnlocked(boolean relock) {
        boolean unlocked = getIntent().getBooleanExtra(EXTRA_UNLOCKED, false);
        if (new PrefUtils(this).isCurrentPasswordEmpty()) {
            unlocked = true;
        }
        if (!unlocked) {
            LockService.showCompare(this, getPackageName());
        }
        getIntent().putExtra(EXTRA_UNLOCKED, !relock);
    }

    private void updateLayout() {
        Log.d("Main", "UPDATE LAYOUT Setting service state: " + AppLockService.isRunning(this));
        if (Build.VERSION.SDK_INT > 18 && Util.checkUsageStatsPermission(this) && !Util.checkIfNotPinPassNotSet(this)) {
            nextFragment();
        }
        if (AppLockService.isRunning(this) && switchAB != null) {
            switchAB.setOnCheckedChangeListener(null);
            switchAB.setChecked(true);
            switchAB.setOnCheckedChangeListener(mainServiceToggler);

            nextFragment();
        } else {
            Log.d("updateLayout() Main", "Something went wrong");
        }
    }

    /**
     * Handle this Intent for searching...
     */
    private void handleIntent() {
        if (getIntent() != null && getIntent().getAction() != null) {
            if (getIntent().getAction().equals(Intent.ACTION_SEARCH)) {
                Log.d("MainActivity", "Action search!");
                final String query = getIntent().getStringExtra(SearchManager.QUERY);
                if (query != null) {
                    ((AppsFragment) mCurrentFragment).onSearch(query);
                }
            }
        }
    }

    /*
    * Use toggle whenever we want to stop the started lock service
    * or start the stopped locked service
    * Pre requisites - any lock pattern or pin be saved already
    * */
    private void toggleService() {
        boolean newState = false;
        if (AppLockService.isRunning(this)) {
            Log.d("", "toggleService() Service is running, now stopping");
            AppLockService.stop(this);
        } else if (Dialogs.addEmptyPasswordDialog(this, mSequencer)) {
            mSequencer.start();
        } else {
            newState = AppLockService.toggle(this);
        }
        if (newState) {
            switchAB.setOnCheckedChangeListener(null);
            switchAB.setChecked(true);
            switchAB.setOnCheckedChangeListener(mainServiceToggler);
        }

    }

    public void onShareButton() {
        // Don't add never button, the user wanted to share
        Dialogs.getShareEditDialog(this, false).show();
    }

    public void onRateButton() {
        toGooglePlay();
    }

    private void toGooglePlay() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=" + getPackageName()));
        if (getPackageManager().queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY).size() >= 1) {
            startActivity(intent);
        }
    }
}
