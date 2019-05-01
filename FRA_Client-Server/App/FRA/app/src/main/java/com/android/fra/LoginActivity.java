package com.android.fra;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import static com.android.fra.ActivityCollector.finishAll;
import static org.litepal.LitePalApplication.getContext;

public class LoginActivity extends BaseActivity {

    private UserLoginTask mAuthTask = null;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private AutoCompleteTextView mAccountView;
    private EditText mPasswordView;
    private CheckBox rememberPass;
    private TextView rememberPassTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        setContentView(R.layout.activity_login);
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        ImageView imageView = (ImageView) findViewById(R.id.login_image_view);
        int resource = R.drawable.login_image;
        Glide.with(this).load(resource).into(imageView);

        mAccountView = (AutoCompleteTextView) findViewById(R.id.textView_login_account);
        mPasswordView = (EditText) findViewById(R.id.textView_login_password);

        Button loginButton = (Button) findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        Button loginRegisterButton = (Button) findViewById(R.id.login_register_button);
        loginRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "点击了注册按钮", Toast.LENGTH_SHORT).show();
                /*注册的方法写在这里*/
            }
        });

        rememberPass = (CheckBox) findViewById(R.id.remember_pass);
        rememberPassTextView = (TextView) findViewById(R.id.remember_pass_textView);
        rememberPassTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rememberPass.isChecked() == true) {
                    rememberPass.setChecked(false);
                } else {
                    rememberPass.setChecked(true);
                }
            }
        });

        boolean isRemember = pref.getBoolean("remember_password", false);
        if (isRemember) {
            String account = pref.getString("account", "");
            String password = pref.getString("password", "");
            mAccountView.setText(account);
            mPasswordView.setText(password);
            rememberPass.setChecked(true);
        }
    }

    private void showProgress() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("登录中");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
    }

    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        mAccountView.setError(null);
        mPasswordView.setError(null);

        String account = mAccountView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.login_error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(account)) {
            mAccountView.setError(getString(R.string.login_error_field_required));
            focusView = mAccountView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress();
            mAuthTask = new UserLoginTask(account, password);
            mAuthTask.execute((Void) null);
        }
    }

    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                // Simulate network access.
                Thread.sleep(2000);
                /*比较用户名和密码*/
            } catch (InterruptedException e) {
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;

            if (success) {
                editor = pref.edit();
                editor.putBoolean("isLogin", true);
                editor.putString("account", mAccountView.getText().toString());
                if (rememberPass.isChecked()) {
                    editor.putBoolean("remember_password", true);
                    editor.putString("password", mPasswordView.getText().toString());
                } else {
                    editor.clear();
                }
                editor.apply();
                finish();
                Intent intent = new Intent(LoginActivity.this, CameraActivity.class);
                startActivity(intent);
            } else {
                /*登录失败后的方法写在这里*/
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finishAll();
        }
        return super.onKeyDown(keyCode, event);
    }

}
