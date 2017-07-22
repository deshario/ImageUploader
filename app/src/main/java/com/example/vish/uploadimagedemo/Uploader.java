package com.example.vish.uploadimagedemo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
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

public class Uploader extends AppCompatActivity {
    EditText name;
    ImageView imageView;
    Button pickImage, upload;
    private Bitmap bitmap;
    private String KEY_IMAGE = "image";
    private String KEY_NAME = "name";
    private int PICK_IMAGE_REQUEST = 1;
    private String UPLOAD_URL = "http://192.168.1.51/deshario/upload_image.php";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload);

        name = (EditText) findViewById(R.id.img_name);
        imageView = (ImageView) findViewById(R.id.preview_img);
        pickImage = (Button) findViewById(R.id.select_img);
        upload = (Button) findViewById(R.id.upload_btn);

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (name.getText().toString().length() <= 0) {
                    name.setError("Please Enter Name !");
                } else if (bitmap == null) {
                    Toast.makeText(Uploader.this, "Please Upload Image", Toast.LENGTH_SHORT).show();
                } else {
                    uploadImage();
                }
            }
        });
        pickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileChooser();
            }
        });
    }


    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                imageView.setVisibility(View.VISIBLE);
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void uploadImage() {
        final ProgressDialog loading = ProgressDialog.show(Uploader.this, "Uploading...", "Please wait...", false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, UPLOAD_URL,
                new Response.Listener < String > () {
                    @Override
                    public void onResponse(String s) {
                        loading.dismiss();
                        Toast.makeText(Uploader.this, s, Toast.LENGTH_LONG).show();
                        name.setText("");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        loading.dismiss();
                        Toast.makeText(Uploader.this, volleyError.getMessage().toString(), Toast.LENGTH_LONG).show();
                    }
                })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                String image = getStringImage(bitmap); // Converting Bitmap to String
                String name1 = name.getText().toString().trim(); // Getting Image Name
                Map<String,String> params = new Hashtable<String,String> ();
                params.put(KEY_IMAGE, image);
                params.put(KEY_NAME, name1);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(Uploader.this);
        requestQueue.add(stringRequest);
    }
}