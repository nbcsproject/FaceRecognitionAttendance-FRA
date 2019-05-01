package com.android.fra;

import android.content.Intent;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;

import static org.litepal.LitePalApplication.getContext;

public class EditActivity extends BaseActivity {

    private Spinner spinner;
    private ArrayAdapter<String> adapter;
    private List<String> genderList;
    private String currentGender;

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

        uid_textView.setText(intent.getStringExtra("uid"));
        spinner = (Spinner) findViewById(R.id.spinner);
        genderList = new ArrayList<String>();
        genderList.add("男");
        genderList.add("女");
        adapter = new ArrayAdapter<String>(this, R.layout.gender_spinner, genderList);
        spinner.setAdapter(adapter);
        currentGender = intent.getStringExtra("gender");
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

        name_edit.setText(intent.getStringExtra("name"));
        phone_edit.setText(intent.getStringExtra("phone"));
        department_edit.setText(intent.getStringExtra("department"));
        post_edit.setText(intent.getStringExtra("post"));
        email_edit.setText(intent.getStringExtra("email"));
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
            Face face = new Face();
            face.setName(name_edit.getText().toString());
            face.setGender(currentGender);
            face.setPhone(phone_edit.getText().toString());
            face.setDepartment(department_edit.getText().toString());
            face.setPost(post_edit.getText().toString());
            face.setEmail(email_edit.getText().toString());
            face.updateAll("uid = ?", uid_textView.getText().toString());
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            Toast.makeText(getContext(), "修改成功", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPhoneValid(String phone) {
        return phone.length() == 11 && phone.matches("[0-9]+");
    }

}
