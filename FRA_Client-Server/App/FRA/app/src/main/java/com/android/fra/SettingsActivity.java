package com.android.fra;

import android.animation.ValueAnimator;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.support.v7.widget.SwitchCompat;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.longsh.optionframelibrary.OptionMaterialDialog;

import static com.android.fra.ActivityCollector.finishAll;
import static org.litepal.LitePalApplication.getContext;

public class SettingsActivity extends BaseActivity {

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private DrawerLayout mDrawerLayout;
    private int hour, minute;
    private int startHour, startMinute, endHour, endMinute;
    private static boolean fingerprintReturn;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        if (!pref.getBoolean("isLogin", false)) {
            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            startActivity(intent);
            finishAll();
        } else {
            if (pref.getBoolean("is_set_fingerprint", false) && pref.getBoolean("is_set_settings_fingerprint", false) && !fingerprintReturn) {
                Intent intent = new Intent(SettingsActivity.this, FingerprintActivity.class);
                startActivityForResult(intent, 0);
            }
            setContentView(R.layout.activity_settings);
            Toolbar toolbar = (Toolbar) findViewById(R.id.settings_activity_toolBar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle(this.getString(R.string.function_settings));

            NavigationView navView = (NavigationView) findViewById(R.id.settings_activity_nav_view);
            mDrawerLayout = (DrawerLayout) findViewById(R.id.settings_activity_drawer_layout);
            View headerLayout = navView.inflateHeaderView(R.layout.nav_header);
            ImageView drawerImageView = (ImageView) headerLayout.findViewById(R.id.nav_header_image);
            TextView navTextView = (TextView) headerLayout.findViewById(R.id.nav_account);
            navView.setCheckedItem(R.id.nav_settings);
            navTextView.setText(pref.getString("account", ""));
            Glide.with(this)
                    .load(R.drawable.nav_icon)
                    .apply(RequestOptions.bitmapTransform(new BlurTransformation(10, 5)))
                    .into(drawerImageView);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
            }

            MenuItem menuItem = navView.getMenu().findItem(R.id.nav_management);
            RelativeLayout fingerprintManagementRelativeLayout = (RelativeLayout) findViewById(R.id.fingerprint_management_relativeLayout);
            boolean isEggOn = pref.getBoolean("is_egg_on", false);
            if (isEggOn) {
                menuItem.setVisible(true);
                fingerprintManagementRelativeLayout.setVisibility(View.VISIBLE);
            } else {
                menuItem.setVisible(false);
                fingerprintManagementRelativeLayout.setVisibility(View.GONE);
            }
            navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                        mDrawerLayout.closeDrawers();
                    }
                    switch (menuItem.getItemId()) {
                        case R.id.nav_capture:
                            Intent cameraIntent = new Intent(SettingsActivity.this, CameraActivity.class);
                            cameraIntent.putExtra("capture_mode", 0);
                            startActivity(cameraIntent);
                            break;
                        case R.id.nav_register:
                            Intent registerIntent = new Intent(SettingsActivity.this, RegisterActivity.class);
                            startActivity(registerIntent);
                            break;
                        case R.id.nav_management:
                            Intent managementIntent = new Intent(SettingsActivity.this, ManagementActivity.class);
                            startActivity(managementIntent);
                            break;
                        case R.id.nav_settings:
                            break;
                        default:
                    }
                    return true;
                }
            });

            final LinearLayout timeChoose = (LinearLayout) findViewById(R.id.time_choose);
            final SwitchCompat timeSwitch = (SwitchCompat) findViewById(R.id.time_switch);
            final TextView isTimeOpen = (TextView) findViewById(R.id.is_time_open);

            boolean isSetTime = pref.getBoolean("is_set_time", false);
            if (isSetTime) {
                timeSwitch.setChecked(true);
                timeChoose.setVisibility(View.VISIBLE);
                isTimeOpen.setText(this.getString(R.string.settings_time_isOpen));
            } else {
                timeSwitch.setChecked(false);
                timeChoose.setVisibility(View.GONE);
                isTimeOpen.setText(this.getString(R.string.settings_time_isClose));
            }
            final RelativeLayout switchTimeSetting = (RelativeLayout) findViewById(R.id.switch_time_setting);
            switchTimeSetting.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (timeSwitch.isChecked()) {
                        timeSwitch.setChecked(false);
                    } else {
                        timeSwitch.setChecked(true);
                    }
                }
            });
            timeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        timeChoose.measure(0, 0);
                        final int height = timeChoose.getMeasuredHeight();
                        show(timeChoose, height);
                        isTimeOpen.setText(SettingsActivity.this.getString(R.string.settings_time_isOpen));
                    } else {
                        timeChoose.measure(0, 0);
                        final int height = timeChoose.getMeasuredHeight();
                        dismiss(timeChoose, height);
                        isTimeOpen.setText(SettingsActivity.this.getString(R.string.settings_time_isClose));
                    }
                }
            });

            final Button beginTimeButton = (Button) findViewById(R.id.begin_time);
            startHour = pref.getInt("startHour", 0);
            startMinute = pref.getInt("startMinute", 0);
            showTime(startHour, startMinute, beginTimeButton);

            beginTimeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TimePickerDialog timePickerDialog = new TimePickerDialog(SettingsActivity.this, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hour, int minute) {
                            if (hour < endHour || (hour == endHour && minute < endMinute)) {
                                showTime(hour, minute, beginTimeButton);
                                startHour = hour;
                                startMinute = minute;
                            } else {
                                final OptionMaterialDialog mMaterialDialog = new OptionMaterialDialog(SettingsActivity.this);
                                mMaterialDialog.setTitle(SettingsActivity.this.getString(R.string.settings_error)).setTitleTextColor(R.color.colorAccent).setTitleTextSize((float) 22.5)
                                        .setMessage(SettingsActivity.this.getString(R.string.settings_timeError1)).setMessageTextSize((float) 16.5)
                                        .setPositiveButton(SettingsActivity.this.getString(R.string.operation_ok), new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                showTime(startHour, startMinute, beginTimeButton);
                                                mMaterialDialog.dismiss();

                                            }
                                        })
                                        .setPositiveButtonTextColor(R.color.colorAccent)
                                        .show();
                            }
                        }
                    }, hour, minute, true);
                    timePickerDialog.show();
                }
            });

            final Button endTimeButton = (Button) findViewById(R.id.end_time);
            endHour = pref.getInt("endHour", 0);
            endMinute = pref.getInt("endMinute", 0);
            showTime(endHour, endMinute, endTimeButton);

            endTimeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final TimePickerDialog timePickerDialog = new TimePickerDialog(SettingsActivity.this, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hour, int minute) {
                            if (hour > startHour || (hour == startHour && minute > startMinute)) {
                                showTime(hour, minute, endTimeButton);
                                endHour = hour;
                                endMinute = minute;
                            } else {
                                final OptionMaterialDialog mMaterialDialog = new OptionMaterialDialog(SettingsActivity.this);
                                mMaterialDialog.setTitle(SettingsActivity.this.getString(R.string.settings_error)).setTitleTextColor(R.color.colorAccent).setTitleTextSize((float) 22.5)
                                        .setMessage(SettingsActivity.this.getString(R.string.settings_timeError2)).setMessageTextSize((float) 16.5)
                                        .setPositiveButton(SettingsActivity.this.getString(R.string.operation_ok), new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                showTime(endHour, endMinute, endTimeButton);
                                                mMaterialDialog.dismiss();

                                            }
                                        })
                                        .setPositiveButtonTextColor(R.color.colorAccent)
                                        .show();
                            }
                        }
                    }, hour, minute, true);
                    timePickerDialog.show();
                }
            });

            RelativeLayout changeLoginState = (RelativeLayout) findViewById(R.id.change_login_state);
            changeLoginState.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final OptionMaterialDialog confirmDialog = new OptionMaterialDialog(SettingsActivity.this);
                    confirmDialog.setMessage(SettingsActivity.this.getString(R.string.settings_exit_account)).setMessageTextSize((float) 16.5)
                            .setPositiveButton(SettingsActivity.this.getString(R.string.operation_ok), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    confirmDialog.dismiss();
                                    Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    editor = pref.edit();
                                    editor.putBoolean("isLogin", false);
                                    editor.apply();
                                }
                            })
                            .setPositiveButtonTextColor(R.color.noFaceOwner)
                            .setNegativeButton(SettingsActivity.this.getString(R.string.operation_cancel), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    confirmDialog.dismiss();
                                }
                            })
                            .setNegativeButtonTextColor(R.color.noFaceOwner)
                            .show();
                }
            });

            TextView versionCode = (TextView) findViewById(R.id.version_code);
            String code = "";
            PackageManager manager = getContext().getPackageManager();
            try {
                PackageInfo info = manager.getPackageInfo(getContext().getPackageName(), 0);
                code = info.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            versionCode.setText(code);

            ImageButton saveSettings = (ImageButton) findViewById(R.id.save_settings);
            saveSettings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveSettings();
                }
            });

            RelativeLayout aboutApp = (RelativeLayout) findViewById(R.id.about_app);
            aboutApp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(SettingsActivity.this, AboutActivity.class);
                    startActivity(intent);
                }
            });
        }

        if (Build.VERSION.SDK_INT >= 23) {
            FingerprintManager fingerprintManager = getSystemService(FingerprintManager.class);
            RelativeLayout fingerprintRelativeLayout = (RelativeLayout) findViewById(R.id.fingerprint_relativeLayout);
            final SwitchCompat fingerprintSwitch = (SwitchCompat) findViewById(R.id.fingerprint_switch);
            if (fingerprintManager.isHardwareDetected()) {
                fingerprintRelativeLayout.setVisibility(View.VISIBLE);
                View blankView = (View) findViewById(R.id.blank_view);
                blankView.setVisibility(View.VISIBLE);
            }
            fingerprintRelativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (fingerprintSwitch.isChecked()) {
                        fingerprintSwitch.setChecked(false);
                    } else {
                        fingerprintSwitch.setChecked(true);
                    }
                }
            });
            final LinearLayout fingerprintChoose = (LinearLayout) findViewById(R.id.fingerprint_choose);
            final SwitchCompat fingerprintRegisterSwitch = (SwitchCompat) findViewById(R.id.fingerprint_register_switch);
            final SwitchCompat fingerprintManagementSwitch = (SwitchCompat) findViewById(R.id.fingerprint_management_switch);
            final SwitchCompat fingerprintSettingsSwitch = (SwitchCompat) findViewById(R.id.fingerprint_settings_switch);
            boolean isSetFingerprint = pref.getBoolean("is_set_fingerprint", false);
            boolean isSetRegisterFingerprint = pref.getBoolean("is_set_register_fingerprint", false);
            boolean isSetManagementFingerprint = pref.getBoolean("is_set_management_fingerprint", false);
            boolean isSetSettingsFingerprint = pref.getBoolean("is_set_settings_fingerprint", false);
            if (isSetFingerprint) {
                fingerprintSwitch.setChecked(true);
                fingerprintChoose.setVisibility(View.VISIBLE);
            } else {
                fingerprintSwitch.setChecked(false);
                fingerprintChoose.setVisibility(View.GONE);
            }
            fingerprintSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        fingerprintChoose.measure(0, 0);
                        final int height = fingerprintChoose.getMeasuredHeight();
                        show(fingerprintChoose, height);
                    } else {
                        fingerprintChoose.measure(0, 0);
                        final int height = fingerprintChoose.getMeasuredHeight();
                        dismiss(fingerprintChoose, height);
                    }
                }
            });
            if (isSetRegisterFingerprint) {
                fingerprintRegisterSwitch.setChecked(true);
            } else {
                fingerprintRegisterSwitch.setChecked(false);
            }
            RelativeLayout fingerprintRegisterRelativeLayout = (RelativeLayout) findViewById(R.id.fingerprint_register_relativeLayout);
            fingerprintRegisterRelativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (fingerprintRegisterSwitch.isChecked()) {
                        fingerprintRegisterSwitch.setChecked(false);
                    } else {
                        fingerprintRegisterSwitch.setChecked(true);
                    }
                }
            });
            fingerprintRegisterSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (!isChecked && !fingerprintManagementSwitch.isChecked() && !fingerprintSettingsSwitch.isChecked()) {
                        fingerprintSwitch.setChecked(false);
                        fingerprintChoose.measure(0, 0);
                        final int height = fingerprintChoose.getMeasuredHeight();
                        dismiss(fingerprintChoose, height);
                    }
                }
            });
            if (isSetManagementFingerprint) {
                fingerprintManagementSwitch.setChecked(true);
            } else {
                fingerprintManagementSwitch.setChecked(false);
            }
            RelativeLayout fingerprintManagementRelativeLayout = (RelativeLayout) findViewById(R.id.fingerprint_management_relativeLayout);
            fingerprintManagementRelativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (fingerprintManagementSwitch.isChecked()) {
                        fingerprintManagementSwitch.setChecked(false);
                    } else {
                        fingerprintManagementSwitch.setChecked(true);
                    }
                }
            });
            fingerprintManagementSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (!isChecked && !fingerprintRegisterSwitch.isChecked() && !fingerprintSettingsSwitch.isChecked()) {
                        fingerprintSwitch.setChecked(false);
                        fingerprintChoose.measure(0, 0);
                        final int height = fingerprintChoose.getMeasuredHeight();
                        dismiss(fingerprintChoose, height);
                    }
                }
            });
            if (isSetSettingsFingerprint) {
                fingerprintSettingsSwitch.setChecked(true);
            } else {
                fingerprintSettingsSwitch.setChecked(false);
            }
            RelativeLayout fingerprintSettingsRelativeLayout = (RelativeLayout) findViewById(R.id.fingerprint_settings_relativeLayout);
            fingerprintSettingsRelativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (fingerprintSettingsSwitch.isChecked()) {
                        fingerprintSettingsSwitch.setChecked(false);
                    } else {
                        fingerprintSettingsSwitch.setChecked(true);
                    }
                }
            });
            fingerprintSettingsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (!isChecked && !fingerprintRegisterSwitch.isChecked() && !fingerprintManagementSwitch.isChecked()) {
                        fingerprintSwitch.setChecked(false);
                        fingerprintChoose.measure(0, 0);
                        final int height = fingerprintChoose.getMeasuredHeight();
                        dismiss(fingerprintChoose, height);
                    }
                }
            });
        }

        if (pref.getInt("language", 0) == 0) {
            TextView textView = (TextView) findViewById(R.id.current_language);
            textView.setText(this.getString(R.string.settings_followSystemLanguage));
        }
        RelativeLayout switchLanguage = (RelativeLayout) findViewById(R.id.switch_language);
        switchLanguage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                builder.setSingleChoiceItems(new String[]{SettingsActivity.this.getString(R.string.settings_followSystemLanguage), "简体中文", "繁體中文", "English"},
                        pref.getInt("language", 0),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (pref.getInt("language", 0) != i) {
                                    editor = pref.edit();
                                    editor.putInt("language", i);
                                    editor.apply();
                                    dialog.dismiss();
                                    Intent intent = new Intent(SettingsActivity.this, CameraActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                }
                            }
                        });
                dialog = builder.create();
                dialog.show();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            default:
        }
        return true;
    }

    private void show(final View v, int height) {
        v.setVisibility(View.VISIBLE);
        ValueAnimator animator = ValueAnimator.ofInt(0, height);
        animator.setDuration(500);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (Integer) animation.getAnimatedValue();
                v.getLayoutParams().height = value;
                v.setLayoutParams(v.getLayoutParams());
            }
        });
        animator.start();
    }

    public void dismiss(final View v, int height) {
        ValueAnimator animator = ValueAnimator.ofInt(height, 0);
        animator.setDuration(500);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (Integer) animation.getAnimatedValue();
                if (value == 0) {
                    v.setVisibility(View.GONE);
                }
                v.getLayoutParams().height = value;
                v.setLayoutParams(v.getLayoutParams());
            }
        });
        animator.start();
    }

    private void showTime(int hour, int minute, Button button) {
        String showHour, showMinute;
        if (hour < 10) {
            showHour = "0" + hour;
        } else {
            showHour = String.valueOf(hour);
        }
        if (minute < 10) {
            showMinute = "0" + minute;
        } else {
            showMinute = String.valueOf(minute);
        }
        button.setText(showHour + ":" + showMinute);
    }

    private void saveSettings() {
        editor = pref.edit();
        final SwitchCompat timeSwitch = (SwitchCompat) findViewById(R.id.time_switch);
        SwitchCompat fingerprintSwitch = (SwitchCompat) findViewById(R.id.fingerprint_switch);
        SwitchCompat fingerprintRegisterSwitch = (SwitchCompat) findViewById(R.id.fingerprint_register_switch);
        SwitchCompat fingerprintManagementSwitch = (SwitchCompat) findViewById(R.id.fingerprint_management_switch);
        SwitchCompat fingerprintSettingsSwitch = (SwitchCompat) findViewById(R.id.fingerprint_settings_switch);
        if (timeSwitch.isChecked() && startHour == endHour && startMinute == endMinute) {
            final OptionMaterialDialog mMaterialDialog = new OptionMaterialDialog(SettingsActivity.this);
            mMaterialDialog.setTitle(SettingsActivity.this.getString(R.string.settings_error)).setTitleTextColor(R.color.colorAccent).setTitleTextSize((float) 22.5)
                    .setMessage(SettingsActivity.this.getString(R.string.settings_timeError0)).setMessageTextSize((float) 16.5)
                    .setPositiveButton(SettingsActivity.this.getString(R.string.operation_ok), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mMaterialDialog.dismiss();

                        }
                    })
                    .setPositiveButtonTextColor(R.color.colorAccent)
                    .show();
            return;
        }
        if (timeSwitch.isChecked()) {
            editor.putBoolean("is_set_time", true);
        } else {
            editor.putBoolean("is_set_time", false);
        }
        if (fingerprintSwitch.isChecked() && (fingerprintRegisterSwitch.isChecked() || fingerprintManagementSwitch.isChecked() || fingerprintSettingsSwitch.isChecked())) {
            editor.putBoolean("is_set_fingerprint", true);
        } else {
            fingerprintSwitch.setChecked(false);
            editor.putBoolean("is_set_fingerprint", false);
        }
        editor.putInt("startHour", startHour);
        editor.putInt("startMinute", startMinute);
        editor.putInt("endHour", endHour);
        editor.putInt("endMinute", endMinute);
        editor.putBoolean("is_set_register_fingerprint", fingerprintRegisterSwitch.isChecked());
        editor.putBoolean("is_set_management_fingerprint", fingerprintManagementSwitch.isChecked());
        editor.putBoolean("is_set_settings_fingerprint", fingerprintSettingsSwitch.isChecked());
        editor.apply();
        Toast.makeText(SettingsActivity.this, R.string.settings_succeed, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(SettingsActivity.this, CameraActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 0:
                if (resultCode == RESULT_OK) {
                    fingerprintReturn = data.getBooleanExtra("fingerprint_return", false);
                } else {
                    finish();
                }
                break;
            default:
        }
    }

}
