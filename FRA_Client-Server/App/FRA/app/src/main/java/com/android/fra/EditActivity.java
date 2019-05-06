package com.android.fra;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.fra.db.Face;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static org.litepal.LitePalApplication.getContext;

public class EditActivity extends BaseActivity {

    private Spinner spinner;
    private ArrayAdapter<String> adapter;
    private List<String> genderList;
    private String currentGender;
    private Face editFace;
    private ProgressDialog progressDialog;
    private String uid;
    private String name;
    private String gender;
    private String phone;
    private String department;
    private String post;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.edit_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("编辑信息");
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        ImageView imageView = (ImageView) findViewById(R.id.edit_image_view);
        int resource = R.drawable.edit_image;
        Glide.with(this).load(resource).into(imageView);

        final TextView uid_textView = (TextView) findViewById(R.id.uid_textView);
        final EditText name_edit = (EditText) findViewById(R.id.name_edit);
        final EditText phone_edit = (EditText) findViewById(R.id.phone_edit);
        final EditText department_edit = (EditText) findViewById(R.id.department_edit);
        final EditText post_edit = (EditText) findViewById(R.id.post_edit);
        final EditText email_edit = (EditText) findViewById(R.id.email_edit);
        Intent intent = getIntent();

        uid = intent.getStringExtra("uid");
        uid_textView.setText(uid);
        spinner = (Spinner) findViewById(R.id.spinner);
        genderList = new ArrayList<String>();
        genderList.add("男");
        genderList.add("女");
        adapter = new ArrayAdapter<String>(this, R.layout.gender_spinner, genderList);
        spinner.setAdapter(adapter);
        gender = intent.getStringExtra("gender");
        currentGender = gender;
        if (intent.getStringExtra("gender").equals("male")) {
            spinner.setSelection(0);
        } else {
            spinner.setSelection(1);
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (genderList.get(position).equals("男")) {
                    currentGender = "male";
                } else {
                    currentGender = "female";
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        name = intent.getStringExtra("name");
        phone = intent.getStringExtra("phone");
        department = intent.getStringExtra("department");
        post = intent.getStringExtra("post");
        email = intent.getStringExtra("email");
        name_edit.setText(name);
        phone_edit.setText(phone);
        department_edit.setText(department);
        post_edit.setText(post);
        email_edit.setText(email);
        if (intent.getStringExtra("post") != null && !intent.getStringExtra("post").equals("") && !intent.getStringExtra("post").equals("null")) {
            post_edit.setText(intent.getStringExtra("post"));
        } else {
            post_edit.setHint("职务");
        }
        if (intent.getStringExtra("email") != null && !intent.getStringExtra("email").equals("") && !intent.getStringExtra("email").equals("null")) {
            email_edit.setText(intent.getStringExtra("email"));
        } else {
            email_edit.setHint("邮箱");
        }

        ImageButton saveEdit = (ImageButton) findViewById(R.id.save_edit);
        saveEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveEdit();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveEdit() {

        TextView uid_textView = (TextView) findViewById(R.id.uid_textView);
        EditText name_edit = (EditText) findViewById(R.id.name_edit);
        EditText phone_edit = (EditText) findViewById(R.id.phone_edit);
        EditText department_edit = (EditText) findViewById(R.id.department_edit);
        EditText post_edit = (EditText) findViewById(R.id.post_edit);
        EditText email_edit = (EditText) findViewById(R.id.email_edit);

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(department_edit.getText().toString())) {
            department_edit.setError(getString(R.string.error_field_required));
            focusView = department_edit;
            cancel = true;
        }

        if (TextUtils.isEmpty(phone_edit.getText().toString())) {
            phone_edit.setError(getString(R.string.error_field_required));
            focusView = phone_edit;
            cancel = true;
        } else if (!isPhoneValid(phone_edit.getText().toString())) {
            phone_edit.setError(getString(R.string.error_invalid_phone));
            focusView = phone_edit;
            cancel = true;
        }

        if (TextUtils.isEmpty(name_edit.getText().toString())) {
            name_edit.setError(getString(R.string.error_field_required));
            focusView = name_edit;
            cancel = true;
        }

        if (!TextUtils.isEmpty(email_edit.getText().toString()) && !isEmailValid(email_edit.getText().toString())) {
            email_edit.setError(getString(R.string.error_invalid_email));
            focusView = email_edit;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            if (!name.equals(name_edit.getText().toString()) || !gender.equals(currentGender) || !phone.equals(phone_edit.getText().toString())
                    || !department.equals(department_edit.getText().toString()) || !post.equals(post_edit.getText().toString()) || !email.equals(email_edit.getText().toString())) {
                showProgress();
                editFace = new Face();
                editFace.setUid(uid_textView.getText().toString());
                editFace.setName(name_edit.getText().toString());
                editFace.setGender(currentGender);
                editFace.setPhone(phone_edit.getText().toString());
                editFace.setDepartment(department_edit.getText().toString());
                editFace.setPost(post_edit.getText().toString());
                editFace.setEmail(email_edit.getText().toString());
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                int second = calendar.get(Calendar.SECOND);
                String Month;
                if (month < 10) {
                    Month = "0" + String.valueOf(month);
                } else {
                    Month = String.valueOf(month);
                }
                String Day;
                if (day < 10) {
                    Day = "0" + String.valueOf(day);
                } else {
                    Day = String.valueOf(day);
                }
                String Hour;
                if (hour < 10) {
                    Hour = "0" + String.valueOf(hour);
                } else {
                    Hour = String.valueOf(hour);
                }
                String Minute;
                if (minute < 10) {
                    Minute = "0" + String.valueOf(minute);
                } else {
                    Minute = String.valueOf(minute);
                }
                String Second;
                if (second < 10) {
                    Second = "0" + String.valueOf(second);
                } else {
                    Second = String.valueOf(second);
                }
                String modTime = String.valueOf(year) + "." + String.valueOf(Month) + "." + String.valueOf(Day) + " " + String.valueOf(Hour) + ":" + String.valueOf(Minute) + ":" + String.valueOf(Second);
                editFace.setModTime(modTime);
                editInBackground();
            } else {
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    }

    private void showProgress() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("保存修改的信息中");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
    }

    private void hideProgress() {
        progressDialog.hide();
    }

    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    TextView uid_textView = (TextView) findViewById(R.id.uid_textView);
                    editFace.updateAll("uid = ?", uid_textView.getText().toString());
                    hideProgress();
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    Toast.makeText(getContext(), "修改成功", Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case 1:
                    hideProgress();
                    Toast.makeText(getContext(), "未连接至服务器", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    private void editInBackground() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String faceJSON = gson.toJson(editFace);
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), faceJSON);
                Request request = new Request.Builder()
                        .url("http://10.10.19.134:3000/app/update")
                        .post(requestBody)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    Message msg = new Message();
                    if (responseData.equals("ok")) {
                        msg.what = 0;
                    } else {
                        msg.what = 1;
                    }
                    mHandler.sendMessage(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                    if (e instanceof SocketTimeoutException) {
                        Message msg = new Message();
                        msg.what = 1;
                        mHandler.sendMessage(msg);
                    }
                    if (e instanceof ConnectException) {
                        Message msg = new Message();
                        msg.what = 1;
                        mHandler.sendMessage(msg);
                    }
                }
            }
        }).start();
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPhoneValid(String phone) {
        return phone.length() == 11 && phone.matches("[0-9]+");
    }

}
