package com.android.fra;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
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

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.android.fra.ActivityCollector.finishAll;

public class LoginActivity extends BaseActivity {

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private AutoCompleteTextView mAccountView;
    private EditText mPasswordView;
    private CheckBox rememberPass;
    private TextView rememberPassTextView;
    private ProgressDialog progressDialog;

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
        mPasswordView.setLongClickable(false);
        mPasswordView.setTextIsSelectable(false);

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
                /*注册的方法写在这里*/
            }
        });

        rememberPass = (CheckBox) findViewById(R.id.remember_pass);
        rememberPassTextView = (TextView) findViewById(R.id.remember_pass_textView);
        rememberPassTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rememberPass.isChecked()) {
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
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(this.getString(R.string.login_loggingIn));
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
    }

    private void hideProgress() {
        progressDialog.hide();
    }

    private void attemptLogin() {

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
            doInBackground();
        }
    }

    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    editor = pref.edit();
                    editor.putBoolean("isLogin", true);
                    editor.putString("account", mAccountView.getText().toString());
                    if (rememberPass.isChecked()) {
                        editor.putBoolean("remember_password", true);
                        editor.putString("password", mPasswordView.getText().toString());
                    } else {
                        editor.putBoolean("remember_password", false);
                        editor.putString("password", null);
                    }
                    editor.apply();
                    finish();
                    Intent intent = new Intent(LoginActivity.this, CameraActivity.class);
                    startActivity(intent);
                    break;
                case 1:
                    Toast.makeText(LoginActivity.this, R.string.login_error_ap, Toast.LENGTH_SHORT).show();
                    hideProgress();
                    break;
                case 2:
                    Toast.makeText(LoginActivity.this, R.string.login_failed, Toast.LENGTH_SHORT).show();
                    hideProgress();
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    private void doInBackground() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(8, TimeUnit.SECONDS)
                        .readTimeout(8, TimeUnit.SECONDS)
                        .build();
                RequestBody requestBody = new FormBody.Builder()
                        .add("account", mAccountView.getText().toString())
                        .add("password", md5(md5(mPasswordView.getText().toString())))
                        .build();
                Request request = new Request.Builder()
                        .url("http://10.10.19.134:3000/app/login")
                        .post(requestBody)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    Message msg = new Message();
                    if (!responseData.equals("no") && !responseData.equals("error")) {
                        editor = pref.edit();
                        editor.putString("currentPid", responseData);
                        editor.apply();
                        msg.what = 0;
                    } else if (responseData.equals("no")) {
                        msg.what = 1;
                    } else if (responseData.equals("error")) {
                        msg.what = 2;
                    }
                    mHandler.sendMessage(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                    if (e instanceof SocketTimeoutException) {
                        Message msg = new Message();
                        msg.what = 2;
                        mHandler.sendMessage(msg);
                    }
                    if (e instanceof ConnectException) {
                        Message msg = new Message();
                        msg.what = 2;
                        mHandler.sendMessage(msg);
                    }
                }
            }
        }).start();
    }

    @NonNull
    private String md5(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            StringBuilder result = new StringBuilder();
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result.append(temp);
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finishAll();
        }
        return super.onKeyDown(keyCode, event);
    }

}
