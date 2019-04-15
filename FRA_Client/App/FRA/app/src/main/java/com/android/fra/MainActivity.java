package com.android.fra;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.android.fra.db.Face;
import com.longsh.optionframelibrary.OptionMaterialDialog;

import org.litepal.LitePal;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import static android.os.Environment.DIRECTORY_DCIM;

public class MainActivity extends AppCompatActivity {

    private Uri imageUri;
    public static final int TAKE_PHOTO=1;
    private ImageView picture;

    // Used to load the 'native-lib' library on application startup.
    //static {
        //System.loadLibrary("native-lib");
    //}
    static {
        if(!OpenCVLoader.initDebug())
        {
            Log.d("MainActivity","初始化失败");
        }else{
            Log.d("MainActivity","初始化成功");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button recognize=(Button)findViewById(R.id.recognition_button);
        recognize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                }else{
                    fun();
                }
            }
        });
        Button register=(Button)findViewById(R.id.register_button);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String path3= Environment.getExternalStoragePublicDirectory(DIRECTORY_DCIM)+"/Camera/3.jpg";
                List<Face> faces = LitePal.findAll(Face.class);
                double minDist = java.lang.Double.MAX_VALUE;
                int minClass = -1;
                int count=0;

                for (Face face : faces) {
                    double dist = new LBP().comparedist(path3, face.getFeature(),1,1);
                    if (dist < minDist) {
                        minDist = dist;
                        minClass = count;
                        count++;
                    }
                }
                int index=0;
                for (Face face : faces) {
                    if(index==minClass) {
                        final OptionMaterialDialog mMaterialDialog = new OptionMaterialDialog(MainActivity.this);
                        mMaterialDialog.setTitle("签到成功").setMessage("工号: "+face.getUid()+"\n"+"姓名: "+face.getName()+"\n"
                                +"性别: "+face.getGender()+"\n"+"部门: "+face.getDepartment()).setMessageTextSize((float) 16.5)
                                .setPositiveButton("确定", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mMaterialDialog.dismiss();
                                    }
                                })
                                .setPositiveButtonTextColor(R.color.colorAccent)
                                .setCanceledOnTouchOutside(false)
                                .setOnDismissListener(
                                        new DialogInterface.OnDismissListener() {
                                            @Override
                                            public void onDismiss(DialogInterface dialog) {
                                                //对话框消失后回调
                                            }
                                        })
                                .show();
                        Log.d("MainActivity", "Id is: " + face.getUid());
                        Log.d("MainActivity", "Department is: " + face.getDepartment());
                        Log.d("MainActivity", "Name is: " + face.getName());
                        Log.d("MainActivity", "Gender is: " + face.getGender());
                        break;
                    }
                    index++;
                }
                Log.d("MainActivity", "index is: " + index);
            }
        });
        Button cameracapture=(Button)findViewById(R.id.camera_button);
        cameracapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                startActivity(intent);
            }
        });

        Button register_test = (Button)findViewById(R.id.register_test);
        register_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        Log.e("MainActivity", "onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        switch (requestCode){
            case TAKE_PHOTO:
                if(resultCode==RESULT_OK){
                    try{
                        Bitmap bitmap= BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        picture.setImageBitmap(bitmap);
                    }catch (FileNotFoundException e){
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions, int[] grantResults){
        switch (requestCode){
            case 1:
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    fun();
                }else {
                    Toast.makeText(this,"You denied the permission",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    private void fun(){
        String path1 = Environment.getExternalStoragePublicDirectory(DIRECTORY_DCIM)+"/Camera/1.jpg";
        String path2 = Environment.getExternalStoragePublicDirectory(DIRECTORY_DCIM)+"/Camera/2.jpg";
        String feature1=new LBP().getfeature(path1,1,1);
        String feature2=new LBP().getfeature(path2,1,1);

        LitePal.getDatabase();
        Face face1=new Face();
        face1.setName("Alex");
        face1.setGender("Male");
        face1.setUid("101");
        face1.setDepartment("技术");
        face1.setFeature(feature1);
        face1.save();
        Face face2=new Face();
        face2.setName("Ariana");
        face2.setGender("Female");
        face2.setUid("102");
        face2.setFeature(feature2);
        face2.save();
    }
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
