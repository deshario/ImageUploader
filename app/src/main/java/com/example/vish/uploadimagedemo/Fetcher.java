package com.example.vish.uploadimagedemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Deshario on 7/22/2017.
 */

public class Fetcher extends AppCompatActivity {

    EditText name;
    Button ViewImage;
    String url = null;
    NetworkImageView previewImage;
    ImageLoader imageLoader;
    String fetch_url = "http://192.168.1.51/deshario/fetch_image.php";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view);

        previewImage = (NetworkImageView) findViewById(R.id.show_img);
        name = (EditText) findViewById(R.id.img_name);
        ViewImage = (Button) findViewById(R.id.fetch_btn);

        imageLoader = VolleyRequest.getInstance(Fetcher.this).getImageLoader();
        ViewImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetch_image();
            }
        });
    }

    public void fetch_image() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST,fetch_url,
                new Response.Listener<String> () {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject res = new JSONObject(response);
                                JSONArray thread = res.getJSONArray("image");
                                // System.out.println("Response :: "+res);
                                // System.out.println("thread :: "+thread);
                                    for (int i = 0; i < thread.length(); i++) {
                                        JSONObject obj = thread.getJSONObject(i);
                                        url = obj.getString("photo");
                                    }
                                imageLoader.get(url, ImageLoader.getImageListener(previewImage, 0, android.R.drawable.ic_dialog_alert));
                                previewImage.setImageUrl(url, imageLoader);
                            }catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            System.out.println("Error : "+error);
                        }
                    })
            {
                @Override
                protected Map<String,String> getParams() throws AuthFailureError {
                    Map<String,String> params = new HashMap<>();
                    params.put("name", name.getText().toString());
                    return params;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(Fetcher.this);
            requestQueue.add(stringRequest);
    }
}