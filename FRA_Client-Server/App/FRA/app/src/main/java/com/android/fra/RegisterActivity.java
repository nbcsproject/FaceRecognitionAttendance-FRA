package com.android.fra;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.android.fra.db.Face;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.longsh.optionframelibrary.OptionMaterialDialog;

import org.litepal.LitePal;

import java.util.List;

import static com.android.fra.ActivityCollector.finishAll;
import static org.litepal.LitePalApplication.getContext;

public class RegisterActivity extends BaseActivity {

    private RegisterActivity.UserRegisterTask mAuthTask = null;

    private SharedPreferences pref;
    private DrawerLayout mDrawerLayout;
    private AutoCompleteTextView mNameView;
    private String mGenderChoose = "female";
    private AutoCompleteTextView mPhoneView;
    private AutoCompleteTextView mEmailView;
    private AutoCompleteTextView mDepartmentView;
    private AutoCompleteTextView mPostView;
    private ProgressDialog progressDialog;
    private String currentUid;
    private static boolean fingerprintReturn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        if (!pref.getBoolean("isLogin", false)) {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finishAll();
        } else {
            if (pref.getBoolean("is_set_fingerprint", false) && pref.getBoolean("is_set_register_fingerprint", false) && !fingerprintReturn) {
                Intent intent = new Intent(RegisterActivity.this, FingerprintActivity.class);
                startActivityForResult(intent, 0);
            }
            setContentView(R.layout.activity_register);
            Toolbar toolbar = (Toolbar) findViewById(R.id.register_activity_toolBar);
            ImageView imageView = (ImageView) findViewById(R.id.image_view);
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("注册");
            NavigationView navView = (NavigationView) findViewById(R.id.register_activity_nav_view);
            mDrawerLayout = (DrawerLayout) findViewById(R.id.register_activity_drawer_layout);
            View headerLayout = navView.inflateHeaderView(R.layout.nav_header);
            ImageView drawerImageView = (ImageView) headerLayout.findViewById(R.id.nav_header_image);
            TextView navTextView = (TextView) headerLayout.findViewById(R.id.nav_account);
            navView.setCheckedItem(R.id.nav_register);
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
                            Intent cameraIntent = new Intent(RegisterActivity.this, CameraActivity.class);
                            cameraIntent.putExtra("capture_mode", 0);
                            startActivity(cameraIntent);
                            break;
                        case R.id.nav_register:
                            break;
                        case R.id.nav_management:
                            Intent managementIntent = new Intent(RegisterActivity.this, ManagementActivity.class);
                            startActivity(managementIntent);
                            break;
                        case R.id.nav_settings:
                            Intent settingsIntent = new Intent(RegisterActivity.this, SettingsActivity.class);
                            startActivity(settingsIntent);
                            break;
                        default:
                    }
                    return true;
                }
            });
            int resource = R.drawable.register_image;
            Glide.with(this).load(resource).into(imageView);

            final RadioGroup mRadioGroup = (RadioGroup) findViewById(R.id.selectGender);
            mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                    if (checkedId == R.id.male) {
                        mGenderChoose = "male";
                    } else {
                        mGenderChoose = "female";
                    }
                }
            });
            FloatingActionButton commitButton = (FloatingActionButton) findViewById(R.id.button_commit);
            commitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
                    attemptSubmit();
                }
            });

            mNameView = (AutoCompleteTextView) findViewById(R.id.textView_name);
            mPhoneView = (AutoCompleteTextView) findViewById(R.id.textView_phone);
            mEmailView = (AutoCompleteTextView) findViewById(R.id.textView_email);
            mDepartmentView = (AutoCompleteTextView) findViewById(R.id.textView_department);
            mPostView = (AutoCompleteTextView) findViewById(R.id.textView_post);
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

    private void showProgress() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("信息上传中");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
    }

    private void hideProgress() {
        progressDialog.dismiss();
    }

    private void attemptSubmit() {
        if (mAuthTask != null) {
            return;
        }

        mNameView.setError(null);
        mPhoneView.setError(null);
        mEmailView.setError(null);
        mDepartmentView.setError(null);
        mPostView.setError(null);

        String name = mNameView.getText().toString();
        String phone = mPhoneView.getText().toString();
        String email = mEmailView.getText().toString();
        String department = mDepartmentView.getText().toString();
        String post = mPostView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(department)) {
            mDepartmentView.setError(getString(R.string.error_field_required));
            focusView = mDepartmentView;
            cancel = true;
        }

        if (TextUtils.isEmpty(phone)) {
            mPhoneView.setError(getString(R.string.error_field_required));
            focusView = mPhoneView;
            cancel = true;
        } else if (!isPhoneValid(phone)) {
            mPhoneView.setError(getString(R.string.error_invalid_phone));
            focusView = mPhoneView;
            cancel = true;
        }

        if (TextUtils.isEmpty(name)) {
            mNameView.setError(getString(R.string.error_field_required));
            focusView = mNameView;
            cancel = true;
        }

        if (!TextUtils.isEmpty(email) && !isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress();
            mAuthTask = new RegisterActivity.UserRegisterTask(name, phone, email, department, post);
            mAuthTask.execute((Void) null);
        }

    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPhoneValid(String phone) {
        return phone.length() == 11 && phone.matches("[0-9]+");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 0:
                if (resultCode == RESULT_OK) {
                    fingerprintReturn = data.getBooleanExtra("fingerprint_return", false);
                }else{
                    finish();
                }
                break;
            default:
        }
    }

    public class UserRegisterTask extends AsyncTask<Void, Void, Boolean> {

        private final String mName;
        private final String mPhone;
        private final String mEmail;
        private final String mDepartment;
        private final String mPost;

        UserRegisterTask(String name, String phone, String email, String department, String post) {
            mName = name;
            mPhone = phone;
            mEmail = email;
            mDepartment = department;
            mPost = post;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                currentUid = CalculateUid();
                Face face = new Face();
                face.setName(mAuthTask.mName);
                face.setGender(mGenderChoose);
                face.setPhone(mAuthTask.mPhone);
                face.setEmail(mAuthTask.mEmail);
                face.setDepartment(mAuthTask.mDepartment);
                face.setPost(mAuthTask.mPost);
                face.setUid(currentUid);
                face.setValid(true);
                face.save();
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;

            if (success) {
                hideProgress();
                final OptionMaterialDialog mMaterialDialog = new OptionMaterialDialog(RegisterActivity.this);
                mMaterialDialog.setTitle("上传成功").setTitleTextColor(R.color.colorPrimary).setMessage("信息已上传")
                        .setPositiveButton("下一步", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(RegisterActivity.this, FaceRecordInfo.class);
                                intent.putExtra("current_uid", currentUid);
                                startActivity(intent);
                                mMaterialDialog.dismiss();
                            }
                        })
                        .setPositiveButtonTextColor(R.color.colorPrimary)
                        .setCanceledOnTouchOutside(false)
                        .setOnDismissListener(
                                new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {
                                    }
                                })
                        .show();
            } else {
                /*注册失败后的方法写在这里*/
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }

        private String CalculateUid() {
            List<Face> uidList = LitePal.select("uid").find(Face.class);
            int uidTemp = 100000;
            for (Face uid : uidList) {
                int uidInteger = Integer.parseInt(uid.getUid());
                if (uidInteger > uidTemp) {
                    uidTemp = uidInteger;
                }
            }
            return Integer.toString(++uidTemp);
        }

    }

}
