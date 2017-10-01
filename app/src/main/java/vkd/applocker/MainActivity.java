package vkd.applocker;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import vkd.locker.appselect.AppAdapter;
import vkd.locker.appselect.AppListElement;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,
        AppAdapter.OnEventListener {
    private AppAdapter mAdapter;
    private Toast mLockedToast;
    private Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activitymain);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        ListView mListView = (ListView) findViewById(R.id.lvAppList);
        mAdapter = new AppAdapter(MainActivity.this);
        mAdapter.setOnEventListener(this);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        AppListElement item = (AppListElement) mAdapter.getItem(position);
        if (item.isApp()) {
            mAdapter.toggle(item);
            showToastSingle(item.locked, item.title);
            // Update lock image
            view.findViewById(R.id.applist_item_image).setVisibility(
                    item.locked ? View.VISIBLE : View.GONE);

            // And the menu
            updateMenuLayout();
        }
    }

    private void showToast(String text) {
        if (mLockedToast != null) {
            mLockedToast.cancel();
        }

        mLockedToast = Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT);
        mLockedToast.show();
    }

    private void showToastSingle(boolean locked, String title) {
        showToast(getString(locked ? R.string.apps_toast_locked_single
                : R.string.apps_toast_unlocked_single, title));
    }

    private void showToastAll(boolean locked) {
        showToast(getString(locked ? R.string.apps_toast_locked_all
                : R.string.apps_toast_unlocked_all));
    }

    public void onSearch(String query) {
        Log.d("AppsFragment", "onSearch (query=" + query + ")");
    }

    private void updateMenuLayout() {
        boolean all = mAdapter.areAllAppsLocked();
        if (mMenu != null && mAdapter.isLoadComplete()) {
            mMenu.findItem(R.id.apps_menu_lock_all).setVisible(!all);
            mMenu.findItem(R.id.apps_menu_unlock_all).setVisible(all);
        }
    }

 /*   @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.apps_menu_sort:
                mAdapter.sort();
                break;
            case R.id.apps_menu_lock_all:
                onLockAllOptions(true);
                break;
            case R.id.apps_menu_unlock_all:
                onLockAllOptions(false);
                break;
            case R.id.apps_menu_search:
                break;
        }
        return super.onOptionsItemSelected(item);
    }*/

    private void onLockAllOptions(boolean lockall) {
        mMenu.findItem(R.id.apps_menu_lock_all).setVisible(!lockall);
        mMenu.findItem(R.id.apps_menu_unlock_all).setVisible(lockall);
        mAdapter.prepareUndo();
        mAdapter.setAllLocked(lockall);
        showToastAll(lockall);
    }

    @Override
    public void onLoadComplete() {
        updateMenuLayout();
    }

    @Override
    public void onDirtyStateChanged(boolean dirty) {
        mMenu.findItem(R.id.apps_menu_sort).setVisible(dirty);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
}
