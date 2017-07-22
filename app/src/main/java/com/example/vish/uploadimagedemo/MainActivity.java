package com.example.vish.uploadimagedemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by Deshario on 7/22/2017.
 */

public class MainActivity extends AppCompatActivity {
    Button btn_send,btn_get;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_send = (Button)findViewById(R.id.send_btn);
        btn_get = (Button)findViewById(R.id.get_btn);

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,Uploader.class));
            }
        });

        btn_get.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,Fetcher.class));
            }
        });

    }
}
