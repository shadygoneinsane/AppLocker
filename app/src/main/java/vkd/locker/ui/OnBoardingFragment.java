package vkd.locker.ui;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import vkd.applocker.R;
import vkd.locker.lock.AppLockService;
import vkd.locker.lock.LockService;
import vkd.locker.util.PrefUtils;
import vkd.locker.util.Util;
import vkd.util.DialogSequencer;

import static vkd.locker.ui.MainActivity.EXTRA_UNLOCKED;

public class OnBoardingFragment extends Fragment implements View.OnClickListener {
    private Button mSettingsButton, mEnablePassPin;
    private DialogSequencer mSequencer;
    private CheckBox permission_checkbox, password_checkbox;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View mParentView = inflater.inflate(R.layout.onboarding_layout, container, false);
        mSettingsButton = mParentView.findViewById(R.id.go_to_settings);
        mSettingsButton.setOnClickListener(this);

        mEnablePassPin = mParentView.findViewById(R.id.enable_pin_password);
        mEnablePassPin.setOnClickListener(this);
        permission_checkbox = mParentView.findViewById(R.id.permission_checkbox);
        password_checkbox = mParentView.findViewById(R.id.password_checkbox);

        mSequencer = new DialogSequencer();
        setHasOptionsMenu(true);
        getActivity().setTitle(R.string.app_name);
        return mParentView;
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT > 18 && Util.checkUsageStatsPermission(getActivity())) {
            permission_checkbox.setChecked(true);
        }
        if (!Util.checkIfNotPinPassNotSet(getActivity())) {
            password_checkbox.setChecked(true);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.go_to_settings:
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                startActivity(intent);
                break;

            case R.id.enable_pin_password:
                Dialogs.getChangePasswordDialog(getActivity()).show();
                break;
        }
    }

    private void toggleService() {
        boolean newState = false;
        if (AppLockService.isRunning(getActivity())) {
            Log.d("", "toggleService() Service is running, now stopping");
            AppLockService.stop(getActivity());
        } else if (Dialogs.addEmptyPasswordDialog(getActivity(), mSequencer)) {
            mSequencer.start();
        } else {
            newState = AppLockService.toggle(getActivity());
        }
    }

    /**
     * @return True if the service is allowed to start
     */
    private boolean showDialogs() {
        boolean deny = false;

        // Recovery code
        mSequencer.addDialog(Dialogs.getRecoveryCodeDialog(getActivity()));

        // Empty password
        deny = Dialogs.addEmptyPasswordDialog(getActivity(), mSequencer);

        mSequencer.start();
        return !deny;
    }

    /**
     * Checks if password is set or not
     * Also pops up window for comparison
     */
    private void showLockerIfNotUnlocked(boolean relock) {
        boolean unlocked = getActivity().getIntent().getBooleanExtra(EXTRA_UNLOCKED, false);
        if (new PrefUtils(getActivity()).isCurrentPasswordEmpty()) {
            unlocked = true;
        }
        if (!unlocked) {
            LockService.showCompare(getActivity(), getActivity().getPackageName());
        }
        getActivity().getIntent().putExtra(EXTRA_UNLOCKED, !relock);
    }

}
