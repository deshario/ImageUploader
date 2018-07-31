package com.example.vish.uploadimagedemo;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created by Deshario on 7/22/2017.
 */

public class UploadActivity extends AppCompatActivity {
    EditText name;
    ImageView imageView;
    Button btn_gallery, btn_camera, btn_upload;
    private String KEY_IMAGE = "image";
    private String KEY_NAME = "name";
    private Bitmap bitmap;
    private final int CAMERA_N_WRITE_PERMISSIONS = 99;
    private final int PICK_IMAGE_CAMERA = 1;
    private final int PICK_IMAGE_GALLERY = 2;
    private final String[] PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    private String UPLOAD_URL = "http://192.168.1.41/deshario/upload_image.php";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_layout);

        name = (EditText) findViewById(R.id.img_name);
        imageView = (ImageView) findViewById(R.id.preview_img);
        btn_gallery = (Button) findViewById(R.id.select_img);
        btn_upload = (Button) findViewById(R.id.upload_btn);

        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitData();
            }
        });

        btn_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ManagePermission();
            }
        });
    }

    private void ManagePermission() {
        int MyVersion = Build.VERSION.SDK_INT;
        if (MyVersion >= Build.VERSION_CODES.M) {
            check_Permission();
        } else {
            access_granted();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void check_Permission(){
        int write_access = ContextCompat.checkSelfPermission(UploadActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int camera_access = ContextCompat.checkSelfPermission(UploadActivity.this, Manifest.permission.CAMERA);
        if (write_access == PackageManager.PERMISSION_GRANTED && camera_access == PackageManager.PERMISSION_GRANTED) { // result = 0
            access_granted();
        } else { // result = -1
            requestPermissions(PERMISSIONS, CAMERA_N_WRITE_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_N_WRITE_PERMISSIONS:
                if (grantResults.length > 0) {
                    boolean cameraGranted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean writeExternalFileGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraGranted && writeExternalFileGranted) {
                        access_granted();
                    } else {
                        Toast.makeText(UploadActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    private void access_granted() {
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(UploadActivity.this);
        pictureDialog.setTitle("Image Chooser");
        String[] pictureDialogItems = {"Gallery", "Camera" };
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                choosePhotoFromGallary();
                                break;
                            case 1:
                                takePhotoFromCamera();
                                break;
                        }
                    }
                });
        pictureDialog.show();
    }

    public void choosePhotoFromGallary() {
        Intent choosePictureIntent = new Intent();
        choosePictureIntent.setType("image/*");
        choosePictureIntent.setAction(Intent.ACTION_GET_CONTENT);
        if(choosePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(choosePictureIntent, PICK_IMAGE_GALLERY);
        }
    }

    private void takePhotoFromCamera(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, PICK_IMAGE_CAMERA);
        }
    }

    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    // GALLERY CAMERA REULT
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case PICK_IMAGE_GALLERY:
                if(resultCode == RESULT_OK && data != null && data.getData() != null){
                    Uri filePath = data.getData();
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                        imageView.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case PICK_IMAGE_CAMERA:
                if(resultCode == RESULT_OK && data != null && data.getExtras() != null){
                    bitmap = (Bitmap) data.getExtras().get("data");
                    imageView.setImageBitmap(bitmap);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void submitData(){
        if (name.getText().toString().length() <= 0) {
            name.setError("Please Enter Name !");
        } else if (bitmap == null) {
            Toast.makeText(UploadActivity.this, "Please Select Image", Toast.LENGTH_SHORT).show();
        } else {
            uploadImage();
        }
    }

    private void uploadImage() {
        final ProgressDialog loading = ProgressDialog.show(UploadActivity.this, "Uploading...", "Please wait...", false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, UPLOAD_URL,
                new Response.Listener < String > () {
                    @Override
                    public void onResponse(String s) {
                        loading.dismiss();
                        Toast.makeText(UploadActivity.this, s, Toast.LENGTH_LONG).show();
                        name.setText("");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        loading.dismiss();
                        Toast.makeText(UploadActivity.this, volleyError.getMessage().toString(), Toast.LENGTH_LONG).show();
                    }
                })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                String image = getStringImage(bitmap); // Converting Bitmap to String
                String name1 = name.getText().toString().trim(); // Getting Image Name
                Map<String,String> params = new Hashtable<String,String>();
                params.put(KEY_IMAGE, image);
                params.put(KEY_NAME, name1);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(UploadActivity.this);
        requestQueue.add(stringRequest);
    }

}