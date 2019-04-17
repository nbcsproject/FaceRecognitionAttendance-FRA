package com.android.fra;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.RadioGroup;

import com.android.fra.db.Face;
import com.bumptech.glide.Glide;
import com.longsh.optionframelibrary.OptionMaterialDialog;

public class RegisterActivity extends AppCompatActivity {

    private RegisterActivity.UserRegisterTask mAuthTask = null;

    private AutoCompleteTextView mNameView;
    private String mGenderChoose = "female";
    private AutoCompleteTextView mPhoneView;
    private AutoCompleteTextView mEmailView;
    private AutoCompleteTextView mDepartmentView;
    private AutoCompleteTextView mPostView;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        setContentView(R.layout.activity_register);
        String RegisterName = "注册";
        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        CollapsingToolbarLayout collapsingToolbar=(CollapsingToolbarLayout)findViewById(R.id.collapsing_toolbar);
        ImageView imageView=(ImageView)findViewById(R.id.image_view);
        setSupportActionBar(toolbar);
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        collapsingToolbar.setTitle(RegisterName);
        int resource = R.drawable.register_image;
        Glide.with(this).load(resource).into(imageView);

        final RadioGroup mRadioGroup = (RadioGroup) findViewById(R.id.selectGender);
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                if(checkedId==R.id.male){
                    mGenderChoose = "male";
                }else {
                    mGenderChoose = "female";
                }
            }
        });
        FloatingActionButton commitButton = (FloatingActionButton)findViewById(R.id.button_commit);
        commitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSubmit();
            }
        });

        mNameView = (AutoCompleteTextView) findViewById(R.id.textView_name);
        mPhoneView = (AutoCompleteTextView) findViewById(R.id.textView_phone);
        mEmailView = (AutoCompleteTextView) findViewById(R.id.textView_email);
        mDepartmentView = (AutoCompleteTextView) findViewById(R.id.textView_department);
        mPostView = (AutoCompleteTextView) findViewById(R.id.textView_post);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showProgress() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("信息上传中");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
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
        }else if(!isPhoneValid(phone)){
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
                Face face=new Face();
                face.setName(mAuthTask.mName);
                face.setGender(mGenderChoose);
                face.setPhone(mAuthTask.mPhone);
                face.setEmail(mAuthTask.mEmail);
                face.setDepartment(mAuthTask.mDepartment);
                face.setPost(mAuthTask.mPost);
                face.setUid("10001");
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
                OptionMaterialDialog mMaterialDialog = new OptionMaterialDialog(RegisterActivity.this);
                mMaterialDialog.setTitle("上传成功").setMessage("信息已上传")
                        .setPositiveButton("确定", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                finish();
                            }
                        })
                        .setPositiveButtonTextColor(R.color.colorAccent)
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
    }

}
