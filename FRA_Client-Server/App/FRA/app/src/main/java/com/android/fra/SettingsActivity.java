package com.android.fra;

import android.animation.ValueAnimator;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        if (!pref.getBoolean("isLogin", false)) {
            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            startActivity(intent);
            finishAll();
        } else {
            setContentView(R.layout.activity_settings);
            Toolbar toolbar = (Toolbar) findViewById(R.id.settings_activity_toolBar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("设置");

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
            boolean isEggOn = pref.getBoolean("is_egg_on", false);
            if (isEggOn == true) {
                menuItem.setVisible(true);
            } else {
                menuItem.setVisible(false);
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
            if (isSetTime == true) {
                timeSwitch.setChecked(true);
                timeChoose.setVisibility(View.VISIBLE);
                isTimeOpen.setText("已开启");
            } else {
                timeSwitch.setChecked(false);
                timeChoose.setVisibility(View.GONE);
                isTimeOpen.setText("未开启");
            }
            timeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        timeChoose.measure(0, 0);
                        final int height = timeChoose.getMeasuredHeight();
                        show(timeChoose, height);
                        isTimeOpen.setText("已开启");
                    } else {
                        timeChoose.measure(0, 0);
                        final int height = timeChoose.getMeasuredHeight();
                        dismiss(timeChoose, height);
                        isTimeOpen.setText("未开启");
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
                            showTime(hour, minute, beginTimeButton);
                            startHour = hour;
                            startMinute = minute;
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
                                mMaterialDialog.setTitle("设置错误").setTitleTextColor(R.color.colorAccent).setTitleTextSize((float) 22.5)
                                        .setMessage("请设置一个大于开始时间的结束时间").setMessageTextSize((float) 16.5)
                                        .setPositiveButton("确定", new View.OnClickListener() {
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
                    Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    editor = pref.edit();
                    editor.putBoolean("isLogin", false);
                    editor.apply();
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
        if (timeSwitch.isChecked() == true) {
            editor.putBoolean("is_set_time", true);
        } else {
            editor.putBoolean("is_set_time", false);
        }
        editor.putInt("startHour", startHour);
        editor.putInt("startMinute", startMinute);
        editor.putInt("endHour", endHour);
        editor.putInt("endMinute", endMinute);
        editor.apply();
        Toast.makeText(getContext(), "设置成功", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(SettingsActivity.this, CameraActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

}
